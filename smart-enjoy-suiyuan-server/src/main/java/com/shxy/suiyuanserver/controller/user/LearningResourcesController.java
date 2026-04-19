package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.dto.ResourceCreateDTO;
import com.shxy.suiyuanentity.vo.ResourceVO;
import com.shxy.suiyuanserver.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 学习资源管理 Controller
 * @author WU, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4 20:57
 */
@RestController
@RequestMapping("/user/resource")
@Tag(name = "学习资源整合模块")
public class LearningResourcesController {
    
    @Autowired
    private ResourceService resourceService;

    /**
     * 学习资源列表接口
     * @param page 页码
     * @param pageSize 每页数量
     * @param type 类型筛选
     * @param sort 排序字段
     * @param order 排序方式
     * @return 分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "学习资源列表", description = "分页获取学习资源列表，支持类型筛选和排序")
    public Result<PageResult> list(@RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
                                   @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) @Max(50) Integer pageSize,
                                   @RequestParam(value = "type", required = false) String type,
                                   @RequestParam(value = "sort", required = false) String sort,
                                   @RequestParam(value = "order", defaultValue = "desc") String order) {
        return resourceService.queryList(page, pageSize, type, sort, order);
    }

    /**
     * 文件上传接口
     * @param file 文件
     * @param resourceCreateDTO 资源创建信息
     * @return 资源 ID
     */
    @PostMapping("/upload")
    @Operation(summary = "上传学习资源", description = "用户上传学习资源文件及元数据")
    public Result<Long> upload(@RequestParam("file") MultipartFile file,
                               @Valid ResourceCreateDTO resourceCreateDTO) {
        return resourceService.uploadResource(file, resourceCreateDTO);
    }

    /**
     * 删除资源接口
     * @param id 资源 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除学习资源", description = "删除指定的学习资源")
    public Result<String> delete(@PathVariable("id") Long id) {
        Long userId = BaseContext.getCurrentUserId();
        return resourceService.deleteResource(userId, id);
    }

    /**
     * 我的资源发布管理接口
     * @return 资源列表
     */
    @GetMapping("/me/publish")
    @Operation(summary = "我的发布资源", description = "获取当前用户发布的所有学习资源")
    public Result<List<ResourceVO>> getMyPublishedResources() {
        Long userId = BaseContext.getCurrentUserId();
        return resourceService.getUserPublishedResources(userId);
    }

    /**
     * 收藏资源接口
     * @param id 资源 ID
     * @return 操作结果
     */
    @GetMapping("/favorite/{id}")
    @Operation(summary = "收藏学习资源", description = "用户收藏指定的学习资源")
    public Result<String> favorite(@PathVariable("id") Long id) {
        Long userId = BaseContext.getCurrentUserId();
        return resourceService.favoriteResource(userId, id);
    }

    /**
     * 取消收藏资源接口
     * @param id 资源 ID
     * @return 操作结果
     */
    @DeleteMapping("/favorite/cancel/{id}")
    @Operation(summary = "取消收藏资源", description = "取消用户对指定学习资源的收藏")
    public Result<String> cancelFavorite(@PathVariable("id") Long id) {
        Long userId = BaseContext.getCurrentUserId();
        return resourceService.cancelFavoriteResource(userId, id);
    }

    /**
     * 获取资源详情接口
     * @param id 资源 ID
     * @return 资源详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取资源详情", description = "根据ID获取学习资源的详细信息")
    public Result<ResourceVO> getResourceDetail(@PathVariable("id") Long id) {
        Long userId = BaseContext.getCurrentUserId();
        return resourceService.getResourceDetail(id, userId);
    }
}
