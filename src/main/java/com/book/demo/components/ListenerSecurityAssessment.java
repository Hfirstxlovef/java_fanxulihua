package com.book.demo.components;

public class ListenerSecurityAssessment {
    private int securityScore;
    private String riskLevel;
    private int riskCount;
    private int totalChecks;
    private String recommendation;
    
    public ListenerSecurityAssessment() {
        this.securityScore = 100;
        this.riskLevel = "LOW";
        this.riskCount = 0;
        this.totalChecks = 0;
        this.recommendation = "系统安全状态良好";
    }
    
    public void updateRecommendation() {
        if ("HIGH".equals(riskLevel)) {
            recommendation = "发现高风险Listener，建议立即检查和处理";
        } else if ("MEDIUM".equals(riskLevel)) {
            recommendation = "发现中等风险问题，建议尽快修复";
        } else {
            recommendation = "Listener安全状态良好，继续监控";
        }
    }
    
    // Getters and Setters
    public int getSecurityScore() { return securityScore; }
    public void setSecurityScore(int securityScore) { 
        this.securityScore = securityScore;
        updateRecommendation();
    }
    
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { 
        this.riskLevel = riskLevel;
        updateRecommendation();
    }
    
    public int getRiskCount() { return riskCount; }
    public void setRiskCount(int riskCount) { this.riskCount = riskCount; }
    
    public int getTotalChecks() { return totalChecks; }
    public void setTotalChecks(int totalChecks) { this.totalChecks = totalChecks; }
    
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    
    @Override
    public String toString() {
        return "ListenerSecurityAssessment{" +
               "securityScore=" + securityScore +
               ", riskLevel='" + riskLevel + '\'' +
               ", riskCount=" + riskCount +
               ", totalChecks=" + totalChecks +
               ", recommendation='" + recommendation + '\'' +
               '}';
    }
}