package com.shxy.suiyuanserver.agent;

import cn.hutool.json.JSONUtil;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.properties.McpProperties;
import com.shxy.suiyuanentity.entity.McpRequest;
import com.shxy.suiyuanentity.entity.McpResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
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

                cn.hutool.http.HttpResponse response = cn.hutool.http.HttpRequest.post(url)
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

            try {
                Thread.sleep(1000L * attempt);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        McpResponse fallback = new McpResponse();
        fallback.setCode(500);
        fallback.setMessage("AI 服务暂时不可用，请稍后重试");
        fallback.setResult("抱歉,当前服务不可用，稍后重试喔~");
        return fallback;
    }

    /**
     * 流式调用 Python Agent 的 MCP 接口
     * @param request MCP 请求
     * @param callback 流式回调接口
     */
    public void callStream(McpRequest request, StreamCallback callback) {
        String baseUrl = mcpProperties.getServerUrl();
        int timeout = mcpProperties.getTimeout();

        String url = baseUrl;
        if (url.endsWith("/mcp")) {
            url = url + "/stream";
        } else if (!url.endsWith("/mcp/stream")) {
            url = url + "/stream";
        }

        String jsonBody = JSONUtil.toJsonStr(request);

        try {
            log.info("MCP Stream Request: URL={}, Body={}", url, jsonBody);

            cn.hutool.http.HttpRequest httpRequest = cn.hutool.http.HttpRequest.post(url)
                    .body(jsonBody)
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .header("Connection", "close");

            cn.hutool.http.HttpResponse response = httpRequest.executeAsync();

            if (!response.isOk()) {
                callback.onError("MCP 服务返回异常状态: " + response.getStatus());
                return;
            }

            log.info("开始读取流式响应...");

            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(response.bodyStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                
                String line;
                boolean hasContent = false;
                int chunkCount = 0;

                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) continue;

                    if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        
                        if ("[DONE]".equals(data)) {
                            log.info("收到 [DONE] 信号，流式调用完成");
                            callback.onComplete();
                            return;
                        }
                        
                        try {
                            Map<String, Object> eventData = JSONUtil.toBean(data, Map.class);
                            
                            if (eventData.containsKey("error")) {
                                callback.onError(String.valueOf(eventData.get("error")));
                                return;
                            }
                            
                            if (eventData.containsKey("content")) {
                                String content = String.valueOf(eventData.get("content"));
                                if (content != null && !content.isEmpty()) {
                                    chunkCount++;
                                    callback.onChunk(content);
                                    hasContent = true;
                                    log.debug("收到第 {} 个 chunk: {}", chunkCount, content.length() > 50 ? content.substring(0, 50) + "..." : content);
                                }
                            }
                        } catch (Exception e) {
                            log.debug("解析 SSE 数据失败，尝试作为纯文本处理");
                            if (!data.isEmpty()) {
                                callback.onChunk(data);
                                hasContent = true;
                            }
                        }
                    }
                }

                log.info("流式读取完成，共收到 {} 个 chunk", chunkCount);

                if (hasContent) {
                    callback.onComplete();
                } else {
                    callback.onError("未收到有效响应");
                }
            }

        } catch (Exception e) {
            log.error("MCP 流式调用异常: {}", e.getMessage(), e);
            callback.onError("服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 流式调用回调接口
     */
    public interface StreamCallback {
        /**
         * 接收到数据块时调用
         * @param chunk 数据块内容
         */
        void onChunk(String chunk);

        /**
         * 流完成时调用
         */
        void onComplete();

        /**
         * 发生错误时调用
         * @param error 错误信息
         */
        void onError(String error);
    }
}