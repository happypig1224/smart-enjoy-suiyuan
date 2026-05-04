package com.shxy.suiyuanentity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源创建 DTO
 * @author Wu, Hui Ming
 * @version 1.0
 * @since 2026/4/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "资源创建数据传输对象")
public class ResourceDTO {
    
    /**
     * 资源标题
     */
    @NotBlank(message = "资源标题不能为空")
    @Size(max = 100, message = "资源标题长度不能超过100个字符")
    @Schema(description = "资源标题")
    private String title;
    
    /**
     * 资源类型：image, pdf, doc, txt, md
     */
    @NotBlank(message = "资源类型不能为空")
    @Schema(description = "资源类型：image, pdf, doc, txt, md")
    private String type;
    
    /**
     * 学科分类 ID
     */
    @Schema(description = "学科分类ID")
    private Integer subject;
    
    /**
     * 资源描述
     */
    @Size(max = 500, message = "资源描述长度不能超过500个字符")
    @Schema(description = "资源描述")
    private String description;
}
