package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.constant.RateLimitConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuancommon.utils.RateLimitUtil;
import com.shxy.suiyuancommon.utils.RedisCacheUtil;
import com.shxy.suiyuanentity.dto.CommentDTO;
import com.shxy.suiyuanentity.entity.Comment;
import com.shxy.suiyuanentity.vo.CommentVO;
import com.shxy.suiyuanserver.mapper.CommentMapper;
import com.shxy.suiyuanserver.mapper.PostMapper;
import com.shxy.suiyuanserver.mapper.ResourceMapper;
import com.shxy.suiyuanserver.service.CommentService;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.shxy.suiyuancommon.constant.LoggerName.COMMENT_SERVICE;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(COMMENT_SERVICE);

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final ResourceMapper resourceMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisCacheUtil redisCacheUtil;

    public CommentServiceImpl(CommentMapper commentMapper, PostMapper postMapper, ResourceMapper resourceMapper,
                              RedisTemplate<String, Object> redisTemplate,
                              StringRedisTemplate stringRedisTemplate,
                              RedisCacheUtil redisCacheUtil) {
        this.commentMapper = commentMapper;
        this.postMapper = postMapper;
        this.resourceMapper = resourceMapper;
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisCacheUtil = redisCacheUtil;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Comment> publishComment(CommentDTO commentDTO) {
        Long userId = BaseContext.getCurrentUserId();

        if (commentDTO == null || commentDTO.getContent() == null || commentDTO.getContent().trim().isEmpty()) {
            throw new BaseException("评论内容不能为空");
        }
        if (commentDTO.getPostId() == null && commentDTO.getResourceId() == null) {
            throw new BaseException("帖子ID或资源ID至少提供一个");
        }
        if (commentDTO.getContent().length() > 1000) {
            throw new BaseException("评论内容长度不能超过1000个字符");
        }

        String rateLimitKey = "comment:rate_limit:user:" + userId;
        RateLimitUtil.checkRateLimit(stringRedisTemplate, rateLimitKey,
                RateLimitConstant.COMMENT_TIME_WINDOW, RateLimitConstant.COMMENT_MAX_REQUESTS);

        Comment comment = Comment.builder()
                .userId(userId)
                .content(commentDTO.getContent())
                .postId(commentDTO.getPostId())
                .resourceId(commentDTO.getResourceId())
                .parentId(commentDTO.getParentId())
                .createTime(new Date())
                .updateTime(new Date())
                .build();

        int insert = commentMapper.insert(comment);
        if (insert <= 0) {
            return Result.fail("发布评论失败");
        }

        if (commentDTO.getPostId() != null) {
            postMapper.incrementCommentCount(commentDTO.getPostId());
        }

        // 发送评论回复通知
        sendCommentNotification(comment, commentDTO);

        clearCommentListCache(commentDTO.getPostId(), commentDTO.getResourceId());
        return Result.success(comment);
    }

    public Result<String> deleteComment(Long id) {
        Long currentUserId = BaseContext.getCurrentUserId();
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            log.error("评论{}不存在", id);
            return Result.fail("评论不存在");
        }
        if (!comment.getUserId().equals(currentUserId)) {
            throw new BaseException("无权删除他人的评论");
        }

        int count = commentMapper.updateIsDelete(id);
        if (count <= 0) {
            log.error("删除评论{}失败", id);
            return Result.fail("删除失败");
        }

        if (comment.getPostId() != null) {
            postMapper.decrementCommentCount(comment.getPostId());
        }

        clearCommentListCache(comment.getPostId(), comment.getResourceId());

        return Result.success("删除成功");
    }

    public Result<PageResult> listComment(Integer page, Integer size, String sort, Long postId, Long resourceId) {
        if (postId == null && resourceId == null) {
            log.error("帖子ID或资源ID至少提供一个");
            throw new BaseException("帖子ID或资源ID至少提供一个");
        }

        // 参数校验
        int validatedPage = (page == null || page < 1) ? 1 : page;
        int validatedSize = (size == null || size < 1) ? 10 : Math.min(size, 100); // 限制最大分页大小

        // 创建final变量以供lambda表达式使用
        final Long finalPostId = postId;
        final Long finalResourceId = resourceId;
        final int finalValidatedPage = validatedPage;
        final int finalValidatedSize = validatedSize;

        String targetPrefix;
        String targetId;
        if (postId != null) {
            targetPrefix = "post";
            targetId = String.valueOf(postId);
        } else {
            targetPrefix = "resource";
            targetId = String.valueOf(resourceId);
        }

        String cacheKey = buildCacheKey(targetPrefix, targetId, finalValidatedPage, finalValidatedSize);

        PageResult pageResult = redisCacheUtil.queryWithPassThrough(
                cacheKey,
                PageResult.class,
                key -> {
                    int offset = (finalValidatedPage - 1) * finalValidatedSize;
                    List<CommentVO> commentVOList = commentMapper.selectCommentListWithUser(finalPostId, finalResourceId, offset, finalValidatedSize, "createTime");
                    Long total = commentMapper.selectCommentCount(finalPostId, finalResourceId);

                    Long currentUserId = BaseContext.getCurrentUserId();
                    for (CommentVO vo : commentVOList) {
                        vo.setIsOwner(currentUserId != null && vo.getUserId().equals(currentUserId));
                    }

                    return PageResult.builder()
                            .total(total != null ? total : 0)
                            .page(finalValidatedPage)
                            .size(finalValidatedSize)
                            .records(commentVOList)
                            .build();
                },
                RedisConstant.COMMENT_LIST_TTL,
                TimeUnit.SECONDS
        );

        if (pageResult == null) {
            return Result.fail("获取评论列表失败");
        }
        return Result.success(pageResult);
    }

    private void clearCommentListCache(Long postId, Long resourceId) {
        Set<String> keysToDelete = new HashSet<>();

        if (postId != null) {
            scanAndCollect(RedisConstant.COMMENT_LIST_KEY_PREFIX + "post:" + postId + "*", keysToDelete);
        }
        if (resourceId != null) {
            scanAndCollect(RedisConstant.COMMENT_LIST_KEY_PREFIX + "resource:" + resourceId + "*", keysToDelete);
        }

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }
        log.info("清除评论列表缓存: postId={}, resourceId={}, 删除 {} 个key",
                postId, resourceId, keysToDelete.size());
    }

    private String buildCacheKey(String targetPrefix, String targetId, int page, int size) {
        String key = RedisConstant.COMMENT_LIST_KEY_PREFIX +
                targetPrefix + ":" + targetId +
                ":" + page + ":" + size;

        if (key.length() > 255) {
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
                byte[] hashBytes = md.digest(key.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) {
                    sb.append(String.format("%02x", b));
                }
                return RedisConstant.COMMENT_LIST_KEY_PREFIX + sb.toString();
            } catch (java.security.NoSuchAlgorithmException e) {
                log.error("生成MD5哈希失败", e);
                return RedisConstant.COMMENT_LIST_KEY_PREFIX +
                       key.substring(key.length() - 64);
            }
        }
        return key;
    }

    private void scanAndCollect(String pattern, Set<String> keys) {
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();
        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        } catch (Exception e) {
            log.error("扫描Redis缓存键时发生错误", e);
        }
    }

    /**
     * 发送评论回复通知
     */
    private void sendCommentNotification(Comment comment, CommentDTO commentDTO) {
        Long currentUserId = BaseContext.getCurrentUserId();
        Long receiverId = null;
        Long targetId = null; // 帖子ID或资源ID
        String targetType = null; // "post" or "resource"

        // 确定接收者
        if (commentDTO.getParentId() != null && commentDTO.getParentId() > 0) {
            // 回复某条评论，通知该评论的作者
            Comment parentComment = commentMapper.selectById(commentDTO.getParentId());
            if (parentComment != null) {
                receiverId = parentComment.getUserId();
            }
        } else {
            // 直接评论帖子或资源，通知作者
            if (commentDTO.getPostId() != null) {
                targetId = commentDTO.getPostId();
                targetType = "post";
                var post = postMapper.selectById(targetId);
                if (post != null) {
                    receiverId = post.getUserId();
                }
            } else if (commentDTO.getResourceId() != null) {
                targetId = commentDTO.getResourceId();
                targetType = "resource";
                var resource = resourceMapper.selectById(targetId);
                if (resource != null) {
                    receiverId = resource.getUserId();
                }
            }
        }

        // 不给自己发通知，且接收者必须存在
        if (receiverId == null || receiverId.equals(currentUserId)) {
            return;
        }

        // 构建通知任务
        long delayScore = System.currentTimeMillis() + 5000; // 延迟5秒
        String contentSnippet = comment.getContent().length() > 20 
                ? comment.getContent().substring(0, 20) 
                : comment.getContent();
        
        String taskValue = String.format(
            "{\"type\":\"comment_reply\",\"from\":%d,\"to\":%d,\"targetId\":%d,\"targetType\":\"%s\",\"content\":\"%s\",\"time\":%d}",
            currentUserId, receiverId, targetId != null ? targetId : 0, 
            targetType != null ? targetType : "unknown",
            contentSnippet.replace("\"", "\\\""), // 转义双引号
            System.currentTimeMillis()
        );

        stringRedisTemplate.opsForZSet().add(RedisConstant.NOTIFY_BUFFER_KEY, taskValue, delayScore);
        log.info("评论通知已加入缓冲区: {} -> {}, targetId: {}", currentUserId, receiverId, targetId);
    }
}
