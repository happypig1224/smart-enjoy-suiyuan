package com.shxy.suiyuancommon.enums;
import lombok.Getter;


/**
 * @author huang qi long
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/7 12:47
 */
@Getter
public enum UserRoleEnum {
    USER(1, "普通用户"),
    ADMIN(2, "管理员");

    private final Integer code;
    private final String description;

    UserRoleEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UserRoleEnum fromCode(Integer code) {
        for (UserRoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知的角色类型: " + code);
    }
}
