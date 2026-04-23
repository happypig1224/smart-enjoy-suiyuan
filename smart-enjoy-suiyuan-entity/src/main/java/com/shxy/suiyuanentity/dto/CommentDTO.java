package com.shxy.suiyuanentity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    @Min(value = 1, message = "父评论ID必须大于0")
    private Long parentId;
    
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 1000, message = "评论内容长度必须在1-1000个字符之间")
    private String content;

    private Integer type;

    @Min(value = 1, message = "帖子ID必须大于0")
    private Long postId;

    @Min(value = 1, message = "失物招领ID必须大于0")
    private Long lostItemId;

    @Min(value = 1, message = "资源ID必须大于0")
    private Long resourceId;
}
