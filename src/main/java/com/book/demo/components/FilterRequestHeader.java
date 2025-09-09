package com.book.demo.components;

import java.io.Serializable;

public class FilterRequestHeader implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String headerName;
    private String headerValue;
    private String sourceFilter;
    private long processingTime;
    
    public FilterRequestHeader() {
        this.processingTime = System.currentTimeMillis();
    }
    
    // 自定义反序列化方法用于演示
    private void readObject(java.io.ObjectInputStream stream) 
            throws java.io.IOException, ClassNotFoundException {
        System.out.println("FilterRequestHeader.readObject() 被调用");
        stream.defaultReadObject();
        
        // 模拟请求头安全验证
        performHeaderSecurityCheck();
        
        System.out.println("FilterRequestHeader 反序列化完成: " + this);
    }
    
    private void performHeaderSecurityCheck() {
        System.out.println("🔍 执行请求头安全检查:");
        System.out.println("  - 头名称检查: " + headerName);
        System.out.println("  - 头值检查: " + headerValue);
        System.out.println("  - 来源过滤器: " + sourceFilter);
        
        // 检查是否包含可疑内容
        if (headerValue != null && (headerValue.contains("<script") || 
                                  headerValue.contains("javascript:") ||
                                  headerValue.contains("eval("))) {
            System.out.println("⚠️ 检测到可疑的头值内容!");
        }
    }
    
    @Override
    public String toString() {
        return "FilterRequestHeader{" +
                "headerName='" + headerName + '\'' +
                ", headerValue='" + headerValue + '\'' +
                ", sourceFilter='" + sourceFilter + '\'' +
                ", processingTime=" + processingTime +
                '}';
    }
    
    // Getters and Setters
    public String getHeaderName() { return headerName; }
    public void setHeaderName(String headerName) { this.headerName = headerName; }
    
    public String getHeaderValue() { return headerValue; }
    public void setHeaderValue(String headerValue) { this.headerValue = headerValue; }
    
    public String getSourceFilter() { return sourceFilter; }
    public void setSourceFilter(String sourceFilter) { this.sourceFilter = sourceFilter; }
    
    public long getProcessingTime() { return processingTime; }
    public void setProcessingTime(long processingTime) { this.processingTime = processingTime; }
}