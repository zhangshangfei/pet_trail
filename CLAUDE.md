# Pet-Trail 项目配置

宠物生活伴侣小程序 - 完整的 AI 辅助开发配置

## 项目信息

- **项目名称**: 宠迹 / 宠叮当 / 客事
- **技术栈**:
  - 后端: Java 21 + Spring Boot 3.2.0 + MyBatis-Plus + MySQL 8.0
  - 前端: Uni-app + Vue.js 3.4+ + uView UI + Pinia + ECharts
  - 部署: Docker + Nginx + Docker Compose

## 开发流程: gstack

使用 gstack 完整的开发流程来管理项目：

### 核心技能 (推荐工作流)

1. **`/office-hours`** - YC 风格的产品规划
   - 描述你要构建的功能
   - 重新思考问题和用户需求
   - 生成实现方案

2. **`/plan-ceo-review`** - CEO 评审
   - 审查产品范围和优先级
   - 找到 10 星产品机会

3. **`/plan-eng-review`** - 工程经理评审
   - 锁定架构和数据流
   - 设计测试策略

4. **`/plan-design-review`** - 设计师评审
   - 评估 UI/UX 设计质量
   - 建议改进

5. **实现功能** - 开始编码

6. **`/review`** - 代码评审
   - 找到生产环境 bug
   - 自动修复明显问题

7. **`/qa <url>`** - QA 测试
   - 真实浏览器测试
   - 发现并修复 bug

8. **`/ship`** - 发布
   - 创建 PR、运行测试、合并

9. **`/land-and-deploy`** - 部署
   - 合并 PR、等待 CI、部署到生产

10. **`/retro`** - 回顾
    - 每周反思和改进

### 其他有用技能

- **`/autoplan`** - 一键生成完整计划（CEO → 设计 → 工程评审）
- **`/investigate`** - 调试和根本原因分析
- **`/cso`** - 安全审计（OWASP + STRIDE）
- **`/benchmark`** - 性能基准测试
- **`/browse`** - 真实浏览器自动化

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
├── .agents/skills/gstack/  # gstack 技能集成（需要安装 bun 生成技能文件）
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/pettrail/pettrailbackend/
│   │   ├── controller/      # REST API 控制器
│   │   ├── service/         # 业务逻辑层
│   │   ├── mapper/          # MyBatis Mapper
│   │   ├── entity/          # 数据库实体
│   │   ├── security/        # 安全配置
│   │   └── config/          # 配置类
│   ├── src/main/resources/
│   │   ├── application.yml  # 应用配置
│   │   └── init.sql         # 数据库初始化
│   └── pom.xml
├── frontend/                # Uni-app 前端
│   ├── pages/               # 页面
│   ├── components/          # 组件
│   ├── api/                 # API 接口
│   └── store/               # Pinia 状态管理
├── nginx/                   # Nginx 配置
├── docs/                    # 项目文档
├── Dockerfile               # Docker 构建文件
├── docker-compose.yml       # Docker Compose 配置
├── CLAUDE.md                # 本文件
└── README.md                # 项目说明
```

## 技术栈详情

### 后端 (Spring Boot)

- **Java 21** - 使用虚拟线程和 Sealed Classes
- **Spring Boot 3.2.0** - 应用框架
- **Spring Security 6.x** - 安全框架
- **JWT** - Token 认证
- **MyBatis-Plus 3.5.5** - ORM 框架
- **MySQL 8.0** - 关系型数据库
- **Redis 7** - 缓存数据库
- **阿里云 OSS** - 文件存储

### 前端 (Uni-app)

- **Uni-app** - 跨平台开发框架
- **Vue.js 3.4+** - 渐进式框架
- **uView UI 3.2+** - UI 组件库
- **Pinia 2.1+** - 状态管理
- **ECharts 5.5+** - 数据可视化
- **Axios 1.6+** - HTTP 客户端
- **Day.js 1.11+** - 日期处理

### 数据库设计

核心表：
- users - 用户表
- pets - 宠物表
- checkin_items - 打卡项表
- checkin_records - 打卡记录表
- weight_records - 体重记录表
- posts - 动态表
- likes - 点赞表
- comments - 评论表
- achievements - 成就表
- user_achievements - 用户成就表
- vaccine_reminders - 疫苗提醒表
- parasite_reminders - 驱虫提醒表

## 开发规范

- **代码规范**: 阿里巴巴 Java 开发手册
- **Git 规范**: Git Flow 工作流
- **接口规范**: RESTful API 设计
- **文档规范**: Swagger 自动生成

## 部署

使用 Docker Compose 进行本地开发部署：

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## 开发计划

### V1.0 (MVP) - 10天上线路线图
- [ ] Day 1-2: 基础架构搭建
- [ ] Day 3-4: 用户认证 + 宠物管理
- [ ] Day 5-6: 打卡系统 + 体重记录
- [ ] Day 7-8: 社区功能 + 激励体系
- [ ] Day 9: 数据看板 + 测试
- [ ] Day 10: 上线 + 推广准备

### V1.1 - 上线后 1-2 周
- 多宠物管理
- 食量/饮水量记录
- 自定义打卡项
- 打卡统计

### V1.2 - 上线后 1 个月
- 主题挑战赛
- 动态同步打卡
- 成就系统

### V2.0 - 上线后 3 个月
- AI 功能（品种识别、表情识别）
- 症状笔记
- 健康周报
- 会员订阅

## 安全架构

- **认证**: JWT Token (7天有效)
- **授权**: Spring Security
- **数据加密**: BCrypt（预留）
- **HTTPS**: 生产环境强制 HTTPS
- **防护**: SQL 注入、XSS、CSRF 防护

## 性能优化

- **缓存**: Redis 缓存热点数据
- **数据库**: 索引优化、分页查询
- **文件存储**: CDN 加速
- **压缩**: Nginx Gzip

## 联系方式

- 项目地址: G:\openclaw_project\pet-trail
- 文档: docs/ 目录
- 问题反馈: GitHub Issues

---

**最后更新**: 2026-03-26
