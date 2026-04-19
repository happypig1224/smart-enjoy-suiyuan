package com.shxy.suiyuanentity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "失物招领更新数据传输对象")
public class LostFoundUpdateDTO {
    
    @NotNull(message = "失物招领ID不能为空")
    @Schema(description = "失物招领ID")
    private Long id;
    
    @Schema(description = "类型：0-寻物启事，1-招领启事")
    private Integer type;
    
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 100, message = "标题长度必须在1-100个字符之间")
    @Schema(description = "标题")
    private String title;
    
    @NotBlank(message = "描述不能为空")
    @Size(min = 1, max = 2000, message = "描述长度必须在1-2000个字符之间")
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "是否紧急：0-否，1-是")
    private Integer urgent;
}
