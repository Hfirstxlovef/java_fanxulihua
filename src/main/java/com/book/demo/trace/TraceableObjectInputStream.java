package com.book.demo.trace;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TraceableObjectInputStream extends ObjectInputStream {
    
    private final List<DeserializationStep> executionTrace;
    private final AtomicInteger stepCounter;
    private final boolean enableVerboseTrace;
    private final StringBuilder traceBuffer;
    
    // 教育模式相关字段
    private final boolean educationMode;
    private final List<String> educationalSteps;
    private final StringBuilder educationLog;
    
    public TraceableObjectInputStream(InputStream in) throws IOException {
        this(in, true, false);
    }
    
    public TraceableObjectInputStream(InputStream in, boolean enableVerbose) throws IOException {
        this(in, enableVerbose, false);
    }
    
    public TraceableObjectInputStream(InputStream in, boolean enableVerbose, boolean educationMode) throws IOException {
        super(in);
        this.executionTrace = new ArrayList<>();
        this.stepCounter = new AtomicInteger(0);
        this.enableVerboseTrace = enableVerbose;
        this.traceBuffer = new StringBuilder();
        this.educationMode = educationMode;
        this.educationalSteps = new ArrayList<>();
        this.educationLog = new StringBuilder();
        
        if (educationMode) {
            logEducationalStep("🎓 [教育模式] TraceableObjectInputStream 已启动教育追踪模式");
            logEducationalStep("📚 [目标] 帮助开发者深度理解Java反序列化过程");
        }
        
        logStep("TraceableObjectInputStream initialized", "INIT", null, getCurrentStackTrace());
    }
    
    public Object readObjectWithTrace() throws IOException, ClassNotFoundException {
        long startTime = System.nanoTime();
        int currentStep = stepCounter.incrementAndGet();
        
        logStep("Starting readObject()", "READ_START", null, getCurrentStackTrace());
        
        try {
            Object result = super.readObject();
            long duration = System.nanoTime() - startTime;
            
            logStep("Completed readObject()", "READ_COMPLETE", result, getCurrentStackTrace(), duration);
            
            // 如果是自定义对象，尝试追踪其readObject方法
            if (result != null && hasCustomReadObject(result.getClass())) {
                logStep("Object has custom readObject method: " + result.getClass().getName(), 
                       "CUSTOM_READ_OBJECT", result, getCurrentStackTrace());
            }
            
            return result;
        } catch (Exception e) {
            logStep("Exception in readObject(): " + e.getMessage(), "READ_ERROR", null, getCurrentStackTrace());
            throw e;
        }
    }
    
    @Override
    protected Object resolveObject(Object obj) throws IOException {
        logStep("Resolving object: " + (obj != null ? obj.getClass().getName() : "null"), 
               "RESOLVE_OBJECT", obj, getCurrentStackTrace());
        return super.resolveObject(obj);
    }
    
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        logStep("Resolving class: " + desc.getName(), "RESOLVE_CLASS", desc, getCurrentStackTrace());
        
        Class<?> clazz = super.resolveClass(desc);
        
        // 检查是否是潜在危险类
        if (isDangerousClass(clazz)) {
            logStep("WARNING: Dangerous class detected: " + clazz.getName(), 
                   "DANGEROUS_CLASS", clazz, getCurrentStackTrace());
        }
        
        return clazz;
    }
    
    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass desc = super.readClassDescriptor();
        logStep("Reading class descriptor: " + desc.getName(), "READ_CLASS_DESC", desc, getCurrentStackTrace());
        return desc;
    }
    
    private void logStep(String message, String type, Object relatedObject, StackTraceElement[] stackTrace) {
        logStep(message, type, relatedObject, stackTrace, 0);
    }
    
    private void logStep(String message, String type, Object relatedObject, 
                        StackTraceElement[] stackTrace, long duration) {
        DeserializationStep step = new DeserializationStep(
            stepCounter.get(),
            System.currentTimeMillis(),
            type,
            message,
            relatedObject,
            stackTrace,
            duration
        );
        
        executionTrace.add(step);
        
        if (enableVerboseTrace) {
            traceBuffer.append(step.toString()).append("\n");
            System.out.println("[TRACE-" + step.getStepNumber() + "] " + step.getMessage());
            
            // 打印相关对象信息
            if (relatedObject != null) {
                System.out.println("  └─ Object: " + relatedObject.getClass().getName() + 
                                 " @ " + System.identityHashCode(relatedObject));
                
                // 如果是自定义类，尝试显示字段信息
                if (!isJavaBuiltinClass(relatedObject.getClass())) {
                    printObjectFields(relatedObject);
                }
            }
        }
    }
    
    private boolean hasCustomReadObject(Class<?> clazz) {
        try {
            Method readObject = clazz.getDeclaredMethod("readObject", ObjectInputStream.class);
            return readObject != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
    
    private boolean isDangerousClass(Class<?> clazz) {
        String className = clazz.getName();
        
        // 已知的危险类列表
        String[] dangerousClasses = {
            "org.apache.commons.collections.Transformer",
            "org.apache.commons.collections.functors.InvokerTransformer",
            "org.apache.commons.collections.functors.ChainedTransformer",
            "org.apache.commons.collections.functors.ConstantTransformer",
            "org.apache.commons.collections.map.LazyMap",
            "java.util.PriorityQueue",
            "java.lang.Runtime",
            "java.lang.ProcessBuilder"
        };
        
        for (String dangerous : dangerousClasses) {
            if (className.contains(dangerous)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isJavaBuiltinClass(Class<?> clazz) {
        String packageName = clazz.getPackage() != null ? clazz.getPackage().getName() : "";
        return packageName.startsWith("java.") || packageName.startsWith("javax.");
    }
    
    private void printObjectFields(Object obj) {
        try {
            Class<?> clazz = obj.getClass();
            Field[] fields = clazz.getDeclaredFields();
            
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    System.out.println("    ├─ " + field.getName() + " = " + 
                                     (value != null ? value.toString() : "null"));
                } catch (IllegalAccessException e) {
                    System.out.println("    ├─ " + field.getName() + " = <inaccessible>");
                }
            }
        } catch (Exception e) {
            System.out.println("    └─ Unable to access fields: " + e.getMessage());
        }
    }
    
    private StackTraceElement[] getCurrentStackTrace() {
        return Thread.currentThread().getStackTrace();
    }
    
    /**
     * 记录教育步骤
     */
    private void logEducationalStep(String step) {
        if (educationMode) {
            educationalSteps.add(step);
            educationLog.append(step).append("\n");
            System.out.println(step);
        }
    }
    
    /**
     * 带教育追踪的反序列化方法
     */
    public Object readObjectWithEducationalTrace() throws IOException, ClassNotFoundException {
        if (educationMode) {
            logEducationalStep("📚 [开始] 启动教育模式的反序列化过程");
            logEducationalStep("🔍 [步骤1] 读取序列化流的魔术数字和版本");
        }
        
        long startTime = System.nanoTime();
        int currentStep = stepCounter.incrementAndGet();
        
        logStep("Starting educational readObject()", "EDUCATIONAL_READ_START", null, getCurrentStackTrace());
        
        if (educationMode) {
            logEducationalStep("🔄 [步骤2] 开始解析对象流");
            logEducationalStep("⚙️ [步骤3] 检查对象类型和类描述符");
        }
        
        try {
            Object result = super.readObject();
            long duration = System.nanoTime() - startTime;
            
            if (educationMode) {
                logEducationalStep("✅ [步骤4] 对象实例创建成功: " + 
                    (result != null ? result.getClass().getName() : "null"));
                logEducationalStep("📊 [性能] 反序列化耗时: " + (duration / 1_000_000.0) + " ms");
                
                if (result != null) {
                    logEducationalStep("🔍 [分析] 对象详细信息:");
                    logEducationalStep("   └─ 类名: " + result.getClass().getName());
                    logEducationalStep("   └─ HashCode: " + System.identityHashCode(result));
                    logEducationalStep("   └─ toString(): " + result.toString());
                    
                    // 检查是否有自定义readObject方法
                    if (hasCustomReadObject(result.getClass())) {
                        logEducationalStep("⚠️ [重要] 检测到自定义readObject方法！");
                        logEducationalStep("   └─ 这可能存在安全风险，需要仔细审查");
                        logEducationalStep("   └─ 自定义readObject可能执行任意代码");
                    }
                    
                    // 检查危险类
                    if (isDangerousClass(result.getClass())) {
                        logEducationalStep("🚨 [警告] 检测到潜在危险类！");
                        logEducationalStep("   └─ 类名: " + result.getClass().getName());
                        logEducationalStep("   └─ 建议: 立即进行安全审查");
                    }
                }
                
                logEducationalStep("🎓 [教育总结] 反序列化过程完成，共执行 " + educationalSteps.size() + " 个教育步骤");
            }
            
            logStep("Completed educational readObject()", "EDUCATIONAL_READ_COMPLETE", result, getCurrentStackTrace(), duration);
            return result;
            
        } catch (Exception e) {
            if (educationMode) {
                logEducationalStep("❌ [异常] 反序列化过程发生异常: " + e.getClass().getSimpleName());
                logEducationalStep("   └─ 异常信息: " + e.getMessage());
                logEducationalStep("   └─ 这可能是由于:");
                logEducationalStep("       • 序列化数据损坏");
                logEducationalStep("       • 类版本不匹配");
                logEducationalStep("       • 安全策略阻止");
                logEducationalStep("       • 自定义readObject方法异常");
            }
            
            logStep("Exception in educational readObject(): " + e.getMessage(), "EDUCATIONAL_READ_ERROR", null, getCurrentStackTrace());
            throw e;
        }
    }
    
    /**
     * 获取教育步骤列表
     */
    public List<String> getEducationalSteps() {
        return new ArrayList<>(educationalSteps);
    }
    
    /**
     * 获取教育日志
     */
    public String getEducationLog() {
        return educationLog.toString();
    }
    
    /**
     * 是否为教育模式
     */
    public boolean isEducationMode() {
        return educationMode;
    }
    
    // 公共方法用于获取执行追踪
    public List<DeserializationStep> getExecutionTrace() {
        return new ArrayList<>(executionTrace);
    }
    
    public String getTraceAsString() {
        return traceBuffer.toString();
    }
    
    public void printFullTrace() {
        System.out.println("=== Deserialization Execution Trace ===");
        for (DeserializationStep step : executionTrace) {
            System.out.println(step.toDetailedString());
        }
        System.out.println("=== End of Trace ===");
    }
    
    public DeserializationStatistics getStatistics() {
        return new DeserializationStatistics(executionTrace);
    }
}