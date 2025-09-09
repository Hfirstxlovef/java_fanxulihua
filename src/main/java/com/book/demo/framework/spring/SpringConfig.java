package com.book.demo.framework.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ComponentScan(basePackages = "com.book.demo.framework.spring")
@EnableAspectJAutoProxy
public class SpringConfig {
    
    @Bean
    public BusinessService businessService() {
        return new BusinessService();
    }
    
    @Bean
    public DataService dataService() {
        return new DataService();
    }
    
    @Bean
    public CustomSerializableBean customBean() {
        CustomSerializableBean bean = new CustomSerializableBean();
        bean.setId("CONFIG_BEAN");
        bean.setName("从配置创建的Bean");
        return bean;
    }
}