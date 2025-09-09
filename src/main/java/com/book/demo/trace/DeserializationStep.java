package com.book.demo.trace;

import java.io.Serializable;
import java.util.Arrays;

public class DeserializationStep implements Serializable {
    
    private final int stepNumber;
    private final long timestamp;
    private final String stepType;
    private final String message;
    private final Object relatedObject;
    private final String relatedObjectInfo;
    private final StackTraceElement[] stackTrace;
    private final long executionDuration;
    
    public DeserializationStep(int stepNumber, long timestamp, String stepType, String message,
                             Object relatedObject, StackTraceElement[] stackTrace, long executionDuration) {
        this.stepNumber = stepNumber;
        this.timestamp = timestamp;
        this.stepType = stepType;
        this.message = message;
        this.relatedObject = relatedObject;
        this.relatedObjectInfo = buildObjectInfo(relatedObject);
        this.stackTrace = stackTrace != null ? Arrays.copyOf(stackTrace, stackTrace.length) : null;
        this.executionDuration = executionDuration;
    }
    
    private String buildObjectInfo(Object obj) {
        if (obj == null) return "null";
        
        StringBuilder info = new StringBuilder();
        info.append("Class: ").append(obj.getClass().getName());
        info.append(", Hash: ").append(System.identityHashCode(obj));
        info.append(", ToString: ");
        
        try {
            String toString = obj.toString();
            // 限制toString长度避免过长输出
            if (toString.length() > 100) {
                info.append(toString.substring(0, 100)).append("...");
            } else {
                info.append(toString);
            }
        } catch (Exception e) {
            info.append("<toString() failed: ").append(e.getMessage()).append(">");
        }
        
        return info.toString();
    }
    
    public String getRelevantStackFrame() {
        if (stackTrace == null || stackTrace.length < 3) return "Unknown";
        
        // 跳过前几个框架(getStackTrace, getCurrentStackTrace, logStep等)
        for (int i = 3; i < stackTrace.length; i++) {
            StackTraceElement frame = stackTrace[i];
            String className = frame.getClassName();
            
            // 跳过我们自己的追踪类
            if (!className.contains("TraceableObjectInputStream") && 
                !className.contains("DeserializationStep")) {
                return frame.getClassName() + "." + frame.getMethodName() + 
                       ":" + frame.getLineNumber();
            }
        }
        
        return "Internal";
    }
    
    @Override
    public String toString() {
        return String.format("[%d] %s: %s (%.3f ms)", 
                           stepNumber, stepType, message, executionDuration / 1_000_000.0);
    }
    
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Step ").append(stepNumber).append(" [").append(stepType).append("]\n");
        sb.append("  Timestamp: ").append(timestamp).append("\n");
        sb.append("  Message: ").append(message).append("\n");
        sb.append("  Duration: ").append(executionDuration / 1_000_000.0).append(" ms\n");
        sb.append("  Location: ").append(getRelevantStackFrame()).append("\n");
        
        if (relatedObjectInfo != null && !"null".equals(relatedObjectInfo)) {
            sb.append("  Object: ").append(relatedObjectInfo).append("\n");
        }
        
        return sb.toString();
    }
    
    public String toJsonString() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"stepNumber\":").append(stepNumber).append(",");
        json.append("\"timestamp\":").append(timestamp).append(",");
        json.append("\"stepType\":\"").append(escape(stepType)).append("\",");
        json.append("\"message\":\"").append(escape(message)).append("\",");
        json.append("\"executionDuration\":").append(executionDuration).append(",");
        json.append("\"location\":\"").append(escape(getRelevantStackFrame())).append("\",");
        json.append("\"objectInfo\":\"").append(escape(relatedObjectInfo)).append("\"");
        json.append("}");
        return json.toString();
    }
    
    private String escape(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
    
    // Getters
    public int getStepNumber() { return stepNumber; }
    public long getTimestamp() { return timestamp; }
    public String getStepType() { return stepType; }
    public String getMessage() { return message; }
    public Object getRelatedObject() { return relatedObject; }
    public String getRelatedObjectInfo() { return relatedObjectInfo; }
    public StackTraceElement[] getStackTrace() { return stackTrace; }
    public long getExecutionDuration() { return executionDuration; }
    
    public boolean isWarning() {
        return stepType.equals("DANGEROUS_CLASS") || message.contains("WARNING");
    }
    
    public boolean isError() {
        return stepType.equals("READ_ERROR") || stepType.contains("ERROR");
    }
}