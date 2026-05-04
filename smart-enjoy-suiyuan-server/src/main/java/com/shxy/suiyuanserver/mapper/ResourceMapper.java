package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.entity.Resource;
import com.shxy.suiyuanentity.vo.ResourceVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Wu, Hui Ming
* @description 针对表【resource】的数据库操作Mapper
* @createDate 2026-04-04 21:30:08
* @Entity com.shxy.entity.Resource
*/
public interface ResourceMapper extends BaseMapper<Resource> {

    List<ResourceVO> selectResourceListWithUser(@Param("type") String type,
                                                 @Param("subject") Integer subject,
                                                 @Param("keyword") String keyword,
                                                 @Param("offset") int offset,
                                                 @Param("pageSize") int pageSize,
                                                 @Param("orderBy") String orderBy);

    Long selectResourceCount(@Param("type") String type,
                              @Param("subject") Integer subject,
                              @Param("keyword") String keyword);
}




