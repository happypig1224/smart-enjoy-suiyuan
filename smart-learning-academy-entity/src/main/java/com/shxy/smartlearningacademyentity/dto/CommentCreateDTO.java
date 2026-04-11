package com.shxy.smartlearningacademyentity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论创建数据传输对象")
public class CommentCreateDTO {
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "评论类型")
    private Integer type;
    
    @Schema(description = "帖子ID")
    private Long postId;
    
    @Schema(description = "失物ID")
    private Long lostItemId;
}
