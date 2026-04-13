package com.shxy.smartlearningacademycommon.constant;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/7 15:52
 */
public class RedisConstant {
    public static final String USER_TOKEN_KEY_PREFIX = "user:token:";
    public static final String USER_INFO_KEY_PREFIX="user:info:";
    public static final String USER_Resource_KEY_PREFIX="user:resource:";
    public static final String USER_LostFound_KEY_PREFIX="user:lostFound:";
    public static final String USER_Comment_KEY_PREFIX="user:comment:";
    public static final String USER_Post_KEY_PREFIX="user:post:";

    public static final String RESOURCE_LIST_KEY_PREFIX = "resource:list:";
    public static final String RESOURCE_DETAIL_KEY_PREFIX = "resource:detail:";

    public static final String POST_LIST_KEY_PREFIX = "post:list:";
    public static final String POST_DETAIL_KEY_PREFIX = "post:detail:";
    public static final String POST_LIKE_KEY_PREFIX = "post:like:users:";
    public static final String POST_LIKE_COUNT_PREFIX = "post:like:count:";

    public static final String LOSTFOUND_LIST_KEY_PREFIX = "lostfound:list:";
    public static final String LOSTFOUND_DETAIL_KEY_PREFIX = "lostfound:detail:";

    public static final String COMMENT_LIST_KEY_PREFIX = "comment:list:";

    public static long USER_Resource_TTL = 1800;
    public static long RESOURCE_LIST_TTL = 600;
    public static long RESOURCE_DETAIL_TTL = 1800;
    public static long POST_LIST_TTL = 300;
    public static long POST_DETAIL_TTL = 900;
    public static long POST_LIKE_TTL = 86400 * 7; // 7天
    public static long LOSTFOUND_LIST_TTL = 600;
    public static long LOSTFOUND_DETAIL_TTL = 1800;
    public static long COMMENT_LIST_TTL = 300;
}
