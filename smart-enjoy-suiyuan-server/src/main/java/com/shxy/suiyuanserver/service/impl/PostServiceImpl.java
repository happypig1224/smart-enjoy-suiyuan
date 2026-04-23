package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuancommon.utils.RedisCacheUtil;
import com.shxy.suiyuanentity.dto.PostDTO;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanentity.vo.PostVO;
import com.shxy.suiyuanserver.service.PostService;
import com.shxy.suiyuanserver.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {

    private final PostMapper postMapper;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCacheUtil redisCacheUtil;

    public Result<PageResult> listPost(Integer page, Integer size, String sort, Integer type) {
        // 参数验证
        int validatedPage = (page == null || page < 1) ? 1 : page;
        int validatedSize = (size == null || size < 1) ? 10 : Math.min(size, 50); // 限制每页最大数量
        String validatedSort = sort;
        if (validatedSort != null && !isValidSortField(validatedSort)) {
            validatedSort = "newest"; // 默认排序
        }
        Integer validatedType = type;

        // 创建final变量以供lambda表达式使用
        final String finalValidatedSort = validatedSort;
        final Integer finalValidatedType = validatedType;
        final int finalValidatedPage = validatedPage;
        final int finalValidatedSize = validatedSize;

        String cacheKey = RedisConstant.POST_LIST_KEY_PREFIX +
                finalValidatedPage + ":" + finalValidatedSize +
                ":" + (finalValidatedType != null ? finalValidatedType : "all") +
                ":" + (finalValidatedSort != null ? finalValidatedSort : "newest");

        PageResult pageResult = redisCacheUtil.queryWithPassThrough(
                cacheKey,
                PageResult.class,
                key -> {
                    String orderBy = "newest";
                    if ("hottest".equals(finalValidatedSort)) {
                        orderBy = "hottest";
                    } else if ("mostLiked".equals(finalValidatedSort)) {
                        orderBy = "mostLiked";
                    }
                    int offset = (finalValidatedPage - 1) * finalValidatedSize;
                    List<PostVO> postVOList = postMapper.selectPostListWithUser(finalValidatedType, offset, finalValidatedSize, orderBy);
                    Long total = postMapper.selectPostCount(finalValidatedType);
                    return PageResult.builder()
                            .total(total != null ? total : 0)
                            .records(postVOList)
                            .page(finalValidatedPage)
                            .size(finalValidatedSize)
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
    
    /**
     * 验证排序字段是否合法
     */
    private boolean isValidSortField(String sort) {
        return "newest".equals(sort) || "hottest".equals(sort) || "mostLiked".equals(sort);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Post> publishPost(PostDTO postDTO) {
        // 验证输入数据
        validatePostData(postDTO);
        
        Long currentUserId = BaseContext.getCurrentUserId();
        String imagesJson = null;
        if (postDTO.getImages() != null && !postDTO.getImages().isEmpty()) {
            try {
                imagesJson = objectMapper.writeValueAsString(postDTO.getImages());
            } catch (JsonProcessingException e) {
                log.error("图片列表序列化失败", e);
                throw new BaseException("图片数据格式错误");
            }
        }

        Post post = Post.builder()
                .userId(currentUserId)
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .type(postDTO.getType())
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
    
    /**
     * 验证帖子数据的有效性
     */
    private void validatePostData(PostDTO postDTO) {
        if (postDTO.getTitle() == null || postDTO.getTitle().trim().length() == 0) {
            throw new BaseException("帖子标题不能为空");
        }
        if (postDTO.getTitle().length() > 100) {
            throw new BaseException("帖子标题长度不能超过100个字符");
        }
        if (postDTO.getContent() == null || postDTO.getContent().trim().length() == 0) {
            throw new BaseException("帖子内容不能为空");
        }
        if (postDTO.getContent().length() > 5000) {
            throw new BaseException("帖子内容长度不能超过5000个字符");
        }
        if (postDTO.getImages() != null && postDTO.getImages().size() > 10) {
            throw new BaseException("最多只能上传10张图片");
        }
        // 验证类型是否在有效范围内
        if (postDTO.getType() != null && (postDTO.getType() < 1 || postDTO.getType() > 10)) {
            throw new BaseException("帖子类型不合法");
        }
    }

    public Result<PostVO> getPostDetail(Long id) {
        if (id == null || id <= 0) {
            return Result.fail("ID不合法");
        }
        String cacheKey = RedisConstant.POST_DETAIL_KEY_PREFIX + id;

        PostVO postVO = redisCacheUtil.queryWithMutex(
                cacheKey,
                PostVO.class,
                key -> {
                    List<PostVO> postVOList = postMapper.selectPostWithUser(id);
                    if (postVOList == null || postVOList.isEmpty()) {
                        return null;
                    }
                    return postVOList.get(0);
                },
                RedisConstant.POST_DETAIL_TTL,
                TimeUnit.SECONDS
        );

        if (postVO == null) {
            throw new BaseException("帖子不存在");
        }

        Long currentUserId = BaseContext.getCurrentUserId();
        if (currentUserId != null && currentUserId > 0) {
            String likeKey = RedisConstant.POST_LIKE_USERS_KEY_PREFIX + id;
            Boolean isLiked = redisTemplate.opsForSet().isMember(likeKey, currentUserId);
            postVO.setIsLiked(Boolean.TRUE.equals(isLiked));
        } else {
            postVO.setIsLiked(false);
        }

        // 更新浏览量，使用LambdaUpdateWrapper防止SQL注入
        postMapper.update(null, new LambdaUpdateWrapper<>(Post.class)
                .eq(Post::getId, id)
                .setSql("view_count = view_count + 1"));

        return Result.success(postVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Post> likePost(Long id) {
        if (id == null || id <= 0) {
            throw new BaseException("帖子ID不合法");
        }

        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BaseException("帖子不存在");
        }

        // 使用分布式锁防止并发问题
        String lockKey = RedisConstant.POST_LIKE_LOCK_KEY_PREFIX + id + ":" + BaseContext.getCurrentUserId();
        String lockValue = UUID.randomUUID().toString();
        
        try {
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 10, TimeUnit.SECONDS);
            if (!lockAcquired) {
                throw new BaseException("操作过于频繁，请稍后再试");
            }

            String likeKey = RedisConstant.POST_LIKE_USERS_KEY_PREFIX + id;
            String countKey = RedisConstant.POST_LIKE_COUNT_KEY_PREFIX + id;
            Long currentUserId = BaseContext.getCurrentUserId();

            // 检查是否已点赞
            Boolean isLiked = redisTemplate.opsForSet().isMember(likeKey, currentUserId);
            if (Boolean.TRUE.equals(isLiked)) {
                throw new BaseException("已经点赞过");
            }

            Long addResult = redisTemplate.opsForSet().add(likeKey, currentUserId);
            if (addResult != null && addResult == 0) {
                throw new BaseException("已经点赞过");
            }

            Boolean countKeyExists = redisTemplate.hasKey(countKey);
            if (!Boolean.TRUE.equals(countKeyExists)) {
                redisTemplate.opsForValue().set(countKey, post.getLikeCount());
            }
            redisTemplate.opsForValue().increment(countKey);
            redisTemplate.expire(likeKey, RedisConstant.POST_LIKE_TTL, TimeUnit.SECONDS);
            redisTemplate.expire(countKey, RedisConstant.POST_LIKE_TTL, TimeUnit.SECONDS);

            syncLikeCountToDatabase(id);

            String detailKey = RedisConstant.POST_DETAIL_KEY_PREFIX + id;
            redisTemplate.delete(detailKey);
            clearPostListCacheByType(post.getType()); // 优化缓存清理

            post.setLikeCount(post.getLikeCount() + 1);
            return Result.success(post);
        } finally {
            // 使用Lua脚本安全删除锁
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            org.springframework.data.redis.core.script.DefaultRedisScript<Long> redisScript = new org.springframework.data.redis.core.script.DefaultRedisScript<>();
            redisScript.setScriptText(luaScript);
            redisScript.setResultType(Long.class);
            redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Post> cancelLikePost(Long id) {
        if (id == null || id <= 0) {
            throw new BaseException("帖子ID不合法");
        }

        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BaseException("帖子不存在");
        }

        // 使用分布式锁防止并发问题
        String lockKey = RedisConstant.POST_LIKE_LOCK_KEY_PREFIX + id + ":" + BaseContext.getCurrentUserId();
        String lockValue = UUID.randomUUID().toString();
        
        try {
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 10, TimeUnit.SECONDS);
            if (!lockAcquired) {
                throw new BaseException("操作过于频繁，请稍后再试");
            }

            String likeKey = RedisConstant.POST_LIKE_USERS_KEY_PREFIX + id;
            String countKey = RedisConstant.POST_LIKE_COUNT_KEY_PREFIX + id;
            Long currentUserId = BaseContext.getCurrentUserId();

            // 检查是否已点赞
            Boolean isLiked = redisTemplate.opsForSet().isMember(likeKey, currentUserId);
            if (!Boolean.TRUE.equals(isLiked)) {
                throw new BaseException("还未点赞，无法取消");
            }

            Long removeResult = redisTemplate.opsForSet().remove(likeKey, currentUserId);
            if (removeResult != null && removeResult == 0) {
                throw new BaseException("还未点赞，无法取消");
            }

            Boolean countKeyExists = redisTemplate.hasKey(countKey);
            if (!Boolean.TRUE.equals(countKeyExists)) {
                redisTemplate.opsForValue().set(countKey, post.getLikeCount());
            }
            redisTemplate.opsForValue().decrement(countKey);
            redisTemplate.expire(likeKey, RedisConstant.POST_LIKE_TTL, TimeUnit.SECONDS);
            redisTemplate.expire(countKey, RedisConstant.POST_LIKE_TTL, TimeUnit.SECONDS);

            syncLikeCountToDatabase(id);

            String detailKey = RedisConstant.POST_DETAIL_KEY_PREFIX + id;
            redisTemplate.delete(detailKey);
            clearPostListCacheByType(post.getType()); // 优化缓存清理

            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            return Result.success(post);
        } finally {
            // 使用Lua脚本安全删除锁
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            org.springframework.data.redis.core.script.DefaultRedisScript<Long> redisScript = new org.springframework.data.redis.core.script.DefaultRedisScript<>();
            redisScript.setScriptText(luaScript);
            redisScript.setResultType(Long.class);
            redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
        }
    }

    private void syncLikeCountToDatabase(Long postId) {
        try {
            String countKey = RedisConstant.POST_LIKE_COUNT_KEY_PREFIX + postId;
            Object countObj = redisTemplate.opsForValue().get(countKey);
            if (countObj != null) {
                int likeCount = Integer.parseInt(countObj.toString());
                likeCount = Math.max(0, likeCount);
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
        log.info("清除帖子列表缓存, 删除 {} 个key", keys.size());
    }
    
    /**
     * 根据帖子类型清理相关缓存，提高缓存清理效率
     */
    private void clearPostListCacheByType(Integer type) {
        Set<String> keys = new HashSet<>();
        String typeStr = type != null ? type.toString() : "all";
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(RedisConstant.POST_LIST_KEY_PREFIX + "*:" + typeStr + ":*")
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
        log.info("根据类型清除帖子列表缓存, 类型: {}, 删除 {} 个key", typeStr, keys.size());
    }
}