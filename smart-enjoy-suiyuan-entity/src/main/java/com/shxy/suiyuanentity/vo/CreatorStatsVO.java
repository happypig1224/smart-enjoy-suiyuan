package com.shxy.suiyuanentity.vo;

import lombok.Data;

@Data
public class CreatorStatsVO {

    private Long totalPosts;

    private Long publishedPosts;

    private Long draftPosts;

    private Long reviewingPosts;

    private Long lockedPosts;

    private Long totalComments;

    private Long totalLikes;

    private Long totalViews;

    private Long totalWords;

    private Long totalFollowers;

    private Long newFollowers;

    private Long interactiveFollowers;
}
