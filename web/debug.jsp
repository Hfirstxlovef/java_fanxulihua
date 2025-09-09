<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Jersey 调试页面</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .test-link { display: block; margin: 10px 0; padding: 10px; background: #f0f0f0; text-decoration: none; border-radius: 5px; }
        .test-link:hover { background: #e0e0e0; }
        .info { background: #e7f3ff; padding: 10px; border-left: 4px solid #2196F3; margin: 10px 0; }
    </style>
</head>
<body>
    <h1>Jersey REST API 调试页面</h1>
    
    <div class="info">
        <strong>当前时间：</strong><%= new java.util.Date() %><br>
        <strong>上下文路径：</strong><%= request.getContextPath() %><br>
        <strong>服务器信息：</strong><%= application.getServerInfo() %>
    </div>
    
    <h2>Jersey API 测试链接：</h2>
    
    <a href="<%= request.getContextPath() %>/api/hello-world" class="test-link" target="_blank">
        🔗 测试简单端点: /api/hello-world
    </a>
    
    <a href="<%= request.getContextPath() %>/api/demo/" class="test-link" target="_blank">
        🏠 演示主页: /api/demo/
    </a>
    
    <a href="<%= request.getContextPath() %>/api/demo/trace/basic" class="test-link" target="_blank">
        🔍 基础追踪演示: /api/demo/trace/basic
    </a>
    
    <a href="<%= request.getContextPath() %>/api/demo/trace/spring" class="test-link" target="_blank">
        🌱 Spring演示: /api/demo/trace/spring
    </a>
    
    <a href="<%= request.getContextPath() %>/api/demo/trace/jvm" class="test-link" target="_blank">
        ⚙️ JVM演示: /api/demo/trace/jvm
    </a>
    
    <h3>Jersey 配置信息：</h3>
    <div class="info">
        <strong>Jersey版本：</strong> 3.1.3<br>
        <strong>Servlet映射：</strong> /api/*<br>
        <strong>扫描包：</strong> com.book.demo<br>
        <strong>Application类：</strong> com.book.demo.HelloApplication
    </div>
    
    <script>
        // 自动测试简单端点
        fetch('<%= request.getContextPath() %>/api/hello-world')
            .then(response => response.text())
            .then(data => {
                const info = document.createElement('div');
                info.className = 'info';
                info.innerHTML = '<strong>Hello World 端点测试：</strong><br>' + data;
                document.body.appendChild(info);
            })
            .catch(error => {
                const info = document.createElement('div');
                info.className = 'info';
                info.style.background = '#ffebee';
                info.style.borderColor = '#f44336';
                info.innerHTML = '<strong>Hello World 端点测试失败：</strong><br>' + error;
                document.body.appendChild(info);
            });
    </script>
</body>
</html>