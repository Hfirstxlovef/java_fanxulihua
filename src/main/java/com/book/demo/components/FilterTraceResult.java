package com.book.demo.components;

import java.util.List;

public class FilterTraceResult {
    private final List<ComponentExecutionStep> executionSteps;
    private final FilterSecurityAssessment securityAssessment;
    private final long executionTime;
    
    public FilterTraceResult(List<ComponentExecutionStep> executionSteps, 
                           FilterSecurityAssessment securityAssessment) {
        this.executionSteps = executionSteps;
        this.securityAssessment = securityAssessment;
        this.executionTime = System.currentTimeMillis();
    }
    
    public String toJsonString() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"status\":\"success\",");
        json.append("\"componentType\":\"filter\",");
        json.append("\"executionTime\":").append(executionTime).append(",");
        json.append("\"totalSteps\":").append(executionSteps.size()).append(",");
        
        // 统计信息
        long warningCount = executionSteps.stream().mapToLong(step -> step.isWarning() ? 1 : 0).sum();
        long errorCount = executionSteps.stream().mapToLong(step -> step.isError() ? 1 : 0).sum();
        long securitySteps = executionSteps.stream().mapToLong(step -> step.isSecurityRelated() ? 1 : 0).sum();
        
        json.append("\"statistics\":{");
        json.append("\"totalSteps\":").append(executionSteps.size()).append(",");
        json.append("\"warningCount\":").append(warningCount).append(",");
        json.append("\"errorCount\":").append(errorCount).append(",");
        json.append("\"securitySteps\":").append(securitySteps).append(",");
        json.append("\"securityScore\":").append(securityAssessment.getSecurityScore()).append(",");
        json.append("\"riskLevel\":\"").append(securityAssessment.getRiskLevel()).append("\"");
        json.append("},");
        
        // 执行步骤
        json.append("\"executionSteps\":[");
        for (int i = 0; i < executionSteps.size(); i++) {
            if (i > 0) json.append(",");
            ComponentExecutionStep step = executionSteps.get(i);
            json.append("{");
            json.append("\"stepNumber\":").append(step.getStepNumber()).append(",");
            json.append("\"timestamp\":").append(step.getTimestamp()).append(",");
            json.append("\"stepType\":\"").append(escapeJson(step.getStepType())).append("\",");
            json.append("\"message\":\"").append(escapeJson(step.getMessage())).append("\",");
            json.append("\"isWarning\":").append(step.isWarning()).append(",");
            json.append("\"isError\":").append(step.isError()).append(",");
            json.append("\"isSecurityRelated\":").append(step.isSecurityRelated());
            json.append("}");
        }
        json.append("],");
        
        // 安全评估
        json.append("\"securityAssessment\":{");
        json.append("\"securityScore\":").append(securityAssessment.getSecurityScore()).append(",");
        json.append("\"riskLevel\":\"").append(securityAssessment.getRiskLevel()).append("\",");
        json.append("\"riskCount\":").append(securityAssessment.getRiskCount()).append(",");
        json.append("\"totalChecks\":").append(securityAssessment.getTotalChecks());
        json.append("}");
        
        json.append("}");
        return json.toString();
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    // Getters
    public List<ComponentExecutionStep> getExecutionSteps() { return executionSteps; }
    public FilterSecurityAssessment getSecurityAssessment() { return securityAssessment; }
    public long getExecutionTime() { return executionTime; }
    
    public long getWarningCount() {
        return executionSteps.stream().mapToLong(step -> step.isWarning() ? 1 : 0).sum();
    }
    
    public long getErrorCount() {
        return executionSteps.stream().mapToLong(step -> step.isError() ? 1 : 0).sum();
    }
}