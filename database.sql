-- 创建智享绥园数据库
CREATE DATABASE IF NOT EXISTS `smart_enjoy_suiyuan`
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- 切换到目标数据库
USE `smart_enjoy_suiyuan`;

-- AI会话表
DROP TABLE IF EXISTS `ai_session`;
CREATE TABLE `ai_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键，会话ID',
  `user_id` bigint(20) NOT NULL COMMENT '所属用户ID',
  `title` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '新会话' COMMENT '会话标题(通常取第一条问题的摘要)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识 (0-正常, 1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '加速按用户查询会话列表'
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='AI会话表';

-- AI聊天记录表
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键，消息ID',
  `session_id` bigint(20) NOT NULL COMMENT '所属会话ID，关联 ai_session.id',
  `user_id` bigint(20) NOT NULL COMMENT '消息所属用户ID',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息角色 (user:用户, assistant:AI助手, system:系统提示词)',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容正文',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送/创建时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识 (0-正常, 1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`) USING BTREE COMMENT '加速加载特定会话的聊天历史',
  KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '加速按用户维度的消息统计'
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='AI聊天记录表';

-- 评论回复表
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '评论者ID，关联user.id',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `post_id` bigint(20) DEFAULT NULL COMMENT '关联帖子ID，回复帖子时使用',
  `resource_id` bigint(20) DEFAULT NULL COMMENT '关联资源ID，回复资源时使用',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父级评论ID，用于实现二级回复',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除标记: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_post_parent` (`post_id`,`parent_id`,`is_deleted`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_resource_time` (`resource_id`,`create_time`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='评论回复表';

-- 失物招领信息表
DROP TABLE IF EXISTS `lost_found`;
CREATE TABLE `lost_found` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '发布人ID，关联user.id',
  `type` tinyint(4) NOT NULL COMMENT '帖子类型: 0-寻物启事, 1-招领启事',
  `status` tinyint(4) DEFAULT '0' COMMENT '处理状态: 0-未解决, 1-已解决/已领取',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题，简述物品',
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细描述，丢失/拾取经过',
  `urgent` tinyint(4) DEFAULT '0' COMMENT '紧急程度: 0-普通, 1-紧急(置顶)',
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '具体地点，如图书馆三楼',
  `phone_contact` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话，可为空',
  `wechat_contact` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系微信号',
  `images` json DEFAULT NULL COMMENT '图片列表，存储URL数组',
  `view_count` int(11) DEFAULT '0' COMMENT '浏览量/查看次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_type_status` (`type`,`status`),
  KEY `idx_urgent` (`urgent`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_type_status_urgent` (`type`,`status`,`urgent`),
  KEY `idx_type_create_time` (`type`,`create_time`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='失物招领信息表';

-- 社区帖子表
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '发布者ID，关联user.id',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子标题',
  `content` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子正文内容',
  `content_format` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'markdown' COMMENT '内容格式: markdown',
  `word_count` int(11) DEFAULT '0' COMMENT '正文字数统计',
  `type` tinyint(4) NOT NULL COMMENT '板块分类: 0-技术讨论, 1-课程问题, 2-校园生活, 3-其他',
  `status` tinyint(4) DEFAULT '1' COMMENT '帖子状态: 0-草稿, 1-已发布, 2-已锁定, 3-审核中',
  `is_top` tinyint(4) DEFAULT '0' COMMENT '是否置顶: 0-否, 1-是',
  `like_count` int(11) DEFAULT '0' COMMENT '点赞总数',
  `comment_count` int(11) DEFAULT '0' COMMENT '评论总数',
  `view_count` int(11) DEFAULT '0' COMMENT '浏览次数',
  `images` json DEFAULT NULL COMMENT '配图列表，存储URL数组',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_type_time` (`user_id`,`type`,`create_time`),
  KEY `idx_type_like` (`type`,`like_count`),
  KEY `idx_status` (`status`),
  KEY `idx_is_top` (`is_top`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='社区帖子表';

-- 帖子收藏表
DROP TABLE IF EXISTS `post_favorite`;
CREATE TABLE `post_favorite` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID，关联user.id',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID，关联post.id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`,`post_id`) COMMENT '同一用户对同一帖子只能收藏一次',
  KEY `idx_user` (`user_id`),
  KEY `idx_post` (`post_id`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='帖子收藏表';

-- 帖子点赞记录表
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID，关联post.id',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID，关联user.id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`) COMMENT '同一用户对同一帖子只能点赞一次',
  KEY `idx_user` (`user_id`),
  KEY `idx_post` (`post_id`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='帖子点赞记录表';

-- 私信会话表
DROP TABLE IF EXISTS `private_conversation`;
CREATE TABLE `private_conversation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user1_id` bigint(20) NOT NULL COMMENT '用户1 ID（值较小的一方）',
  `user2_id` bigint(20) NOT NULL COMMENT '用户2 ID（值较大的一方）',
  `last_message` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一条消息摘要（冗余字段，避免JOIN）',
  `last_message_at` datetime DEFAULT NULL COMMENT '最后一条消息时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会话创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识 (0-正常, 1-已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pair` (`user1_id`,`user2_id`) COMMENT '保证同一对用户只存在一条会话记录',
  KEY `idx_user1_last_msg` (`user1_id`,`last_message_at` DESC) COMMENT '加速会话列表查询（用户1视角）',
  KEY `idx_user2_last_msg` (`user2_id`,`last_message_at` DESC) COMMENT '加速会话列表查询（用户2视角）'
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='私信会话表';

-- 私信消息表
DROP TABLE IF EXISTS `private_message`;
CREATE TABLE `private_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID（主键）',
  `conversation_id` bigint(20) NOT NULL COMMENT '所属会话ID，关联 private_conversation.id',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者用户ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者用户ID',
  `message_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TEXT' COMMENT '消息类型: TEXT / IMAGE / FILE',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容（文本消息存文字，富媒体存JSON: {"url":"...","width":...}）',
  `seq_id` bigint(20) NOT NULL COMMENT '会话维度消息顺序ID（单调递增，用作同步位点）',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SENT' COMMENT '消息状态: SENT-已发送 / DELIVERED-已送达 / READ-已读',
  `client_msg_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端消息ID（UUID，用于幂等去重）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息发送时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识 (0-正常, 1-已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_client_msg` (`sender_id`,`client_msg_id`) COMMENT '发送端去重：同一发送者的同一clientMsgId只存一次',
  KEY `idx_conv_seq` (`conversation_id`,`seq_id`) COMMENT '会话内按序号范围查询消息',
  KEY `idx_receiver_unread` (`receiver_id`,`status`,`create_time`) COMMENT '加速离线消息拉取',
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='私信消息表';

-- 学习资源表
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '上传者ID，关联user.id',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源标题',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源格式: image, pdf, doc, txt, md',
  `college` int(11) DEFAULT NULL COMMENT '所属学院ID（绥化学院二级学院）',
  `professional` int(11) DEFAULT NULL COMMENT '所属专业ID',
  `resource_url` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件在COS/服务器的存储路径',
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小，单位字节(B)',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '资源简介或备注',
  `download_count` int(11) DEFAULT '0' COMMENT '累计下载次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_download` (`download_count`),
  KEY `idx_type_time` (`type`,`create_time`),
  KEY `idx_download_time` (`download_count`,`create_time`),
  KEY `idx_college` (`college`),
  KEY `idx_professional` (`professional`),
  KEY `idx_college_professional` (`college`,`professional`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='学习资源表';

-- 资源收藏表
DROP TABLE IF EXISTS `resource_favorite`;
CREATE TABLE `resource_favorite` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID，关联user.id',
  `resource_id` bigint(20) NOT NULL COMMENT '资源ID，关联资源表',
  `resource_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'resource' COMMENT '资源类型: resource(学习资源), post(帖子), kb_document(知识库文档)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_resource_type` (`user_id`,`resource_id`,`resource_type`) COMMENT '同一用户同一资源类型只能收藏一次',
  KEY `idx_resource_type` (`resource_type`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='资源收藏表';

-- 二手商品收藏表
DROP TABLE IF EXISTS `secondhand_favorite`;
CREATE TABLE `secondhand_favorite` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID，关联user.id',
  `item_id` bigint(20) NOT NULL COMMENT '商品ID，关联secondhand_item.id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_item` (`user_id`,`item_id`) COMMENT '同一用户对同一商品只能收藏一次',
  KEY `idx_item` (`item_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='二手商品收藏表';

-- 二手商品表
DROP TABLE IF EXISTS `secondhand_item`;
CREATE TABLE `secondhand_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `seller_id` bigint(20) NOT NULL COMMENT '卖家ID，关联user.id',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品标题',
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品详细描述',
  `category` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品分类: electronics-数码, books-书籍, daily-日用品, sports-运动, clothes-服装, other-其他',
  `price` decimal(10,2) NOT NULL COMMENT '二手价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `condition_level` tinyint(4) NOT NULL COMMENT '新旧程度: 1-全新, 2-95新, 3-9成新, 4-8成新, 5-7成新及以下',
  `images` json DEFAULT NULL COMMENT '商品图片URL数组',
  `contact_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `contact_wechat` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系微信',
  `view_count` int(11) DEFAULT '0' COMMENT '浏览次数',
  `favorite_count` int(11) DEFAULT '0' COMMENT '收藏次数',
  `status` tinyint(4) DEFAULT '0' COMMENT '商品状态: 0-在售, 1-已售出, 2-已下架',
  `trade_location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '交易地点建议',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_seller` (`seller_id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_price` (`price`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_category_status` (`category`,`status`),
  KEY `idx_seller_status` (`seller_id`,`status`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='二手商品表';

-- 用户信息表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录用户名，全局唯一',
  `user_password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录密码，BCrypt加密存储',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像图片URL地址',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '绑定手机号，唯一索引',
  `role` tinyint(4) DEFAULT '1' COMMENT '角色权限: 1-普通用户, 0-管理员',
  `status` tinyint(4) DEFAULT '1' COMMENT '账户状态: 1-正常, 0-禁用(封号)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册/创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_name` (`user_name`),
  UNIQUE KEY `phone` (`phone`),
  KEY `idx_user_name` (`user_name`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='用户信息表';

-- 用户关注关系表
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `follower_id` bigint(20) NOT NULL COMMENT '关注者ID (粉丝)',
  `followee_id` bigint(20) NOT NULL COMMENT '被关注者ID (博主)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_followee` (`follower_id`,`followee_id`) COMMENT '防止重复关注',
  KEY `idx_followee` (`followee_id`) COMMENT '查询谁关注了我'
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='用户关注关系表';

-- 用户通知表
DROP TABLE IF EXISTS `user_notification`;
CREATE TABLE `user_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '接收者ID',
  `from_user_id` bigint(20) DEFAULT NULL COMMENT '发送者ID',
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型: follow, post_favorite, resource_favorite, comment_reply',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知标题',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '通知内容',
  `business_id` bigint(20) DEFAULT NULL COMMENT '关联业务ID（帖子ID、资源ID等）',
  `link` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '跳转链接',
  `is_read` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否已读: 0-未读, 1-已读',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除: 0-正常, 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`create_time` DESC) COMMENT '用户通知时间索引',
  KEY `idx_user_read` (`user_id`,`is_read`) COMMENT '用户已读状态索引'
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='用户通知表';

-- 用户消息已读位点表
DROP TABLE IF EXISTS `user_read_cursor`;
CREATE TABLE `user_read_cursor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话ID，关联 private_conversation.id',
  `last_read_seq` bigint(20) NOT NULL DEFAULT '0' COMMENT '该用户在此会话中已读到的最大 seq_id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_conv` (`user_id`,`conversation_id`) COMMENT '每个用户在每会话仅一条已读位点',
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='用户消息已读位点表';
