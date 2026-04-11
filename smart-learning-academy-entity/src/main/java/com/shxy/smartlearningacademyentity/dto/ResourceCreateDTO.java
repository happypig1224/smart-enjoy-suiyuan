package com.shxy.smartlearningacademyentity.dto;

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
public class ResourceCreateDTO {
    
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
     * 资源描述
     */
    @Schema(description = "资源描述")
    private String description;
}
