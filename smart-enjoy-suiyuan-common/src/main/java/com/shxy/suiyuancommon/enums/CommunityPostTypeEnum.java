package com.shxy.suiyuancommon.enums;

import lombok.Getter;

/**
 * @author huang qi long
 * 社区帖子类型枚举
 * @since 2026/4/11 21:05
 */
@Getter
public enum CommunityPostTypeEnum {
    
    STUDY_EXCHANGE(0, "学习交流"),
    KNOWLEDGE_NOTES(1, "干货笔记"),
    CAMPUS_LIFE(2, "校园日常"),
    PHOTOGRAPHY(3, "摄影大赏");

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
