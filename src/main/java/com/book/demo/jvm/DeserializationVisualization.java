package com.book.demo.jvm;

import com.book.demo.trace.DeserializationStatistics;
import com.book.demo.trace.DeserializationStep;

import java.util.List;
import java.util.ArrayList;

public class DeserializationVisualization {
    
    private final List<DeserializationStep> deserializationSteps;
    private final List<JVMExecutionVisualizer.ExecutionFrame> executionFrames;
    private final DeserializationStatistics statistics;
    private final long totalExecutionTime;
    
    public DeserializationVisualization(List<DeserializationStep> deserializationSteps,
                                      List<JVMExecutionVisualizer.ExecutionFrame> executionFrames,
                                      DeserializationStatistics statistics) {
        this.deserializationSteps = deserializationSteps;
        this.executionFrames = executionFrames;
        this.statistics = statistics;
        this.totalExecutionTime = calculateTotalTime();
    }
    
    private long calculateTotalTime() {
        if (executionFrames.isEmpty()) return 0;
        
        long minTime = executionFrames.get(0).getTimestamp();
        long maxTime = executionFrames.get(executionFrames.size() - 1).getTimestamp();
        return maxTime - minTime;
    }
    
    public void printVisualization() {
        System.out.println("=== JVM反序列化执行可视化 ===");
        System.out.println("总执行时间: " + (totalExecutionTime / 1_000_000.0) + " ms");
        System.out.println("反序列化步骤: " + deserializationSteps.size());
        System.out.println("JVM执行帧: " + executionFrames.size());
        System.out.println();
        
        System.out.println("=== 执行时间线 ===");
        
        // 合并并排序所有事件
        List<TimelineEvent> timeline = createTimeline();
        timeline.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
        
        long baseTime = timeline.isEmpty() ? 0 : timeline.get(0).getTimestamp();
        
        for (TimelineEvent event : timeline) {
            long relativeTime = event.getTimestamp() - baseTime;
            System.out.printf("[%8.3f ms] %s: %s%n", 
                            relativeTime / 1_000_000.0, 
                            event.getType(), 
                            event.getDescription());
            
            // 显示相关的对象信息
            if (event.hasObjectInfo()) {
                System.out.println("              └─ " + event.getObjectInfo());
            }
        }
        
        System.out.println("\n=== 统计摘要 ===");
        statistics.printSummary();
    }
    
    private List<TimelineEvent> createTimeline() {
        List<TimelineEvent> timeline = new ArrayList<>();
        
        // 添加反序列化步骤事件
        for (DeserializationStep step : deserializationSteps) {
            timeline.add(new TimelineEvent(
                step.getTimestamp() * 1_000_000, // 转换为纳秒
                "DESER",
                step.getMessage(),
                step.getRelatedObjectInfo()
            ));
        }
        
        // 添加JVM执行帧事件
        for (JVMExecutionVisualizer.ExecutionFrame frame : executionFrames) {
            timeline.add(new TimelineEvent(
                frame.getTimestamp(),
                "JVM",
                frame.getDescription(),
                frame.getRelatedObject() != null ? 
                    frame.getRelatedObject().getClass().getSimpleName() : null
            ));
        }
        
        return timeline;
    }
    
    public String generateHtmlVisualization() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<title>Java反序列化执行可视化</title>\n");
        html.append("<style>\n");
        html.append(getVisualizationCSS());
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        
        html.append("<h1>Java反序列化执行可视化</h1>\n");
        html.append("<div class='summary'>\n");
        html.append("<p>总执行时间: ").append(totalExecutionTime / 1_000_000.0).append(" ms</p>\n");
        html.append("<p>反序列化步骤: ").append(deserializationSteps.size()).append("</p>\n");
        html.append("<p>JVM执行帧: ").append(executionFrames.size()).append("</p>\n");
        html.append("</div>\n");
        
        html.append("<div class='timeline'>\n");
        List<TimelineEvent> timeline = createTimeline();
        timeline.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
        
        long baseTime = timeline.isEmpty() ? 0 : timeline.get(0).getTimestamp();
        
        for (TimelineEvent event : timeline) {
            long relativeTime = event.getTimestamp() - baseTime;
            String cssClass = event.getType().toLowerCase();
            
            html.append("<div class='event ").append(cssClass).append("'>\n");
            html.append("<span class='timestamp'>")
                .append(String.format("%.3f ms", relativeTime / 1_000_000.0))
                .append("</span>\n");
            html.append("<span class='type'>").append(event.getType()).append("</span>\n");
            html.append("<span class='description'>").append(escapeHtml(event.getDescription())).append("</span>\n");
            
            if (event.hasObjectInfo()) {
                html.append("<div class='object-info'>")
                    .append(escapeHtml(event.getObjectInfo()))
                    .append("</div>\n");
            }
            html.append("</div>\n");
        }
        html.append("</div>\n");
        
        html.append("</body>\n</html>");
        return html.toString();
    }
    
    private String getVisualizationCSS() {
        return """
            body { font-family: 'Courier New', monospace; margin: 20px; background-color: #f5f5f5; }
            h1 { color: #333; border-bottom: 2px solid #007acc; }
            .summary { background: white; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
            .timeline { background: white; padding: 15px; border-radius: 5px; }
            .event { margin: 5px 0; padding: 10px; border-left: 4px solid #ccc; background-color: #fafafa; }
            .event.deser { border-left-color: #007acc; }
            .event.jvm { border-left-color: #28a745; }
            .timestamp { color: #666; font-weight: bold; min-width: 100px; display: inline-block; }
            .type { color: #007acc; font-weight: bold; min-width: 60px; display: inline-block; }
            .description { color: #333; }
            .object-info { margin-top: 5px; padding-left: 20px; color: #666; font-size: 0.9em; }
            """;
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
    
    public String toJsonString() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"totalExecutionTime\": ").append(totalExecutionTime).append(",\n");
        json.append("  \"deserializationSteps\": ").append(deserializationSteps.size()).append(",\n");
        json.append("  \"executionFrames\": ").append(executionFrames.size()).append(",\n");
        json.append("  \"timeline\": [\n");
        
        List<TimelineEvent> timeline = createTimeline();
        timeline.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
        
        for (int i = 0; i < timeline.size(); i++) {
            TimelineEvent event = timeline.get(i);
            if (i > 0) json.append(",\n");
            json.append("    ").append(event.toJsonString());
        }
        
        json.append("\n  ],\n");
        json.append("  \"statistics\": ").append(statistics.toJsonSummary()).append("\n");
        json.append("}");
        return json.toString();
    }
    
    // 时间线事件内部类
    private static class TimelineEvent {
        private final long timestamp;
        private final String type;
        private final String description;
        private final String objectInfo;
        
        public TimelineEvent(long timestamp, String type, String description, String objectInfo) {
            this.timestamp = timestamp;
            this.type = type;
            this.description = description;
            this.objectInfo = objectInfo;
        }
        
        public long getTimestamp() { return timestamp; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getObjectInfo() { return objectInfo; }
        public boolean hasObjectInfo() { return objectInfo != null && !objectInfo.isEmpty(); }
        
        public String toJsonString() {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"timestamp\":").append(timestamp).append(",");
            json.append("\"type\":\"").append(escapeJson(type)).append("\",");
            json.append("\"description\":\"").append(escapeJson(description)).append("\"");
            if (hasObjectInfo()) {
                json.append(",\"objectInfo\":\"").append(escapeJson(objectInfo)).append("\"");
            }
            json.append("}");
            return json.toString();
        }
        
        private String escapeJson(String str) {
            if (str == null) return "";
            return str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        }
    }
    
    // Getters
    public List<DeserializationStep> getDeserializationSteps() { return deserializationSteps; }
    public List<JVMExecutionVisualizer.ExecutionFrame> getExecutionFrames() { return executionFrames; }
    public DeserializationStatistics getStatistics() { return statistics; }
    public long getTotalExecutionTime() { return totalExecutionTime; }
    
    // Missing methods used in DeserializationDemoResource
    public List<String> getEducationalSteps() {
        // Return educational steps based on deserialization steps
        List<String> educationalSteps = new ArrayList<>();
        for (DeserializationStep step : deserializationSteps) {
            educationalSteps.add(step.getMessage());
        }
        return educationalSteps;
    }
    
    public String getEducationLog() {
        // Generate education log from deserialization steps
        StringBuilder log = new StringBuilder();
        for (DeserializationStep step : deserializationSteps) {
            log.append(step.getMessage()).append("\\n");
        }
        return log.toString();
    }
    
    public String getMemoryUsageData() {
        // Return memory usage data as JSON string
        return String.format("{\"heapUsed\": %d, \"heapTotal\": %d}", 
                           Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(),
                           Runtime.getRuntime().totalMemory());
    }
    
    public String getPerformanceMetrics() {
        // Return performance metrics as JSON string
        return String.format("{\"executionTime\": %d, \"stepCount\": %d, \"frameCount\": %d}", 
                           totalExecutionTime / 1_000_000, // Convert to milliseconds
                           deserializationSteps.size(),
                           executionFrames.size());
    }
}