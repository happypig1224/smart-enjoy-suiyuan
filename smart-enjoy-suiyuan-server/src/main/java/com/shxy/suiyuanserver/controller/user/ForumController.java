package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.annotation.RequireLogin;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.dto.CommentDTO;
import com.shxy.suiyuanentity.dto.PostDTO;
import com.shxy.suiyuanentity.dto.PostUpdateDTO;
import com.shxy.suiyuanentity.entity.Comment;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanentity.vo.PostLikeStatusVO;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户论坛模块接口
 * @author Wu, Hui Ming
 * @version 2.0
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
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "keyword", required = false) String keyword
    )  {
        // 验证排序参数
        if (sort != null && !isValidSortField(sort)) {
            return Result.fail("无效的排序参数");
        }
        return postService.listPost(page, size, sort, type, keyword);
    }

    @PostMapping("post/publish")
    @RequireLogin
    @Operation(summary = "发布帖子", description = "用户发布新的论坛帖子")
    public Result<Post> publishPost(@Valid @RequestBody PostDTO postDTO) {
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
    @RequireLogin
    @Operation(summary = "点赞帖子", description = "用户对帖子进行点赞操作")
    public Result<Post> likePost(@PathVariable @NotNull Long id) {
        if (id <= 0) {
            return Result.fail("帖子ID无效");
        }
        return postService.likePost(id);
    }

    @DeleteMapping("post/like/{id}")
    @RequireLogin
    @Operation(summary = "取消点赞", description = "用户取消对帖子的点赞")
    public Result<Post> cancelLikePost(@PathVariable @NotNull Long id) {
        if (id <= 0) {
            return Result.fail("帖子ID无效");
        }
        return postService.cancelLikePost(id);
    }

    @DeleteMapping("post/{id}")
    @RequireLogin
    @Operation(summary = "删除帖子", description = "删除自己发布的帖子")
    public Result<String> deletePost(@PathVariable @NotNull Long id) {
        if (id <= 0) {
            return Result.fail("帖子ID无效");
        }
        return postService.deletePost(id);
    }

    @PutMapping("post")
    @RequireLogin
    @Operation(summary = "编辑帖子", description = "编辑自己发布的帖子")
    public Result<Post> updatePost(@Valid @RequestBody PostUpdateDTO postUpdateDTO) {
        return postService.updatePost(postUpdateDTO);
    }

    @PostMapping("comment/publish")
    @RequireLogin
    @Operation(summary = "发布评论", description = "用户对帖子或资源发布评论")
    public Result<Comment> publishComment(@Valid @RequestBody CommentDTO commentDTO) {
        return commentService.publishComment(commentDTO);
    }

    @GetMapping("comment/list")
    @Operation(summary = "获取评论列表", description = "分页获取评论列表，支持按帖子或资源筛选")
    public Result<PageResult> listComment(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(50) Integer size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "postId", required = false) Long postId,
            @RequestParam(value = "resourceId", required = false) Long resourceId
    ) {
        if (sort != null && !isValidSortField(sort)) {
            return Result.fail("无效的排序参数");
        }
        return commentService.listComment(page, size, sort, postId, resourceId);
    }


    @DeleteMapping("comment/delete/{id}")
    @RequireLogin
    @Operation(summary = "删除评论", description = "删除指定的评论")
    public Result<String> deleteComment(@PathVariable @NotNull Long id) {
        if (id <= 0) {
            return Result.fail("评论ID无效");
        }
        return commentService.deleteComment(id);
    }

    /**
     * 验证排序字段是否合法
     */
    private boolean isValidSortField(String sort) {
        return "newest".equals(sort) || "hottest".equals(sort) || "mostLiked".equals(sort);
    }

    @GetMapping("post/me/publish")
    @RequireLogin
    @Operation(summary = "我的帖子", description = "获取当前用户发布的帖子列表")
    public Result<java.util.List<PostVO>> getMyPublishedPosts() {
        return postService.getUserPublishedPosts(BaseContext.getCurrentUserId());
    }

    @PostMapping("post/upload/image")
    @RequireLogin
    @Operation(summary = "帖子图片上传", description = "用于帖子发布和编辑的图片上传，返回图片URL")
    public Result<String> uploadImage(@RequestParam("file") @NotNull(message = "上传文件不能为空") MultipartFile file) {
        String imageUrl = postService.uploadPostImage(file);
        return Result.success("上传成功", imageUrl);
    }

    @GetMapping("post/like/status/{id}")
    @RequireLogin
    @Operation(summary = "查询帖子点赞状态", description = "查询当前用户对指定帖子的点赞状态")
    public Result<PostLikeStatusVO> getPostLikeStatus(@PathVariable @NotNull Long id) {
        if (id <= 0) {
            return Result.fail("帖子ID无效");
        }
        return postService.getPostLikeStatus(id);
    }

}
