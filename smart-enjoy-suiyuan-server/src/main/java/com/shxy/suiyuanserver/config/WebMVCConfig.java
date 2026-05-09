package com.shxy.suiyuanserver.config;

import com.shxy.suiyuanserver.interceptor.LoginAdminInterceptor;
import com.shxy.suiyuanserver.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Web MVC配置
 * 包含拦截器配置、CORS跨域配置等
 * @author Wu, Hui Ming
 * @version 2.0
 * @School Suihua University
 * @since 2026/4/4 23:19
 */
@Configuration
public class WebMVCConfig implements WebMvcConfigurer {
    @Autowired
    private LoginAdminInterceptor loginAdminInterceptor;
    @Autowired
    private LoginUserInterceptor loginUserInterceptor;

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

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
                        "/user/user/check-exists",
                        "/user/user/verify-captcha",
                        "/user/user/password/forgot",
                        "/user/forum/post/list",
                        "/user/forum/post/detail/**",
                        "/user/forum/comment/list",
                        "/user/forum/post/batch-counts",
                        "/user/resource/list",
                        "/user/resource/detail/**",
                        "/user/lost-found/list",
                        "/user/lost-found/detail/**",
                        "/user/lost-found/all-for-sync",
                        "/user/captcha/**"
                        );
    }

    /**
     * CORS跨域配置
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        String[] origins = allowedOrigins.split(",");
        if (origins.length == 1 && "*".equals(origins[0].trim())) {
            config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://[::1]:*",
                "https://*",
                "http://*"
            ));
        } else {
            for (String origin : origins) {
                config.addAllowedOriginPattern(origin.trim());
            }
        }
        config.setAllowCredentials(true);
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedHeader("*");
        config.addExposedHeader("authentication");
        config.addExposedHeader("admin-token");
        config.addExposedHeader("X-Trace-Id");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
