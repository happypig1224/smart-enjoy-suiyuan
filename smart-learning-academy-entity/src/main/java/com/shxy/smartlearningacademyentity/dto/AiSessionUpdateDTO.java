package com.shxy.smartlearningacademyentity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSessionUpdateDTO {

    @NotNull(message = "会话ID不能为空")
    private Long id;

    private Integer status;
}