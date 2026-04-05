package com.shxy.smartlearningacademyentity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
    private Long id;
    private String nickName;
    private String userName;
    private String userPassword;
    private Integer userGender;
    private Integer userAge;
    private String userGrade;
    private String avatar;
    private String phone;
    private Integer role;
    private Date createTime;
}
