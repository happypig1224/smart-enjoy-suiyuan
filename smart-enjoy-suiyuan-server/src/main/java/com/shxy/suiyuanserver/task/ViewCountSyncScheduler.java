package com.shxy.suiyuanserver.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanserver.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountSyncScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final PostMapper postMapper;

    /**
     * 批量同步浏览量到数据库
     */
    @Scheduled(fixedRate = 300000)
    public void syncViewCountToDatabase() {
        long startTime = System.currentTimeMillis();
        
        List<String> keys = scanKeys(RedisConstant.POST_VIEW_COUNT_PREFIX + "*");
        if (keys.isEmpty()) {
            return;
        }

        Map<Long, Integer> viewCountMap = batchGetViewCounts(keys);
        if (viewCountMap.isEmpty()) {
            return;
        }

        int syncCount = batchUpdateDatabase(viewCountMap);

        if (syncCount > 0) {
            deleteSyncedKeys(viewCountMap.keySet());
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("浏览量同步完成: 同步帖子数={}, 耗时={}ms", syncCount, elapsed);
        }
    }

    /**
     * 使用SCAN遍历Redis key（非阻塞）
     */
    private List<String> scanKeys(String pattern) {
        List<String> keys = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();

        try (Cursor<byte[]> cursor = stringRedisTemplate.executeWithStickyConnection(
                connection -> connection.scan(options))) {
            if (cursor != null) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            log.error("SCAN遍历Redis key失败: pattern={}, error={}", pattern, e.getMessage());
        }
        return keys;
    }

    /**
     * 使用Pipeline批量读取Redis中的浏览量
     */
    private Map<Long, Integer> batchGetViewCounts(List<String> keys) {
        Map<Long, Integer> viewCountMap = new HashMap<>();
        
        // 使用executePipelined批量读取
        List<Object> results = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String key : keys) {
                connection.get(key.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });

        // 解析结果
        for (int i = 0; i < keys.size(); i++) {
            String countStr = (String) results.get(i);
            if (countStr == null || countStr.isEmpty()) {
                continue;
            }
            
            try {
                int increment = Integer.parseInt(countStr);
                if (increment <= 0) {
                    continue;
                }
                
                String key = keys.get(i);
                String postIdStr = key.substring(RedisConstant.POST_VIEW_COUNT_PREFIX.length());
                Long postId = Long.parseLong(postIdStr);
                viewCountMap.put(postId, increment);
            } catch (NumberFormatException e) {
                log.warn("浏览量计数器key格式异常: key={}", keys.get(i));
            }
        }
        
        return viewCountMap;
    }

    /**
     * 批量更新数据库浏览量
     * 使用CASE WHEN语句一次性更新多条记录
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateDatabase(Map<Long, Integer> viewCountMap) {
        if (viewCountMap.isEmpty()) {
            return 0;
        }

        int syncCount = 0;
        // 分批处理，每批100条（避免SQL过长）
        int batchSize = 100;
        List<Map.Entry<Long, Integer>> entries = new ArrayList<>(viewCountMap.entrySet());
        
        for (int i = 0; i < entries.size(); i += batchSize) {
            int end = Math.min(i + batchSize, entries.size());
            List<Map.Entry<Long, Integer>> batch = entries.subList(i, end);
            
            // 提取当前批次的ID和增量
            List<Long> postIds = new ArrayList<>();
            List<Integer> increments = new ArrayList<>();
            for (Map.Entry<Long, Integer> entry : batch) {
                postIds.add(entry.getKey());
                increments.add(entry.getValue());
            }
            
            try {
                // 使用一条SQL批量更新
                int updated = postMapper.batchUpdateViewCount(postIds, increments);
                syncCount += updated;
                log.debug("批量更新浏览量: 批次大小={}, 成功更新={}", batch.size(), updated);
            } catch (Exception e) {
                log.error("批量更新帖子浏览量失败: 批次大小={}, error={}", 
                        batch.size(), e.getMessage());
                // 如果批量更新失败，逐条重试
                syncCount += retryUpdateIndividually(batch);
            }
        }
        
        return syncCount;
    }

    /**
     * 逐条重试更新（当批量更新失败时使用）
     */
    private int retryUpdateIndividually(List<Map.Entry<Long, Integer>> batch) {
        int successCount = 0;
        for (Map.Entry<Long, Integer> entry : batch) {
            Long postId = entry.getKey();
            Integer increment = entry.getValue();
            
            try {
                postMapper.update(null, new LambdaUpdateWrapper<>(Post.class)
                        .eq(Post::getId, postId)
                        .setSql("view_count = view_count + " + increment));
                successCount++;
            } catch (Exception e) {
                log.error("单条更新帖子浏览量失败: postId={}, increment={}, error={}", 
                        postId, increment, e.getMessage());
            }
        }
        return successCount;
    }

    /**
     * 删除已同步的Redis key
     */
    private void deleteSyncedKeys(java.util.Set<Long> postIds) {
        if (postIds.isEmpty()) {
            return;
        }
        stringRedisTemplate.executePipelined((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
            for (Long postId : postIds) {
                String key = RedisConstant.POST_VIEW_COUNT_PREFIX + postId;
                connection.del(key.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });
    }
}
