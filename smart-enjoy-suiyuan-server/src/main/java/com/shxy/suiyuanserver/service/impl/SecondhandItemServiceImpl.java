package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxy.suiyuancommon.enums.SecondhandCategoryEnum;
import com.shxy.suiyuancommon.enums.SecondhandStatusEnum;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.TencentCOSAvatarUtil;
import com.shxy.suiyuanentity.dto.SecondhandItemDTO;
import com.shxy.suiyuanentity.entity.SecondhandItem;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanentity.vo.SecondhandItemVO;
import com.shxy.suiyuanserver.mapper.SecondhandItemMapper;
import com.shxy.suiyuanserver.mapper.UserMapper;
import com.shxy.suiyuanserver.service.SecondhandFavoriteService;
import com.shxy.suiyuanserver.service.SecondhandItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 二手商品Service实现类
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/28 09:11
 */
@Service
@Slf4j
public class SecondhandItemServiceImpl extends ServiceImpl<SecondhandItemMapper, SecondhandItem>
        implements SecondhandItemService {

    private final SecondhandItemMapper secondhandItemMapper;
    private final SecondhandFavoriteService secondhandFavoriteService;
    private final UserMapper userMapper;
    private final TencentCOSAvatarUtil tencentCOSAvatarUtil;
    private final ObjectMapper objectMapper;

    // 文件上传配置常量
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.]+$");

    public SecondhandItemServiceImpl(SecondhandItemMapper secondhandItemMapper,
                                     SecondhandFavoriteService secondhandFavoriteService,
                                     UserMapper userMapper,
                                     TencentCOSAvatarUtil tencentCOSAvatarUtil,
                                     ObjectMapper objectMapper) {
        this.secondhandItemMapper = secondhandItemMapper;
        this.secondhandFavoriteService = secondhandFavoriteService;
        this.userMapper = userMapper;
        this.tencentCOSAvatarUtil = tencentCOSAvatarUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> publishItem(Long userId, SecondhandItemDTO itemDTO) {
        // 验证分类
        try {
            SecondhandCategoryEnum.fromCode(itemDTO.getCategory());
        } catch (IllegalArgumentException e) {
            return Result.fail("无效的商品分类");
        }

        // 构建实体
        SecondhandItem item = SecondhandItem.builder()
                .sellerId(userId)
                .title(itemDTO.getTitle())
                .description(itemDTO.getDescription())
                .category(itemDTO.getCategory())
                .price(itemDTO.getPrice())
                .originalPrice(itemDTO.getOriginalPrice())
                .conditionLevel(itemDTO.getConditionLevel())
                .images(itemDTO.getImages())
                .contactPhone(itemDTO.getContactPhone())
                .contactWechat(itemDTO.getContactWechat())
                .tradeLocation(itemDTO.getTradeLocation())
                .viewCount(0)
                .favoriteCount(0)
                .status(SecondhandStatusEnum.ON_SALE.getCode())
                .build();

        secondhandItemMapper.insert(item);
        log.info("用户{}发布二手商品成功，商品ID: {}", userId, item.getId());
        return Result.success(item.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateItem(Long userId, SecondhandItemDTO itemDTO) {
        if (itemDTO.getId() == null) {
            return Result.fail("商品ID不能为空");
        }

        SecondhandItem existingItem = secondhandItemMapper.selectById(itemDTO.getId());
        if (existingItem == null) {
            return Result.fail("商品不存在");
        }

        // 权限校验：只有卖家可以修改
        if (!existingItem.getSellerId().equals(userId)) {
            return Result.fail("无权修改此商品");
        }

        // 更新字段
        existingItem.setTitle(itemDTO.getTitle());
        existingItem.setDescription(itemDTO.getDescription());
        existingItem.setCategory(itemDTO.getCategory());
        existingItem.setPrice(itemDTO.getPrice());
        existingItem.setOriginalPrice(itemDTO.getOriginalPrice());
        existingItem.setConditionLevel(itemDTO.getConditionLevel());
        existingItem.setImages(itemDTO.getImages());
        existingItem.setContactPhone(itemDTO.getContactPhone());
        existingItem.setContactWechat(itemDTO.getContactWechat());
        existingItem.setTradeLocation(itemDTO.getTradeLocation());
        existingItem.setUpdateTime(LocalDateTime.now());

        secondhandItemMapper.updateById(existingItem);
        log.info("用户{}更新二手商品成功，商品ID: {}", userId, itemDTO.getId());
        return Result.success("更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteItem(Long userId, Long itemId) {
        SecondhandItem existingItem = secondhandItemMapper.selectById(itemId);
        if (existingItem == null) {
            return Result.fail("商品不存在");
        }

        // 权限校验
        if (!existingItem.getSellerId().equals(userId)) {
            return Result.fail("无权删除此商品");
        }

        // 逻辑删除（MyBatis-Plus会自动处理@TableLogic）
        secondhandItemMapper.deleteById(itemId);
        log.info("用户{}删除二手商品成功，商品ID: {}", userId, itemId);
        return Result.success("删除成功");
    }

    @Override
    public Result<SecondhandItemVO> getItemDetail(Long itemId, Long userId) {
        SecondhandItem item = secondhandItemMapper.selectById(itemId);
        if (item == null) {
            return Result.fail("商品不存在");
        }

        // 递增浏览次数
        incrementViewCount(itemId);

        // 转换为VO
        SecondhandItemVO vo = convertToVO(item);

        // 填充卖家信息
        fillSellerInfo(vo);

        // 填充收藏状态
        if (userId != null) {
            vo.setIsFavorite(secondhandFavoriteService.isFavorited(userId, itemId));
        } else {
            vo.setIsFavorite(false);
        }

        return Result.success(vo);
    }

    @Override
    public Result<PageResult> listItems(Integer page, Integer pageSize, String category,
                                        Integer status, String sort, String keyword) {
        LambdaQueryWrapper<SecondhandItem> queryWrapper = new LambdaQueryWrapper<>();

        // 分类筛选
        if (category != null && !category.isEmpty()) {
            queryWrapper.eq(SecondhandItem::getCategory, category);
        }

        // 状态筛选（默认只显示在售商品）
        if (status != null) {
            queryWrapper.eq(SecondhandItem::getStatus, status);
        } else {
            queryWrapper.eq(SecondhandItem::getStatus, SecondhandStatusEnum.ON_SALE.getCode());
        }

        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(SecondhandItem::getTitle, keyword)
                    .or()
                    .like(SecondhandItem::getDescription, keyword));
        }

        // 排序
        if ("hottest".equals(sort)) {
            queryWrapper.orderByDesc(SecondhandItem::getViewCount);
        } else if ("price_asc".equals(sort)) {
            queryWrapper.orderByAsc(SecondhandItem::getPrice);
        } else if ("price_desc".equals(sort)) {
            queryWrapper.orderByDesc(SecondhandItem::getPrice);
        } else {
            queryWrapper.orderByDesc(SecondhandItem::getCreateTime);
        }

        Page<SecondhandItem> pageInfo = new Page<>(page, pageSize);
        Page<SecondhandItem> result = secondhandItemMapper.selectPage(pageInfo, queryWrapper);

        // 转换为VO列表
        List<SecondhandItemVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 填充卖家信息
        voList.forEach(this::fillSellerInfo);

        PageResult pageResult = PageResult.builder()
                .total(result.getTotal())
                .records(voList)
                .page(result.getCurrent())
                .size(result.getSize())
                .build();

        return Result.success(pageResult);
    }

    @Override
    public Result<List<SecondhandItemVO>> getUserPublishedItems(Long userId) {
        LambdaQueryWrapper<SecondhandItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SecondhandItem::getSellerId, userId);
        queryWrapper.orderByDesc(SecondhandItem::getCreateTime);

        List<SecondhandItem> items = secondhandItemMapper.selectList(queryWrapper);
        List<SecondhandItemVO> voList = items.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        voList.forEach(this::fillSellerInfo);
        return Result.success(voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> onSale(Long userId, Long itemId) {
        return updateItemStatus(userId, itemId, SecondhandStatusEnum.ON_SALE.getCode(), "上架");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> offSale(Long userId, Long itemId) {
        return updateItemStatus(userId, itemId, SecondhandStatusEnum.OFF_SHELF.getCode(), "下架");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> markAsSold(Long userId, Long itemId) {
        return updateItemStatus(userId, itemId, SecondhandStatusEnum.SOLD_OUT.getCode(), "标记为已售出");
    }

    @Override
    public String uploadImage(MultipartFile file) {
        // 验证文件
        validateImageFile(file);

        try {
            // 上传到腾讯云COS
            String imageUrl = tencentCOSAvatarUtil.uploadAvatar(file);
            log.info("图片上传成功: {}", imageUrl);
            return imageUrl;
        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new BaseException("图片上传失败: " + e.getMessage());
        }
    }

    @Override
    public void incrementViewCount(Long itemId) {
        SecondhandItem item = secondhandItemMapper.selectById(itemId);
        if (item != null) {
            item.setViewCount(item.getViewCount() + 1);
            secondhandItemMapper.updateById(item);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 更新商品状态
     */
    private Result<String> updateItemStatus(Long userId, Long itemId, Integer status, String operation) {
        SecondhandItem existingItem = secondhandItemMapper.selectById(itemId);
        if (existingItem == null) {
            return Result.fail("商品不存在");
        }

        if (!existingItem.getSellerId().equals(userId)) {
            return Result.fail("无权操作此商品");
        }

        existingItem.setStatus(status);
        existingItem.setUpdateTime(LocalDateTime.now());
        secondhandItemMapper.updateById(existingItem);

        log.info("用户{}{}商品成功，商品ID: {}", userId, operation, itemId);
        return Result.success(operation + "成功");
    }

    /**
     * 转换为VO
     */
    SecondhandItemVO convertToVO(SecondhandItem item) {
        SecondhandItemVO vo = new SecondhandItemVO();
        vo.setId(item.getId());
        vo.setSellerId(item.getSellerId());
        vo.setTitle(item.getTitle());
        vo.setDescription(item.getDescription());
        vo.setCategory(item.getCategory());
        vo.setPrice(item.getPrice());
        vo.setOriginalPrice(item.getOriginalPrice());
        vo.setConditionLevel(item.getConditionLevel());
        vo.setContactPhone(item.getContactPhone());
        vo.setContactWechat(item.getContactWechat());
        vo.setViewCount(item.getViewCount());
        vo.setFavoriteCount(item.getFavoriteCount());
        vo.setStatus(item.getStatus());
        vo.setTradeLocation(item.getTradeLocation());
        vo.setCreateTime(item.getCreateTime());
        vo.setUpdateTime(item.getUpdateTime());

        // 解析图片JSON
        if (item.getImages() != null && !item.getImages().isEmpty()) {
            try {
                List<String> images = objectMapper.readValue(item.getImages(), new TypeReference<List<String>>() {});
                vo.setImages(images);
            } catch (Exception e) {
                log.warn("解析图片JSON失败", e);
                vo.setImages(new ArrayList<>());
            }
        } else {
            vo.setImages(new ArrayList<>());
        }

        // 设置描述信息
        try {
            SecondhandCategoryEnum categoryEnum = SecondhandCategoryEnum.fromCode(item.getCategory());
            vo.setCategoryDescription(categoryEnum.getDescription());
        } catch (Exception e) {
            vo.setCategoryDescription("未知分类");
        }

        vo.setConditionDescription(getConditionDescription(item.getConditionLevel()));
        vo.setStatusDescription(getStatusDescription(item.getStatus()));

        return vo;
    }

    /**
     * 填充卖家信息
     */
    void fillSellerInfo(SecondhandItemVO vo) {
        User seller = userMapper.selectById(vo.getSellerId());
        if (seller != null) {
            vo.setSellerNickName(seller.getUserName());
            vo.setSellerAvatar(seller.getAvatar());
        }
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseException("上传文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BaseException("图片大小不能超过10MB");
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BaseException("只支持JPG、PNG、GIF格式的图片");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BaseException("文件类型不正确");
        }
    }

    /**
     * 获取新旧程度描述
     */
    private String getConditionDescription(Integer level) {
        return switch (level) {
            case 1 -> "全新";
            case 2 -> "95新";
            case 3 -> "9成新";
            case 4 -> "8成新";
            case 5 -> "7成新及以下";
            default -> "未知";
        };
    }

    /**
     * 获取状态描述
     */
    private String getStatusDescription(Integer status) {
        try {
            return SecondhandStatusEnum.fromCode(status).getDescription();
        } catch (Exception e) {
            return "未知状态";
        }
    }
}
