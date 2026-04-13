package com.shxy.smartlearningacademyentity.vo;

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
@Schema(description = "知识库文档VO")
public class KbDocumentVO {

    @Schema(description = "文档ID")
    private Long id;

    @Schema(description = "所属知识库ID")
    private Long kbId;

    @Schema(description = "文档标题")
    private String docTitle;

    @Schema(description = "文档内容")
    private String docContent;

    @Schema(description = "文件存储路径")
    private String filePath;

    @Schema(description = "文件类型：txt, pdf, docx, md, html")
    private String fileType;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "向量化状态：0-未处理，1-处理中，2-已完成，-1-失败")
    private Integer vectorStatus;

    @Schema(description = "Milvus中的文档ID")
    private String milvusDocId;

    @Schema(description = "向量化错误信息")
    private String vectorErrorMsg;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}