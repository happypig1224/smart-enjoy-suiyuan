package com.shxy.suiyuanentity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 帖子点赞记录表
 * @TableName post_like
 */
@TableName(value = "post_like")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostLike {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 帖子ID，关联post.id
     */
    private Long postId;

    /**
     * 用户ID，关联user.id
     */
    private Long userId;

    /**
     * 点赞时间
     */
    private Date createTime;
}
