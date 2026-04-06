package com.shxy.smartlearningacademycommon.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4 22:53
 */
@Component
@Data
@ConfigurationProperties(prefix = "smart.learning.academy.tencent")
public class TencentCOSProperties {
    private String secretId;
    private String secretKey;
    private String region;
    private String bucketName;
    private String avatarDir;
    private String imageDir;

}
