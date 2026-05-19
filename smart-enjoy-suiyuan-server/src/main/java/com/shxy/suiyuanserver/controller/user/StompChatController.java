package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.dto.PrivateMessageSendDTO;
import com.shxy.suiyuanentity.vo.MessageAckVO;
import com.shxy.suiyuanserver.interceptor.StompJwtChannelInterceptor.StompPrincipal;
import com.shxy.suiyuanserver.service.PrivateMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * STOMP私信消息接口
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/5/14 09:09
 */
@Slf4j
@Controller
public class StompChatController {

    @Resource
    private PrivateMessageService privateMessageService;

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 处理私信发送（STOMP 帧）
     * 前端 SEND → /app/chat/private
     */
    @MessageMapping("/chat/private")
    public void handlePrivateMessage(@Payload PrivateMessageSendDTO dto, Principal principal) {
        Long senderId = getUserId(principal);

        // 调用业务层发送
        MessageAckVO ack = privateMessageService.sendMessage(
                senderId, dto.getReceiverId(), dto.getMessageType(), dto.getContent(), dto.getClientMsgId());

        // 推送 ACK 回执给发送者
        messagingTemplate.convertAndSendToUser(
                senderId.toString(), "/queue/ack", ack);

        log.info("STOMP 私信已发送: {} -> {}, msgId={}, seqId={}",
                senderId, dto.getReceiverId(), ack.getMessageId(), ack.getSeqId());
    }

    /**
     * 处理已读标记（STOMP 帧）
     * 前端 SEND → /app/chat/read
     */
    @MessageMapping("/chat/read")
    public void handleMarkAsRead(@Payload java.util.Map<String, Object> payload, Principal principal) {
        Long userId = getUserId(principal);
        Long conversationId = Long.valueOf(payload.get("conversationId").toString());
        Long lastReadSeq = Long.valueOf(payload.get("lastReadSeq").toString());

        privateMessageService.markAsRead(userId, conversationId, lastReadSeq);
        log.info("已标记已读: userId={}, conversationId={}, lastReadSeq={}", userId, conversationId, lastReadSeq);
    }

    /**
     * 从 Spring Security Principal 提取用户ID
     */
    private Long getUserId(Principal principal) {
        if (principal instanceof StompPrincipal stompPrincipal) {
            return stompPrincipal.userId();
        }
        // 回退到 BaseContext（兼容非 STOMP 场景）
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        return userId;
    }
}
