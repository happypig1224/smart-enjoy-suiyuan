package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanentity.entity.PostFavorite;
import com.shxy.suiyuanentity.vo.PostFavoriteStatusVO;
import com.shxy.suiyuanentity.vo.PostVO;
import com.shxy.suiyuanserver.mapper.PostFavoriteMapper;
import com.shxy.suiyuanserver.mapper.PostMapper;
import com.shxy.suiyuanserver.service.PostFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 帖子收藏服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostFavoriteServiceImpl extends ServiceImpl<PostFavoriteMapper, PostFavorite>
        implements PostFavoriteService {

    private final PostFavoriteMapper postFavoriteMapper;
    private final PostMapper postMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> favoritePost(Long postId) {
        if (postId == null || postId <= 0) {
            throw new BaseException("帖子ID不合法");
        }

        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new BaseException("帖子不存在");
        }

        Long currentUserId = BaseContext.getCurrentUserId();

        // 检查是否已收藏
        PostFavorite existingFavorite = postFavoriteMapper.selectByPostIdAndUserId(postId, currentUserId);
        if (existingFavorite != null) {
            throw new BaseException("已经收藏过");
        }

        // 创建收藏记录
        PostFavorite postFavorite = PostFavorite.builder()
                .postId(postId)
                .userId(currentUserId)
                .createTime(new Date())
                .build();
        int insert = postFavoriteMapper.insert(postFavorite);
        if (insert <= 0) {
            throw new BaseException("收藏失败");
        }

        // 清除帖子详情缓存
        String detailKey = RedisConstant.POST_DETAIL_KEY_PREFIX + postId;
        redisTemplate.delete(detailKey);

        log.info("用户{}收藏帖子{}成功", currentUserId, postId);
        return Result.success("收藏成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelFavoritePost(Long postId) {
        if (postId == null || postId <= 0) {
            throw new BaseException("帖子ID不合法");
        }

        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BaseException("帖子不存在");
        }

        Long currentUserId = BaseContext.getCurrentUserId();

        // 检查是否已收藏
        PostFavorite existingFavorite = postFavoriteMapper.selectByPostIdAndUserId(postId, currentUserId);
        if (existingFavorite == null) {
            throw new BaseException("还未收藏，无法取消");
        }

        // 删除收藏记录
        int delete = postFavoriteMapper.deleteById(existingFavorite.getId());
        if (delete <= 0) {
            throw new BaseException("取消收藏失败");
        }

        // 清除帖子详情缓存
        String detailKey = RedisConstant.POST_DETAIL_KEY_PREFIX + postId;
        redisTemplate.delete(detailKey);

        log.info("用户{}取消收藏帖子{}成功", currentUserId, postId);
        return Result.success("取消收藏成功");
    }

    @Override
    public Result<PostFavoriteStatusVO> getPostFavoriteStatus(Long postId) {
        if (postId == null || postId <= 0) {
            throw new BaseException("帖子ID不合法");
        }

        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BaseException("帖子不存在");
        }

        Long currentUserId = BaseContext.getCurrentUserId();
        boolean isFavorited = false;
        if (currentUserId != null && currentUserId > 0) {
            PostFavorite postFavorite = postFavoriteMapper.selectByPostIdAndUserId(postId, currentUserId);
            isFavorited = postFavorite != null;
        }

        PostFavoriteStatusVO statusVO = PostFavoriteStatusVO.builder()
                .postId(postId)
                .isFavorited(isFavorited)
                .build();

        return Result.success(statusVO);
    }

    @Override
    public Result<List<PostVO>> getUserFavoritePosts() {
        Long currentUserId = BaseContext.getCurrentUserId();
        if (currentUserId == null || currentUserId <= 0) {
            throw new BaseException("用户未登录");
        }

        // 查询用户收藏的所有帖子ID
        List<PostFavorite> favorites = postFavoriteMapper.selectList(
                new LambdaQueryWrapper<>(PostFavorite.class)
                        .eq(PostFavorite::getUserId, currentUserId)
                        .orderByDesc(PostFavorite::getCreateTime)
        );

        if (favorites == null || favorites.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 根据帖子ID列表查询帖子详情
        List<Long> postIds = favorites.stream().map(PostFavorite::getPostId).toList();
        List<PostVO> postVOList = postMapper.selectPostListByIds(postIds);

        if (postVOList == null) {
            return Result.success(Collections.emptyList());
        }

        return Result.success(postVOList);
    }
}
