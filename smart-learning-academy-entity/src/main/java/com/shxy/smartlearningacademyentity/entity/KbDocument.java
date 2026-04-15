package com.shxy.smartlearningacademyentity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库文档表
 * @TableName kb_document
 */
@TableName(value ="kb_document")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KbDocument {
    /**
     * 文档ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属知识库ID
     */
    private Long kbId;

    /**
     * 文档标题
     */
    private String docTitle;

    /**
     * 文档内容
     */
    private String docContent;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件类型: txt, pdf, docx, md, html
     */
    private String fileType;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 向量化状态: 0-未处理, 1-处理中, 2-已完成, -1-失败
     */
    private Integer vectorStatus;

    /**
     * Milvus中的文档ID
     */
    private String milvusDocId;

    /**
     * 向量化错误信息
     */
    private String vectorErrorMsg;

    /**
     * 状态: 1-启用, 0-禁用
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}