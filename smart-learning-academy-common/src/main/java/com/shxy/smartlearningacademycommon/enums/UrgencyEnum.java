package com.shxy.smartlearningacademycommon.enums;
import lombok.Getter;

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
