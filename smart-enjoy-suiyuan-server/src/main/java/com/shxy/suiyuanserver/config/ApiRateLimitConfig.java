package com.shxy.suiyuanserver.config;

import com.shxy.suiyuanserver.filter.ApiRateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API限流过滤器配置
 * 
 * @author Tech Lead
 */
@Configuration
public class ApiRateLimitConfig {
    
    @Bean
    public FilterRegistrationBean<ApiRateLimitFilter> apiRateLimitFilterRegistration(ApiRateLimitFilter apiRateLimitFilter) {
        FilterRegistrationBean<ApiRateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(apiRateLimitFilter);
        registration.addUrlPatterns("/user/*"); // 应用到用户相关接口
        registration.setName("apiRateLimitFilter");
        registration.setOrder(1); // 设置过滤器顺序，确保在其他过滤器之前执行
        return registration;
    }
}