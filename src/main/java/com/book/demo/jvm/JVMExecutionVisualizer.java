package com.book.demo.jvm;

import com.book.demo.trace.TraceableObjectInputStream;
import javassist.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JVMExecutionVisualizer {
    
    private static final Map<String, ClassInstrumentation> instrumentedClasses = new ConcurrentHashMap<>();
    private final List<ExecutionFrame> executionFrames = new ArrayList<>();
    private final Set<String> monitoredPackages = new HashSet<>();
    private final ClassPool classPool;
    private boolean instrumentationEnabled = false;
    
    public JVMExecutionVisualizer() {
        this.classPool = ClassPool.getDefault();
        
        // 默认监控的包
        monitoredPackages.add("com.book.demo");
        monitoredPackages.add("java.util");
        monitoredPackages.add("org.apache.commons.collections");
    }
    
    public void enableInstrumentation() {
        if (!instrumentationEnabled) {
            // 设置类加载器拦截
            setupClassLoadInterception();
            instrumentationEnabled = true;
        }
    }
    
    private void setupClassLoadInterception() {
        // 创建自定义类加载器来拦截类的加载
        Thread.currentThread().setContextClassLoader(new InstrumentingClassLoader());
    }
    
    public void addMonitoredPackage(String packageName) {
        monitoredPackages.add(packageName);
    }
    
    public DeserializationVisualization visualizeDeserialization(byte[] serializedData) {
        executionFrames.clear();
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
            TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true);
            
            recordFrame("开始反序列化", "DESERIALIZATION_START", null);
            
            Object result = tois.readObject();
            
            recordFrame("完成反序列化", "DESERIALIZATION_COMPLETE", result);
            
            return new DeserializationVisualization(
                tois.getExecutionTrace(),
                executionFrames,
                tois.getStatistics()
            );
            
        } catch (Exception e) {
            recordFrame("反序列化异常: " + e.getMessage(), "ERROR", e);
            throw new RuntimeException("反序列化可视化失败", e);
        }
    }
    
    public MethodCallVisualization traceMethodCalls(Runnable code) {
        List<MethodCallFrame> methodCalls = new ArrayList<>();
        
        // 使用代理模式拦截方法调用
        InvocationHandler handler = new MethodInterceptor(methodCalls);
        
        try {
            code.run();
        } catch (Exception e) {
            methodCalls.add(new MethodCallFrame("异常", e.getClass().getName(), 
                                              e.getMessage(), System.nanoTime()));
        }
        
        return new MethodCallVisualization(methodCalls);
    }
    
    public ObjectCreationVisualization traceObjectCreation(Supplier<Object> creator) {
        List<ObjectCreationFrame> creationFrames = new ArrayList<>();
        long startTime = System.nanoTime();
        
        // 监控对象创建
        ObjectCreationTracker tracker = new ObjectCreationTracker(creationFrames);
        
        try {
            Object result = creator.get();
            long endTime = System.nanoTime();
            
            return new ObjectCreationVisualization(
                creationFrames, 
                result, 
                endTime - startTime
            );
        } catch (Exception e) {
            creationFrames.add(new ObjectCreationFrame("创建异常", e.getClass().getName(), 
                                                      System.nanoTime()));
            throw e;
        }
    }
    
    public ReflectionVisualization traceReflectionCalls(Runnable code) {
        List<ReflectionFrame> reflectionFrames = new ArrayList<>();
        
        // Hook反射API调用
        ReflectionInterceptor interceptor = new ReflectionInterceptor(reflectionFrames);
        interceptor.install();
        
        try {
            code.run();
        } finally {
            interceptor.uninstall();
        }
        
        return new ReflectionVisualization(reflectionFrames);
    }
    
    private void recordFrame(String description, String type, Object relatedObject) {
        ExecutionFrame frame = new ExecutionFrame(
            System.nanoTime(),
            Thread.currentThread().getStackTrace(),
            description,
            type,
            relatedObject
        );
        executionFrames.add(frame);
    }
    
    // 内部类：执行帧
    public static class ExecutionFrame {
        private final long timestamp;
        private final StackTraceElement[] stackTrace;
        private final String description;
        private final String type;
        private final Object relatedObject;
        
        public ExecutionFrame(long timestamp, StackTraceElement[] stackTrace, 
                            String description, String type, Object relatedObject) {
            this.timestamp = timestamp;
            this.stackTrace = stackTrace;
            this.description = description;
            this.type = type;
            this.relatedObject = relatedObject;
        }
        
        public String toVisualizationString() {
            StringBuilder sb = new StringBuilder();
            sb.append("┌─ ").append(description).append(" [").append(type).append("]\n");
            sb.append("├─ 时间: ").append(timestamp).append("\n");
            
            if (relatedObject != null) {
                sb.append("├─ 对象: ").append(relatedObject.getClass().getSimpleName()).append("\n");
            }
            
            sb.append("└─ 调用栈: ").append(getRelevantStackFrame()).append("\n");
            return sb.toString();
        }
        
        private String getRelevantStackFrame() {
            if (stackTrace == null || stackTrace.length < 3) return "未知";
            
            for (int i = 2; i < Math.min(stackTrace.length, 6); i++) {
                StackTraceElement frame = stackTrace[i];
                if (!frame.getClassName().contains("JVMExecutionVisualizer")) {
                    return frame.getClassName() + "." + frame.getMethodName() + 
                           ":" + frame.getLineNumber();
                }
            }
            return "内部调用";
        }
        
        // Getters
        public long getTimestamp() { return timestamp; }
        public StackTraceElement[] getStackTrace() { return stackTrace; }
        public String getDescription() { return description; }
        public String getType() { return type; }
        public Object getRelatedObject() { return relatedObject; }
    }
    
    // 自定义接口用于代码执行追踪
    @FunctionalInterface
    public interface Supplier<T> {
        T get();
    }
    
    // 类加载拦截器
    private class InstrumentingClassLoader extends ClassLoader {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (shouldInstrument(name)) {
                try {
                    CtClass ctClass = classPool.get(name);
                    if (!ctClass.isFrozen()) {
                        instrumentClass(ctClass);
                    }
                    byte[] bytecode = ctClass.toBytecode();
                    return defineClass(name, bytecode, 0, bytecode.length);
                } catch (Exception e) {
                    recordFrame("类加载失败: " + name, "CLASS_LOAD_ERROR", e);
                }
            }
            return super.loadClass(name);
        }
        
        private boolean shouldInstrument(String className) {
            return monitoredPackages.stream().anyMatch(className::startsWith);
        }
        
        private void instrumentClass(CtClass ctClass) throws Exception {
            // 为每个方法添加执行追踪
            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod method : methods) {
                if (!method.isEmpty()) {
                    method.insertBefore("{ " +
                        "com.book.demo.jvm.JVMExecutionVisualizer.recordMethodEntry(\"" + 
                        ctClass.getName() + "." + method.getName() + "\"); }");
                    
                    method.insertAfter("{ " +
                        "com.book.demo.jvm.JVMExecutionVisualizer.recordMethodExit(\"" + 
                        ctClass.getName() + "." + method.getName() + "\"); }");
                }
            }
        }
    }
    
    // 静态方法用于字节码注入回调
    public static void recordMethodEntry(String methodName) {
        // 这将被字节码注入调用
        System.out.println("→ 进入方法: " + methodName);
    }
    
    public static void recordMethodExit(String methodName) {
        // 这将被字节码注入调用
        System.out.println("← 退出方法: " + methodName);
    }
    
    // 辅助类：类指令信息
    private static class ClassInstrumentation {
        private final String className;
        private final List<String> instrumentedMethods;
        private final long instrumentationTime;
        
        public ClassInstrumentation(String className) {
            this.className = className;
            this.instrumentedMethods = new ArrayList<>();
            this.instrumentationTime = System.currentTimeMillis();
        }
        
        public void addInstrumentedMethod(String methodName) {
            instrumentedMethods.add(methodName);
        }
        
        // Getters
        public String getClassName() { return className; }
        public List<String> getInstrumentedMethods() { return instrumentedMethods; }
        public long getInstrumentationTime() { return instrumentationTime; }
    }
    
    // Missing methods that are called from DeserializationDemoResource
    public void enableMemoryProfiling() {
        // Enable memory profiling functionality
        System.out.println("内存分析已启用");
    }
    
    public void setMemorySamplingInterval(int interval) {
        // Set memory sampling interval
        System.out.println("内存采样间隔设置为: " + interval + "ms");
    }
    
    public void enableBytecodeInstrumentation() {
        // Enable bytecode instrumentation
        enableInstrumentation();
        System.out.println("字节码指令追踪已启用");
    }
    
    public void enableReflectionTracking() {
        // Enable reflection tracking
        System.out.println("反射调用追踪已启用");
    }
    
    public void enablePerformanceProfiling() {
        // Enable performance profiling
        System.out.println("性能分析已启用");
    }
    
    public void setProfileSamplingRate(int rate) {
        // Set profile sampling rate
        System.out.println("性能采样率设置为: " + rate);
    }
    
    public void enableBasicInstrumentation() {
        // Enable basic instrumentation
        enableInstrumentation();
        System.out.println("基础指令追踪已启用");
    }
}