package com.shxy.suiyuancommon.enums;
import lombok.Getter;

/**
 * 紧急程度枚举
 * @author huang qi long
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/11 21:09
 */
@Getter
public enum UrgencyEnum {
    NORMAL(0, "普通"),
    URGENT(1, "紧急");

    private final Integer code;
    private final String description;

    UrgencyEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UrgencyEnum fromCode(Integer code) {
        for (UrgencyEnum urgency : values()) {
            if (urgency.getCode().equals(code)) {
                return urgency;
            }
        }
        throw new IllegalArgumentException("未知的紧急程度: " + code);
    }
}
