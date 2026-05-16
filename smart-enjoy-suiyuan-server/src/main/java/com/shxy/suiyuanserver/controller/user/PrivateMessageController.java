package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.annotation.RequireLogin;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.dto.PrivateMessageSendDTO;
import com.shxy.suiyuanentity.vo.ConversationVO;
import com.shxy.suiyuanentity.vo.MessageAckVO;
import com.shxy.suiyuanentity.vo.PrivateMessageVO;
import com.shxy.suiyuanentity.vo.UnreadCountVO;
import com.shxy.suiyuanserver.service.PrivateMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 私信消息 REST 接口
 */
@RestController
@RequestMapping("/user/message")
@Tag(name = "私信消息接口")
@RequireLogin
public class PrivateMessageController {

    @Resource
    private PrivateMessageService privateMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送私信消息")
    public Result<MessageAckVO> sendMessage(@Valid @RequestBody PrivateMessageSendDTO dto) {
        Long userId = BaseContext.getCurrentUserId();
        MessageAckVO ack = privateMessageService.sendMessage(
                userId, dto.getReceiverId(), dto.getMessageType(), dto.getContent(), dto.getClientMsgId());
        return Result.success(ack);
    }

    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表")
    public Result<List<ConversationVO>> getConversations() {
        Long userId = BaseContext.getCurrentUserId();
        return Result.success(privateMessageService.getConversations(userId));
    }

    @PostMapping("/conversation")
    @Operation(summary = "获取或创建与指定用户的会话")
    public Result<ConversationVO> getOrCreateConversation(@RequestBody Map<String, Long> params) {
        Long userId = BaseContext.getCurrentUserId();
        Long targetUserId = params.get("targetUserId");
        return Result.success(privateMessageService.getOrCreateConversation(userId, targetUserId));
    }

    @GetMapping("/history/{conversationId}")
    @Operation(summary = "获取历史消息")
    public Result<List<PrivateMessageVO>> getHistoryMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") Long fromSeq,
            @RequestParam(defaultValue = "50") Integer limit) {
        Long userId = BaseContext.getCurrentUserId();
        return Result.success(privateMessageService.getHistoryMessages(userId, conversationId, fromSeq, limit));
    }

    @PutMapping("/read/{conversationId}")
    @Operation(summary = "标记会话已读")
    public Result<String> markAsRead(
            @PathVariable Long conversationId,
            @RequestBody Map<String, Long> params) {
        Long userId = BaseContext.getCurrentUserId();
        Long lastReadSeq = params.get("lastReadSeq");
        privateMessageService.markAsRead(userId, conversationId, lastReadSeq);
        return Result.success("OK");
    }

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息数")
    public Result<UnreadCountVO> getUnreadCount(
            @RequestParam(required = false) Long conversationId) {
        Long userId = BaseContext.getCurrentUserId();
        return Result.success(privateMessageService.getUnreadCount(userId, conversationId));
    }
}
