package com.book.demo.framework.spring;

import java.io.*;
import java.util.Date;

public class CustomSerializableBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private transient String secretData; // 标记为transient，演示自定义处理
    private Date createdTime;
    private int version = 1;
    
    public CustomSerializableBean() {
        this.createdTime = new Date();
    }
    
    // 自定义序列化方法 - 演示完全控制序列化过程
    private void writeObject(ObjectOutputStream out) throws IOException {
        System.out.println("CustomSerializableBean.writeObject() - 开始自定义序列化");
        
        // 写入版本号（用于向后兼容）
        out.writeInt(version);
        
        // 写入基本字段
        out.writeObject(id);
        out.writeObject(name);
        out.writeObject(createdTime);
        
        // 对敏感数据进行加密或编码后写入
        if (secretData != null) {
            String encodedSecret = encodeSecret(secretData);
            out.writeObject(encodedSecret);
            System.out.println("敏感数据已编码写入: " + encodedSecret);
        } else {
            out.writeObject(null);
        }
        
        // 写入校验信息
        String checksum = calculateChecksum();
        out.writeObject(checksum);
        
        System.out.println("CustomSerializableBean 自定义序列化完成");
    }
    
    // 自定义反序列化方法 - 演示完全控制反序列化过程
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        System.out.println("CustomSerializableBean.readObject() - 开始自定义反序列化");
        
        // 读取版本号
        int fileVersion = in.readInt();
        System.out.println("读取到版本号: " + fileVersion);
        
        // 根据版本号进行兼容性处理
        if (fileVersion > version) {
            throw new InvalidObjectException("不支持的序列化版本: " + fileVersion);
        }
        
        // 读取基本字段
        id = (String) in.readObject();
        name = (String) in.readObject();
        createdTime = (Date) in.readObject();
        
        // 读取并解码敏感数据
        String encodedSecret = (String) in.readObject();
        if (encodedSecret != null) {
            secretData = decodeSecret(encodedSecret);
            System.out.println("敏感数据已解码: " + secretData);
        }
        
        // 读取并验证校验信息
        String storedChecksum = (String) in.readObject();
        String calculatedChecksum = calculateChecksum();
        
        if (!calculatedChecksum.equals(storedChecksum)) {
            System.out.println("警告: 数据校验失败! 存储的: " + storedChecksum + 
                             ", 计算的: " + calculatedChecksum);
            throw new InvalidObjectException("数据完整性验证失败");
        }
        
        System.out.println("CustomSerializableBean 自定义反序列化完成，数据完整性验证通过");
    }
    
    // 简单的编码方法（实际应用中应该使用更强的加密）
    private String encodeSecret(String secret) {
        if (secret == null) return null;
        
        StringBuilder encoded = new StringBuilder();
        for (char c : secret.toCharArray()) {
            encoded.append((char) (c + 1)); // 简单的字符偏移
        }
        return encoded.toString();
    }
    
    // 对应的解码方法
    private String decodeSecret(String encoded) {
        if (encoded == null) return null;
        
        StringBuilder decoded = new StringBuilder();
        for (char c : encoded.toCharArray()) {
            decoded.append((char) (c - 1)); // 反向字符偏移
        }
        return decoded.toString();
    }
    
    // 计算简单校验和
    private String calculateChecksum() {
        StringBuilder data = new StringBuilder();
        if (id != null) data.append(id);
        if (name != null) data.append(name);
        if (createdTime != null) data.append(createdTime.getTime());
        
        int checksum = data.toString().hashCode();
        return String.valueOf(Math.abs(checksum));
    }
    
    // 反序列化后验证方法（如果需要）
    private void readObjectNoData() throws ObjectStreamException {
        System.out.println("CustomSerializableBean.readObjectNoData() - 无数据反序列化");
        
        // 设置默认值
        id = "DEFAULT";
        name = "默认对象";
        createdTime = new Date();
        secretData = null;
        version = 1;
    }
    
    // Object替换方法（高级用法）
    private Object readResolve() throws ObjectStreamException {
        System.out.println("CustomSerializableBean.readResolve() - 对象替换检查");
        
        // 可以在这里进行对象替换或额外验证
        // 例如：实现单例模式时替换为单例实例
        
        return this; // 返回当前对象，不进行替换
    }
    
    @Override
    public String toString() {
        return "CustomSerializableBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", secretData='" + secretData + '\'' +
                ", createdTime=" + createdTime +
                ", version=" + version +
                '}';
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSecretData() { return secretData; }
    public void setSecretData(String secretData) { this.secretData = secretData; }
    
    public Date getCreatedTime() { return createdTime; }
    public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }
    
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}