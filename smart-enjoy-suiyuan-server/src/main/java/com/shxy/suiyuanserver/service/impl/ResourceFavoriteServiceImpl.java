package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.entity.Resource;
import com.shxy.suiyuanentity.entity.ResourceFavorite;
import com.shxy.suiyuanserver.mapper.ResourceFavoriteMapper;
import com.shxy.suiyuanserver.service.ResourceFavoriteService;
import com.shxy.suiyuanserver.service.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Wu, Hui Ming
 * @description 针对表【resource_favorite】的数据库操作 Service 实现
 * @createDate 2026-04-10
 */
@Service
@Slf4j
public class ResourceFavoriteServiceImpl extends ServiceImpl<ResourceFavoriteMapper, ResourceFavorite>
        implements ResourceFavoriteService {

    @Autowired
    private ResourceFavoriteMapper resourceFavoriteMapper;

    @Autowired
    @Lazy
    private ResourceService resourceService;

    @Transactional(rollbackFor = Exception.class)
    public Result<String> favorite(Long userId, Long resourceId) {
        log.info("[审计日志] 用户{}尝试收藏资源{}", userId, resourceId);
        
        if (userId == null || userId <= 0) {
            throw new BaseException("用户 ID 不合法");
        }
        if (resourceId == null || resourceId <= 0) {
            throw new BaseException("资源 ID 不合法");
        }

        Resource resource = resourceService.getById(resourceId);
        if (resource == null) {
            log.warn("[审计日志] 用户{}收藏失败: 资源{}不存在", userId, resourceId);
            throw new BaseException("资源不存在");
        }

        ResourceFavorite existing = resourceFavoriteMapper.checkFavorite(userId, resourceId);
        if (existing != null) {
            log.warn("[审计日志] 用户{}收藏失败: 资源{}已收藏", userId, resourceId);
            throw new BaseException("该资源已在收藏列表中");
        }

        int result = resourceFavoriteMapper.addFavorite(userId, resourceId);
        if (result == 0) {
            log.warn("用户{}收藏资源{}失败: 数据库操作失败", userId, resourceId);
            throw new BaseException("收藏失败");
        }

        log.info("[审计日志] 用户{}成功收藏资源{}", userId, resourceId);
        return Result.success("收藏成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelFavorite(Long userId, Long resourceId) {
        log.info("[审计日志] 用户{}尝试取消收藏资源{}", userId, resourceId);
        
        if (userId == null || userId <= 0) {
            throw new BaseException("用户 ID 不合法");
        }
        if (resourceId == null || resourceId <= 0) {
            throw new BaseException("资源 ID 不合法");
        }

        Resource resource = resourceService.getById(resourceId);
        if (resource == null) {
            log.warn("[审计日志] 用户{}取消收藏失败: 资源{}不存在", userId, resourceId);
            throw new BaseException("资源不存在");
        }

        int result = resourceFavoriteMapper.cancelFavorite(userId, resourceId);
        if (result == 0) {
            log.warn("[审计日志] 用户{}取消收藏失败: 未收藏资源{}", userId, resourceId);
            throw new BaseException("取消收藏失败，可能未收藏该资源");
        }

        log.info("[审计日志] 用户{}成功取消收藏资源{}", userId, resourceId);
        return Result.success("取消收藏成功");
    }

    @Override
    public boolean isFavorite(Long userId, Long resourceId) {
        if (userId == null || resourceId == null) {
            return false;
        }
        return resourceFavoriteMapper.isFavorite(userId, resourceId);
    }
}
