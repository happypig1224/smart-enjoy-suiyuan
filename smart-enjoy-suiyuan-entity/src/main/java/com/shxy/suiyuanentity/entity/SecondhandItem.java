package com.shxy.suiyuanentity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 二手商品实体类
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("secondhand_item")
public class SecondhandItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品详细描述
     */
    private String description;

    /**
     * 商品分类
     */
    private String category;

    /**
     * 二手价格
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 新旧程度: 1-全新, 2-95新, 3-9成新, 4-8成新, 5-7成新及以下
     */
    private Integer conditionLevel;

    /**
     * 商品图片URL数组(JSON格式)
     */
    private String images;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系微信
     */
    private String contactWechat;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

    /**
     * 商品状态: 0-在售, 1-已售出, 2-已下架
     */
    private Integer status;

    /**
     * 交易地点建议
     */
    private String tradeLocation;

    /**
     * 发布时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}

