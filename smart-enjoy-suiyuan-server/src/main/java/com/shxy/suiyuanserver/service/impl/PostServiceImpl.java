package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuancommon.utils.RedisCacheUtil;
import com.shxy.suiyuanentity.dto.PostCreateDTO;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanentity.vo.PostVO;
import com.shxy.suiyuanserver.service.PostService;
import com.shxy.suiyuanserver.mapper.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author huang qi long
 * @description 针对表【post】的数据库操作Service实现
 * @createDate 2026-04-04 21:30:08
 */
@Slf4j
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {

    @Autowired
    private PostMapper postMapper;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisCacheUtil redisCacheUtil;



    public Result<PageResult> listPost(Integer page, Integer size, String sort, Integer type) {
        String cacheKey = RedisConstant.POST_LIST_KEY_PREFIX +
                page + ":" + size +
                ":" + (type != null ? type : "all") +
                ":" + (sort != null ? sort : "newest");

        PageResult pageResult = redisCacheUtil.queryWithPassThrough(
                cacheKey,
                PageResult.class,
                key -> {
                    LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(Post::getType, type);
                    if("hottest".equals(sort)){
                        queryWrapper.orderByDesc(Post::getLikeCount);
                    }else{
                        queryWrapper.orderByDesc(Post::getCreateTime);
                    }
                    Page<Post> postPage = new Page<>(page, size);
                    Page<Post> postList = postMapper.selectPage(postPage, queryWrapper);
                    return PageResult.builder()
                            .total(postList.getTotal())
                            .records(postList.getRecords())
                            .page(page)
                            .size(size)
                            .build();
                },
                RedisConstant.POST_LIST_TTL,
                TimeUnit.SECONDS
        );

        if (pageResult == null) {
            return Result.fail("获取帖子列表失败");
        }
        return Result.success(pageResult);
    }

    public Result<Post> publishPost(PostCreateDTO postCreateDTO) {
        Long currentUserId = BaseContext.getCurrentUserId();
        String imagesJson = null;
        if (postCreateDTO.getImages() != null || postCreateDTO.getImages().isEmpty()) {
            try {
                imagesJson = objectMapper.writeValueAsString(postCreateDTO.getImages());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }


        Post post = Post.builder()
                .userId(currentUserId)
                .title(postCreateDTO.getTitle())
                .content(postCreateDTO.getContent())
                .type(postCreateDTO.getType())
                .likeCount(0)
                .commentCount(0)
                .viewCount(0)
                .images(imagesJson)
                .createTime(new Date())
                .updateTime(new Date())
                .build();

        int insert = postMapper.insert(post);
        if (insert <= 0) {
            throw new BaseException("发布帖子失败");
        }

        clearPostListCache();

        return Result.success(post);
    }

    public Result<PostVO> getPostDetail(Long id) {
        if (id == null || id <= 0) {
            return Result.fail("ID不合法");
        }
        String cacheKey = RedisConstant.POST_DETAIL_KEY_PREFIX + id;
        
        // 使用工具类解决缓存穿透+击穿
        PostVO postVO = redisCacheUtil.queryWithMutex(
                cacheKey,
                PostVO.class,
                key -> {
                    List<PostVO> postVOList = postMapper.selectPostWithUser(id);
                    if (postVOList == null || postVOList.isEmpty()) {
                        return null; // 返回null会被工具类缓存空值
                    }
                    return postVOList.get(0);
                },
                RedisConstant.POST_DETAIL_TTL,
                TimeUnit.SECONDS
        );

        if (postVO == null) {
            throw new BaseException("帖子不存在");
        }
        return Result.success(postVO);
    }

    public Result<Post> likePost(Long id) {
        if (id == null || id <= 0) {
            throw new BaseException("帖子ID不合法");
        }

        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BaseException("帖子不存在");
        }

        String likeKey = RedisConstant.POST_LIKE_USERS_KEY_PREFIX + id;
        Long currentUserId = BaseContext.getCurrentUserId();

        // TODO 点赞并发安全
        // 检查是否已点赞
        Boolean isLiked = redisTemplate.opsForSet().isMember(likeKey, currentUserId);
        if (Boolean.TRUE.equals(isLiked)) {
            throw new BaseException("已经点赞过");
        }

        // 使用Redis原子操作增加点赞数
        redisTemplate.opsForValue().increment(RedisConstant.POST_LIKE_COUNT_KEY_PREFIX + id);
        redisTemplate.opsForSet().add(likeKey, currentUserId);
        redisTemplate.expire(likeKey, RedisConstant.POST_LIKE_TTL, TimeUnit.SECONDS);
        redisTemplate.expire(RedisConstant.POST_LIKE_COUNT_KEY_PREFIX + id, RedisConstant.POST_LIKE_TTL, TimeUnit.SECONDS);

        // 异步同步到数据库
        syncLikeCountToDatabase(id);

        // 清除缓存
        String detailKey = RedisConstant.POST_DETAIL_KEY_PREFIX + id;
        redisTemplate.delete(detailKey);
        clearPostListCache();

        post.setLikeCount(post.getLikeCount() + 1);
        return Result.success(post);
    }

    /**
     * 异步同步点赞数到数据库
     */
    private void syncLikeCountToDatabase(Long postId) {
        try {
            String countKey = RedisConstant.POST_LIKE_COUNT_KEY_PREFIX + postId;
            Object countObj = redisTemplate.opsForValue().get(countKey);
            if (countObj != null) {
                int likeCount = Integer.parseInt(countObj.toString());
                postMapper.update(null, new LambdaUpdateWrapper<>(Post.class)
                        .eq(Post::getId, postId)
                        .set(Post::getLikeCount, likeCount));
                log.debug("同步帖子{}的点赞数到数据库: {}", postId, likeCount);
            }
        } catch (Exception e) {
            log.error("同步点赞数失败, postId: {}", postId, e);
        }
    }

    private void clearPostListCache() {
        redisTemplate.delete(RedisConstant.POST_LIST_KEY_PREFIX + "*");
        log.info("清除帖子列表缓存");
    }
}




