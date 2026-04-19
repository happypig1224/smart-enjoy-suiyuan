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
@Schema(description = "知识库信息VO")
public class KbInfoVO {

    @Schema(description = "知识库ID")
    private Long id;

    @Schema(description = "知识库名称")
    private String kbName;

    @Schema(description = "知识库描述")
    private String kbDescription;

    @Schema(description = "知识库分类")
    private String kbCategory;

    @Schema(description = "来源类型：manual-手动，document-文档，web-网页")
    private String sourceType;

    @Schema(description = "文档数量")
    private Integer documentCount;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}