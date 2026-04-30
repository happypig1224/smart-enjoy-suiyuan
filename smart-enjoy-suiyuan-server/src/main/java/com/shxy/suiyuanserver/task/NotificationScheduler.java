package com.shxy.suiyuanserver.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuanserver.mapper.CommentMapper;
import com.shxy.suiyuanserver.mapper.ResourceFavoriteMapper;
import com.shxy.suiyuanserver.mapper.UserFollowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 通知推送调度器
 * 负责从缓冲区扫描待处理的通知任务，校验有效性后推送到用户通知列表
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationScheduler {

    private final StringRedisTemplate redisTemplate;
    private final UserFollowMapper userFollowMapper;
    private final CommentMapper commentMapper;
    private final ResourceFavoriteMapper resourceFavoriteMapper;
    private final ObjectMapper objectMapper;

    /**
     * 每 2 秒执行一次通知推送任务
     */
    @Scheduled(fixedDelay = 2000)
    public void processNotifyBuffer() {
        long now = System.currentTimeMillis();

        // 1. 获取所有 Score <= 当前时间的任务（即已经到期的任务）
        Set<String> tasks = redisTemplate.opsForZSet().rangeByScore(
                RedisConstant.NOTIFY_BUFFER_KEY, 0, now, 0, 100); // 每次最多处理100条

        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        for (String taskJson : tasks) {
            try {
                // 2. 解析任务
                JsonNode task = objectMapper.readTree(taskJson);
                Long fromId = task.get("from").asLong();
                Long toId = task.get("to").asLong();
                String type = task.get("type").asText();

                // 3. 检查关系/状态是否依然存在
                boolean isValid = checkValidity(task, type, fromId, toId);

                if (isValid) {
                    // 4. 正式推送到用户的通知列表
                    String notifyKey = RedisConstant.NOTIFY_LIST_KEY_PREFIX + toId;
                    redisTemplate.opsForList().leftPush(notifyKey, taskJson);

                    // 5. 增加未读数
                    redisTemplate.opsForValue().increment(RedisConstant.NOTIFY_UNREAD_KEY_PREFIX + toId);

                    // 6. 限制通知列表长度(保留最近 100 条)
                    redisTemplate.opsForList().trim(notifyKey, 0, 99);

                    log.info("通知推送成功: {} -> {}, type: {}", fromId, toId, type);
                } else {
                    log.debug("通知已取消或失效: {} -> {}, type: {}", fromId, toId, type);
                }

                // 7. 从缓冲区删除该任务
                redisTemplate.opsForZSet().remove(RedisConstant.NOTIFY_BUFFER_KEY, taskJson);

            } catch (Exception e) {
                log.error("处理通知任务失败: {}", taskJson, e);
                // 出错也删除，防止死循环，实际生产中可加入重试队列
                redisTemplate.opsForZSet().remove(RedisConstant.NOTIFY_BUFFER_KEY, taskJson);
            }
        }
    }

    /**
     * 校验通知的有效性
     */
    private boolean checkValidity(JsonNode task, String type, Long fromId, Long toId) {
        switch (type) {
            case "follow":
                // 校验关注关系是否存在
                return userFollowMapper.selectByFollowerAndFollowee(fromId, toId) != null;
            case "comment_reply":
                // 如果需要更严格的校验，可以查询最近的评论记录
                return true;
            case "resource_favorite":
                // 校验收藏状态是否依然存在
                Long resourceId = task.get("targetId").asLong();
                return resourceId != null && resourceFavoriteMapper.exists(
                        new LambdaQueryWrapper<com.shxy.suiyuanentity.entity.ResourceFavorite>()
                                .eq(com.shxy.suiyuanentity.entity.ResourceFavorite::getUserId, fromId)
                                .eq(com.shxy.suiyuanentity.entity.ResourceFavorite::getResourceId, resourceId)
                );
            default:
                return false;
        }
    }
}
