-- =====================================================
-- 数据库初始化: 智享学堂 (Smart Learning Academy)
-- 描述: 包含用户管理、资源共享、失物招领、社区互动及AI聊天功能的校园综合平台
-- =====================================================
CREATE DATABASE IF NOT EXISTS `smart_enjoy_suiyuan`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `smart_enjoy_suiyuan`;

-- =====================================================
-- 1. 用户表 (User Management)
-- 描述: 存储平台所有用户的基础信息、认证凭据及账户状态
-- =====================================================
CREATE TABLE user
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    nick_name     VARCHAR(50)        NOT NULL COMMENT '用户昵称',
    user_name     VARCHAR(50) UNIQUE NOT NULL COMMENT '登录用户名，全局唯一',
    user_password VARCHAR(100)       NOT NULL COMMENT '登录密码，BCrypt加密存储',
    user_gender   TINYINT  DEFAULT 0 COMMENT '性别: 0-未填写, 1-男, 2-女',
    user_age      INT COMMENT '用户年龄',
    user_grade    VARCHAR(20) COMMENT '所属年级，如2023级',
    avatar        VARCHAR(255) COMMENT '头像图片URL地址',
    phone         VARCHAR(20) UNIQUE COMMENT '绑定手机号，唯一索引',
    role          TINYINT  DEFAULT 1 COMMENT '角色权限: 1-普通用户, 0-管理员',
    status        TINYINT  DEFAULT 1 COMMENT '账户状态: 1-正常, 0-禁用(封号)',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册/创建时间',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    is_deleted    TINYINT  DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_user_name (user_name),
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户信息表';

-- =====================================================
-- 2. 资源表 (Resource Sharing)
-- 描述: 存储用户上传的学习资料（PDF/文档/图片等）元数据
-- =====================================================
CREATE TABLE resource
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id        BIGINT        NOT NULL COMMENT '上传者ID，关联user.id',
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
    INDEX idx_download (download_count),
    INDEX idx_type_time (type, create_time),
    INDEX idx_download_time (download_count, create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='学习资源表';

-- =====================================================
-- 3. 失物招领表 (Lost and Found)
-- 描述: 记录校园内的寻物启事与招领信息发布
-- =====================================================
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

-- =====================================================
-- 4. 帖子表 (Community Posts)
-- 描述: 校园社区论坛，支持技术讨论、生活分享等
-- 4. 帖子表 (Community Posts)
CREATE TABLE post
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id       BIGINT       NOT NULL COMMENT '发布者ID，关联user.id',
    title         VARCHAR(100) NOT NULL COMMENT '帖子标题',
    content       TEXT         NOT NULL COMMENT '帖子正文内容',
    type          TINYINT      NOT NULL COMMENT '板块分类: 0-技术讨论, 1-课程问题, 2-校园生活, 3-其他',
    like_count    INT      DEFAULT 0 COMMENT '点赞总数',
    comment_count INT      DEFAULT 0 COMMENT '评论总数',
    view_count    INT      DEFAULT 0 COMMENT '浏览次数',
    images        JSON COMMENT '配图列表，存储URL数组',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_type_time (user_id, type, create_time),
    INDEX idx_type_like (type, like_count)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='社区帖子表';

-- =====================================================
-- 5. 评论表 (Comments)
-- 描述: 支持对帖子和资源进行评论及回复
-- 5. 评论表 (Comments)
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

-- =====================================================
-- 6. 帖子点赞表 (Post Like)
-- 描述: 记录用户对帖子的点赞关系，用于点赞状态持久化
-- =====================================================
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

-- =====================================================
-- 7. 资源收藏表(Resource Favorite)
-- 描述: 管理用户收藏的资源
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

-- ----------------------------
-- 8. AI 会话表 (ai_session)
-- 作用: 管理用户的多次独立对话，类似 ChatGPT 的左侧历史记录列表
-- ----------------------------
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


-- ----------------------------
-- 9. AI 聊天记录表 (chat_message)
-- 作用: 存储每个会话下具体的对话明细
-- ----------------------------
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