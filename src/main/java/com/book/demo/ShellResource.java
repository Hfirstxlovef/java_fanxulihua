package com.book.demo;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.book.demo.memshell.MemoryShellInjector;
import java.util.List;
import java.util.ArrayList;

@Path("/")
public class ShellResource {
    
    @GET
    @Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
    public Response getShellDemo(@QueryParam("action") String action, 
                                @QueryParam("type") String type,
                                @QueryParam("cmd") String cmd,
                                @QueryParam("servletAction") String servletAction,
                                @QueryParam("filterAction") String filterAction,
                                @QueryParam("filterCmd") String filterCmd) {
        
        // 如果是内存马相关的请求，重定向到实际的内存马处理
        if (isMemoryShellRequest(action, type, cmd, servletAction, filterAction, filterCmd)) {
            return handleMemoryShellRedirect(action, type, cmd, servletAction, filterAction, filterCmd);
        }
        
        // 如果是演示页面请求
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
    
    private boolean isMemoryShellRequest(String action, String type, String cmd, 
                                       String servletAction, String filterAction, String filterCmd) {
        // 检查是否是内存马实际功能请求
        return cmd != null || servletAction != null || filterAction != null || filterCmd != null ||
               (action != null && type != null && !"info".equals(action));
    }
    
    private Response handleMemoryShellRedirect(String action, String type, String cmd, 
                                             String servletAction, String filterAction, String filterCmd) {
        // 构建重定向URL
        StringBuilder redirectUrl = new StringBuilder("/memshell");
        
        if ("servlet".equals(type) || servletAction != null) {
            redirectUrl.append("/servlet");
        } else if ("filter".equals(type) || filterAction != null || filterCmd != null) {
            redirectUrl.append("/filter");
        }
        
        // 添加查询参数
        List<String> params = new ArrayList<>();
        if (action != null) params.add("action=" + action);
        if (type != null) params.add("type=" + type);
        if (cmd != null) params.add("cmd=" + cmd);
        if (servletAction != null) params.add("servletAction=" + servletAction);
        if (filterAction != null) params.add("filterAction=" + filterAction);
        if (filterCmd != null) params.add("filterCmd=" + filterCmd);
        
        if (!params.isEmpty()) {
            redirectUrl.append("?").append(String.join("&", params));
        }
        
        return Response.status(Response.Status.FOUND)
                .header("Location", redirectUrl.toString())
                .build();
    }
    
    @POST
    @Path("/inject")
    @Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
    public Response injectMemoryShell(@QueryParam("type") String type) {
        try {
            MemoryShellInjector.InjectionResult result;
            
            if ("servlet".equals(type)) {
                result = MemoryShellInjector.injectServletShell();
            } else if ("filter".equals(type)) {
                result = MemoryShellInjector.injectFilterShell();
            } else {
                return Response.ok("<h1>Error</h1><p>Invalid type: " + type + "</p>").build();
            }
            
            String html = "<h1>Memory Shell Injection</h1>" +
                         "<p>Type: " + type + "</p>" +
                         "<p>Success: " + result.isSuccess() + "</p>" +
                         "<p>Message: " + result.getMessage() + "</p>" +
                         "<a href='/shell'>Back to Shell</a>";
            
            return Response.ok(html).build();
            
        } catch (Exception e) {
            String html = "<h1>Injection Error</h1>" +
                         "<p>Error: " + e.getMessage() + "</p>" +
                         "<a href='/shell'>Back to Shell</a>";
            return Response.ok(html).build();
        }
    }
    
    private String generateShellDemoHTML(String type) {
        String title = type.substring(0, 1).toUpperCase() + type.substring(1) + " 内存马演示";
        String icon = "servlet".equals(type) ? "🔍" : "filter".equals(type) ? "🔄" : "👂";
        
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>" + title + "</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
            "            margin: 0;\n" +
            "            padding: 20px;\n" +
            "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
            "            min-height: 100vh;\n" +
            "        }\n" +
            "        .container {\n" +
            "            max-width: 1200px;\n" +
            "            margin: 0 auto;\n" +
            "            background: white;\n" +
            "            border-radius: 15px;\n" +
            "            padding: 30px;\n" +
            "            box-shadow: 0 15px 35px rgba(0,0,0,0.1);\n" +
            "        }\n" +
            "        .header {\n" +
            "            text-align: center;\n" +
            "            margin-bottom: 30px;\n" +
            "        }\n" +
            "        .header h1 {\n" +
            "            color: #2c3e50;\n" +
            "            font-size: 2.5em;\n" +
            "            margin-bottom: 10px;\n" +
            "        }\n" +
            "        .back-link {\n" +
            "            display: inline-block;\n" +
            "            margin-top: 30px;\n" +
            "            padding: 10px 20px;\n" +
            "            background: #3498db;\n" +
            "            color: white;\n" +
            "            text-decoration: none;\n" +
            "            border-radius: 5px;\n" +
            "            transition: background 0.3s;\n" +
            "        }\n" +
            "        .back-link:hover {\n" +
            "            background: #2980b9;\n" +
            "        }\n" +
            "        .demo-content {\n" +
            "            background: #f8f9fa;\n" +
            "            padding: 20px;\n" +
            "            border-radius: 8px;\n" +
            "            margin: 20px 0;\n" +
            "        }\n" +
            "        .warning {\n" +
            "            background: #fff3cd;\n" +
            "            border: 1px solid #ffeaa7;\n" +
            "            color: #856404;\n" +
            "            padding: 15px;\n" +
            "            border-radius: 5px;\n" +
            "            margin: 20px 0;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1>" + icon + " " + title + "</h1>\n" +
            "            <p>内存马检测与分析演示平台</p>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"warning\">\n" +
            "            <strong>⚠️ 安全警告</strong><br>\n" +
            "            此页面仅用于安全研究和教育目的。请勿在生产环境中使用。\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"demo-content\">\n" +
            "            <h3>" + type.toUpperCase() + " 内存马特征</h3>\n" +
            "            <ul>\n" +
            ("servlet".equals(type) ? 
                "                <li>动态注册Servlet实现命令执行</li>\n" +
                "                <li>通过ServletContext获取容器控制权</li>\n" +
                "                <li>隐蔽性高，难以检测</li>\n" +
                "                <li>持久化驻留内存</li>\n"
                : "filter".equals(type) ?
                "                <li>通过Filter链实现请求拦截</li>\n" +
                "                <li>可监听所有HTTP请求</li>\n" +
                "                <li>执行恶意代码和命令</li>\n" +
                "                <li>绕过传统安全检测</li>\n"
                :
                "                <li>监听Web应用生命周期事件</li>\n" +
                "                <li>在应用启动时自动激活</li>\n" +
                "                <li>具有系统级权限</li>\n" +
                "                <li>隐蔽性极强</li>\n") +
            "            </ul>\n" +
            "            \n" +
            "            <h3>检测方法</h3>\n" +
            "            <ul>\n" +
            "                <li>内存分析和进程监控</li>\n" +
            "                <li>Web容器日志审计</li>\n" +
            "                <li>动态调试和追踪</li>\n" +
            "                <li>行为模式识别</li>\n" +
            "            </ul>\n" +
            "            \n" +
            "            <h3>防护建议</h3>\n" +
            "            <ul>\n" +
            "                <li>定期更新Web容器和框架</li>\n" +
            "                <li>实施严格的输入验证</li>\n" +
            "                <li>部署Web应用防火墙</li>\n" +
            "                <li>监控异常网络行为</li>\n" +
            "            </ul>\n" +
            "        </div>\n" +
            "        \n" +
            "        <a href=\"/api/demo/\" class=\"back-link\">← 返回演示主页</a>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
    }
}