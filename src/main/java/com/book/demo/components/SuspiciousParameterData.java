package com.book.demo.components;

import java.io.Serializable;

public class SuspiciousParameterData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String parameterName;
    private String parameterValue;
    private String sourceIP;
    private long receiveTime;
    
    public SuspiciousParameterData() {
        this.receiveTime = System.currentTimeMillis();
    }
    
    // 自定义反序列化方法用于演示安全检查
    private void readObject(java.io.ObjectInputStream stream) 
            throws java.io.IOException, ClassNotFoundException {
        System.out.println("⚠️ SuspiciousParameterData.readObject() 被调用 - 执行安全检查");
        stream.defaultReadObject();
        
        // 模拟安全检查
        performSecurityCheck();
        
        System.out.println("SuspiciousParameterData 反序列化完成: " + this);
    }
    
    private void performSecurityCheck() {
        System.out.println("🔍 执行参数安全检查:");
        System.out.println("  - 参数名称检查: " + parameterName);
        System.out.println("  - 参数值检查: " + parameterValue);
        System.out.println("  - 来源IP检查: " + sourceIP);
        
        // 检查是否包含危险关键字
        if (parameterName != null && (parameterName.toLowerCase().contains("command") || 
                                    parameterName.toLowerCase().contains("exec"))) {
            System.out.println("⚠️ 检测到可疑参数名称!");
        }
        
        if (parameterValue != null && (parameterValue.contains("Runtime") || 
                                     parameterValue.contains("ProcessBuilder"))) {
            System.out.println("⚠️ 检测到可疑参数值!");
        }
    }
    
    @Override
    public String toString() {
        return "SuspiciousParameterData{" +
                "parameterName='" + parameterName + '\'' +
                ", parameterValue='" + parameterValue + '\'' +
                ", sourceIP='" + sourceIP + '\'' +
                ", receiveTime=" + receiveTime +
                '}';
    }
    
    // Getters and Setters
    public String getParameterName() { return parameterName; }
    public void setParameterName(String parameterName) { this.parameterName = parameterName; }
    
    public String getParameterValue() { return parameterValue; }
    public void setParameterValue(String parameterValue) { this.parameterValue = parameterValue; }
    
    public String getSourceIP() { return sourceIP; }
    public void setSourceIP(String sourceIP) { this.sourceIP = sourceIP; }
    
    public long getReceiveTime() { return receiveTime; }
    public void setReceiveTime(long receiveTime) { this.receiveTime = receiveTime; }
}