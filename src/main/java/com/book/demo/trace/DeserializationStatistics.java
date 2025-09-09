package com.book.demo.trace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeserializationStatistics {
    
    private final List<DeserializationStep> steps;
    private final Map<String, Integer> stepTypeCount;
    private final Map<String, Long> stepTypeDuration;
    private final long totalDuration;
    private final int totalSteps;
    private final int warningCount;
    private final int errorCount;
    
    public DeserializationStatistics(List<DeserializationStep> steps) {
        this.steps = steps;
        this.stepTypeCount = new HashMap<>();
        this.stepTypeDuration = new HashMap<>();
        this.totalSteps = steps.size();
        
        long duration = 0;
        int warnings = 0;
        int errors = 0;
        
        for (DeserializationStep step : steps) {
            String type = step.getStepType();
            
            // 统计步骤类型数量
            stepTypeCount.put(type, stepTypeCount.getOrDefault(type, 0) + 1);
            
            // 统计步骤类型执行时间
            stepTypeDuration.put(type, stepTypeDuration.getOrDefault(type, 0L) + step.getExecutionDuration());
            
            duration += step.getExecutionDuration();
            
            if (step.isWarning()) warnings++;
            if (step.isError()) errors++;
        }
        
        this.totalDuration = duration;
        this.warningCount = warnings;
        this.errorCount = errors;
    }
    
    public void printSummary() {
        System.out.println("=== Deserialization Statistics ===");
        System.out.println("Total Steps: " + totalSteps);
        System.out.println("Total Duration: " + (totalDuration / 1_000_000.0) + " ms");
        System.out.println("Average Step Duration: " + (totalSteps > 0 ? (totalDuration / totalSteps / 1_000_000.0) : 0) + " ms");
        System.out.println("Warnings: " + warningCount);
        System.out.println("Errors: " + errorCount);
        System.out.println();
        
        System.out.println("Step Type Distribution:");
        for (Map.Entry<String, Integer> entry : stepTypeCount.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();
            long duration = stepTypeDuration.getOrDefault(type, 0L);
            double avgDuration = count > 0 ? (duration / count / 1_000_000.0) : 0;
            
            System.out.printf("  %-20s: %3d steps, %8.3f ms total, %8.3f ms avg%n", 
                            type, count, duration / 1_000_000.0, avgDuration);
        }
        
        if (warningCount > 0) {
            System.out.println("\nWarnings:");
            steps.stream()
                 .filter(DeserializationStep::isWarning)
                 .forEach(step -> System.out.println("  - " + step.getMessage()));
        }
        
        if (errorCount > 0) {
            System.out.println("\nErrors:");
            steps.stream()
                 .filter(DeserializationStep::isError)
                 .forEach(step -> System.out.println("  - " + step.getMessage()));
        }
    }
    
    public List<DeserializationStep> getDangerousSteps() {
        return steps.stream()
                   .filter(step -> step.getStepType().equals("DANGEROUS_CLASS") || 
                                 step.getMessage().contains("WARNING"))
                   .collect(Collectors.toList());
    }
    
    public List<DeserializationStep> getCustomReadObjectSteps() {
        return steps.stream()
                   .filter(step -> step.getStepType().equals("CUSTOM_READ_OBJECT"))
                   .collect(Collectors.toList());
    }
    
    public List<String> getInvolvedClasses() {
        return steps.stream()
                   .filter(step -> step.getRelatedObject() != null)
                   .map(step -> step.getRelatedObject().getClass().getName())
                   .distinct()
                   .collect(Collectors.toList());
    }
    
    public String toJsonSummary() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"totalSteps\":").append(totalSteps).append(",");
        json.append("\"totalDuration\":").append(totalDuration).append(",");
        json.append("\"warningCount\":").append(warningCount).append(",");
        json.append("\"errorCount\":").append(errorCount).append(",");
        
        json.append("\"stepTypeCount\":{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : stepTypeCount.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            first = false;
        }
        json.append("},");
        
        json.append("\"dangerousClasses\":[");
        List<String> dangerousClasses = getDangerousSteps().stream()
                                                          .map(DeserializationStep::getRelatedObjectInfo)
                                                          .distinct()
                                                          .collect(Collectors.toList());
        first = true;
        for (String className : dangerousClasses) {
            if (!first) json.append(",");
            json.append("\"").append(className).append("\"");
            first = false;
        }
        json.append("]");
        
        json.append("}");
        return json.toString();
    }
    
    // Getters
    public int getTotalSteps() { return totalSteps; }
    public long getTotalDuration() { return totalDuration; }
    public int getWarningCount() { return warningCount; }
    public int getErrorCount() { return errorCount; }
    public Map<String, Integer> getStepTypeCount() { return new HashMap<>(stepTypeCount); }
    public List<DeserializationStep> getSteps() { return steps; }
}