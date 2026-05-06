package com.shxy.suiyuanserver.config;

import com.shxy.suiyuancommon.properties.McpProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;

@Component
@Slf4j
public class McpHealthIndicator implements HealthIndicator {

    private final McpProperties mcpProperties;

    public McpHealthIndicator(McpProperties mcpProperties) {
        this.mcpProperties = mcpProperties;
    }

    @Override
    public Health health() {
        try {
            String serverUrl = mcpProperties.getServerUrl();
            String baseUrl = serverUrl;
            if (baseUrl.contains("/mcp")) {
                baseUrl = baseUrl.substring(0, baseUrl.indexOf("/mcp"));
            }
            if (!baseUrl.endsWith("/")) {
                baseUrl = baseUrl + "/";
            }
            String healthUrl = baseUrl + "health";

            HttpURLConnection connection = (HttpURLConnection) new URL(healthUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int status = connection.getResponseCode();
            connection.disconnect();

            if (status == 200) {
                return Health.up().withDetail("url", healthUrl).build();
            } else {
                return Health.down().withDetail("url", healthUrl).withDetail("status", status).build();
            }
        } catch (Exception e) {
            return Health.down(e).withDetail("error", e.getMessage()).build();
        }
    }
}
