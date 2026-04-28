package com.shxy.suiyuanentity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 二手商品VO
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "二手商品视图对象")
public class SecondhandItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @Schema(description = "商品ID")
    private Long id;

    /**
     * 卖家ID
     */
    @Schema(description = "卖家ID")
    private Long sellerId;

    /**
     * 卖家昵称
     */
    @Schema(description = "卖家昵称")
    private String sellerNickName;

    /**
     * 卖家头像
     */
    @Schema(description = "卖家头像")
    private String sellerAvatar;

    /**
     * 商品标题
     */
    @Schema(description = "商品标题")
    private String title;

    /**
     * 商品详细描述
     */
    @Schema(description = "商品详细描述")
    private String description;

    /**
     * 商品分类
     */
    @Schema(description = "商品分类")
    private String category;

    /**
     * 商品分类描述
     */
    @Schema(description = "商品分类描述")
    private String categoryDescription;

    /**
     * 二手价格
     */
    @Schema(description = "二手价格")
    private BigDecimal price;

    /**
     * 原价
     */
    @Schema(description = "原价")
    private BigDecimal originalPrice;

    /**
     * 新旧程度: 1-全新, 2-95新, 3-9成新, 4-8成新, 5-7成新及以下
     */
    @Schema(description = "新旧程度")
    private Integer conditionLevel;

    /**
     * 新旧程度描述
     */
    @Schema(description = "新旧程度描述")
    private String conditionDescription;

    /**
     * 商品图片URL列表
     */
    @Schema(description = "商品图片URL列表")
    private List<String> images;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String contactPhone;

    /**
     * 联系微信
     */
    @Schema(description = "联系微信")
    private String contactWechat;

    /**
     * 浏览次数
     */
    @Schema(description = "浏览次数")
    private Integer viewCount;

    /**
     * 收藏次数
     */
    @Schema(description = "收藏次数")
    private Integer favoriteCount;

    /**
     * 商品状态: 0-在售, 1-已售出, 2-已下架
     */
    @Schema(description = "商品状态")
    private Integer status;

    /**
     * 商品状态描述
     */
    @Schema(description = "商品状态描述")
    private String statusDescription;

    /**
     * 交易地点建议
     */
    @Schema(description = "交易地点建议")
    private String tradeLocation;

    /**
     * 是否已收藏
     */
    @Schema(description = "是否已收藏")
    private Boolean isFavorite;

    /**
     * 发布时间
     */
    @Schema(description = "发布时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
