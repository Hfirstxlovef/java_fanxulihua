package com.book.demo.jvm;

import java.util.List;

public class MethodCallFrame {
    
    private final String className;
    private final String methodName;
    private final String arguments;
    private final long startTime;
    private long endTime;
    private Object result;
    private Exception exception;
    
    public MethodCallFrame(String className, String methodName, String arguments, long startTime) {
        this.className = className;
        this.methodName = methodName;
        this.arguments = arguments;
        this.startTime = startTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    public void setResult(Object result) {
        this.result = result;
    }
    
    public void setException(Exception exception) {
        this.exception = exception;
    }
    
    public long getDuration() {
        return endTime - startTime;
    }
    
    public boolean hasException() {
        return exception != null;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(className).append(".").append(methodName).append(arguments);
        
        if (hasException()) {
            sb.append(" → 异常: ").append(exception.getClass().getSimpleName());
        } else if (result != null) {
            sb.append(" → ").append(result.getClass().getSimpleName());
        }
        
        sb.append(" (").append(String.format("%.3f ms", getDuration() / 1_000_000.0)).append(")");
        return sb.toString();
    }
    
    // Getters
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
    public String getArguments() { return arguments; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public Object getResult() { return result; }
    public Exception getException() { return exception; }
}

class MethodCallVisualization {
    
    private final List<MethodCallFrame> methodCalls;
    
    public MethodCallVisualization(List<MethodCallFrame> methodCalls) {
        this.methodCalls = methodCalls;
    }
    
    public void printVisualization() {
        System.out.println("=== 方法调用可视化 ===");
        System.out.println("总调用次数: " + methodCalls.size());
        
        long totalDuration = methodCalls.stream()
                                      .mapToLong(MethodCallFrame::getDuration)
                                      .sum();
        System.out.println("总执行时间: " + (totalDuration / 1_000_000.0) + " ms");
        System.out.println();
        
        for (int i = 0; i < methodCalls.size(); i++) {
            MethodCallFrame call = methodCalls.get(i);
            System.out.printf("[%3d] %s%n", i + 1, call.toString());
        }
    }
    
    public List<MethodCallFrame> getMethodCalls() {
        return methodCalls;
    }
}