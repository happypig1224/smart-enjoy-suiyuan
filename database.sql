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
-- =====================================================
-- 插入测试数据 (每条表10条)
-- =====================================================

-- 1. 用户测试数据 (密码统一使用 MD5('123456') = e10adc3949ba59abbe56e057f20f883e)
INSERT INTO user (nick_name, user_name, user_password, user_gender, user_age, user_grade, avatar, phone, role, status) VALUES
                                                                                                                           ('张三', 'zhangsan', 'e10adc3949ba59abbe56e057f20f883e', 1, 20, '2023级', '/avatars/zhangsan.jpg', '13800138001', 1, 1),
                                                                                                                           ('李四', 'lisi', 'e10adc3949ba59abbe56e057f20f883e', 1, 21, '2022级', '/avatars/lisi.jpg', '13800138002', 1, 1),
                                                                                                                           ('王芳', 'wangfang', 'e10adc3949ba59abbe56e057f20f883e', 2, 19, '2024级', '/avatars/wangfang.jpg', '13800138003', 1, 1),
                                                                                                                           ('赵强', 'zhaoqiang', 'e10adc3949ba59abbe56e057f20f883e', 1, 22, '2021级', '/avatars/zhaoqiang.jpg', '13800138004', 1, 0),
                                                                                                                           ('刘婷', 'liuting', 'e10adc3949ba59abbe56e057f20f883e', 2, 20, '2023级', '/avatars/liuting.jpg', '13800138005', 1, 1),
                                                                                                                           ('陈晨', 'chenchen', 'e10adc3949ba59abbe56e057f20f883e', 0, 0, NULL, NULL, '13800138006', 1, 1),
                                                                                                                           ('周杰', 'zhoujie', 'e10adc3949ba59abbe56e057f20f883e', 1, 23, '2021级', '/avatars/zhoujie.jpg', '13800138007', 0, 1),
                                                                                                                           ('吴迪', 'wudi', 'e10adc3949ba59abbe56e057f20f883e', 1, 20, '2023级', '/avatars/wudi.jpg', '13800138008', 1, 1),
                                                                                                                           ('郑爽', 'zhengshuang', 'e10adc3949ba59abbe56e057f20f883e', 2, 19, '2024级', '/avatars/zhengshuang.jpg', '13800138009', 1, 1),
                                                                                                                           ('孙阳', 'sunyang', 'e10adc3949ba59abbe56e057f20f883e', 1, 22, '2022级', '/avatars/sunyang.jpg', '13800138010', 1, 1);

-- 2. 资源测试数据
INSERT INTO resource (user_id, type, subject, resource_url, file_name, file_size, description, download_count) VALUES
                                                                                                                   (1, 'pdf', 1, '/resources/math/calculus.pdf', '高等数学讲义.pdf', 2048000, '微积分基础教程', 15),
                                                                                                                   (2, 'doc', 2, '/resources/chinese/poetry.doc', '古诗词鉴赏.doc', 512000, '唐诗宋词精选', 8),
                                                                                                                   (3, 'image', 3, '/resources/english/vocabulary.jpg', '英语词汇表.jpg', 256000, '四级核心词汇', 23),
                                                                                                                   (1, 'pdf', 4, '/resources/physics/mechanics.pdf', '力学基础.pdf', 3072000, '牛顿力学讲解', 12),
                                                                                                                   (4, 'txt', 5, '/resources/chemistry/formula.txt', '化学方程式.txt', 128000, '常见化学方程式汇总', 5),
                                                                                                                   (5, 'md', 1, '/resources/math/linear.md', '线性代数笔记.md', 96000, '矩阵与向量', 7),
                                                                                                                   (2, 'pdf', 6, '/resources/bio/cell.pdf', '细胞生物学.pdf', 4096000, '细胞结构与功能', 3),
                                                                                                                   (3, 'doc', 7, '/resources/history/war.doc', '二战历史.doc', 1536000, '第二次世界大战全史', 9),
                                                                                                                   (6, 'image', 8, '/resources/geography/map.jpg', '世界地图.jpg', 1024000, '高清世界地图', 18),
                                                                                                                   (7, 'pdf', 9, '/resources/politics/law.pdf', '法律基础.pdf', 2048000, '宪法与民法概要', 4);

-- 3. 失物招领测试数据
INSERT INTO lost_found (user_id, type, status, title, description, urgent, location, phone_contact, wechat_contact, images, view_count) VALUES
                                                                                                                                            (1, 0, 0, '丢失蓝色书包', '蓝色双肩包，内含笔记本电脑和课本', 1, '图书馆三楼', '13800138001', 'zhangsan_wechat', '["/images/lost/bag1.jpg"]', 45),
                                                                                                                                            (2, 1, 0, '捡到学生卡', '在食堂捡到一张学生卡，姓名张伟', 0, '第二食堂', '13800138002', NULL, '["/images/found/card1.jpg"]', 32),
                                                                                                                                            (3, 0, 1, '丢失钥匙串', '含U盘和门禁卡的钥匙串', 0, '体育馆', '13800138003', 'wangfang_wx', NULL, 28),
                                                                                                                                            (4, 1, 0, '捡到眼镜', '黑色边框眼镜', 0, '教学楼A栋303', '13800138004', NULL, '["/images/found/glasses.jpg"]', 15),
                                                                                                                                            (5, 0, 0, '丢失手机', '华为Mate40，黑色，有手机壳', 1, '操场看台', '13800138005', 'liuting_wx', '["/images/lost/phone.jpg"]', 67),
                                                                                                                                            (1, 1, 1, '捡到钱包', '棕色钱包，内含现金和银行卡', 0, '校门口', '13800138001', NULL, '["/images/found/wallet.jpg"]', 41),
                                                                                                                                            (6, 0, 0, '丢失笔记本', '蓝色封皮笔记本，记有课堂笔记', 0, '自习室', '13800138006', NULL, NULL, 12),
                                                                                                                                            (7, 1, 0, '捡到U盘', '32G银色U盘', 0, '计算机中心', '13800138007', 'admin_wx', '["/images/found/udisk.jpg"]', 23),
                                                                                                                                            (8, 0, 0, '丢失水杯', '白色保温杯', 0, '图书馆', '13800138008', NULL, NULL, 9),
                                                                                                                                            (9, 1, 1, '捡到身份证', '姓名王小明', 0, '西门', '13800138009', 'zhengshuang_wx', '["/images/found/idcard.jpg"]', 53);

-- 4. 帖子测试数据
INSERT INTO post (user_id, title, content, type, like_count, comment_count, view_count, images) VALUES
                                                                                                    (1, 'Java多线程编程心得', '最近在学习Java多线程，分享一下经验...', 0, 23, 8, 156, NULL),
                                                                                                    (2, '高数作业求助', '请问这道微积分题怎么解？lim(x→0) sinx/x', 1, 5, 12, 89, '["/images/post/math1.jpg"]'),
                                                                                                    (3, '食堂新窗口推荐', '二食堂新开了麻辣烫窗口，味道不错！', 2, 34, 15, 234, '["/images/post/canteen1.jpg","/images/post/canteen2.jpg"]'),
                                                                                                    (4, '考研经验分享', '2024考研上岸经验，希望对学弟学妹有帮助', 0, 67, 23, 456, NULL),
                                                                                                    (5, '英语四级备考攻略', '词汇量不够怎么办？分享我的学习方法', 0, 45, 18, 312, '["/images/post/english1.png"]'),
                                                                                                    (6, '校园网经常断怎么办', '最近校园网特别不稳定，有解决办法吗？', 2, 8, 6, 78, NULL),
                                                                                                    (7, '【公告】系统维护通知', '本周六凌晨进行系统升级维护', 3, 3, 2, 145, NULL),
                                                                                                    (8, '求推荐Java项目', '学完Java基础，有什么适合练手的项目吗？', 0, 19, 14, 203, NULL),
                                                                                                    (9, '周末约羽毛球', '有没有一起打羽毛球的同学？', 2, 12, 7, 98, NULL),
                                                                                                    (1, '数据结构与算法学习路线', '从零开始学习数据结构与算法', 0, 56, 31, 432, '["/images/post/algorithm.jpg"]');

-- 5. 评论测试数据
INSERT INTO comment (user_id, content, post_id, lost_item_id, parent_id, like_count) VALUES
                                                                                         (2, '讲得真好，学到了！', 1, NULL, 0, 5),
                                                                                         (3, '感谢分享，很有帮助', 1, NULL, 0, 3),
                                                                                         (4, '这道题可以用洛必达法则', 2, NULL, 0, 7),
                                                                                         (1, '明天就去试试！', 3, NULL, 0, 2),
                                                                                         (5, '感谢经验分享，收藏了', 4, NULL, 0, 8),
                                                                                         (6, '同样遇到这个问题', 6, NULL, 0, 1),
                                                                                         (8, '推荐做电商项目', 8, NULL, 0, 4),
                                                                                         (9, '我可以，周末几点？', 9, NULL, 0, 3),
                                                                                         (2, '洛必达法则正解', 2, NULL, 4, 2),  -- 回复评论ID=4
                                                                                         (3, '一起加油！', 4, NULL, 5, 1);

-- 6. 聊天消息测试数据
INSERT INTO chat_message (session_id, user_id, role, content) VALUES
                                                                  (1001, 1, 'user', '你好，能帮我解释一下什么是AI吗？'),
                                                                  (1001, 1, 'assistant', 'AI（人工智能）是计算机科学的一个分支，致力于创建能够执行通常需要人类智能的任务的系统。'),
                                                                  (1001, 1, 'user', '那机器学习呢？'),
                                                                  (1001, 1, 'assistant', '机器学习是AI的子集，通过数据训练模型来学习和改进。'),
                                                                  (1002, 2, 'user', '如何学习编程？'),
                                                                  (1002, 2, 'assistant', '建议从Python开始，然后学习数据结构和算法。'),
                                                                  (1002, 2, 'user', '有什么推荐的书籍吗？'),
                                                                  (1002, 2, 'assistant', '《Python编程从入门到实践》和《算法图解》都很不错。'),
                                                                  (1003, 3, 'user', '高等数学好难怎么办？'),
                                                                  (1003, 3, 'assistant', '建议多做题，看视频课程，理解概念本质。'),
                                                                  (1004, 4, 'user', '英语四级怎么准备？'),
                                                                  (1004, 4, 'assistant', '背单词+刷真题+练听力，坚持每天学习。'),
                                                                  (1005, 5, 'user', '什么是数据库索引？'),
                                                                  (1005, 5, 'assistant', '索引是一种数据结构，用于快速查找数据库中的数据。');

-- =====================================================
-- 验证数据插入
-- =====================================================
SELECT 'user' AS table_name, COUNT(*) AS count FROM user
UNION ALL
SELECT 'resource', COUNT(*) FROM resource
UNION ALL
SELECT 'lost_found', COUNT(*) FROM lost_found
UNION ALL
SELECT 'post', COUNT(*) FROM post
UNION ALL
SELECT 'comment', COUNT(*) FROM comment
UNION ALL
SELECT 'chat_message', COUNT(*) FROM chat_message;