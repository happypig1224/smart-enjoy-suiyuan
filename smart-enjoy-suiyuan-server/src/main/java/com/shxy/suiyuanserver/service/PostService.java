package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.PostCreateDTO;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanentity.vo.PostVO;

import java.util.Map;

/**
* @author huang qi long
* @description 针对表【post】的数据库操作Service
* @createDate 2026-04-04 21:30:08
*/
public interface PostService extends IService<Post> {


    Result<PageResult> listPost(Integer page, Integer size, String sort, Integer type);

    Result<Post> publishPost(PostCreateDTO postCreateDTO);

    Result<PostVO> getPostDetail(Long id);

    Result<Post> likePost(Long id);

    Result<Post> cancelLikePost(Long id);
}
