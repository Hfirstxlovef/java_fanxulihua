package com.book.demo.components;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ListenerContextData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String contextName;
    private String contextPath;
    private String serverInfo;
    private Map<String, String> initParameters;
    private Map<String, Object> contextAttributes;
    private long creationTime;
    private boolean isSecure;
    
    public ListenerContextData() {
        this.creationTime = System.currentTimeMillis();
        this.initParameters = new HashMap<>();
        this.contextAttributes = new HashMap<>();
    }
    
    public void addInitParameter(String key, String value) {
        this.initParameters.put(key, value);
    }
    
    public void addContextAttribute(String key, Object value) {
        this.contextAttributes.put(key, value);
    }
    
    // Getters and Setters
    public String getContextName() { return contextName; }
    public void setContextName(String contextName) { this.contextName = contextName; }
    
    public String getContextPath() { return contextPath; }
    public void setContextPath(String contextPath) { this.contextPath = contextPath; }
    
    public String getServerInfo() { return serverInfo; }
    public void setServerInfo(String serverInfo) { this.serverInfo = serverInfo; }
    
    public Map<String, String> getInitParameters() { return initParameters; }
    public void setInitParameters(Map<String, String> initParameters) { this.initParameters = initParameters; }
    
    public Map<String, Object> getContextAttributes() { return contextAttributes; }
    public void setContextAttributes(Map<String, Object> contextAttributes) { this.contextAttributes = contextAttributes; }
    
    public long getCreationTime() { return creationTime; }
    public void setCreationTime(long creationTime) { this.creationTime = creationTime; }
    
    public boolean isSecure() { return isSecure; }
    public void setSecure(boolean secure) { isSecure = secure; }
    
    @Override
    public String toString() {
        return "ListenerContextData{" +
               "contextName='" + contextName + '\'' +
               ", contextPath='" + contextPath + '\'' +
               ", serverInfo='" + serverInfo + '\'' +
               ", isSecure=" + isSecure +
               ", initParameterCount=" + initParameters.size() +
               ", contextAttributeCount=" + contextAttributes.size() +
               '}';
    }
}