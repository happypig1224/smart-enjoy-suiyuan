package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.entity.PostFavorite;
import org.apache.ibatis.annotations.Param;

/**
 * 针对表【post_favorite】的数据库操作Mapper
 */
public interface PostFavoriteMapper extends BaseMapper<PostFavorite> {

    PostFavorite selectByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    Long countByPostId(@Param("postId") Long postId);
}
