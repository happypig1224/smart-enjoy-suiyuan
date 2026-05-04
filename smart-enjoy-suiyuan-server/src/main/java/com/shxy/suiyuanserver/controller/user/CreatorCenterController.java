package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.annotation.RequireLogin;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.dto.PostDTO;
import com.shxy.suiyuanentity.dto.PostUpdateDTO;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanentity.vo.CreatorStatsVO;
import com.shxy.suiyuanentity.vo.PostVO;
import com.shxy.suiyuanserver.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/creator-center")
@Tag(name = "创作者中心")
public class CreatorCenterController {

    @Autowired
    private PostService postService;

    @GetMapping("stats")
    @RequireLogin
    @Operation(summary = "获取创作者统计数据", description = "获取当前用户的帖子、互动、粉丝等统计数据")
    public Result<CreatorStatsVO> getCreatorStats() {
        return postService.getCreatorStats(BaseContext.getCurrentUserId());
    }

    @GetMapping("posts")
    @RequireLogin
    @Operation(summary = "获取创作者帖子列表", description = "分页获取当前用户发布的帖子列表，支持按状态筛选")
    public Result<PageResult> getCreatorPostList(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        return postService.getCreatorPostList(BaseContext.getCurrentUserId(), status, page, size);
    }

    @GetMapping("post/{id}")
    @RequireLogin
    @Operation(summary = "获取帖子详情", description = "获取帖子的详细信息，包括草稿（仅作者可查看）")
    public Result<PostVO> getPostDetail(@PathVariable Long id) {
        if (id <= 0) {
            return Result.fail("帖子ID无效");
        }
        return postService.getPostDetailForCreator(BaseContext.getCurrentUserId(), id);
    }

    @PostMapping("post/publish")
    @RequireLogin
    @Operation(summary = "发布帖子", description = "发布帖子或保存草稿")
    public Result<Post> publishPost(@Valid @RequestBody PostDTO postDTO) {
        return postService.publishPostPublic(postDTO);
    }

    @PutMapping("post")
    @RequireLogin
    @Operation(summary = "编辑帖子", description = "编辑已发布的帖子或草稿")
    public Result<Post> updatePost(@Valid @RequestBody PostUpdateDTO postUpdateDTO) {
        return postService.updatePostPublic(postUpdateDTO);
    }

    @DeleteMapping("post/{id}")
    @RequireLogin
    @Operation(summary = "删除帖子", description = "删除自己发布的帖子")
    public Result<String> deletePost(@PathVariable Long id) {
        if (id <= 0) {
            return Result.fail("帖子ID无效");
        }
        return postService.deletePost(id);
    }
}
