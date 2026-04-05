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
 * 评论回复表
 * @TableName comment
 */
@TableName(value ="comment")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评论者ID，关联user.id
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 关联帖子ID，回复帖子时使用
     */
    private Long postId;

    /**
     * 关联失物招领ID，回复招领时使用
     */
    private Long lostItemId;

    /**
     * 父级评论ID，用于实现二级回复
     */
    private Long parentId;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 状态: 1-正常显示, -1-逻辑删除
     */
    private Integer status;

    /**
     * 评论时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除标记
     */
    private Integer isDeleted;
}