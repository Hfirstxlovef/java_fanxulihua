package com.book.demo.framework.spring;

import com.book.demo.trace.TraceableObjectInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class SpringDeserializationDemo {
    
    public static class SpringExecutionTracer {
        
        private final List<SpringExecutionStep> executionSteps;
        private final Map<String, BeanCreationInfo> beanCreationMap;
        private ApplicationContext applicationContext;
        
        public SpringExecutionTracer() {
            this.executionSteps = new ArrayList<>();
            this.beanCreationMap = new ConcurrentHashMap<>();
        }
        
        public void setApplicationContext(ApplicationContext context) {
            this.applicationContext = context;
        }
        
        public SpringDeserializationResult demonstrateSpringDeserialization() {
            recordStep("初始化Spring上下文", "SPRING_INIT");
            
            // 创建Spring应用上下文
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            context.register(SpringConfig.class);
            context.refresh();
            
            setApplicationContext(context);
            recordStep("Spring容器启动完成", "CONTAINER_READY");
            
            // 演示Bean的序列化和反序列化
            demonstrateBeanSerialization(context);
            
            // 演示AOP代理对象的序列化
            demonstrateProxySerialization(context);
            
            // 演示自定义反序列化处理
            demonstrateCustomDeserialization();
            
            return new SpringDeserializationResult(executionSteps, beanCreationMap, context);
        }
        
        private void demonstrateBeanSerialization(ApplicationContext context) {
            recordStep("开始演示Bean序列化", "BEAN_SERIALIZATION_START");
            
            try {
                // 获取业务Bean
                BusinessService businessService = context.getBean(BusinessService.class);
                recordStep("获取BusinessService Bean", "BEAN_RETRIEVED", businessService);
                
                // 序列化Bean
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(businessService);
                oos.close();
                
                byte[] serializedData = baos.toByteArray();
                recordStep("Bean序列化完成", "BEAN_SERIALIZED", serializedData.length + " bytes");
                
                // 使用TraceableObjectInputStream反序列化
                ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
                TraceableObjectInputStream tois = new TraceableObjectInputStream(bais);
                
                recordStep("开始Bean反序列化", "BEAN_DESERIALIZATION_START");
                Object deserializedBean = tois.readObject();
                recordStep("Bean反序列化完成", "BEAN_DESERIALIZED", deserializedBean);
                
                // 分析反序列化过程
                analyzeDeserializationTrace(tois);
                
                tois.close();
                
            } catch (Exception e) {
                recordStep("Bean序列化失败: " + e.getMessage(), "BEAN_SERIALIZATION_ERROR", e);
            }
        }
        
        private void demonstrateProxySerialization(ApplicationContext context) {
            recordStep("开始演示代理序列化", "PROXY_SERIALIZATION_START");
            
            try {
                // 获取DataService Bean
                DataService dataService = context.getBean(DataService.class);
                recordStep("获取DataService Bean", "PROXY_BEAN_RETRIEVED", dataService);
                
                // 简化的代理检查 - 检查类名是否包含代理标识
                boolean isProxy = dataService.getClass().getName().contains("$Proxy") || 
                                dataService.getClass().getName().contains("CGLIB") ||
                                dataService.getClass().getName().contains("ByteBuddy");
                recordStep("代理检查结果: " + isProxy, "PROXY_CHECK", isProxy);
                
                if (isProxy) {
                    // 演示代理对象的序列化挑战
                    demonstrateProxyChallenges(dataService);
                } else {
                    recordStep("当前Bean不是代理对象，演示直接序列化", "DIRECT_SERIALIZATION");
                    // 演示普通Bean的序列化
                    demonstrateProxyChallenges(dataService);
                }
                
            } catch (Exception e) {
                recordStep("代理序列化演示失败: " + e.getMessage(), "PROXY_DEMO_ERROR", e);
            }
        }
        
        private void demonstrateProxyChallenges(Object proxyBean) {
            recordStep("演示代理对象序列化挑战", "PROXY_CHALLENGES");
            
            try {
                // 尝试直接序列化代理对象
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                
                recordStep("尝试序列化代理对象", "PROXY_SERIALIZE_ATTEMPT");
                oos.writeObject(proxyBean);
                oos.close();
                
                recordStep("代理对象序列化成功", "PROXY_SERIALIZE_SUCCESS");
                
                // 尝试反序列化
                byte[] serializedData = baos.toByteArray();
                ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
                TraceableObjectInputStream tois = new TraceableObjectInputStream(bais);
                
                Object deserializedProxy = tois.readObject();
                recordStep("代理对象反序列化完成", "PROXY_DESERIALIZE_SUCCESS", deserializedProxy);
                
                tois.close();
                
            } catch (Exception e) {
                recordStep("代理对象序列化失败: " + e.getMessage(), "PROXY_SERIALIZE_FAILED", e);
                
                // 演示解决方案：获取目标对象
                demonstrateProxyTargetExtraction(proxyBean);
            }
        }
        
        private void demonstrateProxyTargetExtraction(Object proxyBean) {
            recordStep("演示代理目标对象提取", "PROXY_TARGET_EXTRACTION");
            
            try {
                // 简化的目标类获取 - 直接使用对象的类信息
                Class<?> targetClass = proxyBean.getClass();
                if (targetClass.getName().contains("$Proxy") || 
                    targetClass.getName().contains("CGLIB")) {
                    // 对于代理类，尝试获取父类或接口
                    Class<?>[] interfaces = targetClass.getInterfaces();
                    if (interfaces.length > 0) {
                        recordStep("提取目标接口: " + interfaces[0].getName(), "PROXY_TARGET_INTERFACE", interfaces[0]);
                    }
                    Class<?> superclass = targetClass.getSuperclass();
                    if (superclass != Object.class) {
                        recordStep("提取目标父类: " + superclass.getName(), "PROXY_TARGET_SUPERCLASS", superclass);
                    }
                } else {
                    recordStep("提取目标类: " + targetClass.getName(), "PROXY_TARGET_CLASS", targetClass);
                }
                
                recordStep("代理目标提取演示完成", "PROXY_TARGET_DEMO_COMPLETE");
                
            } catch (Exception e) {
                recordStep("代理目标提取失败: " + e.getMessage(), "PROXY_TARGET_ERROR", e);
            }
        }
        
        private void demonstrateCustomDeserialization() {
            recordStep("演示自定义反序列化处理", "CUSTOM_DESERIALIZATION_START");
            
            try {
                // 创建带有自定义反序列化逻辑的对象
                CustomSerializableBean customBean = new CustomSerializableBean();
                customBean.setId("DEMO_001");
                customBean.setName("Spring反序列化演示对象");
                customBean.setSecretData("这是敏感数据");
                
                // 序列化
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(customBean);
                oos.close();
                
                recordStep("自定义Bean序列化完成", "CUSTOM_SERIALIZE_COMPLETE");
                
                // 使用TraceableObjectInputStream反序列化以观察自定义过程
                byte[] serializedData = baos.toByteArray();
                ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
                TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true);
                
                recordStep("开始自定义反序列化追踪", "CUSTOM_DESERIALIZE_TRACE_START");
                CustomSerializableBean deserializedBean = (CustomSerializableBean) tois.readObject();
                recordStep("自定义反序列化完成", "CUSTOM_DESERIALIZE_COMPLETE", deserializedBean);
                
                // 显示自定义反序列化的效果
                recordStep("验证自定义反序列化效果", "CUSTOM_DESERIALIZE_VERIFY");
                recordStep("反序列化后ID: " + deserializedBean.getId(), "VERIFY_ID");
                recordStep("反序列化后Name: " + deserializedBean.getName(), "VERIFY_NAME");
                recordStep("反序列化后SecretData: " + deserializedBean.getSecretData(), "VERIFY_SECRET");
                
                // 打印完整的追踪信息
                tois.printFullTrace();
                
                tois.close();
                
            } catch (Exception e) {
                recordStep("自定义反序列化演示失败: " + e.getMessage(), "CUSTOM_DESERIALIZE_ERROR", e);
            }
        }
        
        private void analyzeDeserializationTrace(TraceableObjectInputStream tois) {
            recordStep("分析反序列化追踪信息", "TRACE_ANALYSIS");
            
            var statistics = tois.getStatistics();
            recordStep("统计信息 - 总步骤: " + statistics.getTotalSteps(), "STATS_TOTAL_STEPS");
            recordStep("统计信息 - 警告数: " + statistics.getWarningCount(), "STATS_WARNINGS");
            recordStep("统计信息 - 错误数: " + statistics.getErrorCount(), "STATS_ERRORS");
            
            var dangerousSteps = statistics.getDangerousSteps();
            if (!dangerousSteps.isEmpty()) {
                recordStep("发现危险操作: " + dangerousSteps.size() + " 个", "DANGEROUS_OPERATIONS");
                for (var step : dangerousSteps) {
                    recordStep("危险操作: " + step.getMessage(), "DANGEROUS_STEP", step);
                }
            }
        }
        
        private void recordStep(String description, String stepType) {
            recordStep(description, stepType, null);
        }
        
        private void recordStep(String description, String stepType, Object relatedObject) {
            SpringExecutionStep step = new SpringExecutionStep(
                System.nanoTime(),
                Thread.currentThread().getStackTrace(),
                description,
                stepType,
                relatedObject
            );
            executionSteps.add(step);
            
            // 实时输出执行步骤
            System.out.println("[SPRING-DEMO] " + description);
        }
        
        public List<SpringExecutionStep> getExecutionSteps() {
            return new ArrayList<>(executionSteps);
        }
        
        public void printExecutionSummary() {
            System.out.println("\n=== Spring框架反序列化执行摘要 ===");
            System.out.println("总执行步骤: " + executionSteps.size());
            
            // 按步骤类型分组统计
            Map<String, Long> stepTypeCount = executionSteps.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    SpringExecutionStep::getStepType,
                    java.util.stream.Collectors.counting()));
            
            System.out.println("\n步骤类型分布:");
            stepTypeCount.forEach((type, count) -> 
                System.out.println("  " + type + ": " + count + " 次"));
            
            System.out.println("\n详细执行序列:");
            for (int i = 0; i < executionSteps.size(); i++) {
                SpringExecutionStep step = executionSteps.get(i);
                System.out.printf("[%3d] %s%n", i + 1, step.getDescription());
            }
        }
    }
}