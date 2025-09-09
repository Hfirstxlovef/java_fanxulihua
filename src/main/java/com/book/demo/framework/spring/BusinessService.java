package com.book.demo.framework.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class BusinessService implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Autowired(required = false)
    private transient DataService dataService;
    
    private String serviceName = "业务服务";
    private Long serviceId = System.currentTimeMillis();
    
    public String performBusiness(String input) {
        System.out.println("BusinessService处理业务: " + input);
        
        if (dataService != null) {
            return serviceName + " 处理了 " + input + " 并调用了数据服务: " + dataService.getData();
        } else {
            return serviceName + " 处理了 " + input + " (数据服务不可用)";
        }
    }
    
    // 自定义反序列化方法
    private void readObject(java.io.ObjectInputStream stream) 
            throws java.io.IOException, ClassNotFoundException {
        System.out.println("BusinessService.readObject() 被调用");
        
        // 调用默认反序列化
        stream.defaultReadObject();
        
        System.out.println("BusinessService 反序列化完成，服务名: " + serviceName + ", ID: " + serviceId);
        
        // 注意：@Autowired的字段在反序列化后需要重新注入
        System.out.println("注意: 自动注入的字段 dataService 在反序列化后为: " + dataService);
    }
    
    // 自定义序列化方法
    private void writeObject(java.io.ObjectOutputStream stream) 
            throws java.io.IOException {
        System.out.println("BusinessService.writeObject() 被调用");
        
        // 调用默认序列化
        stream.defaultWriteObject();
        
        System.out.println("BusinessService 序列化完成");
    }
    
    // Getters and Setters
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public Long getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }
    
    public DataService getDataService() {
        return dataService;
    }
    
    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }
}