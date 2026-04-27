package com.shxy.suiyuanentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天消息VO")
public class ChatMessageVO {
    
    @Schema(description = "角色(user/assistant)")
    private String role;
    
    @Schema(description = "消息内容")
    private String content;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
