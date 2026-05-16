package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.entity.UserReadCursor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户已读位点 Mapper
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @since 2026/5/12
 */
@Mapper
public interface UserReadCursorMapper extends BaseMapper<UserReadCursor> {
}
