package com.shxy.suiyuanentity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "帖子创建数据传输对象")
public class PostDTO {
    
    @NotBlank(message = "帖子标题不能为空")
    @Size(min = 1, max = 100, message = "帖子标题长度必须在1-100个字符之间")
    @Schema(description = "帖子标题")
    private String title;
    
    @NotBlank(message = "帖子内容不能为空")
    @Size(min = 1, max = 50000, message = "帖子内容长度必须在1-50000个字符之间")
    @Schema(description = "帖子内容 (支持 Markdown 格式)")
    private String content;
    
    @Schema(description = "内容格式: markdown, html")
    private String contentFormat = "markdown";
    
    @NotNull(message = "帖子类型不能为空")
    @Schema(description = "帖子类型")
    private Integer type;
    
    @Schema(description = "帖子图片列表")
    private List<String> images;

    @Schema(description = "帖子状态: 0-草稿, 1-已发布", example = "1")
    private Integer status = 1;


}
