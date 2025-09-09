package com.book.demo.components;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FilterConfigData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String filterName;
    private String servletContext;
    private Map<String, String> initParameters;
    private long configTime;
    
    public FilterConfigData() {
        this.initParameters = new HashMap<>();
        this.configTime = System.currentTimeMillis();
    }
    
    public void setInitParameter(String name, String value) {
        this.initParameters.put(name, value);
    }
    
    // 自定义反序列化方法用于演示
    private void readObject(java.io.ObjectInputStream stream) 
            throws java.io.IOException, ClassNotFoundException {
        System.out.println("FilterConfigData.readObject() 被调用");
        stream.defaultReadObject();
        
        // 验证配置数据安全性
        validateConfigSecurity();
        
        System.out.println("FilterConfigData 反序列化完成: " + this);
    }
    
    private void validateConfigSecurity() {
        System.out.println("🔍 验证Filter配置安全性:");
        System.out.println("  - Filter名称: " + filterName);
        System.out.println("  - Servlet上下文: " + servletContext);
        
        if (initParameters != null) {
            System.out.println("  - 初始化参数数量: " + initParameters.size());
            
            // 检查安全相关配置
            String securityLevel = initParameters.get("securityLevel");
            if (securityLevel != null) {
                System.out.println("  - 安全级别: " + securityLevel);
                if (!"HIGH".equals(securityLevel)) {
                    System.out.println("  ⚠️ 建议设置安全级别为HIGH");
                }
            }
            
            String allowedIPs = initParameters.get("allowedIPs");
            if (allowedIPs != null) {
                System.out.println("  - 允许的IP范围: " + allowedIPs);
            } else {
                System.out.println("  ⚠️ 未配置IP访问控制");
            }
        }
    }
    
    @Override
    public String toString() {
        return "FilterConfigData{" +
                "filterName='" + filterName + '\'' +
                ", servletContext='" + servletContext + '\'' +
                ", initParameterCount=" + (initParameters != null ? initParameters.size() : 0) +
                ", configTime=" + configTime +
                '}';
    }
    
    // Getters and Setters
    public String getFilterName() { return filterName; }
    public void setFilterName(String filterName) { this.filterName = filterName; }
    
    public String getServletContext() { return servletContext; }
    public void setServletContext(String servletContext) { this.servletContext = servletContext; }
    
    public Map<String, String> getInitParameters() { return initParameters; }
    public void setInitParameters(Map<String, String> initParameters) { this.initParameters = initParameters; }
    
    public long getConfigTime() { return configTime; }
    public void setConfigTime(long configTime) { this.configTime = configTime; }
}