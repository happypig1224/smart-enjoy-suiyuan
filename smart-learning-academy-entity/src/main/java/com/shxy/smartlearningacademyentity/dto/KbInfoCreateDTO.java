package com.shxy.smartlearningacademyentity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KbInfoCreateDTO {

    @NotBlank(message = "知识库名称不能为空")
    private String kbName;

    private String kbDescription;

    private String kbCategory;

    @NotBlank(message = "来源类型不能为空")
    private String sourceType;
}