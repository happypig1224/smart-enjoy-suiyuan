package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanserver.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/send")
    @Operation(summary = "发送消息给AI")
    public Result<String> sendChatMessage(@RequestBody Map<String, Object> requestParams) {
        // 实际开发中，userId 应该从 JWT ThreadLocal 工具类中获取
        // Long userId = UserContext.getUserId();
        Long userId = 1L; // 模拟当前登录用户

        String query = (String) requestParams.get("query");
        Long sessionId = requestParams.get("sessionId") != null ?
                Long.valueOf(requestParams.get("sessionId").toString()) : null;

        if (query == null || query.trim().isEmpty()) {
            return Result.fail("消息不能为空");
        }

        String reply = aiChatService.chat(userId, query, sessionId);

        return Result.success(reply);
    }
}