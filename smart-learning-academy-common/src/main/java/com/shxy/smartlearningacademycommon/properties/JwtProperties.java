package com.shxy.smartlearningacademycommon.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4 22:56
 */
@Data
@Component
@ConfigurationProperties(prefix = "smark.learning.academy.jwt")
public class JwtProperties {
    // 管理端
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

    // 用户端
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;
}
