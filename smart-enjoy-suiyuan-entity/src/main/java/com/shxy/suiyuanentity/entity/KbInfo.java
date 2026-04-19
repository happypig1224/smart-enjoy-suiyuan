package com.shxy.suiyuanentity.entity;

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
 * 知识库信息表
 * @TableName kb_info
 */
@TableName(value ="kb_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KbInfo {
    /**
     * 知识库ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 知识库名称
     */
    private String kbName;

    /**
     * 知识库描述
     */
    private String kbDescription;

    /**
     * 知识库分类
     */
    private String kbCategory;

    /**
     * 来源类型: manual-手动, document-文档, web-网页
     */
    private String sourceType;

    /**
     * 文档数量
     */
    private Integer documentCount;

    /**
     * 逻辑删除标记
     */
    private Integer isDeleted;

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