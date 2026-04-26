package com.shxy.suiyuanserver.agent.client;

import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.properties.McpProperties;
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
@Component
public class McpClient {
    @Autowired
    private McpProperties mcpProperties;
    @Autowired(required = false)
    private RestTemplate restTemplate;

    public String callPythonAgent(String query, Long sessionId) {
        // 构建MCP协议报文
        Map<String, Object> payload = Map.of("method", "call_tool", "params", Map.of("name", "chat_agent", "arguments", Map.of("query", query, "session_id", sessionId)));
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(mcpProperties.getServerUrl(), payload, Map.class);
            return (String) response.getBody().get("result");
        } catch (RestClientException e) {
            throw new BaseException("AI 助手连接异常");
        }
    }
}
