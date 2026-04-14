# 宠迹 - 宠物生活伴侣

一款融合宠物分享、健康管理、习惯打卡的小程序，定位为"宠物的生活记录工具+轻社交社区"。

> **最新版本**：v2.1.0 | **更新日期**：2026-04-14 | [项目状态总览](docs/项目状态总览.md)

## 📢 最近更新

### 2026-04-14 - 关注功能上线 ⭐

- ✅ **新增关注系统**：完整的关注/取消关注功能
- ✅ **首页Tab优化**：新增"关注"Tab，支持全部/关注/推荐/收藏切换
- ✅ **收藏功能优化**：持久化到数据库，支持收藏动态流
- ✅ **数据库更新**：新增`follows`表和`post_ee`表

详见：[关注功能实现报告](关注功能实现报告.md)

## 项目概述

- **项目名称**：宠迹 / 宠叮当 / 客事
- **产品定位**：宠物的生活记录工具+轻社交社区
- **目标用户**：20-35岁，女性偏多，有记录和分享习惯
- **核心价值**：帮助宠主科学记录宠物健康、养成良好养宠习惯，同时通过轻松的内容分享获得情感满足

## 核心功能

### ✅ 已上线功能

| 模块 | 功能 | 说明 |
|------|------|------|
| **用户系统** | 微信登录、用户管理 | JWT Token认证 |
| **宠物管理** | 添加/编辑/删除宠物 | 支持猫、狗等类型 |
| **打卡系统** | 8个默认打卡项 | 连续打卡统计 |
| **动态社区** | 发布动态、点赞、评论、收藏 | Redis+数据库双重存储 |
| **关注系统** ⭐ | 关注/取消关注、关注动态流 | 新增功能 |
| **健康记录** | 体重、步数、饮水量记录 | 7天趋势图 |
| **提醒系统** | 疫苗、驱虫提醒 | 定时任务自动检查 |
| **成就系统** | 6个默认成就 | 自动解锁机制 |

### 📋 规划中功能

- **V1.1**：多宠物管理优化、自定义打卡项、宠物相册
- **V1.2**：主题挑战赛、健康周报、会员订阅
- **V2.0**：AI品种识别、宠物表情识别、智能健康建议

## 技术架构

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Uni-app | - | 跨平台开发框架 |
| Vue.js | 3.4+ | 渐进式JavaScript框架 |
| uView UI | 3.2+ | Uni-app UI组件库 |
| Pinia | 2.1+ | 状态管理 |
| Axios | 1.6+ | HTTP客户端 |

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 开发语言 |
| Spring Boot | 3.2.0 | 应用框架 |
| Spring Security | 6.x | 安全框架 |
| JWT | 0.12.3 | Token认证 |
| MyBatis-Plus | 3.5.5 | 持久层框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7 | 缓存数据库 |
| 阿里云 OSS | 3.17.1 | 对象存储 |

### 架构图

```
用户层 (微信小程序/H5)
    ↓
Nginx (反向代理 + 负载均衡)
    ↓
Spring Boot 应用 (Java 21)
    ↓
    ├─→ MySQL (数据持久化)
    ├─→ Redis (缓存加速)
    ├─→ 阿里云OSS (文件存储)
    └─→ 消息队列 (预留)
```

## 快速开始

### 环境要求

- **后端**：Java 21+, Maven 3.6+, MySQL 8.0+, Redis 7.0+
- **前端**：Node.js 18+, npm 8.0+

### 后端启动

```bash
# 1. 进入后端目录
cd backend

# 2. 执行数据库初始化
mysql -u root -p < src/main/resources/init.sql

# 3. 配置环境变量
cp .env.example .env
# 编辑.env文件，填写数据库连接信息

# 4. 启动后端
mvnw.cmd spring-boot:run  # Windows
./mvnw spring-boot:run    # Linux/Mac
```

### 前端启动

```bash
# 1. 进入前端目录
cd frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev
```

### Docker Compose启动

```bash
# 一键启动所有服务
docker-compose up -d
```

### 访问地址

- **前端**：http://localhost:8080
- **后端API**：http://localhost:8080/api
- **健康检查**：http://localhost:8080/api/health

## 项目结构

```
pet-trail/
├── backend/                    # 后端项目
│   ├── src/main/java/         # Java源码
│   │   └── com/pettrail/pettrailbackend/
│   │       ├── controller/    # 控制器
│   │       ├── service/       # 服务层
│   │       ├── mapper/        # MyBatis Mapper
│   │       ├── entity/        # 实体类
│   │       └── config/        # 配置类
│   └── src/main/resources/    # 资源文件
│       ├── application.yml    # 应用配置
│       └── init.sql          # 数据库初始化
├── frontend/                   # 前端项目
│   ├── src/pages/             # 页面
│   ├── src/components/        # 组件
│   ├── src/api/               # API接口
│   └── src/store/             # 状态管理
├── docs/                       # 文档目录
├── nginx/                      # Nginx配置
├── docker-compose.yml          # Docker Compose配置
└── README.md                   # 项目说明
```

## API文档

### 用户相关

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/users/login` | POST | 微信登录 |
| `/api/users/profile` | GET | 获取用户资料 |
| `/api/users/profile` | PUT | 更新用户资料 |

### 宠物相关

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/pets` | POST | 添加宠物 |
| `/api/pets` | GET | 获取宠物列表 |
| `/api/pets/{id}` | GET | 获取宠物详情 |
| `/api/pets/{id}` | PUT | 更新宠物 |
| `/api/pets/{id}` | DELETE | 删除宠物 |

### 动态社区

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/posts` | POST | 发布动态 |
| `/api/posts/feed?tab=all` | GET | 获取动态列表 (tab: all/follow/recommend/collect) |
| `/api/posts/{id}` | GET | 获取动态详情 |
| `/api/posts/{id}/like` | POST | 点赞/取消点赞 |
| `/api/posts/{id}/ee` | POST | 收藏/取消收藏 |
| `/api/posts/{id}/comments` | POST | 发表评论 |
| `/api/posts/{id}/comments` | GET | 获取评论列表 |

### 关注系统 ⭐新增

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/follows/{followeeId}` | POST | 关注/取消关注 |
| `/api/follows/check/{followeeId}` | GET | 检查是否已关注 |
| `/api/follows/list` | GET | 获取关注列表 |

### 打卡系统

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/checkin/items` | GET | 获取打卡项 |
| `/api/checkin` | POST | 执行打卡 |
| `/api/checkin/calendar` | GET | 获取打卡日历 |
| `/api/checkin/{id}/cancel` | POST | 取消打卡 |

### 健康记录

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/health/weight` | POST | 记录体重 |
| `/api/health/steps` | POST | 记录步数 |
| `/api/health/water` | POST | 记录饮水量 |
| `/api/health/dashboard` | GET | 获取健康数据看板 |

### 提醒系统

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/reminders/vaccine` | POST | 创建疫苗提醒 |
| `/api/reminders/vaccine` | GET | 获取疫苗提醒列表 |
| `/api/reminders/parasite` | POST | 创建驱虫提醒 |
| `/api/reminders/parasite` | GET | 获取驱虫提醒列表 |

## 数据库设计

### 核心表结构

| 表名 | 说明 | 状态 |
|------|------|------|
| `users` | 用户表 | ✅ |
| `pets` | 宠物表 | ✅ |
| `posts` | 动态表 | ✅ |
| `post_likes` | 动态点赞表 | ✅ |
| `post_comments` | 动态评论表 | ✅ |
| `post_ee` | 动态收藏表 | ✅ 新增 |
| `follows` | 关注表 | ✅ 新增 |
| `checkin_items` | 打卡项表 | ✅ |
| `checkin_records` | 打卡记录表 | ✅ |
| `weight_records` | 体重记录表 | ✅ |
| `step_records` | 步数记录表 | ✅ |
| `water_records` | 饮水记录表 | ✅ |
| `vaccine_reminders` | 疫苗提醒表 | ✅ |
| `parasite_reminders` | 驱虫提醒表 | ✅ |
| `achievements` | 成就表 | ✅ |
| `user_achievements` | 用户成就表 | ✅ |

详细数据库设计请查看：[数据库设计文档](docs/database-design.md)

## 性能指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 接口响应时间 | < 200ms (P99) | 99%请求响应时间 |
| 系统可用性 | 99.9% | 年可用性 |
| 并发支持 | 10万 DAU | 日活跃用户 |
| 缓存命中率 | > 85% | Redis缓存命中率 |

## 安全特性

- ✅ **JWT Token认证**：所有认证接口需Token
- ✅ **CORS配置**：限制跨域请求
- ✅ **防SQL注入**：MyBatis参数化查询
- ✅ **防XSS攻击**：输入输出转义
- ✅ **文件上传安全**：类型验证+大小限制
- 📋 **HTTPS传输**：生产环境强制HTTPS

## 开发规范

### 代码规范

- **后端**：阿里巴巴Java开发手册
- **前端**：Vue.js官方风格指南
- **Git**：Git Flow工作流
- **API**：RESTful设计规范

### 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

## 项目进度

| 模块 | 进度 | 说明 |
|------|------|------|
| 用户系统 | 100% ✅ | 已完成 |
| 宠物管理 | 100% ✅ | 已完成 |
| 打卡系统 | 100% ✅ | 已完成 |
| 动态社区 | 100% ✅ | 已完成（含关注功能） |
| 关注系统 | 100% ✅ | 已完成 ⭐新增 |
| 健康记录 | 100% ✅ | 已完成 |
| 提醒系统 | 100% ✅ | 已完成 |
| 成就系统 | 100% ✅ | 已完成 |
| 消息通知 | 30% 🚧 | 部分完成 |
| AI功能 | 0% 📋 | 规划中 |

详细进度报告请查看：[项目状态总览](docs/项目状态总览.md)

## 文档索引

- [项目状态总览](docs/项目状态总览.md) - 完整的项目状态和进度
- [关注功能实现报告](关注功能实现报告.md) - 关注功能的详细实现
- [功能完成报告](功能完成报告.md) - 核心功能清单
- [启动说明](启动说明.md) - 项目启动步骤
- [快速启动指南](docs/快速启动指南.md) - 快速开始指南
- [数据库设计](docs/database-design.md) - 数据库表结构

## 贡献指南

欢迎提交Issue和Pull Request！

1. Fork本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交Pull Request

## 许可证

[MIT License](LICENSE)

## 联系方式

- **项目地址**：https://github.com/pettrail/pet-trail
- **问题反馈**：https://github.com/pettrail/pet-trail/issues
- **联系邮箱**：contact@pettrail.com

---

**版本**：v2.1.0  
**最后更新**：2026年4月14日  
**维护团队**：Pet Trail Team
