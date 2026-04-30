package com.shxy.suiyuanentity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户关注关系表
 * @TableName user_follow
 */
@TableName(value ="user_follow")
@Data
public class UserFollow {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关注者ID (粉丝)
     */
    private Long followerId;

    /**
     * 被关注者ID (博主)
     */
    private Long followeeId;

    /**
     * 关注时间
     */
    private Date createTime;
}