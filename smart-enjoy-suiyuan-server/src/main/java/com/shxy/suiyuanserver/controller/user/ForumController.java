package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.dto.CommentDTO;
import com.shxy.suiyuanentity.dto.PostDTO;
import com.shxy.suiyuanentity.entity.Comment;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanentity.vo.PostVO;
import com.shxy.suiyuanserver.service.CommentService;
import com.shxy.suiyuanserver.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author huang qi long
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4 21:00
 */


@RestController
@RequestMapping("/user/forum")
@Tag(name = "用户论坛模块")
public class ForumController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;


    @GetMapping("post/list")
    @Operation(summary = "获取帖子列表", description = "分页获取论坛帖子列表，支持排序和类型筛选")
    public Result<PageResult> listPost(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(50) Integer size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "type", required = false) Integer type
    )  {
        // 验证排序参数
        if (sort != null && !isValidSortField(sort)) {
            return Result.fail("无效的排序参数");
        }
        return postService.listPost(page, size, sort, type);
    }

    @PostMapping("post/publish")
    @Operation(summary = "发布帖子", description = "用户发布新的论坛帖子")
    public Result<Post> publishPost(@Valid @RequestBody PostDTO postDTO) {
        // 验证用户是否已登录
        if (BaseContext.getCurrentUserId() == null) {
            return Result.fail("用户未登录");
        }
        return postService.publishPost(postDTO);
    }

    @GetMapping("post/detail/{id}")
    @Operation(summary = "获取帖子详情", description = "根据ID获取帖子的详细信息")
    public Result<PostVO> getPostDetail(@PathVariable @NotNull Long id) {
        if (id <= 0) {
            return Result.fail("帖子ID无效");
        }
        return postService.getPostDetail(id);
    }

    @PostMapping("post/like/{id}")
    @Operation(summary = "点赞帖子", description = "用户对帖子进行点赞操作")
    public Result<Post> likePost(@PathVariable @NotNull Long id) {
        if (id <= 0) {
            return Result.fail("帖子ID无效");
        }
        if (BaseContext.getCurrentUserId() == null) {
            return Result.fail("用户未登录");
        }
        return postService.likePost(id);
    }

    @DeleteMapping("post/like/{id}")
    @Operation(summary = "取消点赞", description = "用户取消对帖子的点赞")
    public Result<Post> cancelLikePost(@PathVariable @NotNull Long id) {
        if (id <= 0) {
            return Result.fail("帖子ID无效");
        }
        if (BaseContext.getCurrentUserId() == null) {
            return Result.fail("用户未登录");
        }
        return postService.cancelLikePost(id);
    }

    @PostMapping("comment/publish")
    @Operation(summary = "发布评论", description = "用户对帖子或失物招领发布评论")
    public Result<Comment> publishComment(@Valid @RequestBody CommentDTO commentDTO) {
        if (BaseContext.getCurrentUserId() == null) {
            return Result.fail("用户未登录");
        }
        return commentService.publishComment(commentDTO);
    }

    @GetMapping("comment/list")
    @Operation(summary = "获取评论列表", description = "分页获取评论列表，支持按帖子、失物招领或资源筛选")
    public Result<PageResult> listComment(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(50) Integer size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "postId", required = false) Long postId,
            @RequestParam(value = "lostItemId", required = false) Long lostItemId,
            @RequestParam(value = "resourceId", required = false) Long resourceId
    ) {
        // 验证排序参数
        if (sort != null && !isValidSortField(sort)) {
            return Result.fail("无效的排序参数");
        }
        return commentService.listComment(page, size, sort, postId, lostItemId, resourceId);
    }


    @DeleteMapping("comment/delete/{id}")
    @Operation(summary = "删除评论", description = "删除指定的评论")
    public Result<String> deleteComment(@PathVariable @NotNull Long id) {
        if (id <= 0) {
            return Result.fail("评论ID无效");
        }
        if (BaseContext.getCurrentUserId() == null) {
            return Result.fail("用户未登录");
        }
        return commentService.deleteComment(id);
    }

    /**
     * 验证排序字段是否合法
     */
    private boolean isValidSortField(String sort) {
        return "newest".equals(sort) || "hottest".equals(sort) || "mostLiked".equals(sort);
    }

}
