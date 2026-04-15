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
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联user.id
     */
    private Long userId;

    /**
     * 资源ID，关联资源表
     */
    private Long resourceId;

    /**
     * 资源类型: post(帖子), lost_item(失物招领), kb_document(知识库文档)
     */
    private Object resourceType;

    /**
     * 收藏时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}