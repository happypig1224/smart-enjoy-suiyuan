package com.shxy.suiyuanentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户公开信息VO（他人主页，不含敏感信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户公开信息")
public class UserProfileVO {
    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "注册时间")
    private Date createTime;

    @Schema(description = "关注数")
    private Integer followingCount;

    @Schema(description = "粉丝数")
    private Integer followersCount;
}
