package com.shxy.suiyuancommon.enums;
import lombok.Getter;

/**
 * @author huang qi long
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/7 12:47
 */
@Getter
public enum UserGenderEnum {
    UNKNOWN(0, "未填写"),
    MALE(1, "男"),
    FEMALE(2, "女");

    private final Integer code;
    private final String description;

    UserGenderEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UserGenderEnum fromCode(Integer code) {
        for (UserGenderEnum gender : values()) {
            if (gender.getCode().equals(code)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("未知的性别类型: " + code);
    }
}

