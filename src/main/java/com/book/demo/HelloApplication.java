package com.book.demo;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
public class HelloApplication extends Application {
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        
        // 显式注册资源类
        classes.add(HelloResource.class);
        classes.add(DeserializationDemoResource.class);
        
        return classes;
    }
}