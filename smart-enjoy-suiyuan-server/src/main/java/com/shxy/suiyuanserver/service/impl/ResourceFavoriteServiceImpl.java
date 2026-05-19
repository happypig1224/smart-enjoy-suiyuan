package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.entity.Resource;
import com.shxy.suiyuanentity.entity.ResourceFavorite;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanentity.vo.ResourceVO;
import com.shxy.suiyuanserver.mapper.ResourceFavoriteMapper;
import com.shxy.suiyuanserver.service.ResourceFavoriteService;
import com.shxy.suiyuanserver.service.ResourceService;
import com.shxy.suiyuanserver.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ResourceFavoriteServiceImpl extends ServiceImpl<ResourceFavoriteMapper, ResourceFavorite>
        implements ResourceFavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceFavoriteServiceImpl.class);

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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Transactional(rollbackFor = Exception.class)
    public Result<String> favorite(Long userId, Long resourceId) {
        logger.info("用户{}尝试收藏资源{}", userId, resourceId);

        if (userId == null || userId <= 0) {
            throw new BaseException("用户 ID 不合法");
        }
        if (resourceId == null || resourceId <= 0) {
            throw new BaseException("资源 ID 不合法");
        }

        Resource resource = resourceService.getById(resourceId);
        if (resource == null) {
            logger.warn("用户{}收藏失败: 资源{}不存在", userId, resourceId);
            throw new BaseException("资源不存在");
        }

        ResourceFavorite existing = resourceFavoriteMapper.checkFavorite(userId, resourceId);
        if (existing != null) {
            logger.warn("[审计日志] 用户{}收藏失败: 资源{}已收藏", userId, resourceId);
            throw new BaseException("该资源已在收藏列表中");
        }

        ResourceFavorite favorite = ResourceFavorite.builder()
                .userId(userId)
                .resourceId(resourceId)
                .resourceType("resource")
                .createTime(new Date())
                .build();

        boolean success = this.save(favorite);
        if (!success) {
            logger.warn("用户{}收藏资源{}失败: 数据库操作失败", userId, resourceId);
            throw new BaseException("收藏失败");
        }

        logger.info("[审计日志] 用户{}成功收藏资源{}", userId, resourceId);

        sendFavoriteNotification(userId, resourceId, resource.getFileName());

        clearUserFavoriteCache(userId);
        String detailKey = RedisConstant.RESOURCE_DETAIL_KEY_PREFIX + resourceId;
        redisTemplate.delete(detailKey);

        return Result.success("收藏成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelFavorite(Long userId, Long resourceId) {
        logger.info("[审计日志] 用户{}尝试取消收藏资源{}", userId, resourceId);

        if (userId == null || userId <= 0) {
            throw new BaseException("用户 ID 不合法");
        }
        if (resourceId == null || resourceId <= 0) {
            throw new BaseException("资源 ID 不合法");
        }

        Resource resource = resourceService.getById(resourceId);
        if (resource == null) {
            logger.warn("[审计日志] 用户{}取消收藏失败: 资源{}不存在", userId, resourceId);
            throw new BaseException("资源不存在");
        }

        int result = resourceFavoriteMapper.cancelFavorite(userId, resourceId);
        if (result == 0) {
            logger.warn("[审计日志] 用户{}取消收藏失败: 未收藏资源{}", userId, resourceId);
            throw new BaseException("取消收藏失败，可能未收藏该资源");
        }

        logger.info("[审计日志] 用户{}成功取消收藏资源{}", userId, resourceId);

        clearUserFavoriteCache(userId);
        String detailKey = RedisConstant.RESOURCE_DETAIL_KEY_PREFIX + resourceId;
        redisTemplate.delete(detailKey);

        return Result.success("取消收藏成功");
    }

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

    @Transactional(readOnly = true)
    @Override
    public Result<List<ResourceVO>> getUserFavoriteResources(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BaseException("用户 ID 不合法");
        }
        List<Long> resourcesIds = resourceFavoriteMapper.getUserFavoriteResources(userId);
        if (resourcesIds == null || resourcesIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<Resource> resources = resourceService.listByIds(resourcesIds);
        resources = resources.stream().filter(Objects::nonNull).toList();
        if (resources.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

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
                            .title(resource.getTitle())
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
                        vo.setUserName(uploader.getUserName());
                    }
                    return vo;
                })
                .toList();
        return Result.success(resourceVOS);
    }

    private void sendFavoriteNotification(Long currentUserId, Long resourceId, String resourceName) {
        Resource resource = resourceService.getById(resourceId);
        if (resource == null) {
            return;
        }

        Long uploaderId = resource.getUserId();

        if (uploaderId == null || uploaderId.equals(currentUserId)) {
            return;
        }

        String resourceTitle = resource.getTitle() != null ? resource.getTitle() :
                              (resource.getFileName() != null ? resource.getFileName() : "未知资源");

        long delayScore = System.currentTimeMillis() + 5000;
        String safeResourceTitle = resourceTitle.replace("\"", "\\\"");

        String taskValue = String.format(
            "{\"type\":\"resource_favorite\",\"from\":%d,\"to\":%d,\"targetId\":%d,\"resTitle\":\"%s\",\"time\":%d}",
            currentUserId, uploaderId, resourceId, safeResourceTitle, System.currentTimeMillis()
        );

        stringRedisTemplate.opsForZSet().add(RedisConstant.NOTIFY_BUFFER_KEY, taskValue, delayScore);
        logger.info("资源收藏通知已加入缓冲区: {} -> {}, resourceId: {}", currentUserId, uploaderId, resourceId);
    }
}
