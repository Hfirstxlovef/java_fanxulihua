import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import java.io.File;

/**
 * 启动内置Tomcat来测试WAR应用
 */
public class EmbeddedTomcatLauncher {
    
    public static void main(String[] args) throws LifecycleException, InterruptedException {
        
        System.out.println("🚀 启动内置Tomcat服务器...");
        
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.setHostname("localhost");
        
        // 设置工作目录
        String webappDirLocation = "target/demo-1.0-SNAPSHOT";
        Context context = tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());
        
        System.out.println("📁 Web应用路径: " + new File(webappDirLocation).getAbsolutePath());
        System.out.println("🌐 服务器地址: http://localhost:8080");
        
        tomcat.start();
        
        System.out.println("✅ Tomcat服务器启动完成!");
        System.out.println("📋 可以访问以下地址进行测试:");
        System.out.println("   - 主页: http://localhost:8080/api/demo/");
        System.out.println("   - Servlet追踪: http://localhost:8080/api/demo/trace/servlet");
        System.out.println("   - Filter追踪: http://localhost:8080/api/demo/trace/filter");
        System.out.println("   - Listener追踪: http://localhost:8080/api/demo/trace/listener");
        System.out.println("   - 所有组件追踪: http://localhost:8080/api/demo/trace/components");
        System.out.println("   - 组件可视化: http://localhost:8080/api/demo/visualization/components");
        System.out.println();
        System.out.println("按 Ctrl+C 停止服务器...");
        
        tomcat.getServer().await();
    }
}