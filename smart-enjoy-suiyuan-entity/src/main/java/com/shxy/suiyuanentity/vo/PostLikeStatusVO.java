package com.shxy.suiyuanentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "帖子点赞状态视图对象")
public class PostLikeStatusVO {
    
    @Schema(description = "帖子ID")
    private Long postId;
    
    @Schema(description = "是否已点赞")
    private Boolean isLiked;
}
