package com.shxy.suiyuanentity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/5 22:51
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    @Schema(description = "用户ID")
    private Long id;
    
    @Schema(description = "用户名")
    private String userName;
    
    @Schema(description = "头像")
    private String avatar;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "角色")
    private Integer role;
    
    @Schema(description = "创建时间")
    private Date createTime;
}
