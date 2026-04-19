package com.shxy.suiyuanserver.config;


import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
        return openApi -> {
            // 可以自定义一些配置，如：
            // 配置全局鉴权参数-Authorize
            // 根据@Tag 上的排序，写入x-order
        };
    }

    @Bean
    public OpenAPI openApi() {

        return new OpenAPI().info(new io.swagger.v3.oas.models.info.Info()
                .description("智享绥园平台API文档")
                .version("v1.0.0")
                .title("智享绥园平台API文档"));
    }

}