package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.constant.RateLimitConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.exception.ResourceException;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuancommon.utils.RateLimitUtil;
import com.shxy.suiyuancommon.utils.RedisCacheUtil;
import com.shxy.suiyuancommon.utils.TencentCOSAvatarUtil;
import com.shxy.suiyuanentity.dto.ResourceDTO;
import com.shxy.suiyuanentity.entity.Resource;
import com.shxy.suiyuanentity.entity.ResourceFavorite;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanentity.vo.ResourceVO;
import com.shxy.suiyuanserver.mapper.ResourceMapper;
import com.shxy.suiyuanserver.service.ResourceFavoriteService;
import com.shxy.suiyuanserver.service.ResourceService;
import com.shxy.suiyuanserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
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

    private final ResourceMapper resourceMapper;
    private final ResourceFavoriteService resourceFavoriteService;
    private final UserService userService;
    private final TencentCOSAvatarUtil tencentCOSAvatarUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisCacheUtil redisCacheUtil;

    // 文件上传配置常量
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "txt", "md", "jpg", "jpeg", "png", "gif"
    );
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "text/markdown",
            "application/octet-stream",
            "image/jpeg",
            "image/png",
            "image/gif"
    );

    // 文件名安全处理正则表达式
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.]+$");

    public ResourceServiceImpl(ResourceMapper resourceMapper,
                              ResourceFavoriteService resourceFavoriteService,
                              UserService userService,
                              TencentCOSAvatarUtil tencentCOSAvatarUtil,
                              RedisTemplate<String, Object> redisTemplate,
                              StringRedisTemplate stringRedisTemplate,
                              ObjectMapper objectMapper,
                              RedisCacheUtil redisCacheUtil) {
        this.resourceMapper = resourceMapper;
        this.resourceFavoriteService = resourceFavoriteService;
        this.userService = userService;
        this.tencentCOSAvatarUtil = tencentCOSAvatarUtil;
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.redisCacheUtil = redisCacheUtil;
    }

    public Result<PageResult> queryList(Integer page, Integer pageSize, String type, Integer subject, String sort) {
        String cleanType = type != null ? type.replaceAll("[^a-zA-Z0-9_-]", "") : "all";
        String cleanSort = sort != null ? sort.replaceAll("[^a-zA-Z0-9_-]", "") : "newest";

        // 验证参数的有效性
        if (!isValidSort(cleanSort)) {
            cleanSort = "newest";
        }
        
        // 创建final变量以供lambda表达式使用
        final String finalCleanType = cleanType;
        final Integer finalSubject = subject;
        final String finalCleanSort = cleanSort;
        final Integer finalPage = page;
        final Integer finalPageSize = pageSize;
        
        String cacheKey = RedisConstant.RESOURCE_LIST_KEY_PREFIX +
                finalPage + ":" + finalPageSize + ":" + finalCleanType + ":" + (finalSubject != null ? finalSubject : "all") + ":" + finalCleanSort;

        // 使用工具类解决缓存雪崩(随机过期时间)
        PageResult pageResult = redisCacheUtil.queryWithPassThrough(
                cacheKey,
                PageResult.class,
                key -> {
                    LambdaQueryWrapper<Resource> queryWrapper = new LambdaQueryWrapper<>();
                    if (!"all".equals(finalCleanType) && !finalCleanType.isEmpty()) {
                        queryWrapper.eq(Resource::getType, finalCleanType);
                    }
                    if (finalSubject != null) {
                        queryWrapper.eq(Resource::getSubject, finalSubject);
                    }
                    if ("newest".equals(finalCleanSort)) {
                        queryWrapper.orderByDesc(Resource::getCreateTime);
                    } else if ("hottest".equals(finalCleanSort)) {
                        queryWrapper.orderByDesc(Resource::getDownloadCount);
                    } else {
                        queryWrapper.orderByDesc(Resource::getCreateTime);
                    }
                    Page<Resource> pageInfo = new Page<>(finalPage, finalPageSize);
                    Page<Resource> result = resourceMapper.selectPage(pageInfo, queryWrapper);
                    List<ResourceVO> voList = convertToVOWithoutFavorite(result.getRecords());
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
            log.warn("获取资源列表失败：pageResult为null");
            return Result.fail("获取资源列表失败");
        }

        log.info("获取资源列表成功，开始填充收藏状态，记录数={}", 
                pageResult.getRecords() != null ? pageResult.getRecords().size() : 0);
        
        // 深拷贝避免污染缓存对象(防止并发用户看到错误的收藏状态)
        @SuppressWarnings("unchecked")
        List<ResourceVO> voListCopy = objectMapper.convertValue(pageResult.getRecords(), new TypeReference<List<ResourceVO>>() {});
        
        // 填充当前用户的收藏状态（不缓存，每个用户单独查询）
        fillFavoriteStatus(voListCopy);
        
        PageResult resultWithFavoriteStatus = PageResult.builder()
                .total(pageResult.getTotal())
                .records(voListCopy)
                .page(pageResult.getPage())
                .size(pageResult.getSize())
                .build();
        
        log.info("收藏状态填充完成");
        return Result.success(resultWithFavoriteStatus);
    }
    
    /**
     * 验证排序参数的有效性
     */
    private boolean isValidSort(String sort) {
        return "newest".equals(sort) || "hottest".equals(sort);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Long> uploadResource(MultipartFile file, ResourceDTO resourceDTO) {
        Long userId = BaseContext.getCurrentUserId();
        
        if (userId == null || userId <= 0) {
            throw new BaseException("用户 ID 不合法");
        }

        if (file == null || file.isEmpty()) {
            throw new BaseException("上传文件不能为空");
        }

        if (resourceDTO == null || resourceDTO.getType() == null) {
            throw new BaseException("资源类型不能为空");
        }

        // 验证上传文件的安全性
        validateUploadFile(file);

        // 文件上传限流: 60秒内最多上传5次
        String rateLimitKey = RateLimitConstant.UPLOAD_RATE_LIMIT_KEY + userId;
        RateLimitUtil.checkRateLimit(stringRedisTemplate, rateLimitKey,
            RateLimitConstant.UPLOAD_TIME_WINDOW, RateLimitConstant.UPLOAD_MAX_REQUESTS);

        // 安全处理文件名
        String sanitizedFileName = sanitizeFileName(file.getOriginalFilename());
        
        String resourceUrl = tencentCOSAvatarUtil.uploadFile(file);

        Resource resource = Resource.builder()
                .userId(userId)
                .type(resourceDTO.getType())
                .subject(resourceDTO.getSubject())
                .resourceUrl(resourceUrl)
                .fileName(sanitizedFileName)
                .fileSize(file.getSize())
                .description(resourceDTO.getDescription())
                .downloadCount(0)
                .createTime(new Date())
                .updateTime(new Date())
                .build();

        resourceMapper.insert(resource);

        clearResourceListCache();

        log.info("用户 {} 上传了资源：{}", userId, sanitizedFileName);

        return Result.success(resource.getId());

    }
    
    /**
     * 验证上传文件的安全性
     */
    private void validateUploadFile(MultipartFile file) {
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BaseException("文件大小超出限制，最大支持50MB");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new BaseException("文件名不能为空");
        }
        
        // 检查文件扩展名
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        if (extension == null || extension.isEmpty()) {
            throw new BaseException("文件必须包含扩展名");
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BaseException("不允许的文件类型: " + extension);
        }
        
        // 检查MIME类型
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new BaseException("不允许的文件类型: " + contentType);
        }
        
        // 检查文件名是否包含危险字符
        if (fileName.contains("..") || fileName.contains("/")) {
            throw new BaseException("文件名包含非法字符");
        }
    }
    
    /**
     * 安全处理文件名，防止路径遍历和其他安全问题
     */
    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null) {
            return null;
        }
        
        // 移除路径字符，防止路径遍历
        String sanitized = originalFileName.replaceAll("\\.\\./", "")
                                          .replaceAll("\\.\\.\\\\", "")
                                          .replaceAll("/", "_")
                                          .replaceAll("\\\\", "_");
        
        // 保留原始文件扩展名，但确保安全
        String baseName = FilenameUtils.getBaseName(sanitized);
        String extension = FilenameUtils.getExtension(originalFileName); // 使用原始文件名获取扩展名以保持正确性
        
        // 清理基础名称
        baseName = baseName.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
        
        // 限制文件名长度
        if (baseName.length() > 100) {
            baseName = baseName.substring(0, 100);
        }
        
        return baseName + "." + extension.toLowerCase();
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
            throw ResourceException.notFound(String.valueOf(id));
        }

        if (!resource.getUserId().equals(userId)) {
            throw ResourceException.unauthorized("delete", String.valueOf(id));
        }

        resourceMapper.deleteById(id);

        // 清除资源详情缓存
        String detailKey = RedisConstant.RESOURCE_DETAIL_KEY_PREFIX + id;
        redisTemplate.delete(detailKey);

        // 清除用户发布的资源列表缓存
        String userResourceKey = RedisConstant.USER_RESOURCE_LIST_KEY_PREFIX + userId;
        redisTemplate.delete(userResourceKey);

        // 清除公共列表缓存
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
            throw ResourceException.notFound(String.valueOf(id));
        }
        return Result.success(resourceVO);
    }

    /**
     * 将 Resource 转换为 ResourceVO（不包含收藏状态）
     */
    private List<ResourceVO> convertToVOWithoutFavorite(List<Resource> resources) {
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

        // 转换为 VO（isFavorite 设为 false，稍后由 fillFavoriteStatus 填充）
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
                vo.setUserAvatar(user.getAvatar());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 填充当前用户的收藏状态
     */
    @SuppressWarnings("unchecked")
    private void fillFavoriteStatus(List<ResourceVO> voList) {
        if (voList == null || voList.isEmpty()) {
            log.debug("填充收藏状态：资源列表为空，跳过");
            return;
        }

        Long currentUserId = BaseContext.getCurrentUserId();
        if (currentUserId == null || currentUserId <= 0) {
            log.debug("填充收藏状态：当前用户ID无效，跳过");
            return;
        }

        List<Long> resourceIds = voList.stream()
                .map(ResourceVO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (resourceIds.isEmpty()) {
            log.debug("填充收藏状态：资源ID列表为空，跳过");
            return;
        }

        log.debug("填充收藏状态：用户ID={}，资源数量={}", currentUserId, resourceIds.size());

        // 批量查询当前用户对这些资源的收藏状态
        LambdaQueryWrapper<ResourceFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ResourceFavorite::getUserId, currentUserId)
                .in(ResourceFavorite::getResourceId, resourceIds)
                .select(ResourceFavorite::getResourceId);
        List<ResourceFavorite> favorites = resourceFavoriteService.list(queryWrapper);
        
        log.debug("填充收藏状态：查询到收藏记录数={}", favorites.size());
        
        Map<Long, Boolean> favoriteMap = favorites.stream()
                .collect(Collectors.toMap(ResourceFavorite::getResourceId, f -> true, (k1, k2) -> k1));

        for (ResourceVO vo : voList) {
            vo.setIsFavorite(favoriteMap.getOrDefault(vo.getId(), false));
        }
    }

    /**
     * 将 Resource 转换为 ResourceVO（用于其他场景，如用户发布的资源列表）
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
                vo.setUserAvatar(user.getAvatar());
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

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseException("上传图片不能为空");
        }
        
        // 使用统一的文件验证方法
        validateUploadFile(file);
        
        // 验证是否为图片类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BaseException("只能上传图片文件");
        }
        
        String imageUrl = tencentCOSAvatarUtil.uploadFile(file);
        log.info("用户 {} 上传了图片：{}", BaseContext.getCurrentUserId(), imageUrl);
        return imageUrl;
    }

    @Override
    public Result<String> downloadResource(Long id) {
        if (id == null || id <= 0) {
            throw new BaseException("资源ID不合法");
        }

        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw ResourceException.notFound(String.valueOf(id));
        }

        // 递增下载次数
        resourceMapper.update(null, new LambdaUpdateWrapper<>(Resource.class)
                .eq(Resource::getId, id)
                .setSql("download_count = download_count + 1"));

        // 清除资源详情缓存
        String detailKey = RedisConstant.RESOURCE_DETAIL_KEY_PREFIX + id;
        redisTemplate.delete(detailKey);
        clearResourceListCache();

        log.info("下载资源{}，下载次数+1",  id);
        return Result.success(resource.getResourceUrl());
    }

    @Override
    public Result<String> updateResource(Long id, Long userId, ResourceDTO resourceDTO) {
        if (id == null || id <= 0) {
            throw new BaseException("资源ID不合法");
        }
        if (userId == null || userId <= 0) {
            throw new BaseException("用户ID不合法");
        }

        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw ResourceException.notFound(String.valueOf(id));
        }

        if (!resource.getUserId().equals(userId)) {
            throw ResourceException.unauthorized("update", String.valueOf(id));
        }

        // 构建更新条件
        LambdaUpdateWrapper<Resource> updateWrapper = new LambdaUpdateWrapper<>(Resource.class)
                .eq(Resource::getId, id);

        if (resourceDTO.getType() != null) {
            updateWrapper.set(Resource::getType, resourceDTO.getType());
        }
        if (resourceDTO.getSubject() != null) {
            updateWrapper.set(Resource::getSubject, resourceDTO.getSubject());
        }
        if (resourceDTO.getDescription() != null) {
            updateWrapper.set(Resource::getDescription, resourceDTO.getDescription());
        }
        updateWrapper.set(Resource::getUpdateTime, new Date());

        int update = resourceMapper.update(null, updateWrapper);
        if (update <= 0) {
            throw new BaseException("更新资源失败");
        }

        // 清除缓存
        String detailKey = RedisConstant.RESOURCE_DETAIL_KEY_PREFIX + id;
        redisTemplate.delete(detailKey);
        String userResourceKey = RedisConstant.USER_RESOURCE_LIST_KEY_PREFIX + userId;
        redisTemplate.delete(userResourceKey);
        clearResourceListCache();

        log.info("用户{}更新资源{}成功", userId, id);
        return Result.success("更新成功");
    }

}