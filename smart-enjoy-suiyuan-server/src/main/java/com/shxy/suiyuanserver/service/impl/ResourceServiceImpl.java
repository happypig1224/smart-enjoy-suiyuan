package com.shxy.suiyuanserver.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.constant.RateLimitConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuancommon.utils.RateLimitUtil;
import com.shxy.suiyuancommon.utils.RedisCacheUtil;
import com.shxy.suiyuancommon.utils.TencentCOSAvatarUtil;
import com.shxy.suiyuanentity.dto.ResourceCreateDTO;
import com.shxy.suiyuanentity.entity.Resource;
import com.shxy.suiyuanentity.entity.ResourceFavorite;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanentity.vo.ResourceVO;
import com.shxy.suiyuanserver.mapper.ResourceMapper;
import com.shxy.suiyuanserver.service.ResourceFavoriteService;
import com.shxy.suiyuanserver.service.ResourceService;
import com.shxy.suiyuanserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Wu, Hui Ming
 * @description 针对表【resource】的数据库操作 Service 实现
 * @createDate 2026-04-04 21:30:08
 */
@Service
@Slf4j
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource>
        implements ResourceService {

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private ResourceFavoriteService resourceFavoriteService;

    @Autowired
    private UserService userService;

    @Autowired
    private TencentCOSAvatarUtil tencentCOSAvatarUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    public Result<PageResult> queryList(Integer page, Integer pageSize, String type, String sort, String order) {
        String cacheKey = RedisConstant.RESOURCE_LIST_KEY_PREFIX +
                page + ":" + pageSize +
                ":" + (type != null ? type : "all") +
                ":" + (sort != null ? sort : "newest");

        // 使用工具类解决缓存雪崩(随机过期时间)
        PageResult pageResult = redisCacheUtil.queryWithPassThrough(
                cacheKey,
                PageResult.class,
                key -> {
                    LambdaQueryWrapper<Resource> queryWrapper = new LambdaQueryWrapper<>();
                    if (type != null && !type.isEmpty()) {
                        queryWrapper.eq(Resource::getType, type);
                    }
                    if ("newest".equals(sort)) {
                        queryWrapper.orderByDesc(Resource::getCreateTime);
                    } else if ("hottest".equals(sort)) {
                        queryWrapper.orderByDesc(Resource::getDownloadCount);
                    } else {
                        queryWrapper.orderByDesc(Resource::getCreateTime);
                    }
                    Page<Resource> pageInfo = new Page<>(page, pageSize);
                    Page<Resource> result = resourceMapper.selectPage(pageInfo, queryWrapper);
                    List<Object> voList = Arrays.asList(result.getRecords().toArray());
                    return PageResult.builder()
                            .total(result.getTotal())
                            .records(voList)
                            .page(result.getCurrent())
                            .size(result.getSize())
                            .build();
                },
                RedisConstant.RESOURCE_LIST_TTL,
                TimeUnit.SECONDS
        );

        if (pageResult == null) {
            return Result.fail("获取资源列表失败");
        }
        return Result.success(pageResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Long> uploadResource(MultipartFile file, ResourceCreateDTO resourceCreateDTO) {
        Long userId = BaseContext.getCurrentUserId();
        
        if (userId == null || userId <= 0) {
            throw new BaseException("用户 ID 不合法");
        }

        if (file == null || file.isEmpty()) {
            throw new BaseException("上传文件不能为空");
        }

        if (resourceCreateDTO == null || resourceCreateDTO.getType() == null) {
            throw new BaseException("资源类型不能为空");
        }

        // 文件上传限流: 60秒内最多上传5次
        String rateLimitKey = RateLimitConstant.UPLOAD_RATE_LIMIT_KEY + userId;
        RateLimitUtil.checkRateLimit(redisTemplate, rateLimitKey,
            RateLimitConstant.UPLOAD_TIME_WINDOW, RateLimitConstant.UPLOAD_MAX_REQUESTS);

        String resourceUrl = tencentCOSAvatarUtil.uploadFile(file);

        Resource resource = Resource.builder()
                .userId(userId)
                .type(resourceCreateDTO.getType())
                .subject(resourceCreateDTO.getSubject())
                .resourceUrl(resourceUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .description(resourceCreateDTO.getDescription())
                .downloadCount(0)
                .createTime(new Date())
                .updateTime(new Date())
                .build();

        resourceMapper.insert(resource);

        clearResourceListCache();

        log.info("用户 {} 上传了资源：{}", userId, file.getOriginalFilename());

        return Result.success(resource.getId());

    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteResource(Long userId, Long id) {
        if (userId == null || userId <= 0) {
            throw new BaseException("用户 ID 不合法");
        }

        if (id == null || id <= 0) {
            throw new BaseException("资源 ID 不合法");
        }

        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BaseException("资源不存在");
        }

        if (!resource.getUserId().equals(userId)) {
            throw new BaseException("只能删除自己发布的资源");
        }

        resourceMapper.deleteById(id);

        String detailKey = RedisConstant.RESOURCE_DETAIL_KEY_PREFIX + id;
        redisTemplate.delete(detailKey);

        String userResourceKey = RedisConstant.USER_RESOURCE_LIST_KEY_PREFIX + userId;
        redisTemplate.delete(userResourceKey);

        clearResourceListCache();

        log.info("用户 {} 删除了资源 {}", userId, id);

        return Result.success("删除成功");
    }

    public Result<List<ResourceVO>> getUserPublishedResources(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BaseException("用户 ID 不合法");
        }

        String cacheKey = RedisConstant.USER_RESOURCE_LIST_KEY_PREFIX + userId;
        
        // 使用工具类解决缓存穿透+雪崩(列表数据用queryWithPassThrough)
        @SuppressWarnings("unchecked")
        List<ResourceVO> resourceVOList = (List<ResourceVO>) redisCacheUtil.queryWithPassThrough(
                cacheKey,
                Object.class,
                key -> {
                    LambdaQueryWrapper<Resource> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(Resource::getUserId, userId);
                    queryWrapper.orderByDesc(Resource::getCreateTime);
                    List<Resource> resources = resourceMapper.selectList(queryWrapper);
                    return this.convertToVO(resources);
                },
                RedisConstant.RESOURCE_LIST_TTL,
                TimeUnit.SECONDS
        );

        if (resourceVOList == null) {
            resourceVOList = Collections.emptyList();
        }
        return Result.success(resourceVOList);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> favoriteResource(Long userId, Long id) {
        return resourceFavoriteService.favorite(userId, id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelFavoriteResource(Long userId, Long id) {
        return resourceFavoriteService.cancelFavorite(userId, id);
    }

    public Result<ResourceVO> getResourceDetail(Long id, Long userId) {
        if (id == null || id <= 0) {
            throw new BaseException("资源 ID 不合法");
        }

        String cacheKey = RedisConstant.RESOURCE_DETAIL_KEY_PREFIX + id;
        
        // 使用工具类解决缓存穿透+击穿
        ResourceVO resourceVO = redisCacheUtil.queryWithMutex(
                cacheKey,
                ResourceVO.class,
                key -> {
                    Resource resource = resourceMapper.selectById(id);
                    if (resource == null) {
                        return null; // 返回null会被工具类缓存空值
                    }
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
                            .isFavorite(false)
                            .build();
                    
                    if (userId != null && userId > 0) {
                        boolean isFavorite = resourceFavoriteService.isFavorite(userId, id);
                        vo.setIsFavorite(isFavorite);
                    }
                    return vo;
                },
                RedisConstant.RESOURCE_DETAIL_TTL,
                TimeUnit.SECONDS
        );

        if (resourceVO == null) {
            throw new BaseException("资源不存在");
        }
        return Result.success(resourceVO);
    }

    /**
     * 将 Resource 转换为 ResourceVO
     */
    private List<ResourceVO> convertToVO(List<Resource> resources) {
        if (resources == null || resources.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询用户信息
        List<Long> userIds = resources.stream()
                .map(Resource::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (k1, k2) -> k1));

        // 转换为 VO
        return resources.stream().map(resource -> {
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
                    .isFavorite(false)
                    .build();

            User user = userMap.get(resource.getUserId());
            if (user != null) {
                vo.setUserNickName(user.getNickName());
            }

            return vo;
        }).collect(Collectors.toList());
    }


    private void clearResourceListCache() {
        Set<String> keys = new HashSet<>();
        Cursor<byte[]> cursor = redisTemplate.executeWithStickyConnection(
                connection -> connection.scan(ScanOptions.scanOptions()
                        .match(RedisConstant.RESOURCE_LIST_KEY_PREFIX + "*")
                        .count(100).build())
        );
        while (cursor.hasNext()) {
            keys.add(new String(cursor.next()));
        }
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

}