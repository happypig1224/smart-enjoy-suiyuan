package com.shxy.suiyuanserver.config;

import com.shxy.suiyuanserver.filter.CsrfFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CSRF过滤器配置
 * 
 * @author Tech Lead
 */
@Configuration
public class CsrfFilterConfig {
    
    @Bean
    public FilterRegistrationBean<CsrfFilter> csrfFilterRegistration(CsrfFilter csrfFilter) {
        FilterRegistrationBean<CsrfFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(csrfFilter);
        registration.addUrlPatterns("/user/*"); // 应用到用户相关接口
        registration.setName("csrfFilter");
        registration.setOrder(2); // 设置过滤器顺序，确保在登录拦截器之后执行
        return registration;
    }
}