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
    /**
     * Python Agent 服务的 MCP 接口地址
     */
    private String serverUrl;

    /**
     * 请求超时时间 (毫秒)
     */
    private Integer timeout;

    /**
     * 失败重试次数
     */
    private Integer retryCount;
}
