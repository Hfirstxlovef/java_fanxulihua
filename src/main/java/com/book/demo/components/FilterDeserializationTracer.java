package com.book.demo.components;

import com.book.demo.trace.TraceableObjectInputStream;
import com.book.demo.trace.DeserializationStatistics;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FilterDeserializationTracer {
    
    private final List<ComponentExecutionStep> executionSteps;
    private int stepCounter;
    
    public FilterDeserializationTracer() {
        this.executionSteps = new ArrayList<>();
        this.stepCounter = 0;
    }
    
    public FilterTraceResult traceFilterDeserialization() {
        recordStep("开始Filter反序列化追踪演示", "FILTER_TRACE_START");
        
        try {
            // 演示1：模拟Filter处理请求头中的序列化数据
            demonstrateRequestHeaderDeserialization();
            
            // 演示2：模拟Filter链中的参数传递反序列化
            demonstrateFilterChainDeserialization();
            
            // 演示3：模拟Filter配置参数反序列化
            demonstrateFilterConfigDeserialization();
            
            // 演示4：检测恶意Filter注入
            detectMaliciousFilterInjection();
            
            recordStep("Filter反序列化追踪完成", "FILTER_TRACE_COMPLETE");
            
            return new FilterTraceResult(executionSteps, generateSecurityAssessment());
            
        } catch (Exception e) {
            recordStep("Filter追踪失败: " + e.getMessage(), "FILTER_TRACE_ERROR", e);
            return new FilterTraceResult(executionSteps, generateSecurityAssessment());
        }
    }
    
    private void demonstrateRequestHeaderDeserialization() {
        recordStep("演示请求头反序列化处理", "HEADER_DESERIALIZATION_DEMO");
        
        try {
            // 创建模拟的请求头数据
            FilterRequestHeader headerData = new FilterRequestHeader();
            headerData.setHeaderName("X-Custom-Data");
            headerData.setHeaderValue("序列化的用户偏好数据");
            headerData.setSourceFilter("AuthenticationFilter");
            headerData.setProcessingTime(System.currentTimeMillis());
            
            recordStep("创建请求头数据对象", "HEADER_DATA_CREATED", headerData);
            
            // 序列化请求头数据
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(headerData);
            oos.close();
            
            recordStep("请求头数据序列化完成", "HEADER_SERIALIZED");
            
            // 使用TraceableObjectInputStream反序列化
            byte[] serializedData = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
            TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true);
            
            recordStep("开始请求头反序列化追踪", "HEADER_DESERIALIZE_START");
            FilterRequestHeader deserializedHeader = (FilterRequestHeader) tois.readObjectWithTrace();
            recordStep("请求头反序列化完成", "HEADER_DESERIALIZE_COMPLETE", deserializedHeader);
            
            // 验证请求头数据
            recordStep("验证请求头 - 名称: " + deserializedHeader.getHeaderName(), "HEADER_VERIFY_NAME");
            recordStep("验证请求头 - 来源: " + deserializedHeader.getSourceFilter(), "HEADER_VERIFY_SOURCE");
            
            // 分析反序列化过程
            analyzeDeserializationTrace(tois, "HEADER");
            
            tois.close();
            
        } catch (Exception e) {
            recordStep("请求头反序列化演示失败: " + e.getMessage(), "HEADER_DEMO_ERROR", e);
        }
    }
    
    private void demonstrateFilterChainDeserialization() {
        recordStep("演示Filter链参数传递反序列化", "FILTER_CHAIN_DEMO");
        
        try {
            // 创建Filter链数据
            FilterChainData chainData = new FilterChainData();
            chainData.setFilterName("SecurityFilter");
            chainData.setNextFilter("LoggingFilter");
            chainData.addParameter("userId", "12345");
            chainData.addParameter("sessionId", "SESS_789");
            chainData.addParameter("requestId", "REQ_" + System.currentTimeMillis());
            
            recordStep("创建Filter链数据对象", "CHAIN_DATA_CREATED", chainData);
            
            // 序列化Filter链数据
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(chainData);
            oos.close();
            
            recordStep("Filter链数据序列化完成", "CHAIN_SERIALIZED");
            
            // 反序列化并追踪
            byte[] serializedData = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
            TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true);
            
            recordStep("开始Filter链反序列化追踪", "CHAIN_DESERIALIZE_START");
            FilterChainData deserializedChain = (FilterChainData) tois.readObjectWithTrace();
            recordStep("Filter链反序列化完成", "CHAIN_DESERIALIZE_COMPLETE", deserializedChain);
            
            // 验证链数据
            recordStep("验证Filter链 - 过滤器: " + deserializedChain.getFilterName(), "CHAIN_VERIFY_FILTER");
            recordStep("验证Filter链 - 参数数量: " + deserializedChain.getParameters().size(), "CHAIN_VERIFY_PARAMS");
            
            // 检查链的完整性
            checkFilterChainIntegrity(deserializedChain);
            
            // 分析反序列化过程
            analyzeDeserializationTrace(tois, "CHAIN");
            
            tois.close();
            
        } catch (Exception e) {
            recordStep("Filter链反序列化演示失败: " + e.getMessage(), "CHAIN_DEMO_ERROR", e);
        }
    }
    
    private void demonstrateFilterConfigDeserialization() {
        recordStep("演示Filter配置参数反序列化", "FILTER_CONFIG_DEMO");
        
        try {
            // 创建Filter配置数据
            FilterConfigData configData = new FilterConfigData();
            configData.setFilterName("CustomSecurityFilter");
            configData.setInitParameter("securityLevel", "HIGH");
            configData.setInitParameter("allowedIPs", "192.168.1.0/24");
            configData.setInitParameter("blockSuspicious", "true");
            configData.setServletContext("DemoApplication");
            
            recordStep("创建Filter配置数据对象", "CONFIG_DATA_CREATED", configData);
            
            // 序列化配置数据
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(configData);
            oos.close();
            
            recordStep("Filter配置数据序列化完成", "CONFIG_SERIALIZED");
            
            // 反序列化并追踪
            byte[] serializedData = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
            TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true);
            
            recordStep("开始Filter配置反序列化追踪", "CONFIG_DESERIALIZE_START");
            FilterConfigData deserializedConfig = (FilterConfigData) tois.readObjectWithTrace();
            recordStep("Filter配置反序列化完成", "CONFIG_DESERIALIZE_COMPLETE", deserializedConfig);
            
            // 验证配置数据
            recordStep("验证配置 - Filter名称: " + deserializedConfig.getFilterName(), "CONFIG_VERIFY_NAME");
            recordStep("验证配置 - 参数数量: " + deserializedConfig.getInitParameters().size(), "CONFIG_VERIFY_PARAMS");
            
            // 检查配置安全性
            checkFilterConfigSecurity(deserializedConfig);
            
            // 分析反序列化过程
            analyzeDeserializationTrace(tois, "CONFIG");
            
            tois.close();
            
        } catch (Exception e) {
            recordStep("Filter配置反序列化演示失败: " + e.getMessage(), "CONFIG_DEMO_ERROR", e);
        }
    }
    
    private void detectMaliciousFilterInjection() {
        recordStep("检测恶意Filter注入", "MALICIOUS_FILTER_DETECTION");
        
        // 检查1：检测可疑Filter类名
        checkForSuspiciousFilterNames();
        
        // 检查2：检测Filter运行时注入
        checkForRuntimeFilterInjection();
        
        // 检查3：检测Filter配置篡改
        checkForFilterConfigTampering();
        
        recordStep("恶意Filter检测完成", "MALICIOUS_DETECTION_COMPLETE");
    }
    
    private void checkForSuspiciousFilterNames() {
        recordStep("检查可疑Filter类名", "SUSPICIOUS_FILTER_NAMES");
        
        String[] suspiciousNames = {
            "ShellFilter",
            "CommandFilter", 
            "EvalFilter",
            "RuntimeExecFilter",
            "FileAccessFilter",
            "MemShellFilter"
        };
        
        for (String name : suspiciousNames) {
            recordStep("检查可疑Filter名称: " + name, "FILTER_NAME_CHECK", name);
        }
        
        recordStep("Filter名称检查完成", "FILTER_NAME_CHECK_COMPLETE");
    }
    
    private void checkForRuntimeFilterInjection() {
        recordStep("检查Filter运行时注入", "RUNTIME_INJECTION_CHECK");
        
        // 模拟检测运行时注入的Filter
        recordStep("检查动态注册的Filter", "DYNAMIC_FILTER_CHECK");
        recordStep("检查Filter注册时间异常", "FILTER_TIMING_CHECK");
        recordStep("检查Filter类加载来源", "FILTER_CLASSLOADER_CHECK");
        
        recordStep("运行时注入检查完成", "RUNTIME_INJECTION_CHECK_COMPLETE");
    }
    
    private void checkForFilterConfigTampering() {
        recordStep("检查Filter配置篡改", "CONFIG_TAMPERING_CHECK");
        
        // 模拟检测配置篡改
        recordStep("检查初始化参数完整性", "INIT_PARAM_INTEGRITY");
        recordStep("检查Filter映射配置", "FILTER_MAPPING_CHECK");
        recordStep("检查Filter执行顺序", "FILTER_ORDER_CHECK");
        
        recordStep("配置篡改检查完成", "CONFIG_TAMPERING_CHECK_COMPLETE");
    }
    
    private void checkFilterChainIntegrity(FilterChainData chainData) {
        recordStep("检查Filter链完整性", "CHAIN_INTEGRITY_CHECK");
        
        // 检查链的连续性
        if (chainData.getNextFilter() != null) {
            recordStep("✅ Filter链连续性正常: " + chainData.getNextFilter(), "CHAIN_CONTINUITY_OK");
        } else {
            recordStep("⚠️ Filter链可能中断", "CHAIN_CONTINUITY_WARNING");
        }
        
        // 检查参数合法性
        if (chainData.getParameters().containsKey("userId") && 
            chainData.getParameters().containsKey("sessionId")) {
            recordStep("✅ 必要参数完整", "CHAIN_PARAMS_OK");
        } else {
            recordStep("⚠️ 缺少必要参数", "CHAIN_PARAMS_WARNING");
        }
        
        recordStep("Filter链完整性检查完成", "CHAIN_INTEGRITY_CHECK_COMPLETE");
    }
    
    private void checkFilterConfigSecurity(FilterConfigData configData) {
        recordStep("检查Filter配置安全性", "CONFIG_SECURITY_CHECK");
        
        // 检查安全参数
        String securityLevel = configData.getInitParameters().get("securityLevel");
        if ("HIGH".equals(securityLevel)) {
            recordStep("✅ 安全级别设置正确: " + securityLevel, "SECURITY_LEVEL_OK");
        } else {
            recordStep("⚠️ 安全级别可能过低: " + securityLevel, "SECURITY_LEVEL_WARNING");
        }
        
        // 检查IP白名单配置
        String allowedIPs = configData.getInitParameters().get("allowedIPs");
        if (allowedIPs != null && !allowedIPs.isEmpty()) {
            recordStep("✅ IP访问控制已配置: " + allowedIPs, "IP_CONTROL_OK");
        } else {
            recordStep("⚠️ 未配置IP访问控制", "IP_CONTROL_WARNING");
        }
        
        recordStep("Filter配置安全检查完成", "CONFIG_SECURITY_CHECK_COMPLETE");
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
    
    private FilterSecurityAssessment generateSecurityAssessment() {
        FilterSecurityAssessment assessment = new FilterSecurityAssessment();
        
        // 计算安全分数
        int securityScore = 100;
        int riskCount = 0;
        
        for (ComponentExecutionStep step : executionSteps) {
            if (step.getStepType().contains("ERROR")) {
                securityScore -= 20;
                riskCount++;
            } else if (step.getMessage().contains("⚠️") || step.getStepType().contains("WARNING")) {
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
        System.out.println("[FILTER-TRACE] " + message);
    }
    
    // 获取追踪结果
    public List<ComponentExecutionStep> getExecutionSteps() {
        return new ArrayList<>(executionSteps);
    }
}