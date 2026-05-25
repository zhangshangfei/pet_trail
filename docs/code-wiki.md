# 宠迹 (Pet Trail) - Code Wiki

> **版本**：v2.1.0 | **生成日期**：2026-05-24 | **项目状态**：开发迭代中

---

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 整体架构](#2-整体架构)
- [3. 技术栈总览](#3-技术栈总览)
- [4. 后端架构详解](#4-后端架构详解)
  - [4.1 分层架构](#41-分层架构)
  - [4.2 核心配置类](#42-核心配置类)
  - [4.3 安全与认证](#43-安全与认证)
  - [4.4 自定义注解体系](#44-自定义注解体系)
  - [4.5 统一响应与异常处理](#45-统一响应与异常处理)
  - [4.6 多级缓存架构](#46-多级缓存架构)
  - [4.7 消息队列 (RabbitMQ)](#47-消息队列-rabbitmq)
  - [4.8 WebSocket 实时通信](#48-websocket-实时通信)
  - [4.9 定时任务](#49-定时任务)
- [5. 核心业务模块详解](#5-核心业务模块详解)
  - [5.1 用户系统](#51-用户系统)
  - [5.2 宠物管理](#52-宠物管理)
  - [5.3 打卡系统](#53-打卡系统)
  - [5.4 动态社区](#54-动态社区)
  - [5.5 推荐算法](#55-推荐算法)
  - [5.6 健康分析](#56-健康分析)
  - [5.7 AI 分析服务](#57-ai-分析服务)
  - [5.8 内容审核](#58-内容审核)
  - [5.9 通知系统](#59-通知系统)
  - [5.10 会员与支付](#510-会员与支付)
- [6. 前端架构详解 (Uni-app)](#6-前端架构详解-uni-app)
  - [6.1 页面结构](#61-页面结构)
  - [6.2 API 模块](#62-api-模块)
  - [6.3 状态管理](#63-状态管理)
  - [6.4 工具类](#64-工具类)
  - [6.5 组件库](#65-组件库)
- [7. 管理后台架构详解](#7-管理后台架构详解)
  - [7.1 页面与权限](#71-页面与权限)
  - [7.2 API 模块](#72-api-模块)
- [8. 数据库设计](#8-数据库设计)
  - [8.1 表结构总览](#81-表结构总览)
  - [8.2 核心表关系](#82-核心表关系)
  - [8.3 索引设计](#83-索引设计)
- [9. 依赖关系图](#9-依赖关系图)
- [10. 项目运行与部署](#10-项目运行与部署)
  - [10.1 环境要求](#101-环境要求)
  - [10.2 本地开发启动](#102-本地开发启动)
  - [10.3 Docker Compose 部署](#103-docker-compose-部署)
  - [10.4 微信云托管部署](#104-微信云托管部署)
- [11. 设计优缺点分析](#11-设计优缺点分析)
  - [11.1 设计优点](#111-设计优点)
  - [11.2 设计缺点与技术债务](#112-设计缺点与技术债务)
- [12. 改进建议](#12-改进建议)
- [13. 后续盈利模式分析](#13-后续盈利模式分析)

---

## 1. 项目概述

**宠迹 (Pet Trail)** 是一款融合宠物分享、健康管理、习惯打卡的微信小程序，定位为"宠物的生活记录工具 + 轻社交社区"。

| 维度 | 说明 |
|------|------|
| 项目名称 | 宠迹 (Pet Trail) |
| 产品定位 | 宠物生活记录工具 + 轻社交社区 |
| 目标用户 | 20-35岁宠物主人，女性偏多，有记录和分享习惯 |
| 核心价值 | 科学记录宠物健康、养成良好养宠习惯、通过内容分享获得情感满足 |
| 官网 | https://pettrail.openclaw.com |
| 当前版本 | v2.1.0 |
| 许可证 | MIT License |

---

## 2. 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                          用户层                              │
│   微信小程序 (Uni-app)  │  H5 页面  │  管理后台 (Vue3+EP)    │
└────────────────────────────┬────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────┐
│                          网关层                              │
│   Nginx (反向代理 + 负载均衡 + SSL + Gzip)                   │
│   微信云托管 (小程序环境 callContainer)                       │
└────────────────────────────┬────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────┐
│                   应用层 (Spring Boot 3.1.5)                 │
│                                                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ 用户认证  │ │ 宠物管理  │ │ 打卡系统  │ │ 社区动态  │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ 健康记录  │ │ 提醒系统  │ │ 关注系统  │ │ 通知推送  │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ 内容审核  │ │ 推荐算法  │ │ AI 分析   │ │ 会员支付  │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 基础设施: JWT认证 / AOP日志 / Sentinel限流 / WebSocket │   │
│  └─────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────┐
│                          数据层                              │
│   MySQL 8.0  │  Redis 7.x (L2)  │  Caffeine (L1)  │  COS   │
└─────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────┐
│                        消息层 (可选)                          │
│   RabbitMQ (延迟队列 + 死信队列)                              │
└─────────────────────────────────────────────────────────────┘
```

### 架构特点

- **前后端分离**：前端 (Uni-app) + 后端 (Spring Boot) + 管理后台 (Vue3) 三端独立
- **多级缓存**：Caffeine (L1) → Redis (L2) → MySQL (L3) 三级缓存架构
- **双存储策略**：社交互动数据采用 Redis + 数据库双重存储
- **异步解耦**：Spring Event + RabbitMQ 实现异步处理
- **容器化部署**：Docker 多阶段构建 + Docker Compose 编排

---

## 3. 技术栈总览

### 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 开发语言 (虚拟线程 + Sealed Classes) |
| Spring Boot | 3.1.5 | 应用框架 |
| Spring Security | 6.x | 安全框架 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.x | 分布式缓存 (Lettuce 连接池) |
| Caffeine | 3.1.8 | 本地缓存 (L1) |
| JWT (jjwt) | 0.12.6 | Token 认证 |
| 腾讯云 COS | 5.6.89 | 对象存储 |
| Sentinel | 2022.0.0.0 | 限流熔断 |
| Spring WebSocket | — | 实时通信 |
| RabbitMQ | — | 消息队列 (可选) |
| SpringDoc OpenAPI | 2.3.0 | API 文档 |
| Fastjson2 | 2.0.47 | JSON 处理 |
| Apache POI | 5.2.5 | Excel 导出 |
| GoogleAuth | 1.5.0 | TOTP 两步验证 |
| ZXing | 3.5.3 | 二维码生成 |
| Lombok | — | 代码简化 |

### 前端技术栈 (小程序)

| 技术 | 版本 | 用途 |
|------|------|------|
| Uni-app | 3.0 (alpha) | 跨平台框架 (微信小程序 + H5) |
| Vue.js | 3.4+ | 渐进式框架 |
| uView Plus | 3.1+ | UI 组件库 |
| Pinia | 2.1+ | 状态管理 |
| ECharts | 5.5+ | 数据可视化 |
| Sass | 1.70+ | CSS 预处理器 |

### 管理后台技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4.27 | 前端框架 |
| Element Plus | 2.6.1 | UI 组件库 |
| ECharts | 5.5.0 | 图表可视化 |
| Pinia | 2.1.7 | 状态管理 |
| Axios | 1.6.8 | HTTP 客户端 |
| SortableJS | 1.15.7 | 拖拽排序 |

---

## 4. 后端架构详解

### 4.1 分层架构

后端采用经典的 **Controller → Service → Mapper** 三层架构：

```
com.pettrail.pettrailbackend/
├── controller/          # 控制层 (34个控制器)
│   ├── admin/           # 管理端控制器 (21个)
│   │   └── BaseAdminController.java
│   ├── BaseController.java          # 用户端基类
│   ├── UserController.java          # 用户
│   ├── PetController.java           # 宠物
│   ├── PostController.java          # 动态
│   ├── CheckinController.java       # 打卡
│   ├── HealthRecordController.java  # 健康记录
│   ├── HealthAnalysisController.java # 健康分析
│   ├── FollowController.java        # 关注
│   ├── NotificationController.java  # 通知
│   ├── ReminderController.java      # 提醒
│   ├── ... (更多控制器)
│   └── WechatPayCallbackController.java # 支付回调
├── service/             # 服务层 (42个服务)
├── mapper/              # 数据访问层 (41个Mapper)
├── entity/              # 实体层 (40个实体类)
├── dto/                 # 数据传输对象 (60+ DTO/VO)
├── converter/           # 对象转换器
├── config/              # 配置类 (16个)
├── security/            # 安全过滤器
├── annotation/          # 自定义注解 (6个)
├── websocket/           # WebSocket 处理器
├── task/                # 定时任务 (4个)
├── mq/                  # 消息队列 (3个)
├── event/               # Spring 事件
├── listener/            # 事件监听器
├── exception/           # 自定义异常 (4个)
├── enums/               # 错误码枚举
└── util/                # 工具类 (5个)
```

### 4.2 核心配置类

| 配置类 | 职责 |
|--------|------|
| `SecurityConfig` | Spring Security 安全策略、CORS、JWT 过滤器注册 |
| `CacheConfig` | Caffeine 本地缓存 + Redis 分布式缓存配置 |
| `RedisConfig` | Redis 序列化配置 |
| `MybatisPlusConfig` | 分页插件、逻辑删除配置 |
| `RabbitMQConfig` | RabbitMQ 延迟队列、死信队列、交换机绑定 (条件加载) |
| `CosConfig` | 腾讯云 COS 客户端配置 |
| `JacksonConfig` | JSON 序列化/反序列化配置 |
| `SentinelConfig` | Sentinel 限流规则配置 |
| `WebSocketConfig` | WebSocket 端点注册 |
| `SpringDocConfig` | API 文档配置 |
| `DataInitConfig` | 数据初始化 (默认管理员、打卡项等) |
| `GlobalExceptionHandler` | 全局异常处理器 |

### 4.3 安全与认证

#### 认证流程

```
用户请求 → JwtAuthenticationFilter → 解析 Bearer Token
    → 校验 Token 有效性 (JwtUtil)
    → 检查 Token 黑名单 (Redis: jwt:blacklist:{sha256(token)})
    → 提取 userId + openid
    → 判断角色 (openid 以 "admin:" 开头为管理员)
    → 设置 SecurityContext
```

#### 关键类说明

**`JwtUtil`** — JWT 工具类

| 方法 | 说明 |
|------|------|
| `generateToken(Long userId, String openid)` | 生成 JWT Token，有效期 7 天 |
| `getUserIdFromToken(String token)` | 从 Token 提取用户 ID |
| `getOpenidFromToken(String token)` | 从 Token 提取微信 OpenID |
| `validateToken(String token)` | 验证 Token 有效性 |
| `getRemainingTimeInSeconds(String token)` | 获取 Token 剩余有效期 |

**`JwtAuthenticationFilter`** — JWT 认证过滤器

- 继承 `OncePerRequestFilter`，每次请求只执行一次
- 从 `Authorization: Bearer xxx` 提取 Token
- 使用 SHA-256 哈希 Token 后检查 Redis 黑名单
- 管理员 Token 的 openid 格式为 `admin:{ROLE}`，自动赋予 `ROLE_ADMIN` + `ROLE_{ROLE}` 权限
- 普通用户赋予 `ROLE_USER` 权限

**`SecurityConfig`** — 安全配置

- 无状态会话 (STATELESS)
- CSRF 禁用 (小程序场景不需要)
- CORS 允许所有来源 (开发阶段)
- 公开端点：登录、注册、动态 Feed、Swagger、WebSocket、支付回调
- 管理端需要 `ROLE_ADMIN`
- 部分 GET 请求公开访问 (动态详情、用户资料等)

### 4.4 自定义注解体系

| 注解 | 作用域 | 功能 |
|------|--------|------|
| `@RequireRole("ADMIN")` | 方法 | 角色校验，通过 AOP 检查当前用户角色 |
| `@RequireButton("export")` | 方法 | 按钮级权限校验 |
| `@OperationLog(module="", action="", detail="")` | 方法 | 操作日志自动记录，通过 AOP 切面实现 |

### 4.5 统一响应与异常处理

**`Result<T>`** — 统一响应封装

```java
{
    "success": true/false,
    "code": 200/400/401/403/404/500/1xxx-5xxx,
    "data": T,
    "message": "操作成功/错误信息"
}
```

**`ErrorCodeEnum`** — 错误码规范

| 范围 | 模块 | 示例 |
|------|------|------|
| 1xxx | 用户相关 | 1001-用户未登录, 1004-登录失败, 1006-密码错误 |
| 2xxx | 宠物相关 | 2001-宠物不存在, 2002-无权限操作 |
| 3xxx | 健康记录 | 3001-当天已记录体重 |
| 4xxx | 社区相关 | 4001-帖子不存在, 4004-评论频率过高 |
| 45xx | 文件上传 | 4501-文件为空, 4502-文件过大 |
| 5xxx | 系统通用 | 5000-系统错误, 5005-请求频繁 |

**`GlobalExceptionHandler`** — 全局异常处理

| 异常类型 | HTTP 状态码 | 处理方式 |
|----------|-------------|----------|
| `BusinessException` | 200 | 返回业务错误码和消息 |
| `NotFoundException` | 404 | 资源不存在 |
| `UnauthorizedException` | 401 | 未授权 |
| `ForbiddenException` | 403 | 权限不足 |
| `AccessDeniedException` | 403 | Spring Security 拒绝 |
| `MethodArgumentNotValidException` | 400 | 参数校验失败 |
| `Exception` | 500 | 兜底处理 |

### 4.6 多级缓存架构

```
请求 → L1: Caffeine 本地缓存 (毫秒级)
         ↓ miss
       L2: Redis 分布式缓存 (亚毫秒级)
         ↓ miss
       L3: MySQL 数据库
         ↓
       回写 L1 + L2
```

**`MultiLevelCache`** 工具类核心方法：

| 方法 | 说明 |
|------|------|
| `get(key, clazz, loader)` | 三级缓存读取，miss 时从 loader 加载 |
| `get(key, clazz, loader, timeout, unit)` | 支持自定义过期时间 |
| `put(key, value)` | 同时写入 L1 + L2 |
| `evict(key)` | 同时清除 L1 + L2 |

**缓存策略**：

| 缓存名 | 容量 | 过期时间 | 用途 |
|--------|------|----------|------|
| `local` | 5000 | 10min | 通用本地缓存 |
| `user` | 1000 | 10min | 用户信息 |
| `pet` | 2000 | 10min | 宠物信息 |
| `post` | 5000 | 5min | 动态详情 |
| Redis 默认 | — | 30min | 分布式缓存 |

### 4.7 消息队列 (RabbitMQ)

RabbitMQ 为**可选组件**，通过 `spring.rabbitmq.enabled=true` 启用。

**队列拓扑**：

```
reminder.delayed.exchange (x-delayed-message)
    ├── reminder.feeding  → reminder.feeding.queue  (喂食提醒)
    └── reminder.checkin  → reminder.checkin.queue  (打卡提醒)

reminder.dlx.exchange (死信交换机)
    ├── feeding.dlq  (喂食提醒死信队列)
    └── checkin.dlq  (打卡提醒死信队列)
```

**核心类**：

| 类 | 职责 |
|----|------|
| `RabbitMQConfig` | 声明交换机、队列、绑定关系 |
| `ReminderMessageProducer` | 发送延迟提醒消息 |
| `ReminderMessageConsumer` | 消费提醒消息并推送通知 |
| `PostCreateConsumer` | 消费动态创建事件 (异步审核、粉丝通知) |

### 4.8 WebSocket 实时通信

**`NotificationWebSocketHandler`** — 通知 WebSocket 处理器

- 连接地址：`ws://host/ws/notify?token={jwt}`
- 使用 `ConcurrentHashMap<Long, WebSocketSession>` 管理用户连接
- 支持同一用户重连时自动关闭旧连接
- 连接建立后推送初始数据 (未读数 + 系统消息)
- 支持 `ping/pong` 心跳保活
- 支持客户端主动查询未读数

| 方法 | 说明 |
|------|------|
| `sendToUser(userId, data)` | 向指定用户推送消息 |
| `notifyNewNotification(userId, type, content, targetId)` | 推送新通知 |
| `notifyUnreadCount(userId)` | 推送未读数更新 |
| `isUserOnline(userId)` | 检查用户是否在线 |

### 4.9 定时任务

| 任务类 | 职责 | 调度频率 |
|--------|------|----------|
| `ReminderTask` | 检查并触发到期提醒 (疫苗/驱虫/喂食) | 定时执行 |
| `FallbackReminderTask` | 兜底提醒 (RabbitMQ 不可用时) | 定时执行 |
| `PostAuditScheduledTask` | 定时复审动态内容 | 定时执行 |
| `AchievementCheckTask` | 成就达成检查 | 定时执行 |

---

## 5. 核心业务模块详解

### 5.1 用户系统

**核心类**：`UserService`、`UserController`

| 功能 | 说明 |
|------|------|
| 微信登录 | 通过 `wx.login` 获取 code → 后端换取 openid → 生成 JWT |
| 资料编辑 | 昵称、头像、性别修改 |
| 头像裁剪 | 前端裁剪后上传至 COS |
| 发现用户 | 基于推荐算法的用户推荐 |
| 用户行为追踪 | 浏览/点赞/收藏/评论/分享行为记录 |

### 5.2 宠物管理

**核心类**：`PetService`、`PetController`、`PetAlbumService`

| 功能 | 说明 |
|------|------|
| CRUD | 添加/编辑/删除宠物 |
| 成长相册 | 按日期记录宠物照片 |
| 日历视图 | 日历形式展示宠物重要日期 |
| 品种分类 | 支持猫(1)、狗(2)、其他(0) |
| 体重管理 | 体重记录与趋势分析 |

### 5.3 打卡系统

**核心类**：`CheckinService`、`CheckinController`、`CheckinReminderService`

| 功能 | 说明 |
|------|------|
| 默认打卡项 | 8 个系统预置打卡项 (喂食、遛狗、梳毛等) |
| 自定义打卡项 | 用户可添加/隐藏打卡项 |
| 打卡统计 | 连续打卡天数、周打卡率 |
| 打卡报表 | 周报/月报生成 |
| 打卡提醒 | 定时提醒用户打卡 |

### 5.4 动态社区

**核心类**：`PostService`、`PostController`、`CommentService`

**核心方法**：

| 方法 | 说明 |
|------|------|
| `createPost()` | 发布动态 (含内容审核) |
| `getFeed(page, size, tab, userId)` | 获取动态流 (all/follow/recommend/collect) |
| `toggleLike(postId, userId)` | 点赞/取消点赞 (Redis + DB 双写) |
| `toggleEe(postId, userId)` | 收藏/取消收藏 (Redis + DB 双写) |
| `deletePost(postId, userId)` | 删除动态 (级联清除缓存) |

**互动数据存储策略**：

- 点赞/收藏状态：Redis Hash (`post:like:{postId}`, `post:user:like:{postId}:{userId}`)
- 计数：Redis Hash + 数据库原子更新 (`updateLikeCountAtomic`)
- 读取优先查 Redis，miss 时回源数据库
- 使用唯一索引防重复 (DuplicateKeyException 处理)

### 5.5 推荐算法

**核心类**：`RecommendService`

#### 用户推荐 (6 维评分)

| 维度 | 权重 | 计算方式 |
|------|------|----------|
| 品种匹配 (breed) | 30% | 同品种 1.0、同类别 0.6、不匹配 0.1 |
| 活跃度 (activity) | 25% | 粉丝数/50 + 帖子数/20 加权 |
| 社交关系 (social) | 20% | 二度关注 1.0、否则 0.0 |
| 互动关系 (interact) | 15% | 点赞过同一动态 1.0、否则 0.0 |
| 新用户加权 (newUser) | 10% | 3天内1.0、7天0.8、14天0.5、30天0.3 |

#### 动态推荐 (6 维评分)

| 维度 | 权重 | 计算方式 |
|------|------|----------|
| 兴趣匹配 (interest) | 30% | 作者宠物品种/类别匹配度 |
| 热度 (hot) | 25% | (点赞×3 + 评论×2 + 分享) / (小时+2) |
| 社交关系 (social) | 20% | 关注0.5、二度1.0 |
| 互动历史 (interact) | 15% | 点赞过作者 1.0 |
| 新鲜度 (fresh) | 5% | 1h内1.0、6h内0.9、24h内0.7 递减 |
| 行为偏好 (behavior) | 5% | 近期浏览/收藏过 0.8/1.0 |

**缓存策略**：推荐结果缓存 2 小时 (用户) / 30 分钟 (动态)

### 5.6 健康分析

**核心类**：`HealthAnalysisService`、`HealthAnalysisCacheService`

**4 维度动态评分算法**：

| 维度 | 权重 | 评分逻辑 |
|------|------|----------|
| 疫苗 (vaccine) | 30% | 完成率×100 - 逾期数×15 |
| 驱虫 (parasite) | 25% | 完成率×100 - 逾期数×12 |
| 体重 (weight) | 25% | 基于近30天体重波动率 (≤2%=100, ≤5%=90, ≤10%=75, ≤15%=60, >15%=40) |
| 打卡 (checkin) | 20% | 本周活跃天数/7 × 100 |

**输出结构**：

```
HealthAnalysisVO
├── score (0-100 综合评分)
├── level (优秀/良好/一般/需关注)
├── warnings[] (预警项: type + message + severity)
├── suggestions[] (建议列表)
├── trends{} (趋势: vaccine/weight/activity)
├── detail
│   ├── vaccineScore / parasiteScore / weightScore / checkinScore
│   ├── vaccineAnalysis (total/completed/overdue/nextVaccine)
│   └── weightAnalysis (currentWeight/avgWeight30d/trend/changeRate)
└── aiAnalysis (AI 生成的深度分析文本)
```

### 5.7 AI 分析服务

**核心类**：`AiAnalysisService`、`AiModelService`

**特性**：

- 支持多模型切换 (通过管理后台配置)
- 默认使用 OpenRouter API (兼容 OpenAI 格式)
- 备选智谱 GLM-4.7-Flash
- **熔断器机制**：连续失败 3 次后开启熔断，60 秒后自动恢复
- 模型调用统计与记录
- 分析结果缓存 (避免重复调用)

**调用流程**：

```
健康分析请求 → 检查 AI 开关 → 检查熔断器 → 检查缓存
    → 获取当前模型配置 → 构建 Prompt → 调用 LLM API
    → 解析响应 → 缓存结果 → 记录调用统计
    → 失败时降级为规则引擎结果
```

### 5.8 内容审核

**核心类**：`ContentAuditService`

**双重审核机制**：

1. **本地敏感词检测**：内置敏感词列表，快速拦截
2. **微信 API 审核**：
   - 文本：`msg_sec_check` (v2 版本)
   - 图片：`img_sec_check`
   - 支持自动重试 (最多 2 次，指数退避)
   - access_token 过期自动刷新
   - 审核失败时**放行** (降级策略，避免阻断正常使用)

### 5.9 通知系统

**核心类**：`NotificationService`、`NotificationWebSocketHandler`

**5 种通知类型**：

| 类型 | 触发场景 |
|------|----------|
| like | 动态被点赞 |
| comment | 动态被评论 |
| follow | 被关注 |
| favorite | 动态被收藏 |
| system | 系统通知 (广播) |

**推送机制**：WebSocket 实时推送 + 数据库持久化

### 5.10 会员与支付

**核心类**：`UserMembershipService`、`WechatPayService`

**会员方案**：

| 方案 | 价格 | 说明 |
|------|------|------|
| 月度会员 | ¥9.9/月 | 基础会员权益 |
| 年度会员 | ¥99/年 | 省¥19.8 |

**会员权益**：无限相册、高级统计、主题定制、智能提醒、数据导出、医院优先

**支付流程**：

```
用户下单 → WechatPayService.createJsapiOrder() → 生成预付单
    → 微信支付 → 回调 WechatPayCallbackController
    → 验签 → 更新订单状态 → 激活会员
```

---

## 6. 前端架构详解 (Uni-app)

### 6.1 页面结构

前端共 **28 个页面**，分为 4 个 TabBar 页 + 24 个子页面：

**TabBar 页面**：

| Tab | 页面 | 功能 |
|-----|------|------|
| 首页 | `home/index` | 宠物信息展示、快捷操作 |
| 发现 | `discover/index` | 动态 Feed 流、推荐用户 |
| 打卡 | `checkin/index` | 每日打卡、打卡统计 |
| 我的 | `me/index` | 个人中心、设置 |

**子页面分类**：

| 模块 | 页面 |
|------|------|
| 宠物 | `pets/index`, `pets/detail`, `pets/album`, `pets/calendar` |
| 健康 | `health/index`, `health/analysis`, `health/record`, `health/vaccine`, `health/vaccine-list`, `health/parasite`, `health/parasite-list` |
| 打卡 | `checkin/add-item`, `checkin/manage-items`, `checkin/reminder`, `checkin/report` |
| 社区 | `post/detail`, `publish/index`, `tag/posts` |
| 用户 | `user/detail`, `user/edit`, `follows/index` |
| 通知 | `notification/index` |
| 我的 | `me/settings`, `me/about`, `me/feedback`, `me/feedback-list`, `me/achievement`, `me/membership`, `me/feeding-reminder` |
| 挑战 | `challenge/index`, `challenge/detail`, `challenge/my` |
| 仪表盘 | `dashboard/index` |
| 商城 | `shop/index`, `shop/detail` |
| 医院 | `vet/index`, `vet/detail`, `vet/appointments` |
| 成就 | `achievement/detail` |

### 6.2 API 模块

共 **20 个 API 模块**：

| 模块 | 文件 | 功能 |
|------|------|------|
| 认证 | `auth.js` | 微信登录、Token 管理 |
| 宠物 | `pet.js` | 宠物 CRUD、相册 |
| 动态 | `post.js` | 发布、Feed、点赞、收藏、评论 |
| 打卡 | `checkin.js` | 打卡、统计、报表 |
| 健康 | `health.js` | 体重记录、健康分析 |
| 关注 | `follow.js` | 关注/取关、粉丝列表 |
| 通知 | `notification.js` | 通知列表、已读标记 |
| 提醒 | `reminder.js` | 疫苗/驱虫/喂食提醒 |
| 标签 | `tag.js` | 话题标签 |
| 上传 | `upload.js` | 文件上传 |
| 反馈 | `feedback.js` | 用户反馈 |
| 举报 | `report.js` | 举报 |
| 行为 | `behavior.js` | 用户行为追踪 |
| 挑战 | `challenge.js` | 挑战赛 |
| 会员 | `membership.js` | 会员信息、订阅 |
| 商品 | `product.js` | 商城商品 |
| 医院 | `vet.js` | 宠物医院 |
| 喂食 | `feeding.js` | 喂食提醒 |
| 订阅 | `subscribe.js` | 微信订阅消息 |
| 成就 | `achievement.js` | 成就系统 |

### 6.3 状态管理

使用 **Pinia** 进行状态管理，共 4 个 Store：

| Store | 文件 | 管理内容 |
|-------|------|----------|
| 主 Store | `store/index.js` | 全局状态入口 |
| 用户 Store | `store/user.js` | 用户信息、Token、登录状态 |
| 宠物 Store | `store/pet.js` | 当前宠物、宠物列表 |
| TabBar Store | `store/tabbar.js` | TabBar 状态、角标 |

### 6.4 工具类

| 工具 | 文件 | 功能 |
|------|------|------|
| 请求封装 | `request.js` | Axios/uni.request 封装、Token 拦截、错误处理 |
| WebSocket | `websocket.js` | WS 连接管理、自动重连、消息分发 |
| 心跳 | `heartbeat.js` | WebSocket 心跳保活 |
| 主题 | `theme.js` | 主题切换、样式管理 |
| 格式化 | `format.js` | 日期、数字格式化 |
| 图片压缩 | `imageCompress.js` | 上传前图片压缩 |
| 事件总线 | `eventBus.js` | 组件间通信 |
| 通用 | `index.js` | 通用工具函数 |

### 6.5 组件库

| 组件 | 文件 | 功能 |
|------|------|------|
| 自定义 TabBar | `custom-tab-bar/index.vue` | 微信小程序自定义 TabBar |
| 添加宠物弹窗 | `AddPetModal.vue` | 快速添加宠物 |
| 头像展示 | `AvatarView.vue` | 用户/宠物头像 |
| 毛玻璃卡片 | `GlassCard.vue` | 毛玻璃风格卡片容器 |
| 图片裁剪 | `ImageCropper.vue` | 头像裁剪 |
| 用户顶栏 | `UserTopBar.vue` | 用户信息展示栏 |
| 视频卡片 | `VideoCard.vue` | 视频播放卡片 |
| 波浪头部 | `WaveHeader.vue` | 波浪形页面头部装饰 |

---

## 7. 管理后台架构详解

### 7.1 页面与权限

管理后台共 **19 个功能页面**，采用 RBAC 权限模型：

| 页面 | 路由 | 权限标识 |
|------|------|----------|
| 仪表盘 | `/dashboard` | dashboard |
| 用户管理 | `/users` | user:view |
| 宠物管理 | `/pets` | pet:view |
| 动态管理 | `/posts` | post:view |
| 评论管理 | `/comments` | comment:view |
| 举报管理 | `/reports` | report:view |
| 通知管理 | `/notifications` | notification:view |
| 反馈管理 | `/feedbacks` | feedback:view |
| 管理员管理 | `/admins` | admin:manage |
| 操作日志 | `/logs` | log:view |
| 系统设置 | `/settings` | setting:manage |
| 系统配置 | `/config` | config:manage |
| AI 模型管理 | `/ai-models` | ai-model:view |
| 挑战赛配置 | `/challenges` | challenge:view |
| 商城管理 | `/products` | product:view |
| 医院信息管理 | `/vet-clinics` | vet-clinic:view |
| 商户管理 | `/merchants` | merchant:manage |
| 角色管理 | `/roles` | admin:manage |
| 菜单管理 | `/sys-menus` | admin:manage |

**权限控制**：

- 路由守卫：`router.beforeEach` 检查 Token + 菜单权限 + 角色权限
- `SUPER_ADMIN` 拥有所有权限
- 普通管理员根据分配的菜单和权限按钮控制
- 菜单权限：后端返回菜单树，前端动态匹配路由
- 按钮权限：`@RequireButton` 注解 + 前端 `v-permission` 指令

### 7.2 API 模块

管理后台 API 共 **21 个模块**：

| 模块 | 文件 | 功能 |
|------|------|------|
| 认证 | `auth.js` | 管理员登录、TOTP 两步验证 |
| 用户管理 | `user.js` | 用户列表、封禁、解封 |
| 宠物管理 | `pet.js` | 宠物列表、详情 |
| 动态管理 | `post.js` | 动态审核、删除、恢复 |
| 评论管理 | `comment.js` | 评论审核、删除 |
| 举报管理 | `report.js` | 举报处理 |
| 通知管理 | `notification.js` | 通知发送、广播 |
| 反馈管理 | `feedback.js` | 反馈查看、回复 |
| 管理员 | `admins.js` | 管理员 CRUD |
| 角色管理 | `role.js` | 角色 CRUD、权限分配 |
| 菜单管理 | `menu.js` | 菜单 CRUD |
| 操作日志 | `log.js` | 日志查询 |
| 系统设置 | `setting.js` | 系统参数配置 |
| 系统配置 | `config.js` | KV 配置管理 |
| AI 模型 | `aiModel.js` | 模型切换、统计 |
| 挑战赛 | `challenge.js` | 挑战赛配置 |
| 商品管理 | `product.js` | 商品 CRUD |
| 医院管理 | `vetClinic.js` | 医院信息 CRUD |
| 商户管理 | `merchant.js` | 商户 CRUD |
| 数据导出 | `export.js` | Excel 导出 |
| 仪表盘 | `dashboard.js` | 统计数据 |

---

## 8. 数据库设计

### 8.1 表结构总览

共 **22+ 张表**，按模块划分：

| 模块 | 表名 | 说明 | 核心字段 |
|------|------|------|----------|
| **用户** | `users` | 用户表 | openid, unionid, nickname, avatar, phone, gender, status |
| **用户** | `follows` | 关注表 | follower_id, followee_id |
| **用户** | `user_achievements` | 用户成就 | user_id, achievement_id, status |
| **用户** | `user_behaviors` | 行为追踪 | user_id, action, target_type, target_id, duration |
| **用户** | `user_hidden_items` | 隐藏打卡项 | user_id, item_id |
| **用户** | `user_membership` | 会员 | user_id, plan, amount, start_date, expire_date, status |
| **宠物** | `pets` | 宠物表 | user_id, name, breed, gender, sterilized, category, birthday, weight, color |
| **宠物** | `pet_album` | 宠物相册 | pet_id, image_url, title, note, record_date |
| **宠物** | `weight_records` | 体重记录 | pet_id, weight, record_date |
| **健康** | `health_daily_stats` | 每日统计 | user_id, pet_id, stat_date, total_steps, total_water |
| **健康** | `step_records` | 步数记录 | user_id, pet_id, steps, distance, record_date |
| **健康** | `water_records` | 饮水记录 | user_id, pet_id, amount, record_date |
| **社交** | `posts` | 动态表 | user_id, pet_id, content, images, videos, like_count, comment_count, share_count, ee_count, audit_status |
| **社交** | `post_likes` | 点赞表 | post_id, user_id |
| **社交** | `post_comments` | 评论表 | post_id, user_id, content, parent_id |
| **社交** | `post_ee` | 收藏表 | post_id, user_id |
| **社交** | `follows` | 关注表 | follower_id, followee_id |
| **社交** | `tags` | 标签表 | name, usage_count |
| **社交** | `post_tags` | 动态标签 | post_id, tag_id |
| **打卡** | `checkin_items` | 打卡项 | user_id, name, icon, is_default, sort_order |
| **打卡** | `checkin_records` | 打卡记录 | user_id, item_id, record_date, status |
| **打卡** | `checkin_stats` | 打卡统计 | user_id, item_id, current_streak, max_streak |
| **打卡** | `checkin_reminders` | 打卡提醒 | user_id, item_id, reminder_time |
| **提醒** | `vaccine_reminders` | 疫苗提醒 | pet_id, vaccine_name, next_date, status |
| **提醒** | `parasite_reminders` | 驱虫提醒 | pet_id, drug_name, next_date, status |
| **成就** | `achievements` | 成就表 | name, description, icon, condition_type, condition_value |
| **管理** | `notifications` | 通知表 | user_id, type, content, target_id, is_read |
| **管理** | `reports` | 举报表 | reporter_id, target_type, target_id, reason, status |
| **管理** | `feedbacks` | 反馈表 | user_id, content, reply, status |
| **管理** | `admins` | 管理员表 | username, password_hash, role, totp_secret |
| **管理** | `admin_operation_logs` | 操作日志 | admin_id, module, action, detail, ip |
| **管理** | `system_settings` | 系统设置 | setting_key, setting_value |
| **管理** | `orders` | 订单表 | user_id, order_no, amount, status |
| **扩展** | `merchants` | 商户表 | name, contact, status |
| **扩展** | `products` | 商品表 | merchant_id, name, price, stock |
| **扩展** | `vet_clinics` | 医院表 | name, address, phone, rating |
| **扩展** | `vet_appointments` | 预约表 | user_id, clinic_id, pet_id, appointment_time |
| **扩展** | `ai_models` | AI 模型 | provider, model_name, display_name, api_key, base_url, is_active |
| **扩展** | `ai_model_stats` | 模型统计 | model_id, date, call_count, avg_latency_ms |
| **扩展** | `ai_model_switch_log` | 切换日志 | from_model_id, to_model_id, admin_id |
| **扩展** | `sys_configs` | 系统配置 | config_key, config_value, description |
| **扩展** | `sys_roles` | 系统角色 | role_name, role_code, description |
| **扩展** | `sys_menus` | 系统菜单 | parent_id, name, path, icon, permission, sort_order |
| **扩展** | `sys_role_menus` | 角色菜单 | role_id, menu_id |
| **扩展** | `feeding_reminders` | 喂食提醒 | pet_id, feeding_time, food_type, amount |
| **扩展** | `wx_subscribe_authorization` | 微信订阅 | user_id, template_id, authorized |

### 8.2 核心表关系

```
users (1) ──── (N) pets
users (1) ──── (N) posts
users (1) ──── (N) follows (follower_id / followee_id)
users (1) ──── (N) notifications
users (1) ──── (N) user_membership
users (1) ──── (N) user_behaviors
users (1) ──── (N) checkin_records

pets (1) ──── (N) weight_records
pets (1) ──── (N) vaccine_reminders
pets (1) ──── (N) parasite_reminders
pets (1) ──── (N) pet_album
pets (1) ──── (N) feeding_reminders

posts (1) ──── (N) post_likes
posts (1) ──── (N) post_comments
posts (1) ──── (N) post_ee
posts (1) ──── (N) post_tags
```

### 8.3 索引设计

- **唯一索引**：`users.openid`、`users.unionid`、`follows(follower_id, followee_id)`、`post_likes(post_id, user_id)`、`weight_records(pet_id, record_date)`
- **常规索引**：`pets.user_id`、`posts.user_id`、`notifications.user_id`、`user_behaviors(user_id, action)`
- **逻辑删除**：所有表支持 `deleted` 字段逻辑删除

---

## 9. 依赖关系图

### 后端模块依赖

```
┌──────────────────────────────────────────────────────────────┐
│                        Controller 层                         │
│  UserController  PetController  PostController  ...          │
└──────────┬──────────┬──────────┬─────────────────────────────┘
           ↓          ↓          ↓
┌──────────────────────────────────────────────────────────────┐
│                        Service 层                            │
│                                                              │
│  UserService ──→ FollowService ──→ RecommendService          │
│       │              │                   │                    │
│       ↓              ↓                   ↓                    │
│  PetService ──→ PostService ──→ ContentAuditService           │
│       │              │                   │                    │
│       ↓              ↓                   ↓                    │
│  HealthAnalysisService ──→ AiAnalysisService                  │
│       │                                                      │
│       ↓                                                      │
│  CheckinService ──→ AchievementService                       │
│       │                                                      │
│       ↓                                                      │
│  NotificationService ──→ WebSocketHandler                    │
│       │                                                      │
│       ↓                                                      │
│  UserMembershipService ──→ WechatPayService                  │
└──────────┬──────────┬──────────┬─────────────────────────────┘
           ↓          ↓          ↓
┌──────────────────────────────────────────────────────────────┐
│                        Mapper 层                             │
│  UserMapper  PetMapper  PostMapper  FollowMapper  ...        │
└──────────┬──────────┬──────────┬─────────────────────────────┘
           ↓          ↓          ↓
┌──────────────────────────────────────────────────────────────┐
│                     基础设施层                                │
│  MySQL  │  Redis  │  Caffeine  │  COS  │  RabbitMQ  │  WS   │
└──────────────────────────────────────────────────────────────┘
```

### 前端依赖关系

```
pages/ (页面)
  ├── api/ (接口调用)
  │     └── utils/request.js (请求封装)
  ├── store/ (状态管理)
  │     └── api/ (接口调用)
  ├── components/ (公共组件)
  └── utils/ (工具类)
        ├── request.js ←── api/
        ├── websocket.js ←── store/
        └── theme.js ←── components/
```

---

## 10. 项目运行与部署

### 10.1 环境要求

| 组件 | 最低版本 | 说明 |
|------|----------|------|
| Java | 21+ | 必须支持虚拟线程 |
| Maven | 3.6+ | 后端构建 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.0+ | 缓存 |
| Node.js | 18+ | 前端构建 |
| npm | 8.0+ | 包管理 |
| Docker | 20+ | 容器化部署 (可选) |

### 10.2 本地开发启动

```bash
# 1. 初始化数据库
mysql -u root -p < backend/src/main/resources/init.sql

# 2. 启动后端
cd backend
mvnw.cmd spring-boot:run

# 3. 启动前端 (小程序)
cd frontend
npm install
npm run dev:mp-weixin

# 4. 启动管理后台
cd admin
npm install
npm run dev
```

### 10.3 Docker Compose 部署

```bash
# 一键启动 (MySQL + Redis + RabbitMQ + Backend + Nginx)
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止
docker-compose down
```

**服务编排**：

| 服务 | 镜像 | 端口 | 依赖 |
|------|------|------|------|
| mysql | mysql:8.0 | 3306 | — |
| redis | redis:7-alpine | 6379 | — |
| rabbitmq | 自定义 Dockerfile | 5672/15672 | — |
| backend | 多阶段构建 | 8080 | mysql, redis |
| nginx | nginx:alpine | 80/443 | backend |

### 10.4 微信云托管部署

后端支持部署到微信云托管环境，通过 `callContainer` 方式供小程序调用：

```bash
# 构建镜像
docker build -t pet-trail-backend:latest .

# 推送到微信云托管控制台
```

**Docker 构建特点**：

- 多阶段构建：Maven 构建 → JRE 运行
- JVM 容器优化：`UseContainerSupport` + `MaxRAMPercentage=75.0`
- Alpine 基础镜像，体积小
- CA 证书更新，确保 HTTPS 请求正常

### 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 H5 | http://localhost:5173 | 开发模式 |
| 后端 API | http://localhost:8080/api | RESTful API |
| API 文档 | http://localhost:8080/swagger-ui/index.html | SpringDoc |
| 管理后台 | http://localhost:5174 | 管理端 |

---

## 11. 设计优缺点分析

### 11.1 设计优点

#### 1. 架构设计合理

- **前后端分离**：三端独立开发部署，职责清晰
- **多级缓存**：Caffeine + Redis + DB 三级缓存，有效降低数据库压力
- **双存储策略**：社交互动数据 Redis + DB 双写，兼顾性能与可靠性

#### 2. 安全体系完善

- JWT + Spring Security 认证授权
- Token 黑名单机制 (Redis)
- 自定义权限注解 (`@RequireRole`, `@RequireButton`)
- 操作日志 AOP 自动记录
- Sentinel 限流熔断
- 内容双重审核 (本地 + 微信 API)
- BCrypt 密码加密 + TOTP 两步验证 (管理端)

#### 3. 业务功能完整

- 覆盖宠物管理全生命周期 (健康、打卡、提醒、社交)
- 6 维推荐算法，具备一定智能化
- AI 健康分析 + 熔断降级，提升用户体验
- WebSocket 实时通知，交互体验好

#### 4. 运维友好

- Docker 多阶段构建，镜像精简
- Docker Compose 一键编排
- SpringDoc 自动 API 文档
- 全局异常统一处理
- 详细的错误码体系

#### 5. 代码规范

- 统一响应格式 (`Result<T>`)
- DTO/VO 分层，避免实体直接暴露
- Lombok 减少样板代码
- 逻辑删除保护数据

### 11.2 设计缺点与技术债务

#### 1. 性能问题

| 问题 | 位置 | 影响 |
|------|------|------|
| N+1 查询 | `PostController.convertToPostVOList` | 每个 post 单独查询用户和宠物信息，列表页性能差 | ✅ 已修复：改为 `selectBatchIds` 批量查询 + 批量查点赞/收藏状态 |
| 推荐算法全量加载 | `RecommendService.computeRecommendations` | 全量加载候选用户计算分数，用户量大时 OOM 风险 |  |
| 缓存 key 模糊删除 | `PostService.deletePost` 使用 `redisTemplate.keys()` | 生产环境 keys 命令阻塞 Redis | ✅ 已修复：创建 `RedisScanUtil` 工具类，统一使用 SCAN 替代 keys |

#### 2. 代码质量

| 问题 | 位置 | 影响 |
|------|------|------|
| Controller 臃肿 | `PostController` 超 500 行 | VO 转换逻辑应抽取到 Converter/Service |
| Service 返回 Map | `RecommendService`, `UserMembershipService` | 返回 `Map<String, Object>` 缺乏类型安全 | ✅ 已修复：创建专用 VO 类替换 |
| 测试覆盖不足 | 仅 5 个测试类，覆盖率 <15% | 重构和迭代风险高 |

#### 3. 架构问题

| 问题 | 说明 |
|------|------|
| 单体架构 | 所有功能耦合在一个 Spring Boot 应用中，随着功能增长将难以维护 |
| RabbitMQ 残留 | 配置已条件化但 pom.xml 仍保留依赖，增加包体积 |
| 缺少数据库迁移工具 | init.sql 混合 DDL 和 ALTER TABLE，无版本管理 |
| 敏感信息硬编码 | application.yml 默认值暴露生产凭据 |
| 缺少接口限流统一管理 | Sentinel 规则硬编码，缺少动态配置 |

#### 4. 前端问题

| 问题 | 说明 |
|------|------|
| Uni-app alpha 版本 | 使用 3.0 alpha 版本，稳定性风险 |
| 缺少 TypeScript | 前端代码全量 JavaScript，缺乏类型检查 |
| 组件复用不足 | 部分页面存在重复逻辑 |

#### 5. 数据库问题

| 问题 | 说明 |
|------|------|
| 缺少外键约束 | 表间关系仅靠应用层维护，数据一致性风险 |
| 缺少分表策略 | posts、user_behaviors 等大表无分表方案 |
| init.sql 幂等性差 | DROP TABLE + CREATE TABLE，升级时数据丢失风险 |

---

## 12. 改进建议

### 短期 (1-3 个月)

1. ~~**修复 N+1 查询**~~：✅ 已完成 — PostVO 转换改为批量查询，使用 IN 查询替代循环单查
2. ~~**替换 keys 命令**~~：✅ 已完成 — 创建 RedisScanUtil 工具类，使用 SCAN 游标迭代替代 `redisTemplate.keys()`
3. **引入 Flyway**：数据库版本化管理，支持增量迁移
4. **增加单元测试**：核心 Service 层测试覆盖率达到 60%+
5. **敏感信息外部化**：所有凭据通过环境变量注入，移除默认值
6. ~~**Service 返回类型化**~~：✅ 已完成 — 创建 8 个专用 VO 类替换 `Map<String, Object>`

### 中期 (3-6 个月)

1. ~~**模块化拆分**~~：❌ 废弃 — 当前项目规模（47 个 Service）远未到需要拆分的程度，单体应用简单高效易维护，过早拆分只会增加复杂度
2. ~~**前端 TypeScript 化**~~：❌ 废弃 — 项目已有 TS 基础设施，当前 JS 代码量可控，暂不需要迁移
3. ~~**推荐算法优化**~~：✅ 已完成 — 引入 Redis Vector Search (RediSearch) 向量检索替代全量计算，两阶段推荐架构（向量召回 Top-K ANN + 规则精排 6 维评分），支持熔断降级
4. ~~**统一限流管理**~~：❌ 废弃 — 当前无限流需求，后续可用 Redis 令牌桶方案更轻量，无需额外部署 Nacos
5. **接口版本管理**：API 路径增加版本号 (`/api/v1/`, `/api/v2/`)
6. ~~**日志体系完善**~~：❌ 废弃 — 当前单机部署 docker logs 够用，后续用户量上来后直接上 Loki（比 ELK 轻量）

### 长期 (6-12 个月)

1. **微服务化**：按业务域拆分服务 (用户服务、社区服务、健康服务等)
2. ~~**引入消息总线**~~：❌ 废弃 — 当前仅1个 Spring Event + 2个 RabbitMQ 消息类型，两套机制分工合理（进程内实时 vs 分布式持久化），统一抽象属于过度设计
3. **数据库读写分离**：MySQL 主从 + ShardingSphere
4. ~~**CDN 加速**~~：⏸️ 暂缓 — 腾讯云 COS 已有地域加速，当前用户量直连速度够用；CDN 开通免费+每月10GB免费额度，等用户量增长后实施（改动极小，仅 CosService 域名拼接改1处）
5. ~~**可观测性**~~：❌ 废弃 — 单体应用不需要全链路追踪，日志加 traceId + Actuator 即可；微服务化后再考虑，或直接用腾讯云 CLS + 云监控
6. ~~**自动化运维**~~：❌ 废弃 — 单机 Docker Compose 够用，K8s 对单机是过度设计；CI/CD 后续需要时可单独做，当前手动部署频率低

---

## 13. 后续盈利模式分析

### 13.1 当前盈利基础

项目已具备会员系统框架 (月度 ¥9.9 / 年度 ¥99)，但支付逻辑尚未完全打通。

### 13.2 盈利模式规划

#### 模式一：会员订阅 (SaaS) — 核心收入

| 层级 | 价格 | 权益 |
|------|------|------|
| 免费版 | ¥0 | 基础打卡、3个宠物、5张相册、基础健康记录 |
| Pro 月度 | ¥9.9/月 | 无限相册、高级统计、AI 分析、数据导出 |
| Pro 年度 | ¥99/年 | 同月度 + 优先客服、专属标签 |

**预估**：若 1 万 DAU 中 5% 转化，月收入约 ¥4,950

#### 模式二：增值服务 — 单次付费

| 服务 | 定价 | 说明 |
|------|------|------|
| AI 深度健康报告 | ¥2.9/次 | 详细的 AI 健康分析报告 (含饮食/运动建议) |
| 宠物基因解读 | ¥19.9/次 | 上传照片 AI 识别品种+性格+健康风险 |
| 健康数据 PDF 导出 | ¥1.9/次 | 就医时提供给兽医参考 |
| 个性化主题 | ¥3-6/个 | 限定主题皮肤购买 |

#### 模式三：电商佣金 — 平台抽成

| 方向 | 模式 | 预估佣金率 |
|------|------|------------|
| 宠物用品商城 | 自营/入驻商家 | 10-20% |
| 宠物医院预约 | 在线预约导流 | 5-15% |
| 宠物保险 | 保险产品分销 | 15-30% |
| 宠物食品订阅 | 定期配送服务 | 10-15% |

#### 模式四：广告收入 — 流量变现

| 形式 | 说明 | 预估 CPM |
|------|------|----------|
| 信息流广告 | 动态 Feed 中原生广告 | ¥30-80 |
| 品牌合作 | 宠物品牌定制内容 | 按项目定价 |
| 开屏广告 | 小程序开屏展示 | ¥50-100 |

#### 模式五：数据服务 — B 端变现

| 服务 | 目标客户 | 定价 |
|------|----------|------|
| 宠物行业趋势报告 | 宠物品牌、投资机构 | ¥5,000-20,000/份 |
| 用户画像数据 (脱敏) | 市场调研公司 | 按需定价 |
| 宠物健康指数 | 宠物医院、保险公司 | API 调用计费 |

#### 模式六：社区经济 — 生态建设

| 功能 | 模式 | 盈利方式 |
|------|------|----------|
| 宠物达人认证 | 内容创作者 | 平台抽成 |
| 付费问答 | 宠物专家咨询 | 佣金 20% |
| 虚拟礼物 | 直播/动态打赏 | 30% 抽成 |
| 宠物日历/周边 | UGC 内容变现 | 销售分成 |

### 13.3 盈利路线图

```
Phase 1 (0-3月): 会员支付打通 + 增值服务上线
    ↓ 预期月收入: ¥5,000-10,000
Phase 2 (3-6月): 电商商城 + 医院预约
    ↓ 预期月收入: ¥20,000-50,000
Phase 3 (6-12月): 广告系统 + 数据服务
    ↓ 预期月收入: ¥50,000-100,000
Phase 4 (12月+): 社区经济 + 生态建设
    ↓ 预期月收入: ¥100,000+
```

### 13.4 关键指标

| 指标 | 当前 | 目标 (6个月) | 目标 (12个月) |
|------|------|-------------|---------------|
| DAU | — | 10,000 | 50,000 |
| 付费转化率 | — | 5% | 8% |
| ARPU | — | ¥1.5 | ¥3.0 |
| 月收入 | — | ¥50,000 | ¥200,000 |
| 留存率 (次日) | — | 40% | 50% |

---

> **文档生成日期**：2026-05-24 | **基于代码版本**：v2.1.0 | **分析工具**：SOLO AI
