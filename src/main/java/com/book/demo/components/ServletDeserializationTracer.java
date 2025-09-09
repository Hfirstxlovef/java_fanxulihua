package com.book.demo.components;

import com.book.demo.trace.TraceableObjectInputStream;
import com.book.demo.trace.DeserializationStep;
import com.book.demo.trace.DeserializationStatistics;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ServletDeserializationTracer {
    
    private final List<ComponentExecutionStep> executionSteps;
    private int stepCounter;
    
    public ServletDeserializationTracer() {
        this.executionSteps = new ArrayList<>();
        this.stepCounter = 0;
    }
    
    public ServletTraceResult traceServletDeserialization() {
        recordStep("开始Servlet反序列化追踪演示", "SERVLET_TRACE_START");
        
        try {
            // 演示1：模拟Servlet处理POST请求中的序列化数据
            demonstrateRequestDeserialization();
            
            // 演示2：模拟Session属性反序列化
            demonstrateSessionDeserialization();
            
            // 演示3：模拟Servlet参数反序列化（潜在攻击向量）
            demonstrateParameterDeserialization();
            
            // 演示4：检测Servlet中的危险反序列化模式
            detectDangerousPatterns();
            
            recordStep("Servlet反序列化追踪完成", "SERVLET_TRACE_COMPLETE");
            
            return new ServletTraceResult(executionSteps, generateSecurityAssessment());
            
        } catch (Exception e) {
            recordStep("Servlet追踪失败: " + e.getMessage(), "SERVLET_TRACE_ERROR", e);
            return new ServletTraceResult(executionSteps, generateSecurityAssessment());
        }
    }
    
    private void demonstrateRequestDeserialization() {
        recordStep("演示请求数据反序列化", "REQUEST_DESERIALIZATION_DEMO");
        
        try {
            // 创建模拟的请求数据对象
            ServletRequestData requestData = new ServletRequestData();
            requestData.setUserId("user123");
            requestData.setAction("processData");
            requestData.setPayload("模拟请求负载数据");
            
            recordStep("创建模拟请求数据对象", "REQUEST_DATA_CREATED", requestData);
            
            // 序列化请求数据
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(requestData);
            oos.close();
            
            byte[] serializedData = baos.toByteArray();
            recordStep("请求数据序列化完成，大小: " + serializedData.length + " bytes", "REQUEST_SERIALIZED");
            
            // 使用TraceableObjectInputStream反序列化
            ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
            TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true);
            
            recordStep("开始请求数据反序列化追踪", "REQUEST_DESERIALIZE_START");
            ServletRequestData deserializedData = (ServletRequestData) tois.readObjectWithTrace();
            recordStep("请求数据反序列化完成", "REQUEST_DESERIALIZE_COMPLETE", deserializedData);
            
            // 分析反序列化过程
            analyzeDeserializationTrace(tois, "REQUEST");
            
            tois.close();
            
        } catch (Exception e) {
            recordStep("请求反序列化演示失败: " + e.getMessage(), "REQUEST_DEMO_ERROR", e);
        }
    }
    
    private void demonstrateSessionDeserialization() {
        recordStep("演示Session属性反序列化", "SESSION_DESERIALIZATION_DEMO");
        
        try {
            // 创建模拟的Session数据
            SessionData sessionData = new SessionData();
            sessionData.setSessionId("SESS_" + System.currentTimeMillis());
            sessionData.setUserId("user456");
            sessionData.setLoginTime(System.currentTimeMillis());
            sessionData.addAttribute("userRole", "admin");
            sessionData.addAttribute("permissions", "read,write,delete");
            
            recordStep("创建Session数据对象", "SESSION_DATA_CREATED", sessionData);
            
            // 序列化Session数据（模拟Session持久化）
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(sessionData);
            oos.close();
            
            recordStep("Session数据序列化完成", "SESSION_SERIALIZED");
            
            // 反序列化Session数据（模拟Session恢复）
            byte[] serializedData = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
            TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true);
            
            recordStep("开始Session反序列化追踪", "SESSION_DESERIALIZE_START");
            SessionData deserializedSession = (SessionData) tois.readObjectWithTrace();
            recordStep("Session反序列化完成", "SESSION_DESERIALIZE_COMPLETE", deserializedSession);
            
            // 验证Session数据完整性
            recordStep("验证Session数据 - ID: " + deserializedSession.getSessionId(), "SESSION_VERIFY_ID");
            recordStep("验证Session数据 - 用户: " + deserializedSession.getUserId(), "SESSION_VERIFY_USER");
            recordStep("验证Session数据 - 属性数量: " + deserializedSession.getAttributes().size(), "SESSION_VERIFY_ATTRS");
            
            // 分析Session反序列化安全性
            analyzeDeserializationTrace(tois, "SESSION");
            
            tois.close();
            
        } catch (Exception e) {
            recordStep("Session反序列化演示失败: " + e.getMessage(), "SESSION_DEMO_ERROR", e);
        }
    }
    
    private void demonstrateParameterDeserialization() {
        recordStep("演示参数反序列化（潜在攻击向量）", "PARAMETER_DESERIALIZATION_DEMO");
        
        try {
            // 模拟接收到的可疑参数数据
            SuspiciousParameterData paramData = new SuspiciousParameterData();
            paramData.setParameterName("userCommand");
            paramData.setParameterValue("serialized_malicious_data");
            paramData.setSourceIP("192.168.1.100");
            
            recordStep("创建可疑参数对象", "SUSPICIOUS_PARAM_CREATED", paramData);
            
            // 序列化参数数据
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(paramData);
            oos.close();
            
            recordStep("参数数据序列化完成", "PARAM_SERIALIZED");
            
            // 使用增强的安全追踪反序列化
            byte[] serializedData = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
            TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true);
            
            recordStep("开始参数反序列化安全追踪", "PARAM_SECURITY_TRACE_START");
            SuspiciousParameterData deserializedParam = (SuspiciousParameterData) tois.readObjectWithTrace();
            recordStep("参数反序列化完成", "PARAM_DESERIALIZE_COMPLETE", deserializedParam);
            
            // 执行安全检查
            performParameterSecurityCheck(deserializedParam);
            
            // 分析潜在的安全风险
            analyzeDeserializationTrace(tois, "PARAMETER");
            
            tois.close();
            
        } catch (Exception e) {
            recordStep("参数反序列化演示失败: " + e.getMessage(), "PARAM_DEMO_ERROR", e);
        }
    }
    
    private void detectDangerousPatterns() {
        recordStep("检测Servlet中的危险反序列化模式", "DANGEROUS_PATTERN_DETECTION");
        
        // 检查1：检测可能的恶意Servlet类
        checkForMaliciousServletPatterns();
        
        // 检查2：检测不安全的反序列化调用
        checkForUnsafeDeserializationCalls();
        
        // 检查3：检测Session固定攻击模式
        checkForSessionFixationPatterns();
        
        recordStep("危险模式检测完成", "PATTERN_DETECTION_COMPLETE");
    }
    
    private void checkForMaliciousServletPatterns() {
        recordStep("检查恶意Servlet模式", "MALICIOUS_SERVLET_CHECK");
        
        String[] suspiciousServletPatterns = {
            "CommandServlet",
            "ShellServlet", 
            "FileUploadServlet",
            "EvalServlet",
            "RuntimeServlet"
        };
        
        for (String pattern : suspiciousServletPatterns) {
            recordStep("检查可疑Servlet模式: " + pattern, "SERVLET_PATTERN_CHECK", pattern);
        }
        
        recordStep("Servlet模式检查完成", "SERVLET_PATTERN_CHECK_COMPLETE");
    }
    
    private void checkForUnsafeDeserializationCalls() {
        recordStep("检查不安全的反序列化调用", "UNSAFE_DESERIAL_CHECK");
        
        // 模拟检测到的不安全调用
        String[] unsafeMethods = {
            "ObjectInputStream.readObject()",
            "XMLDecoder.readObject()", 
            "ObjectMapper.readValue()",
            "Gson.fromJson()"
        };
        
        for (String method : unsafeMethods) {
            recordStep("检测到不安全方法调用: " + method, "UNSAFE_METHOD_DETECTED", method);
        }
        
        recordStep("不安全调用检查完成", "UNSAFE_CALL_CHECK_COMPLETE");
    }
    
    private void checkForSessionFixationPatterns() {
        recordStep("检查Session固定攻击模式", "SESSION_FIXATION_CHECK");
        
        // 模拟Session安全检查
        recordStep("检查Session ID生成安全性", "SESSION_ID_CHECK");
        recordStep("检查Session属性安全性", "SESSION_ATTR_CHECK");
        recordStep("检查Session生命周期安全性", "SESSION_LIFECYCLE_CHECK");
        
        recordStep("Session安全检查完成", "SESSION_SECURITY_CHECK_COMPLETE");
    }
    
    private void performParameterSecurityCheck(SuspiciousParameterData param) {
        recordStep("执行参数安全检查", "PARAM_SECURITY_CHECK");
        
        // 检查参数名称
        if (param.getParameterName().toLowerCase().contains("command") ||
            param.getParameterName().toLowerCase().contains("exec") ||
            param.getParameterName().toLowerCase().contains("eval")) {
            recordStep("⚠️ 发现可疑参数名称: " + param.getParameterName(), "SUSPICIOUS_PARAM_NAME", param);
        }
        
        // 检查参数值
        if (param.getParameterValue().contains("java.lang.Runtime") ||
            param.getParameterValue().contains("ProcessBuilder") ||
            param.getParameterValue().contains("serialized_malicious")) {
            recordStep("⚠️ 发现可疑参数值: " + param.getParameterValue(), "SUSPICIOUS_PARAM_VALUE", param);
        }
        
        // 检查来源IP
        recordStep("参数来源IP: " + param.getSourceIP(), "PARAM_SOURCE_IP", param.getSourceIP());
        
        recordStep("参数安全检查完成", "PARAM_SECURITY_CHECK_COMPLETE");
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
    
    private ServletSecurityAssessment generateSecurityAssessment() {
        ServletSecurityAssessment assessment = new ServletSecurityAssessment();
        
        // 计算安全分数
        int securityScore = 100;
        int riskCount = 0;
        
        for (ComponentExecutionStep step : executionSteps) {
            if (step.getStepType().contains("ERROR")) {
                securityScore -= 20;
                riskCount++;
            } else if (step.getMessage().contains("⚠️") || step.getStepType().contains("SUSPICIOUS")) {
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
        System.out.println("[SERVLET-TRACE] " + message);
    }
    
    // 获取追踪结果
    public List<ComponentExecutionStep> getExecutionSteps() {
        return new ArrayList<>(executionSteps);
    }
}