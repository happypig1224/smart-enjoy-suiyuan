package com.shxy.suiyuanentity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 私信消息发送请求 DTO
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @since 2026/5/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessageSendDTO {

    /** 接收者用户ID */
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    /** 消息类型: TEXT / IMAGE / FILE */
    @NotBlank(message = "消息类型不能为空")
    private String messageType;

    /** 消息内容 */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 5000, message = "消息内容不能超过5000个字符")
    private String content;

    /** 客户端消息ID（UUID，用于幂等去重） */
    @NotBlank(message = "客户端消息ID不能为空")
    @Size(max = 64, message = "客户端消息ID过长")
    private String clientMsgId;
}
