# 宠迹微信小程序 - 功能分析与扩展设计文档

> 更新日期：2026-04-18
> 版本：v2.0

---

## 一、现有功能概览

### 1.1 功能模块地图

```
宠迹小程序
├── 🔐 认证模块
│   ├── 微信一键登录
│   └── 开发模式登录
│
├── 🐾 宠物管理
│   ├── 宠物列表
│   ├── 添加/编辑宠物
│   ├── 宠物详情
│   └── 删除宠物
│
├── 📱 社交模块
│   ├── 动态流（全部/收藏/关注/推荐）
│   ├── 发布动态（文字/图片/视频/贴纸/气泡/位置）
│   ├── 动态详情
│   ├── 点赞/收藏/分享
│   ├── 评论与回复
│   ├── 用户主页
│   ├── 关注/粉丝
│   ├── 发现用户（推荐/热门/新人）
│   └── 举报
│
├── 💚 健康管理
│   ├── 健康仪表盘（体重趋势/提醒概览）
│   ├── 体重记录
│   ├── 疫苗提醒
│   ├── 驱虫提醒
│   ├── 步数记录
│   └── 饮水记录
│
├── ✅ 每日打卡
│   ├── 打卡项目
│   ├── 执行打卡
│   ├── 打卡日历
│   └── 打卡统计
│
├── 🔔 通知系统
│   ├── 通知列表
│   ├── 未读计数
│   ├── 标记已读/全部已读
│   └── 删除/清空通知
│
└── 👤 个人中心
    ├── 个人资料
    ├── 编辑资料
    └── 宠物管理入口
```

### 1.2 页面清单

| 页面 | 路径 | 状态 | 说明 |
|------|------|------|------|
| 首页 | pages/home/index | ✅ | 动态流，4 个 Tab |
| 健康仪表盘 | pages/dashboard/index | ✅ | 体重趋势+提醒 |
| 打卡 | pages/checkin/index | ✅ | 每日打卡 |
| 个人中心 | pages/me/index | ⚠️ | 多个入口未实现 |
| 添加健康记录 | pages/health/index | ⚠️ | 健康评分硬编码 |
| 宠物列表 | pages/pets/index | ✅ | |
| 宠物详情 | pages/pets/detail | ✅ | |
| 发布动态 | pages/publish/index | ✅ | |
| 动态详情 | pages/post/detail | ✅ | |
| 用户主页 | pages/user/detail | ✅ | |
| 编辑资料 | pages/user/edit | ✅ | |
| 通知 | pages/notification/index | ✅ | |
| 发现用户 | pages/discover/index | ✅ | |
| 关注/粉丝 | pages/follows/index | ✅ | |
| 体重记录 | pages/health/record | ❌ | 未注册路由 |
| 疫苗提醒 | pages/health/vaccine | ❌ | 未注册路由 |
| 驱虫提醒 | pages/health/parasite | ❌ | 未注册路由 |

### 1.3 后端 API 清单

| 模块 | 接口数 | 状态 |
|------|--------|------|
| 用户认证 | 6 | ✅ |
| 宠物管理 | 6 | ✅ |
| 动态/社交 | 14 | ✅ |
| 关注 | 5 | ✅ |
| 打卡 | 5 | ⚠️ stats 未实现 |
| 健康记录 | 3 | ✅ |
| 体重记录 | 6 | ✅ |
| 通知 | 6 | ✅ |
| 疫苗提醒 | 7 | ✅ |
| 驱虫提醒 | 7 | ✅ |
| 仪表盘 | 4 | ✅ |
| 文件上传 | 3 | ✅ |
| 举报 | 1 | ✅ |

---

## 二、优点分析

### 2.1 架构设计

| 优点 | 说明 |
|------|------|
| **双模式请求** | 支持 HTTP 和微信云托管两种请求方式，灵活适配部署环境 |
| **Redis 缓存体系** | 用户资料、宠物信息、动态详情、关注状态、未读通知等均有缓存，TTL 分级合理 |
| **推荐算法** | 基于品种、活跃度、社交关系、互动、新鲜度的加权评分推荐，超越简单排序 |
| **内容安全** | 双重审核（本地敏感词 + 微信 API），审核失败降级放行，不阻塞业务 |
| **通知去重** | 60 秒窗口去重，避免刷屏 |
| **Spring Event** | 动态创建使用事件驱动，解耦业务逻辑 |
| **软删除** | 动态、评论使用软删除，数据可恢复 |
| **统一响应格式** | Result<T> 包装，前后端交互规范 |

### 2.2 用户体验

| 优点 | 说明 |
|------|------|
| **自定义 TabBar** | 毛玻璃效果，视觉精致 |
| **设计系统** | 全局 CSS 变量，统一的暖色调风格 |
| **图片压缩** | 上传前自动压缩，节省流量和存储 |
| **Base64 上传回退** | 云托管 callContainer 场景下的兼容方案 |
| **视频自动播放** | 可见视频自动播放，提升沉浸感 |
| **打卡动画+振动** | 打卡成功有动画和触觉反馈 |
| **下拉刷新+上拉加载** | 列表页均有分页和刷新支持 |

### 2.3 业务完整性

| 优点 | 说明 |
|------|------|
| **宠物社交闭环** | 养宠 → 记录 → 分享 → 互动 → 发现同好 |
| **健康管理完整** | 体重、疫苗、驱虫、步数、饮水多维记录 |
| **打卡体系** | 连续打卡统计、日历视图、成就激励 |

---

## 三、缺点与问题分析

### 3.1 🔴 严重问题（影响功能可用性）

| 编号 | 问题 | 位置 | 影响 |
|------|------|------|------|
| S1 | **Store 中 API 方法名不匹配** | store/pet.js 调用 `petApi.getList()` / `petApi.add()`，但实际导出为 `getPetList` / `createPet` | 宠物 Store 操作运行时必报错 |
| S2 | **3 个健康子页面未注册路由** | pages.json 缺少 health/vaccine、health/parasite、health/record | 疫苗/驱虫/体重记录页面无法访问 |
| S3 | **请求层双重实现** | main.js 和 utils/request.js 都实现了 cloudRequest/httpRequest | 维护成本翻倍，行为可能不一致 |
| S4 | **heartbeat.js 完全损坏** | 导入不存在的 `@openclaw/utils`，重复 const 声明 | 文件不可用，如果被引用会导致崩溃 |
| S5 | **打卡统计接口未实现** | CheckinController.getStats() 返回硬编码空数据 | 前端打卡统计展示为 0 |
| S6 | **旧版提醒查询接口未实现** | ReminderController 的 GET /vaccine 和 GET /parasite 返回 null | 如果前端调用旧接口则无数据 |

### 3.2 🟡 中等问题（影响体验或效率）

| 编号 | 问题 | 位置 | 影响 |
|------|------|------|------|
| M1 | **DashboardController 认证方式不一致** | 使用 @RequestHeader 手动解析 token，其他控制器用 UserContext | 维护混乱，可能绕过安全校验 |
| M2 | **大量使用 Map 手动解析请求体** | PostController、CheckinController 等 | 缺少类型安全，参数校验不充分 |
| M3 | **关注列表内存分页** | FollowController 的 followees/followers 先查全部 ID 再切片 | 用户量大时内存溢出风险 |
| M4 | **健康评分硬编码** | health/index.vue 中 petHealthScore = 86 | 数据无意义，误导用户 |
| M5 | **绝育状态始终返回 "-"** | health/index.vue 中 petSpayedText 逻辑错误 | 宠物绝育信息无法正确展示 |
| M6 | **首页发现入口提示"设计中"** | home/index.vue goToDiscover() 显示 toast | 但 discover 页面已存在，应直接跳转 |
| M7 | **打卡页通知铃铛提示"未实现"** | checkin/index.vue | 通知功能已存在，应接入 |
| M8 | **个人中心多个入口未实现** | me/index.vue：关于我们、意见反馈、设置、喂食提醒 | 功能缺失 |
| M9 | **logout 仅清本地** | api/auth.js 的 logout 不调用服务端 | 服务端 token 仍有效，存在安全隐患 |
| M10 | **Redis 防重代码被注释** | CheckinService 第 53-63 行 | 打卡防重仅依赖 DB 查询，高并发下可能重复打卡 |
| M11 | **过期提醒清理未实现** | ReminderService.cleanExpiredReminders() 仅打印日志 | 过期提醒堆积，影响数据准确性 |

### 3.3 🟢 轻微问题（代码质量/可维护性）

| 编号 | 问题 | 位置 | 影响 |
|------|------|------|------|
| L1 | **Vue 2/3 写法混用** | 部分页面 Options API，部分 Composition API | 代码风格不统一 |
| L2 | **PostVO 转换逻辑过重** | PostController.convertToPostVO() 批量预加载+JSON解析 | 性能瓶颈风险 |
| L3 | **无全局错误边界** | 前端缺少统一的错误处理组件 | 异常可能导致白屏 |
| L4 | **无骨架屏/Loading 统一方案** | 各页面自行实现加载状态 | 体验不一致 |
| L5 | **API 路径不统一** | auth.js 用 `/auth/xxx`，post.js 用 `/posts/xxx` | 与后端实际路径 `/api/xxx` 不一致，依赖请求层前缀拼接 |

---

## 四、扩展功能设计

### 模块总览

```
宠迹小程序 v2.0 扩展规划
├── A. 基础修复（必须）
├── B. 宠物健康管理增强
├── C. 社交体验增强
├── D. 内容与社区增强
├── E. 用户体系增强
├── F. 智能化功能
└── G. 商业化基础
```

---

### A. 基础修复（必须优先）

#### A.1 修复 Store API 方法名

```
store/pet.js:
  petApi.getList()  → petApi.getPetList()
  petApi.add()      → petApi.createPet()
```

#### A.2 注册健康子页面路由

pages.json 添加：
```json
{
  "path": "pages/health/vaccine",
  "style": { "navigationBarTitleText": "疫苗提醒" }
},
{
  "path": "pages/health/parasite",
  "style": { "navigationBarTitleText": "驱虫提醒" }
},
{
  "path": "pages/health/record",
  "style": { "navigationBarTitleText": "体重记录" }
}
```

#### A.3 统一请求层

删除 main.js 中的重复请求实现，统一使用 utils/request.js。

#### A.4 修复/删除 heartbeat.js

删除损坏的 heartbeat.js，或重新实现。

#### A.5 实现打卡统计接口

```
GET /api/checkin/stats?petId=xxx

返回：
{
  "totalCount": 30,
  "currentStreak": 5,
  "maxStreak": 12,
  "todayChecked": true,
  "itemStats": [
    { "itemId": 1, "itemName": "喂食", "totalCount": 20, "currentStreak": 3 }
  ]
}
```

#### A.6 修复首页发现入口

```javascript
// home/index.vue
goToDiscover() {
  uni.navigateTo({ url: '/pages/discover/index' })
}
```

#### A.7 修复打卡页通知入口

```javascript
// checkin/index.vue
goToNotifications() {
  uni.navigateTo({ url: '/pages/notification/index' })
}
```

#### A.8 统一 DashboardController 认证方式

改用 UserContext.getCurrentUserId()，与其他控制器保持一致。

---

### B. 宠物健康管理增强

#### B.1 健康评分系统

**设计**：基于多维度数据自动计算宠物健康评分（0-100 分）

| 维度 | 权重 | 数据来源 |
|------|------|----------|
| 体重健康 | 30% | 体重是否在品种标准范围内，变化趋势 |
| 疫苗接种 | 25% | 是否按时接种，有无逾期 |
| 驱虫记录 | 15% | 是否定期驱虫 |
| 打卡活跃度 | 15% | 近 30 天打卡频率 |
| 运动量 | 15% | 步数记录是否达标 |

**后端接口**：
```
GET /api/pets/{id}/health-score

返回：
{
  "score": 86,
  "level": "良好",
  "items": [
    { "name": "体重健康", "score": 90, "weight": 0.30, "suggestion": "体重正常，继续保持" },
    { "name": "疫苗接种", "score": 80, "weight": 0.25, "suggestion": "有1项疫苗即将到期" },
    ...
  ]
}
```

#### B.2 健康报告

**设计**：生成周/月度健康报告，可分享

| 内容 | 说明 |
|------|------|
| 体重变化曲线 | 折线图 + 变化率 |
| 运动数据 | 步数统计 + 趋势 |
| 打卡完成率 | 环形图 |
| 提醒完成情况 | 疫苗/驱虫完成率 |
| 健康建议 | AI 生成或规则引擎 |

**后端接口**：
```
GET /api/health/report?petId=xxx&period=weekly|monthly

返回：
{
  "period": "2026-04-11 ~ 2026-04-18",
  "weightChange": { "start": 4.5, "end": 4.6, "trend": "up" },
  "avgSteps": 3200,
  "checkinRate": 0.85,
  "vaccineStatus": "up_to_date",
  "suggestions": ["体重略有上升，注意控制饮食"]
}
```

#### B.3 用药记录

**新增模块**：记录宠物用药情况

```sql
CREATE TABLE IF NOT EXISTS `medication_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pet_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL COMMENT '药品名称',
  `dosage` varchar(50) DEFAULT NULL COMMENT '剂量',
  `frequency` varchar(50) DEFAULT NULL COMMENT '频率',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `note` varchar(500) DEFAULT NULL,
  `status` tinyint(4) DEFAULT 1 COMMENT '1-进行中 2-已完成 3-已停用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用药记录表';
```

**后端接口**：
```
GET    /api/pets/{petId}/medications      用药记录列表
POST   /api/pets/{petId}/medications      添加用药记录
PUT    /api/pets/{petId}/medications/{id} 更新用药记录
DELETE /api/pets/{petId}/medications/{id} 删除用药记录
```

#### B.4 就医记录

**新增模块**：记录宠物就医情况

```sql
CREATE TABLE IF NOT EXISTS `vet_visits` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pet_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `visit_date` date NOT NULL COMMENT '就诊日期',
  `hospital` varchar(100) DEFAULT NULL COMMENT '医院名称',
  `doctor` varchar(50) DEFAULT NULL COMMENT '医生',
  `symptoms` varchar(500) DEFAULT NULL COMMENT '症状',
  `diagnosis` varchar(500) DEFAULT NULL COMMENT '诊断',
  `treatment` varchar(500) DEFAULT NULL COMMENT '治疗方案',
  `cost` decimal(10,2) DEFAULT NULL COMMENT '费用',
  `images` json DEFAULT NULL COMMENT '检查报告图片',
  `note` varchar(500) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_visit_date` (`visit_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就医记录表';
```

---

### C. 社交体验增强

#### C.1 私信系统

**设计**：用户间一对一私信

```sql
CREATE TABLE IF NOT EXISTS `conversations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user1_id` bigint(20) NOT NULL,
  `user2_id` bigint(20) NOT NULL,
  `last_message` varchar(500) DEFAULT NULL,
  `last_message_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users` (LEAST(`user1_id`, `user2_id`), GREATEST(`user1_id`, `user2_id`)),
  KEY `idx_user1` (`user1_id`),
  KEY `idx_user2` (`user2_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

CREATE TABLE IF NOT EXISTS `messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint(20) NOT NULL,
  `sender_id` bigint(20) NOT NULL,
  `content` varchar(1000) NOT NULL,
  `type` varchar(20) DEFAULT 'text' COMMENT 'text/image/system',
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_conversation` (`conversation_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';
```

**后端接口**：
```
GET    /api/messages/conversations         会话列表
POST   /api/messages/conversations         创建会话
GET    /api/messages/conversations/{id}    消息记录（分页）
POST   /api/messages/conversations/{id}    发送消息
PUT    /api/messages/conversations/{id}/read  标记已读
DELETE /api/messages/conversations/{id}    删除会话
```

**前端页面**：
- 会话列表页（显示最近联系人、未读数）
- 聊天页（支持文字、图片消息）

#### C.2 话题/标签系统

**设计**：动态可关联话题标签，用户可关注话题

```sql
CREATE TABLE IF NOT EXISTS `topics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '话题名称',
  `icon` varchar(255) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `post_count` int(11) DEFAULT 0,
  `follower_count` int(11) DEFAULT 0,
  `is_hot` tinyint(1) DEFAULT 0,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题表';

CREATE TABLE IF NOT EXISTS `post_topics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL,
  `topic_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_topic` (`post_id`, `topic_id`),
  KEY `idx_topic` (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态话题关联表';

CREATE TABLE IF NOT EXISTS `topic_followers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `topic_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_topic_user` (`topic_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题关注表';
```

**后端接口**：
```
GET  /api/topics/hot                热门话题
GET  /api/topics/{id}/posts         话题下的动态
POST /api/topics/{id}/follow        关注话题
POST /api/topics/{id}/unfollow      取消关注
```

**前端**：
- 发布动态时可选话题标签
- 首页增加话题入口
- 话题详情页（话题信息 + 动态列表）

#### C.3 动态互动增强

| 功能 | 说明 |
|------|------|
| @提及 | 动态/评论中 @其他用户，被提及者收到通知 |
| 表情回复 | 评论支持表情包 |
| 动态转发 | 转发他人动态到自己的动态流 |
| 评论点赞 | 评论支持点赞 |

**后端接口**：
```
POST /api/posts/{id}/comments/{commentId}/like   评论点赞
POST /api/posts/{id}/repost                       转发动态
GET  /api/search?keyword=xxx&type=post|user|topic 全局搜索
```

#### C.4 全局搜索

**设计**：统一搜索入口，支持搜索动态、用户、话题

```
GET /api/search?keyword=xxx&type=all|post|user|topic&page=1&size=10

返回：
{
  "posts": [...],
  "users": [...],
  "topics": [...],
  "total": 100
}
```

---

### D. 内容与社区增强

#### D.1 宠物品种百科

**设计**：内置品种数据库，提供养护指南

| 内容 | 说明 |
|------|------|
| 品种信息 | 名称、图片、体型、寿命、性格特点 |
| 养护指南 | 饮食建议、运动需求、常见疾病 |
| 美容护理 | 毛发护理、洗澡频率 |
| 训练建议 | 基础训练、社会化训练 |

```sql
CREATE TABLE IF NOT EXISTS `breed_encyclopedia` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '品种名称',
  `category` tinyint(4) NOT NULL COMMENT '类别: 1-猫 2-狗 3-其他',
  `size` varchar(20) DEFAULT NULL COMMENT '体型: 小型/中型/大型',
  `lifespan` varchar(20) DEFAULT NULL COMMENT '寿命范围',
  `temperament` varchar(200) DEFAULT NULL COMMENT '性格特点',
  `care_guide` text DEFAULT NULL COMMENT '养护指南(JSON)',
  `common_diseases` varchar(500) DEFAULT NULL COMMENT '常见疾病',
  `image` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品种百科表';
```

#### D.2 宠物社区/圈子

**设计**：按品种或兴趣创建圈子，用户可加入

```sql
CREATE TABLE IF NOT EXISTS `circles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '圈子名称',
  `icon` varchar(255) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `category` varchar(30) DEFAULT NULL COMMENT '分类: breed/activity/region',
  `owner_id` bigint(20) NOT NULL,
  `member_count` int(11) DEFAULT 0,
  `post_count` int(11) DEFAULT 0,
  `status` tinyint(4) DEFAULT 1,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子表';
```

#### D.3 内容合集

**设计**：用户可创建内容合集，将动态归类

```
POST /api/collections              创建合集
POST /api/collections/{id}/posts   添加动态到合集
GET  /api/collections/{id}         合集详情
```

---

### E. 用户体系增强

#### E.1 用户等级与成长值

**设计**：用户通过活跃行为获取成长值，升级解锁权益

| 等级 | 所需成长值 | 权益 |
|------|-----------|------|
| Lv.1 萌新 | 0 | 基础功能 |
| Lv.2 爱宠达人 | 100 | 自定义头像框 |
| Lv.3 养宠专家 | 500 | 专属标签 |
| Lv.4 社区之星 | 2000 | 优先推荐 |
| Lv.5 宠迹大使 | 5000 | 专属徽章 + 官方活动优先 |

**成长值来源**：

| 行为 | 成长值 | 频率限制 |
|------|--------|----------|
| 每日登录 | +5 | 每日1次 |
| 发布动态 | +10 | 每日3次 |
| 评论 | +5 | 每日5次 |
| 打卡 | +3 | 每日1次 |
| 被点赞 | +2 | 无限制 |
| 被关注 | +5 | 无限制 |

```sql
ALTER TABLE `users` ADD COLUMN `level` int DEFAULT 1 COMMENT '用户等级';
ALTER TABLE `users` ADD COLUMN `exp` int DEFAULT 0 COMMENT '成长值';
```

#### E.2 成就系统完善

**设计**：现有成就表已有基础，需完善触发和展示

| 成就 | 条件 | 奖励 |
|------|------|------|
| 初来乍到 | 注册成功 | 10 成长值 |
| 宠物主人 | 添加第一只宠物 | 20 成长值 |
| 社交达人 | 获得 10 个关注 | 50 成长值 |
| 打卡王者 | 连续打卡 30 天 | 100 成长值 |
| 健康卫士 | 完成所有疫苗接种 | 80 成长值 |
| 内容创作者 | 发布 50 条动态 | 100 成长值 |

**后端接口**：
```
GET /api/achievements           成就列表（含完成状态）
POST /api/achievements/{id}/claim  领取成就奖励
```

#### E.3 个人主页增强

| 功能 | 说明 |
|------|------|
| 个人简介 | 自我介绍文本 |
| 宠物展示 | 主页展示宠物卡片 |
| 成就墙 | 展示已获得的成就 |
| 动态标签页 | 动态 / 喜欢 / 收藏 |
| 等级标识 | 头像框 + 等级标签 |

#### E.4 账号安全

| 功能 | 说明 |
|------|------|
| 手机号绑定 | 微信手机号快速验证 |
| 注销确认 | 注销前二次确认 + 冷静期 |
| 隐私设置 | 谁可以看我的动态/宠物/关注列表 |

---

### F. 智能化功能

#### F.1 AI 宠物识别

**设计**：上传宠物照片自动识别品种

```
POST /api/ai/identify-pet
Body: { image: "base64..." }
返回：{ breed: "英短蓝猫", confidence: 0.92, suggestions: [...] }
```

可接入百度 AI / 腾讯 AI 的动物识别 API。

#### F.2 智能喂养建议

**设计**：根据宠物品种、年龄、体重、活动量，自动生成喂养建议

```
GET /api/pets/{id}/feeding-advice

返回：
{
  "dailyCalories": 350,
  "feedingSchedule": [
    { "time": "08:00", "amount": "80g", "type": "干粮" },
    { "time": "18:00", "amount": "60g", "type": "湿粮" }
  ],
  "waterIntake": "250ml/天",
  "tips": ["建议定时定量喂食", "避免喂食巧克力、葡萄等"]
}
```

#### F.3 智能提醒

| 提醒类型 | 触发条件 |
|----------|----------|
| 体重异常 | 体重短期波动超过 10% |
| 疫苗到期 | 疫苗到期前 7/3/1 天 |
| 驱虫到期 | 驱虫到期前 3 天 |
| 生日提醒 | 宠物生日当天 |
| 久坐提醒 | 步数连续 3 天低于目标 |
| 喂食提醒 | 用户自定义时间 |

**微信订阅消息**：利用微信小程序订阅消息能力推送提醒

```
POST /api/subscribe-message/send
Body: { templateId: "xxx", toUser: "openid", data: {...} }
```

#### F.4 内容推荐优化

| 优化方向 | 说明 |
|----------|------|
| 协同过滤 | 基于相似用户的偏好推荐 |
| 内容理解 | 分析动态图片内容（宠物类型、场景） |
| 实时热度 | 基于近期互动数据调整推荐权重 |
| 个性化 Tab | 根据用户行为自动调整首页 Tab 顺序 |

---

### G. 商业化基础

#### G.1 宠物服务预约

**设计**：接入周边宠物服务

| 服务类型 | 说明 |
|----------|------|
| 宠物医院 | 预约挂号、在线问诊 |
| 宠物美容 | 预约洗澡/美容 |
| 宠物寄养 | 预约寄养 |
| 宠物训练 | 预约训练课程 |

#### G.2 宠物商城

**设计**：推荐宠物用品

| 功能 | 说明 |
|------|------|
| 商品推荐 | 基于宠物品种/年龄推荐 |
| 好物分享 | 用户分享宠物用品使用体验 |
| 优惠券 | 新用户/活动优惠券 |

#### G.3 广告位

| 位置 | 形式 |
|------|------|
| 首页信息流 | 原生广告卡片 |
| 发现页 Banner | 轮播广告 |
| 开屏广告 | 启动页展示 |

---

## 五、开发优先级

### P0 - 紧急修复（1-2 天）

| 编号 | 任务 | 工作量 |
|------|------|--------|
| A.1 | 修复 Store API 方法名 | 0.5h |
| A.2 | 注册健康子页面路由 | 0.5h |
| A.3 | 统一请求层 | 2h |
| A.4 | 修复/删除 heartbeat.js | 0.5h |
| A.6 | 修复首页发现入口 | 0.5h |
| A.7 | 修复打卡页通知入口 | 0.5h |
| A.8 | 统一 DashboardController 认证 | 1h |

### P1 - 核心补全（1-2 周）

| 编号 | 任务 | 工作量 |
|------|------|--------|
| A.5 | 实现打卡统计接口 | 4h |
| B.1 | 健康评分系统 | 1天 |
| E.3 | 个人主页增强 | 1天 |
| C.4 | 全局搜索 | 1天 |
| C.3 | 评论点赞 + @提及 | 1天 |
| E.4 | 账号安全（隐私设置） | 1天 |
| M4/M5 | 修复健康评分/绝育状态 | 2h |

### P2 - 体验提升（2-4 周）

| 编号 | 任务 | 工作量 |
|------|------|--------|
| C.1 | 私信系统 | 3天 |
| C.2 | 话题/标签系统 | 2天 |
| B.2 | 健康报告 | 2天 |
| F.3 | 智能提醒（微信订阅消息） | 2天 |
| E.1 | 用户等级与成长值 | 2天 |
| E.2 | 成就系统完善 | 1天 |

### P3 - 差异化功能（1-2 月）

| 编号 | 任务 | 工作量 |
|------|------|--------|
| F.1 | AI 宠物识别 | 3天 |
| F.2 | 智能喂养建议 | 2天 |
| D.1 | 宠物品种百科 | 3天 |
| D.2 | 宠物社区/圈子 | 5天 |
| B.3 | 用药记录 | 1天 |
| B.4 | 就医记录 | 1天 |

### P4 - 商业化（3+ 月）

| 编号 | 任务 | 工作量 |
|------|------|--------|
| G.1 | 宠物服务预约 | 2周 |
| G.2 | 宠物商城 | 2周 |
| G.3 | 广告位 | 1周 |

---

## 六、数据库变更汇总

### 新增表

| 表名 | 模块 | 优先级 |
|------|------|--------|
| conversations | 私信系统 | P2 |
| messages | 私信系统 | P2 |
| topics | 话题系统 | P2 |
| post_topics | 话题系统 | P2 |
| topic_followers | 话题系统 | P2 |
| breed_encyclopedia | 品种百科 | P3 |
| circles | 宠物圈子 | P3 |
| medication_records | 用药记录 | P3 |
| vet_visits | 就医记录 | P3 |

### 修改表

| 表名 | 变更 | 优先级 |
|------|------|--------|
| users | 新增 level, exp 字段 | P2 |
| posts | 新增 topic_ids 字段 | P2 |
| post_comments | 新增 like_count 字段 | P1 |

---

## 七、技术选型建议

| 需求 | 方案 | 说明 |
|------|------|------|
| 私信实时通信 | WebSocket / 微信云托管实时推送 | 低延迟消息推送 |
| 全局搜索 | MySQL FULLTEXT / Elasticsearch | 初期 MySQL 足够，量大迁移 ES |
| AI 识别 | 腾讯云 AI / 百度 AI | 动物识别 API |
| 图表可视化 | uCharts / ECharts（uni-app 版） | 健康趋势图 |
| 订阅消息 | 微信订阅消息 API | 官方推送通道 |
| 图片 CDN | 腾讯云 COS + CDN | 已有 COS，加 CDN 加速 |
