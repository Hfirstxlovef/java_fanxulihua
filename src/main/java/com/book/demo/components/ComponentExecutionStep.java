package com.book.demo.components;

import java.io.Serializable;

public class ComponentExecutionStep implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final int stepNumber;
    private final long timestamp;
    private final String stepType;
    private final String message;
    private final Object relatedObject;
    private final String relatedObjectInfo;
    private final StackTraceElement[] stackTrace;
    
    public ComponentExecutionStep(int stepNumber, long timestamp, String stepType, 
                                String message, Object relatedObject, StackTraceElement[] stackTrace) {
        this.stepNumber = stepNumber;
        this.timestamp = timestamp;
        this.stepType = stepType;
        this.message = message;
        this.relatedObject = relatedObject;
        this.relatedObjectInfo = buildObjectInfo(relatedObject);
        this.stackTrace = stackTrace;
    }
    
    private String buildObjectInfo(Object obj) {
        if (obj == null) return "null";
        
        StringBuilder info = new StringBuilder();
        info.append("Class: ").append(obj.getClass().getSimpleName());
        info.append(", Hash: ").append(System.identityHashCode(obj));
        
        try {
            String toString = obj.toString();
            if (toString.length() > 80) {
                info.append(", ToString: ").append(toString.substring(0, 80)).append("...");
            } else {
                info.append(", ToString: ").append(toString);
            }
        } catch (Exception e) {
            info.append(", ToString: <error>");
        }
        
        return info.toString();
    }
    
    public String getDescription() {
        return String.format("[%d] %s: %s", stepNumber, stepType, message);
    }
    
    public boolean isWarning() {
        return message.contains("⚠️") || stepType.contains("SUSPICIOUS") || stepType.contains("WARNING");
    }
    
    public boolean isError() {
        return stepType.contains("ERROR") || message.contains("失败");
    }
    
    public boolean isSecurityRelated() {
        return stepType.contains("SECURITY") || stepType.contains("DANGEROUS") || 
               stepType.contains("SUSPICIOUS") || message.contains("安全");
    }
    
    // Getters
    public int getStepNumber() { return stepNumber; }
    public long getTimestamp() { return timestamp; }
    public String getStepType() { return stepType; }
    public String getMessage() { return message; }
    public Object getRelatedObject() { return relatedObject; }
    public String getRelatedObjectInfo() { return relatedObjectInfo; }
    public StackTraceElement[] getStackTrace() { return stackTrace; }
}