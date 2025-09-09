#!/usr/bin/env java --source 21

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

/**
 * 验证Web组件追踪功能的测试脚本
 */
public class TestComponentTrace {
    
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("开始验证Web组件追踪功能...\n");
        
        HttpClient client = HttpClient.newHttpClient();
        String baseUrl = "http://localhost:8080/api/demo";
        
        // 测试端点列表
        String[] testEndpoints = {
            "/trace/servlet",
            "/trace/filter", 
            "/trace/listener",
            "/trace/components"
        };
        
        for (String endpoint : testEndpoints) {
            try {
                System.out.println("测试端点: " + endpoint);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
                
                HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                System.out.println("状态码: " + response.statusCode());
                
                if (response.statusCode() == 200) {
                    String body = response.body();
                    // 检查响应是否包含预期的JSON结构
                    if (body.contains("\"status\":\"success\"") && 
                        body.contains("executionSteps") &&
                        body.contains("securityScore")) {
                        System.out.println("✅ 测试通过 - 响应结构正确");
                        
                        // 提取一些关键信息
                        if (body.contains("\"type\":\"servlet\"")) {
                            System.out.println("   - Servlet追踪功能正常");
                        }
                        if (body.contains("\"type\":\"filter\"")) {
                            System.out.println("   - Filter追踪功能正常");
                        }
                        if (body.contains("\"type\":\"listener\"")) {
                            System.out.println("   - Listener追踪功能正常");
                        }
                        if (body.contains("\"type\":\"all_components\"")) {
                            System.out.println("   - 所有组件追踪功能正常");
                            System.out.println("   - 检测到组件数量: " + 
                                (body.split("\"components\":").length - 1));
                        }
                    } else {
                        System.out.println("⚠️ 响应格式异常");
                        System.out.println("响应内容: " + body.substring(0, Math.min(200, body.length())) + "...");
                    }
                } else {
                    System.out.println("❌ 请求失败: " + response.statusCode());
                }
                
            } catch (Exception e) {
                System.out.println("❌ 测试失败: " + e.getMessage());
            }
            
            System.out.println();
        }
        
        // 测试可视化页面
        System.out.println("测试可视化页面...");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/visualization/components"))
                .header("Accept", "text/html")
                .GET()
                .build();
            
            HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 && 
                response.body().contains("Web组件反序列化追踪可视化")) {
                System.out.println("✅ 组件可视化页面正常");
            } else {
                System.out.println("⚠️ 可视化页面异常");
            }
        } catch (Exception e) {
            System.out.println("❌ 可视化页面测试失败: " + e.getMessage());
        }
        
        System.out.println("\n测试完成！");
    }
}