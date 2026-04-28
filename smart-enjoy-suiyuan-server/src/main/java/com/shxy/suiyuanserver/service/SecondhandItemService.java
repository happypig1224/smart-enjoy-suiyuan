package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.SecondhandItemDTO;
import com.shxy.suiyuanentity.entity.SecondhandItem;
import com.shxy.suiyuanentity.vo.SecondhandItemVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 二手商品Service
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/28 09:10
 */
public interface SecondhandItemService extends IService<SecondhandItem> {

    /**
     * 发布二手商品
     * @param userId 用户ID
     * @param itemDTO 商品信息
     * @return 商品ID
     */
    Result<Long> publishItem(Long userId, SecondhandItemDTO itemDTO);

    /**
     * 更新二手商品
     * @param userId 用户ID
     * @param itemDTO 商品信息
     * @return 操作结果
     */
    Result<String> updateItem(Long userId, SecondhandItemDTO itemDTO);

    /**
     * 删除二手商品
     * @param userId 用户ID
     * @param itemId 商品ID
     * @return 操作结果
     */
    Result<String> deleteItem(Long userId, Long itemId);

    /**
     * 获取商品详情
     * @param itemId 商品ID
     * @param userId 用户ID（用于判断是否收藏）
     * @return 商品详情
     */
    Result<SecondhandItemVO> getItemDetail(Long itemId, Long userId);

    /**
     * 分页查询商品列表
     * @param page 页码
     * @param pageSize 每页数量
     * @param category 分类筛选
     * @param status 状态筛选
     * @param sort 排序方式
     * @param keyword 关键词搜索
     * @return 分页结果
     */
    Result<PageResult> listItems(Integer page, Integer pageSize, String category, 
                                  Integer status, String sort, String keyword);

    /**
     * 获取用户发布的商品列表
     * @param userId 用户ID
     * @return 商品列表
     */
    Result<List<SecondhandItemVO>> getUserPublishedItems(Long userId);

    /**
     * 上架商品
     * @param userId 用户ID
     * @param itemId 商品ID
     * @return 操作结果
     */
    Result<String> onSale(Long userId, Long itemId);

    /**
     * 下架商品
     * @param userId 用户ID
     * @param itemId 商品ID
     * @return 操作结果
     */
    Result<String> offSale(Long userId, Long itemId);

    /**
     * 标记为已售出
     * @param userId 用户ID
     * @param itemId 商品ID
     * @return 操作结果
     */
    Result<String> markAsSold(Long userId, Long itemId);

    /**
     * 上传图片
     * @param file 图片文件
     * @return 图片URL
     */
    String uploadImage(MultipartFile file);

    /**
     * 递增浏览次数
     * @param itemId 商品ID
     */
    void incrementViewCount(Long itemId);
}
