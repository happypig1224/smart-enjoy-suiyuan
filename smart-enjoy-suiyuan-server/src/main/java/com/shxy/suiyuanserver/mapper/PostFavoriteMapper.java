package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.entity.PostFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 针对表【post_favorite】的数据库操作Mapper
 */
@Mapper
public interface PostFavoriteMapper extends BaseMapper<PostFavorite> {

    PostFavorite selectByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    Long countByPostId(@Param("postId") Long postId);

    /**
     * 统计用户收藏的帖子数
     */
    @Select("SELECT COUNT(*) FROM post_favorite WHERE user_id = #{userId}")
    Integer countByUserId(@Param("userId") Long userId);
}
