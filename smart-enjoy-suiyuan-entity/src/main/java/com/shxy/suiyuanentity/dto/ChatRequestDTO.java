package com.shxy.suiyuanentity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRequestDTO {

    @NotBlank(message = "消息不能为空")
    @Size(max = 2000, message = "消息长度不能超过2000个字符")
    private String query;

    private Long sessionId;
}
