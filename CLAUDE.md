# Pet-Trail 项目配置

宠物生活伴侣小程序 - AI 辅助开发配置

## 项目信息

- **项目名称**: 宠迹 (Pet Trail)
- **产品定位**: 宠物生活记录工具 + 轻社交社区
- **技术栈**:
  - 后端: Java 21 + Spring Boot 3.1.5 + MyBatis-Plus 3.5.7 + MySQL 8.0 + Redis 7.x + Caffeine
  - 前端: Uni-app (Vue 3) + uView Plus + Pinia + ECharts
  - 管理后台: Vue 3 + Element Plus + ECharts + Axios
  - 部署: Docker + Nginx + 微信云托管
- **数据库**: 22张表 (users/pets/posts/follows/checkin_items/checkin_records等)
- **版本**: v2.1.0

## 开发流程: gstack

使用 gstack 完整的开发流程来管理项目：

### 核心技能 (推荐工作流)

1. **`/office-hours`** - YC 风格的产品规划
2. **`/plan-ceo-review`** - CEO 评审
3. **`/plan-eng-review`** - 工程经理评审
4. **`/plan-design-review`** - 设计师评审
5. **实现功能** - 开始编码
6. **`/review`** - 代码评审
7. **`/qa <url>`** - QA 测试
8. **`/ship`** - 发布
9. **`/land-and-deploy`** - 部署
10. **`/retro`** - 回顾

### 快捷命令

```
/office-hours     # 开始规划
/autoplan         # 自动化计划生成
/review           # 代码评审
/qa <url>         # QA 测试
/ship             # 发布
/retro            # 团队回顾
```

## 项目结构

```
pet-trail/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/pettrail/pettrailbackend/
│   │   ├── controller/      # 34个REST API控制器
│   │   ├── service/         # 22个业务服务
│   │   ├── mapper/          # MyBatis Mapper + XML
│   │   ├── entity/          # 28个实体类
│   │   ├── dto/             # 数据传输对象
│   │   ├── config/          # 配置类(缓存/安全/COS/Sentinel等)
│   │   ├── security/        # JWT认证过滤器
│   │   ├── annotation/      # 自定义注解(@RequireRole/@OperationLog)
│   │   ├── websocket/       # WebSocket通知处理器
│   │   ├── task/            # 定时任务(提醒/审核)
│   │   └── util/            # 工具类(JWT/缓存/上下文)
│   ├── src/main/resources/
│   │   ├── application.yml  # 应用配置
│   │   ├── init.sql         # 数据库初始化(22张表)
│   │   └── mapper/          # MyBatis XML映射(12个)
│   └── pom.xml
├── frontend/                # Uni-app 前端(28个页面)
│   └── src/
│       ├── pages/           # 页面(4个TabBar+24个子页面)
│       ├── components/      # 组件(AddPetModal/ImageCropper/UserTopBar)
│       ├── api/             # 10个API模块
│       ├── store/           # Pinia状态(user/pet/tabbar)
│       ├── utils/           # 工具(request/websocket/theme/eventBus)
│       └── config/          # 环境配置(env/constants)
├── admin/                   # Vue3 管理后台(12个页面)
│   └── src/
│       ├── views/           # 管理页面(12个)
│       ├── api/             # 管理端API(60个方法)
│       ├── router/          # 路由+权限守卫
│       └── layout/          # AdminLayout
├── website/                 # 官网(静态HTML)
├── nginx/                   # Nginx配置
├── docs/                    # 项目文档
├── docker-compose.yml       # Docker Compose(4个服务)
├── Dockerfile               # 多阶段Docker构建
└── .env.example             # 环境变量示例
```

## 技术栈详情

### 后端 (Spring Boot)

- **Java 21** - 虚拟线程和 Sealed Classes
- **Spring Boot 3.1.5** - 应用框架
- **Spring Security 6.x** - 安全框架 + JWT + 自定义注解权限
- **MyBatis-Plus 3.5.7** - ORM 框架
- **MySQL 8.0** - 关系型数据库
- **Redis 7.x** - 分布式缓存(Lettuce)
- **Caffeine 3.1.8** - 本地缓存(L1)，多级缓存架构
- **腾讯云 COS 5.6.89** - 文件存储
- **Sentinel 2022.0.0.0** - 限流熔断
- **Spring WebSocket** - 实时通知推送
- **SpringDoc OpenAPI 2.3.0** - API文档
- **Fastjson2 2.0.47** - JSON处理

### 前端 (Uni-app)

- **Uni-app 3.0** - 跨平台开发框架(微信小程序+H5)
- **Vue.js 3.4+** - 渐进式框架
- **uView Plus 3.1+** - UI 组件库
- **Pinia 2.1+** - 状态管理
- **ECharts 5.5+** - 数据可视化
- **Sass 1.70+** - CSS预处理器
- **vue-i18n 9.14+** - 国际化

### 管理后台 (Vue 3)

- **Vue 3.4.27** - 前端框架
- **Element Plus 2.6.1** - UI组件库
- **ECharts 5.5.0** - 图表
- **Pinia 2.1.7** - 状态管理
- **Axios 1.6.8** - HTTP客户端

## 核心数据库表

| 模块 | 表 |
|------|-----|
| 用户 | users |
| 宠物 | pets |
| 社交 | posts, post_likes, post_comments, post_ee, follows, tags, post_tags |
| 打卡 | checkin_items, checkin_records, checkin_stats, user_hidden_items, checkin_reminders |
| 健康 | weight_records, step_records, water_records, health_daily_stats |
| 提醒 | vaccine_reminders, parasite_reminders |
| 成就 | achievements, user_achievements |
| 管理 | notifications, reports, feedbacks, admins, admin_operation_logs, system_settings, user_behaviors, user_memberships |

## 开发规范

- **代码规范**: 阿里巴巴 Java 开发手册 / Vue.js 官方风格指南
- **Git 规范**: Git Flow 工作流
- **接口规范**: RESTful API 设计
- **文档规范**: SpringDoc 自动生成
- **提交规范**: feat/fix/docs/style/refactor/test/chore

## 部署

### Docker Compose 本地开发

```bash
docker-compose up -d
docker-compose logs -f
docker-compose down
```

### 微信云托管生产部署

```bash
docker build -t pet-trail-backend:latest .
# 推送到微信云托管控制台
```

详见: [微信云托管指南](docs/微信云托管指南.md)

## 安全架构

- **认证**: JWT Token (7天有效) + Spring Security
- **授权**: @RequireRole 自定义注解 + AOP (ADMIN/SUPER_ADMIN)
- **数据加密**: BCrypt 密码编码
- **HTTPS**: 生产环境强制 HTTPS
- **防护**: SQL注入(MyBatis参数化)、XSS(输入过滤)、CSRF
- **限流**: Sentinel (动态发布10次/分、点赞5次/秒、登录3次/秒)
- **审核**: 本地敏感词 + 微信 msg_sec_check/img_sec_check API
- **日志**: @OperationLog AOP自动记录管理端操作

## 性能优化

- **多级缓存**: Caffeine(L1) + Redis(L2) + DB(L3)
- **缓存策略**: 用户信息30min/宠物详情20min/宠物列表5min/动态列表3min
- **数据库**: 索引优化、唯一约束防重、逻辑删除
- **文件存储**: 腾讯云COS + CDN加速
- **压缩**: Nginx Gzip
- **容器优化**: JVM UseContainerSupport + MaxRAMPercentage=75.0

## 已知技术债务

1. **敏感信息硬编码**: application.yml 中默认值暴露生产凭据，需迁移至环境变量
2. **N+1查询**: PostController.convertToPostVOList 对每个post查询用户和宠物信息
3. **推荐算法性能**: RecommendService 全量加载候选用户计算分数
4. **测试覆盖不足**: 34个Controller+22个Service仅5个测试类，覆盖率<15%
5. **RabbitMQ残留**: RabbitMQConfig已注释但pom.xml仍保留依赖
6. **缺少数据库迁移工具**: init.sql混合DDL和ALTER TABLE，需引入Flyway
7. **Controller层臃肿**: PostController超500行，VO转换逻辑应抽取

---

**最后更新**: 2026-04-20
