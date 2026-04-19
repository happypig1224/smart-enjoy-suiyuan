package com.shxy.suiyuanentity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息更新DTO
 * @author huang qi long
 * @since 2026/4/8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickName;
    
    @Min(value = 0, message = "性别值不正确")
    @Max(value = 2, message = "性别值不正确")
    private Integer userGender;
    
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 150, message = "年龄不合理")
    private Integer userAge;
    
    @Size(max = 20, message = "年级长度不能超过20个字符")
    private String userGrade;
    
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;
}
