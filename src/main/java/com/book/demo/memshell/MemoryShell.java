package com.book.demo.memshell;

/**
 * 内存马基础接口
 * 用于安全教育和演示目的
 */
public interface MemoryShell {
    
    /**
     * 内存马类型
     */
    enum Type {
        SERVLET("Servlet内存马", "动态注册恶意Servlet"),
        FILTER("Filter内存马", "注入恶意Filter到过滤器链"),
        LISTENER("Listener内存马", "注册恶意事件监听器");
        
        private final String name;
        private final String description;
        
        Type(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
    
    /**
     * 获取内存马类型
     */
    Type getType();
    
    /**
     * 获取内存马名称
     */
    String getName();
    
    /**
     * 注入内存马到容器中
     * @return 注入是否成功
     */
    boolean inject() throws Exception;
    
    /**
     * 检查内存马是否处于活跃状态
     */
    boolean isActive();
    
    /**
     * 移除内存马
     */
    boolean remove() throws Exception;
    
    /**
     * 获取内存马详细信息
     */
    MemoryShellInfo getInfo();
    
    /**
     * 执行命令（仅用于演示）
     * @param command 要执行的命令
     * @return 执行结果
     */
    String executeCommand(String command) throws Exception;
}