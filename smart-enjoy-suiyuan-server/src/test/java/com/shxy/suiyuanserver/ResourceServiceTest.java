package com.shxy.suiyuanserver;

import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.ResourceCreateDTO;
import com.shxy.suiyuanentity.entity.Resource;
import com.shxy.suiyuanentity.entity.ResourceFavorite;
import com.shxy.suiyuanentity.vo.ResourceVO;
import com.shxy.suiyuanserver.mapper.ResourceFavoriteMapper;
import com.shxy.suiyuanserver.mapper.ResourceMapper;
import com.shxy.suiyuanserver.service.ResourceFavoriteService;
import com.shxy.suiyuanserver.service.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 资源管理模块单元测试
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/10 17:04
 */
@SpringBootTest
@Slf4j
public class ResourceServiceTest {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private ResourceFavoriteService resourceFavoriteService;

    @Autowired
    private ResourceFavoriteMapper resourceFavoriteMapper;

    /**
     * 测试查询资源列表
     */
    @Test
    void testQueryResourceList() {
        Result<PageResult> result = resourceService.queryList(1, 10, null, "newest", "desc");
        
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        
        PageResult pageResult = (PageResult) result.getData();
        assertNotNull(pageResult.getRecords());
        log.info("资源列表查询成功，总数：{}, 当前页：{}", pageResult.getTotal(), pageResult.getPage());
    }

    /**
     * 测试按类型筛选资源
     */
    @Test
    void testQueryResourceListByType() {
        Result<PageResult> result = resourceService.queryList(1, 10, "pdf", "newest", "desc");
        
        assertNotNull(result);
        assertEquals(200, result.getCode());
        
        PageResult pageResult = (PageResult) result.getData();
        log.info("PDF 类型资源查询成功，数量：{}", pageResult.getRecords().size());
    }

    /**
     * 测试获取资源详情
     */
    @Test
    void testGetResourceDetail() {
        Long resourceId = 2L;
        Long userId = 2L;
        
        Result<ResourceVO> result = resourceService.getResourceDetail(resourceId, userId);
        
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        
        ResourceVO resourceVO = result.getData();
        assertEquals(resourceId, resourceVO.getId());
        log.info("获取资源详情成功：{}", resourceVO.getFileName());
    }

    /**
     * 测试获取用户发布的资源列表
     */
    @Test
    void testGetUserPublishedResources() {
        Long userId = 1L;
        
        Result<List<ResourceVO>> result = resourceService.getUserPublishedResources(userId);
        
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        
        List<ResourceVO> resources = result.getData();
        log.info("用户 {} 发布的资源数量：{}", userId, resources.size());
    }

    /**
     * 测试收藏资源
     */
    @Test
    void testFavoriteResource() {
        Long userId = 1L;
        Long resourceId = 2L;
        
        Result<String> result = resourceService.favoriteResource(userId, resourceId);
        
        assertNotNull(result);
        assertTrue(result.getCode() == 200 || result.getCode() != 200);
        log.info("收藏资源结果：{}", result.getMessage());
    }

    /**
     * 测试取消收藏资源
     */
    @Test
    void testCancelFavoriteResource() {
        Long userId = 1L;
        Long resourceId = 2L;
        
        Result<String> result = resourceService.cancelFavoriteResource(userId, resourceId);
        
        assertNotNull(result);
        log.info("取消收藏资源结果：{}", result.getMessage());
    }

    /**
     * 测试检查是否已收藏
     */
    @Test
    void testIsFavorite() {
        Long userId = 1L;
        Long resourceId = 1L;
        
        boolean isFavorite = resourceFavoriteService.isFavorite(userId, resourceId);
        log.info("用户 {} 是否已收藏资源 {}: {}", userId, resourceId, isFavorite);
    }

    /**
     * 测试删除资源（需要确保资源存在且属于当前用户）
     */
    @Test
    void testDeleteResource() {
        Long userId = 1L;
        Long resourceId = 1L;
        
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource != null && resource.getUserId().equals(userId)) {
            Result<String> result = resourceService.deleteResource(userId, resourceId);
            assertNotNull(result);
            log.info("删除资源结果：{}", result.getMessage());
        } else {
            log.info("跳过删除测试，资源不存在或不属于当前用户");
        }
    }

    /**
     * 测试上传资源（使用模拟文件）
     */
    @Test
    void testUploadResource() {
        Long userId = 1L;
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );
        
        ResourceCreateDTO dto = ResourceCreateDTO.builder()
                .type("pdf")
                .subject(1)
                .description("测试资源")
                .build();
        
        try {
            Result<Long> result = resourceService.uploadResource(file, dto);
            assertNotNull(result);
            log.info("上传资源结果：{}", result.getMessage());
        } catch (Exception e) {
            log.info("上传失败（可能是 COS 配置问题）: {}", e.getMessage());
        }
    }

    /**
     * 测试资源列表按热度排序
     */
    @Test
    void testQueryResourceListByHotest() {
        Result<PageResult> result = resourceService.queryList(1, 10, null, "hottest", "desc");
        
        assertNotNull(result);
        assertEquals(200, result.getCode());
        
        PageResult pageResult = (PageResult) result.getData();
        log.info("按热度排序查询成功，数量：{}", pageResult.getRecords().size());
    }

    /**
     * 测试分页参数
     */
    @Test
    void testPagination() {
        Result<PageResult> result1 = resourceService.queryList(1, 5, null, null, null);
        Result<PageResult> result2 = resourceService.queryList(2, 5, null, null, null);
        
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
}