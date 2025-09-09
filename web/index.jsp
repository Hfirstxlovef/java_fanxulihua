<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>欢迎页面</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            text-align: center;
            margin-top: 50px;
        }
        h1 {
            color: #333;
        }
        p {
            color: #666;
        }
    </style>
</head>
<body>
    <h1>欢迎来到我们的网站</h1>
    <p>当前时间是: <%= new java.util.Date() %></p>
    <p>这是一个简单的JSP页面示例。</p>
</body>
</html>
