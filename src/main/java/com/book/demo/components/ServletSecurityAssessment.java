package com.book.demo.components;

public class ServletSecurityAssessment {
    private int securityScore;
    private String riskLevel;
    private int riskCount;
    private int totalChecks;
    
    public ServletSecurityAssessment() {
        this.securityScore = 100;
        this.riskLevel = "LOW";
        this.riskCount = 0;
        this.totalChecks = 0;
    }
    
    public String getRiskDescription() {
        switch (riskLevel) {
            case "LOW":
                return "安全风险较低，未发现明显的安全问题";
            case "MEDIUM":
                return "存在中等安全风险，建议进一步检查";
            case "HIGH":
                return "存在高安全风险，需要立即处理";
            default:
                return "未知风险级别";
        }
    }
    
    public String getSecurityRecommendations() {
        StringBuilder recommendations = new StringBuilder();
        
        if (riskLevel.equals("HIGH")) {
            recommendations.append("• 立即检查和修复发现的安全问题\n");
            recommendations.append("• 实施输入验证和过滤机制\n");
            recommendations.append("• 升级相关依赖包到最新安全版本\n");
        } else if (riskLevel.equals("MEDIUM")) {
            recommendations.append("• 定期进行安全审计\n");
            recommendations.append("• 加强监控和日志记录\n");
            recommendations.append("• 考虑实施额外的安全控制\n");
        } else {
            recommendations.append("• 继续保持当前的安全实践\n");
            recommendations.append("• 定期更新安全策略\n");
            recommendations.append("• 监控新出现的安全威胁\n");
        }
        
        return recommendations.toString();
    }
    
    // Getters and Setters
    public int getSecurityScore() { return securityScore; }
    public void setSecurityScore(int securityScore) { this.securityScore = securityScore; }
    
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    
    public int getRiskCount() { return riskCount; }
    public void setRiskCount(int riskCount) { this.riskCount = riskCount; }
    
    public int getTotalChecks() { return totalChecks; }
    public void setTotalChecks(int totalChecks) { this.totalChecks = totalChecks; }
    
    public String getRecommendation() {
        return getRiskDescription();
    }
}