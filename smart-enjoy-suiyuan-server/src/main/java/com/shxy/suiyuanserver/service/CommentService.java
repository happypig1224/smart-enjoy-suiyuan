package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.CommentCreateDTO;
import com.shxy.suiyuanentity.entity.Comment;

import java.util.Map;

/**
* @author huang qi long
* @description 针对表【comment】的数据库操作Service
* @createDate 2026-04-04 21:30:08
*/
public interface CommentService extends IService<Comment> {

    Result<Comment> publishComment(CommentCreateDTO commentCreateDTO);

    Result<String> deleteComment(Long id);

    Result<PageResult> listComment(Integer page, Integer size, String sort, Long postId, Long lostItemId);
}
