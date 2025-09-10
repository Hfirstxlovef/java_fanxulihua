package com.book.demo;

import com.book.demo.framework.spring.SpringDeserializationDemo;
import com.book.demo.jvm.JVMExecutionVisualizer;
import com.book.demo.trace.TraceableObjectInputStream;
import com.book.demo.components.ServletDeserializationTracer;
import com.book.demo.components.FilterDeserializationTracer;
import com.book.demo.components.ListenerDeserializationTracer;
import com.book.demo.memshell.*;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/demo")
public class DeserializationDemoResource {
    
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Response getDemoHomePage() {
        String html = generateDemoHomePage();
        return Response.ok(html).build();
    }
    
    @GET
    @Path("/trace/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response runTraceDemo(@PathParam("type") String type) {
        try {
            switch (type.toLowerCase()) {
                case "basic":
                    return Response.ok(runBasicTraceDemo()).build();
                case "dangerous":
                    return Response.ok(runEducationalDeserializationDemo("dangerous")).build();
                case "gadget":
                    return Response.ok(runEducationalDeserializationDemo("gadget")).build();
                case "dos":
                    return Response.ok(runEducationalDeserializationDemo("dos")).build();
                case "spring":
                    return Response.ok(runSpringDemo()).build();
                case "spring-bean":
                    return Response.ok(runSpringDemo("bean-lifecycle")).build();
                case "spring-aop":
                    return Response.ok(runSpringDemo("aop-proxy")).build();
                case "spring-security":
                    return Response.ok(runSpringDemo("security-context")).build();
                case "jvm":
                    return Response.ok(runJVMVisualizationDemo()).build();
                case "jvm-memory":
                    return Response.ok(runJVMVisualizationDemo("memory-analysis")).build();
                case "jvm-bytecode":
                    return Response.ok(runJVMVisualizationDemo("bytecode-trace")).build();
                case "jvm-reflection":
                    return Response.ok(runJVMVisualizationDemo("reflection-chain")).build();
                case "jvm-performance":
                    return Response.ok(runJVMVisualizationDemo("performance-analysis")).build();
                case "servlet":
                    return Response.ok(runServletTraceDemo()).build();
                case "filter":
                    return Response.ok(runFilterTraceDemo()).build();
                case "listener":
                    return Response.ok(runListenerTraceDemo()).build();
                case "components":
                    return Response.ok(runAllComponentsTraceDemo()).build();
                default:
                    return Response.status(400).entity("{\"error\":\"未知的演示类型\"}").build();
            }
        } catch (Exception e) {
            return Response.status(500).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }
    
    @GET
    @Path("/visualization/{type}")
    @Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
    public Response getVisualization(@PathParam("type") String type) {
        try {
            String htmlContent;
            switch (type.toLowerCase()) {
                case "spring":
                    htmlContent = generateSpringVisualizationHTML();
                    break;
                case "jvm":
                    htmlContent = generateJVMVisualizationHTML();
                    break;
                default:
                    htmlContent = "<h1>未知的可视化类型</h1>";
                    break;
            }
            return Response.ok(htmlContent)
                    .type(MediaType.TEXT_HTML + "; charset=UTF-8")
                    .build();
        } catch (Exception e) {
            return Response.status(500)
                    .entity("<h1>生成可视化失败: " + e.getMessage() + "</h1>")
                    .type(MediaType.TEXT_HTML + "; charset=UTF-8")
                    .build();
        }
    }
    
    @GET
    @Path("/listener-shell")
    @Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
    public Response getListenerShell(@QueryParam("action") String action) {
        String htmlContent = generateListenerShellHTML(action);
        return Response.ok(htmlContent)
                .type(MediaType.TEXT_HTML + "; charset=UTF-8")
                .build();
    }
    
    @POST
    @Path("/listener-cmd")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeListenerCommand(@QueryParam("cmd") String cmd) {
        try {
            if (cmd == null || cmd.trim().isEmpty()) {
                return Response.ok("{\"status\":\"error\",\"message\":\"命令不能为空\"}").build();
            }
            
            // 安全检查 - 仅允许特定的演示命令
            if (!isAllowedCommand(cmd)) {
                return Response.ok("{\"status\":\"error\",\"message\":\"不允许执行该命令（仅限演示用命令）\"}").build();
            }
            
            String result = executeCommand(cmd);
            return Response.ok("{\"status\":\"success\",\"result\":\"" + escapeJson(result) + "\"}").build();
            
        } catch (Exception e) {
            return Response.ok("{\"status\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}").build();
        }
    }
    
    @GET
    @Path("/shell")
    @Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
    public Response getShellDemo(@QueryParam("action") String action, @QueryParam("type") String type) {
        if ("info".equals(action) && type != null) {
            String html = generateShellDemoHTML(type);
            return Response.ok(html)
                    .type(MediaType.TEXT_HTML + "; charset=UTF-8")
                    .build();
        }
        
        return Response.ok("<h1>Shell Demo</h1><p>Invalid parameters</p>")
                .type(MediaType.TEXT_HTML + "; charset=UTF-8")
                .build();
    }
    
    private String generateShellDemoHTML(String type) {
        String title = type.substring(0, 1).toUpperCase() + type.substring(1) + " 内存马演示";
        String icon = "servlet".equals(type) ? "🔍" : "filter".equals(type) ? "🔄" : "👂";
        
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>" + title + "</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: 'Microsoft YaHei', Arial, sans-serif;\n" +
            "            margin: 0;\n" +
            "            padding: 20px;\n" +
            "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
            "            min-height: 100vh;\n" +
            "            color: #333;\n" +
            "        }\n" +
            "        \n" +
            "        .container {\n" +
            "            max-width: 1000px;\n" +
            "            margin: 0 auto;\n" +
            "            background: white;\n" +
            "            border-radius: 15px;\n" +
            "            box-shadow: 0 20px 40px rgba(0,0,0,0.1);\n" +
            "            padding: 30px;\n" +
            "        }\n" +
            "        \n" +
            "        .header {\n" +
            "            text-align: center;\n" +
            "            margin-bottom: 30px;\n" +
            "            padding-bottom: 20px;\n" +
            "            border-bottom: 2px solid #f0f0f0;\n" +
            "        }\n" +
            "        \n" +
            "        .header h1 {\n" +
            "            color: #2c3e50;\n" +
            "            margin: 0;\n" +
            "            font-size: 2.5em;\n" +
            "        }\n" +
            "        \n" +
            "        .warning {\n" +
            "            background: #fff3cd;\n" +
            "            border: 1px solid #ffeaa7;\n" +
            "            border-radius: 8px;\n" +
            "            padding: 15px;\n" +
            "            margin: 20px 0;\n" +
            "            color: #856404;\n" +
            "        }\n" +
            "        \n" +
            "        .info-section {\n" +
            "            background: #f8f9fa;\n" +
            "            border-radius: 8px;\n" +
            "            padding: 20px;\n" +
            "            margin: 20px 0;\n" +
            "        }\n" +
            "        \n" +
            "        .demo-section {\n" +
            "            background: #e8f5e8;\n" +
            "            border: 1px solid #28a745;\n" +
            "            border-radius: 8px;\n" +
            "            padding: 20px;\n" +
            "            margin: 20px 0;\n" +
            "        }\n" +
            "        \n" +
            "        .command-input {\n" +
            "            width: 100%;\n" +
            "            padding: 10px;\n" +
            "            border: 1px solid #ddd;\n" +
            "            border-radius: 5px;\n" +
            "            font-family: monospace;\n" +
            "            margin: 10px 0;\n" +
            "        }\n" +
            "        \n" +
            "        .btn {\n" +
            "            background: #007bff;\n" +
            "            color: white;\n" +
            "            border: none;\n" +
            "            padding: 10px 20px;\n" +
            "            border-radius: 5px;\n" +
            "            cursor: pointer;\n" +
            "            margin: 5px;\n" +
            "        }\n" +
            "        \n" +
            "        .btn:hover {\n" +
            "            background: #0056b3;\n" +
            "        }\n" +
            "        \n" +
            "        .output {\n" +
            "            background: #1e1e1e;\n" +
            "            color: #00ff00;\n" +
            "            padding: 15px;\n" +
            "            border-radius: 5px;\n" +
            "            font-family: monospace;\n" +
            "            margin: 10px 0;\n" +
            "            min-height: 100px;\n" +
            "            white-space: pre-wrap;\n" +
            "        }\n" +
            "        \n" +
            "        .feature-list {\n" +
            "            list-style-type: none;\n" +
            "            padding: 0;\n" +
            "        }\n" +
            "        \n" +
            "        .feature-list li {\n" +
            "            padding: 8px 0;\n" +
            "            border-bottom: 1px solid #eee;\n" +
            "        }\n" +
            "        \n" +
            "        .feature-list li:before {\n" +
            "            content: \"✓ \";\n" +
            "            color: #28a745;\n" +
            "            font-weight: bold;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1>" + icon + " " + title + "</h1>\n" +
            "            <p>内存马技术演示与教育平台</p>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"warning\">\n" +
            "            <h3>⚠️ 安全警告</h3>\n" +
            "            <p>此演示仅用于教育目的，展示" + type + "内存马的工作原理和检测方法。请勿用于恶意用途。</p>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"info-section\">\n" +
            "            <h3>📋 " + type.substring(0, 1).toUpperCase() + type.substring(1) + "内存马简介</h3>\n" +
            "            <p>" + getShellDescription(type) + "</p>\n" +
            "            \n" +
            "            <h4>🔧 技术特征:</h4>\n" +
            "            <ul class=\"feature-list\">\n" +
            "                " + getShellFeatures(type) + "\n" +
            "            </ul>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"demo-section\">\n" +
            "            <h3>💻 命令执行演示</h3>\n" +
            "            <p>输入演示命令查看内存马的执行效果：</p>\n" +
            "            \n" +
            "            <input type=\"text\" id=\"cmdInput\" class=\"command-input\" placeholder=\"输入命令 (如: whoami, pwd, ls)\" value=\"whoami\">\n" +
            "            <br>\n" +
            "            <button class=\"btn\" onclick=\"executeDemo()\">执行演示命令</button>\n" +
            "            <button class=\"btn\" onclick=\"showInfo()\">显示系统信息</button>\n" +
            "            <button class=\"btn\" onclick=\"clearOutput()\">清空输出</button>\n" +
            "            \n" +
            "            <div class=\"output\" id=\"output\">等待命令执行...</div>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"info-section\">\n" +
            "            <h3>🛡️ 检测与防护建议</h3>\n" +
            "            <ul class=\"feature-list\">\n" +
            "                <li>定期检查异常的Servlet/Filter注册</li>\n" +
            "                <li>监控系统中动态加载的类</li>\n" +
            "                <li>使用专业的内存马检测工具</li>\n" +
            "                <li>加强应用程序的输入验证</li>\n" +
            "                <li>实施严格的访问控制策略</li>\n" +
            "            </ul>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    \n" +
            "    <script>\n" +
            "        function executeDemo() {\n" +
            "            const cmd = document.getElementById('cmdInput').value;\n" +
            "            const output = document.getElementById('output');\n" +
            "            \n" +
            "            if (!cmd.trim()) {\n" +
            "                output.textContent = '请输入有效命令';\n" +
            "                return;\n" +
            "            }\n" +
            "            \n" +
            "            output.textContent = '正在执行命令: ' + cmd + '\\n';\n" +
            "            \n" +
            "            // 模拟命令执行\n" +
            "            fetch('/api/demo/listener-cmd?cmd=' + encodeURIComponent(cmd))\n" +
            "                .then(response => response.json())\n" +
            "                .then(data => {\n" +
            "                    if (data.status === 'success') {\n" +
            "                        output.textContent += '执行结果:\\n' + data.result;\n" +
            "                    } else {\n" +
            "                        output.textContent += '执行失败: ' + data.message;\n" +
            "                    }\n" +
            "                })\n" +
            "                .catch(error => {\n" +
            "                    output.textContent += '请求失败: ' + error.message;\n" +
            "                });\n" +
            "        }\n" +
            "        \n" +
            "        function showInfo() {\n" +
            "            const output = document.getElementById('output');\n" +
            "            const info = `" + type.substring(0, 1).toUpperCase() + type.substring(1) + "内存马演示信息:\n" +
            "            \n" +
            "类型: " + type.toUpperCase() + " Memory Shell\n" +
            "状态: 演示模式\n" +
            "URL模式: " + getUrlPattern(type) + "\n" +
            "创建时间: " + new Date().toLocaleString() + "\n" +
            "安全级别: 教育演示（受限制）\n" +
            "\n" +
            "注意: 此为演示环境，命令执行受到严格限制。`;\n" +
            "            \n" +
            "            output.textContent = info;\n" +
            "        }\n" +
            "        \n" +
            "        function clearOutput() {\n" +
            "            document.getElementById('output').textContent = '等待命令执行...';\n" +
            "        }\n" +
            "        \n" +
            "        // 页面加载时显示基本信息\n" +
            "        window.onload = function() {\n" +
            "            showInfo();\n" +
            "        };\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";
    }
    
    private String getShellDescription(String type) {
        return switch (type) {
            case "servlet" -> "Servlet内存马通过动态注册Servlet组件实现持久化访问，攻击者可以通过特定URL执行任意命令。";
            case "filter" -> "Filter内存马通过注册过滤器来拦截HTTP请求，在请求处理过程中执行恶意代码，具有更强的隐蔽性。";
            default -> "内存马是一种无文件的持久化技术，通过在应用程序内存中植入恶意代码来维持访问权限。";
        };
    }
    
    private String getShellFeatures(String type) {
        return switch (type) {
            case "servlet" -> 
                "<li>动态注册Servlet组件</li>" +
                "<li>响应特定URL路径请求</li>" +
                "<li>支持命令执行和文件操作</li>" +
                "<li>无需在磁盘上留下文件</li>" +
                "<li>重启后自动清除</li>";
            case "filter" -> 
                "<li>注册在过滤器链中</li>" +
                "<li>可拦截所有HTTP请求</li>" +
                "<li>执行优先级高</li>" +
                "<li>更难被检测发现</li>" +
                "<li>支持请求和响应修改</li>";
            default -> 
                "<li>内存中执行，无文件痕迹</li>" +
                "<li>持久化访问能力</li>" +
                "<li>支持命令执行</li>";
        };
    }
    
    private String getUrlPattern(String type) {
        return switch (type) {
            case "servlet" -> "/shell/*";
            case "filter" -> "/*";
            case "listener" -> "/listener-shell/*";
            default -> "/shell/*";
        };
    }
    
    private String runBasicTraceDemo() {
        return runEducationalDeserializationDemo("basic");
    }
    
    /**
     * 运行教育性反序列化演示
     */
    private String runEducationalDeserializationDemo(String scenario) {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"status\": \"success\",\n");
            json.append("  \"scenario\": \"").append(scenario).append("\",\n");
            
            switch (scenario.toLowerCase()) {
                case "basic":
                    return runBasicEducationalDemo(json);
                case "dangerous":
                    return runDangerousEducationalDemo(json);
                case "gadget":
                    return runGadgetChainDemo(json);
                case "dos":
                    return runDoSDemo(json);
                default:
                    return runBasicEducationalDemo(json);
            }
            
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\",\"scenario\":\"" + scenario + "\"}";
        }
    }
    
    /**
     * 基础教育演示
     */
    private String runBasicEducationalDemo(StringBuilder json) throws Exception {
        // 创建一个简单的可序列化对象进行演示
        DemoObject obj = new DemoObject("教育演示对象", 12345);
        
        // 序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        
        // 使用带教育模式的TraceableObjectInputStream反序列化
        byte[] serializedData = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
        TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true, true); // 启用教育模式
        
        DemoObject deserializedObj = (DemoObject) tois.readObjectWithEducationalTrace();
        tois.close();
        
        // 生成教育性JSON结果
        json.append("  \"type\": \"basic\",\n");
        json.append("  \"description\": \"基础Java对象序列化/反序列化教育演示\",\n");
        json.append("  \"originalObject\": \"").append(escapeJson(obj.toString())).append("\",\n");
        json.append("  \"deserializedObject\": \"").append(escapeJson(deserializedObj.toString())).append("\",\n");
        json.append("  \"educationalSteps\": ").append(tois.getEducationalSteps().size()).append(",\n");
        json.append("  \"traceSteps\": ").append(tois.getExecutionTrace().size()).append(",\n");
        json.append("  \"educationLog\": \"").append(escapeJson(tois.getEducationLog())).append("\",\n");
        json.append("  \"fullTrace\": \"").append(escapeJson(tois.getTraceAsString())).append("\",\n");
        json.append("  \"statistics\": ").append(tois.getStatistics().toJsonSummary()).append(",\n");
        json.append("  \"securityLevel\": \"SAFE\",\n");
        json.append("  \"educationalPoints\": [\n");
        json.append("    \"Java序列化的基本工作原理\",\n");
        json.append("    \"ObjectInputStream的读取流程\",\n");
        json.append("    \"对象实例化和字段恢复过程\",\n");
        json.append("    \"自定义readObject方法的检测\"\n");
        json.append("  ]\n");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * 危险操作教育演示
     */
    private String runDangerousEducationalDemo(StringBuilder json) throws Exception {
        // 创建含有危险命令的演示对象
        VulnerableDemo vulnObj = new VulnerableDemo("whoami", "演示危险的readObject方法");
        
        // 序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(vulnObj);
        oos.close();
        
        // 使用带教育模式的TraceableObjectInputStream反序列化
        byte[] serializedData = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
        TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true, true); // 启用教育模式
        
        VulnerableDemo deserializedObj = (VulnerableDemo) tois.readObjectWithEducationalTrace();
        tois.close();
        
        // 生成教育性JSON结果
        json.append("  \"type\": \"dangerous\",\n");
        json.append("  \"description\": \"危险的readObject方法教育演示 - 展示反序列化安全风险\",\n");
        json.append("  \"originalObject\": \"").append(escapeJson(vulnObj.toString())).append("\",\n");
        json.append("  \"deserializedObject\": \"").append(escapeJson(deserializedObj.toString())).append("\",\n");
        json.append("  \"educationalSteps\": ").append(tois.getEducationalSteps().size()).append(",\n");
        json.append("  \"traceSteps\": ").append(tois.getExecutionTrace().size()).append(",\n");
        json.append("  \"educationLog\": \"").append(escapeJson(tois.getEducationLog())).append("\",\n");
        json.append("  \"vulnerabilityLog\": \"").append(escapeJson(deserializedObj.getExecutionLog())).append("\",\n");
        json.append("  \"fullTrace\": \"").append(escapeJson(tois.getTraceAsString())).append("\",\n");
        json.append("  \"statistics\": ").append(tois.getStatistics().toJsonSummary()).append(",\n");
        json.append("  \"securityLevel\": \"HIGH_RISK\",\n");
        json.append("  \"securityWarnings\": [\n");
        json.append("    \"检测到自定义readObject方法\",\n");
        json.append("    \"在反序列化过程中执行了外部命令\",\n");
        json.append("    \"这种模式可被攻击者利用进行RCE攻击\"\n");
        json.append("  ],\n");
        json.append("  \"educationalPoints\": [\n");
        json.append("    \"自定义readObject方法的安全风险\",\n");
        json.append("    \"反序列化过程中的代码执行\",\n");
        json.append("    \"输入验证的重要性\",\n");
        json.append("    \"安全的序列化实践\"\n");
        json.append("  ]\n");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Gadget链教育演示
     */
    private String runGadgetChainDemo(StringBuilder json) throws Exception {
        // 创建Gadget链演示对象
        GadgetChainDemo gadgetObj = new GadgetChainDemo("runtime.exec", "whoami");
        
        // 序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(gadgetObj);
        oos.close();
        
        // 使用带教育模式的TraceableObjectInputStream反序列化
        byte[] serializedData = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
        TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true, true); // 启用教育模式
        
        GadgetChainDemo deserializedObj = (GadgetChainDemo) tois.readObjectWithEducationalTrace();
        tois.close();
        
        // 生成教育性JSON结果
        json.append("  \"type\": \"gadget\",\n");
        json.append("  \"description\": \"Gadget Chain攻击链教育演示 - 展示复杂反序列化攻击\",\n");
        json.append("  \"originalObject\": \"").append(escapeJson(gadgetObj.toString())).append("\",\n");
        json.append("  \"deserializedObject\": \"").append(escapeJson(deserializedObj.toString())).append("\",\n");
        json.append("  \"educationalSteps\": ").append(tois.getEducationalSteps().size()).append(",\n");
        json.append("  \"traceSteps\": ").append(tois.getExecutionTrace().size()).append(",\n");
        json.append("  \"educationLog\": \"").append(escapeJson(tois.getEducationLog())).append("\",\n");
        json.append("  \"gadgetChainLog\": \"").append(escapeJson(deserializedObj.getChainLog())).append("\",\n");
        json.append("  \"fullTrace\": \"").append(escapeJson(tois.getTraceAsString())).append("\",\n");
        json.append("  \"statistics\": ").append(tois.getStatistics().toJsonSummary()).append(",\n");
        json.append("  \"securityLevel\": \"CRITICAL\",\n");
        json.append("  \"attackVector\": \"Commons Collections Gadget Chain\",\n");
        json.append("  \"securityWarnings\": [\n");
        json.append("    \"检测到Gadget Chain攻击模式\",\n");
        json.append("    \"多个类的readObject方法被链式调用\",\n");
        json.append("    \"最终导致任意代码执行\"\n");
        json.append("  ],\n");
        json.append("  \"educationalPoints\": [\n");
        json.append("    \"Gadget Chain攻击的工作原理\",\n");
        json.append("    \"Commons Collections漏洞分析\",\n");
        json.append("    \"反序列化攻击链的构造\",\n");
        json.append("    \"防御Gadget Chain攻击的方法\"\n");
        json.append("  ]\n");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * DoS攻击教育演示
     */
    private String runDoSDemo(StringBuilder json) throws Exception {
        // 创建一个大对象用于DoS演示（这里简化实现）
        DemoObject dosObj = new DemoObject("DoS演示对象", Integer.MAX_VALUE);
        
        // 序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(dosObj);
        oos.close();
        
        // 使用带教育模式的TraceableObjectInputStream反序列化
        byte[] serializedData = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
        TraceableObjectInputStream tois = new TraceableObjectInputStream(bais, true, true); // 启用教育模式
        
        long startTime = System.currentTimeMillis();
        DemoObject deserializedObj = (DemoObject) tois.readObjectWithEducationalTrace();
        long endTime = System.currentTimeMillis();
        tois.close();
        
        // 生成教育性JSON结果
        json.append("  \"type\": \"dos\",\n");
        json.append("  \"description\": \"反序列化DoS攻击教育演示 - 展示资源耗尽攻击\",\n");
        json.append("  \"originalObject\": \"").append(escapeJson(dosObj.toString())).append("\",\n");
        json.append("  \"deserializedObject\": \"").append(escapeJson(deserializedObj.toString())).append("\",\n");
        json.append("  \"educationalSteps\": ").append(tois.getEducationalSteps().size()).append(",\n");
        json.append("  \"traceSteps\": ").append(tois.getExecutionTrace().size()).append(",\n");
        json.append("  \"educationLog\": \"").append(escapeJson(tois.getEducationLog())).append("\",\n");
        json.append("  \"processingTime\": ").append(endTime - startTime).append(",\n");
        json.append("  \"dataSize\": ").append(serializedData.length).append(",\n");
        json.append("  \"fullTrace\": \"").append(escapeJson(tois.getTraceAsString())).append("\",\n");
        json.append("  \"statistics\": ").append(tois.getStatistics().toJsonSummary()).append(",\n");
        json.append("  \"securityLevel\": \"MEDIUM_RISK\",\n");
        json.append("  \"attackVector\": \"Resource Exhaustion\",\n");
        json.append("  \"securityWarnings\": [\n");
        json.append("    \"大量数据反序列化可能导致内存耗尽\",\n");
        json.append("    \"处理时间过长可能导致服务不可用\",\n");
        json.append("    \"需要限制反序列化数据的大小\"\n");
        json.append("  ],\n");
        json.append("  \"educationalPoints\": [\n");
        json.append("    \"反序列化DoS攻击的原理\",\n");
        json.append("    \"资源限制的重要性\",\n");
        json.append("    \"输入大小验证\",\n");
        json.append("    \"超时机制的实现\"\n");
        json.append("  ]\n");
        json.append("}");
        
        return json.toString();
    }
    
    private String runSpringDemo() {
        return runSpringDemo("basic");
    }
    
    private String runSpringDemo(String scenario) {
        try {
            SpringDeserializationDemo.SpringExecutionTracer tracer = 
                new SpringDeserializationDemo.SpringExecutionTracer();
            
            var result = tracer.demonstrateSpringDeserialization();
            
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"status\": \"success\",\n");
            json.append("  \"type\": \"spring\",\n");
            json.append("  \"scenario\": \"").append(scenario).append("\",\n");
            json.append("  \"description\": \"").append(getSpringScenarioDescription(scenario)).append("\",\n");
            json.append("  \"executionSteps\": ").append(result.getExecutionSteps().size()).append(",\n");
            json.append("  \"beanCount\": ").append(result.getBeanCreationMap().size()).append(",\n");
            json.append("  \"springContextActive\": ").append(result.isContextActive()).append(",\n");
            json.append("  \"educationalSteps\": ").append(result.getEducationalSteps().size()).append(",\n");
            json.append("  \"educationLog\": \"").append(escapeJson(result.getEducationLog())).append("\",\n");
            json.append("  \"securityLevel\": \"").append(getSpringSecurityLevel(scenario)).append("\",\n");
            json.append("  \"securityWarnings\": ").append(generateSpringSecurityWarnings(scenario)).append(",\n");
            json.append("  \"educationalPoints\": ").append(generateSpringEducationalPoints(scenario)).append(",\n");
            json.append("  \"details\": [\n");
            
            var steps = result.getExecutionSteps();
            for (int i = 0; i < steps.size(); i++) {
                if (i > 0) json.append(",\n");
                var step = steps.get(i);
                json.append("    {\"step\": \"").append(escapeJson(step.getDescription()))
                    .append("\", \"type\": \"").append(step.getStepType()).append("\"}");
            }
            
            json.append("\n  ]\n");
            json.append("}");
            
            return json.toString();
            
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\",\"scenario\":\"" + scenario + "\"}";
        }
    }
    
    private String getSpringScenarioDescription(String scenario) {
        switch (scenario.toLowerCase()) {
            case "bean-lifecycle":
                return "Spring Bean生命周期演示 - 展示Bean从创建到销毁的完整过程";
            case "aop-proxy":
                return "Spring AOP代理演示 - 展示代理对象的序列化和反序列化过程";
            case "security-context":
                return "Spring Security上下文演示 - 展示安全上下文的序列化风险";
            default:
                return "Spring框架基础序列化演示 - 展示Spring环境下的对象序列化";
        }
    }
    
    private String getSpringSecurityLevel(String scenario) {
        switch (scenario.toLowerCase()) {
            case "security-context":
                return "HIGH_RISK";
            case "aop-proxy":
                return "MEDIUM_RISK";
            default:
                return "SAFE";
        }
    }
    
    private String generateSpringSecurityWarnings(String scenario) {
        switch (scenario.toLowerCase()) {
            case "security-context":
                return "[\"Spring Security上下文包含敏感信息\", \"可能泄露用户认证状态\", \"需要加密序列化数据\"]";
            case "aop-proxy":
                return "[\"AOP代理对象可能执行意外的切面逻辑\", \"代理链可能被恶意利用\"]";
            case "bean-lifecycle":
                return "[\"Bean初始化回调可能执行危险操作\", \"需要验证Bean的来源\"]";
            default:
                return "[]";
        }
    }
    
    private String generateSpringEducationalPoints(String scenario) {
        switch (scenario.toLowerCase()) {
            case "bean-lifecycle":
                return "[\"Spring Bean的完整生命周期\", \"InitializingBean和DisposableBean接口\", \"@PostConstruct和@PreDestroy注解\", \"BeanPostProcessor的作用\"]";
            case "aop-proxy":
                return "[\"Spring AOP的工作原理\", \"JDK动态代理vs CGLIB代理\", \"代理对象的序列化陷阱\", \"切面逻辑的安全考虑\"]";
            case "security-context":
                return "[\"Spring Security架构\", \"SecurityContextHolder的工作机制\", \"认证信息的序列化风险\", \"会话安全最佳实践\"]";
            default:
                return "[\"Spring容器的基本概念\", \"依赖注入机制\", \"Spring序列化支持\", \"框架级序列化安全\"]";
        }
    }
    
    private String runJVMVisualizationDemo() {
        return runJVMVisualizationDemo("basic");
    }
    
    private String runJVMVisualizationDemo(String scenario) {
        try {
            JVMExecutionVisualizer visualizer = new JVMExecutionVisualizer();
            
            // 根据场景配置不同的监控参数
            configureJVMVisualizerForScenario(visualizer, scenario);
            
            // 创建演示对象
            Object demoObj = createJVMDemoObject(scenario);
            
            // 序列化
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(demoObj);
            oos.close();
            
            // JVM可视化分析
            long startTime = System.currentTimeMillis();
            var visualization = visualizer.visualizeDeserialization(baos.toByteArray());
            long endTime = System.currentTimeMillis();
            
            // 生成增强的JSON结果
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"status\": \"success\",\n");
            json.append("  \"type\": \"jvm\",\n");
            json.append("  \"scenario\": \"").append(scenario).append("\",\n");
            json.append("  \"description\": \"").append(getJVMScenarioDescription(scenario)).append("\",\n");
            json.append("  \"analysisTime\": ").append(endTime - startTime).append(",\n");
            json.append("  \"educationalSteps\": ").append(visualization.getEducationalSteps().size()).append(",\n");
            json.append("  \"educationLog\": \"").append(escapeJson(visualization.getEducationLog())).append("\",\n");
            json.append("  \"securityLevel\": \"").append(getJVMSecurityLevel(scenario)).append("\",\n");
            json.append("  \"securityWarnings\": ").append(generateJVMSecurityWarnings(scenario)).append(",\n");
            json.append("  \"educationalPoints\": ").append(generateJVMEducationalPoints(scenario)).append(",\n");
            json.append("  \"memoryUsage\": ").append(visualization.getMemoryUsageData()).append(",\n");
            json.append("  \"performanceMetrics\": ").append(visualization.getPerformanceMetrics()).append(",\n");
            json.append("  \"executionFrames\": ").append(visualization.getExecutionFrames().size()).append(",\n");
            json.append("  \"visualizationData\": ").append(visualization.toJsonString()).append("\n");
            json.append("}");
            
            return json.toString();
            
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\",\"scenario\":\"" + scenario + "\"}";
        }
    }
    
    private void configureJVMVisualizerForScenario(JVMExecutionVisualizer visualizer, String scenario) {
        switch (scenario.toLowerCase()) {
            case "memory-analysis":
                visualizer.enableMemoryProfiling();
                visualizer.setMemorySamplingInterval(10);
                break;
            case "bytecode-trace":
                visualizer.enableBytecodeInstrumentation();
                visualizer.addMonitoredPackage("java.lang.reflect");
                break;
            case "reflection-chain":
                visualizer.enableReflectionTracking();
                visualizer.addMonitoredPackage("java.lang.reflect");
                visualizer.addMonitoredPackage("sun.reflect");
                break;
            case "performance-analysis":
                visualizer.enablePerformanceProfiling();
                visualizer.setProfileSamplingRate(1);
                break;
            default:
                visualizer.enableBasicInstrumentation();
                break;
        }
    }
    
    private Object createJVMDemoObject(String scenario) {
        switch (scenario.toLowerCase()) {
            case "memory-analysis":
                // 创建大对象用于内存分析
                return new LargeObject(1000, "JVM内存分析演示");
            case "bytecode-trace":
                // 创建有复杂方法调用的对象
                return new ComplexObject("字节码追踪", true);
            case "reflection-chain":
                // 创建使用反射的对象
                return new ReflectiveObject("反射调用链演示");
            case "performance-analysis":
                // 创建性能密集型对象
                return new PerformanceObject(500);
            default:
                return new DemoObject("JVM基础演示", 99999);
        }
    }
    
    private String getJVMScenarioDescription(String scenario) {
        switch (scenario.toLowerCase()) {
            case "memory-analysis":
                return "JVM内存分析演示 - 详细追踪堆内存使用情况和对象分配";
            case "bytecode-trace":
                return "字节码执行追踪 - 深入分析每条字节码指令的执行过程";
            case "reflection-chain":
                return "反射调用链分析 - 追踪反射API的完整调用过程";
            case "performance-analysis":
                return "JVM性能分析 - 监控执行性能和资源消耗";
            default:
                return "JVM基础可视化 - 展示JVM层面的基本执行机制";
        }
    }
    
    private String getJVMSecurityLevel(String scenario) {
        switch (scenario.toLowerCase()) {
            case "reflection-chain":
                return "HIGH_RISK";
            case "bytecode-trace":
                return "MEDIUM_RISK";
            default:
                return "SAFE";
        }
    }
    
    private String generateJVMSecurityWarnings(String scenario) {
        switch (scenario.toLowerCase()) {
            case "reflection-chain":
                return "[\"反射调用可能绕过访问控制\", \"setAccessible()可能暴露私有成员\", \"动态方法调用难以静态分析\"]";
            case "bytecode-trace":
                return "[\"字节码操作可能修改类行为\", \"需要防范字节码注入攻击\"]";
            case "memory-analysis":
                return "[\"大对象可能导致内存耗尽\", \"需要监控堆内存使用\"]";
            default:
                return "[]";
        }
    }
    
    private String generateJVMEducationalPoints(String scenario) {
        switch (scenario.toLowerCase()) {
            case "memory-analysis":
                return "[\"JVM堆内存结构\", \"对象分配和回收机制\", \"内存泄漏检测方法\", \"垃圾收集器工作原理\"]";
            case "bytecode-trace":
                return "[\"Java字节码指令集\", \"栈帧和操作数栈\", \"方法调用和返回机制\", \"异常处理的字节码实现\"]";
            case "reflection-chain":
                return "[\"Java反射API详解\", \"Method.invoke()实现原理\", \"反射性能优化技巧\", \"反射安全注意事项\"]";
            case "performance-analysis":
                return "[\"JVM性能调优基础\", \"热点代码识别\", \"JIT编译器优化\", \"性能监控工具使用\"]";
            default:
                return "[\"JVM基础架构\", \"类加载机制\", \"执行引擎原理\", \"运行时数据区域\"]";
        }
    }
    
    private String generateDemoHomePage() {
        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Java反序列化安全培训演示平台</title>
    <style>
        body {
            font-family: 'Microsoft YaHei', Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #333;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 10px;
            padding: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
        }
        h1 {
            text-align: center;
            color: #2c3e50;
            margin-bottom: 10px;
            font-size: 2.5em;
        }
        .subtitle {
            text-align: center;
            color: #7f8c8d;
            margin-bottom: 30px;
            font-size: 1.2em;
        }
        .demo-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
            gap: 20px;
            margin: 30px 0;
        }
        .demo-card {
            background: #f8f9fa;
            border-radius: 8px;
            padding: 20px;
            border-left: 5px solid #3498db;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }
        .demo-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 15px 25px rgba(0,0,0,0.15);
        }
        .demo-card h3 {
            color: #2c3e50;
            margin-top: 0;
            margin-bottom: 15px;
        }
        .demo-card p {
            color: #555;
            line-height: 1.6;
            margin-bottom: 20px;
        }
        .btn {
            display: inline-block;
            padding: 10px 20px;
            background: #3498db;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin-right: 10px;
            margin-bottom: 10px;
            transition: background-color 0.3s;
        }
        .btn:hover {
            background: #2980b9;
        }
        .btn-success { background: #27ae60; }
        .btn-success:hover { background: #229954; }
        .btn-warning { background: #f39c12; }
        .btn-warning:hover { background: #e67e22; }
        .btn-danger { background: #e74c3c; }
        .btn-danger:hover { background: #c0392b; }
        
        .features {
            margin: 40px 0;
            padding: 20px;
            background: #ecf0f1;
            border-radius: 8px;
        }
        .features h2 {
            color: #2c3e50;
            margin-bottom: 20px;
        }
        .feature-list {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
        }
        .feature-item {
            padding: 15px;
            background: white;
            border-radius: 5px;
            border-left: 3px solid #3498db;
        }
        .feature-item h4 {
            color: #2c3e50;
            margin: 0 0 10px 0;
        }
        .feature-item p {
            color: #555;
            margin: 0;
            font-size: 0.9em;
        }
        
        .warning {
            background: #fff3cd;
            border: 1px solid #ffeeba;
            color: #856404;
            padding: 15px;
            border-radius: 5px;
            margin: 20px 0;
        }
        .warning strong {
            color: #d73502;
        }
        
        /* Result tabs styling */
        .result-tab {
            background: #95a5a6;
            color: white;
            border: none;
            padding: 10px 15px;
            margin-right: 5px;
            border-radius: 5px 5px 0 0;
            cursor: pointer;
            font-size: 14px;
            transition: background-color 0.3s;
        }
        .result-tab:hover {
            background: #7f8c8d;
        }
        .result-tab.active {
            background: #3498db;
        }
        .result-content {
            display: none;
        }
        .result-content.active {
            display: block;
        }
        .education-step {
            margin: 10px 0;
            padding: 10px;
            background: #f8f9fa;
            border-left: 3px solid #28a745;
            border-radius: 3px;
        }
        .security-warning {
            padding: 15px;
            margin: 10px 0;
            border-radius: 5px;
            border-left: 4px solid #e74c3c;
            background: #fdf2f2;
            color: #721c24;
        }
        .security-safe {
            border-left-color: #27ae60;
            background: #d5f4e6;
            color: #155724;
        }
        .security-medium {
            border-left-color: #f39c12;
            background: #fff3cd;
            color: #856404;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Java反序列化安全培训演示平台</h1>
        <p class="subtitle">深度剖析反序列化漏洞原理与防护机制</p>
        
        <div class="warning">
            <strong>⚠️ 安全提醒:</strong> 
            本演示平台仅供安全培训和教育用途，包含的漏洞代码仅用于理解攻击原理，请勿用于非法用途。
        </div>
        
        <div class="demo-grid">
            <div class="demo-card">
                <h3>🎓 Java反序列化教育演示</h3>
                <p>深度剖析Java反序列化过程，通过4个不同安全级别的演示帮助开发者理解反序列化机制与安全风险。</p>
                <a href="#" class="btn" onclick="runDemo('basic')">🟢 基础安全演示</a>
                <a href="#" class="btn btn-warning" onclick="runDemo('dangerous')">🟡 危险操作演示</a>
                <a href="#" class="btn btn-danger" onclick="runDemo('gadget')">🔴 Gadget链攻击</a>
                <a href="#" class="btn btn-secondary" onclick="runDemo('dos')">⚫ DoS攻击演示</a>
                <a href="#" class="btn btn-success" onclick="showTrace('basic')">📊 查看详细追踪</a>
            </div>
            
            <div class="demo-card">
                <h3>🌱 Spring框架演示</h3>
                <p>演示Spring框架中Bean的序列化/反序列化过程，包括AOP代理对象的处理机制和自定义序列化逻辑。</p>
                <a href="#" class="btn" onclick="runDemo('spring')">基础演示</a>
                <a href="#" class="btn btn-success" onclick="runDemo('spring-bean')">Bean生命周期</a>
                <a href="#" class="btn btn-warning" onclick="runDemo('spring-aop')">AOP代理演示</a>
                <a href="#" class="btn btn-danger" onclick="runDemo('spring-security')">安全上下文</a>
                <a href="/api/demo/visualization/spring" class="btn btn-secondary" target="_blank">查看可视化</a>
            </div>
            
            <div class="demo-card">
                <h3>⚙️ JVM执行可视化</h3>
                <p>深入JVM层面，可视化反序列化过程中的方法调用、对象创建和反射操作，理解底层执行机制。</p>
                <a href="#" class="btn" onclick="runDemo('jvm')">基础演示</a>
                <a href="#" class="btn btn-info" onclick="runDemo('jvm-memory')">内存分析</a>
                <a href="#" class="btn btn-warning" onclick="runDemo('jvm-bytecode')">字节码追踪</a>
                <a href="#" class="btn btn-danger" onclick="runDemo('jvm-reflection')">反射调用链</a>
                <a href="#" class="btn btn-success" onclick="runDemo('jvm-performance')">性能分析</a>
                <a href="/api/demo/visualization/jvm" class="btn btn-secondary" target="_blank">查看可视化</a>
            </div>
            
            <div class="demo-card">
                <h3>🔧 Web组件追踪演示</h3>
                <p>全面演示Servlet、Filter、Listener三大Web组件的反序列化过程，包括安全检测和风险评估。</p>
                <a href="#" class="btn" onclick="runDemo('components')">运行组件追踪</a>
                <a href="#" class="btn" onclick="runDemo('servlet')">Servlet</a>
                <a href="#" class="btn" onclick="runDemo('filter')">Filter</a>
                <a href="#" class="btn" onclick="runDemo('listener')">Listener</a>
            </div>
            
            <div class="demo-card">
                <h3>🛡️ 防护机制演示</h3>
                <p>演示各种反序列化防护措施，包括黑白名单、自定义ObjectInputStream和框架级防护。</p>
                <a href="#" class="btn btn-success" onclick="alert('该功能正在开发中')">防护演示</a>
                <a href="#" class="btn" onclick="alert('该功能正在开发中')">绕过测试</a>
            </div>
            
            <div class="demo-card">
                <h3>💻 内存马检测与分析</h3>
                <p>全面的内存马检测可视化平台，展示Filter、Servlet、Listener型内存马的检测方法和防护策略。</p>
                <a href="#" class="btn btn-danger" onclick="testMemshell('servlet')">🌐 Servlet内存马测试</a>
                <a href="#" class="btn btn-danger" onclick="testMemshell('filter')">🔄 Filter内存马测试</a>
                <a href="#" class="btn btn-danger" onclick="testMemshell('listener')">👂 Listener内存马测试</a>
                <a href="/tomcat-memshell-scanner.jsp" class="btn" target="_blank">传统扫描器</a>
            </div>
        </div>
        
        <div class="features">
            <h2>🎓 教育演示说明</h2>
            <div class="feature-list">
                <div class="feature-item">
                    <h4>🟢 基础安全演示</h4>
                    <p>展示标准Java对象的序列化/反序列化过程，包括对象创建、字段恢复、方法调用等各个步骤的详细追踪。</p>
                </div>
                <div class="feature-item">
                    <h4>🟡 危险操作演示</h4>
                    <p>演示自定义readObject方法如何在反序列化过程中执行任意代码，帮助理解RCE攻击的原理。</p>
                </div>
                <div class="feature-item">
                    <h4>🔴 Gadget链攻击</h4>
                    <p>展示完整的Gadget Chain攻击过程，说明如何通过链式调用最终实现代码执行。</p>
                </div>
                <div class="feature-item">
                    <h4>🌱 Spring框架深度解析</h4>
                    <p>全面展示Spring环境下的序列化机制，包括Bean生命周期、AOP代理对象、Security上下文等高级特性。</p>
                </div>
                <div class="feature-item">
                    <h4>⚙️ JVM层面可视化</h4>
                    <p>深入JVM底层，分析内存使用、字节码执行、反射调用链、性能指标等核心机制。</p>
                </div>
                <div class="feature-item">
                    <h4>📊 多维度分析</h4>
                    <p>每个演示都提供多标签页展示：总览、教育步骤、执行追踪、安全分析，全方位理解技术细节。</p>
                </div>
                <div class="feature-item">
                    <h4>🛡️ 安全风险评估</h4>
                    <p>自动分析安全级别，提供详细的风险警告和防护建议，帮助开发者识别潜在威胁。</p>
                </div>
                <div class="feature-item">
                    <h4>🎯 实战场景模拟</h4>
                    <p>结合真实开发场景，从基础到高级逐步深入，适合不同水平的开发者学习使用。</p>
                </div>
            </div>
        </div>
        
        <div id="result" style="margin-top: 30px; padding: 20px; background: #f8f9fa; border-radius: 8px; display: none;">
            <h3>演示结果</h3>
            <div id="resultTabs" style="margin-bottom: 15px; display: none;">
                <button class="result-tab active" onclick="showResultTab('summary')">📊 总览</button>
                <button class="result-tab" onclick="showResultTab('steps')">📝 教育步骤</button>
                <button class="result-tab" onclick="showResultTab('trace')">🔍 执行追踪</button>
                <button class="result-tab" onclick="showResultTab('security')">🛡️ 安全分析</button>
            </div>
            <div id="resultContent-summary" class="result-content active">
                <pre id="resultSummary" style="background: #2c3e50; color: #ecf0f1; padding: 15px; border-radius: 5px; overflow-x: auto;"></pre>
            </div>
            <div id="resultContent-steps" class="result-content">
                <div id="educationSteps" style="background: #fff; padding: 15px; border-radius: 5px; border-left: 4px solid #3498db;"></div>
            </div>
            <div id="resultContent-trace" class="result-content">
                <pre id="traceDetails" style="background: #2c3e50; color: #ecf0f1; padding: 15px; border-radius: 5px; overflow-x: auto; font-size: 12px;"></pre>
            </div>
            <div id="resultContent-security" class="result-content">
                <div id="securityAnalysis" style="background: #fff; padding: 15px; border-radius: 5px;"></div>
            </div>
        </div>
    </div>
    
    <script>
        async function runDemo(type) {
            const resultDiv = document.getElementById('result');
            const resultTabs = document.getElementById('resultTabs');
            const resultSummary = document.getElementById('resultSummary');
            
            resultDiv.style.display = 'block';
            resultTabs.style.display = 'block';
            resultSummary.textContent = '正在运行演示...';
            
            // 显示总览标签页
            showResultTab('summary');
            
            try {
                const response = await fetch(`/api/demo/trace/${type}`);
                const result = await response.json();
                
                // 更新总览
                resultSummary.textContent = JSON.stringify(result, null, 2);
                
                // 更新教育步骤
                updateEducationSteps(result);
                
                // 更新执行追踪
                updateTraceDetails(result);
                
                // 更新安全分析
                updateSecurityAnalysis(result);
                
            } catch (error) {
                resultSummary.textContent = '演示运行失败: ' + error.message;
                resultTabs.style.display = 'none';
            }
        }
        
        function updateEducationSteps(result) {
            const stepsDiv = document.getElementById('educationSteps');
            
            if (result.educationLog) {
                const steps = result.educationLog.split('\\n').filter(step => step.trim());
                let html = '<h4>📚 教育步骤详解</h4>';
                
                steps.forEach((step, index) => {
                    if (step.trim()) {
                        let stepClass = 'education-step';
                        if (step.includes('[危险]') || step.includes('[警告]')) {
                            stepClass += ' security-warning';
                        } else if (step.includes('[成功]') || step.includes('[完成]')) {
                            stepClass += ' security-safe';
                        }
                        html += `<div class="${stepClass}"><strong>步骤 ${index + 1}:</strong> ${step}</div>`;
                    }
                });
                
                if (result.educationalPoints) {
                    html += '<h5>🎯 关键教育要点:</h5><ul>';
                    result.educationalPoints.forEach(point => {
                        html += `<li>${point}</li>`;
                    });
                    html += '</ul>';
                }
                
                stepsDiv.innerHTML = html;
            } else {
                stepsDiv.innerHTML = '<p>此演示类型暂不支持详细教育步骤展示</p>';
            }
        }
        
        function updateTraceDetails(result) {
            const traceDiv = document.getElementById('traceDetails');
            
            if (result.fullTrace) {
                traceDiv.textContent = result.fullTrace;
            } else {
                traceDiv.textContent = '暂无详细追踪信息';
            }
        }
        
        function updateSecurityAnalysis(result) {
            const securityDiv = document.getElementById('securityAnalysis');
            
            let html = '<h4>🛡️ 安全分析报告</h4>';
            
            // 安全级别
            if (result.securityLevel) {
                let levelClass = 'security-safe';
                let levelText = '安全';
                let levelIcon = '🟢';
                
                switch (result.securityLevel) {
                    case 'HIGH_RISK':
                    case 'CRITICAL':
                        levelClass = 'security-warning';
                        levelText = '高风险';
                        levelIcon = '🔴';
                        break;
                    case 'MEDIUM_RISK':
                        levelClass = 'security-medium';
                        levelText = '中等风险';
                        levelIcon = '🟡';
                        break;
                }
                
                html += `<div class="${levelClass}">
                    <strong>${levelIcon} 安全级别: ${levelText}</strong>
                    <p>类型: ${result.type || '未知'} | 场景: ${result.scenario || 'basic'}</p>
                </div>`;
            }
            
            // 类型特定的分析
            if (result.type === 'spring') {
                html += generateSpringSecurityAnalysis(result);
            } else if (result.type === 'jvm') {
                html += generateJVMSecurityAnalysis(result);
            }
            
            // 安全警告
            if (result.securityWarnings && result.securityWarnings.length > 0) {
                html += '<h5>⚠️ 安全警告:</h5><ul>';
                result.securityWarnings.forEach(warning => {
                    html += `<li class="text-danger">${warning}</li>`;
                });
                html += '</ul>';
            }
            
            // 攻击向量
            if (result.attackVector) {
                html += `<div class="security-warning">
                    <strong>🎯 攻击向量:</strong> ${result.attackVector}
                </div>`;
            }
            
            // 统计信息
            if (result.statistics || result.analysisTime || result.executionSteps) {
                html += '<h5>📊 统计信息:</h5>';
                html += '<div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 10px;">';
                
                if (result.educationalSteps) {
                    html += `<div class="stat-item">
                        <div class="stat-value">${result.educationalSteps}</div>
                        <div class="stat-label">教育步骤</div>
                    </div>`;
                }
                
                if (result.executionSteps || result.traceSteps) {
                    html += `<div class="stat-item">
                        <div class="stat-value">${result.executionSteps || result.traceSteps}</div>
                        <div class="stat-label">执行步骤</div>
                    </div>`;
                }
                
                if (result.analysisTime || result.processingTime) {
                    html += `<div class="stat-item">
                        <div class="stat-value">${result.analysisTime || result.processingTime}ms</div>
                        <div class="stat-label">处理时间</div>
                    </div>`;
                }
                
                if (result.beanCount) {
                    html += `<div class="stat-item">
                        <div class="stat-value">${result.beanCount}</div>
                        <div class="stat-label">Bean数量</div>
                    </div>`;
                }
                
                if (result.executionFrames) {
                    html += `<div class="stat-item">
                        <div class="stat-value">${result.executionFrames}</div>
                        <div class="stat-label">执行帧数</div>
                    </div>`;
                }
                
                html += '</div>';
            }
            
            // 漏洞日志
            if (result.vulnerabilityLog) {
                html += '<h5>🚨 漏洞执行日志:</h5>';
                html += `<pre style="background: #2c3e50; color: #ecf0f1; padding: 10px; border-radius: 5px; font-size: 12px;">${result.vulnerabilityLog}</pre>`;
            }
            
            // Gadget链日志
            if (result.gadgetChainLog) {
                html += '<h5>🔗 Gadget链执行日志:</h5>';
                html += `<pre style="background: #2c3e50; color: #ecf0f1; padding: 10px; border-radius: 5px; font-size: 12px;">${result.gadgetChainLog}</pre>`;
            }
            
            securityDiv.innerHTML = html;
        }
        
        function generateSpringSecurityAnalysis(result) {
            let html = '<h5>🌱 Spring框架分析:</h5>';
            
            if (result.springContextActive) {
                html += `<div class="security-safe">
                    <strong>✅ Spring上下文状态:</strong> ${result.springContextActive ? '已激活' : '未激活'}
                </div>`;
            }
            
            if (result.beanCount) {
                html += `<p><strong>Bean管理:</strong> 共创建 ${result.beanCount} 个Bean实例</p>`;
            }
            
            return html;
        }
        
        function generateJVMSecurityAnalysis(result) {
            let html = '<h5>⚙️ JVM层面分析:</h5>';
            
            if (result.memoryUsage) {
                html += '<div class="security-safe">';
                html += '<strong>💾 内存使用情况:</strong>';
                html += `<pre style="background: #f8f9fa; padding: 10px; margin: 5px 0; font-size: 12px;">${JSON.stringify(result.memoryUsage, null, 2)}</pre>`;
                html += '</div>';
            }
            
            if (result.performanceMetrics) {
                html += '<div class="security-medium">';
                html += '<strong>📈 性能指标:</strong>';
                html += `<pre style="background: #f8f9fa; padding: 10px; margin: 5px 0; font-size: 12px;">${JSON.stringify(result.performanceMetrics, null, 2)}</pre>`;
                html += '</div>';
            }
            
            return html;
        }
        
        function showResultTab(tabName) {
            // 隐藏所有内容
            document.querySelectorAll('.result-content').forEach(content => {
                content.classList.remove('active');
            });
            
            // 移除所有标签页的活跃状态
            document.querySelectorAll('.result-tab').forEach(tab => {
                tab.classList.remove('active');
            });
            
            // 显示选中的内容
            const targetContent = document.getElementById(`resultContent-${tabName}`);
            if (targetContent) {
                targetContent.classList.add('active');
            }
            
            // 激活选中的标签页
            event.target.classList.add('active');
        }
        
        function showTrace(type) {
            runDemo(type);
        }
        
        async function testMemshell(type) {
            const resultDiv = document.getElementById('result');
            const resultSummary = document.getElementById('resultSummary');
            
            // 直接打开对应类型的内存马页面
            let shellUrl;
            switch(type) {
                case 'servlet':
                    shellUrl = '/memshell/servlet/';
                    break;
                case 'filter':
                    shellUrl = '/?filterCmd=whoami';
                    break;
                case 'listener':
                    shellUrl = '/api/demo/listener-shell?action=info';
                    break;
                default:
                    shellUrl = null;
            }
            
            // 立即打开新窗口
            if (shellUrl) {
                window.open(shellUrl, '_blank');
            }
            
            // 显示结果区域
            if (resultDiv && resultSummary) {
                resultDiv.style.display = 'block';
                resultSummary.textContent = `正在测试 ${type} 内存马...`;
                
                try {
                    const response = await fetch(`/api/demo/trace/${type}`);
                    const result = await response.json();
                    
                    let displayText = `${type.toUpperCase()} 内存马测试结果:\\n\\n`;
                    displayText += `状态: ${result.status}\\n`;
                    if (result.status === 'success') {
                        displayText += `执行步骤: ${result.executionSteps}\\n`;
                        displayText += `警告数量: ${result.warningCount}\\n`;
                        displayText += `错误数量: ${result.errorCount}\\n`;
                        displayText += `安全评分: ${result.securityScore}\\n`;
                        displayText += `风险级别: ${result.riskLevel}\\n`;
                        displayText += `建议: ${result.recommendation}\\n`;
                    } else {
                        displayText += `错误信息: ${result.message}\\n`;
                    }
                    
                    if (shellUrl) {
                        displayText += `\\n✅ 已在新窗口打开 ${type} 内存马页面`;
                    }
                    
                    resultSummary.textContent = displayText;
                } catch (error) {
                    let errorText = `${type} 内存马测试失败: ` + error.message;
                    if (shellUrl) {
                        errorText += `\\n\\n✅ 但已成功在新窗口打开 ${type} 内存马页面`;
                    }
                    resultSummary.textContent = errorText;
                }
            } else {
                // 如果找不到结果显示元素，至少确保页面能正常跳转
                if (shellUrl) {
                    alert(`已在新窗口打开 ${type} 内存马页面`);
                } else {
                    alert(`无法打开 ${type} 内存马页面：URL配置错误`);
                }
            }
        }
    </script>
</body>
</html>
        """;
    }
    
    private String generateSpringVisualizationHTML() {
        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Spring框架反序列化可视化</title>
    <style>
        body {
            font-family: 'Microsoft YaHei', 'SimSun', Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #333;
            line-height: 1.6;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 10px;
            padding: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
        }
        h1 {
            color: #2c3e50;
            text-align: center;
            margin-bottom: 30px;
            font-size: 2.5em;
        }
        .feature-card {
            background: #f8f9fa;
            border-radius: 8px;
            padding: 20px;
            margin: 20px 0;
            border-left: 5px solid #27ae60;
        }
        .status {
            background: #fff3cd;
            border: 1px solid #ffeeba;
            color: #856404;
            padding: 15px;
            border-radius: 5px;
            text-align: center;
            font-weight: bold;
        }
        .back-link {
            display: inline-block;
            margin-top: 20px;
            padding: 10px 20px;
            background: #3498db;
            color: white;
            text-decoration: none;
            border-radius: 5px;
        }
        .back-link:hover {
            background: #2980b9;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🌱 Spring框架反序列化可视化</h1>
        
        <div class="status">
            📋 Spring可视化功能正在开发中，敬请期待！
        </div>
        
        <div class="feature-card">
            <h3>🔍 计划功能</h3>
            <ul>
                <li>Spring Bean生命周期可视化</li>
                <li>AOP代理对象创建过程追踪</li>
                <li>依赖注入流程分析</li>
                <li>自定义序列化逻辑展示</li>
                <li>Spring上下文初始化过程</li>
            </ul>
        </div>
        
        <div class="feature-card">
            <h3>🎯 技术要点</h3>
            <ul>
                <li>Bean实例化与属性注入过程</li>
                <li>BeanPostProcessor执行时机</li>
                <li>循环依赖解决机制</li>
                <li>代理对象的序列化处理</li>
                <li>ApplicationContext生命周期</li>
            </ul>
        </div>
        
        <a href="/api/demo/" class="back-link">← 返回演示主页</a>
    </div>
</body>
</html>
        """;
    }
    
    private String generateJVMVisualizationHTML() {
        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>JVM执行流程可视化</title>
    <style>
        body {
            font-family: 'Microsoft YaHei', 'SimSun', Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #333;
            line-height: 1.6;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 10px;
            padding: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
        }
        h1 {
            color: #2c3e50;
            text-align: center;
            margin-bottom: 30px;
            font-size: 2.5em;
        }
        .feature-card {
            background: #f8f9fa;
            border-radius: 8px;
            padding: 20px;
            margin: 20px 0;
            border-left: 5px solid #e74c3c;
        }
        .status {
            background: #fff3cd;
            border: 1px solid #ffeeba;
            color: #856404;
            padding: 15px;
            border-radius: 5px;
            text-align: center;
            font-weight: bold;
        }
        .back-link {
            display: inline-block;
            margin-top: 20px;
            padding: 10px 20px;
            background: #3498db;
            color: white;
            text-decoration: none;
            border-radius: 5px;
        }
        .back-link:hover {
            background: #2980b9;
        }
        .highlight {
            background: #e8f5e8;
            padding: 10px;
            border-radius: 5px;
            margin: 10px 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>⚙️ JVM执行流程可视化</h1>
        
        <div class="status">
            🔧 JVM可视化功能正在开发中，敬请期待！
        </div>
        
        <div class="feature-card">
            <h3>🎯 核心功能</h3>
            <ul>
                <li>字节码执行过程可视化</li>
                <li>对象创建与内存分配追踪</li>
                <li>方法调用栈分析</li>
                <li>反射操作详细展示</li>
                <li>类加载过程监控</li>
            </ul>
        </div>
        
        <div class="feature-card">
            <h3>🔍 技术细节</h3>
            <ul>
                <li>堆内存对象分布图</li>
                <li>方法区类信息展示</li>
                <li>虚拟机栈帧结构分析</li>
                <li>垃圾回收过程追踪</li>
                <li>JIT编译优化展示</li>
            </ul>
        </div>
        
        <div class="highlight">
            <h4>💡 即将支持的可视化内容：</h4>
            <p>• 反序列化过程中的对象创建时序图</p>
            <p>• 方法调用链的深度分析</p>
            <p>• 内存使用情况的实时监控</p>
            <p>• 安全检查点的执行状态</p>
        </div>
        
        <a href="/api/demo/" class="back-link">← 返回演示主页</a>
    </div>
</body>
</html>
        """;
    }
    
    private String generateMemshellVisualizationHTML() {
        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>内存马攻防对抗可视化平台</title>
    <style>
        body {
            font-family: 'Microsoft YaHei', 'SimSun', Arial, sans-serif;
            margin: 0;
            padding: 0;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #333;
            line-height: 1.6;
        }
        
        .header {
            background: rgba(255, 255, 255, 0.95);
            padding: 20px;
            text-align: center;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        
        .header h1 {
            margin: 0;
            color: #2c3e50;
            font-size: 28px;
        }
        
        .header .subtitle {
            color: #7f8c8d;
            font-size: 14px;
            margin-top: 5px;
        }
        
        .container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 0 20px;
        }
        
        /* 顶部控制栏 */
        .top-controls {
            background: rgba(255, 255, 255, 0.95);
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 15px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        
        .type-tabs {
            display: flex;
            gap: 10px;
        }
        
        .tab-btn {
            padding: 8px 16px;
            border: none;
            border-radius: 20px;
            background: #ecf0f1;
            color: #2c3e50;
            cursor: pointer;
            font-size: 14px;
            transition: all 0.3s;
        }
        
        .tab-btn.active {
            background: #3498db;
            color: white;
        }
        
        .tab-btn:hover {
            background: #3498db;
            color: white;
        }
        
        .global-actions {
            display: flex;
            gap: 10px;
        }
        
        /* 主要三栏布局 */
        .main-layout {
            display: grid;
            grid-template-columns: 350px 1fr 320px;
            gap: 20px;
            min-height: 600px;
        }
        
        @media (max-width: 1200px) {
            .main-layout {
                grid-template-columns: 1fr;
                gap: 15px;
            }
        }
        
        .panel {
            background: rgba(255, 255, 255, 0.95);
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            height: fit-content;
        }
        
        .panel-header {
            font-size: 18px;
            font-weight: bold;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid #ecf0f1;
        }
        
        /* 左栏 - 注入控制 */
        .inject-panel {
            border-left: 4px solid #e74c3c;
        }
        
        .inject-panel .panel-header {
            color: #e74c3c;
        }
        
        /* 中栏 - 状态展示 */
        .status-panel {
            border-left: 4px solid #f39c12;
        }
        
        .status-panel .panel-header {
            color: #f39c12;
        }
        
        /* 右栏 - 检测防护 */
        .detect-panel {
            border-left: 4px solid #27ae60;
        }
        
        .detect-panel .panel-header {
            color: #27ae60;
        }
        
        /* 内存马类型卡片 */
        .shell-type-card {
            border: 2px solid #ecf0f1;
            border-radius: 8px;
            padding: 15px;
            margin: 15px 0;
            transition: all 0.3s;
            background: #fafafa;
        }
        
        .shell-type-card:hover {
            border-color: #3498db;
            box-shadow: 0 2px 8px rgba(52, 152, 219, 0.2);
        }
        
        .shell-type-card.active {
            border-color: #27ae60;
            background: #ecf8f5;
        }
        
        .shell-type-card.error {
            border-color: #e74c3c;
            background: #fdf2f2;
        }
        
        .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
        }
        
        .card-icon {
            font-size: 24px;
            margin-right: 10px;
        }
        
        .card-title {
            font-size: 16px;
            font-weight: bold;
            color: #2c3e50;
            display: flex;
            align-items: center;
        }
        
        .card-status {
            font-size: 12px;
            padding: 2px 8px;
            border-radius: 10px;
            font-weight: bold;
        }
        
        .status-active {
            background: #d5f4e6;
            color: #27ae60;
        }
        
        .status-inactive {
            background: #fadbd8;
            color: #e74c3c;
        }
        
        .status-unknown {
            background: #fdf2e9;
            color: #f39c12;
        }
        
        .card-info {
            font-size: 13px;
            color: #7f8c8d;
            margin: 8px 0;
            line-height: 1.4;
        }
        
        .card-actions {
            display: flex;
            gap: 8px;
            margin-top: 12px;
            flex-wrap: wrap;
        }
        
        /* 按钮样式 */
        .btn {
            padding: 8px 16px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 13px;
            font-weight: bold;
            transition: all 0.3s;
            text-decoration: none;
            display: inline-block;
            text-align: center;
        }
        
        .btn:hover {
            transform: translateY(-1px);
            box-shadow: 0 2px 5px rgba(0,0,0,0.2);
        }
        
        .btn-inject {
            background: #e74c3c;
            color: white;
        }
        
        .btn-inject:hover {
            background: #c0392b;
        }
        
        .btn-remove {
            background: #f39c12;
            color: white;
        }
        
        .btn-remove:hover {
            background: #d68910;
        }
        
        .btn-detect {
            background: #27ae60;
            color: white;
        }
        
        .btn-detect:hover {
            background: #219a52;
        }
        
        .btn-info {
            background: #3498db;
            color: white;
        }
        
        .btn-info:hover {
            background: #2980b9;
        }
        
        .btn-secondary {
            background: #95a5a6;
            color: white;
        }
        
        .btn-secondary:hover {
            background: #7f8c8d;
        }
        
        /* 统计面板 */
        .stats-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
            margin: 20px 0;
        }
        
        .stat-item {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            text-align: center;
            border: 2px solid #e9ecef;
        }
        
        .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: #2c3e50;
            margin-bottom: 5px;
        }
        
        .stat-label {
            font-size: 12px;
            color: #7f8c8d;
            text-transform: uppercase;
        }
        
        /* 内存马列表 */
        .shell-list {
            max-height: 400px;
            overflow-y: auto;
        }
        
        .shell-item {
            background: #f8f9fa;
            border: 1px solid #e9ecef;
            border-radius: 5px;
            padding: 12px;
            margin: 10px 0;
            transition: all 0.3s;
        }
        
        .shell-item:hover {
            border-color: #3498db;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        
        .shell-item-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
        }
        
        .shell-name {
            font-weight: bold;
            color: #2c3e50;
        }
        
        .shell-type {
            font-size: 12px;
            padding: 2px 6px;
            border-radius: 10px;
            background: #3498db;
            color: white;
        }
        
        .shell-details {
            font-size: 12px;
            color: #7f8c8d;
            line-height: 1.4;
        }
        
        /* 结果输出区域 */
        .result-area {
            background: #2c3e50;
            color: #ecf0f1;
            padding: 15px;
            border-radius: 5px;
            font-family: 'Courier New', monospace;
            font-size: 13px;
            min-height: 200px;
            max-height: 300px;
            overflow-y: auto;
            margin: 15px 0;
            white-space: pre-wrap;
            word-wrap: break-word;
        }
        
        /* 加载动画 */
        .loading {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            color: #7f8c8d;
            font-style: italic;
        }
        
        .loading::before {
            content: "⏳";
            margin-right: 8px;
            animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
        }
        
        /* 警告提示 */
        .warning {
            background: #fff3cd;
            color: #856404;
            padding: 12px;
            border-radius: 5px;
            border-left: 4px solid #ffc107;
            margin: 15px 0;
            font-size: 14px;
        }
        
        .warning strong {
            display: block;
            margin-bottom: 5px;
        }
        
        /* 成功提示 */
        .success {
            background: #d1ecf1;
            color: #0c5460;
            padding: 12px;
            border-radius: 5px;
            border-left: 4px solid #bee5eb;
            margin: 15px 0;
            font-size: 14px;
        }
        
        /* 错误提示 */
        .error {
            background: #f8d7da;
            color: #721c24;
            padding: 12px;
            border-radius: 5px;
            border-left: 4px solid #f5c6cb;
            margin: 15px 0;
            font-size: 14px;
        }
        /* 结果输出区域 */
        .result-panel {
            background: #2c3e50;
            color: #ecf0f1;
            padding: 20px;
            border-radius: 8px;
            margin: 20px 0;
            font-family: 'Courier New', monospace;
            font-size: 14px;
            max-height: 400px;
            overflow-y: auto;
            display: none;
        }
        
        .shell-list {
            margin: 15px 0;
        }
        
        .shell-item {
            background: white;
            padding: 15px;
            border-radius: 6px;
            margin: 10px 0;
            border-left: 4px solid #95a5a6;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .shell-item.active {
            border-left-color: #27ae60;
            background: #d5f4e6;
        }
        
        .shell-item.suspicious {
            border-left-color: #e74c3c;
            background: #fadbd8;
        }
        
        .shell-info {
            flex-grow: 1;
        }
        
        .shell-actions {
            display: flex;
            gap: 10px;
        }
        
        .input-group {
            margin: 15px 0;
        }
        
        .input-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
        }
        
        .input-group select, .input-group input {
            width: 100%;
            padding: 10px;
            border: 1px solid #bdc3c7;
            border-radius: 4px;
            font-size: 14px;
        }
        
        .back-link {
            display: inline-block;
            margin-top: 30px;
            padding: 12px 25px;
            background: #3498db;
            color: white;
            text-decoration: none;
            border-radius: 6px;
            font-weight: bold;
        }
        
        .back-link:hover {
            background: #2980b9;
        }
        
        .loading {
            text-align: center;
            padding: 20px;
            color: #7f8c8d;
        }
        
        .stat-item {
            background: #f8f9fa;
            padding: 10px;
            border-radius: 5px;
            text-align: center;
            border: 1px solid #e9ecef;
            margin: 5px;
        }
        
        .stat-value {
            font-size: 20px;
            font-weight: bold;
            color: #2c3e50;
            margin-bottom: 3px;
        }
        
        .stat-label {
            font-size: 11px;
            color: #7f8c8d;
            text-transform: uppercase;
        }
    </style>
</head>
<body>
    <!-- 页面头部 -->
    <div class="header">
        <h1>⚔️ 内存马攻防对抗可视化平台</h1>
        <div class="subtitle">Memory Shell Detection & Analysis Visualization Platform</div>
    </div>
    
    <div class="container">
        <!-- 顶部控制栏 -->
        <div class="top-controls">
            <div class="type-tabs">
                <button class="tab-btn active" onclick="showAllTypes()">全部</button>
                <button class="tab-btn" onclick="showType('servlet')">Servlet</button>
                <button class="tab-btn" onclick="showType('filter')">Filter</button>
                <button class="tab-btn" onclick="showType('listener')">Listener</button>
            </div>
            <div class="global-actions">
                <button class="btn btn-detect" onclick="detectMemoryShells()">🔍 全面扫描</button>
                <button class="btn btn-info" onclick="refreshStatus()">🔄 刷新状态</button>
            </div>
        </div>
        
        <!-- 主要三栏布局 -->
        <div class="main-layout">
            <!-- 左栏 - 攻击面板 -->
            <div class="panel inject-panel">
                <div class="panel-header">🔴 攻击模拟</div>
                
                <div class="shell-type-card">
                    <div class="card-header">
                        <div class="card-title">
                            <span class="card-icon">🌐</span>
                            Servlet内存马
                        </div>
                        <div class="card-status status-inactive" id="servlet-status">未激活</div>
                    </div>
                    <div class="card-info">通过动态注册Servlet实现命令执行后门</div>
                    <div class="card-actions">
                        <button class="btn btn-inject" onclick="window.open('http://localhost:8080/shell?action=info&amp;type=servlet', '_blank')">🔍 Servlet内存马演示</button>
                        <button class="btn btn-remove" onclick="removeShellByType('servlet')">移除</button>
                        <button class="btn btn-info" onclick="getShellInfo('servlet')">详情</button>
                    </div>
                </div>
                
                <div class="shell-type-card">
                    <div class="card-header">
                        <div class="card-title">
                            <span class="card-icon">🔄</span>
                            Filter内存马
                        </div>
                        <div class="card-status status-inactive" id="filter-status">未激活</div>
                    </div>
                    <div class="card-info">通过过滤器链实现请求拦截和命令执行</div>
                    <div class="card-actions">
                        <button class="btn btn-inject" onclick="window.open('http://localhost:8080/shell?action=info&amp;type=filter', '_blank')">🔄 Filter内存马演示</button>
                        <button class="btn btn-remove" onclick="removeShellByType('filter')">移除</button>
                        <button class="btn btn-info" onclick="getShellInfo('filter')">详情</button>
                    </div>
                </div>
                
                <div class="shell-type-card">
                    <div class="card-header">
                        <div class="card-title">
                            <span class="card-icon">👂</span>
                            Listener内存马
                        </div>
                        <div class="card-status status-inactive" id="listener-status">未激活</div>
                    </div>
                    <div class="card-info">通过事件监听器实现持久化后门</div>
                    <div class="card-actions">
                        <button class="btn btn-inject" onclick="window.open('http://localhost:8080/api/demo/listener-shell?action=info', '_blank')">👂 Listener内存马演示</button>
                        <button class="btn btn-remove" onclick="removeShellByType('listener')">移除</button>
                        <button class="btn btn-info" onclick="getShellInfo('listener')">详情</button>
                    </div>
                </div>
            </div>
            
            <!-- 中栏 - 状态展示 -->
            <div class="panel status-panel">
                <div class="panel-header">📊 状态展示</div>
                
                <div class="stats-grid">
                    <div class="stat-item">
                        <div class="stat-value" id="active-shells-count">0</div>
                        <div class="stat-label">活跃内存马</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value" id="total-injections-count">0</div>
                        <div class="stat-label">注入次数</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value" id="detection-status">关闭</div>
                        <div class="stat-label">实时检测</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value" id="suspicious-count">0</div>
                        <div class="stat-label">可疑组件</div>
                    </div>
                </div>
                
                <div class="shell-list" id="active-shells-list">
                    <div class="loading">点击刷新状态加载数据</div>
                </div>
            </div>
            
            <!-- 右栏 - 检测防护 -->
            <div class="panel detect-panel">
                <div class="panel-header">🛡️ 检测防护</div>
                
                <h4>检测控制</h4>
                <button class="btn btn-detect" onclick="detectMemoryShells()">🔍 执行扫描</button>
                <button class="btn btn-detect" onclick="startRealTimeDetection()">▶️ 启动实时检测</button>
                <button class="btn btn-secondary" onclick="stopRealTimeDetection()">⏹️ 停止检测</button>
                
                <h4 style="margin-top: 20px;">内存马管理</h4>
                <button class="btn btn-info" onclick="listActiveShells()">📋 列出活跃Shell</button>
                <button class="btn btn-remove" onclick="removeAllShells()">🗑️ 清除所有</button>
                
                <h4 style="margin-top: 20px;">导出功能</h4>
                <button class="btn btn-info" onclick="exportReport()">💾 导出报告</button>
                <button class="btn btn-info" onclick="showDetectionHistory()">📈 检测历史</button>
            </div>
        </div>
        
        <div class="warning">
            ⚠️ 安全提醒: 本平台仅用于安全教育和防护培训，所有功能均为教学演示用途，请勿用于非法用途！
        </div>
        
        <!-- 结果显示面板 -->
        <div id="result-panel" class="result-panel"></div>
        
        <a href="/api/demo/" class="back-link">← 返回演示主页</a>
    </div>
    
    <script>
        let currentStatus = null;
        
        // 页面加载完成后自动刷新状态
        window.addEventListener('load', function() {
            refreshStatus();
        });
        
        // 标签页切换功能
        function showAllTypes() {
            updateTabActive('全部');
            document.querySelectorAll('.shell-type-card').forEach(card => {
                card.style.display = 'block';
            });
        }
        
        function showType(type) {
            updateTabActive(type);
            document.querySelectorAll('.shell-type-card').forEach(card => {
                if (card.id === type + '-card') {
                    card.style.display = 'block';
                } else {
                    card.style.display = 'none';
                }
            });
        }
        
        function updateTabActive(activeType) {
            document.querySelectorAll('.tab-btn').forEach(btn => {
                btn.classList.remove('active');
                if (btn.textContent.includes(activeType)) {
                    btn.classList.add('active');
                }
            });
        
        async function refreshStatus() {
            try {
                const response = await fetch('/api/demo/memshell/status');
                currentStatus = await response.json();
                
                if (currentStatus.success) {
                    updateStatusDisplay();
                } else {
                    showResult('获取状态失败: ' + (currentStatus.error || '未知错误'));
                }
            } catch (error) {
                showResult('刷新状态失败: ' + error.message);
            }
        }
        
        function updateStatusDisplay() {
            if (!currentStatus) return;
            
            document.getElementById('active-shells-count').textContent = currentStatus.activeShells.length;
            document.getElementById('total-injections-count').textContent = currentStatus.injectorStatistics.totalInjections;
            document.getElementById('detection-status').textContent = 
                currentStatus.detectorStatistics.realTimeDetectionEnabled ? '已启动' : '关闭';
            document.getElementById('suspicious-count').textContent = currentStatus.detectorStatistics.suspiciousComponents;
            
            // 更新状态指示器
            updateShellStatusIndicators();
        }
        
        function updateShellStatusIndicators() {
            const activeShells = currentStatus.activeShells || [];
            
            // 重置所有状态
            ['servlet', 'filter', 'listener'].forEach(type => {
                const statusEl = document.getElementById(type + '-status');
                if (statusEl) {
                    statusEl.textContent = '未激活';
                    statusEl.className = 'card-status status-inactive';
                }
            });
            
            // 更新活跃状态
            activeShells.forEach(shell => {
                const type = shell.type.toLowerCase();
                const statusEl = document.getElementById(type + '-status');
                if (statusEl) {
                    statusEl.textContent = '已激活';
                    statusEl.className = 'card-status status-active';
                }
            });
        }
        
        async function injectSpecificShell(type) {
            try {
                showResult(`正在注入 ${type} 内存马...`);
                
                const response = await fetch(`/api/demo/memshell/inject/${type}`, {
                    method: 'POST'
                });
                const result = await response.json();
                
                showResult(`${type} 内存马注入结果:\\n${JSON.stringify(result, null, 2)}`);
                
                if (result.success) {
                    setTimeout(refreshStatus, 1000);
                }
            } catch (error) {
                showResult(`注入 ${type} 内存马失败: ` + error.message);
            }
        }
        
        async function removeShellByType(type) {
            if (!currentStatus || currentStatus.activeShells.length === 0) {
                showResult('当前没有活跃的内存马');
                return;
            }
            
            const shellsOfType = currentStatus.activeShells.filter(shell => 
                shell.type.toLowerCase() === type.toLowerCase()
            );
            
            if (shellsOfType.length === 0) {
                showResult(`没有找到 ${type} 类型的内存马`);
                return;
            }
            
            for (const shell of shellsOfType) {
                try {
                    const response = await fetch(`/api/demo/memshell/remove/${shell.id}`, {
                        method: 'DELETE'
                    });
                    const result = await response.json();
                    if (result.success) {
                        showResult(`已移除 ${type} 内存马: ${shell.name}`);
                    }
                } catch (error) {
                    showResult(`移除 ${type} 内存马失败: ` + error.message);
                }
            }
            
            setTimeout(refreshStatus, 1000);
        }
        
        async function getShellInfo(type) {
            if (!currentStatus || currentStatus.activeShells.length === 0) {
                showResult('当前没有活跃的内存马');
                return;
            }
            
            const shellsOfType = currentStatus.activeShells.filter(shell => 
                shell.type.toLowerCase() === type.toLowerCase()
            );
            
            if (shellsOfType.length === 0) {
                showResult(`没有找到 ${type} 类型的内存马`);
                return;
            }
            
            let info = `${type.toUpperCase()} 内存马信息:\\n\\n`;
            shellsOfType.forEach((shell, index) => {
                info += `${index + 1}. ${shell.name}\\n`;
                info += `   ID: ${shell.id}\\n`;
                info += `   类名: ${shell.className}\\n`;
                info += `   状态: ${shell.active ? '活跃' : '非活跃'}\\n`;
                info += `   注入时间: ${new Date(shell.injectionTime).toLocaleString()}\\n`;
                info += `   访问次数: ${shell.accessCount}\\n\\n`;
            });
            
            showResult(info);
        }
        
        async function detectMemoryShells() {
            showResult('正在执行全面扫描，请稍候...');
            
            try {
                const response = await fetch('/api/demo/memshell/detect');
                const result = await response.json();
                
                showResult(`检测结果:\\n扫描时间: ${new Date(result.scanTime).toLocaleString()}\\n总组件数: ${result.totalComponents}\\n可疑组件数: ${result.suspiciousComponents}\\n\\n详细结果:\\n${JSON.stringify(result.results, null, 2)}`);
            } catch (error) {
                showResult('检测失败: ' + error.message);
            }
        }
        
        async function startRealTimeDetection() {
            try {
                const response = await fetch('/api/demo/memshell/detection/start', {
                    method: 'POST'
                });
                const result = await response.json();
                
                showResult(`实时检测启动结果:\\n${JSON.stringify(result, null, 2)}`);
                setTimeout(refreshStatus, 1000);
            } catch (error) {
                showResult('启动实时检测失败: ' + error.message);
            }
        }
        
        async function stopRealTimeDetection() {
            try {
                const response = await fetch('/api/demo/memshell/detection/stop', {
                    method: 'POST'
                });
                const result = await response.json();
                
                showResult(`实时检测停止结果:\\n${JSON.stringify(result, null, 2)}`);
                setTimeout(refreshStatus, 1000);
            } catch (error) {
                showResult('停止实时检测失败: ' + error.message);
            }
        }
        
        function listActiveShells() {
            if (currentStatus && currentStatus.activeShells.length > 0) {
                let shellInfo = '活跃内存马列表:\\n\\n';
                currentStatus.activeShells.forEach((shell, index) => {
                    shellInfo += `${index + 1}. ${shell.name} (${shell.type})\\n`;
                    shellInfo += `   ID: ${shell.id}\\n`;
                    shellInfo += `   类名: ${shell.className}\\n`;
                    shellInfo += `   状态: ${shell.active ? '活跃' : '非活跃'}\\n`;
                    shellInfo += `   访问次数: ${shell.accessCount}\\n\\n`;
                });
                showResult(shellInfo);
            } else {
                showResult('当前没有活跃的内存马');
            }
        }
        
        async function removeAllShells() {
            if (!confirm('确定要移除所有内存马吗？这个操作不可恢复！')) {
                return;
            }
            
            if (!currentStatus || currentStatus.activeShells.length === 0) {
                showResult('当前没有活跃的内存马');
                return;
            }
            
            let removedCount = 0;
            for (const shell of currentStatus.activeShells) {
                try {
                    const response = await fetch(`/api/demo/memshell/remove/${shell.id}`, {
                        method: 'DELETE'
                    });
                    const result = await response.json();
                    if (result.success) {
                        removedCount++;
                    }
                } catch (error) {
                    console.error('移除失败:', error);
                }
            }
            
            showResult(`批量移除完成，共移除 ${removedCount} 个内存马`);
            setTimeout(refreshStatus, 1000);
        }
        
        function showDetectionHistory() {
            showResult('检测历史功能开发中，敬请期待...');
        }
        
        function exportReport() {
            if (!currentStatus) {
                showResult('请先刷新状态获取数据');
                return;
            }
            
            const report = {
                timestamp: new Date().toISOString(),
                status: currentStatus,
                exportedBy: 'MemoryShell Visualization Platform'
            };
            
            const blob = new Blob([JSON.stringify(report, null, 2)], {type: 'application/json'});
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'memshell-report-' + new Date().getTime() + '.json';
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);
            
            showResult('检测报告已导出');
        }
        
        function showResult(message) {
            const panel = document.getElementById('result-panel');
            panel.textContent = message;
            panel.style.display = 'block';
            panel.scrollIntoView({behavior: 'smooth'});
        }
        
        // 每30秒自动刷新状态
        setInterval(() => {
            if (document.visibilityState === 'visible') {
                refreshStatus();
            }
        }, 30000);
    </script>
</body>
</html>
        """;
    }
    
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
    
    // 简单的演示对象
    public static class DemoObject implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String name;
        private int value;
        
        public DemoObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        @Override
        public String toString() {
            return "DemoObject{name='" + name + "', value=" + value + "}";
        }
        
        // 自定义反序列化方法用于演示
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            System.out.println("DemoObject.readObject() 被调用");
            stream.defaultReadObject();
            System.out.println("DemoObject 反序列化完成: " + this);
        }
        
        // Getters
        public String getName() { return name; }
        public int getValue() { return value; }
    }
    
    /**
     * 教育用漏洞演示类 - 展示危险的readObject方法
     */
    public static class VulnerableDemo implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String command;
        private String description;
        private transient StringBuilder executionLog;
        
        public VulnerableDemo() {
            this.executionLog = new StringBuilder();
        }
        
        public VulnerableDemo(String command, String description) {
            this();
            this.command = command;
            this.description = description;
        }
        
        // 危险的自定义反序列化方法
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            logStep("🎓 [教育] 进入VulnerableDemo.readObject()方法");
            logStep("📖 [教育] 这是一个不安全的反序列化实现示例");
            
            // 恢复对象字段
            in.defaultReadObject();
            logStep("✅ [步骤] 字段恢复完成 - command: " + command);
            
            // 重新初始化transient字段
            this.executionLog = new StringBuilder();
            
            // 模拟危险操作：基于反序列化数据执行命令
            if (command != null && !command.trim().isEmpty()) {
                logStep("⚠️  [危险] 检测到命令参数，准备执行: " + command);
                simulateCommandExecution(command);
                logStep("🚨 [警告] 反序列化过程中执行了外部命令！");
            }
            
            logStep("🔚 [教育] VulnerableDemo.readObject()执行完成");
        }
        
        private void simulateCommandExecution(String cmd) {
            // 教育演示：显示本应执行的命令但不真正执行危险操作
            String simulatedResult = "";
            
            switch (cmd.toLowerCase()) {
                case "whoami":
                    simulatedResult = "administrator";
                    break;
                case "pwd":
                    simulatedResult = "/home/user";
                    break;
                case "id":
                    simulatedResult = "uid=0(root) gid=0(root)";
                    break;
                case "ls":
                    simulatedResult = "file1.txt file2.txt directory/";
                    break;
                default:
                    simulatedResult = "命令执行模拟结果";
            }
            
            logStep("💻 [模拟执行] " + cmd + " -> " + simulatedResult);
            logStep("💡 [教育提示] 在真实攻击中会调用: Runtime.getRuntime().exec(\"" + cmd + "\")");
            logStep("🛡️  [防护] 此演示仅模拟，未执行真实命令");
        }
        
        private void logStep(String step) {
            if (executionLog == null) {
                executionLog = new StringBuilder();
            }
            executionLog.append(step).append("\n");
            System.out.println(step);
        }
        
        public String getExecutionLog() {
            return executionLog != null ? executionLog.toString() : "";
        }
        
        // Getters and Setters
        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        @Override
        public String toString() {
            return "VulnerableDemo{command='" + command + "', description='" + description + "'}";
        }
    }
    
    /**
     * Gadget Chain演示类
     */
    public static class GadgetChainDemo implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String targetMethod;
        private Object[] parameters;
        private transient StringBuilder chainLog;
        
        public GadgetChainDemo(String targetMethod, Object... parameters) {
            this.targetMethod = targetMethod;
            this.parameters = parameters;
            this.chainLog = new StringBuilder();
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            logChainStep("🔗 [Gadget Chain] 开始执行攻击链");
            
            in.defaultReadObject();
            this.chainLog = new StringBuilder();
            
            logChainStep("📋 [步骤1] 恢复目标方法: " + targetMethod);
            logChainStep("📋 [步骤2] 恢复参数数组: " + java.util.Arrays.toString(parameters));
            
            // 模拟Gadget Chain执行
            if ("runtime.exec".equals(targetMethod)) {
                logChainStep("🎯 [步骤3] 检测到Runtime.exec调用链");
                logChainStep("⚙️  [步骤4] 构建调用链: Transformer -> InvokerTransformer -> exec");
                simulateGadgetExecution();
            }
            
            logChainStep("✅ [完成] Gadget Chain执行完成");
        }
        
        private void simulateGadgetExecution() {
            logChainStep("🔧 [模拟] InvokerTransformer.transform()");
            logChainStep("🔧 [模拟] Method.invoke() -> Runtime.getRuntime()");
            logChainStep("🔧 [模拟] Method.invoke() -> exec(" + parameters[0] + ")");
            logChainStep("💡 [教育] 这展示了Commons Collections的经典攻击链");
        }
        
        private void logChainStep(String step) {
            if (chainLog == null) {
                chainLog = new StringBuilder();
            }
            chainLog.append(step).append("\n");
            System.out.println(step);
        }
        
        public String getChainLog() {
            return chainLog != null ? chainLog.toString() : "";
        }
    }
    
    private String runServletTraceDemo() {
        try {
            ServletDeserializationTracer tracer = new ServletDeserializationTracer();
            var result = tracer.traceServletDeserialization();
            
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"status\": \"success\",\n");
            json.append("  \"type\": \"servlet\",\n");
            json.append("  \"executionSteps\": ").append(result.getExecutionSteps().size()).append(",\n");
            json.append("  \"warningCount\": ").append(result.getWarningCount()).append(",\n");
            json.append("  \"errorCount\": ").append(result.getErrorCount()).append(",\n");
            json.append("  \"securityScore\": ").append(result.getSecurityAssessment().getSecurityScore()).append(",\n");
            json.append("  \"riskLevel\": \"").append(result.getSecurityAssessment().getRiskLevel()).append("\",\n");
            json.append("  \"recommendation\": \"").append(escapeJson(result.getSecurityAssessment().getRecommendation())).append("\"\n");
            json.append("}");
            
            return json.toString();
            
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}";
        }
    }
    
    private String runFilterTraceDemo() {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"status\": \"success\",\n");
            json.append("  \"type\": \"filter\",\n");
            json.append("  \"message\": \"Filter追踪演示\",\n");
            json.append("  \"executionSteps\": 8,\n");
            json.append("  \"warningCount\": 1,\n");
            json.append("  \"errorCount\": 0,\n");
            json.append("  \"securityScore\": 75,\n");
            json.append("  \"riskLevel\": \"Medium\",\n");
            json.append("  \"recommendation\": \"建议加强Filter链的安全检查\"\n");
            json.append("}");
            
            return json.toString();
            
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}";
        }
    }
    
    private String runListenerTraceDemo() {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"status\": \"success\",\n");
            json.append("  \"type\": \"listener\",\n");
            json.append("  \"message\": \"Listener追踪演示\",\n");
            json.append("  \"executionSteps\": 6,\n");
            json.append("  \"warningCount\": 2,\n");
            json.append("  \"errorCount\": 0,\n");
            json.append("  \"securityScore\": 80,\n");
            json.append("  \"riskLevel\": \"Low\",\n");
            json.append("  \"recommendation\": \"Listener机制相对安全，建议定期检查\"\n");
            json.append("}");
            
            return json.toString();
            
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}";
        }
    }
    
    private String runAllComponentsTraceDemo() {
        return "{\"status\":\"success\",\"type\":\"all_components\",\"message\":\"全组件追踪演示\",\"executionSteps\":25,\"warningCount\":4,\"errorCount\":1,\"securityScore\":65,\"riskLevel\":\"High\",\"recommendation\":\"发现多个安全风险点，建议立即加强防护\"}";
    }
    
    private String generateListenerShellHTML(String action) {
        String html = "<!DOCTYPE html>\n" +
            "<html lang=\"zh-CN\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>内存马演示 - Listener</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: Arial, sans-serif;\n" +
            "            margin: 40px;\n" +
            "            background-color: #f5f5f5;\n" +
            "        }\n" +
            "        .warning {\n" +
            "            color: red;\n" +
            "            font-weight: bold;\n" +
            "            border: 2px solid red;\n" +
            "            padding: 10px;\n" +
            "            margin: 20px 0;\n" +
            "            background-color: #ffe6e6;\n" +
            "        }\n" +
            "        .info {\n" +
            "            background: #e6f3ff;\n" +
            "            padding: 15px;\n" +
            "            margin: 10px 0;\n" +
            "            border-left: 4px solid #007acc;\n" +
            "        }\n" +
            "        .success {\n" +
            "            background: #e6ffe6;\n" +
            "            padding: 15px;\n" +
            "            margin: 10px 0;\n" +
            "            border-left: 4px solid #00cc00;\n" +
            "        }\n" +
            "        input[type=text] {\n" +
            "            width: 500px;\n" +
            "            padding: 5px;\n" +
            "        }\n" +
            "        input[type=submit], .btn {\n" +
            "            padding: 8px 15px;\n" +
            "            margin: 5px;\n" +
            "            background-color: #007acc;\n" +
            "            color: white;\n" +
            "            border: none;\n" +
            "            cursor: pointer;\n" +
            "        }\n" +
            "        .btn-danger {\n" +
            "            background-color: #dc3545;\n" +
            "        }\n" +
            "        .log-area {\n" +
            "            background: #2d3748;\n" +
            "            color: #e2e8f0;\n" +
            "            padding: 15px;\n" +
            "            font-family: 'Courier New', monospace;\n" +
            "            font-size: 12px;\n" +
            "            max-height: 300px;\n" +
            "            overflow-y: auto;\n" +
            "            border-radius: 5px;\n" +
            "            margin: 15px 0;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"warning\">\n" +
            "        ⚠️ 警告: 这是一个用于安全教育的Listener内存马演示<br>\n" +
            "        此功能仅用于学习和演示目的，请勿用于非法用途！\n" +
            "    </div>\n" +
            "    \n" +
            "    <h1>👂 Listener内存马演示</h1>\n" +
            "    \n" +
            "    <div class=\"info\">\n" +
            "        <h3>Listener内存马信息:</h3>\n" +
            "        名称: DemoMemoryListener<br>\n" +
            "        类型: LISTENER<br>\n" +
            "        监听器类型: ServletContext, HttpSession, ServletRequest<br>\n" +
            "        状态: 活跃<br>\n" +
            "        描述: 监听所有Web应用事件，收集系统信息\n" +
            "    </div>\n" +
            "    \n";
            
        // 根据action参数决定是否添加详细信息
        if (action != null && "info".equals(action)) {
            html += "    <div class=\"success\">\n" +
                "        <h3>详细信息:</h3>\n" +
                "        • 监听ServletContext生命周期事件<br>\n" +
                "        • 监听HttpSession创建和销毁<br>\n" +
                "        • 监听ServletRequest的创建和销毁<br>\n" +
                "        • 收集访问者信息和系统状态<br>\n" +
                "        • 记录所有Web应用活动日志\n" +
                "    </div>\n" +
                "    \n" +
                "    <div class=\"info\">\n" +
                "        <h3>收集的信息类型:</h3>\n" +
                "        • 用户会话信息<br>\n" +
                "        • 请求路径和参数<br>\n" +
                "        • 客户端IP地址<br>\n" +
                "        • 浏览器信息<br>\n" +
                "        • 系统运行状态\n" +
                "    </div>\n" +
                "    \n" +
                "    <h3>事件日志:</h3>\n" +
                "    <div class=\"log-area\">\n" +
                "    [INFO] Listener内存马已激活\n" +
                "    [EVENT] ServletContext初始化完成\n" +
                "    [EVENT] 监听到新的HTTP会话创建\n" +
                "    [EVENT] ServletRequest事件触发\n" +
                "    [WARNING] 检测到可疑访问模式\n" +
                "    [INFO] 系统状态正常，继续监听...\n" +
                "    </div>\n";
        }
            
        html += "    \n" +
            "    <div style=\"margin-top: 30px;\">\n" +
            "        <h3>命令执行 (通过Listener代理):</h3>\n" +
            "        <form onsubmit=\"executeListenerCommand(event)\">\n" +
            "            <input type=\"text\" id=\"listenerCmd\" placeholder=\"输入命令 (如: whoami, pwd, date)\" style=\"width: 500px; padding: 8px;\">\n" +
            "            <input type=\"submit\" value=\"执行\" class=\"btn\">\n" +
            "        </form>\n" +
            "        <div id=\"cmdResult\" style=\"display: none; margin-top: 15px;\">\n" +
            "            <h4>执行结果:</h4>\n" +
            "            <div class=\"log-area\" id=\"cmdOutput\"></div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    \n" +
            "    <div style=\"margin-top: 30px;\">\n" +
            "        <h3>操作选项:</h3>\n" +
            "        <a href=\"/api/demo/listener-shell?action=info\" class=\"btn\">📊 查看详细信息</a>\n" +
            "        <a href=\"#\" class=\"btn btn-danger\" onclick=\"alert('演示环境中无法真正移除')\">🗑️ 移除Listener</a>\n" +
            "        <a href=\"/api/demo/\" class=\"btn\">← 返回主页</a>\n" +
            "    </div>\n" +
            "    \n" +
            "    <div class=\"info\" style=\"margin-top: 30px;\">\n" +
            "        <h4>💡 安全提示:</h4>\n" +
            "        Listener型内存马具有以下特点:<br>\n" +
            "        • 隐蔽性强，不直接处理HTTP请求<br>\n" +
            "        • 可以监听所有Web应用事件<br>\n" +
            "        • 难以通过常规检测方法发现<br>\n" +
            "        • 建议定期检查应用的监听器列表\n" +
            "    </div>\n" +
            "    \n" +
            "    <script>\n" +
            "    async function executeListenerCommand(event) {\n" +
            "        event.preventDefault();\n" +
            "        \n" +
            "        const cmd = document.getElementById('listenerCmd').value.trim();\n" +
            "        const resultDiv = document.getElementById('cmdResult');\n" +
            "        const outputDiv = document.getElementById('cmdOutput');\n" +
            "        \n" +
            "        if (!cmd) {\n" +
            "            alert('请输入命令');\n" +
            "            return;\n" +
            "        }\n" +
            "        \n" +
            "        resultDiv.style.display = 'block';\n" +
            "        outputDiv.textContent = '正在执行命令...';\n" +
            "        \n" +
            "        try {\n" +
            "            const response = await fetch('/api/demo/listener-cmd?cmd=' + encodeURIComponent(cmd), {\n" +
            "                method: 'POST'\n" +
            "            });\n" +
            "            const result = await response.json();\n" +
            "            \n" +
            "            if (result.status === 'success') {\n" +
            "                outputDiv.textContent = result.result;\n" +
            "            } else {\n" +
            "                outputDiv.textContent = '错误: ' + result.message;\n" +
            "                outputDiv.style.color = '#ff4444';\n" +
            "            }\n" +
            "        } catch (error) {\n" +
            "            outputDiv.textContent = '请求失败: ' + error.message;\n" +
            "            outputDiv.style.color = '#ff4444';\n" +
            "        }\n" +
            "    }\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";
            
        return html;
    }
    
    private boolean isAllowedCommand(String command) {
        // 移除命令限制，支持所有命令用于演示
        return true;
    }
    
    private String executeCommand(String command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            pb.command("cmd", "/c", command);
        } else {
            pb.command("/bin/bash", "-c", command);
        }
        
        Process process = pb.start();
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            result.append(line).append("\n");
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            java.io.BufferedReader errorReader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getErrorStream()));
            StringBuilder errorResult = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorResult.append(line).append("\n");
            }
            return "命令执行失败 (退出码: " + exitCode + "):\n" + errorResult.toString();
        }
        
        return result.length() > 0 ? result.toString() : "命令执行成功，无输出";
    }
    
    // JVM演示用辅助类
    public static class LargeObject implements Serializable {
        private static final long serialVersionUID = 1L;
        private final byte[] data;
        private final String description;
        
        public LargeObject(int size, String description) {
            this.data = new byte[size * 1024]; // KB
            this.description = description;
            Arrays.fill(data, (byte) 0x42);
        }
        
        @Override
        public String toString() {
            return "LargeObject{size=" + (data.length / 1024) + "KB, desc='" + description + "'}";
        }
    }
    
    public static class ComplexObject implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private boolean enableComplexLogic;
        
        public ComplexObject(String name, boolean enableComplexLogic) {
            this.name = name;
            this.enableComplexLogic = enableComplexLogic;
        }
        
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            if (enableComplexLogic) {
                performComplexCalculation();
            }
        }
        
        private void performComplexCalculation() {
            // 模拟复杂计算以产生字节码追踪
            long result = 1;
            for (int i = 1; i <= 10; i++) {
                result = fibonacci(i);
            }
            System.out.println("[COMPLEX] 计算结果: " + result);
        }
        
        private long fibonacci(int n) {
            if (n <= 1) return n;
            return fibonacci(n - 1) + fibonacci(n - 2);
        }
        
        @Override
        public String toString() {
            return "ComplexObject{name='" + name + "', complex=" + enableComplexLogic + "}";
        }
    }
    
    public static class ReflectiveObject implements Serializable {
        private static final long serialVersionUID = 1L;
        private String message;
        
        public ReflectiveObject(String message) {
            this.message = message;
        }
        
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            performReflectionOperations();
        }
        
        private void performReflectionOperations() {
            try {
                // 反射调用链演示
                Class<?> clazz = this.getClass();
                Method[] methods = clazz.getDeclaredMethods();
                
                for (Method method : methods) {
                    if ("toString".equals(method.getName())) {
                        method.setAccessible(true);
                        Object result = method.invoke(this);
                        System.out.println("[REFLECTION] 调用结果: " + result);
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("[REFLECTION] 反射调用异常: " + e.getMessage());
            }
        }
        
        @Override
        public String toString() {
            return "ReflectiveObject{message='" + message + "'}";
        }
    }
    
    public static class PerformanceObject implements Serializable {
        private static final long serialVersionUID = 1L;
        private int iterations;
        private List<String> data;
        
        public PerformanceObject(int iterations) {
            this.iterations = iterations;
            this.data = new ArrayList<>();
        }
        
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            performanceIntensiveOperation();
        }
        
        private void performanceIntensiveOperation() {
            long startTime = System.nanoTime();
            
            // CPU密集型操作
            for (int i = 0; i < iterations; i++) {
                data.add("Performance-" + i + "-" + Math.random());
                
                // 模拟一些计算
                double result = Math.sqrt(i * Math.PI);
                if (result > 0) {
                    data.set(i, data.get(i) + "-" + (int)result);
                }
            }
            
            long endTime = System.nanoTime();
            System.out.println("[PERFORMANCE] 操作耗时: " + (endTime - startTime) / 1_000_000.0 + "ms");
        }
        
        @Override
        public String toString() {
            return "PerformanceObject{iterations=" + iterations + ", dataSize=" + data.size() + "}";
        }
    }
}
