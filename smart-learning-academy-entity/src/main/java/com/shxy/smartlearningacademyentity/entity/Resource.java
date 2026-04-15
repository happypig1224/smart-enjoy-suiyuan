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
 * 学习资源表
 * @TableName resource
 */
@TableName(value ="resource")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Resource {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 上传者ID，关联user.id
     */
    private Long userId;

    /**
     * 资源格式: image, pdf, doc, txt, md
     */
    private String type;

    /**
     * 所属学科分类ID
     */
    private Integer subject;

    /**
     * 文件在COS/服务器的存储路径
     */
    private String resourceUrl;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件大小，单位字节(B)
     */
    private Long fileSize;

    /**
     * 资源简介或备注
     */
    private String description;

    /**
     * 累计下载次数
     */
    private Integer downloadCount;

    /**
     * 上传时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}