package com.shxy.suiyuanentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户统计信息VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户统计信息")
public class UserStatsVO {
    @Schema(description = "关注数")
    private Integer followingCount;

    @Schema(description = "收藏数")
    private Integer favoriteCount;
}
