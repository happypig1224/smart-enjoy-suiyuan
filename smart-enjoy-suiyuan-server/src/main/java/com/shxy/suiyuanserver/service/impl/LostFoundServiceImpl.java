package com.shxy.suiyuanserver.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.enums.PostTypeEnum;
import com.shxy.suiyuancommon.exception.AccountExistsException;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.exception.FileUploadException;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuancommon.utils.RedisCacheUtil;
import com.shxy.suiyuanentity.dto.LostFoundDTO;
import com.shxy.suiyuanentity.entity.LostFound;
import com.shxy.suiyuanentity.entity.Resource;
import com.shxy.suiyuanserver.service.LostFoundService;
import com.shxy.suiyuanserver.mapper.LostFoundMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.shxy.suiyuancommon.enums.PostTypeEnum.LOST;

/**
 * @author Wu, Hui Ming
 * @description 针对表【lost_found】的数据库操作Service实现
 * @createDate 2026-04-04 21:30:08
 */

@Slf4j
@Service
public class LostFoundServiceImpl extends ServiceImpl<LostFoundMapper, LostFound>
        implements LostFoundService {

    @Autowired
    private LostFoundMapper lostFoundMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisCacheUtil redisCacheUtil;


    @Transactional
    public Result<LostFound> createLostFound(LostFoundDTO lostFoundDTO) {
        Long userId = BaseContext.getCurrentUserId();

        LostFound lostFound = LostFound.builder()
                .userId(userId)
                .type(lostFoundDTO.getType())
                .title(lostFoundDTO.getTitle())
                .description(lostFoundDTO.getDescription())
                .urgent(lostFoundDTO.getUrgent())
                .location(lostFoundDTO.getLocation())
                .phoneContact(lostFoundDTO.getPhoneContact())
                .wechatContact(lostFoundDTO.getWechatContact())
                .status(lostFoundDTO.getType() == 1 ? LOST.getCode() : PostTypeEnum.FOUND.getCode())
                .viewCount(0)
                .createTime(new Date())
                .updateTime(new Date())
                .build();

        if (lostFoundDTO.getImages() != null && !lostFoundDTO.getImages().isEmpty()) {
            try {
                String imagesJson = objectMapper.writeValueAsString(lostFoundDTO.getImages());
                lostFound.setImages(imagesJson);
            } catch (JsonProcessingException e) {
                throw new FileUploadException("图片上传失败");
            }
        }

        int count = lostFoundMapper.insert(lostFound);
        if (count < 0) {
            return Result.fail("创建失物招领失败!");
        }

        clearLostFoundListCache();

        return Result.success(lostFound);
    }

    public Result<PageResult> listLostFound(Integer page, Integer pageSize, String type, String status, String urgent) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1 || pageSize > 50) pageSize = 10;
        
        // 使用final变量保证lambda表达式可用
        final int finalPage = page;
        final int finalPageSize = pageSize;
        
        String cacheKey = RedisConstant.LOSTFOUND_LIST_KEY_PREFIX +
                finalPage + ":" + finalPageSize + (type != null ? type : "all") + ":" +
                (status != null ? status : "all") + ":" +
                (urgent != null ? urgent : "all");

        // 使用工具类解决缓存雪崩(随机过期时间)
        PageResult pageResult = redisCacheUtil.queryWithPassThrough(
                cacheKey,
                PageResult.class,
                key -> {
                    LambdaQueryWrapper<LostFound> queryWrapper = new LambdaQueryWrapper<>();
                    if (type != null && !type.isEmpty()) {
                        try {
                            queryWrapper.eq(LostFound::getType, Integer.parseInt(type));
                        } catch (NumberFormatException e) {
                            throw new BaseException("类型参数格式错误");
                        }
                    }
                    if (status != null && !status.isEmpty()) {
                        try {
                            queryWrapper.eq(LostFound::getStatus, Integer.parseInt(status));
                        } catch (NumberFormatException e) {
                            throw new BaseException("状态参数格式错误");
                        }
                    }
                    if (urgent != null && !urgent.isEmpty()) {
                        try {
                            queryWrapper.eq(LostFound::getUrgent, Integer.parseInt(urgent));
                        } catch (NumberFormatException e) {
                            throw new BaseException("紧急程度参数格式错误");
                        }
                    }
                    queryWrapper.orderByDesc(LostFound::getCreateTime);
                    Page<LostFound> pageInfo = new Page<>(finalPage, finalPageSize);
                    Page<LostFound> result = lostFoundMapper.selectPage(pageInfo, queryWrapper);
                    List<Object> records = Arrays.asList(result.getRecords().toArray());
                    return PageResult.builder()
                            .total(result.getTotal())
                            .records(records)
                            .page(result.getCurrent())
                            .size(result.getSize())
                            .build();
                },
                RedisConstant.LOSTFOUND_LIST_TTL,
                TimeUnit.SECONDS
        );

        if (pageResult == null) {
            return Result.fail("获取失物招领列表失败");
        }
        return Result.success(pageResult);
    }

    public Result<LostFound> detailLostFound(Long id) {
        if (id == null || id <= 0) {
            return Result.fail("ID不合法");
        }
        String cacheKey = RedisConstant.LOSTFOUND_DETAIL_KEY_PREFIX + id;
        
        // 使用工具类解决缓存穿透+击穿
        LostFound lostFound = redisCacheUtil.queryWithMutex(
                cacheKey,
                LostFound.class,
                key -> lostFoundMapper.selectOne(new LambdaQueryWrapper<>(LostFound.class).eq(LostFound::getId, id)),
                RedisConstant.LOSTFOUND_DETAIL_TTL,
                TimeUnit.SECONDS
        );

        if (lostFound == null) {
            return Result.fail("失物招领不存在");
        }
        return Result.success(lostFound);
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
        Integer count = lostFoundMapper.delete(new LambdaQueryWrapper<>(LostFound.class).eq(LostFound::getId, id));
        if (count < 0) {
            return Result.fail("删除失败!");
        }
        String detailKey = RedisConstant.LOSTFOUND_DETAIL_KEY_PREFIX + id;
        redisTemplate.delete(detailKey);
        clearLostFoundListCache();
        return Result.success("删除成功!");
    }

    public Result<String> updateLostFound(LostFoundDTO lostFoundDTO) {
        Integer count = lostFoundMapper.updateLostFound(lostFoundDTO);
        if (count < 0) {
            return Result.fail("修改失败!");
        }
        if (lostFoundDTO.getId() != null) {
            String detailKey = RedisConstant.LOSTFOUND_DETAIL_KEY_PREFIX + lostFoundDTO.getId();
            redisTemplate.delete(detailKey);
        }
        clearLostFoundListCache();
        return Result.success("修改成功!");
    }

    public Result<String> updateLostFoundStatus(Long id, Integer status) {
        Integer count = lostFoundMapper.updateLostFoundStatus(id, status);
        if (count < 0) {
            return Result.fail("修改失败!");
        }
        String detailKey = RedisConstant.LOSTFOUND_DETAIL_KEY_PREFIX + id;
        redisTemplate.delete(detailKey);
        clearLostFoundListCache();
        return Result.success("修改成功!");
    }

    private void clearLostFoundListCache() {
        redisTemplate.delete(RedisConstant.LOSTFOUND_LIST_KEY_PREFIX + "*");
        log.info("清除失物招领列表缓存");
    }

}




