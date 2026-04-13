package com.shxy.smartlearningacademyentity.dto;

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
public class KbDocumentDTO {

    @NotNull(message = "知识库ID不能为空")
    private Long kbId;

    @NotBlank(message = "文档标题不能为空")
    private String docTitle;

    private String docContent;

    private String filePath;

    private String fileType;

    private Long fileSize;

    private Integer vectorStatus;
}