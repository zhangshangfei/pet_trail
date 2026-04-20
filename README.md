# 宠迹 - 宠物生活伴侣

一款融合宠物分享、健康管理、习惯打卡的小程序，定位为"宠物的生活记录工具+轻社交社区"。

> **最新版本**：v2.1.0 | **更新日期**：2026-04-20 | [项目状态总览](docs/项目状态总览.md)

## 项目概述

- **项目名称**：宠迹 (Pet Trail)
- **产品定位**：宠物生活记录工具 + 轻社交社区
- **目标用户**：20-35岁宠物主人，女性偏多，有记录和分享习惯
- **核心价值**：科学记录宠物健康、养成良好养宠习惯、通过内容分享获得情感满足
- **官网**：https://pettrail.openclaw.com

## 核心功能

### 已上线功能

| 模块 | 功能 | 说明 |
|------|------|------|
| **用户系统** | 微信登录、资料编辑、头像裁剪 | JWT Token认证(7天) |
| **宠物管理** | 添加/编辑/删除宠物、成长相册、日历 | 支持猫、狗等类型 |
| **打卡系统** | 8个默认+自定义打卡项、报表、提醒 | 连续打卡统计、周报/月报 |
| **动态社区** | 发布图文/视频、点赞、收藏、评论、分享 | Redis+数据库双重存储 |
| **关注系统** | 关注/取消关注、关注动态流、发现用户 | 6维评分推荐算法 |
| **健康记录** | 体重、步数、饮水量记录、健康评分 | 4维度动态计算(疫苗30%+驱虫30%+体重20%+打卡20%) |
| **提醒系统** | 疫苗、驱虫、喂食、打卡提醒 | 定时任务+系统通知推送 |
| **通知系统** | 5种通知类型、WebSocket实时推送 | 心跳保活+自动重连 |
| **话题标签** | 热门标签、搜索、发布时选择 | 8个官方预置标签 |
| **用户行为** | 浏览/点赞/收藏/评论/分享行为追踪 | 推荐算法增强 |
| **后台管理** | 12个管理页面、权限控制、操作日志 | ADMIN/SUPER_ADMIN二级角色 |
| **内容审核** | 本地敏感词+微信API双重审核 | 异步审核+定时复审 |
| **成就系统** | 6个默认成就 | 数据库已创建，前端待开发 |

## 技术架构

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Uni-app | 3.0 | 跨平台开发框架(微信小程序+H5) |
| Vue.js | 3.4+ | 渐进式JavaScript框架 |
| uView Plus | 3.1+ | Uni-app UI组件库 |
| Pinia | 2.1+ | 状态管理 |
| ECharts | 5.5+ | 数据可视化 |
| Sass | 1.70+ | CSS预处理器 |

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 开发语言(虚拟线程+Sealed Classes) |
| Spring Boot | 3.1.5 | 应用框架 |
| Spring Security | 6.x | 安全框架 |
| JWT | 0.12.6 | Token认证 |
| MyBatis-Plus | 3.5.7 | 持久层框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.x | 缓存(Lettuce) |
| Caffeine | 3.1.8 | 本地缓存(L1) |
| 腾讯云 COS | 5.6.89 | 对象存储 |
| Sentinel | 2022.0.0.0 | 限流熔断 |
| Spring WebSocket | - | 实时通信 |
| SpringDoc OpenAPI | 2.3.0 | API文档 |

### 管理后台技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue 3 | 3.4.27 | 前端框架 |
| Element Plus | 2.6.1 | UI组件库 |
| ECharts | 5.5.0 | 图表 |
| Pinia | 2.1.7 | 状态管理 |
| Axios | 1.6.8 | HTTP客户端 |

### 架构图

```
┌─────────────────────────────────────────────────────┐
│                       用户层                         │
│  微信小程序 │ H5页面 │ 管理后台(Vue3+ElementPlus)    │
└──────────────────────────┬──────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────┐
│                       网关层                         │
│  Nginx (反向代理 + 负载均衡 + SSL + Gzip)           │
│  微信云托管 (小程序环境 callContainer)               │
└──────────────────────────┬──────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────┐
│                  应用层 (Spring Boot 3.1.5)          │
│  用户认证 │ 宠物管理 │ 打卡系统 │ 社区动态 │ 健康记录 │
│  提醒系统 │ 关注系统 │ 通知推送 │ 内容审核 │ 推荐算法 │
└──────────────────────────┬──────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────┐
│                       数据层                         │
│  MySQL 8.0 │ Redis 7.x │ Caffeine L1 │ 腾讯云COS   │
└─────────────────────────────────────────────────────┘
```

## 快速开始

### 环境要求

- **后端**：Java 21+, Maven 3.6+, MySQL 8.0+, Redis 7.0+
- **前端**：Node.js 18+, npm 8.0+

### Docker Compose 一键启动

```bash
docker-compose up -d
```

### 手动启动

```bash
# 后端
cd backend
mysql -u root -p < src/main/resources/init.sql
mvnw.cmd spring-boot:run

# 前端
cd frontend
npm install
npm run dev

# 管理后台
cd admin
npm install
npm run dev
```

### 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 H5 | http://localhost:5173 | 开发模式 |
| 后端 API | http://localhost:8080/api | RESTful API |
| API 文档 | http://localhost:8080/swagger-ui/index.html | SpringDoc |
| 管理后台 | http://localhost:5174 | 管理端 |

## 项目结构

```
pet-trail/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/          # Java源码
│   │   └── com/pettrail/pettrailbackend/
│   │       ├── controller/     # 34个控制器(15用户端+13管理端+6其他)
│   │       ├── service/        # 22个服务
│   │       ├── mapper/         # MyBatis Mapper + XML
│   │       ├── entity/         # 28个实体类
│   │       ├── dto/            # 数据传输对象
│   │       ├── config/         # 配置类(缓存/安全/COS/MQ等)
│   │       ├── security/       # JWT认证过滤器
│   │       ├── annotation/     # 自定义注解(权限/日志)
│   │       ├── websocket/      # WebSocket处理器
│   │       ├── task/           # 定时任务
│   │       └── util/           # 工具类
│   └── src/main/resources/
│       ├── application.yml     # 应用配置
│       ├── init.sql            # 数据库初始化(22张表)
│       └── mapper/             # MyBatis XML映射
├── frontend/                   # Uni-app 前端(28个页面)
│   └── src/
│       ├── pages/              # 页面(4个TabBar+24个子页面)
│       ├── components/         # 组件
│       ├── api/                # 10个API模块
│       ├── store/              # Pinia状态管理
│       ├── utils/              # 工具(请求/WebSocket/主题)
│       └── config/             # 环境配置
├── admin/                      # Vue3 管理后台(12个页面)
│   └── src/
│       ├── views/              # 管理页面
│       ├── api/                # 管理端API(60个方法)
│       ├── router/             # 路由+权限守卫
│       └── layout/             # 管理端布局
├── website/                    # 官网(静态HTML)
├── nginx/                      # Nginx配置
├── docs/                       # 项目文档
├── docker-compose.yml          # Docker Compose配置
├── Dockerfile                  # 多阶段Docker构建
└── .env.example                # 环境变量示例
```

## 数据库设计

共 **22 张表**，按模块划分：

| 模块 | 表名 | 说明 |
|------|------|------|
| **用户** | users | 用户表(openid/unionid/昵称/头像) |
| **宠物** | pets | 宠物表(品种/性别/绝育/类别/体重/毛色) |
| **社交** | posts, post_likes, post_comments, post_ee, follows, tags, post_tags | 动态/点赞/评论/收藏/关注/标签 |
| **打卡** | checkin_items, checkin_records, checkin_stats, user_hidden_items, checkin_reminders | 打卡项/记录/统计/隐藏/提醒 |
| **健康** | weight_records, step_records, water_records, health_daily_stats | 体重/步数/饮水/每日统计 |
| **提醒** | vaccine_reminders, parasite_reminders | 疫苗/驱虫提醒 |
| **成就** | achievements, user_achievements | 成就/用户成就 |
| **管理** | notifications, reports, feedbacks, admins, admin_operation_logs, system_settings, user_behaviors, user_memberships | 通知/举报/反馈/管理员/日志/设置/行为/会员 |

详细设计请查看：[数据库设计文档](docs/database-design.md)

## 安全特性

- JWT Token认证(7天有效期)
- Spring Security + 自定义权限注解(@RequireRole)
- CORS跨域配置
- MyBatis参数化查询防SQL注入
- 内容审核(本地敏感词+微信API)
- 文件上传安全(类型验证+大小限制)
- 操作日志AOP自动记录(@OperationLog)
- Sentinel限流熔断(动态发布10次/分、点赞5次/秒、登录3次/秒)

## 项目进度

| 模块 | 进度 | 说明 |
|------|------|------|
| 用户系统 | 95% ✅ | 微信登录/资料/发现用户(账号注销前端入口暂隐藏) |
| 宠物管理 | 90% ✅ | CRUD+相册+日历 |
| 打卡系统 | 90% ✅ | 打卡/统计/报表/提醒/自定义项 |
| 动态社区 | 90% ✅ | 发布/Feed/点赞/收藏/评论/分享/标签 |
| 关注系统 | 85% ✅ | 关注/粉丝/关注动态流/发现用户 |
| 健康记录 | 80% ✅ | 体重/步数/饮水/健康评分/看板 |
| 提醒系统 | 80% ✅ | 疫苗/驱虫/喂食/打卡提醒 |
| 通知系统 | 85% ✅ | 5种类型+WebSocket推送+分类筛选 |
| 内容审核 | 80% ✅ | 双重审核+定时复审 |
| 后台管理 | 85% ✅ | 12页面+权限+日志(数据导出待实现) |
| 成就系统 | 30% 🚧 | 数据库完成，Service/前端待开发 |
| 会员系统 | 40% 🚧 | Entity+前端页面完成，支付逻辑待开发 |
| AI功能 | 0% 📋 | 规划中 |

详细进度请查看：[项目状态总览](docs/项目状态总览.md)

## 文档索引

| 文档 | 说明 |
|------|------|
| [项目状态总览](docs/项目状态总览.md) | 完整的项目状态、架构和进度 |
| [项目发展规划](docs/项目发展规划.md) | 后续发展规划与设计方案 |
| [微信小程序功能分析](docs/微信小程序功能分析.md) | 小程序功能详细分析 |
| [后台管理系统功能分析](docs/后台管理系统功能分析.md) | 管理后台功能分析 |
| [数据库设计](docs/database-design.md) | 数据库表结构设计 |
| [微信云托管指南](docs/微信云托管指南.md) | 云托管配置与部署 |
| [快速启动指南](docs/快速启动指南.md) | 快速开始开发 |
| [功能完成报告](功能完成报告.md) | 已完成功能清单 |
| [启动说明](启动说明.md) | 项目启动步骤 |

## 贡献指南

1. Fork本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交Pull Request

### 提交规范

```
feat: 新功能 | fix: 修复bug | docs: 文档更新 | style: 代码格式
refactor: 重构 | test: 测试 | chore: 构建/工具
```

## 许可证

[MIT License](LICENSE)

---

**版本**：v2.1.0 | **最后更新**：2026年4月20日 | **维护团队**：Pet Trail Team
