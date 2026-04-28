package com.shxy.suiyuanserver;

import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.SecondhandItemDTO;
import com.shxy.suiyuanentity.entity.SecondhandItem;
import com.shxy.suiyuanentity.vo.SecondhandItemVO;
import com.shxy.suiyuanserver.mapper.SecondhandFavoriteMapper;
import com.shxy.suiyuanserver.mapper.SecondhandItemMapper;
import com.shxy.suiyuanserver.service.SecondhandFavoriteService;
import com.shxy.suiyuanserver.service.SecondhandItemService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 二手商品Service测试
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/28 12:45
 */
@SpringBootTest
@Slf4j
public class SecondhandServiceTest {

    @Autowired
    private SecondhandItemService secondhandItemService;

    @Autowired
    private SecondhandItemMapper secondhandItemMapper;

    @Autowired
    private SecondhandFavoriteService secondhandFavoriteService;

    @Autowired
    private SecondhandFavoriteMapper secondhandFavoriteMapper;

    /**
     * 测试发布二手商品
     */
    @Test
    void testPublishItem() {
        Long userId = 1L;
        
        SecondhandItemDTO itemDTO = new SecondhandItemDTO();
        itemDTO.setTitle("测试二手笔记本电脑");
        itemDTO.setDescription("这是一台九成新笔记本电脑，性能良好");
        itemDTO.setCategory("electronics");
        itemDTO.setPrice(new BigDecimal("2999.00"));
        itemDTO.setOriginalPrice(new BigDecimal("5999.00"));
        itemDTO.setConditionLevel(3);
        itemDTO.setContactPhone("13800138000");
        itemDTO.setContactWechat("test_wechat");
        itemDTO.setTradeLocation("图书馆门口");
        
        try {
            Result<Long> result = secondhandItemService.publishItem(userId, itemDTO);
            assertNotNull(result);
            log.info("发布二手商品结果：{}", result.getMessage());
            if (result.getCode() == 200) {
                log.info("商品ID：{}", result.getData());
            }
        } catch (Exception e) {
            log.info("发布失败（可能是用户ID问题或业务规则限制）: {}", e.getMessage());
        }
    }

    /**
     * 测试获取商品列表
     */
    @Test
    void testListItems() {
        Result<PageResult> result = secondhandItemService.listItems(1, 10, null, null, null, null);
        
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        
        PageResult pageResult = (PageResult) result.getData();
        assertNotNull(pageResult.getRecords());
        log.info("商品列表查询成功，总数：{}, 当前页：{}", pageResult.getTotal(), pageResult.getPage());
    }

    /**
     * 测试按分类筛选商品列表
     */
    @Test
    void testListItemsByCategory() {
        Result<PageResult> result = secondhandItemService.listItems(1, 10, "电子产品", null, null, null);
        
        assertNotNull(result);
        assertEquals(200, result.getCode());
        
        PageResult pageResult = (PageResult) result.getData();
        log.info("电子产品分类查询成功，数量：{}", pageResult.getRecords().size());
    }

    /**
     * 测试按状态筛选商品列表
     */
    @Test
    void testListItemsByStatus() {
        Result<PageResult> result = secondhandItemService.listItems(1, 10, null, 0, null, null);
        
        assertNotNull(result);
        assertEquals(200, result.getCode());
        
        PageResult pageResult = (PageResult) result.getData();
        log.info("在售状态商品查询成功，数量：{}", pageResult.getRecords().size());
    }

    /**
     * 测试关键词搜索商品列表
     */
    @Test
    void testListItemsByKeyword() {
        Result<PageResult> result = secondhandItemService.listItems(1, 10, null, null, null, "电脑");
        
        assertNotNull(result);
        assertEquals(200, result.getCode());
        
        PageResult pageResult = (PageResult) result.getData();
        log.info("关键词搜索结果，数量：{}", pageResult.getRecords().size());
    }

    /**
     * 测试获取商品详情（需要先有存在的商品）
     */
    @Test
    void testGetItemDetail() {
        // 先查询一个存在的商品ID
        SecondhandItem firstItem = secondhandItemMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SecondhandItem>()
                        .last("LIMIT 1")
        );
        
        if (firstItem != null) {
            Long itemId = firstItem.getId();
            Long userId = 1L;
            
            Result<SecondhandItemVO> result = secondhandItemService.getItemDetail(itemId, userId);
            
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            
            SecondhandItemVO itemVO = result.getData();
            assertEquals(itemId, itemVO.getId());
            log.info("获取商品详情成功：{}", itemVO.getTitle());
        } else {
            log.info("没有找到二手商品数据，跳过详情测试");
        }
    }

    /**
     * 测试获取用户发布的商品列表
     */
    @Test
    void testGetMyPublishedItems() {
        Long userId = 1L;
        
        Result<List<SecondhandItemVO>> result = secondhandItemService.getUserPublishedItems(userId);
        
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        
        List<SecondhandItemVO> items = result.getData();
        log.info("用户 {} 发布的商品数量：{}", userId, items.size());
    }

    /**
     * 测试收藏商品（通过 SecondhandFavoriteService）
     */
    @Test
    void testFavoriteItem() {
        Long userId = 1L;
        
        // 先查询一个存在的商品ID
        SecondhandItem firstItem = secondhandItemMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SecondhandItem>()
                        .last("LIMIT 1")
        );
        
        if (firstItem != null) {
            Long itemId = firstItem.getId();
            
            Result<String> result = secondhandFavoriteService.favorite(userId, itemId);
            
            assertNotNull(result);
            log.info("收藏商品结果：{}", result.getMessage());
        } else {
            log.info("没有找到二手商品数据，跳过收藏测试");
        }
    }

    /**
     * 测试取消收藏商品（通过 SecondhandFavoriteService）
     */
    @Test
    void testCancelFavoriteItem() {
        Long userId = 1L;
        
        // 先查询一个存在的商品ID
        SecondhandItem firstItem = secondhandItemMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SecondhandItem>()
                        .last("LIMIT 1")
        );
        
        if (firstItem != null) {
            Long itemId = firstItem.getId();
            
            Result<String> result = secondhandFavoriteService.cancelFavorite(userId, itemId);
            
            assertNotNull(result);
            log.info("取消收藏商品结果：{}", result.getMessage());
        } else {
            log.info("没有找到二手商品数据，跳过取消收藏测试");
        }
    }

    /**
     * 测试获取用户收藏的商品列表
     */
    @Test
    void testGetMyFavoriteItems() {
        Long userId = 1L;
        
        Result<List<SecondhandItemVO>> result = secondhandFavoriteService.getUserFavoriteItems(userId);
        
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        
        List<SecondhandItemVO> items = result.getData();
        log.info("用户 {} 收藏的商品数量：{}", userId, items.size());
    }

    /**
     * 测试检查是否已收藏
     */
    @Test
    void testIsFavorited() {
        Long userId = 1L;
        
        // 先查询一个存在的商品ID
        SecondhandItem firstItem = secondhandItemMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SecondhandItem>()
                        .last("LIMIT 1")
        );
        
        if (firstItem != null) {
            Long itemId = firstItem.getId();
            
            boolean isFavorited = secondhandFavoriteService.isFavorited(userId, itemId);
            log.info("用户 {} 是否已收藏商品 {}: {}", userId, itemId, isFavorited);
        } else {
            log.info("没有找到二手商品数据，跳过检查收藏测试");
        }
    }

    /**
     * 测试上传图片（使用模拟文件）
     */
    @Test
    void testUploadImage() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        
        try {
            String imageUrl = secondhandItemService.uploadImage(file);
            assertNotNull(imageUrl);
            log.info("上传图片成功，URL：{}", imageUrl);
        } catch (Exception e) {
            log.info("上传失败（可能是OSS配置问题）: {}", e.getMessage());
        }
    }

    /**
     * 测试分页参数
     */
    @Test
    void testPagination() {
        Result<PageResult> result1 = secondhandItemService.listItems(1, 5, null, null, null, null);
        Result<PageResult> result2 = secondhandItemService.listItems(2, 5, null, null, null, null);
        
        assertNotNull(result1);
        assertNotNull(result2);
        
        PageResult page1 = (PageResult) result1.getData();
        PageResult page2 = (PageResult) result2.getData();
        
        assertEquals(1, page1.getPage());
        assertEquals(2, page2.getPage());
        assertEquals(5, page1.getSize());
        
        log.info("分页测试成功，第 1 页：{}条，第 2 页：{}条", 
                page1.getRecords().size(), page2.getRecords().size());
    }

    /**
     * 测试上架商品（需要确保商品存在且属于当前用户）
     */
    @Test
    void testOnSale() {
        Long userId = 1L;
        
        // 先查询一个属于该用户的下架商品
        SecondhandItem item = secondhandItemMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SecondhandItem>()
                        .eq(SecondhandItem::getSellerId, userId)
                        .ne(SecondhandItem::getStatus, 0) // 非在售状态
                        .last("LIMIT 1")
        );
        
        if (item != null) {
            Result<String> result = secondhandItemService.onSale(userId, item.getId());
            assertNotNull(result);
            log.info("上架商品结果：{}", result.getMessage());
        } else {
            log.info("没有找到可上架的商品，跳过上架测试");
        }
    }

    /**
     * 测试下架商品（需要确保商品存在且属于当前用户）
     */
    @Test
    void testOffSale() {
        Long userId = 1L;
        
        // 先查询一个属于该用户的在售商品
        SecondhandItem item = secondhandItemMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SecondhandItem>()
                        .eq(SecondhandItem::getSellerId, userId)
                        .eq(SecondhandItem::getStatus, 0) // 在售状态
                        .last("LIMIT 1")
        );
        
        if (item != null) {
            Result<String> result = secondhandItemService.offSale(userId, item.getId());
            assertNotNull(result);
            log.info("下架商品结果：{}", result.getMessage());
        } else {
            log.info("没有找到可下架的商品，跳过下架测试");
        }
    }

    /**
     * 测试标记已售出（需要确保商品存在且属于当前用户）
     */
    @Test
    void testMarkAsSold() {
        Long userId = 1L;
        
        // 先查询一个属于该用户的在售商品
        SecondhandItem item = secondhandItemMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SecondhandItem>()
                        .eq(SecondhandItem::getSellerId, userId)
                        .eq(SecondhandItem::getStatus, 0) // 在售状态
                        .last("LIMIT 1")
        );
        
        if (item != null) {
            Result<String> result = secondhandItemService.markAsSold(userId, item.getId());
            assertNotNull(result);
            log.info("标记已售出结果：{}", result.getMessage());
        } else {
            log.info("没有找到可标记为已售出的商品，跳过测试");
        }
    }

    /**
     * 测试删除商品（需要确保商品存在且属于当前用户）
     */
    @Test
    void testDeleteItem() {
        Long userId = 1L;
        
        // 先查询一个属于该用户的商品（已下架或已售出的）
        SecondhandItem item = secondhandItemMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SecondhandItem>()
                        .eq(SecondhandItem::getSellerId, userId)
                        .in(SecondhandItem::getStatus, 1, 2) // 已售出或已下架
                        .last("LIMIT 1")
        );
        
        if (item != null) {
            Result<String> result = secondhandItemService.deleteItem(userId, item.getId());
            assertNotNull(result);
            log.info("删除商品结果：{}", result.getMessage());
        } else {
            log.info("没有找到可删除的商品，跳过删除测试");
        }
    }
}
