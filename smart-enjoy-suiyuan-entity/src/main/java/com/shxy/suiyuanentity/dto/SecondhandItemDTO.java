package com.shxy.suiyuanentity.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 二手商品发布DTO
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/28
 */
@Data
public class SecondhandItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID（更新时使用）
     */
    private Long id;

    /**
     * 商品标题
     */
    @NotBlank(message = "商品标题不能为空")
    @Size(min = 5, max = 100, message = "商品标题长度必须在5-100个字符之间")
    private String title;

    /**
     * 商品详细描述
     */
    @NotBlank(message = "商品描述不能为空")
    @Size(max = 2000, message = "商品描述不能超过2000个字符")
    private String description;

    /**
     * 商品分类
     */
    @NotBlank(message = "商品分类不能为空")
    private String category;

    /**
     * 二手价格
     */
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    @Digits(integer = 8, fraction = 2, message = "价格格式不正确")
    private BigDecimal price;

    /**
     * 原价
     */
    @DecimalMin(value = "0.01", message = "原价必须大于0")
    @Digits(integer = 8, fraction = 2, message = "价格格式不正确")
    private BigDecimal originalPrice;

    /**
     * 新旧程度: 1-全新, 2-95新, 3-9成新, 4-8成新, 5-7成新及以下
     */
    @NotNull(message = "新旧程度不能为空")
    @Min(value = 1, message = "新旧程度最小值为1")
    @Max(value = 5, message = "新旧程度最大值为5")
    private Integer conditionLevel;

    /**
     * 商品图片URL列表
     */
    private List<String> images;

    /**
     * 联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    /**
     * 联系微信
     */
    @Size(max = 50, message = "微信号不能超过50个字符")
    private String contactWechat;

    /**
     * 交易地点建议
     */
    @Size(max = 100, message = "交易地点不能超过100个字符")
    private String tradeLocation;
}
