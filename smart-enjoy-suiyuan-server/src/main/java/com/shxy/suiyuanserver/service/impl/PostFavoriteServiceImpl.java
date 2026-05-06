package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanentity.entity.PostFavorite;
import com.shxy.suiyuanentity.vo.PostFavoriteStatusVO;
import com.shxy.suiyuanentity.vo.PostVO;
import com.shxy.suiyuanserver.mapper.PostFavoriteMapper;
import com.shxy.suiyuanserver.mapper.PostMapper;
import com.shxy.suiyuanserver.mapper.UserMapper;
import com.shxy.suiyuanserver.service.PostFavoriteService;
import com.shxy.suiyuanserver.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

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

        if (currentUserId.equals(post.getUserId())) {
            throw new BaseException("不能收藏自己的帖子");
        }

        PostFavorite existingFavorite = postFavoriteMapper.selectByPostIdAndUserId(postId, currentUserId);
        if (existingFavorite != null) {
            return Result.success("已经收藏过");
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

        String detailKey = RedisConstant.POST_DETAIL_KEY_PREFIX + postId;
        redisTemplate.delete(detailKey);

        Set<String> keys = new HashSet<>();
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(RedisConstant.POST_LIST_KEY_PREFIX + "*")
                .count(100)
                .build();
        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        sendFavoriteNotification(currentUserId, post.getUserId(), postId, post.getTitle());

        log.info("用户{}收藏帖子{}成功", currentUserId, postId);
        return Result.success("收藏成功");
    }

    /**
     * 发送帖子收藏通知
     */
    private void sendFavoriteNotification(Long fromUserId, Long toUserId, Long postId, String postTitle) {
        if (toUserId == null || toUserId.equals(fromUserId)) {
            return;
        }

        long delayScore = System.currentTimeMillis() + 5000;
        String safePostTitle = postTitle != null ? postTitle.replace("\"", "\\\"") : "未知帖子";

        String taskValue = String.format(
            "{\"type\":\"post_favorite\",\"from\":%d,\"to\":%d,\"targetId\":%d,\"postTitle\":\"%s\",\"time\":%d}",
            fromUserId, toUserId, postId, safePostTitle, System.currentTimeMillis()
        );

        stringRedisTemplate.opsForZSet().add(RedisConstant.NOTIFY_BUFFER_KEY, taskValue, delayScore);
        log.info("帖子收藏通知已加入缓冲区: {} -> {}, postId: {}", fromUserId, toUserId, postId);
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

        PostFavorite existingFavorite = postFavoriteMapper.selectByPostIdAndUserId(postId, currentUserId);
        if (existingFavorite == null) {
            return Result.success("还未收藏");
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    @Override
    public Result<PageResult> getUserFavoritePosts(Integer page, Integer size) {
        Long currentUserId = BaseContext.getCurrentUserId();
        if (currentUserId == null || currentUserId <= 0) {
            throw new BaseException("用户未登录");
        }

        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        Page<PostFavorite> favoritePage = postFavoriteMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<>(PostFavorite.class)
                        .eq(PostFavorite::getUserId, currentUserId)
                        .orderByDesc(PostFavorite::getCreateTime)
        );

        if (favoritePage.getRecords() == null || favoritePage.getRecords().isEmpty()) {
            return Result.success(PageResult.builder().total(0).records(Collections.emptyList()).page(page).size(size).build());
        }

        List<Long> postIds = favoritePage.getRecords().stream().map(PostFavorite::getPostId).toList();
        List<PostVO> postVOList = postMapper.selectPostListByIds(postIds);

        if (postVOList == null) {
            postVOList = Collections.emptyList();
        }

        return Result.success(PageResult.builder().total(favoritePage.getTotal()).records(postVOList).page(page).size(size).build());
    }
}
