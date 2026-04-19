package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.AiChatDTO;
import com.shxy.suiyuanentity.dto.AiSessionCreateDTO;
import com.shxy.suiyuanentity.dto.AiSessionUpdateDTO;
import com.shxy.suiyuanentity.vo.AiChatVO;
import com.shxy.suiyuanentity.vo.AiSessionVO;
import com.shxy.suiyuanentity.vo.ChatMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@RestController("userAIAssistantController")
@RequestMapping("/user/ai")
@Tag(name = "用户端AI助手模块")
public class AIAssistantController {
    @Autowired
    private ChatClient chatClient;

    @PostMapping("/chat/send")
    @Operation(summary = "AI聊天发送", description = "用户与AI助手进行智能对话")
    public ChatResponse chatSend(@RequestBody AiChatDTO aiChatDTO) {
        String userMessage = aiChatDTO.getQuery();
        // 流式返回给前端
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .chatResponse();
    }
    @PostMapping("/session/create")
    @Operation(summary = "创建AI会话", description = "创建一个新的AI聊天会话")
    public Result<AiSessionVO> createSession(@RequestBody AiSessionCreateDTO createDTO) {
        return Result.success("创建会话功能待完善");
    }

    @GetMapping("/session/list")
    @Operation(summary = "AI会话列表", description = "获取当前用户的所有AI会话列表")
    public Result<List<AiSessionVO>> getSessionList(@RequestParam(required = false) Integer status) {
        return Result.success("会话列表功能待完善");
    }

    @GetMapping("/session/{id}")
    @Operation(summary = "获取会话详情", description = "获取指定AI会话的详细信息")
    public Result<AiSessionVO> getSessionDetail(@PathVariable Long id) {
        return Result.success("会话详情功能待完善");
    }

    @PutMapping("/session/{id}")
    @Operation(summary = "更新会话状态", description = "更新AI会话状态（归档/激活）")
    public Result<String> updateSessionStatus(@PathVariable Long id, @RequestBody AiSessionUpdateDTO updateDTO) {
        return Result.success("更新会话状态功能待完善");
    }

    @DeleteMapping("/session/{id}")
    @Operation(summary = "删除会话", description = "删除指定的AI会话及关联聊天记录")
    public Result<String> deleteSession(@PathVariable Long id) {
        return Result.success("删除会话功能待完善");
    }

    @GetMapping("/chat/history/{sessionId}")
    @Operation(summary = "获取聊天历史", description = "获取指定会话的完整聊天记录")
    public Result<List<ChatMessageVO>> getChatHistory(@PathVariable Long sessionId) {
        return Result.success("聊天历史功能待完善");
    }

    @PutMapping("/chat/clear")
    @Operation(summary = "清空会话历史", description = "清空指定会话的聊天记录")
    public Result<String> clearChatHistory(@RequestParam Long sessionId) {
        return Result.success("清空会话历史功能待完善");
    }
}
