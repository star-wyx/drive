package com.netdisk.config;

//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.web.header.writers.StaticHeadersWriter;
//
//@EnableWebSecurity
//@Configuration
//public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .headers().frameOptions().disable()
//                .addHeaderWriter(new StaticHeadersWriter("X-FRAME-OPTIONS",
//                        "X-Frame-Options ALLOW-FROM " +
//                                "http://nighttown.aijiangsb.com " +
//                                "http://nighttown.aijiangsb.com:9070 " +
//                                "https://nighttown.aijiangsb.com " +
//                                "https://nighttown.aijiangsb.com:9070"));
//    }
//}

public class WebSecurityConfig {

}