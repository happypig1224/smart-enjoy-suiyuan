package com.shxy.smartlearningacademyentity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 社区帖子表
 * @TableName post
 */
@TableName(value ="post")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Post {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布者ID，关联user.id
     */
    private Long userId;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子正文内容
     */
    private String content;

    /**
     * 板块分类: 0-技术讨论, 1-课程问题, 2-校园生活, 3-其他
     */
    private Integer type;

    /**
     * 点赞总数
     */
    private Integer likeCount;

    /**
     * 评论总数
     */
    private Integer commentCount;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 配图列表，存储URL数组
     */
    private Object images;

    /**
     * 发布时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}