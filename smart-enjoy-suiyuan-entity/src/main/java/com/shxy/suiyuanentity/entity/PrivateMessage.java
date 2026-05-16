package com.shxy.suiyuanentity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信消息表实体
 * @TableName private_message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("private_message")
public class PrivateMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 所属会话ID */
    private Long conversationId;

    /** 发送者用户ID */
    private Long senderId;

    /** 接收者用户ID */
    private Long receiverId;

    /** 消息类型: TEXT / IMAGE / FILE */
    private String messageType;

    /** 消息内容（文本存字符串，富媒体存JSON） */
    private String content;

    /** 会话维度顺序ID（单调递增） */
    private Long seqId;

    /** 消息状态: SENT / DELIVERED / READ */
    private String status;

    /** 客户端消息ID（UUID，幂等去重） */
    private String clientMsgId;

    /** 发送时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 逻辑删除标识 */
    @TableLogic
    private Integer isDeleted;
}
