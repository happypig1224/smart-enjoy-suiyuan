package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.entity.SecondhandFavorite;
import com.shxy.suiyuanentity.vo.SecondhandItemVO;

import java.util.List;

/**
 * 二手商品收藏Service
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/28 09:10
 */
public interface SecondhandFavoriteService extends IService<SecondhandFavorite> {

    /**
     * 收藏商品
     * @param userId 用户ID
     * @param itemId 商品ID
     * @return 操作结果
     */
    Result<String> favorite(Long userId, Long itemId);

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param itemId 商品ID
     * @return 操作结果
     */
    Result<String> cancelFavorite(Long userId, Long itemId);

    /**
     * 获取用户收藏的商品列表
     * @param userId 用户ID
     * @return 收藏的商品列表
     */
    Result<List<SecondhandItemVO>> getUserFavoriteItems(Long userId);

    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param itemId 商品ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long userId, Long itemId);
}
