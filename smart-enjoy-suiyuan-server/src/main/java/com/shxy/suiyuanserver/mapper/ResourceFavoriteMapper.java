package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.entity.ResourceFavorite;
import com.shxy.suiyuanentity.vo.ResourceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源收藏 Mapper
 * @author Wu, Hui Ming
 * @since 2026/4/10
 */
@Mapper
public interface ResourceFavoriteMapper extends BaseMapper<ResourceFavorite> {

    /**
     * 检查是否已收藏
     * @param userId 用户 ID
     * @param resourceId 资源 ID
     * @return 收藏记录
     */
    ResourceFavorite checkFavorite(@Param("userId") Long userId, @Param("resourceId") Long resourceId);

    /**
     * 添加收藏
     * @param userId 用户 ID
     * @param resourceId 资源 ID
     * @return 影响行数
     */
    int addFavorite(@Param("userId") Long userId, @Param("resourceId") Long resourceId);

    /**
     * 取消收藏
     * @param userId 用户 ID
     * @param resourceId 资源 ID
     * @return 影响行数
     */
    int cancelFavorite(@Param("userId") Long userId, @Param("resourceId") Long resourceId);

    /**
     * 判断是否已收藏
     * @param userId 用户 ID
     * @param resourceId 资源 ID
     * @return true-已收藏，false-未收藏
     */
    boolean isFavorite(@Param("userId") Long userId, @Param("resourceId") Long resourceId);

    /**
     * 获取用户收藏的资源
     * @param userId 用户 ID
     * @return 收藏的资源列表
     */
    List<Long> getUserFavoriteResources(Long userId);
}