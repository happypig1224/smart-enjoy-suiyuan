package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.annotation.RequireLogin;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.dto.ResourceDTO;
import com.shxy.suiyuanentity.vo.ResourceVO;
import com.shxy.suiyuanserver.service.ResourceFavoriteService;
import com.shxy.suiyuanserver.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 资源管理模块接口
 * @author WU, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4 20:57
 */
@RestController
@RequestMapping("/user/resource")
@Tag(name = "学习资源整合模块")
public class LearningResourcesController {
    
    private final ResourceService resourceService;

    private final ResourceFavoriteService resourceFavoriteService;

    public LearningResourcesController(ResourceService resourceService, ResourceFavoriteService resourceFavoriteService) {
        this.resourceService = resourceService;
        this.resourceFavoriteService = resourceFavoriteService;
    }

    @PostMapping("/upload/image")
    @Operation(summary = "通用图片上传", description = "用于失物招领等模块的图片上传，返回图片URL")
    public Result<String> uploadImage(@RequestParam("file") @NotNull(message = "上传文件不能为空") MultipartFile file) {
        String imageUrl = resourceService.uploadImage(file);
        return Result.success("上传成功", imageUrl);
    }

    /**
     * 学习资源列表接口
     * @param page 页码
     * @param pageSize 每页数量
     * @param type 类型筛选
     * @param sort 排序字段
     * @return 分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "学习资源列表", description = "分页获取学习资源列表，支持类型筛选、学科筛选和排序")
    public Result<PageResult> list(@RequestParam(value = "page", defaultValue = "1") @Min(value = 1, message = "页码最小值为1") Integer page,
                                   @RequestParam(value = "pageSize", defaultValue = "10") @Min(value = 1, message = "每页数量最小值为1") @Max(value = 50, message = "每页数量最大值为50") Integer pageSize,
                                   @RequestParam(value = "type", required = false)  String type,
                                   @RequestParam(value = "subject", required = false) Integer subject,
                                   @RequestParam(value = "sort", required = false)  String sort) {
        return resourceService.queryList(page, pageSize, type, subject, sort);
    }

    /**
     * 文件上传接口
     * @param file 文件
     * @param resourceDTO 资源创建信息
     * @return 资源 ID
     */
    @PostMapping("/upload")
    @Operation(summary = "上传学习资源", description = "用户上传学习资源文件及元数据")
    public Result<Long> upload(@RequestParam("file") @NotNull(message = "上传文件不能为空") MultipartFile file,
                               @Valid ResourceDTO resourceDTO) {
        return resourceService.uploadResource(file, resourceDTO);
    }

    /**
     * 删除资源接口
     * @param id 资源 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除学习资源", description = "删除指定的学习资源")
    public Result<String> delete(@PathVariable("id") @NotNull(message = "资源ID不能为空") @Min(value = 1, message = "资源ID必须大于0") Long id) {
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
    @PostMapping("/favorite/{id}")
    @RequireLogin
    @Operation(summary = "收藏学习资源", description = "用户收藏指定的学习资源")
    public Result<String> favorite(@PathVariable("id") @NotNull(message = "资源ID不能为空") @Min(value = 1, message = "资源ID必须大于0") Long id) {
        Long userId = BaseContext.getCurrentUserId();
        return resourceFavoriteService.favorite(userId, id);
    }

    /**
     * 取消收藏资源接口
     * @param id 资源 ID
     * @return 操作结果
     */
    @DeleteMapping("/favorite/{id}")
    @RequireLogin
    @Operation(summary = "取消收藏资源", description = "取消用户对指定学习资源的收藏")
    public Result<String> cancelFavorite(@PathVariable("id") @NotNull(message = "资源ID不能为空") @Min(value = 1, message = "资源ID必须大于0") Long id) {
        Long userId = BaseContext.getCurrentUserId();
        return resourceFavoriteService.cancelFavorite(userId, id);
    }

    /**
     * 获取资源详情接口
     * @param id 资源 ID
     * @return 资源详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取资源详情", description = "根据ID获取学习资源的详细信息")
    public Result<ResourceVO> getResourceDetail(@PathVariable("id") @NotNull(message = "资源ID不能为空") @Min(value = 1, message = "资源ID必须大于0") Long id) {
        Long userId = BaseContext.getCurrentUserId();
        return resourceService.getResourceDetail(id, userId);
    }

    /**
     * 下载资源接口
     * @param id 资源 ID
     * @return 资源下载URL
     */
    @GetMapping("/download/{id}")
    @Operation(summary = "下载学习资源", description = "获取资源下载链接，同时递增下载次数")
    public Result<String> download(@PathVariable("id") @NotNull(message = "资源ID不能为空") @Min(value = 1, message = "资源ID必须大于0") Long id) {
        return resourceService.downloadResource(id);
    }

    /**
     * 更新资源信息接口
     * @param id 资源 ID
     * @param resourceDTO 资源更新信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新学习资源", description = "更新资源的描述、分类等元数据信息")
    public Result<String> update(@PathVariable("id") @NotNull(message = "资源ID不能为空") @Min(value = 1, message = "资源ID必须大于0") Long id,
                                  @RequestBody @Valid ResourceDTO resourceDTO) {
        Long userId = BaseContext.getCurrentUserId();
        return resourceService.updateResource(id, userId, resourceDTO);
    }
    /**
     * 我的收藏资源接口
     * @return 收藏的资源列表
     */
    @GetMapping("/me/favorite")
    @Operation(summary = "我的收藏资源", description = "获取当前用户收藏的所有学习资源")
    public Result<List<ResourceVO>> getMyFavoriteResources() {
        Long userId = BaseContext.getCurrentUserId();
        return resourceFavoriteService.getUserFavoriteResources(userId);
    }
}
