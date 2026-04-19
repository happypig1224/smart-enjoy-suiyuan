package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.CommentCreateDTO;
import com.shxy.suiyuanentity.dto.PostCreateDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
        return postService.listPost(page, size, sort, type);
    }

    @PostMapping("post/publish")
    @Operation(summary = "发布帖子", description = "用户发布新的论坛帖子")
    public Result<Post> publishPost(@Valid @RequestBody PostCreateDTO postCreateDTO) {
        return postService.publishPost(postCreateDTO);
    }

    @GetMapping("post/detail/{id}")
    @Operation(summary = "获取帖子详情", description = "根据ID获取帖子的详细信息")
    public Result<PostVO> getPostDetail(@PathVariable Long id) {
        return postService.getPostDetail(id);
    }

    @PostMapping("post/like/{id}")
    @Operation(summary = "点赞帖子", description = "用户对帖子进行点赞操作")
    public Result<Post> likePost(@PathVariable Long id) {
        return postService.likePost(id);
    }

    @PostMapping("comment/publish")
    @Operation(summary = "发布评论", description = "用户对帖子或失物招领发布评论")
    public Result<Comment> publishComment(@Valid @RequestBody CommentCreateDTO commentCreateDTO) {
        return commentService.publishComment(commentCreateDTO);
    }

    @GetMapping("comment/list")
    @Operation(summary = "获取评论列表", description = "分页获取评论列表，支持按帖子或失物招领筛选")
    public Result<PageResult> listComment(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(50) Integer size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "postId", required = false) Long postId,
            @RequestParam(value = "lostItemId", required = false) Long lostItemId
    ) {
        return commentService.listComment(page, size, sort, postId,lostItemId);
    }


    @DeleteMapping("comment/delete/{id}")
    @Operation(summary = "删除评论", description = "删除指定的评论")
    public Result<String> deleteComment(@PathVariable Long id) {
        return commentService.deleteComment(id);
    }

}
