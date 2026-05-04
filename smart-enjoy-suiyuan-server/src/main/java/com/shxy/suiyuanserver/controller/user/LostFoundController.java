package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.annotation.RequireLogin;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.dto.LostFoundDTO;
import com.shxy.suiyuanentity.entity.LostFound;
import com.shxy.suiyuanentity.vo.LostFoundVO;
import com.shxy.suiyuanserver.service.LostFoundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 失物招领模块接口
 * @author Wu, Hui Ming
 * @version 2.0
 * @School Suihua University
 * @since 2026/4/4 21:00
 */
@RestController
@RequestMapping("/user/lost-found")
@Tag(name = "失物招领模块")
public class LostFoundController {

    @Autowired
    private LostFoundService lostFoundService;

    @PostMapping("publish")
    @RequireLogin
    @Operation(summary = "发布失物招领", description = "用户发布失物或招领信息")
    public Result<LostFound> createLostFound(@Valid @RequestBody LostFoundDTO lostFoundDTO) {
        return lostFoundService.createLostFound(lostFoundDTO);
    }

    @GetMapping("list")
    @Operation(summary = "获取失物招领列表", description = "分页获取失物招领信息，支持类型、状态和紧急程度筛选")
    public Result<PageResult> listLostFound(@RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
                                            @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) @Max(50) Integer pageSize,
                                            @RequestParam(value = "type", required = false) Integer type,
                                            @RequestParam(value = "status", required = false) Integer status,
                                            @RequestParam(value = "urgent",  required = false) Integer urgent,
                                            @RequestParam(value = "keyword", required = false) String keyword) {
        return lostFoundService.listLostFound(page, pageSize, type, status, urgent, keyword);
    }

    @GetMapping("detail/{id}")
    @Operation(summary = "获取失物招领详情", description = "根据ID获取失物招领的详细信息")
    public Result<LostFoundVO> detailLostFound(@PathVariable("id") Long id) {
        return lostFoundService.detailLostFound(id);
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    @Operation(summary = "删除失物招领", description = "删除指定的失物招领信息")
    public Result<String> deleteLostFound(@PathVariable("id") Long id) {
        return lostFoundService.deleteLostFound(id);
    }

    @PutMapping
    @RequireLogin
    @Operation(summary = "更新失物招领", description = "更新失物招领信息")
    public Result<String> updateLostFound(@Valid @RequestBody LostFoundDTO lostFoundDTO) {
        return lostFoundService.updateLostFound(lostFoundDTO);
    }

    @PutMapping("status/{id}")
    @RequireLogin
    @Operation(summary = "更新失物招领状态", description = "更新失物招领的处理状态")
    public Result<String> updateLostFoundStatus(@PathVariable("id") Long id,
                                                @RequestParam("status") Integer status) {
        return lostFoundService.updateLostFoundStatus(id, status);
    }

    @GetMapping("me/publish")
    @RequireLogin
    @Operation(summary = "我的失物招领", description = "获取当前用户发布的失物招领列表")
    public Result<List<LostFoundVO>> getMyPublishedLostFound() {
        return lostFoundService.getUserPublishedLostFound(BaseContext.getCurrentUserId());
    }

    /**
     * 获取所有未解决的失物招领记录（用于同步到向量库）
     * @return 失物招领列表
     */
    @GetMapping("/all-for-sync")
    @Operation(summary = "获取所有未解决记录", description = "供Python Agent同步使用")
    public Result<List<LostFoundVO>> getAllForSync() {
        return lostFoundService.getAllForSync();
    }

}
