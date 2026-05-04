package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.PostDTO;
import com.shxy.suiyuanentity.dto.PostUpdateDTO;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanentity.vo.CreatorPostListVO;
import com.shxy.suiyuanentity.vo.CreatorStatsVO;
import com.shxy.suiyuanentity.vo.PostLikeStatusVO;
import com.shxy.suiyuanentity.vo.PostVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author huang qi long
* @description 针对表【post】的数据库操作Service
* @createDate 2026-04-04 21:30:08
*/
public interface PostService extends IService<Post> {


    Result<PageResult> listPost(Integer page, Integer size, String sort, Integer type);

    Result<Post> publishPost(PostDTO postDTO);

    Result<PostVO> getPostDetail(Long id);

    Result<Post> likePost(Long id);

    Result<Post> cancelLikePost(Long id);

    Result<String> deletePost(Long id);

    Result<Post> updatePost(PostUpdateDTO postUpdateDTO);

    /**
     * 获取用户发布的帖子列表
     * @param userId 用户ID
     * @return 帖子列表
     */
    Result<List<PostVO>> getUserPublishedPosts(Long userId);

    /**
     * 上传帖子图片
     * @param file 图片文件
     * @return 图片URL
     */
    String uploadPostImage(MultipartFile file);

    /**
     * 查询帖子点赞状态
     * @param postId 帖子ID
     * @return 点赞状态
     */
    Result<PostLikeStatusVO> getPostLikeStatus(Long postId);

    /**
     * 获取创作者中心统计数据
     * @param userId 用户ID
     * @return 统计数据
     */
    Result<CreatorStatsVO> getCreatorStats(Long userId);

    /**
     * 获取创作者帖子列表（内容管理）
     * @param userId 用户ID
     * @param status 状态筛选 null-全部, 0-草稿, 1-已发布, 2-已锁定, 3-审核中
     * @param page 页码
     * @param size 每页大小
     * @return 帖子列表
     */
    Result<PageResult> getCreatorPostList(Long userId, Integer status, Integer page, Integer size);

    /**
     * 发布帖子（公开方法，支持草稿和发布）
     * @param postDTO 帖子信息
     * @return 帖子
     */
    Result<Post> publishPostPublic(PostDTO postDTO);

    /**
     * 更新帖子
     * @param postUpdateDTO 帖子更新信息
     * @return 帖子
     */
    Result<Post> updatePostPublic(PostUpdateDTO postUpdateDTO);

    /**
     * 获取帖子详情（创作者中心使用，包含草稿）
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 帖子详情
     */
    Result<PostVO> getPostDetailForCreator(Long userId, Long postId);
}
