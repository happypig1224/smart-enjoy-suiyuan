package com.shxy.suiyuanentity.entity;

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
     * 帖子正文内容 (支持 Markdown 富文本)
     */
    private String content;

    /**
     * 内容格式: markdown, html
     */
    private String contentFormat;

    /**
     * 正文字数统计
     */
    private Integer wordCount;

    /**
     * 板块分类: 0-技术讨论, 1-课程问题, 2-校园生活, 3-其他
     */
    private Integer type;

    /**
     * 帖子状态: 0-草稿, 1-已发布, 2-已锁定, 3-审核中
     */
    private Integer status;

    /**
     * 是否置顶: 0-否, 1-是
     */
    private Integer isTop;

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
    private String images;

    /**
     * 发布时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    private Integer isDeleted;
}