package com.book.demo.jvm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectionFrame {
    
    private final String operationType; // METHOD_INVOKE, FIELD_ACCESS, CLASS_LOAD, etc.
    private final String targetClass;
    private final String memberName;
    private final Object[] arguments;
    private final long timestamp;
    private final long duration;
    private final Object result;
    private final Exception exception;
    
    public ReflectionFrame(String operationType, String targetClass, String memberName,
                         Object[] arguments, long timestamp, long duration, 
                         Object result, Exception exception) {
        this.operationType = operationType;
        this.targetClass = targetClass;
        this.memberName = memberName;
        this.arguments = arguments;
        this.timestamp = timestamp;
        this.duration = duration;
        this.result = result;
        this.exception = exception;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(operationType).append(": ").append(targetClass);
        
        if (memberName != null) {
            sb.append(".").append(memberName);
        }
        
        if (arguments != null && arguments.length > 0) {
            sb.append("(").append(arguments.length).append(" args)");
        }
        
        if (exception != null) {
            sb.append(" → 异常: ").append(exception.getClass().getSimpleName());
        } else if (result != null) {
            sb.append(" → ").append(result.getClass().getSimpleName());
        }
        
        sb.append(" (").append(String.format("%.3f ms", duration / 1_000_000.0)).append(")");
        return sb.toString();
    }
    
    // Getters
    public String getOperationType() { return operationType; }
    public String getTargetClass() { return targetClass; }
    public String getMemberName() { return memberName; }
    public Object[] getArguments() { return arguments; }
    public long getTimestamp() { return timestamp; }
    public long getDuration() { return duration; }
    public Object getResult() { return result; }
    public Exception getException() { return exception; }
    public boolean hasException() { return exception != null; }
}

class ReflectionVisualization {
    
    private final List<ReflectionFrame> reflectionFrames;
    
    public ReflectionVisualization(List<ReflectionFrame> reflectionFrames) {
        this.reflectionFrames = reflectionFrames;
    }
    
    public void printVisualization() {
        System.out.println("=== 反射调用可视化 ===");
        System.out.println("反射操作数量: " + reflectionFrames.size());
        
        long totalDuration = reflectionFrames.stream()
                                           .mapToLong(ReflectionFrame::getDuration)
                                           .sum();
        System.out.println("总执行时间: " + (totalDuration / 1_000_000.0) + " ms");
        
        // 统计操作类型
        System.out.println("\n操作类型分布:");
        reflectionFrames.stream()
                       .collect(java.util.stream.Collectors.groupingBy(
                           ReflectionFrame::getOperationType,
                           java.util.stream.Collectors.counting()))
                       .forEach((type, count) -> 
                           System.out.println("  " + type + ": " + count + " 次"));
        
        System.out.println("\n详细调用序列:");
        for (int i = 0; i < reflectionFrames.size(); i++) {
            ReflectionFrame frame = reflectionFrames.get(i);
            System.out.printf("[%3d] %s%n", i + 1, frame.toString());
        }
    }
    
    public List<ReflectionFrame> getReflectionFrames() {
        return reflectionFrames;
    }
}

class ReflectionInterceptor {
    
    private final List<ReflectionFrame> reflectionFrames;
    private boolean installed = false;
    
    public ReflectionInterceptor(List<ReflectionFrame> reflectionFrames) {
        this.reflectionFrames = reflectionFrames;
    }
    
    public void install() {
        if (!installed) {
            // 在实际实现中，这里会安装Java Agent或使用字节码操作
            // 来拦截反射API调用
            installed = true;
            System.out.println("反射拦截器已安装");
        }
    }
    
    public void uninstall() {
        if (installed) {
            installed = false;
            System.out.println("反射拦截器已卸载");
        }
    }
    
    // 这些方法会被字节码注入调用
    public static void recordMethodInvoke(Method method, Object target, Object[] args, 
                                        long startTime, long endTime, Object result, Exception ex) {
        // 记录方法调用
    }
    
    public static void recordFieldAccess(Field field, Object target, Object value, 
                                       long startTime, long endTime, Exception ex) {
        // 记录字段访问
    }
    
    public static void recordClassLoad(String className, long startTime, long endTime) {
        // 记录类加载
    }
}