package com.book.demo.memshell;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 内存马实时检测器
 * 用于检测和监控系统中的内存马
 */
public class MemoryShellDetector {
    
    private static final Map<String, DetectionResult> detectionCache = new ConcurrentHashMap<>();
    private static final List<DetectionRecord> detectionHistory = Collections.synchronizedList(new ArrayList<>());
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    private static boolean realTimeDetectionEnabled = false;
    private static long lastScanTime = 0;
    
    /**
     * 检测结果
     */
    public static class DetectionResult {
        private final String id;
        private final MemoryShell.Type type;
        private final String componentName;
        private final String className;
        private final boolean suspicious;
        private final int riskLevel; // 1-10
        private final List<String> suspiciousFeatures;
        private final long detectionTime;
        private final Map<String, Object> metadata;
        
        public DetectionResult(String id, MemoryShell.Type type, String componentName, String className,
                             boolean suspicious, int riskLevel, List<String> suspiciousFeatures) {
            this.id = id;
            this.type = type;
            this.componentName = componentName;
            this.className = className;
            this.suspicious = suspicious;
            this.riskLevel = riskLevel;
            this.suspiciousFeatures = new ArrayList<>(suspiciousFeatures);
            this.detectionTime = System.currentTimeMillis();
            this.metadata = new HashMap<>();
        }
        
        // Getters
        public String getId() { return id; }
        public MemoryShell.Type getType() { return type; }
        public String getComponentName() { return componentName; }
        public String getClassName() { return className; }
        public boolean isSuspicious() { return suspicious; }
        public int getRiskLevel() { return riskLevel; }
        public List<String> getSuspiciousFeatures() { return suspiciousFeatures; }
        public long getDetectionTime() { return detectionTime; }
        public Map<String, Object> getMetadata() { return metadata; }
        
        public void addMetadata(String key, Object value) {
            this.metadata.put(key, value);
        }
    }
    
    /**
     * 检测记录
     */
    public static class DetectionRecord {
        private final long scanTime;
        private final int totalComponents;
        private final int suspiciousComponents;
        private final List<DetectionResult> suspiciousResults;
        
        public DetectionRecord(long scanTime, int totalComponents, int suspiciousComponents, 
                             List<DetectionResult> suspiciousResults) {
            this.scanTime = scanTime;
            this.totalComponents = totalComponents;
            this.suspiciousComponents = suspiciousComponents;
            this.suspiciousResults = new ArrayList<>(suspiciousResults);
        }
        
        // Getters
        public long getScanTime() { return scanTime; }
        public int getTotalComponents() { return totalComponents; }
        public int getSuspiciousComponents() { return suspiciousComponents; }
        public List<DetectionResult> getSuspiciousResults() { return suspiciousResults; }
    }
    
    /**
     * 启动实时检测
     */
    public static void startRealTimeDetection() {
        if (realTimeDetectionEnabled) {
            System.out.println("[DETECTOR] 实时检测已在运行中");
            return;
        }
        
        realTimeDetectionEnabled = true;
        
        // 每30秒执行一次完整扫描
        scheduler.scheduleAtFixedRate(() -> {
            try {
                performFullScan();
            } catch (Exception e) {
                System.err.println("[DETECTOR] 实时检测异常: " + e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS);
        
        // 每5秒执行一次快速检测
        scheduler.scheduleAtFixedRate(() -> {
            try {
                performQuickScan();
            } catch (Exception e) {
                System.err.println("[DETECTOR] 快速检测异常: " + e.getMessage());
            }
        }, 5, 5, TimeUnit.SECONDS);
        
        System.out.println("[DETECTOR] 内存马实时检测已启动");
    }
    
    /**
     * 停止实时检测
     */
    public static void stopRealTimeDetection() {
        realTimeDetectionEnabled = false;
        scheduler.shutdown();
        System.out.println("[DETECTOR] 内存马实时检测已停止");
    }
    
    /**
     * 执行完整扫描
     */
    public static List<DetectionResult> performFullScan() {
        System.out.println("[DETECTOR] 开始完整内存马扫描");
        
        List<DetectionResult> allResults = new ArrayList<>();
        List<DetectionResult> suspiciousResults = new ArrayList<>();
        
        try {
            // 扫描Servlet
            allResults.addAll(scanServlets());
            
            // 扫描Filter
            allResults.addAll(scanFilters());
            
            // 扫描Listener
            allResults.addAll(scanListeners());
            
            // 筛选可疑结果
            for (DetectionResult result : allResults) {
                if (result.isSuspicious()) {
                    suspiciousResults.add(result);
                }
                detectionCache.put(result.getId(), result);
            }
            
            // 记录扫描结果
            DetectionRecord record = new DetectionRecord(
                System.currentTimeMillis(),
                allResults.size(),
                suspiciousResults.size(),
                suspiciousResults
            );
            
            detectionHistory.add(record);
            lastScanTime = System.currentTimeMillis();
            
            // 限制历史记录大小
            if (detectionHistory.size() > 50) {
                detectionHistory.remove(0);
            }
            
            System.out.println("[DETECTOR] 扫描完成: 总组件 " + allResults.size() + 
                             ", 可疑组件 " + suspiciousResults.size());
            
            return suspiciousResults;
            
        } catch (Exception e) {
            System.err.println("[DETECTOR] 完整扫描失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 执行快速扫描（仅检查已知的活跃内存马）
     */
    public static List<DetectionResult> performQuickScan() {
        List<DetectionResult> results = new ArrayList<>();
        
        // 检查已知的活跃内存马
        Map<String, MemoryShell> activeShells = MemoryShellInjector.getActiveShells();
        
        for (MemoryShell shell : activeShells.values()) {
            if (shell.isActive()) {
                List<String> features = Arrays.asList("已知内存马", "活跃状态");
                DetectionResult result = new DetectionResult(
                    shell.getInfo().getId(),
                    shell.getType(),
                    shell.getName(),
                    shell.getClass().getName(),
                    true,
                    10, // 最高风险级别
                    features
                );
                
                result.addMetadata("source", "MemoryShellInjector");
                result.addMetadata("injectionTime", shell.getInfo().getInjectionTime());
                result.addMetadata("accessCount", shell.getInfo().getAccessCount());
                
                results.add(result);
            }
        }
        
        return results;
    }
    
    /**
     * 扫描Servlet
     */
    private static List<DetectionResult> scanServlets() {
        List<DetectionResult> results = new ArrayList<>();
        
        try {
            StandardContext context = getCurrentStandardContext();
            if (context == null) {
                return results;
            }
            
            // 获取所有Servlet
            Container[] children = context.findChildren();
            for (Container child : children) {
                if (child instanceof StandardWrapper) {
                    StandardWrapper wrapper = (StandardWrapper) child;
                    
                    try {
                        Servlet servlet = wrapper.getServlet();
                        if (servlet != null) {
                            DetectionResult result = analyzeServlet(wrapper, servlet);
                            if (result != null) {
                                results.add(result);
                            }
                        }
                    } catch (Exception e) {
                        // 忽略单个Servlet的分析错误
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("[DETECTOR] Servlet扫描失败: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * 扫描Filter
     */
    private static List<DetectionResult> scanFilters() {
        List<DetectionResult> results = new ArrayList<>();
        
        try {
            StandardContext context = getCurrentStandardContext();
            if (context == null) {
                return results;
            }
            
            // 获取FilterConfigs
            Field filterConfigsField = StandardContext.class.getDeclaredField("filterConfigs");
            filterConfigsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            HashMap<String, ApplicationFilterConfig> filterConfigs = 
                (HashMap<String, ApplicationFilterConfig>) filterConfigsField.get(context);
            
            for (Map.Entry<String, ApplicationFilterConfig> entry : filterConfigs.entrySet()) {
                try {
                    ApplicationFilterConfig filterConfig = entry.getValue();
                    
                    // Use reflection to access getFilter()
                    java.lang.reflect.Method getFilterMethod = ApplicationFilterConfig.class.getDeclaredMethod("getFilter");
                    getFilterMethod.setAccessible(true);
                    Filter filter = (Filter) getFilterMethod.invoke(filterConfig);
                    
                    if (filter != null) {
                        DetectionResult result = analyzeFilter(entry.getKey(), filter, filterConfig);
                        if (result != null) {
                            results.add(result);
                        }
                    }
                } catch (Exception e) {
                    // 忽略单个Filter的分析错误
                }
            }
            
        } catch (Exception e) {
            System.err.println("[DETECTOR] Filter扫描失败: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * 扫描Listener
     */
    private static List<DetectionResult> scanListeners() {
        List<DetectionResult> results = new ArrayList<>();
        
        try {
            StandardContext context = getCurrentStandardContext();
            if (context == null) {
                return results;
            }
            
            // 获取ApplicationEventListeners
            Field eventListenersField = StandardContext.class.getDeclaredField("applicationEventListenersObjects");
            eventListenersField.setAccessible(true);
            Object[] eventListeners = (Object[]) eventListenersField.get(context);
            
            if (eventListeners != null) {
                for (int i = 0; i < eventListeners.length; i++) {
                    Object listener = eventListeners[i];
                    if (listener != null) {
                        DetectionResult result = analyzeListener("EventListener_" + i, listener);
                        if (result != null) {
                            results.add(result);
                        }
                    }
                }
            }
            
            // 获取ApplicationLifecycleListeners
            Field lifecycleListenersField = StandardContext.class.getDeclaredField("applicationLifecycleListenersObjects");
            lifecycleListenersField.setAccessible(true);
            Object[] lifecycleListeners = (Object[]) lifecycleListenersField.get(context);
            
            if (lifecycleListeners != null) {
                for (int i = 0; i < lifecycleListeners.length; i++) {
                    Object listener = lifecycleListeners[i];
                    if (listener != null) {
                        DetectionResult result = analyzeListener("LifecycleListener_" + i, listener);
                        if (result != null) {
                            results.add(result);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("[DETECTOR] Listener扫描失败: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * 分析Servlet
     */
    private static DetectionResult analyzeServlet(StandardWrapper wrapper, Servlet servlet) {
        String servletName = wrapper.getName();
        String className = servlet.getClass().getName();
        List<String> suspiciousFeatures = new ArrayList<>();
        boolean suspicious = false;
        int riskLevel = 1;
        
        // 检查可疑类名
        if (isSuspiciousClassName(className)) {
            suspiciousFeatures.add("可疑类名: " + className);
            suspicious = true;
            riskLevel += 3;
        }
        
        // 检查是否是内存马
        if (servlet instanceof MemoryShell) {
            suspiciousFeatures.add("实现了MemoryShell接口");
            suspicious = true;
            riskLevel += 5;
        }
        
        // 检查动态注册
        try {
            String[] mappings = wrapper.findMappings();
            if (mappings != null && mappings.length > 0) {
                for (String mapping : mappings) {
                    if (isSuspiciousMapping(mapping)) {
                        suspiciousFeatures.add("可疑URL映射: " + mapping);
                        suspicious = true;
                        riskLevel += 2;
                    }
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
        
        // 检查运行时添加的特征
        if (isRuntimeAdded(servlet)) {
            suspiciousFeatures.add("运行时动态添加");
            suspicious = true;
            riskLevel += 4;
        }
        
        String id = "servlet_" + servletName + "_" + System.currentTimeMillis();
        DetectionResult result = new DetectionResult(id, MemoryShell.Type.SERVLET, servletName, 
                                                   className, suspicious, Math.min(riskLevel, 10), 
                                                   suspiciousFeatures);
        
        result.addMetadata("servletName", servletName);
        result.addMetadata("loadOnStartup", wrapper.getLoadOnStartup());
        
        return result;
    }
    
    /**
     * 分析Filter
     */
    private static DetectionResult analyzeFilter(String filterName, Filter filter, 
                                               ApplicationFilterConfig filterConfig) {
        String className = filter.getClass().getName();
        List<String> suspiciousFeatures = new ArrayList<>();
        boolean suspicious = false;
        int riskLevel = 1;
        
        // 检查可疑类名
        if (isSuspiciousClassName(className)) {
            suspiciousFeatures.add("可疑类名: " + className);
            suspicious = true;
            riskLevel += 3;
        }
        
        // 检查是否是内存马
        if (filter instanceof MemoryShell) {
            suspiciousFeatures.add("实现了MemoryShell接口");
            suspicious = true;
            riskLevel += 5;
        }
        
        // 检查URL模式
        try {
            // Use reflection to access getFilterDef()
            java.lang.reflect.Method getFilterDefMethod = ApplicationFilterConfig.class.getDeclaredMethod("getFilterDef");
            getFilterDefMethod.setAccessible(true);
            FilterDef filterDef = (FilterDef) getFilterDefMethod.invoke(filterConfig);
            // 这里可以进一步检查FilterMap的配置
        } catch (Exception e) {
            // 忽略错误
        }
        
        // 检查运行时添加的特征
        if (isRuntimeAdded(filter)) {
            suspiciousFeatures.add("运行时动态添加");
            suspicious = true;
            riskLevel += 4;
        }
        
        String id = "filter_" + filterName + "_" + System.currentTimeMillis();
        DetectionResult result = new DetectionResult(id, MemoryShell.Type.FILTER, filterName, 
                                                   className, suspicious, Math.min(riskLevel, 10), 
                                                   suspiciousFeatures);
        
        result.addMetadata("filterName", filterName);
        
        return result;
    }
    
    /**
     * 分析Listener
     */
    private static DetectionResult analyzeListener(String listenerName, Object listener) {
        String className = listener.getClass().getName();
        List<String> suspiciousFeatures = new ArrayList<>();
        boolean suspicious = false;
        int riskLevel = 1;
        
        // 检查可疑类名
        if (isSuspiciousClassName(className)) {
            suspiciousFeatures.add("可疑类名: " + className);
            suspicious = true;
            riskLevel += 3;
        }
        
        // 检查是否是内存马
        if (listener instanceof MemoryShell) {
            suspiciousFeatures.add("实现了MemoryShell接口");
            suspicious = true;
            riskLevel += 5;
        }
        
        // 检查运行时添加的特征
        if (isRuntimeAdded(listener)) {
            suspiciousFeatures.add("运行时动态添加");
            suspicious = true;
            riskLevel += 4;
        }
        
        // 检查实现的接口
        Class<?>[] interfaces = listener.getClass().getInterfaces();
        int listenerInterfaceCount = 0;
        for (Class<?> intf : interfaces) {
            if (intf.getName().contains("Listener")) {
                listenerInterfaceCount++;
            }
        }
        
        if (listenerInterfaceCount > 3) {
            suspiciousFeatures.add("实现过多Listener接口: " + listenerInterfaceCount);
            suspicious = true;
            riskLevel += 2;
        }
        
        String id = "listener_" + listenerName + "_" + System.currentTimeMillis();
        DetectionResult result = new DetectionResult(id, MemoryShell.Type.LISTENER, listenerName, 
                                                   className, suspicious, Math.min(riskLevel, 10), 
                                                   suspiciousFeatures);
        
        result.addMetadata("listenerName", listenerName);
        result.addMetadata("interfaceCount", listenerInterfaceCount);
        
        return result;
    }
    
    /**
     * 检查是否是可疑的类名
     */
    private static boolean isSuspiciousClassName(String className) {
        String[] suspiciousPatterns = {
            "Shell", "Command", "Exec", "Runtime", "Memory", "Inject", 
            "Backdoor", "Webshell", "Evil", "Malicious", "Attack", "Exploit"
        };
        
        String lowerClassName = className.toLowerCase();
        for (String pattern : suspiciousPatterns) {
            if (lowerClassName.contains(pattern.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查是否是可疑的URL映射
     */
    private static boolean isSuspiciousMapping(String mapping) {
        String[] suspiciousMappings = {
            "/shell", "/cmd", "/exec", "/backdoor", "/webshell", "/hack"
        };
        
        String lowerMapping = mapping.toLowerCase();
        for (String suspicious : suspiciousMappings) {
            if (lowerMapping.contains(suspicious)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查是否是运行时添加的组件
     */
    private static boolean isRuntimeAdded(Object component) {
        // 简单的启发式检查
        // 在实际实现中，可以通过检查类加载器、创建时间等来判断
        try {
            // 检查是否在我们的包中（这通常表示是动态创建的）
            String className = component.getClass().getName();
            if (className.startsWith("com.book.demo.memshell")) {
                return true;
            }
            
            // 检查类加载器
            ClassLoader loader = component.getClass().getClassLoader();
            if (loader != null && loader.getClass().getName().contains("WebappClassLoader")) {
                // 进一步检查可以在这里实现
                return false;
            }
            
        } catch (Exception e) {
            // 忽略错误
        }
        
        return false;
    }
    
    /**
     * 获取当前的StandardContext
     */
    private static StandardContext getCurrentStandardContext() throws Exception {
        try {
            Thread currentThread = Thread.currentThread();
            ClassLoader classLoader = currentThread.getContextClassLoader();
            
            if (classLoader.getClass().getName().contains("WebappClassLoader")) {
                Field contextField = classLoader.getClass().getDeclaredField("context");
                contextField.setAccessible(true);
                Object context = contextField.get(classLoader);
                
                if (context instanceof StandardContext) {
                    return (StandardContext) context;
                }
            }
            
        } catch (Exception e) {
            System.err.println("[DETECTOR] 获取StandardContext失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 获取检测缓存
     */
    public static Map<String, DetectionResult> getDetectionCache() {
        return new HashMap<>(detectionCache);
    }
    
    /**
     * 获取检测历史
     */
    public static List<DetectionRecord> getDetectionHistory() {
        return new ArrayList<>(detectionHistory);
    }
    
    /**
     * 获取统计信息
     */
    public static Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("realTimeDetectionEnabled", realTimeDetectionEnabled);
        stats.put("lastScanTime", lastScanTime);
        stats.put("totalDetections", detectionCache.size());
        stats.put("detectionHistory", detectionHistory.size());
        
        // 统计可疑组件
        long suspiciousCount = detectionCache.values().stream()
                .mapToLong(result -> result.isSuspicious() ? 1 : 0).sum();
        stats.put("suspiciousComponents", suspiciousCount);
        
        // 按类型统计
        Map<String, Integer> typeStats = new HashMap<>();
        for (DetectionResult result : detectionCache.values()) {
            String typeName = result.getType().getName();
            typeStats.put(typeName, typeStats.getOrDefault(typeName, 0) + 1);
        }
        stats.put("componentsByType", typeStats);
        
        return stats;
    }
    
    /**
     * 清理缓存和历史
     */
    public static void clearCache() {
        detectionCache.clear();
        detectionHistory.clear();
        System.out.println("[DETECTOR] 检测缓存和历史已清理");
    }
    
    /**
     * 检查实时检测状态
     */
    public static boolean isRealTimeDetectionEnabled() {
        return realTimeDetectionEnabled;
    }
}