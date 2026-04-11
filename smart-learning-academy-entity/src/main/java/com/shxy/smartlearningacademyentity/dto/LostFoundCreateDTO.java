package com.shxy.smartlearningacademyentity.dto;

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
    
    @Schema(description = "类型：0-寻物启事，1-招领启事")
    private Integer type;
    
    @Schema(description = "标题")
    private String title;
    
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "是否紧急：0-否，1-是")
    private Integer urgent;
    
    @Schema(description = "位置")
    private String location;
    
    @Schema(description = "联系电话")
    private String phoneContact;
    
    @Schema(description = "微信联系方式")
    private String wechatContact;
    
    @Schema(description = "图片列表")
    private List<String> images;
}
