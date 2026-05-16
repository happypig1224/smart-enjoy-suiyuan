package com.shxy.suiyuanserver.config;

import com.shxy.suiyuanserver.interceptor.StompJwtChannelInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP 协议配置
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfig implements WebSocketMessageBrokerConfigurer {

    @Resource
    private StompJwtChannelInterceptor stompJwtChannelInterceptor;

    /**
     * 配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 内置消息代理：客户端订阅这些前缀的消息
        registry.enableSimpleBroker("/topic", "/queue", "/user");

        // 服务端接收消息的应用前缀
        registry.setApplicationDestinationPrefixes("/app");

        // 用户专属消息前缀（点对点推送）
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 注册 STOMP 端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * 配置入站通道拦截器（JWT 认证 + 权限校验）
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompJwtChannelInterceptor);
    }
}
