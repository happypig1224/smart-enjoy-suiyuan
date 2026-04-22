package com.shxy.suiyuanserver.config;

import com.shxy.suiyuanserver.interceptor.LoginAdminInterceptor;
import com.shxy.suiyuanserver.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4 23:19
 */
@Configuration
public class WebMVCConfig implements WebMvcConfigurer {
    @Autowired
    private LoginAdminInterceptor loginAdminInterceptor;
    @Autowired
    private LoginUserInterceptor loginUserInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginAdminInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login", "/api/admin/register");
                
        registry.addInterceptor(loginUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns(
                    "/user/user/login", 
                    "/user/user/register", 
                    "/user/user/captcha/send",
                    // 论坛公共接口
                    "/user/forum/post/list",
                    "/user/forum/post/detail/**",
                    "/user/forum/comment/list",
                    // 资源公共接口
                    "/user/resource/list",
                    "/user/resource/{id}",
                    "/user/resource/detail/**"
                );
    }
}
