package com.shxy.smartlearningacademyentity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "失物招领创建数据传输对象")
public class LostFoundCreateDTO {
    
    @NotNull(message = "类型不能为空")
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
    
    @Size(max = 100, message = "位置长度不能超过100个字符")
    @Schema(description = "位置")
    private String location;
    
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    @Schema(description = "联系电话")
    private String phoneContact;
    
    @Size(max = 50, message = "微信号长度不能超过50个字符")
    @Schema(description = "微信联系方式")
    private String wechatContact;
    
    @Schema(description = "图片列表")
    private List<String> images;
}
