package com.shxy.suiyuanentity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 未读计数 VO
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @since 2026/5/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户总未读消息数 */
    private Integer totalUnread;

    /** 指定会话的未读消息数（单个会话查询时使用） */
    private Integer conversationUnread;
}
