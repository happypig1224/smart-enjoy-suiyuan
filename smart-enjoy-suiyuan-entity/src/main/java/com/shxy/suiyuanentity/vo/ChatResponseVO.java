package com.shxy.suiyuanentity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI 聊天响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前会话 ID
     */
    private Long sessionId;

    /**
     * AI 回复内容
     */
    private String reply;
}
