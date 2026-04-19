package com.shxy.suiyuanserver.config;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import darabonba.core.client.ClientOverrideConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/13 17:04
 */
@Configuration
public class SmsConfig {
    @Bean
    @Lazy
    public  AsyncClient client(StaticCredentialProvider provider) {
        return AsyncClient.builder()
                .region("cn-shenzhen") // Region ID
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride("dypnsapi.aliyuncs.com")
                )
                .build();
    }
    @Bean
    public StaticCredentialProvider provider() {
        return StaticCredentialProvider.create(
                Credential.builder()
                        .accessKeyId(System.getenv("ALIYUN_ACCESS_KEY_ID"))
                        .accessKeySecret(System.getenv("ALIYUN_ACCESS_KEY_SECRET"))
                        .build()
        );
    }
}
