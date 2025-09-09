package com.book.demo.components;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ListenerEventData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String eventType;
    private String listenerClass;
    private String eventSource;
    private long eventTime;
    private Map<String, Object> eventAttributes;
    private String sessionId;
    private String contextPath;
    
    public ListenerEventData() {
        this.eventTime = System.currentTimeMillis();
        this.eventAttributes = new HashMap<>();
    }
    
    public void addAttribute(String key, Object value) {
        this.eventAttributes.put(key, value);
    }
    
    // Getters and Setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getListenerClass() { return listenerClass; }
    public void setListenerClass(String listenerClass) { this.listenerClass = listenerClass; }
    
    public String getEventSource() { return eventSource; }
    public void setEventSource(String eventSource) { this.eventSource = eventSource; }
    
    public long getEventTime() { return eventTime; }
    public void setEventTime(long eventTime) { this.eventTime = eventTime; }
    
    public Map<String, Object> getEventAttributes() { return eventAttributes; }
    public void setEventAttributes(Map<String, Object> eventAttributes) { this.eventAttributes = eventAttributes; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getContextPath() { return contextPath; }
    public void setContextPath(String contextPath) { this.contextPath = contextPath; }
    
    @Override
    public String toString() {
        return "ListenerEventData{" +
               "eventType='" + eventType + '\'' +
               ", listenerClass='" + listenerClass + '\'' +
               ", eventSource='" + eventSource + '\'' +
               ", sessionId='" + sessionId + '\'' +
               ", contextPath='" + contextPath + '\'' +
               ", attributeCount=" + eventAttributes.size() +
               '}';
    }
}