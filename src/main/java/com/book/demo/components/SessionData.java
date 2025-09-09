package com.book.demo.components;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SessionData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String sessionId;
    private String userId;
    private long loginTime;
    private long lastAccessTime;
    private Map<String, String> attributes;
    
    public SessionData() {
        this.attributes = new HashMap<>();
        this.lastAccessTime = System.currentTimeMillis();
    }
    
    public void addAttribute(String key, String value) {
        this.attributes.put(key, value);
    }
    
    // 自定义反序列化方法用于演示
    private void readObject(java.io.ObjectInputStream stream) 
            throws java.io.IOException, ClassNotFoundException {
        System.out.println("SessionData.readObject() 被调用");
        stream.defaultReadObject();
        
        // 模拟Session安全验证
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        
        System.out.println("SessionData 反序列化完成，属性数量: " + attributes.size());
    }
    
    @Override
    public String toString() {
        return "SessionData{" +
                "sessionId='" + sessionId + '\'' +
                ", userId='" + userId + '\'' +
                ", loginTime=" + loginTime +
                ", attributeCount=" + (attributes != null ? attributes.size() : 0) +
                '}';
    }
    
    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public long getLoginTime() { return loginTime; }
    public void setLoginTime(long loginTime) { this.loginTime = loginTime; }
    
    public long getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(long lastAccessTime) { this.lastAccessTime = lastAccessTime; }
    
    public Map<String, String> getAttributes() { return attributes; }
    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }
}