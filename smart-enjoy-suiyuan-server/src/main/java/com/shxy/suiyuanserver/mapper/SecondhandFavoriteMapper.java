package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.entity.SecondhandFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 二手商品收藏Mapper
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/28
 */
@Mapper
public interface SecondhandFavoriteMapper extends BaseMapper<SecondhandFavorite> {

    /**
     * 统计用户收藏的二手商品数
     */
    @Select("SELECT COUNT(*) FROM secondhand_favorite WHERE user_id = #{userId}")
    Integer countByUserId(Long userId);
}
