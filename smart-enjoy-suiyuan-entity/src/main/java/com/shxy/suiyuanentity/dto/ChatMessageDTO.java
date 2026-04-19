package com.shxy.suiyuanentity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    private Object role;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}