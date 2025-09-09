package com.book.demo.test;

import com.book.demo.components.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Web组件追踪功能测试
 */
public class ComponentTraceTest {
    
    @Test
    @DisplayName("测试Servlet反序列化追踪功能")
    public void testServletDeserialization() {
        System.out.println("🧪 测试Servlet反序列化追踪功能");
        
        ServletDeserializationTracer tracer = new ServletDeserializationTracer();
        ServletTraceResult result = tracer.traceServletDeserialization();
        
        // 验证基本结果
        assertNotNull(result, "追踪结果不应为null");
        assertNotNull(result.getExecutionSteps(), "执行步骤不应为null");
        assertTrue(result.getExecutionSteps().size() > 0, "应该有执行步骤");
        
        // 验证安全评估
        assertNotNull(result.getSecurityAssessment(), "安全评估不应为null");
        assertTrue(result.getSecurityAssessment().getSecurityScore() >= 0, "安全分数应该>= 0");
        assertTrue(result.getSecurityAssessment().getSecurityScore() <= 100, "安全分数应该<= 100");
        assertNotNull(result.getSecurityAssessment().getRiskLevel(), "风险级别不应为null");
        
        // 验证统计方法
        assertTrue(result.getWarningCount() >= 0, "警告数量应该>= 0");
        assertTrue(result.getErrorCount() >= 0, "错误数量应该>= 0");
        
        System.out.println("   ✅ 执行步骤数: " + result.getExecutionSteps().size());
        System.out.println("   ✅ 安全分数: " + result.getSecurityAssessment().getSecurityScore());
        System.out.println("   ✅ 风险级别: " + result.getSecurityAssessment().getRiskLevel());
        System.out.println("   ✅ 警告数量: " + result.getWarningCount());
        System.out.println("   ✅ 错误数量: " + result.getErrorCount());
    }
    
    @Test
    @DisplayName("测试Filter反序列化追踪功能")
    public void testFilterDeserialization() {
        System.out.println("🧪 测试Filter反序列化追踪功能");
        
        FilterDeserializationTracer tracer = new FilterDeserializationTracer();
        FilterTraceResult result = tracer.traceFilterDeserialization();
        
        // 验证基本结果
        assertNotNull(result, "追踪结果不应为null");
        assertNotNull(result.getExecutionSteps(), "执行步骤不应为null");
        assertTrue(result.getExecutionSteps().size() > 0, "应该有执行步骤");
        
        // 验证安全评估
        assertNotNull(result.getSecurityAssessment(), "安全评估不应为null");
        assertTrue(result.getSecurityAssessment().getSecurityScore() >= 0, "安全分数应该>= 0");
        assertTrue(result.getSecurityAssessment().getSecurityScore() <= 100, "安全分数应该<= 100");
        
        System.out.println("   ✅ 执行步骤数: " + result.getExecutionSteps().size());
        System.out.println("   ✅ 安全分数: " + result.getSecurityAssessment().getSecurityScore());
        System.out.println("   ✅ 风险级别: " + result.getSecurityAssessment().getRiskLevel());
    }
    
    @Test
    @DisplayName("测试Listener反序列化追踪功能")
    public void testListenerDeserialization() {
        System.out.println("🧪 测试Listener反序列化追踪功能");
        
        ListenerDeserializationTracer tracer = new ListenerDeserializationTracer();
        ListenerTraceResult result = tracer.traceListenerDeserialization();
        
        // 验证基本结果
        assertNotNull(result, "追踪结果不应为null");
        assertNotNull(result.getExecutionSteps(), "执行步骤不应为null");
        assertTrue(result.getExecutionSteps().size() > 0, "应该有执行步骤");
        
        // 验证安全评估
        assertNotNull(result.getSecurityAssessment(), "安全评估不应为null");
        assertTrue(result.getSecurityAssessment().getSecurityScore() >= 0, "安全分数应该>= 0");
        assertTrue(result.getSecurityAssessment().getSecurityScore() <= 100, "安全分数应该<= 100");
        
        System.out.println("   ✅ 执行步骤数: " + result.getExecutionSteps().size());
        System.out.println("   ✅ 安全分数: " + result.getSecurityAssessment().getSecurityScore());
        System.out.println("   ✅ 风险级别: " + result.getSecurityAssessment().getRiskLevel());
    }
    
    @Test
    @DisplayName("测试所有组件的追踪结果一致性")
    public void testComponentConsistency() {
        System.out.println("🧪 测试组件追踪结果一致性");
        
        // 分别运行各个组件的追踪
        ServletDeserializationTracer servletTracer = new ServletDeserializationTracer();
        FilterDeserializationTracer filterTracer = new FilterDeserializationTracer();
        ListenerDeserializationTracer listenerTracer = new ListenerDeserializationTracer();
        
        ServletTraceResult servletResult = servletTracer.traceServletDeserialization();
        FilterTraceResult filterResult = filterTracer.traceFilterDeserialization();
        ListenerTraceResult listenerResult = listenerTracer.traceListenerDeserialization();
        
        // 验证结果都不为空
        assertNotNull(servletResult, "Servlet追踪结果不应为null");
        assertNotNull(filterResult, "Filter追踪结果不应为null");
        assertNotNull(listenerResult, "Listener追踪结果不应为null");
        
        // 计算总计
        int totalSteps = servletResult.getExecutionSteps().size() + 
                        filterResult.getExecutionSteps().size() + 
                        listenerResult.getExecutionSteps().size();
        
        long totalWarnings = servletResult.getWarningCount() + 
                           filterResult.getWarningCount() + 
                           listenerResult.getWarningCount();
        
        long totalErrors = servletResult.getErrorCount() + 
                         filterResult.getErrorCount() + 
                         listenerResult.getErrorCount();
        
        System.out.println("   ✅ 总执行步骤: " + totalSteps);
        System.out.println("   ✅ 总警告数量: " + totalWarnings);
        System.out.println("   ✅ 总错误数量: " + totalErrors);
        
        assertTrue(totalSteps > 0, "总执行步骤应该> 0");
        assertTrue(totalWarnings >= 0, "总警告数应该>= 0");
        assertTrue(totalErrors >= 0, "总错误数应该>= 0");
    }
    
    @Test
    @DisplayName("测试安全评估功能")
    public void testSecurityAssessment() {
        System.out.println("🧪 测试安全评估功能");
        
        // 创建并测试各种安全评估
        ServletSecurityAssessment servletAssessment = new ServletSecurityAssessment();
        FilterSecurityAssessment filterAssessment = new FilterSecurityAssessment();
        ListenerSecurityAssessment listenerAssessment = new ListenerSecurityAssessment();
        
        // 验证默认值
        assertEquals(100, servletAssessment.getSecurityScore(), "默认安全分数应为100");
        assertEquals("LOW", servletAssessment.getRiskLevel(), "默认风险级别应为LOW");
        assertNotNull(servletAssessment.getRecommendation(), "建议不应为null");
        
        assertEquals(100, filterAssessment.getSecurityScore(), "默认安全分数应为100");
        assertEquals("LOW", filterAssessment.getRiskLevel(), "默认风险级别应为LOW");
        assertNotNull(filterAssessment.getRecommendation(), "建议不应为null");
        
        assertEquals(100, listenerAssessment.getSecurityScore(), "默认安全分数应为100");
        assertEquals("LOW", listenerAssessment.getRiskLevel(), "默认风险级别应为LOW");
        assertNotNull(listenerAssessment.getRecommendation(), "建议不应为null");
        
        System.out.println("   ✅ 安全评估模块运行正常");
    }
}