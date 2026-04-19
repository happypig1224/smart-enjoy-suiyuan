package com.shxy.suiyuanentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "失物招领视图对象")
public class LostFoundVO {
    
    @Schema(description = "失物招领ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户昵称")
    private String userNickName;
    
    @Schema(description = "用户头像")
    private String userAvatar;
    
    @Schema(description = "类型：0-寻物启事，1-招领启事")
    private Integer type;
    
    @Schema(description = "类型名称")
    private String typeName;
    
    @Schema(description = "状态：0-进行中，1-已完成")
    private Integer status;
    
    @Schema(description = "状态名称")
    private String statusName;
    
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
    
    @Schema(description = "浏览数")
    private Integer viewCount;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新时间")
    private Date updateTime;
}
