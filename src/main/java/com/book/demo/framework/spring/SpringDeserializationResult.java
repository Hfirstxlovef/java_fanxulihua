package com.book.demo.framework.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpringDeserializationResult {
    private final List<SpringExecutionStep> executionSteps;
    private final Map<String, BeanCreationInfo> beanCreationMap;
    private final ApplicationContext applicationContext;
    
    public SpringDeserializationResult(List<SpringExecutionStep> executionSteps,
                                     Map<String, BeanCreationInfo> beanCreationMap,
                                     ApplicationContext applicationContext) {
        this.executionSteps = executionSteps;
        this.beanCreationMap = beanCreationMap;
        this.applicationContext = applicationContext;
    }
    
    public void printSummary() {
        System.out.println("=== Spring反序列化演示结果 ===");
        System.out.println("执行步骤数: " + executionSteps.size());
        System.out.println("创建的Bean数: " + beanCreationMap.size());
        
        // 检查ApplicationContext状态
        boolean isActive = true;
        if (applicationContext instanceof ConfigurableApplicationContext) {
            isActive = ((ConfigurableApplicationContext) applicationContext).isActive();
        }
        System.out.println("Spring容器状态: " + (isActive ? "活跃" : "非活跃"));
        
        System.out.println("\nBean创建信息:");
        beanCreationMap.forEach((name, info) -> {
            System.out.println("  " + name + " (" + info.getBeanClass().getSimpleName() + 
                             ") - 代理: " + info.isProxy());
        });
    }
    
    // Getters
    public List<SpringExecutionStep> getExecutionSteps() { return executionSteps; }
    public Map<String, BeanCreationInfo> getBeanCreationMap() { return beanCreationMap; }
    public ApplicationContext getApplicationContext() { return applicationContext; }
    
    public boolean isContextActive() {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            return ((ConfigurableApplicationContext) applicationContext).isActive();
        }
        return true; // 假设其他类型的context是活跃的
    }
    
    // Missing methods used in DeserializationDemoResource
    public List<String> getEducationalSteps() {
        // Return educational steps based on execution steps
        List<String> educationalSteps = new ArrayList<>();
        for (SpringExecutionStep step : executionSteps) {
            educationalSteps.add(step.getDescription());
        }
        return educationalSteps;
    }
    
    public String getEducationLog() {
        // Generate education log from execution steps
        StringBuilder log = new StringBuilder();
        for (SpringExecutionStep step : executionSteps) {
            log.append(step.getDescription()).append("\\n");
        }
        return log.toString();
    }
}