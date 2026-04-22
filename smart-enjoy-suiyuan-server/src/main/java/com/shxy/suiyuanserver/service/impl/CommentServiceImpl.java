package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuancommon.utils.InputSanitizerUtil;
import com.shxy.suiyuancommon.utils.RedisCacheUtil;
import com.shxy.suiyuancommon.utils.SensitiveWordFilter;
import com.shxy.suiyuanentity.dto.CommentCreateDTO;
import com.shxy.suiyuanentity.entity.Comment;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanentity.vo.CommentVO;
import com.shxy.suiyuanserver.service.CommentService;
import com.shxy.suiyuanserver.mapper.CommentMapper;
import com.shxy.suiyuanserver.mapper.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.shxy.suiyuancommon.constant.LoggerName.COMMENT_SERVICE;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {
    private static final Logger log = LoggerFactory.getLogger(COMMENT_SERVICE);

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCacheUtil redisCacheUtil;
    private final SensitiveWordFilter sensitiveWordFilter;

    public CommentServiceImpl(CommentMapper commentMapper, 
                             PostMapper postMapper, 
                             RedisTemplate<String, Object> redisTemplate, 
                             RedisCacheUtil redisCacheUtil,
                             SensitiveWordFilter sensitiveWordFilter) {
        this.commentMapper = commentMapper;
        this.postMapper = postMapper;
        this.redisTemplate = redisTemplate;
        this.redisCacheUtil = redisCacheUtil;
        this.sensitiveWordFilter = sensitiveWordFilter;
    }

    @Override
    public Result<Comment> publishComment(CommentCreateDTO commentCreateDTO) {
        Long userId = BaseContext.getCurrentUserId();

        if (commentCreateDTO == null) {
            throw new BaseException("评论信息不能为空");
        }
        if (commentCreateDTO.getContent() == null || commentCreateDTO.getContent().trim().isEmpty()) {
            throw new BaseException("评论内容不能为空");
        }
        if (commentCreateDTO.getPostId() == null && commentCreateDTO.getLostItemId() == null && commentCreateDTO.getResourceId() == null) {
            throw new BaseException("帖子ID、失物招领ID或资源ID至少提供一个");
        }
        
        // 验证评论内容长度
        if (commentCreateDTO.getContent().length() > 1000) {
            throw new BaseException("评论内容长度不能超过1000个字符");
        }
        
        // 检查评论频率限制
        if (!checkCommentRateLimit(userId)) {
            return Result.fail("评论频率过高，请稍后再试");
        }
        
        // 净化输入内容，防止XSS攻击
        String content = InputSanitizerUtil.sanitizeHtml(commentCreateDTO.getContent());
        
        // 检查敏感词
        if (sensitiveWordFilter.containsSensitiveWords(content)) {
            return Result.fail("评论内容包含敏感词汇，请修改后重新提交");
        }
        
        // 过滤敏感词（如果需要）
        content = sensitiveWordFilter.filterSensitiveWords(content);
        
        Comment comment = Comment.builder()
                .userId(userId)
                .content(content)
                .postId(commentCreateDTO.getPostId())
                .lostItemId(commentCreateDTO.getLostItemId())
                .resourceId(commentCreateDTO.getResourceId())
                .parentId(commentCreateDTO.getParentId())
                .likeCount(0)
                .status(1)
                .createTime(new Date())
                .updateTime(new Date())
                .build();

        int insert = commentMapper.insert(comment);
        if (insert <= 0) {
            log.error("发布评论失败");
            return Result.fail("发布评论失败");
        }

        if (commentCreateDTO.getPostId() != null) {
            postMapper.incrementCommentCount(commentCreateDTO.getPostId());
        }

        clearCommentListCache(commentCreateDTO.getPostId(), commentCreateDTO.getLostItemId(), commentCreateDTO.getResourceId());

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

        clearCommentListCache(comment.getPostId(), comment.getLostItemId(), comment.getResourceId());

        return Result.success("删除成功");
    }

    public Result<PageResult> listComment(Integer page, Integer size, String sort, Long postId, Long lostItemId, Long resourceId) {
        if (postId == null && lostItemId == null && resourceId == null) {
            log.error("帖子ID、失物招领ID或资源ID至少提供一个");
            throw new BaseException("帖子ID、失物招领ID或资源ID至少提供一个");
        }

        // 参数校验
        int validatedPage = (page == null || page < 1) ? 1 : page;
        int validatedSize = (size == null || size < 1) ? 10 : Math.min(size, 100); // 限制最大分页大小
        
        // 验证排序参数
        String validatedSort = sort;
        if (validatedSort != null && !Arrays.asList("createTime", "likeCount", "hottest").contains(validatedSort)) {
            validatedSort = "createTime"; // 默认排序
        }

        // 创建final变量以供lambda表达式使用
        final Long finalPostId = postId;
        final Long finalLostItemId = lostItemId;
        final Long finalResourceId = resourceId;
        final String finalValidatedSort = validatedSort;
        final int finalValidatedPage = validatedPage;
        final int finalValidatedSize = validatedSize;

        String targetPrefix;
        String targetId;
        if (postId != null) {
            targetPrefix = "post";
            targetId = String.valueOf(postId);
        } else if (lostItemId != null) {
            targetPrefix = "lost";
            targetId = String.valueOf(lostItemId);
        } else {
            targetPrefix = "resource";
            targetId = String.valueOf(resourceId);
        }

        String cacheKey = buildCacheKey(targetPrefix, targetId, finalValidatedPage, finalValidatedSize, finalValidatedSort);

        PageResult pageResult = redisCacheUtil.queryWithPassThrough(
                cacheKey,
                PageResult.class,
                key -> {
                    String orderBy = "createTime";
                    if ("hottest".equals(finalValidatedSort) || "likeCount".equals(finalValidatedSort)) {
                        orderBy = "likeCount";
                    }
                    int offset = (finalValidatedPage - 1) * finalValidatedSize;
                    List<CommentVO> commentVOList = commentMapper.selectCommentListWithUser(finalPostId, finalLostItemId, finalResourceId, offset, finalValidatedSize, orderBy);
                    Long total = commentMapper.selectCommentCount(finalPostId, finalLostItemId, finalResourceId);

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

    private void clearCommentListCache(Long postId, Long lostItemId, Long resourceId) {
        Set<String> keysToDelete = new HashSet<>();

        if (postId != null) {
            scanAndCollect(RedisConstant.COMMENT_LIST_KEY_PREFIX + "post:" + postId + "*", keysToDelete);
        }
        if (lostItemId != null) {
            scanAndCollect(RedisConstant.COMMENT_LIST_KEY_PREFIX + "lost:" + lostItemId + "*", keysToDelete);
        }
        if (resourceId != null) {
            scanAndCollect(RedisConstant.COMMENT_LIST_KEY_PREFIX + "resource:" + resourceId + "*", keysToDelete);
        }

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }
        log.info("清除评论列表缓存: postId={}, lostItemId={}, resourceId={}, 删除 {} 个key",
                postId, lostItemId, resourceId, keysToDelete.size());
    }

    private String buildCacheKey(String targetPrefix, String targetId, int page, int size, String sort) {
        // 参数校验 - 由于参数已经是验证过的值，这里不再修改参数
        
        // 构造缓存键并限制总长度
        String key = RedisConstant.COMMENT_LIST_KEY_PREFIX +
                targetPrefix + ":" + targetId +
                ":" + page + ":" + size +
                ":" + (sort != null ? sort : "createTime");
        
        // 限制缓存键长度，防止过长的键值
        if (key.length() > 255) {
            // 使用哈希截断过长的键
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
                // 如果MD5失败，使用简单截断
                return RedisConstant.COMMENT_LIST_KEY_PREFIX + 
                       key.substring(key.length() - 64); // 取最后64个字符
            }
        }
        return key;
    }

    /**
     * 检查评论频率限制
     * @param userId 用户ID
     * @return 是否允许发布评论
     */
    private boolean checkCommentRateLimit(Long userId) {
        String rateLimitKey = "comment:rate_limit:user:" + userId;
        String currentMinute = String.valueOf(System.currentTimeMillis() / 60000); // 按分钟计算
        String rateLimitCurrentKey = rateLimitKey + ":" + currentMinute;
        
        Long currentCount = (Long) redisTemplate.opsForValue().get(rateLimitCurrentKey);
        if (currentCount == null) {
            redisTemplate.opsForValue().set(rateLimitCurrentKey, 1L, 60, TimeUnit.SECONDS);
            return true;
        } else if (currentCount >= 10) { // 每分钟最多10条评论
            return false;
        } else {
            redisTemplate.opsForValue().increment(rateLimitCurrentKey);
            return true;
        }
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
}