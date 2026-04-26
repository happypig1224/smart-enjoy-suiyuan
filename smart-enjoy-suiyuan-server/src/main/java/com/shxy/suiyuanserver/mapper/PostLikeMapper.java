package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.entity.PostLike;
import org.apache.ibatis.annotations.Param;

/**
 * 针对表【post_like】的数据库操作Mapper
 */
public interface PostLikeMapper extends BaseMapper<PostLike> {

    /**
     * 根据帖子ID和用户ID查询点赞记录
     */
    PostLike selectByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 根据帖子ID统计点赞数
     */
    Long countByPostId(@Param("postId") Long postId);
}
