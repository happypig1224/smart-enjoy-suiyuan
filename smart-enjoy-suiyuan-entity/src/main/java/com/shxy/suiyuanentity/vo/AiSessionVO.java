package com.shxy.suiyuanentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI会话VO")
public class AiSessionVO {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "会话名称")
    private String sessionName;

    @Schema(description = "AI模型")
    private String aiModel;

    @Schema(description = "Token消耗")
    private Integer tokenCount;

    @Schema(description = "消息数")
    private Integer messageCount;

    @Schema(description = "状态：1-活跃，0-归档，-1-删除")
    private Integer status;

    @Schema(description = "最后一条消息摘要")
    private String lastMessage;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}