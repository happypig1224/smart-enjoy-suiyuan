package com.shxy.suiyuanentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论视图对象")
public class CommentVO {
    
    @Schema(description = "评论ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户昵称")
    private String userNickName;
    
    @Schema(description = "用户头像")
    private String userAvatar;
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "帖子ID")
    private Long postId;
    
    @Schema(description = "失物ID")
    private Long lostItemId;
    
    @Schema(description = "父评论ID")
    private Long parentId;
    
    @Schema(description = "点赞数")
    private Integer likeCount;
    
    @Schema(description = "创建时间")
    private Date createTime;
}
