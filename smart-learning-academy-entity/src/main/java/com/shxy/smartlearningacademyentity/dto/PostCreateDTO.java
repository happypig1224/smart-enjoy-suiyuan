package com.shxy.smartlearningacademyentity.dto;

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
public class PostCreateDTO {
    
    @Schema(description = "帖子标题")
    private String title;
    
    @Schema(description = "帖子内容")
    private String content;
    
    @Schema(description = "帖子类型")
    private Integer type;
    
    @Schema(description = "帖子图片列表")
    private List<String> images;
}
