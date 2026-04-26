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

/**
 * Web MVC配置
 * <p>
 * 包含拦截器配置、CORS跨域配置等
 * </p>
 *
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
                        "/user/forum/post/list",
                        "/user/forum/post/detail/**",
                        "/user/forum/comment/list",
                        "/user/resource/list",
                        "/user/resource/detail/**",
                        "/user/resource/download/**",
                        "/user/lost-found/list",
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
        // 如果配置了"*"，使用allowedOriginPatterns替代allowedOrigin以支持credentials
        if (origins.length == 1 && "*".equals(origins[0].trim())) {
            config.setAllowedOriginPatterns(java.util.Collections.singletonList("*"));
        } else {
            for (String origin : origins) {
                config.addAllowedOrigin(origin.trim());
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
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
