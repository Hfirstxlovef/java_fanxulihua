package com.book.demo;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "DemoServlet", urlPatterns = {"/demo-servlet"})
public class DemoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Demo Servlet</title></head><body>");
        out.println("<h2>Demo Servlet - 反序列化演示</h2>");
        out.println("<p>这是一个用于演示Servlet反序列化追踪的示例Servlet</p>");
        out.println("<h3>功能说明：</h3>");
        out.println("<ul>");
        out.println("<li>处理GET请求：显示此演示页面</li>");
        out.println("<li>处理POST请求：接收并反序列化用户数据</li>");
        out.println("</ul>");
        
        out.println("<h3>测试表单：</h3>");
        out.println("<form method='post' action='/demo-servlet'>");
        out.println("<textarea name='data' placeholder='输入要序列化的文本数据' rows='4' cols='50'></textarea><br><br>");
        out.println("<input type='submit' value='提交数据进行序列化处理'>");
        out.println("</form>");
        
        out.println("</body></html>");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // 获取用户输入的数据
            String inputData = request.getParameter("data");
            if (inputData == null || inputData.trim().isEmpty()) {
                inputData = "默认演示数据";
            }
            
            // 创建一个用于演示的数据对象
            Map<String, Object> demoData = new HashMap<>();
            demoData.put("inputText", inputData);
            demoData.put("timestamp", System.currentTimeMillis());
            demoData.put("servletName", this.getClass().getSimpleName());
            demoData.put("sessionId", request.getSession().getId());
            demoData.put("remoteAddr", request.getRemoteAddr());
            
            // 模拟序列化过程
            String serializedData = serializeToBase64(demoData);
            
            // 模拟反序列化过程
            Object deserializedData = deserializeFromBase64(serializedData);
            
            // 返回处理结果
            out.println("{");
            out.println("  \"status\": \"success\",");
            out.println("  \"message\": \"数据序列化和反序列化处理完成\",");
            out.println("  \"originalData\": \"" + escapeJson(inputData) + "\",");
            out.println("  \"processedAt\": " + System.currentTimeMillis() + ",");
            out.println("  \"serializedSize\": " + serializedData.length() + ",");
            out.println("  \"deserializationResult\": \"" + escapeJson(deserializedData.toString()) + "\"");
            out.println("}");
            
        } catch (Exception e) {
            // 返回错误信息
            out.println("{");
            out.println("  \"status\": \"error\",");
            out.println("  \"message\": \"处理过程中发生错误\",");
            out.println("  \"error\": \"" + escapeJson(e.getMessage()) + "\"");
            out.println("}");
        }
    }
    
    private String serializeToBase64(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        
        byte[] serializedData = baos.toByteArray();
        return Base64.getEncoder().encodeToString(serializedData);
    }
    
    private Object deserializeFromBase64(String base64Data) throws IOException, ClassNotFoundException {
        byte[] serializedData = Base64.getDecoder().decode(base64Data);
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
        ObjectInputStream ois = new ObjectInputStream(bais);
        
        Object result = ois.readObject();
        ois.close();
        
        return result;
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("[DEMO-SERVLET] DemoServlet 初始化完成");
    }
    
    @Override
    public void destroy() {
        super.destroy();
        System.out.println("[DEMO-SERVLET] DemoServlet 销毁完成");
    }
}