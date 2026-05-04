package com.shxy.suiyuanentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 资源 VO 对象
 * @author Wu, Hui Ming
 * @version 1.0
 * @since 2026/4/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "资源视图对象")
public class ResourceVO {
    
    /**
     * 资源 ID
     */
    @Schema(description = "资源ID")
    private Long id;
    
    /**
     * 上传者 ID
     */
    @Schema(description = "上传者ID")
    private Long userId;
    
    /**
     * 上传者昵称
     */
    @Schema(description = "上传者昵称")
    private String userNickName;
    
    /**
     * 上传者头像
     */
    @Schema(description = "上传者头像")
    private String userAvatar;
    
    /**
     * 资源标题
     */
    @Schema(description = "资源标题")
    private String title;
    
    /**
     * 资源类型：image, pdf, doc, txt, md
     */
    @Schema(description = "资源类型：image, pdf, doc, txt, md")
    private String type;
    
    /**
     * 学科分类 ID
     */
    @Schema(description = "学科分类ID")
    private Integer subject;
    
    /**
     * 资源存储路径
     */
    @Schema(description = "资源存储路径")
    private String resourceUrl;
    
    /**
     * 原始文件名
     */
    @Schema(description = "原始文件名")
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小（字节）")
    private Long fileSize;
    
    /**
     * 资源描述
     */
    @Schema(description = "资源描述")
    private String description;
    
    /**
     * 下载次数
     */
    @Schema(description = "下载次数")
    private Integer downloadCount;
    
    /**
     * 是否已收藏：true-已收藏，false-未收藏
     */
    @Schema(description = "是否已收藏：true-已收藏，false-未收藏")
    private Boolean isFavorite;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;
}
