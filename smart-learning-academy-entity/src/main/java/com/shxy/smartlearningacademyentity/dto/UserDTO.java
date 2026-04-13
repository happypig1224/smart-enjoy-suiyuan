package com.shxy.smartlearningacademyentity.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
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
public class UserDTO {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String userName;
    
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickName;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String userPassword;
    
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    private String newPassword;

    @NotBlank(message = "性别不能为空")
    private Integer userGender;
    
    private Integer userAge;
    
    @Size(max = 20, message = "年级长度不能超过20个字符")
    private String userGrade;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 6, message = "验证码长度不正确")
    private String code;

}
