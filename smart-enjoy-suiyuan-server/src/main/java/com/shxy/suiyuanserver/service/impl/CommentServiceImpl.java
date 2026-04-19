package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuancommon.utils.RedisCacheUtil;
import com.shxy.suiyuanentity.dto.CommentCreateDTO;
import com.shxy.suiyuanentity.entity.Comment;
import com.shxy.suiyuanentity.vo.CommentVO;
import com.shxy.suiyuanserver.service.CommentService;
import com.shxy.suiyuanserver.mapper.CommentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author huang qi long
 * @description 针对表【comment】的数据库操作Service实现
 * @createDate 2026-04-04 21:30:08
 */
@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public Result<Comment> publishComment(CommentCreateDTO commentCreateDTO) {
        Long userId = BaseContext.getCurrentUserId();

        if (commentCreateDTO == null) {
            throw new BaseException("评论信息不能为空");
        }
        if (commentCreateDTO.getContent() == null || commentCreateDTO.getContent().trim().isEmpty()) {
            throw new BaseException("评论内容不能为空");
        }
        if (commentCreateDTO.getPostId() == null && commentCreateDTO.getLostItemId() == null) {
            throw new BaseException("帖子ID或失物招领ID至少提供一个");
        }
        Comment comment = Comment.builder()
                .userId(userId)
                .content(commentCreateDTO.getContent())
                .postId(commentCreateDTO.getPostId())
                .lostItemId(commentCreateDTO.getLostItemId())
                .parentId(commentCreateDTO.getParentId())
                .likeCount(0)
                .status(1)
                .build();

        int insert = commentMapper.insert(comment);
        if (insert <= 0) {
            return Result.fail("发布评论失败");
        }

        clearCommentListCache(commentCreateDTO.getPostId(), commentCreateDTO.getLostItemId());

        return Result.success(comment);
    }

    public Result<String> deleteComment(Long id) {
        // TODO 后续完善
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            return Result.fail("评论不存在");
        }
        int count = commentMapper.updateIsDelete(id);
        if (count <= 0) {
            return Result.fail("删除失败");
        }

        clearCommentListCache(comment.getPostId(), comment.getLostItemId());

        return Result.success("删除成功");
    }

    public Result<PageResult> listComment(Integer page, Integer size, String sort, Long postId, Long lostItemId) {
        if (postId == null && lostItemId == null) {
            throw new BaseException("帖子ID或失物招领ID至少提供一个");
        }

        String cacheKey = RedisConstant.COMMENT_LIST_KEY_PREFIX +
                page + ":" + size +
                ":" + (postId != null ? postId : "null") +
                ":" + (lostItemId != null ? lostItemId : "null") +
                ":" +
                ":" + (sort != null ? sort : "createTime");

        // 使用工具类解决缓存雪崩(随机过期时间)
        PageResult pageResult = redisCacheUtil.queryWithPassThrough(
                cacheKey,
                PageResult.class,
                key -> {
                    LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
                    if (postId != null) {
                        queryWrapper.eq(Comment::getPostId, postId);
                    }
                    if (lostItemId != null) {
                        queryWrapper.eq(Comment::getLostItemId, lostItemId);
                    }
                    if ("hottest".equals(sort)) {
                        queryWrapper.orderByDesc(Comment::getLikeCount);
                    } else {
                        queryWrapper.orderByDesc(Comment::getCreateTime);
                    }
                    Page<Comment> commentPage = new Page<>(page, size);
                    Page<Comment> result = commentMapper.selectPage(commentPage, queryWrapper);
                    return PageResult.builder()
                            .total(result.getTotal())
                            .page(page)
                            .size(size)
                            .records(result.getRecords())
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

    private void clearCommentListCache(Long postId, Long lostItemId) {
        if (postId != null) {
            redisTemplate.delete(RedisConstant.COMMENT_LIST_KEY_PREFIX + "post:" + postId + "*");
        }
        if (lostItemId != null) {
            redisTemplate.delete(RedisConstant.COMMENT_LIST_KEY_PREFIX + "lost:" + lostItemId + "*");
        }
        log.info("清除评论列表缓存: postId={}, lostItemId={}", postId, lostItemId);
    }
}




