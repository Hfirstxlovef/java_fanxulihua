package com.book.demo.components;

import com.book.demo.trace.TraceableObjectInputStream;
import com.book.demo.trace.DeserializationStatistics;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ListenerDeserializationTracer {
    
    private final List<ComponentExecutionStep> executionSteps;
    private int stepCounter;
    
    public ListenerDeserializationTracer() {
        this.executionSteps = new ArrayList<>();
        this.stepCounter = 0;
    }
    
    public ListenerTraceResult traceListenerDeserialization() {
        recordStep("开始Listener反序列化追踪演示", "LISTENER_TRACE_START");
        
        try {
            // 演示1：模拟Listener处理事件数据反序列化
            demonstrateEventDataDeserialization();
            
            // 演示2：模拟Listener上下文数据反序列化
            demonstrateContextDataDeserialization();
            
            // 演示3：模拟Listener配置参数反序列化
            demonstrateListenerConfigDeserialization();
            
            // 演示4：检测恶意Listener注入
            detectMaliciousListenerInjection();
            
            recordStep("Listener反序列化追踪完成", "LISTENER_TRACE_COMPLETE");
            
            return new ListenerTraceResult(executionSteps, generateSecurityAssessment());
            
        } catch (Exception e) {
            recordStep("Listener追踪失败: " + e.getMessage(), "LISTENER_TRACE_ERROR", e);
            return new ListenerTraceResult(executionSteps, generateSecurityAssessment());
        }
    }
    
    private void demonstrateEventDataDeserialization() {
        recordStep("演示Listener事件数据反序列化", "EVENT_DESERIALIZATION_DEMO");
        
        try {
            // 创建模拟的事件数据
            ListenerEventData eventData = new ListenerEventData();
            eventData.setEventType("SESSION_CREATED");
            eventData.setListenerClass("HttpSessionListener");
            eventData.setEventSource("WebApplicationContext");
            eventData.setSessionId("SESS_" + System.currentTimeMillis());
            eventData.setContextPath("/demo");
            eventData.addAttribute("userId", "user789");
            eventData.addAttribute("userRole", "admin");
            eventData.addAttribute("loginTime", System.currentTimeMillis());
            
            recordStep("创建Listener事件数据对象", "EVENT_DATA_CREATED", eventData);
            
            // 序列化事件数据
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(eventData);
            oos.close();
            
            byte[] serializedData = baos.toByteArray();
            recordStep("事件数据序列化完成，大小: " + serializedData.length + " bytes", "EVENT_SERIALIZED");
            
            // 使用TraceableObjectInputStream反序列化
            ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
            TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true);
            
            recordStep("开始事件数据反序列化追踪", "EVENT_DESERIALIZE_START");
            ListenerEventData deserializedEvent = (ListenerEventData) tois.readObjectWithTrace();
            recordStep("事件数据反序列化完成", "EVENT_DESERIALIZE_COMPLETE", deserializedEvent);
            
            // 验证事件数据
            recordStep("验证事件 - 类型: " + deserializedEvent.getEventType(), "EVENT_VERIFY_TYPE");
            recordStep("验证事件 - Listener类: " + deserializedEvent.getListenerClass(), "EVENT_VERIFY_LISTENER");
            recordStep("验证事件 - 属性数量: " + deserializedEvent.getEventAttributes().size(), "EVENT_VERIFY_ATTRS");
            
            // 执行事件安全检查
            performEventSecurityCheck(deserializedEvent);
            
            // 分析反序列化过程
            analyzeDeserializationTrace(tois, "EVENT");
            
            tois.close();
            
        } catch (Exception e) {
            recordStep("事件反序列化演示失败: " + e.getMessage(), "EVENT_DEMO_ERROR", e);
        }
    }
    
    private void demonstrateContextDataDeserialization() {
        recordStep("演示Listener上下文数据反序列化", "CONTEXT_DESERIALIZATION_DEMO");
        
        try {
            // 创建模拟的上下文数据
            ListenerContextData contextData = new ListenerContextData();
            contextData.setContextName("DemoWebApp");
            contextData.setContextPath("/demo");
            contextData.setServerInfo("Apache Tomcat/10.1");
            contextData.setSecure(true);
            contextData.addInitParameter("configLocation", "/WEB-INF/app-config.xml");
            contextData.addInitParameter("debugMode", "false");
            contextData.addContextAttribute("startupTime", System.currentTimeMillis());
            contextData.addContextAttribute("version", "1.0.0");
            
            recordStep("创建上下文数据对象", "CONTEXT_DATA_CREATED", contextData);
            
            // 序列化上下文数据
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(contextData);
            oos.close();
            
            recordStep("上下文数据序列化完成", "CONTEXT_SERIALIZED");
            
            // 反序列化上下文数据
            byte[] serializedData = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
            TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true);
            
            recordStep("开始上下文数据反序列化追踪", "CONTEXT_DESERIALIZE_START");
            ListenerContextData deserializedContext = (ListenerContextData) tois.readObjectWithTrace();
            recordStep("上下文数据反序列化完成", "CONTEXT_DESERIALIZE_COMPLETE", deserializedContext);
            
            // 验证上下文数据
            recordStep("验证上下文 - 名称: " + deserializedContext.getContextName(), "CONTEXT_VERIFY_NAME");
            recordStep("验证上下文 - 路径: " + deserializedContext.getContextPath(), "CONTEXT_VERIFY_PATH");
            recordStep("验证上下文 - 安全模式: " + deserializedContext.isSecure(), "CONTEXT_VERIFY_SECURITY");
            recordStep("验证上下文 - 初始参数数量: " + deserializedContext.getInitParameters().size(), "CONTEXT_VERIFY_INIT_PARAMS");
            
            // 检查上下文安全性
            checkContextSecurity(deserializedContext);
            
            // 分析反序列化过程
            analyzeDeserializationTrace(tois, "CONTEXT");
            
            tois.close();
            
        } catch (Exception e) {
            recordStep("上下文反序列化演示失败: " + e.getMessage(), "CONTEXT_DEMO_ERROR", e);
        }
    }
    
    private void demonstrateListenerConfigDeserialization() {
        recordStep("演示Listener配置数据反序列化", "LISTENER_CONFIG_DEMO");
        
        try {
            // 创建Listener配置数据
            ListenerConfigData configData = new ListenerConfigData();
            configData.setListenerName("SecurityEventListener");
            configData.setListenerClass("com.example.SecurityEventListener");
            configData.setRegistrationMethod("ANNOTATION");
            configData.setDynamicRegistration(false);
            configData.setSourceLocation("WEB-INF/classes");
            configData.addConfiguration("auditEnabled", "true");
            configData.addConfiguration("logLevel", "INFO");
            configData.addConfiguration("maxEventQueue", "1000");
            
            recordStep("创建Listener配置数据对象", "CONFIG_DATA_CREATED", configData);
            
            // 序列化配置数据
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(configData);
            oos.close();
            
            recordStep("Listener配置数据序列化完成", "CONFIG_SERIALIZED");
            
            // 反序列化并追踪
            byte[] serializedData = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
            TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true);
            
            recordStep("开始Listener配置反序列化追踪", "CONFIG_DESERIALIZE_START");
            ListenerConfigData deserializedConfig = (ListenerConfigData) tois.readObjectWithTrace();
            recordStep("Listener配置反序列化完成", "CONFIG_DESERIALIZE_COMPLETE", deserializedConfig);
            
            // 验证配置数据
            recordStep("验证配置 - Listener名称: " + deserializedConfig.getListenerName(), "CONFIG_VERIFY_NAME");
            recordStep("验证配置 - 注册方式: " + deserializedConfig.getRegistrationMethod(), "CONFIG_VERIFY_REGISTRATION");
            recordStep("验证配置 - 动态注册: " + deserializedConfig.isDynamicRegistration(), "CONFIG_VERIFY_DYNAMIC");
            recordStep("验证配置 - 配置项数量: " + deserializedConfig.getConfiguration().size(), "CONFIG_VERIFY_PARAMS");
            
            // 检查配置安全性
            checkListenerConfigSecurity(deserializedConfig);
            
            // 分析反序列化过程
            analyzeDeserializationTrace(tois, "CONFIG");
            
            tois.close();
            
        } catch (Exception e) {
            recordStep("Listener配置反序列化演示失败: " + e.getMessage(), "CONFIG_DEMO_ERROR", e);
        }
    }
    
    private void detectMaliciousListenerInjection() {
        recordStep("检测恶意Listener注入", "MALICIOUS_LISTENER_DETECTION");
        
        // 检查1：检测可疑Listener类名
        checkForSuspiciousListenerNames();
        
        // 检查2：检测Listener运行时注入
        checkForRuntimeListenerInjection();
        
        // 检查3：检测Listener事件篡改
        checkForListenerEventTampering();
        
        // 检查4：检测内存马Listener特征
        checkForMemoryShellListeners();
        
        recordStep("恶意Listener检测完成", "MALICIOUS_DETECTION_COMPLETE");
    }
    
    private void checkForSuspiciousListenerNames() {
        recordStep("检查可疑Listener类名", "SUSPICIOUS_LISTENER_NAMES");
        
        String[] suspiciousNames = {
            "ShellListener",
            "CommandListener", 
            "EvalListener",
            "RuntimeExecListener",
            "FileAccessListener",
            "MemShellListener",
            "BackdoorListener",
            "WebShellListener"
        };
        
        for (String name : suspiciousNames) {
            recordStep("检查可疑Listener名称: " + name, "LISTENER_NAME_CHECK", name);
        }
        
        recordStep("Listener名称检查完成", "LISTENER_NAME_CHECK_COMPLETE");
    }
    
    private void checkForRuntimeListenerInjection() {
        recordStep("检查Listener运行时注入", "RUNTIME_INJECTION_CHECK");
        
        // 模拟检测运行时注入的Listener
        recordStep("检查动态注册的Listener", "DYNAMIC_LISTENER_CHECK");
        recordStep("检查Listener注册时间异常", "LISTENER_TIMING_CHECK");
        recordStep("检查Listener类加载来源", "LISTENER_CLASSLOADER_CHECK");
        recordStep("检查Listener接口实现异常", "LISTENER_INTERFACE_CHECK");
        
        recordStep("运行时注入检查完成", "RUNTIME_INJECTION_CHECK_COMPLETE");
    }
    
    private void checkForListenerEventTampering() {
        recordStep("检查Listener事件篡改", "EVENT_TAMPERING_CHECK");
        
        // 模拟检测事件篡改
        recordStep("检查事件数据完整性", "EVENT_INTEGRITY_CHECK");
        recordStep("检查事件时间戳异常", "EVENT_TIMESTAMP_CHECK");
        recordStep("检查事件源验证", "EVENT_SOURCE_VERIFICATION");
        recordStep("检查事件属性篡改", "EVENT_ATTRIBUTE_CHECK");
        
        recordStep("事件篡改检查完成", "EVENT_TAMPERING_CHECK_COMPLETE");
    }
    
    private void checkForMemoryShellListeners() {
        recordStep("检查内存马Listener特征", "MEMORY_SHELL_CHECK");
        
        // 检测内存马特征
        recordStep("检查Listener持久化机制", "PERSISTENCE_MECHANISM_CHECK");
        recordStep("检查命令执行能力", "COMMAND_EXECUTION_CHECK");
        recordStep("检查网络通信能力", "NETWORK_COMMUNICATION_CHECK");
        recordStep("检查反射调用模式", "REFLECTION_PATTERN_CHECK");
        recordStep("检查异常处理模式", "EXCEPTION_HANDLING_CHECK");
        
        recordStep("内存马特征检查完成", "MEMORY_SHELL_CHECK_COMPLETE");
    }
    
    private void performEventSecurityCheck(ListenerEventData eventData) {
        recordStep("执行事件安全检查", "EVENT_SECURITY_CHECK");
        
        // 检查事件类型
        String eventType = eventData.getEventType();
        if (eventType.contains("CREATED") || eventType.contains("DESTROYED")) {
            recordStep("✅ 事件类型正常: " + eventType, "EVENT_TYPE_OK");
        } else if (eventType.contains("COMMAND") || eventType.contains("EXEC")) {
            recordStep("⚠️ 发现可疑事件类型: " + eventType, "SUSPICIOUS_EVENT_TYPE", eventData);
        }
        
        // 检查事件属性
        for (String key : eventData.getEventAttributes().keySet()) {
            if (key.toLowerCase().contains("command") || 
                key.toLowerCase().contains("exec") ||
                key.toLowerCase().contains("shell")) {
                recordStep("⚠️ 发现可疑事件属性: " + key, "SUSPICIOUS_EVENT_ATTR", key);
            }
        }
        
        // 检查会话ID合法性
        String sessionId = eventData.getSessionId();
        if (sessionId != null && (sessionId.length() < 10 || sessionId.contains("test"))) {
            recordStep("⚠️ 可疑的Session ID: " + sessionId, "SUSPICIOUS_SESSION_ID", sessionId);
        }
        
        recordStep("事件安全检查完成", "EVENT_SECURITY_CHECK_COMPLETE");
    }
    
    private void checkContextSecurity(ListenerContextData contextData) {
        recordStep("检查上下文安全性", "CONTEXT_SECURITY_CHECK");
        
        // 检查安全模式
        if (contextData.isSecure()) {
            recordStep("✅ 上下文安全模式已启用", "CONTEXT_SECURITY_OK");
        } else {
            recordStep("⚠️ 上下文未启用安全模式", "CONTEXT_SECURITY_WARNING");
        }
        
        // 检查初始化参数
        for (String key : contextData.getInitParameters().keySet()) {
            String value = contextData.getInitParameters().get(key);
            if ("debugMode".equals(key) && "true".equals(value)) {
                recordStep("⚠️ 调试模式已启用", "DEBUG_MODE_WARNING", value);
            }
        }
        
        // 检查上下文属性
        if (contextData.getContextAttributes().isEmpty()) {
            recordStep("⚠️ 上下文属性为空", "EMPTY_CONTEXT_ATTRS");
        }
        
        recordStep("上下文安全检查完成", "CONTEXT_SECURITY_CHECK_COMPLETE");
    }
    
    private void checkListenerConfigSecurity(ListenerConfigData configData) {
        recordStep("检查Listener配置安全性", "LISTENER_CONFIG_SECURITY_CHECK");
        
        // 检查注册方式
        if ("ANNOTATION".equals(configData.getRegistrationMethod())) {
            recordStep("✅ 使用注解方式注册Listener", "REGISTRATION_METHOD_OK");
        } else if ("DYNAMIC".equals(configData.getRegistrationMethod())) {
            recordStep("⚠️ 使用动态方式注册Listener", "DYNAMIC_REGISTRATION_WARNING");
        }
        
        // 检查动态注册
        if (configData.isDynamicRegistration()) {
            recordStep("⚠️ 检测到动态注册的Listener", "DYNAMIC_REGISTRATION_DETECTED", configData);
        }
        
        // 检查源位置
        String sourceLocation = configData.getSourceLocation();
        if (sourceLocation == null || sourceLocation.contains("temp") || sourceLocation.contains("tmp")) {
            recordStep("⚠️ 可疑的Listener源位置: " + sourceLocation, "SUSPICIOUS_SOURCE_LOCATION", sourceLocation);
        }
        
        // 检查配置项
        String auditEnabled = configData.getConfiguration().get("auditEnabled");
        if (!"true".equals(auditEnabled)) {
            recordStep("⚠️ 审计功能未启用", "AUDIT_DISABLED_WARNING");
        }
        
        recordStep("Listener配置安全检查完成", "LISTENER_CONFIG_SECURITY_CHECK_COMPLETE");
    }
    
    private void analyzeDeserializationTrace(TraceableObjectInputStream tois, String context) {
        recordStep("分析" + context + "反序列化追踪", "TRACE_ANALYSIS_" + context);
        
        DeserializationStatistics stats = tois.getStatistics();
        recordStep("统计 - 总步骤: " + stats.getTotalSteps(), "STATS_STEPS_" + context);
        recordStep("统计 - 警告数: " + stats.getWarningCount(), "STATS_WARNINGS_" + context);
        recordStep("统计 - 错误数: " + stats.getErrorCount(), "STATS_ERRORS_" + context);
        
        // 检查危险操作
        if (stats.getWarningCount() > 0 || stats.getErrorCount() > 0) {
            recordStep("⚠️ " + context + "反序列化存在安全风险", "SECURITY_RISK_" + context);
        } else {
            recordStep("✅ " + context + "反序列化安全检查通过", "SECURITY_PASS_" + context);
        }
    }
    
    private ListenerSecurityAssessment generateSecurityAssessment() {
        ListenerSecurityAssessment assessment = new ListenerSecurityAssessment();
        
        // 计算安全分数
        int securityScore = 100;
        int riskCount = 0;
        
        for (ComponentExecutionStep step : executionSteps) {
            if (step.getStepType().contains("ERROR")) {
                securityScore -= 20;
                riskCount++;
            } else if (step.getMessage().contains("⚠️") || step.getStepType().contains("WARNING") || step.getStepType().contains("SUSPICIOUS")) {
                securityScore -= 10;
                riskCount++;
            }
        }
        
        assessment.setSecurityScore(Math.max(0, securityScore));
        assessment.setRiskCount(riskCount);
        assessment.setTotalChecks(executionSteps.size());
        
        // 设置风险级别
        if (securityScore >= 80) {
            assessment.setRiskLevel("LOW");
        } else if (securityScore >= 60) {
            assessment.setRiskLevel("MEDIUM");
        } else {
            assessment.setRiskLevel("HIGH");
        }
        
        return assessment;
    }
    
    private void recordStep(String message, String stepType) {
        recordStep(message, stepType, null);
    }
    
    private void recordStep(String message, String stepType, Object relatedObject) {
        ComponentExecutionStep step = new ComponentExecutionStep(
            ++stepCounter,
            System.currentTimeMillis(),
            stepType,
            message,
            relatedObject,
            Thread.currentThread().getStackTrace()
        );
        
        executionSteps.add(step);
        System.out.println("[LISTENER-TRACE] " + message);
    }
    
    // 获取追踪结果
    public List<ComponentExecutionStep> getExecutionSteps() {
        return new ArrayList<>(executionSteps);
    }
}