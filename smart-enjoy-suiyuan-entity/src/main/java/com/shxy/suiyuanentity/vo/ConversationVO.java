package com.shxy.suiyuanentity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话列表项 VO
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @since 2026/5/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long conversationId;

    /** 对方用户ID */
    private Long targetUserId;

    /** 对方用户昵称 */
    private String targetUserName;

    /** 对方用户头像URL */
    private String targetUserAvatar;

    /** 最后一条消息摘要 */
    private String lastMessage;

    /** 最后一条消息时间 */
    private LocalDateTime lastMessageAt;

    /** 未读消息数 */
    private Integer unreadCount;
}
