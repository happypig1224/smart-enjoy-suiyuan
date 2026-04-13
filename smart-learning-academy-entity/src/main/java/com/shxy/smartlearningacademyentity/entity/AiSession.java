package com.shxy.smartlearningacademyentity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * AI会话管理表
 * @TableName ai_session
 */
@TableName(value ="ai_session")
@Data
public class AiSession {
    /**
     * 会话ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联user.id
     */
    private Long userId;

    /**
     * 会话名称/标题
     */
    private String sessionName;

    /**
     * 累计消耗Token数
     */
    private Integer tokenCount;

    /**
     * 会话消息数
     */
    private Integer messageCount;

    /**
     * 状态: 1-活跃, 0-归档, -1-删除
     */
    private Integer status;

    /**
     * 最后一条消息摘要
     */
    private String lastMessage;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}