package com.shxy.smartlearningacademyentity.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterRequestDTO {

    private String userName;
    private String userPassword;
    private String phone;
    private String code;

}
