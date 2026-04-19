package com.shxy.suiyuanentity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatDTO {

    private Long sessionId;

    @NotBlank(message = "问题内容不能为空")
    private String query;
}