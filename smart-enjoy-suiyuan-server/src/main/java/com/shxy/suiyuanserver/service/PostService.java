package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.PostDTO;
import com.shxy.suiyuanentity.dto.PostUpdateDTO;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanentity.vo.PostVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author huang qi long
* @description 针对表【post】的数据库操作Service
* @createDate 2026-04-04 21:30:08
*/
public interface PostService extends IService<Post> {


    Result<PageResult> listPost(Integer page, Integer size, String sort, Integer type);

    Result<Post> publishPost(PostDTO postDTO);

    Result<PostVO> getPostDetail(Long id);

    Result<Post> likePost(Long id);

    Result<Post> cancelLikePost(Long id);

    Result<String> deletePost(Long id);

    Result<Post> updatePost(PostUpdateDTO postUpdateDTO);

    /**
     * 获取用户发布的帖子列表
     * @param userId 用户ID
     * @return 帖子列表
     */
    Result<List<PostVO>> getUserPublishedPosts(Long userId);

    /**
     * 上传帖子图片
     * @param file 图片文件
     * @return 图片URL
     */
    String uploadPostImage(MultipartFile file);
}
