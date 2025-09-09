package com.book.demo.framework.spring;

import java.util.List;
import java.util.Map;

public class SpringExecutionStep {
    
    private final long timestamp;
    private final StackTraceElement[] stackTrace;
    private final String description;
    private final String stepType;
    private final Object relatedObject;
    
    public SpringExecutionStep(long timestamp, StackTraceElement[] stackTrace,
                             String description, String stepType, Object relatedObject) {
        this.timestamp = timestamp;
        this.stackTrace = stackTrace;
        this.description = description;
        this.stepType = stepType;
        this.relatedObject = relatedObject;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s", stepType, description);
    }
    
    // Getters
    public long getTimestamp() { return timestamp; }
    public StackTraceElement[] getStackTrace() { return stackTrace; }
    public String getDescription() { return description; }
    public String getStepType() { return stepType; }
    public Object getRelatedObject() { return relatedObject; }
}