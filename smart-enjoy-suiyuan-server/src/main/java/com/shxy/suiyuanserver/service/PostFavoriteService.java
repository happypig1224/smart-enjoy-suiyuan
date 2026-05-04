package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.entity.PostFavorite;
import com.shxy.suiyuanentity.vo.PostFavoriteStatusVO;

/**
 * 帖子收藏服务接口
 */
public interface PostFavoriteService extends IService<PostFavorite> {

    /**
     * 收藏帖子
     */
    Result<String> favoritePost(Long postId);

    /**
     * 取消收藏
     */
    Result<String> cancelFavoritePost(Long postId);

    /**
     * 查询收藏状态
     */
    Result<PostFavoriteStatusVO> getPostFavoriteStatus(Long postId);

    /**
     * 获取用户收藏的帖子列表
     */
    Result<java.util.List<com.shxy.suiyuanentity.vo.PostVO>> getUserFavoritePosts();
}
