package com.shxy.suiyuanentity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户消息已读位点表实体
 * @TableName user_read_cursor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_read_cursor")
public class UserReadCursor implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 会话ID */
    private Long conversationId;

    /** 该用户在此会话中已读到的最大 seq_id */
    private Long lastReadSeq;

    /** 最后更新时间 */
    private LocalDateTime updateTime;
}
