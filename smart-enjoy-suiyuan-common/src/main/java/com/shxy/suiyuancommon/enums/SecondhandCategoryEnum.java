package com.shxy.suiyuancommon.enums;

import lombok.Getter;

/**
 * 二手商品分类枚举
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/28
 */
@Getter
public enum SecondhandCategoryEnum {
    ELECTRONICS("electronics", "数码产品"),
    BOOKS("books", "书籍教材"),
    DAILY("daily", "生活用品"),
    SPORTS("sports", "运动健身"),
    CLOTHES("clothes", "服装鞋帽"),
    OTHER("other", "其他");

    private final String code;
    private final String description;

    SecondhandCategoryEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SecondhandCategoryEnum fromCode(String code) {
        for (SecondhandCategoryEnum category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("未知的商品分类: " + code);
    }
}
