package com.shxy.smartlearningacademycommon.constant;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/7 15:52
 */
public class RedisConstant {

    // ==================== 用户模块 ====================
    /**
     * 用户 Token 缓存：user:token:{userId}
     */
    public static final String USER_TOKEN_KEY_PREFIX = "user:token:";

    /**
     * 用户信息缓存：user:info:{userId}
     */
    public static final String USER_INFO_KEY_PREFIX = "user:info:";

    /**
     * 用户资源列表缓存：user:resource:list:{userId}
     */
    public static final String USER_RESOURCE_LIST_KEY_PREFIX = "user:resource:list:";

    /**
     * 用户失物招领列表缓存：user:lostfound:list:{userId}
     */
    public static final String USER_LOSTFOUND_LIST_KEY_PREFIX = "user:lostfound:list:";

    /**
     * 用户评论列表缓存：user:comment:list:{userId}
     */
    public static final String USER_COMMENT_LIST_KEY_PREFIX = "user:comment:list:";

    /**
     * 用户帖子列表缓存：user:post:list:{userId}
     */
    public static final String USER_POST_LIST_KEY_PREFIX = "user:post:list:";

    // ==================== 资源模块 ====================
    /**
     * 资源列表缓存：resource:list:{page}:{pageSize}:{type}:{sort}
     */
    public static final String RESOURCE_LIST_KEY_PREFIX = "resource:list:";

    /**
     * 资源详情缓存：resource:detail:{id}
     */
    public static final String RESOURCE_DETAIL_KEY_PREFIX = "resource:detail:";

    // ==================== 帖子模块 ====================
    /**
     * 帖子列表缓存：post:list:{page}:{size}:{type}:{sort}
     */
    public static final String POST_LIST_KEY_PREFIX = "post:list:";

    /**
     * 帖子详情缓存：post:detail:{id}
     */
    public static final String POST_DETAIL_KEY_PREFIX = "post:detail:";

    /**
     * 帖子点赞用户集合：post:like:users:{postId}
     */
    public static final String POST_LIKE_USERS_KEY_PREFIX = "post:like:users:";

    /**
     * 帖子点赞数缓存：post:like:count:{postId}
     */
    public static final String POST_LIKE_COUNT_KEY_PREFIX = "post:like:count:";

    // ==================== 失物招领模块 ====================
    /**
     * 失物招领列表缓存：lostfound:list:{page}:{pageSize}:{type}:{status}:{urgent}
     */
    public static final String LOSTFOUND_LIST_KEY_PREFIX = "lostfound:list:";

    /**
     * 失物招领详情缓存：lostfound:detail:{id}
     */
    public static final String LOSTFOUND_DETAIL_KEY_PREFIX = "lostfound:detail:";

    // ==================== 评论模块 ====================
    /**
     * 评论列表缓存：comment:list:{page}:{size}:{targetType}:{targetId}:{sort}
     * targetType: post(帖子) 或 lost(失物招领)
     */
    public static final String COMMENT_LIST_KEY_PREFIX = "comment:list:";

    // ==================== TTL 配置（统一秒为单位） ====================
    /**
     * 用户资源列表 TTL：30 分钟
     */
    public static final long USER_RESOURCE_TTL = 1800;

    /**
     * 资源列表 TTL：10 分钟
     */
    public static final long RESOURCE_LIST_TTL = 600;

    /**
     * 资源详情 TTL：30 分钟
     */
    public static final long RESOURCE_DETAIL_TTL = 1800;

    /**
     * 帖子列表 TTL：5 分钟
     */
    public static final long POST_LIST_TTL = 300;

    /**
     * 帖子详情 TTL：15 分钟
     */
    public static final long POST_DETAIL_TTL = 900;

    /**
     * 帖子点赞数据 TTL：7 天
     */
    public static final long POST_LIKE_TTL = 86400 * 7;

    /**
     * 失物招领列表 TTL：10 分钟
     */
    public static final long LOSTFOUND_LIST_TTL = 600;

    /**
     * 失物招领详情 TTL：30 分钟
     */
    public static final long LOSTFOUND_DETAIL_TTL = 1800;

    /**
     * 评论列表 TTL：5 分钟
     */
    public static final long COMMENT_LIST_TTL = 300;
}
