package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.vo.ChatMessageVO;
import com.shxy.suiyuanentity.vo.ChatResponseVO;
import com.shxy.suiyuanentity.vo.SessionVO;
import com.shxy.suiyuanserver.service.AiChatService;
import jakarta.annotation.Resource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/27 17:39
 */
@RestController
@RequestMapping("/user/ai/chat")
@Tag(name = "AI助手接口")
public class AiChatController {

    @Resource
    private AiChatService aiChatService;

    private static final Logger log= LoggerFactory.getLogger(AiChatController.class);

    /**
     * 发送消息给AI - 非流式版本
     */
    @PostMapping("/send")
    @Operation(summary = "发送消息给AI")
    public Result<ChatResponseVO> sendChatMessage(@RequestBody Map<String, Object> requestParams) {

        String query = (String) requestParams.get("query");
        Object sessionIdObj = requestParams.get("sessionId");
        Long sessionId = (sessionIdObj != null && !sessionIdObj.toString().trim().isEmpty()) ?
                Long.valueOf(sessionIdObj.toString()) : null;

        if (query == null || query.trim().isEmpty()) {
            return Result.fail("消息不能为空");
        }

        ChatResponseVO response = aiChatService.chat(query, sessionId);

        return Result.success(response);
    }

    @PostMapping(value = "/send/stream", produces = "text/event-stream")
    @Operation(summary = "发送消息给AI（流式输出）")
    public void sendChatMessageStream(@RequestBody Map<String, Object> requestParams, HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        String query = (String) requestParams.get("query");
        Object sessionIdObj = requestParams.get("sessionId");
        Long sessionId = (sessionIdObj != null && !sessionIdObj.toString().trim().isEmpty()) ?
                Long.valueOf(sessionIdObj.toString()) : null;

        if (query == null || query.trim().isEmpty()) {
            try {
                response.getWriter().write("data: 消息不能为空\n\n");
                response.getWriter().flush();
            } catch (Exception e) {
                log.error("发送错误消息失败", e);
            }
            return;
        }

        try {
            aiChatService.chatStream(query, sessionId, response);
        } catch (Exception e) {
            log.error("流式聊天处理异常", e);
            try {
                response.getWriter().write("data: 服务暂时不可用，请稍后重试\n\n");
                response.getWriter().flush();
            } catch (Exception ex) {
                log.error("发送错误消息失败", ex);
            }
        }
    }

    @GetMapping("/history")
    @Operation(summary = "获取所有历史会话")
    public Result<List<SessionVO>> getHistory() {
        return Result.success(aiChatService.getHistory());
    }
    @GetMapping("/history/message")
    @Operation(summary = "获取当前会话历史消息")
    public Result<List<ChatMessageVO>> getHistoryMessage(@RequestParam(required = false) Long sessionId) {
        return Result.success(aiChatService.getHistoryMessage(sessionId));
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "删除指定会话")
    public Result<String> deleteSession(@PathVariable Long sessionId) {
        return aiChatService.deleteSession(sessionId);
    }

    @PutMapping("/session/{sessionId}/rename")
    @Operation(summary = "重命名会话")
    public Result<String> renameSession(@PathVariable Long sessionId, @RequestBody Map<String, String> params) {
        String title = params.get("title");
        return aiChatService.renameSession(sessionId, title);
    }

    @PostMapping("/session/batch-delete")
    @Operation(summary = "批量删除会话")
    public Result<String> batchDeleteSessions(@RequestBody Map<String, List<Long>> params) {
        return aiChatService.batchDeleteSessions(params.get("sessionIds"));
    }
}