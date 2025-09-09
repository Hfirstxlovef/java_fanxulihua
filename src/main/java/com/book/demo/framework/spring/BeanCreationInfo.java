package com.book.demo.framework.spring;

public class BeanCreationInfo {
    private final String beanName;
    private final Class<?> beanClass;
    private final long creationTime;
    private final boolean isProxy;
    
    public BeanCreationInfo(String beanName, Class<?> beanClass, boolean isProxy) {
        this.beanName = beanName;
        this.beanClass = beanClass;
        this.creationTime = System.currentTimeMillis();
        this.isProxy = isProxy;
    }
    
    // Getters
    public String getBeanName() { return beanName; }
    public Class<?> getBeanClass() { return beanClass; }
    public long getCreationTime() { return creationTime; }
    public boolean isProxy() { return isProxy; }
}