package com.shxy.suiyuanentity.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话信息VO")
public class SessionVO {
    
    @Schema(description = "会话ID")
    private Long sessionId;
    
    @Schema(description = "会话标题")
    private String title;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
