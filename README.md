# 智享绥园

## 项目简介

智享绥园是为在校学生打造的综合性校园服务平台，集成学习资源共享、AI智能助手、失物招领、社区论坛、二手交易等核心功能，旨在提升学生的校园生活体验和学习效率。

本项目采用前后端分离架构，后端使用 Spring Boot 框架，前端使用 Vue 3 框架，AI 服务使用 Python 构建，为用户提供流畅、智能的校园服务体验。

## 功能模块

### 用户中心
- 用户注册、登录、登出
- 个人资料管理（头像上传、信息修改）
- 关注/取关功能
- 浏览历史记录
- 我的收藏、我的帖子、我的资源统一管理

### AI 智能助手
- 智能问答对话（支持流式输出）
- 知识检索（学习资源智能推荐）
- 多轮对话管理
- 对话历史记录

### 社区论坛
- 帖子发布、编辑、删除（支持 Markdown 编辑）
- 评论系统（树形结构展示）
- 点赞、收藏功能
- 帖子分类浏览
- 搜索与筛选

### 失物招领
- 寻物启事发布
- 招领启事发布
- 紧急标记功能
- 状态管理（待处理/已解决）
- 多条件搜索筛选

### 二手市场
- 商品发布、编辑、下架
- 商品分类管理
- 商品浏览、搜索
- 交易状态管理

### 学习资源
- 资源上传、下载
- 资源分类浏览
- 标签系统
- 资源搜索

### 创作者中心
- 内容管理（帖子、资源、商品统一管理的）
- 数据分析（浏览量、点赞数统计）
- 数据可视化展示

### 私信系统
- 用户间私信发送
- 实时消息推送（WebSocket）
- 消息历史记录
- 未读消息提醒

## 技术栈

### 后端技术栈
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.12 | 核心框架 |
| MyBatis | 3.0.3 | ORM 框架 |
| MyBatis Plus | 3.5.5 | ORM 增强框架 |
| MySQL | 8.0.33 | 关系型数据库 |
| Redis | 7.x | 缓存数据库 |
| Spring Security Crypto | 6.2.4 | 密码加密 |
| JJWT | 0.12.6 | JWT 令牌生成与验证 |
| Druid | 1.2.23 | 数据库连接池 |
| Knife4j | 4.5.0 | API 文档生成 |
| PageHelper | 2.1.0 | 分页插件 |
| Hutool | 5.8.27 | Java 工具类库 |
| Fastjson2 | 2.0.47 | JSON 处理 |
| 腾讯云 COS | 5.6.89 | 对象存储（文件上传） |

### AI 服务技术栈
| 技术 | 版本 | 说明 |
|------|------|------|
| Python | 3.11+ | 编程语言 |
| FastAPI | - | Web 框架 |
| LangChain | 0.1.9 | AI 应用开发框架 |
| LangGraph | 0.0.26 | 多 agent 工作流编排 |
| Milvus | 2.4.1 | 向量数据库 |
| DashScope | 1.14.4 | 阿里云大模型服务（通义千问）|
| PyMilvus | 2.3.6 | Milvus Python SDK |

## 项目架构

### 后端架构（Maven 多模块）

```
smart-enjoy-suiyuan/
|
|-- smart-enjoy-suiyuan-common/       # 公共模块
|   |-- annotation/                   # 自定义注解（@RateLimit、@RequireLogin）
|   |-- constant/                     # 常量定义
|   |-- enums/                        # 枚举类
|   |-- exception/                    # 自定义异常体系
|   |-- properties/                   # 配置属性类
|   |-- result/                       # 统一返回结果封装
|   |-- utils/                        # 工具类（JWT、Redis、SMS、COS）
|
|-- smart-enjoy-suiyuan-entity/       # 实体模块
|   |-- dto/                          # 数据传输对象
|   |-- vo/                           # 视图对象
|   |-- pojo/                         # 持久化对象
|   |-- enums/                        # 实体枚举
|
|-- smart-enjoy-suiyuan-server/       # 业务模块
|   |-- controller/                   # 控制器层
|   |-- service/                      # 业务服务层
|   |-- mapper/                       # 数据访问层
|   |-- websocket/                    # WebSocket 处理
|   |-- config/                       # 配置类
|
|-- agent-server-python/               # Python AI 代理服务
|   |-- app/
|       |-- agent/                    # AI Agent 核心
|       |   |-- graph.py              # LangGraph 工作流定义
|       |   |-- nodes/               # 各功能节点
|       |   |-- tools/               # AI 工具封装
|       |-- api/                      # API 接口层
|       |-- config/                   # 配置管理
|
|-- database.sql                       # 数据库初始化脚本
```

## 环境要求

### 基础环境
- JDK 17 或以上版本（推荐 JDK 21）
- Node.js 20 或以上版本
- MySQL 8.0 或以上版本
- Redis 7.0 或以上版本
- Python 3.12 或以上版本
- Maven 3.6 或以上版本

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/your-org/smart-enjoy-suiyuan.git
cd smart-enjoy-suiyuan
```

### 2. 数据库初始化

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE suiyuan CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据库脚本
mysql -u root -p suiyuan < smart-enjoy-suiyuan/database.sql
```

### 3. 配置环境变量

在 `smart-enjoy-suiyuan` 目录下创建 `env.properties` 文件，配置以下内容：

```properties
# 数据库配置
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=suiyuan
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_password

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_password

# JWT 配置
JWT_USER_SECRET_KEY=smartEnjoySuiyuanUserSecretKey2026Secure
JWT_ADMIN_SECRET_KEY=smartEnjoySuiyuanAdminSecretKey2026Secure

# 短信服务配置（腾讯云 SMS）
SMS_SIGN_NAME=你的签名
SMS_TEMPLATE_CODE=你的模板代码

# 腾讯云 COS 配置
TENCENT_COS_SECRET_ID=your_secret_id
TENCENT_COS_SECRET_KEY=your_secret_key
TENCENT_COS_BUCKET=your_bucket
TENCENT_COS_REGION=your_region

# AI 服务配置
DASHSCOPE_API_KEY=your_dashscope_api_key
MCP_SERVICE_TOKEN=your_mcp_token
```

### 4. 启动后端服务

```bash
cd smart-enjoy-suiyuan
mvn clean install
cd smart-enjoy-suiyuan-server
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`

### 5. 启动 Python AI 服务

```bash
cd smart-enjoy-suiyuan/agent-server-python

# 创建虚拟环境
python -m venv .venv

# 激活虚拟环境（Windows）
.venv\Scripts\activate

# 安装依赖
pip install -r requirements.txt

# 启动服务
python -m app.main
```

AI 服务默认运行在 `http://localhost:8000`

### 6. 启动前端服务

```bash
cd suiyuan-user-web
npm install
npm run dev
```

前端服务默认运行在 `http://localhost:5173`

### 7. 访问应用

- 前端页面：`http://localhost:5173`
- 后端 API：`http://localhost:8080`

## 数据库设计

### 数据库表

| 表名 | 说明 | 主要用途 |
|------|------|----------|
| `user` | 用户信息表 | 存储用户基本信息、登录凭证 |
| `user_follow` | 用户关注关系表 | 实现用户关注/粉丝功能 |
| `user_notification` | 用户通知表 | 系统通知、互动提醒 |
| `user_read_cursor` | 用户消息已读位点表 | 记录私信已读位置 |
| `post` | 社区帖子表 | 论坛帖子内容管理 |
| `comment` | 评论回复表 | 帖子/资源的评论互动 |
| `post_like` | 帖子点赞记录表 | 帖子点赞功能 |
| `post_favorite` | 帖子收藏表 | 帖子收藏功能 |
| `resource` | 学习资源表 | 学习资料上传分享 |
| `resource_favorite` | 资源收藏表 | 资源收藏功能 |
| `lost_found` | 失物招领信息表 | 寻物/招领信息发布 |
| `secondhand_item` | 二手商品表 | 二手交易商品管理 |
| `secondhand_favorite` | 二手商品收藏表 | 商品收藏功能 |
| `ai_session` | AI会话表 | AI对话会话管理 |
| `chat_message` | AI聊天记录表 | AI对话历史记录 |
| `private_conversation` | 私信会话表 | 私信会话管理 |
| `private_message` | 私信消息表 | 私信消息内容 |



