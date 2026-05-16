package com.shxy.suiyuanentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "作者统计信息")
public class AuthorStatsVO {
    @Schema(description = "文章数")
    private Integer postCount;

    @Schema(description = "粉丝数")
    private Integer followerCount;
}
