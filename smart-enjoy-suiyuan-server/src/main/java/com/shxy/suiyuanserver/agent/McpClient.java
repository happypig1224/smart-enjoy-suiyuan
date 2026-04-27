package com.shxy.suiyuanserver.agent;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.properties.McpProperties;
import com.shxy.suiyuanentity.entity.McpRequest;
import com.shxy.suiyuanentity.entity.McpResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/25 17:11
 */
@Slf4j
@Component
public class McpClient {

    @Resource
    private McpProperties mcpProperties;

    /**
     * 调用 Python Agent 的 MCP 接口
     */
    public McpResponse call(McpRequest request) {
        String url = mcpProperties.getServerUrl();
        int timeout = mcpProperties.getTimeout();
        int maxRetries = mcpProperties.getRetryCount();

        String jsonBody = JSONUtil.toJsonStr(request);
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                log.info("MCP Request (Attempt {}): URL={}, Body={}", attempt + 1, url, jsonBody);

                HttpResponse response = HttpRequest.post(url)
                        .body(jsonBody)
                        .timeout(timeout)
                        .execute();

                if (response.isOk()) {
                    String responseBody = response.body();
                    log.info("MCP Response: {}", responseBody);
                    return JSONUtil.toBean(responseBody, McpResponse.class);
                } else {
                    log.error("MCP 调用 HTTP 状态异常: {}", response.getStatus());
                }
            } catch (Exception e) {
                log.error("MCP 调用网络异常: {}", e.getMessage());
            }
            attempt++;

            // 短暂休眠后重试
            try {
                Thread.sleep(1000L * attempt);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        // 兜底返回
        McpResponse fallback = new McpResponse();
        fallback.setCode(500);
        fallback.setMessage("AI 服务暂时不可用，请稍后重试");
        fallback.setResult("抱歉，我暂时无法连接到知识库大脑，请稍后再试。");
        return fallback;
    }
}