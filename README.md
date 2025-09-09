# Java反序列化安全培训演示平台

## 项目简介

这是一个专为企业开发人员设计的Java反序列化安全培训演示平台。与传统的安全靶场不同，本项目**重点关注漏洞执行的完整过程和在经典框架中的具体表现**，通过深度追踪和可视化技术，让开发者从底层原理理解反序列化安全问题。

## 核心特色

### 🎯 教学理念
- **从"黑盒攻击"转向"白盒理解"** - 展示漏洞在JVM内部的执行流程
- **框架层面深度分析** - 重点展示Spring、Jackson等框架的处理机制  
- **完整执行追踪** - 不仅展示攻击成功，更重要的是展示执行过程

### 🔧 技术架构
- **TraceableObjectInputStream**: 核心调试工具，追踪每个反序列化步骤
- **JVM执行可视化**: 深入字节码级别，观察内存分配和方法调用
- **框架级集成**: 真实展示Spring容器、Jackson处理器的内部机制
- **多层次追踪**: 应用层→框架层→JVM层→系统层的全方位分析

## 功能模块

### 1. 基础追踪演示
```java
// 使用TraceableObjectInputStream进行深度追踪
TraceableObjectInputStream tois = new TraceableObjectInputStream(inputStream);
Object result = tois.readObject();
tois.printFullTrace();  // 打印完整执行轨迹
```

### 2. Spring框架演示
- Bean序列化/反序列化的完整生命周期
- AOP代理对象的序列化挑战和解决方案
- 自定义序列化逻辑的安全实现
- Spring容器上下文的影响分析

### 3. JVM执行可视化
- 方法调用栈的实时追踪
- 对象创建过程的详细记录
- 反射操作的安全风险分析
- 字节码执行的可视化展示

### 4. 内存马分析（基于现有样本）
- 分析项目中的`gslshell.jsp`内存马样本
- 展示内存马的注入机制和隐藏技术
- 使用`tomcat-memshell-scanner.jsp`进行检测演示

## 快速开始

### 环境要求
- Java 23+
- Maven 3.8+
- Tomcat 10.1+ 或其他Jakarta EE兼容容器

### 启动项目
1. 克隆项目并安装依赖：
```bash
mvn clean install
```

2. 部署到Web容器或使用嵌入式服务器

3. 访问演示页面：
```
http://localhost:8080/api/demo/
```

### 演示流程

#### 基础追踪演示
1. 访问主页面，点击"运行基础演示"
2. 观察TraceableObjectInputStream的详细追踪输出
3. 分析每个反序列化步骤和潜在安全风险

#### Spring框架演示
1. 点击"运行Spring演示"查看框架级处理
2. 观察Bean的序列化过程和AOP代理处理
3. 查看自定义序列化逻辑的执行细节

#### JVM可视化演示
1. 运行JVM演示查看底层执行过程
2. 分析方法调用时序和对象创建过程
3. 理解反射操作的安全隐患

## 培训价值

### 对开发者的价值
- **理解根本原理**: 从JVM层面理解反序列化的工作机制
- **识别风险点**: 学会在开发中识别潜在的安全风险
- **安全编码实践**: 掌握安全的序列化/反序列化实现方式

### 对架构师的价值
- **框架选型参考**: 了解不同框架的安全特性和风险点
- **系统设计指导**: 在架构设计中考虑序列化安全问题
- **防护策略制定**: 制定企业级的反序列化安全策略

### 对安全专家的价值
- **攻击原理深度理解**: 从代码级别理解攻击的执行过程
- **检测技术开发**: 基于执行特征开发检测规则
- **应急响应能力**: 快速定位和分析反序列化攻击

## 核心组件说明

### TraceableObjectInputStream
核心调试工具，继承自ObjectInputStream，添加了完整的执行追踪功能：
- 记录每个反序列化步骤的详细信息
- 检测潜在的危险类和操作
- 提供统计分析和可视化输出
- 支持自定义安全策略

### JVMExecutionVisualizer  
JVM层面的执行可视化工具：
- 使用Javassist进行字节码增强
- 追踪方法调用和对象创建过程
- 监控反射操作和安全风险
- 生成时间线可视化报告

### Spring集成演示
真实的Spring框架集成示例：
- 完整的ApplicationContext生命周期
- Bean的依赖注入和AOP代理处理
- 自定义序列化逻辑的最佳实践
- 企业级安全配置示例

## 安全声明

⚠️ **重要提醒**: 
- 本项目仅供安全培训和教育用途
- 包含的漏洞代码仅用于理解攻击原理
- 请勿将相关技术用于非法用途
- 在生产环境使用前请进行充分的安全评估

## 项目结构

```
src/main/java/com/book/demo/
├── trace/                          # 核心追踪工具
│   ├── TraceableObjectInputStream.java
│   ├── DeserializationStep.java
│   └── DeserializationStatistics.java
├── jvm/                            # JVM可视化模块
│   ├── JVMExecutionVisualizer.java
│   ├── DeserializationVisualization.java
│   └── MethodInterceptor.java
├── framework/                      # 框架演示模块
│   └── spring/
│       ├── SpringDeserializationDemo.java
│       ├── BusinessService.java
│       ├── DataService.java
│       └── CustomSerializableBean.java
├── DeserializationDemoResource.java # Web接口
└── HelloApplication.java           # 应用入口
```

## 扩展开发

本项目设计为可扩展的培训平台，可以轻松添加新的演示模块：

1. **新框架支持**: 在`framework`包下添加新的框架演示
2. **新攻击技术**: 在相应模块中添加新的攻击向量演示  
3. **新防护机制**: 扩展防护策略和检测规则
4. **新可视化方式**: 增加更多的可视化展示方式

## 贡献指南

欢迎贡献新的演示案例和教学内容，请确保：
- 代码具有教育价值，能够清晰展示安全原理
- 提供详细的注释和说明文档
- 遵循项目的安全使用准则

## 许可证

本项目仅供教育和培训用途，请遵守相关法律法规。