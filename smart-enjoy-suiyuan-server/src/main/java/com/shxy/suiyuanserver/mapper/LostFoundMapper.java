package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.dto.LostFoundDTO;
import com.shxy.suiyuanentity.entity.LostFound;

/**
* @author huang qi long
* @description 针对表【lost_found】的数据库操作Mapper
* @createDate 2026-04-04 21:30:08
* @Entity com.shxy.entity.LostFound
*/
public interface LostFoundMapper extends BaseMapper<LostFound> {

    Integer updateLostFound(LostFoundDTO lostFoundDTO);

    Integer updateLostFoundStatus(Long id, Integer status);
}




