package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.entity.SecondhandFavorite;
import com.shxy.suiyuanentity.entity.SecondhandItem;
import com.shxy.suiyuanentity.vo.SecondhandItemVO;
import com.shxy.suiyuanserver.mapper.SecondhandFavoriteMapper;
import com.shxy.suiyuanserver.mapper.SecondhandItemMapper;
import com.shxy.suiyuanserver.mapper.UserMapper;
import com.shxy.suiyuanserver.service.SecondhandFavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 二手商品收藏Service实现类
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/28 09:11
 */
@Service
@Slf4j
public class SecondhandFavoriteServiceImpl extends ServiceImpl<SecondhandFavoriteMapper, SecondhandFavorite>
        implements SecondhandFavoriteService {

    private final SecondhandFavoriteMapper secondhandFavoriteMapper;
    private final SecondhandItemMapper secondhandItemMapper;
    private final UserMapper userMapper;
    private final SecondhandItemServiceImpl secondhandItemService;

    public SecondhandFavoriteServiceImpl(SecondhandFavoriteMapper secondhandFavoriteMapper,
                                         SecondhandItemMapper secondhandItemMapper,
                                         UserMapper userMapper,
                                         @Lazy SecondhandItemServiceImpl secondhandItemService) {
        this.secondhandFavoriteMapper = secondhandFavoriteMapper;
        this.secondhandItemMapper = secondhandItemMapper;
        this.userMapper = userMapper;
        this.secondhandItemService = secondhandItemService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> favorite(Long userId, Long itemId) {
        // 检查商品是否存在
        SecondhandItem item = secondhandItemMapper.selectById(itemId);
        if (item == null) {
            return Result.fail("商品不存在");
        }

        // 检查是否已收藏
        LambdaQueryWrapper<SecondhandFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SecondhandFavorite::getUserId, userId);
        queryWrapper.eq(SecondhandFavorite::getItemId, itemId);
        if (secondhandFavoriteMapper.selectCount(queryWrapper) > 0) {
            return Result.fail("已经收藏过该商品");
        }

        // 创建收藏记录
        SecondhandFavorite favorite = SecondhandFavorite.builder()
                .userId(userId)
                .itemId(itemId)
                .build();
        secondhandFavoriteMapper.insert(favorite);

        // 更新商品收藏数
        item.setFavoriteCount(item.getFavoriteCount() + 1);
        secondhandItemMapper.updateById(item);

        log.info("用户{}收藏商品{}成功", userId, itemId);
        return Result.success("收藏成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelFavorite(Long userId, Long itemId) {
        // 查找收藏记录
        LambdaQueryWrapper<SecondhandFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SecondhandFavorite::getUserId, userId);
        queryWrapper.eq(SecondhandFavorite::getItemId, itemId);
        SecondhandFavorite favorite = secondhandFavoriteMapper.selectOne(queryWrapper);

        if (favorite == null) {
            return Result.fail("未收藏该商品");
        }

        // 删除收藏记录
        secondhandFavoriteMapper.delete(queryWrapper);

        // 更新商品收藏数
        SecondhandItem item = secondhandItemMapper.selectById(itemId);
        if (item != null && item.getFavoriteCount() > 0) {
            item.setFavoriteCount(item.getFavoriteCount() - 1);
            secondhandItemMapper.updateById(item);
        }

        log.info("用户{}取消收藏商品{}成功", userId, itemId);
        return Result.success("取消收藏成功");
    }

    @Override
    public Result<List<SecondhandItemVO>> getUserFavoriteItems(Long userId) {
        // 查询用户收藏的商品ID列表
        LambdaQueryWrapper<SecondhandFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SecondhandFavorite::getUserId, userId);
        queryWrapper.orderByDesc(SecondhandFavorite::getCreateTime);
        List<SecondhandFavorite> favorites = secondhandFavoriteMapper.selectList(queryWrapper);

        if (favorites.isEmpty()) {
            return Result.success(List.of());
        }

        // 获取商品详情
        List<Long> itemIds = favorites.stream()
                .map(SecondhandFavorite::getItemId)
                .collect(Collectors.toList());

        List<SecondhandItem> items = secondhandItemMapper.selectBatchIds(itemIds);
        List<SecondhandItemVO> voList = items.stream()
                .map(secondhandItemService::convertToVO)
                .collect(Collectors.toList());

        // 填充卖家信息
        voList.forEach(secondhandItemService::fillSellerInfo);

        return Result.success(voList);
    }

    @Override
    public boolean isFavorited(Long userId, Long itemId) {
        LambdaQueryWrapper<SecondhandFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SecondhandFavorite::getUserId, userId);
        queryWrapper.eq(SecondhandFavorite::getItemId, itemId);
        return secondhandFavoriteMapper.selectCount(queryWrapper) > 0;
    }
}
