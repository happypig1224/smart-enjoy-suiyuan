package com.shxy.suiyuanentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatsVO {
    
    @Schema(description = "总通知数")
    private Long totalCount;
    
    @Schema(description = "未读通知数")
    private Long unreadCount;
}
