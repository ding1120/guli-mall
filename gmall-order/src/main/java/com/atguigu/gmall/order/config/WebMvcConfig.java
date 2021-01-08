package com.atguigu.gmall.order.config;

import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    //添加拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //拦截所有路径
        //向拦截器中注册
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**");
    }
}