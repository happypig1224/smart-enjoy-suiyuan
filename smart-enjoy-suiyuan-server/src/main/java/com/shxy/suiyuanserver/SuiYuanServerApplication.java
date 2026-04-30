package com.shxy.suiyuanserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = {"com.shxy.suiyuancommon", "com.shxy.suiyuanentity", "com.shxy.suiyuanserver",})
@MapperScan("com.shxy.suiyuanserver.mapper")
@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
public class SuiYuanServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuiYuanServerApplication.class, args);
    }

}
