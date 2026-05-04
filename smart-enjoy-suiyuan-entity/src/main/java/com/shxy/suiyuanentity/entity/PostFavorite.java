package com.shxy.suiyuanentity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 帖子收藏表
 * @TableName post_favorite
 */
@TableName(value = "post_favorite")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long postId;

    private Date createTime;
}
