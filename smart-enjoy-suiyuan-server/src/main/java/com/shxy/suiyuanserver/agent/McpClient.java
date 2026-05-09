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
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

    @Value("${smart.enjoy.suiyuan.ai.mcp.service-token:}")
    private String mcpServiceToken;

    private static final Semaphore SSE_PERMITS = new Semaphore(50);

    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private static final int CIRCUIT_BREAKER_THRESHOLD = 5;
    private static final long CIRCUIT_BREAKER_RESET_MS = 30000;

    private boolean isCircuitOpen() {
        if (failureCount.get() >= CIRCUIT_BREAKER_THRESHOLD) {
            if (System.currentTimeMillis() - lastFailureTime.get() > CIRCUIT_BREAKER_RESET_MS) {
                failureCount.set(0);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 调用 Python Agent 的 MCP 接口
     */
    public McpResponse call(McpRequest request) {
        if (isCircuitOpen()) {
            McpResponse fallback = new McpResponse();
            fallback.setCode(503);
            fallback.setMessage("AI 服务暂时不可用，请稍后重试");
            fallback.setResult("抱歉，AI服务暂时不可用，稍后重试喔~");
            return fallback;
        }

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
                        .header("X-Service-Token", mcpServiceToken)
                        .header("X-Trace-Id", org.slf4j.MDC.get("traceId") != null ? org.slf4j.MDC.get("traceId") : java.util.UUID.randomUUID().toString())
                        .execute();

                if (response.isOk()) {
                    String responseBody = response.body();
                    log.info("MCP Response: {}", responseBody);
                    failureCount.set(0);
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
        failureCount.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());
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
            if (!SSE_PERMITS.tryAcquire(10, TimeUnit.SECONDS)) {
                callback.onError("AI服务繁忙，请稍后重试");
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            callback.onError("获取连接许可被中断");
            return;
        }

        try {
            log.info("MCP Stream Request: URL={}", url);

            HttpRequest httpRequest = HttpRequest.post(url)
                    .body(jsonBody)
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .header("Connection", "close")
                    .header("X-Service-Token", mcpServiceToken)
                    .header("X-Trace-Id", MDC.get("traceId") != null ? MDC.get("traceId") : java.util.UUID.randomUUID().toString());

            HttpResponse response = httpRequest.executeAsync();

            if (!response.isOk()) {
                callback.onError("MCP 服务返回异常状态: " + response.getStatus());
                return;
            }

            log.info("开始读取流式响应...");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.bodyStream(), StandardCharsets.UTF_8))) {

                String line;
                boolean hasContent = false;
                int chunkCount = 0;
                long streamStartTime = System.currentTimeMillis();
                long lastChunkTime = streamStartTime;

                while ((line = reader.readLine()) != null) {
                    long chunkTime = System.currentTimeMillis();
                    if (chunkTime - lastChunkTime > 30000) {
                        callback.onError("流式响应超时：30秒内未收到数据");
                        return;
                    }
                    if (chunkTime - streamStartTime > 120000) {
                        callback.onError("流式响应超时：总时长超过120秒");
                        return;
                    }
                    lastChunkTime = chunkTime;

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
        } finally {
            SSE_PERMITS.release();
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