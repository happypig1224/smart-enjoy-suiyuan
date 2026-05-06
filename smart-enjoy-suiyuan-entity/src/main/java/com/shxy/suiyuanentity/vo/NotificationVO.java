package com.shxy.suiyuanentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 通知VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationVO {
    
    @Schema(description = "通知ID")
    private Long id;
    
    @Schema(description = "通知类型: follow, post_favorite, resource_favorite, comment_reply")
    private String type;
    
    @Schema(description = "通知标题")
    private String title;
    
    @Schema(description = "通知内容")
    private String content;
    
    @Schema(description = "发送者ID")
    private Long userId;
    
    @Schema(description = "发送者用户名")
    private String userName;
    
    @Schema(description = "发送者头像")
    private String userAvatar;
    
    @Schema(description = "关联业务ID")
    private Long businessId;
    
    @Schema(description = "跳转链接")
    private String link;
    
    @Schema(description = "是否已读")
    private Boolean isRead;
    
    @Schema(description = "创建时间")
    private Date createTime;
}
