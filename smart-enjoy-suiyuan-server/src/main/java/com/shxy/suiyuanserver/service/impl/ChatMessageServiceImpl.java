package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shxy.suiyuanentity.entity.ChatMessage;
import com.shxy.suiyuanserver.service.ChatMessageService;
import com.shxy.suiyuanserver.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author Wu, Hui Ming
* @description 针对表【chat_message】的数据库操作Service实现
* @createDate 2026-04-04 21:30:08
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService{

}




