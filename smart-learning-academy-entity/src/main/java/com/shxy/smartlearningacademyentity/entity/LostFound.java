package com.shxy.smartlearningacademyentity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 失物招领信息表
 * @TableName lost_found
 */
@TableName(value ="lost_found")
@Data
public class LostFound {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布人ID，关联user.id
     */
    private Long userId;

    /**
     * 帖子类型: 0-寻物启事, 1-招领启事
     */
    private Integer type;

    /**
     * 处理状态: 0-未解决, 1-已解决/已领取
     */
    private Integer status;

    /**
     * 标题，简述物品
     */
    private String title;

    /**
     * 详细描述，丢失/拾取经过
     */
    private String description;

    /**
     * 紧急程度: 0-普通, 1-紧急(置顶)
     */
    private Integer urgent;

    /**
     * 具体地点，如图书馆三楼
     */
    private String location;

    /**
     * 联系电话，可为空
     */
    private String phoneContact;

    /**
     * 联系微信号
     */
    private String wechatContact;

    /**
     * 图片列表，存储URL数组
     */
    private Object images;

    /**
     * 浏览量/查看次数
     */
    private Integer viewCount;

    /**
     * 发布时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}