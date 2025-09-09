package com.book.demo.framework.spring;

import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class DataService implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String dataSource = "内存数据库";
    private int connectionCount = 0;
    
    public String getData() {
        connectionCount++;
        System.out.println("DataService.getData() 调用次数: " + connectionCount);
        return "来自 " + dataSource + " 的数据 (调用 #" + connectionCount + ")";
    }
    
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
        System.out.println("数据源已更改为: " + dataSource);
    }
    
    // 演示可能存在的反序列化安全问题的方法
    public void executeQuery(String query) {
        System.out.println("执行查询: " + query);
        // 在真实场景中，这里可能存在SQL注入等风险
        // 这里只是演示，不会真正执行危险操作
    }
    
    // 自定义反序列化方法 - 演示如何在反序列化时进行安全检查
    private void readObject(java.io.ObjectInputStream stream) 
            throws java.io.IOException, ClassNotFoundException {
        System.out.println("DataService.readObject() 开始反序列化");
        
        // 首先进行安全检查
        String callerInfo = getCallerInfo();
        System.out.println("反序列化调用者信息: " + callerInfo);
        
        // 调用默认反序列化
        stream.defaultReadObject();
        
        // 反序列化后的安全验证
        if (dataSource != null && dataSource.contains("../")) {
            System.out.println("警告: 检测到可疑的数据源路径: " + dataSource);
            dataSource = "安全的默认数据源"; // 清理可疑数据
        }
        
        // 重置连接计数器（安全考虑）
        connectionCount = 0;
        
        System.out.println("DataService 反序列化完成，数据源: " + dataSource);
    }
    
    private void writeObject(java.io.ObjectOutputStream stream) 
            throws java.io.IOException {
        System.out.println("DataService.writeObject() 开始序列化");
        
        // 在序列化前可以进行数据清理
        String originalDataSource = dataSource;
        
        // 如果包含敏感信息，可以在序列化时替换
        if (dataSource != null && dataSource.contains("password")) {
            dataSource = dataSource.replaceAll("password=\\w+", "password=***");
            System.out.println("序列化时清理了敏感信息");
        }
        
        // 执行序列化
        stream.defaultWriteObject();
        
        // 恢复原始值（因为序列化不应该影响当前对象状态）
        dataSource = originalDataSource;
        
        System.out.println("DataService 序列化完成");
    }
    
    private String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder callerInfo = new StringBuilder();
        
        // 查找调用栈中的相关信息
        for (int i = 0; i < Math.min(stackTrace.length, 5); i++) {
            StackTraceElement element = stackTrace[i];
            if (!element.getClassName().equals(getClass().getName()) &&
                !element.getMethodName().equals("getCallerInfo")) {
                callerInfo.append(element.getClassName())
                         .append(".")
                         .append(element.getMethodName())
                         .append(":")
                         .append(element.getLineNumber())
                         .append(" ");
            }
        }
        
        return callerInfo.toString();
    }
    
    // Getters
    public String getDataSource() {
        return dataSource;
    }
    
    public int getConnectionCount() {
        return connectionCount;
    }
}