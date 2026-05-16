package com.shxy.suiyuanentity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信会话表实体
 * @TableName private_conversation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("private_conversation")
public class PrivateConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户1 ID（值较小的一方，保证唯一约束生效） */
    private Long user1Id;

    /** 用户2 ID（值较大的一方） */
    private Long user2Id;

    /** 最后一条消息摘要（冗余字段，避免会话列表每次都 JOIN 消息表） */
    private String lastMessage;

    /** 最后一条消息时间 */
    private LocalDateTime lastMessageAt;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标识 */
    @TableLogic
    private Integer isDeleted;
}
