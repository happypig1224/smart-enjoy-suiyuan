package com.shxy.suiyuancommon.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/25 15:05
 */
@ConfigurationProperties(prefix = "smart.enjoy.suiyuan.ai.mcp")
@Component
@Data
public class McpProperties {
    private String serverUrl; // Python的AgentServer的URL
    private Integer timeout = 30000; // 默认30秒
    private Integer retryCount = 3;  // 重试次数
}
