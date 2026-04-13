-- =====================================================
-- 数据库初始化: 智享学堂 (Smart Learning Academy)
-- 描述: 包含用户管理、资源共享、失物招领、社区互动及AI聊天功能的校园综合平台
-- =====================================================
CREATE DATABASE IF NOT EXISTS `smart_learning_academy`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `smart_learning_academy`;

-- =====================================================
-- 1. 用户表 (User Management)
-- 描述: 存储平台所有用户的基础信息、认证凭据及账户状态
-- =====================================================
CREATE TABLE user (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                      nick_name VARCHAR(50) NOT NULL COMMENT '用户昵称',
                      user_name VARCHAR(50) UNIQUE NOT NULL COMMENT '登录用户名，全局唯一',
                      user_password VARCHAR(100) NOT NULL COMMENT '登录密码，BCrypt加密存储',
                      user_gender TINYINT DEFAULT 0 COMMENT '性别: 0-未填写, 1-男, 2-女',
                      user_age INT COMMENT '用户年龄',
                      user_grade VARCHAR(20) COMMENT '所属年级，如2023级',
                      avatar VARCHAR(255) COMMENT '头像图片URL地址',
                      phone VARCHAR(20) UNIQUE COMMENT '绑定手机号，唯一索引',
                      role TINYINT DEFAULT 1 COMMENT '角色权限: 1-普通用户, 0-管理员',
                      status TINYINT DEFAULT 1 COMMENT '账户状态: 1-正常, 0-禁用(封号)',
                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册/创建时间',
                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                      is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
                      INDEX idx_user_name (user_name),
                      INDEX idx_phone (phone),
                      INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- =====================================================
-- 2. 资源表 (Resource Sharing)
-- 描述: 存储用户上传的学习资料（PDF/文档/图片等）元数据
-- =====================================================
CREATE TABLE resource (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                          user_id BIGINT NOT NULL COMMENT '上传者ID，关联user.id',
                          type VARCHAR(20) NOT NULL COMMENT '资源格式: image, pdf, doc, txt, md',
                          subject INT DEFAULT NULL COMMENT '所属学科分类ID',
                          resource_url VARCHAR(1000) NOT NULL COMMENT '文件在COS/服务器的存储路径',
                          file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
                          file_size BIGINT COMMENT '文件大小，单位字节(B)',
                          description TEXT COMMENT '资源简介或备注',
                          download_count INT DEFAULT 0 COMMENT '累计下载次数',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          INDEX idx_user (user_id),
                          INDEX idx_type (type),
                          INDEX idx_subject (subject),
                          INDEX idx_download (download_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习资源表';

-- =====================================================
-- 3. 失物招领表 (Lost and Found)
-- 描述: 记录校园内的寻物启事与招领信息发布
-- =====================================================
CREATE TABLE lost_found (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                            user_id BIGINT NOT NULL COMMENT '发布人ID，关联user.id',
                            type TINYINT NOT NULL COMMENT '帖子类型: 0-寻物启事, 1-招领启事',
                            status TINYINT DEFAULT 0 COMMENT '处理状态: 0-未解决, 1-已解决/已领取',
                            title VARCHAR(100) NOT NULL COMMENT '标题，简述物品',
                            description TEXT NOT NULL COMMENT '详细描述，丢失/拾取经过',
                            urgent TINYINT DEFAULT 0 COMMENT '紧急程度: 0-普通, 1-紧急(置顶)',
                            location VARCHAR(100) COMMENT '具体地点，如图书馆三楼',
                            phone_contact VARCHAR(20) COMMENT '联系电话，可为空',
                            wechat_contact VARCHAR(50) COMMENT '联系微信号',
                            images JSON COMMENT '图片列表，存储URL数组',
                            view_count INT DEFAULT 0 COMMENT '浏览量/查看次数',
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
                            update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            INDEX idx_user (user_id),
                            INDEX idx_type_status (type, status),
                            INDEX idx_urgent (urgent),
                            INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='失物招领信息表';

-- =====================================================
-- 4. 帖子表 (Community Posts)
-- 描述: 校园社区论坛，支持技术讨论、生活分享等
-- =====================================================
CREATE TABLE post (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                      user_id BIGINT NOT NULL COMMENT '发布者ID，关联user.id',
                      title VARCHAR(100) NOT NULL COMMENT '帖子标题',
                      content TEXT NOT NULL COMMENT '帖子正文内容',
                      type TINYINT NOT NULL COMMENT '板块分类: 0-技术讨论, 1-课程问题, 2-校园生活, 3-其他',
                      like_count INT DEFAULT 0 COMMENT '点赞总数',
                      comment_count INT DEFAULT 0 COMMENT '评论总数',
                      view_count INT DEFAULT 0 COMMENT '浏览次数',
                      images JSON COMMENT '配图列表，存储URL数组',
                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      INDEX idx_user (user_id),
                      INDEX idx_type (type),
                      INDEX idx_like_count (like_count),
                      INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子表';

-- =====================================================
-- 5. 评论表 (Comments)
-- 描述: 支持对帖子和失物招领进行评论及回复
-- =====================================================
CREATE TABLE comment (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                         user_id BIGINT NOT NULL COMMENT '评论者ID，关联user.id',
                         content TEXT NOT NULL COMMENT '评论内容',
                         post_id BIGINT NULL COMMENT '关联帖子ID，回复帖子时使用',
                         lost_item_id BIGINT NULL COMMENT '关联失物招领ID，回复招领时使用',
                         parent_id BIGINT DEFAULT 0 COMMENT '父级评论ID，用于实现二级回复',
                         like_count INT DEFAULT 0 COMMENT '点赞数',
                         status TINYINT DEFAULT 1 COMMENT '状态: 1-正常显示, -1-逻辑删除',
                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
                         update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
                         FOREIGN KEY (user_id) REFERENCES user(id),
                         FOREIGN KEY (post_id) REFERENCES post(id),
                         FOREIGN KEY (lost_item_id) REFERENCES lost_found(id),
                         INDEX idx_user (user_id),
                         INDEX idx_post (post_id),
                         INDEX idx_lost_item (lost_item_id),
                         INDEX idx_parent (parent_id),
                         INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论回复表';

-- =====================================================
-- 6. 聊天消息表 (AI Chat History)
-- 描述: 存储用户与AI智能助手的对话历史记录
-- =====================================================
CREATE TABLE chat_message (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                              session_id BIGINT NOT NULL COMMENT '会话ID，用于区分不同轮次的对话',
                              user_id BIGINT NOT NULL COMMENT '用户ID，关联user.id',
                              role ENUM('user','assistant','system','tool') NOT NULL COMMENT '角色: user(用户), assistant(AI), system(系统), tool(工具)',
                              content TEXT NOT NULL COMMENT '消息内容，包含文本或Token',
                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              INDEX idx_session_create (session_id, create_time),
                              INDEX idx_user_create (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天记录表';
