package com.shxy.suiyuancommon.enums;

import lombok.Getter;

/**
 * @author huang qi long
 * 社区帖子类型枚举
 * @since 2026/4/11 21:05
 */
@Getter
public enum CommunityPostTypeEnum {
    
    TECH_DISCUSSION(0, "技术讨论"),
    COURSE_QUESTION(1, "课程问题"),
    CAMPUS_LIFE(2, "校园生活"),
    OTHER(3, "其他");

    private final Integer code;
    private final String description;

    CommunityPostTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static CommunityPostTypeEnum fromCode(Integer code) {
        for (CommunityPostTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的帖子类型: " + code);
    }
}
