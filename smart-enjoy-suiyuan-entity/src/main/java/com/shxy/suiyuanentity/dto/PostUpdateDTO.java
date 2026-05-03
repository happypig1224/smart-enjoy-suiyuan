package com.shxy.suiyuanentity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "帖子更新数据传输对象")
public class PostUpdateDTO {

    @NotNull(message = "帖子ID不能为空")
    @Min(value = 1, message = "帖子ID必须大于0")
    @Schema(description = "帖子ID")
    private Long id;

    @Size(min = 1, max = 100, message = "帖子标题长度必须在1-100个字符之间")
    @Schema(description = "帖子标题")
    private String title;

    @Size(min = 1, max = 50000, message = "帖子内容长度必须在1-50000个字符之间")
    @Schema(description = "帖子内容 (支持 Markdown 格式)")
    private String content;

    @Schema(description = "内容格式: markdown, html")
    private String contentFormat;

    @Schema(description = "帖子类型：0-技术讨论, 1-课程问题, 2-校园生活, 3-其他")
    private Integer type;

    @Schema(description = "帖子图片列表")
    private List<String> images;
}
