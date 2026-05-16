package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.entity.PrivateMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 私信消息 Mapper
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @since 2026/5/12
 */
@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {
}
