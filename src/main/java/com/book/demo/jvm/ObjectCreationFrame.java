package com.book.demo.jvm;

import java.util.List;

public class ObjectCreationFrame {
    
    private final String objectType;
    private final String creationContext;
    private final long timestamp;
    private final int objectHashCode;
    
    public ObjectCreationFrame(String objectType, String creationContext, long timestamp) {
        this.objectType = objectType;
        this.creationContext = creationContext;
        this.timestamp = timestamp;
        this.objectHashCode = System.identityHashCode(this);
    }
    
    @Override
    public String toString() {
        return String.format("创建 %s @ %s (hash: %x)", 
                           objectType, creationContext, objectHashCode);
    }
    
    // Getters
    public String getObjectType() { return objectType; }
    public String getCreationContext() { return creationContext; }
    public long getTimestamp() { return timestamp; }
    public int getObjectHashCode() { return objectHashCode; }
}

class ObjectCreationVisualization {
    
    private final List<ObjectCreationFrame> creationFrames;
    private final Object finalResult;
    private final long totalCreationTime;
    
    public ObjectCreationVisualization(List<ObjectCreationFrame> creationFrames, 
                                     Object finalResult, long totalCreationTime) {
        this.creationFrames = creationFrames;
        this.finalResult = finalResult;
        this.totalCreationTime = totalCreationTime;
    }
    
    public void printVisualization() {
        System.out.println("=== 对象创建可视化 ===");
        System.out.println("创建的对象数量: " + creationFrames.size());
        System.out.println("总创建时间: " + (totalCreationTime / 1_000_000.0) + " ms");
        
        if (finalResult != null) {
            System.out.println("最终结果: " + finalResult.getClass().getSimpleName());
        }
        System.out.println();
        
        for (int i = 0; i < creationFrames.size(); i++) {
            ObjectCreationFrame frame = creationFrames.get(i);
            System.out.printf("[%3d] %s%n", i + 1, frame.toString());
        }
    }
    
    public List<ObjectCreationFrame> getCreationFrames() { return creationFrames; }
    public Object getFinalResult() { return finalResult; }
    public long getTotalCreationTime() { return totalCreationTime; }
}

class ObjectCreationTracker {
    
    private final List<ObjectCreationFrame> creationFrames;
    
    public ObjectCreationTracker(List<ObjectCreationFrame> creationFrames) {
        this.creationFrames = creationFrames;
    }
    
    public void recordCreation(String objectType, String context) {
        creationFrames.add(new ObjectCreationFrame(objectType, context, System.nanoTime()));
    }
}