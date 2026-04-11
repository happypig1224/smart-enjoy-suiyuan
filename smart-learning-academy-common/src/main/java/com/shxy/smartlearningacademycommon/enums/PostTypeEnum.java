package com.shxy.smartlearningacademycommon.enums;

import lombok.Getter;
/**
 * 帖子类型枚举
 * @author huang qi long
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/11 21:05
 */
@Getter
public enum PostTypeEnum {
    LOST(0, "寻物启事"),
    FOUND(1, "招领启事");

    private final Integer code;
    private final String description;

    PostTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PostTypeEnum fromCode(Integer code) {
        for (PostTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的帖子类型: " + code);
    }
}
