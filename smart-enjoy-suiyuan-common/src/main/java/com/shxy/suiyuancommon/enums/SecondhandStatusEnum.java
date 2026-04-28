package com.shxy.suiyuancommon.enums;

import lombok.Getter;

/**
 * 二手商品状态枚举
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/28
 */
@Getter
public enum SecondhandStatusEnum {
    ON_SALE(0, "在售"),
    SOLD_OUT(1, "已售出"),
    OFF_SHELF(2, "已下架");

    private final Integer code;
    private final String description;

    SecondhandStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SecondhandStatusEnum fromCode(Integer code) {
        for (SecondhandStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的商品状态: " + code);
    }
}
