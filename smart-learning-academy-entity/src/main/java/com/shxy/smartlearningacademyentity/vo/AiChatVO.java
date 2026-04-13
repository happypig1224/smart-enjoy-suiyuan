package com.shxy.smartlearningacademyentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI聊天响应VO")
public class AiChatVO {

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "AI回答")
    private String answer;
}