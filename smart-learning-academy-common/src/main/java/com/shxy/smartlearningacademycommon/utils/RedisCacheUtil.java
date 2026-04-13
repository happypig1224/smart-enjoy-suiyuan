package com.shxy.smartlearningacademycommon.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/13 18:03
 */
@Slf4j
@Component
public class RedisCacheUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public RedisCacheUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 默认空值缓存时间(分钟)
    private static final long DEFAULT_NULL_CACHE_MINUTES = 5;
    // 默认自旋重试次数
    private static final int DEFAULT_RETRY_COUNT = 10;
    // 默认自旋间隔(毫秒)
    private static final long DEFAULT_RETRY_INTERVAL_MS = 50;

    // 缓存空值占位符
    private static final String CACHE_NULL_FLAG = "__NULL__";

    /**
     * 1. 解决缓存穿透
     * 策略:缓存空对象。如果数据库也没数据,写入一个空值占位符,并设置较短过期时间。
     *
     * @param key          缓存键
     * @param type         返回数据类型
     * @param dbFunction   数据库查询函数 (例如: id -> userMapper.selectById(id))
     * @param expireTime   正常数据的过期时间
     * @param timeUnit     时间单位
     * @param <T>          泛型
     * @return 查询结果
     */
    public <T> T queryWithPassThrough(String key, Class<T> type, Function<String, T> dbFunction, long expireTime, TimeUnit timeUnit) {
        return queryWithPassThrough(key, type, dbFunction, expireTime, timeUnit, DEFAULT_NULL_CACHE_MINUTES);
    }
    
    /**
     * 1. 解决缓存穿透(支持自定义空值缓存时间)
     *
     * @param key                缓存键
     * @param type               返回数据类型
     * @param dbFunction         数据库查询函数
     * @param expireTime         正常数据的过期时间
     * @param timeUnit           时间单位
     * @param nullCacheMinutes   空值缓存时间(分钟)
     * @param <T>                泛型
     * @return 查询结果
     */
    public <T> T queryWithPassThrough(String key, Class<T> type, Function<String, T> dbFunction, long expireTime, TimeUnit timeUnit, long nullCacheMinutes) {
        // 参数校验
        if (!StringUtils.hasText(key) || type == null || dbFunction == null) {
            log.warn("queryWithPassThrough 参数无效: key={}, type={}", key, type);
            return null;
        }
    
        // 1. 查询缓存
        String json = getCacheValue(key);
    
        // 2. 缓存命中
        if (json != null) {
            // 如果是空值占位符,说明数据库也没数据,直接返回 null
            if (CACHE_NULL_FLAG.equals(json)) {
                return null;
            }
            try {
                return objectMapper.readValue(json, type);
            } catch (JsonProcessingException e) {
                log.error("JSON反序列化失败, key: {}", key, e);
                return null;
            }
        }
    
        // 3. 缓存未命中,查询数据库
        T data = dbFunction.apply(key);
    
        // 4. 写入缓存
        if (data == null) {
            // 解决穿透的核心:将空值也写入缓存,设置较短过期时间
            redisTemplate.opsForValue().set(key, CACHE_NULL_FLAG, nullCacheMinutes, TimeUnit.MINUTES);
            return null;
        }
    
        // 写入正常数据
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(data), expireTime, timeUnit);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败, key: {}", key, e);
        }
        return data;
    }

    /**
     * 2. 解决缓存击穿
     * 策略:互斥锁。缓存失效时,只允许一个线程重建缓存,其他线程等待。
     *
     * @param key          缓存键
     * @param type         返回数据类型
     * @param dbFunction   数据库查询函数
     * @param expireTime   过期时间
     * @param timeUnit     时间单位
     * @param <T>          泛型
     * @return 查询结果
     */
    public <T> T queryWithMutex(String key, Class<T> type, Function<String, T> dbFunction, long expireTime, TimeUnit timeUnit) {
        return queryWithMutex(key, type, dbFunction, expireTime, timeUnit, DEFAULT_NULL_CACHE_MINUTES, DEFAULT_RETRY_COUNT, DEFAULT_RETRY_INTERVAL_MS);
    }
    
    /**
     * 2. 解决缓存击穿(支持自定义参数)
     *
     * @param key                缓存键
     * @param type               返回数据类型
     * @param dbFunction         数据库查询函数
     * @param expireTime         过期时间
     * @param timeUnit           时间单位
     * @param nullCacheMinutes   空值缓存时间(分钟)
     * @param retryCount         自旋重试次数
     * @param retryIntervalMs    自旋间隔(毫秒)
     * @param <T>                泛型
     * @return 查询结果
     */
    public <T> T queryWithMutex(String key, Class<T> type, Function<String, T> dbFunction, long expireTime, TimeUnit timeUnit, long nullCacheMinutes, int retryCount, long retryIntervalMs) {
        // 参数校验
        if (!StringUtils.hasText(key) || type == null || dbFunction == null) {
            log.warn("queryWithMutex 参数无效: key={}, type={}", key, type);
            return null;
        }
    
        // 1. 查询缓存
        String json = getCacheValue(key);
        if (json != null) {
            if (CACHE_NULL_FLAG.equals(json)) return null;
            try {
                return objectMapper.readValue(json, type);
            } catch (JsonProcessingException e) {
                log.error("JSON反序列化失败, key: {}", key, e);
                return null;
            }
        }
    
        // 2. 缓存未命中,尝试获取互斥锁
        T data = null;
        String lockKey = "lock:" + key;
        String lockValue = tryLock(lockKey, 10);
    
        try {
            if (lockValue != null) {
                // 3.1 获取锁成功,再次检测缓存(双重检查)
                json = getCacheValue(key);
                if (json != null) {
                    // 其他线程已经重建了缓存
                    if (CACHE_NULL_FLAG.equals(json)) return null;
                    try {
                        return objectMapper.readValue(json, type);
                    } catch (JsonProcessingException e) {
                        log.error("JSON反序列化失败, key: {}", key, e);
                        return null;
                    }
                }
    
                // 3.2 查询数据库
                data = dbFunction.apply(key);
    
                // 3.3 写入缓存(处理空值防止穿透)
                if (data == null) {
                    redisTemplate.opsForValue().set(key, CACHE_NULL_FLAG, nullCacheMinutes, TimeUnit.MINUTES);
                } else {
                    try {
                        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(data), expireTime, timeUnit);
                    } catch (JsonProcessingException e) {
                        log.error("JSON序列化失败, key: {}", key, e);
                    }
                }
            } else {
                // 3.4 获取锁失败,自旋重试
                for (int i = 0; i < retryCount; i++) {
                    try {
                        Thread.sleep(retryIntervalMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("等待锁被中断, key: {}", key);
                        throw new RuntimeException("等待锁被中断", e);
                    }
                    // 重试读取缓存
                    T cachedData = readCache(key, type);
                    if (cachedData != null) {
                        return cachedData;
                    }
                }
                log.warn("获取锁超时,返回null, key: {}", key);
                return null;
            }
        } finally {
            // 4. 释放锁
            if (lockValue != null) {
                unlock(lockKey, lockValue);
            }
        }
        return data;
    }

    /**
     * 3. 解决缓存雪崩
     * 策略:随机过期时间。在原有过期时间基础上增加随机值,避免集体失效。
     *
     * @param key          缓存键
     * @param value        缓存值
     * @param baseExpire   基础过期时间
     * @param randomBound  随机波动范围(例如 300 表示 +/- 300秒)
     * @param timeUnit     时间单位
     */
    public void setWithRandomExpire(String key, Object value, long baseExpire, long randomBound, TimeUnit timeUnit) {
        // 参数校验
        if (!StringUtils.hasText(key) || value == null) {
            log.warn("setWithRandomExpire 参数无效: key={}", key);
            return;
        }
    
        try {
            // 计算随机时间:base + random(-bound ~ +bound)
            // 确保最终时间不为负数
            long randomOffset = randomBound > 0 ? ThreadLocalRandom.current().nextLong(-randomBound, randomBound + 1) : 0;
            long finalExpire = baseExpire + randomOffset;
            if (finalExpire <= 0) finalExpire = baseExpire; // 兜底
    
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, finalExpire, timeUnit);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败, key: {}", key, e);
        }
    }

    /**
     * 读取缓存的辅助方法
     */
    private <T> T readCache(String key, Class<T> type) {
        String json = getCacheValue(key);
        if (json == null) {
            return null;
        }
        if (CACHE_NULL_FLAG.equals(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败, key: {}", key, e);
            return null;
        }
    }

    /**
     * 安全获取缓存值,避免NPE
     *
     * @param key 缓存键
     * @return 缓存值的JSON字符串,不存在则返回null
     */
    private String getCacheValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    // --- 辅助方法：改进的分布式锁实现 ---

    /**
     * 尝试获取分布式锁
     * @param key 锁的key
     * @param expireSeconds 锁的过期时间（秒）
     * @return 锁的唯一标识，用于释放时验证
     */
    private  String tryLock(String key, long expireSeconds) {
        String lockValue = UUID.randomUUID().toString();
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, lockValue, expireSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(flag) ? lockValue : null;
    }

    /**
     * 安全释放分布式锁（验证锁持有者）
     * @param key 锁的key
     * @param lockValue 锁的唯一标识
     */
    private  void unlock(String key, String lockValue) {
        if (lockValue == null) {
            return;
        }
        // 使用Lua脚本保证原子性：先比较再删除
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        try {
            redisTemplate.execute(
                    (org.springframework.data.redis.core.RedisCallback<Long>) connection ->
                            connection.eval(
                                    script.getBytes(),
                                    org.springframework.data.redis.connection.ReturnType.INTEGER,
                                    1,
                                    key.getBytes(),
                                    lockValue.getBytes()
                            )
            );
        } catch (Exception e) {
            log.error("释放锁失败, key: {}", key, e);
        }
    }
}
