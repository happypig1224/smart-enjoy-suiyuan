package com.shxy.suiyuanentity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户通知表
 */
@TableName(value = "user_notification")
@Data
public class UserNotification {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收者ID
     */
    private Long userId;

    /**
     * 发送者ID
     */
    private Long fromUserId;

    /**
     * 通知类型: follow, post_favorite, resource_favorite, comment_reply
     */
    private String type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联业务ID（帖子ID、资源ID等）
     */
    private Long businessId;

    /**
     * 跳转链接
     */
    private String link;

    /**
     * 是否已读: 0-未读, 1-已读
     */
    private Integer isRead;

    /**
     * 是否删除: 0-正常, 1-已删除
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 阅读时间
     */
    private Date readTime;
}
