package com.book.demo.memshell;

import java.util.HashMap;
import java.util.Map;

/**
 * 内存马信息类
 */
public class MemoryShellInfo {
    private String id;
    private MemoryShell.Type type;
    private String name;
    private String className;
    private long injectionTime;
    private boolean isActive;
    private String injectionPoint;
    private String description;
    private Map<String, Object> metadata;
    private int accessCount;
    private long lastAccessTime;
    
    public MemoryShellInfo() {
        this.metadata = new HashMap<>();
        this.injectionTime = System.currentTimeMillis();
        this.accessCount = 0;
        this.lastAccessTime = 0;
    }
    
    public MemoryShellInfo(String id, MemoryShell.Type type, String name, String className) {
        this();
        this.id = id;
        this.type = type;
        this.name = name;
        this.className = className;
    }
    
    // 记录访问
    public void recordAccess() {
        this.accessCount++;
        this.lastAccessTime = System.currentTimeMillis();
    }
    
    // 添加元数据
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public MemoryShell.Type getType() { return type; }
    public void setType(MemoryShell.Type type) { this.type = type; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public long getInjectionTime() { return injectionTime; }
    public void setInjectionTime(long injectionTime) { this.injectionTime = injectionTime; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public String getInjectionPoint() { return injectionPoint; }
    public void setInjectionPoint(String injectionPoint) { this.injectionPoint = injectionPoint; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public int getAccessCount() { return accessCount; }
    public void setAccessCount(int accessCount) { this.accessCount = accessCount; }
    
    public long getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(long lastAccessTime) { this.lastAccessTime = lastAccessTime; }
    
    @Override
    public String toString() {
        return "MemoryShellInfo{" +
               "id='" + id + '\'' +
               ", type=" + type +
               ", name='" + name + '\'' +
               ", className='" + className + '\'' +
               ", injectionTime=" + injectionTime +
               ", isActive=" + isActive +
               ", accessCount=" + accessCount +
               '}';
    }
}