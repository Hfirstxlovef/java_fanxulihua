package com.book.demo.components;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ListenerConfigData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String listenerName;
    private String listenerClass;
    private Map<String, String> configuration;
    private String registrationMethod;
    private long registrationTime;
    private boolean isDynamicRegistration;
    private String sourceLocation;
    
    public ListenerConfigData() {
        this.registrationTime = System.currentTimeMillis();
        this.configuration = new HashMap<>();
    }
    
    public void addConfiguration(String key, String value) {
        this.configuration.put(key, value);
    }
    
    // Getters and Setters
    public String getListenerName() { return listenerName; }
    public void setListenerName(String listenerName) { this.listenerName = listenerName; }
    
    public String getListenerClass() { return listenerClass; }
    public void setListenerClass(String listenerClass) { this.listenerClass = listenerClass; }
    
    public Map<String, String> getConfiguration() { return configuration; }
    public void setConfiguration(Map<String, String> configuration) { this.configuration = configuration; }
    
    public String getRegistrationMethod() { return registrationMethod; }
    public void setRegistrationMethod(String registrationMethod) { this.registrationMethod = registrationMethod; }
    
    public long getRegistrationTime() { return registrationTime; }
    public void setRegistrationTime(long registrationTime) { this.registrationTime = registrationTime; }
    
    public boolean isDynamicRegistration() { return isDynamicRegistration; }
    public void setDynamicRegistration(boolean dynamicRegistration) { isDynamicRegistration = dynamicRegistration; }
    
    public String getSourceLocation() { return sourceLocation; }
    public void setSourceLocation(String sourceLocation) { this.sourceLocation = sourceLocation; }
    
    @Override
    public String toString() {
        return "ListenerConfigData{" +
               "listenerName='" + listenerName + '\'' +
               ", listenerClass='" + listenerClass + '\'' +
               ", registrationMethod='" + registrationMethod + '\'' +
               ", isDynamicRegistration=" + isDynamicRegistration +
               ", sourceLocation='" + sourceLocation + '\'' +
               ", configurationCount=" + configuration.size() +
               '}';
    }
}