package com.shxy.suiyuanentity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 帖子收藏状态 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostFavoriteStatusVO {

    private Long postId;

    private Boolean isFavorited;
}
