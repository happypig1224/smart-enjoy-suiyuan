CREATE DATABASE IF NOT EXISTS `smart_enjoy_suiyuan`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `smart_enjoy_suiyuan`;

-- ==================== 基础表 ====================

-- 用户信息表
CREATE TABLE user
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_name     VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名，系统自动生成，全局唯一',
    user_password VARCHAR(100)       NOT NULL COMMENT '登录密码，BCrypt加密存储',
    avatar        VARCHAR(255) COMMENT '头像图片URL地址',
    phone         VARCHAR(20) UNIQUE NOT NULL COMMENT '手机号，用于登录和接收验证码',
    role          TINYINT  DEFAULT 1 COMMENT '角色权限: 1-普通用户, 0-管理员',
    status        TINYINT  DEFAULT 1 COMMENT '账户状态: 1-正常, 0-禁用(封号)',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册/创建时间',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    is_deleted    TINYINT  DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户信息表';

-- ==================== 核心业务表 ====================

-- 学习资源表
CREATE TABLE resource
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id        BIGINT        NOT NULL COMMENT '上传者ID，关联user.id',
    title          VARCHAR(100)  NOT NULL COMMENT '资源标题',
    type           VARCHAR(20)   NOT NULL COMMENT '资源格式: image, pdf, doc, txt, md',
    subject        INT      DEFAULT NULL COMMENT '所属学科分类ID',
    resource_url   VARCHAR(1000) NOT NULL COMMENT '文件在COS/服务器的存储路径',
    file_name      VARCHAR(255)  NOT NULL COMMENT '原始文件名',
    file_size      BIGINT COMMENT '文件大小，单位字节(B)',
    description    TEXT COMMENT '资源简介或备注',
    download_count INT      DEFAULT 0 COMMENT '累计下载次数',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user (user_id),
    INDEX idx_type (type),
    INDEX idx_subject (subject),
    INDEX idx_title (title),
    INDEX idx_download (download_count),
    INDEX idx_type_time (type, create_time),
    INDEX idx_download_time (download_count, create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='学习资源表';

-- 失物招领信息表
CREATE TABLE lost_found
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id        BIGINT       NOT NULL COMMENT '发布人ID，关联user.id',
    type           TINYINT      NOT NULL COMMENT '帖子类型: 0-寻物启事, 1-招领启事',
    status         TINYINT  DEFAULT 0 COMMENT '处理状态: 0-未解决, 1-已解决/已领取',
    title          VARCHAR(100) NOT NULL COMMENT '标题，简述物品',
    description    TEXT         NOT NULL COMMENT '详细描述，丢失/拾取经过',
    urgent         TINYINT  DEFAULT 0 COMMENT '紧急程度: 0-普通, 1-紧急(置顶)',
    location       VARCHAR(100) COMMENT '具体地点，如图书馆三楼',
    phone_contact  VARCHAR(20) COMMENT '联系电话，可为空',
    wechat_contact VARCHAR(50) COMMENT '联系微信号',
    images         JSON COMMENT '图片列表，存储URL数组',
    view_count     INT      DEFAULT 0 COMMENT '浏览量/查看次数',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user (user_id),
    INDEX idx_type_status (type, status),
    INDEX idx_urgent (urgent),
    INDEX idx_create_time (create_time),
    INDEX idx_type_status_urgent (type, status, urgent),
    INDEX idx_type_create_time (type, create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='失物招领信息表';

-- 社区帖子表
CREATE TABLE post
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id        BIGINT       NOT NULL COMMENT '发布者ID，关联user.id',
    title          VARCHAR(100) NOT NULL COMMENT '帖子标题',
    content        MEDIUMTEXT   NOT NULL COMMENT '帖子正文内容',
    content_format VARCHAR(10) DEFAULT 'markdown' COMMENT '内容格式: markdown',
    word_count     INT         DEFAULT 0 COMMENT '正文字数统计',
    type           TINYINT      NOT NULL COMMENT '板块分类: 0-技术讨论, 1-课程问题, 2-校园生活, 3-其他',
    status         TINYINT     DEFAULT 1 COMMENT '帖子状态: 0-草稿, 1-已发布, 2-已锁定, 3-审核中',
    is_top         TINYINT     DEFAULT 0 COMMENT '是否置顶: 0-否, 1-是',
    like_count     INT         DEFAULT 0 COMMENT '点赞总数',
    comment_count  INT         DEFAULT 0 COMMENT '评论总数',
    view_count     INT         DEFAULT 0 COMMENT '浏览次数',
    images         JSON COMMENT '配图列表，存储URL数组',
    create_time    DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    update_time    DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted     TINYINT     DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_user_type_time (user_id, type, create_time),
    INDEX idx_type_like (type, like_count),
    INDEX idx_status (status),
    INDEX idx_is_top (is_top)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='社区帖子表';

-- 二手商品表
CREATE TABLE secondhand_item
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    seller_id       BIGINT         NOT NULL COMMENT '卖家ID，关联user.id',
    title           VARCHAR(100)   NOT NULL COMMENT '商品标题',
    description     TEXT           NOT NULL COMMENT '商品详细描述',
    category        VARCHAR(20)    NOT NULL COMMENT '商品分类: electronics-数码, books-书籍, daily-日用品, sports-运动, clothes-服装, other-其他',
    price           DECIMAL(10, 2) NOT NULL COMMENT '二手价格',
    original_price  DECIMAL(10, 2) COMMENT '原价',
    condition_level TINYINT        NOT NULL COMMENT '新旧程度: 1-全新, 2-95新, 3-9成新, 4-8成新, 5-7成新及以下',
    images          JSON COMMENT '商品图片URL数组',
    contact_phone   VARCHAR(20) COMMENT '联系电话',
    contact_wechat  VARCHAR(50) COMMENT '联系微信',
    view_count      INT      DEFAULT 0 COMMENT '浏览次数',
    favorite_count  INT      DEFAULT 0 COMMENT '收藏次数',
    status          TINYINT  DEFAULT 0 COMMENT '商品状态: 0-在售, 1-已售出, 2-已下架',
    trade_location  VARCHAR(100) COMMENT '交易地点建议',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT  DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_seller (seller_id),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_price (price),
    INDEX idx_create_time (create_time),
    INDEX idx_category_status (category, status),
    INDEX idx_seller_status (seller_id, status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='二手商品表';

-- AI会话表
CREATE TABLE `ai_session`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键，会话ID',
    `user_id`     bigint       NOT NULL COMMENT '所属用户ID',
    `title`       varchar(128) NOT NULL DEFAULT '新会话' COMMENT '会话标题(通常取第一条问题的摘要)',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除标识 (0-正常, 1-已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '加速按用户查询会话列表'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='AI会话表';

-- AI聊天记录表
CREATE TABLE `chat_message`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键，消息ID',
    `session_id`  bigint      NOT NULL COMMENT '所属会话ID，关联 ai_session.id',
    `user_id`     bigint      NOT NULL COMMENT '消息所属用户ID',
    `role`        varchar(20) NOT NULL COMMENT '消息角色 (user:用户, assistant:AI助手, system:系统提示词)',
    `content`     text        NOT NULL COMMENT '消息内容正文',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送/创建时间',
    `is_deleted`  tinyint     NOT NULL DEFAULT 0 COMMENT '逻辑删除标识 (0-正常, 1-已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`) USING BTREE COMMENT '加速加载特定会话的聊天历史',
    KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '加速按用户维度的消息统计'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='AI聊天记录表';

-- ==================== 关联关系表 ====================

-- 评论回复表
CREATE TABLE comment
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT NOT NULL COMMENT '评论者ID，关联user.id',
    content     TEXT   NOT NULL COMMENT '评论内容',
    post_id     BIGINT NULL COMMENT '关联帖子ID，回复帖子时使用',
    resource_id BIGINT NULL COMMENT '关联资源ID，回复资源时使用',
    parent_id   BIGINT   DEFAULT 0 COMMENT '父级评论ID，用于实现二级回复',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT  DEFAULT 0 COMMENT '逻辑删除标记: 0-未删除, 1-已删除',
    INDEX idx_user (user_id),
    INDEX idx_post_parent (post_id, parent_id, is_deleted),
    INDEX idx_create_time (create_time),
    INDEX idx_resource_time (resource_id, create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='评论回复表';

-- 帖子点赞记录表
CREATE TABLE post_like
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    post_id     BIGINT NOT NULL COMMENT '帖子ID，关联post.id',
    user_id     BIGINT NOT NULL COMMENT '用户ID，关联user.id',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_post_user (post_id, user_id) COMMENT '同一用户对同一帖子只能点赞一次',
    INDEX idx_user (user_id),
    INDEX idx_post (post_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='帖子点赞记录表';

-- 用户关注关系表
CREATE TABLE user_follow
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    follower_id BIGINT NOT NULL COMMENT '关注者ID (粉丝)',
    followee_id BIGINT NOT NULL COMMENT '被关注者ID (博主)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    UNIQUE KEY uk_follower_followee (follower_id, followee_id) COMMENT '防止重复关注',
    INDEX idx_followee (followee_id) COMMENT '查询谁关注了我'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户关注关系表';

-- 资源收藏表
CREATE TABLE resource_favorite
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id       BIGINT      NOT NULL COMMENT '用户ID，关联user.id',
    resource_id   BIGINT      NOT NULL COMMENT '资源ID，关联资源表',
    resource_type VARCHAR(20) NOT NULL DEFAULT 'resource' COMMENT '资源类型: resource(学习资源), post(帖子), kb_document(知识库文档)',
    create_time   DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    update_time   DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_resource_type (user_id, resource_id, resource_type) COMMENT '同一用户同一资源类型只能收藏一次',
    INDEX idx_resource_type (resource_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='资源收藏表';

-- 帖子收藏表
CREATE TABLE post_favorite
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID，关联user.id',
    post_id     BIGINT NOT NULL COMMENT '帖子ID，关联post.id',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_post (user_id, post_id) COMMENT '同一用户对同一帖子只能收藏一次',
    INDEX idx_user (user_id),
    INDEX idx_post (post_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='帖子收藏表';

-- 二手商品收藏表
CREATE TABLE secondhand_favorite
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID，关联user.id',
    item_id     BIGINT NOT NULL COMMENT '商品ID，关联secondhand_item.id',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_item (user_id, item_id) COMMENT '同一用户对同一商品只能收藏一次',
    INDEX idx_item (item_id),
    INDEX idx_user (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='二手商品收藏表';

-- 用户通知表
CREATE TABLE IF NOT EXISTS `user_notification`
(
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`      BIGINT      NOT NULL COMMENT '接收者ID',
    `from_user_id` BIGINT               DEFAULT NULL COMMENT '发送者ID',
    `type`         VARCHAR(50) NOT NULL COMMENT '通知类型: follow, post_favorite, resource_favorite, comment_reply',
    `title`        VARCHAR(200)         DEFAULT NULL COMMENT '通知标题',
    `content`      TEXT COMMENT '通知内容',
    `business_id`  BIGINT               DEFAULT NULL COMMENT '关联业务ID（帖子ID、资源ID等）',
    `link`         VARCHAR(500)         DEFAULT NULL COMMENT '跳转链接',
    `is_read`      TINYINT     NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `is_deleted`   TINYINT     NOT NULL DEFAULT 0 COMMENT '是否删除: 0-正常, 1-已删除',
    `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `read_time`    DATETIME             DEFAULT NULL COMMENT '阅读时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `create_time` DESC) COMMENT '用户通知时间索引',
    KEY `idx_user_read` (`user_id`, `is_read`) COMMENT '用户已读状态索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户通知表';

-- ==================== 补充索引（性能优化） ====================

-- post表：列表查询核心索引（按类型+状态+时间排序）
ALTER TABLE post ADD INDEX idx_type_status_deleted_time (type, status, is_deleted, create_time DESC);

-- post表：搜索优化（标题全文索引）
ALTER TABLE post ADD INDEX idx_title_trash (title, is_deleted);

-- comment表：按帖子查询+逻辑删除组合索引
ALTER TABLE comment ADD INDEX idx_post_deleted_time (post_id, is_deleted, create_time);

-- lost_found表：紧急+状态+时间组合索引
ALTER TABLE lost_found ADD INDEX idx_urgent_status_time (urgent, status, create_time DESC);

-- chat_message表：会话+时间组合索引（避免文件排序）
ALTER TABLE chat_message ADD INDEX idx_session_time (session_id, create_time);

-- secondhand_item表：搜索优化
ALTER TABLE secondhand_item ADD INDEX idx_title_status (title, status);

-- user_notification表：类型过滤索引
ALTER TABLE user_notification ADD INDEX idx_user_type_read (user_id, type, is_read);

