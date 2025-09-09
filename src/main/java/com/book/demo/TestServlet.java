package com.book.demo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class TestServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Test Servlet</title></head>");
        out.println("<body>");
        out.println("<h1>✅ Servlet容器工作正常！</h1>");
        out.println("<p>时间: " + new java.util.Date() + "</p>");
        out.println("<p>上下文路径: " + request.getContextPath() + "</p>");
        out.println("<p>请求URI: " + request.getRequestURI() + "</p>");
        out.println("<h2>测试链接:</h2>");
        out.println("<ul>");
        out.println("<li><a href='/api/hello-world'>Jersey Hello API</a></li>");
        out.println("<li><a href='/api/demo/'>演示主页</a></li>");
        out.println("</ul>");
        out.println("</body>");
        out.println("</html>");
    }
}