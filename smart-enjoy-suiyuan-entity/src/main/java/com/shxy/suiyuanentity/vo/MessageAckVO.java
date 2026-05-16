package com.shxy.suiyuanentity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 消息发送回执 VO（通过 WebSocket 推给发送者）
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @since 2026/5/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageAckVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 客户端消息ID（关联前端本地消息） */
    private String clientMsgId;

    /** 服务端生成的消息ID */
    private Long messageId;

    /** 服务端生成的顺序ID */
    private Long seqId;

    /** 会话ID */
    private Long conversationId;

    /** 消息状态: SENT / DELIVERED */
    private String status;
}
