package com.shxy.suiyuanentity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信消息历史 VO
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @since 2026/5/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long messageId;

    /** 会话ID */
    private Long conversationId;

    /** 发送者用户ID */
    private Long senderId;

    /** 发送者昵称 */
    private String senderName;

    /** 发送者头像URL */
    private String senderAvatar;

    /** 接收者用户ID */
    private Long receiverId;

    /** 消息类型: TEXT / IMAGE / FILE */
    private String messageType;

    /** 消息内容 */
    private String content;

    /** 会话维度顺序ID */
    private Long seqId;

    /** 消息状态: SENT / DELIVERED / READ */
    private String status;

    /** 发送时间 */
    private LocalDateTime createTime;
}
