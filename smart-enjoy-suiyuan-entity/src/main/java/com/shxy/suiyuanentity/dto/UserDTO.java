package com.shxy.suiyuanentity.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/6 20:25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
    
    /**
     * 手机号（用于登录和注册）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 密码（用于登录和注册）
     */
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String userPassword;
    
    /**
     * 新密码（用于重置密码）
     */
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    private String newPassword;
    
    /**
     * 验证码（用于注册和重置密码）
     */
    @Size(min = 4, max = 6, message = "验证码长度不正确")
    private String verifyCode;

}
