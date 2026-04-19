package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.entity.ResourceFavorite;

/**
 * 资源收藏 Service
 * @author Wu, Hui Ming
 * @since 2026/4/10
 */
public interface ResourceFavoriteService extends IService<ResourceFavorite> {

    /**
     * 收藏资源
     * @param userId 用户 ID
     * @param resourceId 资源 ID
     * @return 操作结果
     */
    Result<String> favorite(Long userId, Long resourceId);

    /**
     * 取消收藏资源
     * @param userId 用户 ID
     * @param resourceId 资源 ID
     * @return 操作结果
     */
    Result<String> cancelFavorite(Long userId, Long resourceId);

    /**
     * 判断是否已收藏
     * @param userId 用户 ID
     * @param resourceId 资源 ID
     * @return true-已收藏，false-未收藏
     */
    boolean isFavorite(Long userId, Long resourceId);
}