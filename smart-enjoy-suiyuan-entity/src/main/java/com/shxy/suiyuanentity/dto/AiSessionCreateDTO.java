package com.shxy.suiyuanentity.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSessionCreateDTO {

    @Size(max = 100, message = "会话名称长度不能超过100个字符")
    private String sessionName;
}