package com.shxy.smartlearningacademyentity.dto;

import com.shxy.smartlearningacademyentity.entity.User;
import com.shxy.smartlearningacademyentity.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/5 22:50
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO implements Serializable {
    private String token;
    private UserVO user;
}


