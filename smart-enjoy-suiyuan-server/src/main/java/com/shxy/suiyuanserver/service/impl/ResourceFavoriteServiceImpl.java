package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.entity.Resource;
import com.shxy.suiyuanentity.entity.ResourceFavorite;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanentity.vo.ResourceVO;
import com.shxy.suiyuanserver.mapper.ResourceFavoriteMapper;
import com.shxy.suiyuanserver.service.ResourceFavoriteService;
import com.shxy.suiyuanserver.service.ResourceService;
import com.shxy.suiyuanserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    public Result<String> favorite(Long userId, Long resourceId) {
        log.info("用户{}尝试收藏资源{}", userId, resourceId);

        if (userId == null || userId <= 0) {
            throw new BaseException("用户 ID 不合法");
        }
        if (resourceId == null || resourceId <= 0) {
            throw new BaseException("资源 ID 不合法");
        }

        Resource resource = resourceService.getById(resourceId);
        if (resource == null) {
            log.warn(" 用户{}收藏失败: 资源{}不存在", userId, resourceId);
            throw new BaseException("资源不存在");
        }

        ResourceFavorite existing = resourceFavoriteMapper.checkFavorite(userId, resourceId);
        if (existing != null) {
            log.warn("[审计日志] 用户{}收藏失败: 资源{}已收藏", userId, resourceId);
            throw new BaseException("该资源已在收藏列表中");
        }

        // 使用 MyBatis-Plus 的 save 方法，自动处理 ENUM 类型
        ResourceFavorite favorite = ResourceFavorite.builder()
                .userId(userId)
                .resourceId(resourceId)
                .resourceType("resource")
                .createTime(new Date())
                .build();

        boolean success = this.save(favorite);
        if (!success) {
            log.warn("用户{}收藏资源{}失败: 数据库操作失败", userId, resourceId);
            throw new BaseException("收藏失败");
        }

        log.info("[审计日志] 用户{}成功收藏资源{}", userId, resourceId);

        // 清除相关缓存
        clearUserFavoriteCache(userId);
        String detailKey = RedisConstant.RESOURCE_DETAIL_KEY_PREFIX + resourceId;
        redisTemplate.delete(detailKey);

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

        // 清除相关缓存
        clearUserFavoriteCache(userId);
        String detailKey = RedisConstant.RESOURCE_DETAIL_KEY_PREFIX + resourceId;
        redisTemplate.delete(detailKey);

        return Result.success("取消收藏成功");
    }

    /**
     * 清理用户收藏列表缓存
     */
    private void clearUserFavoriteCache(Long userId) {
        String favoriteListKey = RedisConstant.USER_RESOURCE_FAVORITE_LIST_KEY_PREFIX + userId;
        redisTemplate.delete(favoriteListKey);
    }

    @Override
    public boolean isFavorite(Long userId, Long resourceId) {
        if (userId == null || resourceId == null) {
            return false;
        }
        return resourceFavoriteMapper.isFavorite(userId, resourceId);
    }

    @Override
    public Result<List<ResourceVO>> getUserFavoriteResources(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BaseException("用户 ID 不合法");
        }
        List<Long> resourcesIds = resourceFavoriteMapper.getUserFavoriteResources(userId);
        if (resourcesIds == null || resourcesIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<Resource> resources = resourcesIds.stream()
                .map(resourcesId -> resourceService.getById(resourcesId))
                .filter(Objects::nonNull)
                .toList();
        if (resources.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 批量查询上传者信息
        List<Long> uploaderIds = resources.stream()
                .map(Resource::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userService.listByIds(uploaderIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (k1, k2) -> k1));

        List<ResourceVO> resourceVOS = resources.stream()
                .map(resource -> {
                    ResourceVO vo = ResourceVO.builder()
                            .id(resource.getId())
                            .userId(resource.getUserId())
                            .type(resource.getType())
                            .subject(resource.getSubject())
                            .resourceUrl(resource.getResourceUrl())
                            .fileName(resource.getFileName())
                            .fileSize(resource.getFileSize())
                            .description(resource.getDescription())
                            .downloadCount(resource.getDownloadCount())
                            .createTime(resource.getCreateTime())
                            .updateTime(resource.getUpdateTime())
                            .isFavorite(true)
                            .build();
                    User uploader = userMap.get(resource.getUserId());
                    if (uploader != null) {
                        vo.setUserNickName(uploader.getNickName());
                    }
                    return vo;
                })
                .toList();
        return Result.success(resourceVOS);
    }
}
