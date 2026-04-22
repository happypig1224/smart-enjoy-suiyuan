package com.shxy.suiyuanentity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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
@Schema(description = "帖子视图对象")
public class PostVO {
    
    @Schema(description = "帖子ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户昵称")
    private String userNickName;
    
    @Schema(description = "用户头像")
    private String userAvatar;
    
    @Schema(description = "帖子标题")
    private String title;
    
    @Schema(description = "帖子内容")
    private String content;
    
    @Schema(description = "帖子类型")
    private Integer type;
    
    @Schema(description = "类型名称")
    private String typeName;
    
    @Schema(description = "点赞数")
    private Integer likeCount;
    
    @Schema(description = "评论数")
    private Integer commentCount;
    
    @Schema(description = "浏览数")
    private Integer viewCount;
    
    @Schema(description = "图片列表")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;
}
