package com.shxy.suiyuanentity.vo;

import lombok.Data;

@Data
public class CreatorPostListVO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String contentFormat;
    private Integer wordCount;
    private Integer type;
    private Integer status;
    private Integer isTop;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private String images;
    private String createTime;
    private String updateTime;
}
