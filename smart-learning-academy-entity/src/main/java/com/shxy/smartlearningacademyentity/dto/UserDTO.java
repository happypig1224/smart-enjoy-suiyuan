package com.shxy.smartlearningacademyentity.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String userName;
    private String nickName;
    private String userPassword;
    private String newPassword;
    private Integer userGender;
    private Integer userAge;
    private String userGrade;
    private String phone;
    private String code;

}
