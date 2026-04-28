package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.annotation.RequireLogin;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.dto.SecondhandItemDTO;
import com.shxy.suiyuanentity.vo.SecondhandItemVO;
import com.shxy.suiyuanserver.service.SecondhandFavoriteService;
import com.shxy.suiyuanserver.service.SecondhandItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 二手交易市场模块接口
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/28 09:09
 */
@RestController
@RequestMapping("/user/secondhand")
@Tag(name = "二手交易市场模块")
public class SecondhandController {

    private final SecondhandItemService secondhandItemService;
    private final SecondhandFavoriteService secondhandFavoriteService;

    public SecondhandController(SecondhandItemService secondhandItemService,
                                SecondhandFavoriteService secondhandFavoriteService) {
        this.secondhandItemService = secondhandItemService;
        this.secondhandFavoriteService = secondhandFavoriteService;
    }


    @PostMapping("/publish")
    @RequireLogin
    @Operation(summary = "发布二手商品", description = "用户发布二手商品信息")
    public Result<Long> publishItem(@Valid @RequestBody SecondhandItemDTO itemDTO) {
        Long userId = BaseContext.getCurrentUserId();
        return secondhandItemService.publishItem(userId, itemDTO);
    }

    @PutMapping("/update")
    @RequireLogin
    @Operation(summary = "更新二手商品", description = "更新已发布的商品信息")
    public Result<String> updateItem(@Valid @RequestBody SecondhandItemDTO itemDTO) {
        Long userId = BaseContext.getCurrentUserId();
        return secondhandItemService.updateItem(userId, itemDTO);
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    @Operation(summary = "删除二手商品", description = "删除已发布的商品")
    public Result<String> deleteItem(@PathVariable("id") @NotNull(message = "商品ID不能为空") Long id) {
        Long userId = BaseContext.getCurrentUserId();
        return secondhandItemService.deleteItem(userId, id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情", description = "根据ID获取二手商品的详细信息，包含卖家联系方式")
    public Result<SecondhandItemVO> getItemDetail(@PathVariable("id") @NotNull(message = "商品ID不能为空") Long id) {
        Long userId = BaseContext.getCurrentUserId();
        return secondhandItemService.getItemDetail(id, userId);
    }

    @GetMapping("/list")
    @Operation(summary = "商品列表", description = "分页获取二手商品列表，支持分类、状态筛选和排序")
    public Result<PageResult> listItems(
            @RequestParam(value = "page", defaultValue = "1") @Min(value = 1, message = "页码最小值为1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(value = 1, message = "每页数量最小值为1") @Max(value = 50, message = "每页数量最大值为50") Integer pageSize,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return secondhandItemService.listItems(page, pageSize, category, status, sort, keyword);
    }

    @GetMapping("/me/publish")
    @RequireLogin
    @Operation(summary = "我的发布", description = "获取当前用户发布的所有二手商品")
    public Result<List<SecondhandItemVO>> getMyPublishedItems() {
        Long userId = BaseContext.getCurrentUserId();
        return secondhandItemService.getUserPublishedItems(userId);
    }


    @PostMapping("/on-sale/{id}")
    @RequireLogin
    @Operation(summary = "上架商品", description = "将商品标记为在售状态")
    public Result<String> onSale(@PathVariable("id") @NotNull(message = "商品ID不能为空") Long id) {
        Long userId = BaseContext.getCurrentUserId();
        return secondhandItemService.onSale(userId, id);
    }

    @PostMapping("/off-sale/{id}")
    @RequireLogin
    @Operation(summary = "下架商品", description = "将商品标记为下架状态")
    public Result<String> offSale(@PathVariable("id") @NotNull(message = "商品ID不能为空") Long id) {
        Long userId = BaseContext.getCurrentUserId();
        return secondhandItemService.offSale(userId, id);
    }

    @PostMapping("/sold/{id}")
    @RequireLogin
    @Operation(summary = "标记已售出", description = "将商品标记为已售出状态")
    public Result<String> markAsSold(@PathVariable("id") @NotNull(message = "商品ID不能为空") Long id) {
        Long userId = BaseContext.getCurrentUserId();
        return secondhandItemService.markAsSold(userId, id);
    }


    @PostMapping("/upload/image")
    @RequireLogin
    @Operation(summary = "上传图片", description = "上传商品图片，返回图片URL")
    public Result<String> uploadImage(@RequestParam("file") @NotNull(message = "上传文件不能为空") MultipartFile file) {
        String imageUrl = secondhandItemService.uploadImage(file);
        return Result.success("上传成功", imageUrl);
    }


    @PostMapping("/favorite/{id}")
    @RequireLogin
    @Operation(summary = "收藏商品", description = "用户收藏指定的二手商品")
    public Result<String> favorite(@PathVariable("id") @NotNull(message = "商品ID不能为空") Long id) {
        Long userId = BaseContext.getCurrentUserId();
        return secondhandFavoriteService.favorite(userId, id);
    }

    @DeleteMapping("/favorite/{id}")
    @RequireLogin
    @Operation(summary = "取消收藏", description = "取消用户对指定二手商品的收藏")
    public Result<String> cancelFavorite(@PathVariable("id") @NotNull(message = "商品ID不能为空") Long id) {
        Long userId = BaseContext.getCurrentUserId();
        return secondhandFavoriteService.cancelFavorite(userId, id);
    }

    @GetMapping("/me/favorite")
    @RequireLogin
    @Operation(summary = "我的收藏", description = "获取当前用户收藏的所有二手商品")
    public Result<List<SecondhandItemVO>> getMyFavoriteItems() {
        Long userId = BaseContext.getCurrentUserId();
        return secondhandFavoriteService.getUserFavoriteItems(userId);
    }
}
