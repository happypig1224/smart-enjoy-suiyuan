package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.entity.SecondhandItem;
import com.shxy.suiyuanentity.vo.SecondhandItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 二手商品Mapper
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/28
 */
@Mapper
public interface SecondhandItemMapper extends BaseMapper<SecondhandItem> {

    List<SecondhandItemVO> selectSecondhandItemListWithUser(@Param("category") String category,
                                                             @Param("status") Integer status,
                                                             @Param("keyword") String keyword,
                                                             @Param("offset") int offset,
                                                             @Param("pageSize") int pageSize,
                                                             @Param("orderBy") String orderBy);

    Long selectSecondhandItemCount(@Param("category") String category,
                                    @Param("status") Integer status,
                                    @Param("keyword") String keyword);
}
