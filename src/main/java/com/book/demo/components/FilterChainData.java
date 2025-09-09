package com.book.demo.components;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FilterChainData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String filterName;
    private String nextFilter;
    private Map<String, String> parameters;
    private long chainStartTime;
    
    public FilterChainData() {
        this.parameters = new HashMap<>();
        this.chainStartTime = System.currentTimeMillis();
    }
    
    public void addParameter(String key, String value) {
        this.parameters.put(key, value);
    }
    
    // 自定义反序列化方法用于演示
    private void readObject(java.io.ObjectInputStream stream) 
            throws java.io.IOException, ClassNotFoundException {
        System.out.println("FilterChainData.readObject() 被调用");
        stream.defaultReadObject();
        
        // 验证Filter链数据完整性
        validateChainData();
        
        System.out.println("FilterChainData 反序列化完成，参数数量: " + 
                         (parameters != null ? parameters.size() : 0));
    }
    
    private void validateChainData() {
        System.out.println("🔍 验证Filter链数据完整性:");
        System.out.println("  - Filter名称: " + filterName);
        System.out.println("  - 下一个Filter: " + nextFilter);
        System.out.println("  - 参数数量: " + (parameters != null ? parameters.size() : 0));
        
        // 检查必要参数
        if (parameters != null) {
            if (parameters.containsKey("userId")) {
                System.out.println("  ✅ 用户ID参数存在");
            } else {
                System.out.println("  ⚠️ 缺少用户ID参数");
            }
            
            if (parameters.containsKey("sessionId")) {
                System.out.println("  ✅ 会话ID参数存在");
            } else {
                System.out.println("  ⚠️ 缺少会话ID参数");
            }
        }
    }
    
    @Override
    public String toString() {
        return "FilterChainData{" +
                "filterName='" + filterName + '\'' +
                ", nextFilter='" + nextFilter + '\'' +
                ", parameterCount=" + (parameters != null ? parameters.size() : 0) +
                ", chainStartTime=" + chainStartTime +
                '}';
    }
    
    // Getters and Setters
    public String getFilterName() { return filterName; }
    public void setFilterName(String filterName) { this.filterName = filterName; }
    
    public String getNextFilter() { return nextFilter; }
    public void setNextFilter(String nextFilter) { this.nextFilter = nextFilter; }
    
    public Map<String, String> getParameters() { return parameters; }
    public void setParameters(Map<String, String> parameters) { this.parameters = parameters; }
    
    public long getChainStartTime() { return chainStartTime; }
    public void setChainStartTime(long chainStartTime) { this.chainStartTime = chainStartTime; }
}