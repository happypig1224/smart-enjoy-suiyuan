package com.shxy.suiyuanentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户关注VO
 * @author System Generated
 * @since 2026-04-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFollowVO {
    
    @Schema(description = "关注关系ID")
    private Long id;
    
    @Schema(description = "被关注者ID")
    private Long followeeId;
    
    @Schema(description = "被关注者用户名")
    private String followeeUserName;
    
    @Schema(description = "被关注者头像")
    private String followeeAvatar;
    
    @Schema(description = "被关注者手机号（脱敏）")
    private String followeePhone;
    
    @Schema(description = "关注时间")
    private Date createTime;
}
