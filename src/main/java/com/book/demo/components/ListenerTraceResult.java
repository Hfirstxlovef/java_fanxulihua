package com.book.demo.components;

import java.util.List;

public class ListenerTraceResult {
    private final List<ComponentExecutionStep> executionSteps;
    private final ListenerSecurityAssessment securityAssessment;
    
    public ListenerTraceResult(List<ComponentExecutionStep> executionSteps, 
                              ListenerSecurityAssessment securityAssessment) {
        this.executionSteps = executionSteps;
        this.securityAssessment = securityAssessment;
    }
    
    public List<ComponentExecutionStep> getExecutionSteps() {
        return executionSteps;
    }
    
    public ListenerSecurityAssessment getSecurityAssessment() {
        return securityAssessment;
    }
    
    public int getTotalSteps() {
        return executionSteps.size();
    }
    
    public long getWarningCount() {
        return executionSteps.stream().mapToLong(step -> step.isWarning() ? 1 : 0).sum();
    }
    
    public long getErrorCount() {
        return executionSteps.stream().mapToLong(step -> step.isError() ? 1 : 0).sum();
    }
    
    public long getSecurityRelatedCount() {
        return executionSteps.stream().mapToLong(step -> step.isSecurityRelated() ? 1 : 0).sum();
    }
    
    @Override
    public String toString() {
        return "ListenerTraceResult{" +
               "totalSteps=" + getTotalSteps() +
               ", warnings=" + getWarningCount() +
               ", errors=" + getErrorCount() +
               ", securityIssues=" + getSecurityRelatedCount() +
               ", securityScore=" + securityAssessment.getSecurityScore() +
               '}';
    }
}