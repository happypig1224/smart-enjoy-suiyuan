package com.shxy.suiyuanserver.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuancommon.utils.RedisCacheUtil;
import com.shxy.suiyuanentity.dto.LostFoundDTO;
import com.shxy.suiyuanentity.entity.LostFound;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanentity.vo.LostFoundVO;
import com.shxy.suiyuanserver.service.LostFoundService;
import com.shxy.suiyuanserver.service.UserService;
import com.shxy.suiyuanserver.mapper.LostFoundMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Wu, Hui Ming
 * @description 针对表【lost_found】的数据库操作Service实现
 * @createDate 2026-04-04 21:30:08
 */

@Slf4j
@Service
public class LostFoundServiceImpl extends ServiceImpl<LostFoundMapper, LostFound>
        implements LostFoundService {

    private final LostFoundMapper lostFoundMapper;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCacheUtil redisCacheUtil;
    private final UserService userService;

    public LostFoundServiceImpl(LostFoundMapper lostFoundMapper, 
                               ObjectMapper objectMapper, 
                               RedisTemplate<String, Object> redisTemplate, 
                               RedisCacheUtil redisCacheUtil, 
                               UserService userService) {
        this.lostFoundMapper = lostFoundMapper;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.redisCacheUtil = redisCacheUtil;
        this.userService = userService;
    }


    @Transactional(rollbackFor = Exception.class)
    public Result<LostFound> createLostFound(LostFoundDTO lostFoundDTO) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new BaseException("用户ID不合法");
        }

        log.info("用户{}开始发布失物招领，类型：{}，标题：{}，图片数组：{}", 
                userId, lostFoundDTO.getType(), lostFoundDTO.getTitle(), lostFoundDTO.getImages());

        LostFound lostFound = LostFound.builder()
                .userId(userId)
                .type(lostFoundDTO.getType())
                .title(lostFoundDTO.getTitle())
                .description(lostFoundDTO.getDescription())
                .urgent(lostFoundDTO.getUrgent())
                .location(lostFoundDTO.getLocation())
                .phoneContact(lostFoundDTO.getPhoneContact())
                .wechatContact(lostFoundDTO.getWechatContact())
                .status(0)
                .viewCount(0)
                .createTime(new Date())
                .updateTime(new Date())
                .build();

        if (lostFoundDTO.getImages() != null && !lostFoundDTO.getImages().isEmpty()) {
            // 将 List<String> 转换为 JSON 字符串存储
            try {
                String imagesJson = objectMapper.writeValueAsString(lostFoundDTO.getImages());
                lostFound.setImages(imagesJson);
                log.info("设置图片JSON：{}", imagesJson);
            } catch (Exception e) {
                log.error("图片列表转JSON失败", e);
                throw new BaseException("图片数据处理失败");
            }
        } else {
            log.info("未上传图片或图片为空");
        }

        int count = lostFoundMapper.insert(lostFound);
        if (count <= 0) {
            log.warn("失物招领创建失败，用户ID：{}", userId);
            return Result.fail("创建失物招领失败!");
        }

        clearLostFoundListCache();
        log.info("失物招领创建成功，ID：{}，用户ID：{}", lostFound.getId(), userId);
        return Result.success(lostFound);
    }

    public Result<PageResult> listLostFound(Integer page, Integer pageSize, Integer type, Integer status, Integer urgent) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1 || pageSize > 50) pageSize = 10;
        
        // 使用final变量保证lambda表达式可用
        final int finalPage = page;
        final int finalPageSize = pageSize;
        
        String cacheKey = RedisConstant.LOSTFOUND_LIST_KEY_PREFIX +
                "page:" + finalPage + ":size:" + finalPageSize + 
                ":type:" + (type != null ? type : "all") + 
                ":status:" + (status != null ? status : "all") + 
                ":urgent:" + (urgent != null ? urgent : "all");

        log.info("查询失物招领列表，页码：{}，每页数量：{}，类型：{}，状态：{}，紧急程度：{}", finalPage, finalPageSize, type, status, urgent);

        // 使用工具类解决缓存雪崩(随机过期时间)
        PageResult pageResult = redisCacheUtil.queryWithPassThrough(
                cacheKey,
                PageResult.class,
                key -> {
                    LambdaQueryWrapper<LostFound> queryWrapper = new LambdaQueryWrapper<>();
                    if (type != null) {
                        queryWrapper.eq(LostFound::getType, type);
                    }
                    if (status != null) {
                        queryWrapper.eq(LostFound::getStatus, status);
                    }
                    if (urgent != null) {
                        queryWrapper.eq(LostFound::getUrgent, urgent);
                    }
                    queryWrapper.orderByDesc(LostFound::getCreateTime);
                    Page<LostFound> pageInfo = new Page<>(finalPage, finalPageSize);
                    Page<LostFound> result = lostFoundMapper.selectPage(pageInfo, queryWrapper);
                    
                    List<LostFoundVO> voList = convertToVO(result.getRecords());
                    return PageResult.builder()
                            .total(result.getTotal())
                            .records(voList)
                            .page(result.getCurrent())
                            .size(result.getSize())
                            .build();
                },
                RedisConstant.LOSTFOUND_LIST_TTL,
                TimeUnit.SECONDS
        );

        if (pageResult == null) {
            log.warn("获取失物招领列表失败");
            return Result.fail("获取失物招领列表失败");
        }
        log.info("获取失物招领列表成功，总数：{}", pageResult.getTotal());
        return Result.success(pageResult);
    }

    public Result<LostFoundVO> detailLostFound(Long id) {
        if (id == null || id <= 0) {
            log.warn("查询失物招领详情失败，ID不合法：{}", id);
            return Result.fail("ID不合法");
        }
        
        Long currentUserId = BaseContext.getCurrentUserId();
        String cacheKey = RedisConstant.LOSTFOUND_DETAIL_KEY_PREFIX + id;
        String viewCountKey = "lostfound:view:" + id;
        
        log.info("用户{}开始查询失物招领详情，ID：{}", currentUserId, id);

        // 使用Redis原子递增浏览量，避免缓存与数据库不一致
        try {
            redisTemplate.opsForValue().increment(viewCountKey);
        } catch (Exception e) {
            log.error("更新浏览量缓存失败，ID：{}", id, e);
        }

        LostFoundVO vo = redisCacheUtil.queryWithMutex(
                cacheKey,
                LostFoundVO.class,
                key -> {
                    LostFound lostFound = lostFoundMapper.selectById(id);
                    if (lostFound == null) {
                        return null;
                    }
                    return convertToVO(Collections.singletonList(lostFound)).get(0);
                },
                RedisConstant.LOSTFOUND_DETAIL_TTL,
                TimeUnit.SECONDS
        );

        if (vo == null) {
            log.warn("失物招领不存在，ID：{}", id);
            return Result.fail("失物招领不存在");
        }
        
        // 从缓存或数据库获取浏览量，确保数据一致性
        try {
            Object viewCountObj = redisTemplate.opsForValue().get(viewCountKey);
            if (viewCountObj != null) {
                vo.setViewCount(Integer.parseInt(viewCountObj.toString()));
            }
        } catch (Exception e) {
            log.error("获取浏览量失败，ID：{}", id, e);
        }
        
        log.info("失物招领详情查询成功，ID：{}，浏览量：{}", id, vo.getViewCount());
        return Result.success(vo);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteLostFound(Long id) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new BaseException("用户 ID 不合法");
        }

        if (id == null || id <= 0) {
            throw new BaseException("失物 ID 不合法");
        }

        LostFound lostFound = lostFoundMapper.selectById(id);
        if (lostFound == null) {
            throw new BaseException("失物不存在");
        }

        if (!lostFound.getUserId().equals(userId)) {
            throw new BaseException("只能删除自己发布的失物");
        }
        
        Integer count = lostFoundMapper.deleteById(id);
        if (count <= 0) {
            log.warn("失物删除失败，ID：{}，用户ID：{}", id, userId);
            return Result.fail("删除失败!");
        }
        
        String detailKey = RedisConstant.LOSTFOUND_DETAIL_KEY_PREFIX + id;
        redisTemplate.delete(detailKey);
        clearLostFoundListCache();
        log.info("失物删除成功，ID：{}，用户ID：{}", id, userId);
        return Result.success("删除成功!");
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateLostFound(LostFoundDTO lostFoundDTO) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new BaseException("用户ID不合法");
        }
        
        if (lostFoundDTO.getId() == null || lostFoundDTO.getId() <= 0) {
            throw new BaseException("失物ID不合法");
        }
        
        LostFound existingLostFound = lostFoundMapper.selectById(lostFoundDTO.getId());
        if (existingLostFound == null) {
            throw new BaseException("失物不存在");
        }
        
        if (!existingLostFound.getUserId().equals(userId)) {
            throw new BaseException("只能修改自己发布的失物");
        }
        
        // 处理图片字段：将 List<String> 转换为 JSON 字符串
        String imagesJson = null;
        if (lostFoundDTO.getImages() != null && !lostFoundDTO.getImages().isEmpty()) {
            try {
                imagesJson = objectMapper.writeValueAsString(lostFoundDTO.getImages());
            } catch (Exception e) {
                log.error("图片列表转JSON失败", e);
                throw new BaseException("图片数据处理失败");
            }
        }
        
        // 构建更新实体
        LostFound updateEntity = LostFound.builder()
                .id(lostFoundDTO.getId())
                .title(lostFoundDTO.getTitle())
                .description(lostFoundDTO.getDescription())
                .urgent(lostFoundDTO.getUrgent())
                .location(lostFoundDTO.getLocation())
                .phoneContact(lostFoundDTO.getPhoneContact())
                .wechatContact(lostFoundDTO.getWechatContact())
                .images(imagesJson)
                .build();
        
        Integer count = lostFoundMapper.updateById(updateEntity);
        if (count <= 0) {
            log.warn("失物修改失败，ID：{}，用户ID：{}", lostFoundDTO.getId(), userId);
            return Result.fail("修改失败!");
        }
        
        if (lostFoundDTO.getId() != null) {
            String detailKey = RedisConstant.LOSTFOUND_DETAIL_KEY_PREFIX + lostFoundDTO.getId();
            redisTemplate.delete(detailKey);
        }
        clearLostFoundListCache();
        log.info("失物修改成功，ID：{}，用户ID：{}", lostFoundDTO.getId(), userId);
        return Result.success("修改成功!");
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateLostFoundStatus(Long id, Integer status) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new BaseException("用户ID不合法");
        }

        if (id == null || id <= 0) {
            throw new BaseException("失物ID不合法");
        }

        if (status == null || (status != 0 && status != 1)) {
            throw new BaseException("状态值不合法，有效值为0(未解决)或1(已解决)");
        }

        LostFound lostFound = lostFoundMapper.selectById(id);
        if (lostFound == null) {
            throw new BaseException("失物招领不存在");
        }

        if (!lostFound.getUserId().equals(userId)) {
            throw new BaseException("只能修改自己发布的失物招领状态");
        }

        Integer count = lostFoundMapper.updateLostFoundStatus(id, status);
        if (count <= 0) {
            return Result.fail("修改失败!");
        }
        String detailKey = RedisConstant.LOSTFOUND_DETAIL_KEY_PREFIX + id;
        redisTemplate.delete(detailKey);
        clearLostFoundListCache();
        log.info("失物招领状态更新成功，ID：{}，状态：{}，用户ID：{}", id, status, userId);
        return Result.success("修改成功!");
    }

    public Result<List<LostFoundVO>> getUserPublishedLostFound(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BaseException("用户ID不合法");
        }

        String cacheKey = RedisConstant.USER_LOSTFOUND_LIST_KEY_PREFIX + userId;

        List<LostFoundVO> voList = redisCacheUtil.queryWithPassThrough(
                cacheKey,
                List.class,
                key -> {
                    LambdaQueryWrapper<LostFound> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(LostFound::getUserId, userId);
                    queryWrapper.orderByDesc(LostFound::getCreateTime);
                    List<LostFound> lostFounds = lostFoundMapper.selectList(queryWrapper);
                    return convertToVO(lostFounds);
                },
                RedisConstant.LOSTFOUND_LIST_TTL,
                TimeUnit.SECONDS
        );

        if (voList == null) {
            voList = Collections.emptyList();
        }
        return Result.success(voList);
    }

    private void clearLostFoundListCache() {
        Set<String> keys = new HashSet<>();
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(RedisConstant.LOSTFOUND_LIST_KEY_PREFIX + "*")
                .count(100)
                .build();
        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        } catch (Exception e) {
            log.error("扫描失物招领列表缓存失败", e);
        }
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        log.info("清除失物招领列表缓存, 删除 {} 个key", keys.size());
    }

    /**
     * 将 LostFound 转换为 LostFoundVO
     */
    private List<LostFoundVO> convertToVO(List<LostFound> lostFounds) {
        if (lostFounds == null || lostFounds.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询用户信息
        List<Long> userIds = lostFounds.stream()
                .map(LostFound::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (k1, k2) -> k1));

        // 转换为 VO
        return lostFounds.stream().map(lostFound -> {
            LostFoundVO vo = LostFoundVO.builder()
                    .id(lostFound.getId())
                    .userId(lostFound.getUserId())
                    .type(lostFound.getType())
                    .status(lostFound.getStatus())
                    .title(lostFound.getTitle())
                    .description(lostFound.getDescription())
                    .urgent(lostFound.getUrgent())
                    .location(lostFound.getLocation())
                    .phoneContact(lostFound.getPhoneContact())
                    .wechatContact(lostFound.getWechatContact())
                    .viewCount(lostFound.getViewCount())
                    .createTime(lostFound.getCreateTime())
                    .updateTime(lostFound.getUpdateTime())
                    .build();

            // 设置类型名称和状态名称
            if (Integer.valueOf(0).equals(lostFound.getType())) {
                vo.setTypeName("寻物启事");
            } else {
                vo.setTypeName("招领启事");
            }
            
            if (Integer.valueOf(0).equals(lostFound.getStatus())) {
                vo.setStatusName("进行中");
            } else {
                vo.setStatusName("已完成");
            }

            // 解析图片JSON
            String imagesStr = lostFound.getImages();
            if (imagesStr != null && !imagesStr.isEmpty()) {
                try {
                    List<String> images = objectMapper.readValue(imagesStr, new TypeReference<List<String>>() {});
                    vo.setImages(images);
                } catch (JsonProcessingException e) {
                    log.warn("图片JSON解析失败，ID：{}", lostFound.getId());
                    vo.setImages(Collections.emptyList());
                }
            } else {
                vo.setImages(Collections.emptyList());
            }

            // 设置用户信息
            User user = userMap.get(lostFound.getUserId());
            if (user != null) {
                vo.setUserNickName(user.getNickName());
                vo.setUserAvatar(user.getAvatar());
            }

            return vo;
        }).collect(Collectors.toList());
    }

}




