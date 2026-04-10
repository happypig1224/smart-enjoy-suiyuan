package com.shxy.smartlearningacademyentity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源收藏表
 * @TableName resource_favorite
 */
@TableName(value ="resource_favorite")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceFavorite {
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 资源 ID
     */
    private Long resourceId;

    /**
     * 收藏时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}