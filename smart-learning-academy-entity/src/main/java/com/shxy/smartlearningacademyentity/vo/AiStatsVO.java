package com.shxy.smartlearningacademyentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI使用统计VO")
public class AiStatsVO {

    @Schema(description = "总会话数")
    private Integer totalSessions;

    @Schema(description = "总消息数")
    private Integer totalMessages;

    @Schema(description = "总Token消耗")
    private Integer totalTokens;

    @Schema(description = "活跃用户数")
    private Integer activeUsers;

    @Schema(description = "知识库数量")
    private Integer kbCount;

    @Schema(description = "文档数量")
    private Integer documentCount;
}