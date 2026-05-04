package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanentity.vo.CreatorPostListVO;
import com.shxy.suiyuanentity.vo.CreatorStatsVO;
import com.shxy.suiyuanentity.vo.PostVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Wu, Hui Ming
* @description 针对表【post】的数据库操作Mapper
* @createDate 2026-04-04 21:30:08
* @Entity com.shxy.entity.Post
*/
public interface PostMapper extends BaseMapper<Post> {
    List<PostVO> selectPostWithUser(@Param("postId") Long postId);

    List<PostVO> selectPostListWithUser(@Param("type") Integer type,
                                        @Param("offset") int offset,
                                        @Param("size") int size,
                                        @Param("orderBy") String orderBy);

    Long selectPostCount(@Param("type") Integer type);
    
    /**
     * 增加帖子评论数
     */
    int incrementCommentCount(@Param("postId") Long postId);
    
    /**
     * 减少帖子评论数，确保不低于0
     */
    int decrementCommentCount(@Param("postId") Long postId);

    /**
     * 根据用户ID查询帖子列表
     */
    List<PostVO> selectPostListByUserId(@Param("userId") Long userId);

    /**
     * 查询创作者统计数据
     */
    CreatorStatsVO selectCreatorStats(@Param("userId") Long userId);

    /**
     * 查询创作者帖子列表（内容管理）
     */
    List<CreatorPostListVO> selectCreatorPostList(@Param("userId") Long userId,
                                                   @Param("status") Integer status,
                                                   @Param("offset") int offset,
                                                   @Param("size") int size);

    /**
     * 查询创作者帖子总数
     */
    Long selectCreatorPostListCount(@Param("userId") Long userId,
                                     @Param("status") Integer status);

    /**
     * 查询帖子详情（创作者中心使用，包含草稿）
     */
    List<PostVO> selectPostWithUserForCreator(@Param("postId") Long postId, @Param("userId") Long userId);
}




