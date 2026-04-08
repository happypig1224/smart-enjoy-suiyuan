package com.shxy.smartlearningacademyentity.dto;

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
    private String nickName;
    private Integer userGender;
    private Integer userAge;
    private String userGrade;
    private String avatar;
}
