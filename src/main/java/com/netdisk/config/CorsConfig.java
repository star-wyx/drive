package com.netdisk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")//项目中的所有接口都支持跨域
                .allowedOrigins("http://172.17.0.1", "http://172.17.0.1:9070", "http://www.aijiangsb.com", "http://aijiangsb.com:9070",
                        "https://172.17.0.1", "https://172.17.0.1:9070", "https://www.aijiangsb.com", "https://aijiangsb.com:9070",
                        "https://www.aijiangsb.com:9070", "http://www.aijiangsb.com:9070")//所有地址都可以访问，也可以配置具体地址
                .allowCredentials(true)
                .allowedMethods("*")//"GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS"
                .maxAge(3600);// 跨域允许时间
    }
}