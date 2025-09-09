package com.book.demo.components;

import java.io.Serializable;

public class ServletRequestData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String userId;
    private String action;
    private String payload;
    private long timestamp;
    
    public ServletRequestData() {
        this.timestamp = System.currentTimeMillis();
    }
    
    // 自定义反序列化方法用于演示
    private void readObject(java.io.ObjectInputStream stream) 
            throws java.io.IOException, ClassNotFoundException {
        System.out.println("ServletRequestData.readObject() 被调用");
        stream.defaultReadObject();
        System.out.println("ServletRequestData 反序列化完成: " + this);
    }
    
    @Override
    public String toString() {
        return "ServletRequestData{" +
                "userId='" + userId + '\'' +
                ", action='" + action + '\'' +
                ", payload='" + payload + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}