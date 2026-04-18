# 宠迹后台管理系统 - 功能扩展设计文档

> 更新日期：2026-04-18
> 版本：v2.0

---

## 一、现状分析

### 已有功能

| 模块 | 功能 | 状态 |
|------|------|------|
| 管理员认证 | 登录 / 获取信息 | ✅ 基础完成 |
| 仪表盘 | 总览统计 / 今日数据 | ✅ 基础完成 |
| 用户管理 | 列表 / 状态切换 | ✅ 基础完成 |
| 动态管理 | 列表 / 审核 / 删除 | ✅ 基础完成 |
| 举报管理 | 列表 / 处理 / 驳回 | ✅ 基础完成 |
| 通知管理 | 列表 / 发送通知 / 广播 | ⚠️ 广播未实现 |

### 存在问题

| 编号 | 问题 | 严重程度 |
|------|------|----------|
| P1 | 广播通知后端空实现 | 🔴 高 |
| P2 | 管理员接口无角色权限校验 | 🔴 高 |
| P3 | 前端路由守卫仅检查 token 存在性 | 🔴 高 |
| P4 | 审核/处理举报无备注输入 | 🟡 中 |
| P5 | 通知页面无筛选功能 | 🟡 中 |
| P6 | 用户/动态无详情查看 | 🟡 中 |
| P7 | 无管理员账号管理 | 🟡 中 |
| P8 | 无宠物管理模块 | 🟡 中 |
| P9 | 无评论管理模块 | 🟡 中 |
| P10 | 仪表盘无趋势图表 | 🟢 低 |
| P11 | 无操作日志 | 🟢 低 |
| P12 | 举报处理未联动处罚 | 🟢 低 |
| P13 | 无 Token 过期自动处理 | 🟡 中 |

---

## 二、扩展功能设计

### 模块总览

```
后台管理系统 v2.0
├── 1. 安全与权限（修复 + 增强）
├── 2. 仪表盘（增强）
├── 3. 用户管理（增强）
├── 4. 宠物管理（新增）
├── 5. 动态管理（增强）
├── 6. 评论管理（新增）
├── 7. 举报管理（增强）
├── 8. 通知管理（修复 + 增强）
├── 9. 管理员管理（新增）
├── 10. 操作日志（新增）
├── 11. 系统设置（新增）
└── 12. 数据导出（新增）
```

---

### 1. 安全与权限

#### 1.1 后端权限校验增强

**问题**：当前所有 Admin 接口仅依赖 Spring Security 的 `hasRole("ADMIN")`，没有区分 SUPER_ADMIN 和 ADMIN 的权限差异。

**方案**：自定义注解 `@RequireRole` + AOP 拦截

```
@RequireRole("SUPER_ADMIN")   // 仅超级管理员
@RequireRole("ADMIN")         // 所有管理员（含超级）
```

**权限矩阵**：

| 功能 | SUPER_ADMIN | ADMIN |
|------|:-----------:|:-----:|
| 仪表盘 | ✅ | ✅ |
| 用户管理（查看） | ✅ | ✅ |
| 用户管理（禁用/启用） | ✅ | ❌ |
| 宠物管理（查看） | ✅ | ✅ |
| 动态管理（查看/审核） | ✅ | ✅ |
| 动态管理（删除） | ✅ | ❌ |
| 评论管理（查看） | ✅ | ✅ |
| 评论管理（删除） | ✅ | ❌ |
| 举报管理 | ✅ | ✅ |
| 通知管理（查看/发送） | ✅ | ✅ |
| 通知管理（广播） | ✅ | ❌ |
| 管理员管理 | ✅ | ❌ |
| 操作日志 | ✅ | ✅ |
| 系统设置 | ✅ | ❌ |
| 数据导出 | ✅ | ✅ |

#### 1.2 前端 Token 过期处理

- Axios 响应拦截器：401 状态码自动清除 token 并跳转登录页
- 路由守卫：调用 `/api/admin/auth/profile` 验证 token 有效性，失败则跳转登录

#### 1.3 前端菜单权限控制

- 登录后存储管理员角色信息
- 根据角色动态显示/隐藏菜单项
- 超级管理员独占菜单：管理员管理、系统设置

---

### 2. 仪表盘增强

#### 2.1 统计卡片扩展

在现有 4 个卡片基础上新增：

| 卡片 | 数据来源 |
|------|----------|
| 总用户数 | users 表 count |
| 总宠物数 | pets 表 count |
| 总动态数 | posts 表 count |
| 总评论数 | post_comments 表 count |
| **总举报数** | reports 表 count |
| **待审核动态** | posts 表 audit_status=0 |

#### 2.2 趋势图表

使用 ECharts 展示：

| 图表 | 类型 | 数据 |
|------|------|------|
| 用户增长趋势 | 折线图 | 近 30 天每日新增用户数 |
| 动态发布趋势 | 折线图 | 近 30 天每日新增动态数 |
| 动态审核状态分布 | 饼图 | 待审核/已通过/已拒绝 数量 |
| 用户活跃度 | 柱状图 | 近 7 天日活用户数（有动态/打卡/登录） |

#### 2.3 新增后端接口

```
GET /api/admin/dashboard/trend?days=30
返回：{ dates: [], newUsers: [], newPosts: [], activeUsers: [] }

GET /api/admin/dashboard/audit-stats
返回：{ pending: 0, approved: 0, rejected: 0 }
```

---

### 3. 用户管理增强

#### 3.1 用户详情弹窗

点击用户行展开详情，展示：

| 字段 | 说明 |
|------|------|
| 基本信息 | 头像、昵称、OpenID、性别、手机号 |
| 注册时间 | created_at |
| 宠物数量 | 关联 pets 表统计 |
| 动态数量 | 关联 posts 表统计 |
| 获赞总数 | 关联 post_likes 表统计 |
| 粉丝数 | 关联 follows 表统计 |
| 关注数 | 关联 follows 表统计 |
| 状态 | 正常 / 已禁用 |

#### 3.2 新增后端接口

```
GET /api/admin/users/{id}/stats
返回：{ petCount, postCount, likeCount, followerCount, followingCount }
```

#### 3.3 前端筛选增强

- 注册时间范围筛选
- 性别筛选

---

### 4. 宠物管理（新增）

#### 4.1 宠物列表

| 列 | 字段 |
|----|------|
| ID | id |
| 宠物名称 | name |
| 品种 | breed |
| 类别 | category（猫/狗/其他） |
| 性别 | gender |
| 体重 | weight |
| 主人 | 关联用户昵称 + ID |
| 创建时间 | created_at |

#### 4.2 筛选条件

- 关键词（宠物名称）
- 类别（猫/狗/其他）
- 主人用户 ID

#### 4.3 后端接口

```
GET    /api/admin/pets              分页查询宠物列表
GET    /api/admin/pets/{id}         获取宠物详情
DELETE /api/admin/pets/{id}         删除宠物（逻辑删除）
```

#### 4.4 宠物详情弹窗

展示宠物完整信息 + 关联的健康记录概览（体重趋势、最近打卡记录）

---

### 5. 动态管理增强

#### 5.1 动态详情弹窗

| 区域 | 内容 |
|------|------|
| 发布者信息 | 头像、昵称、ID |
| 动态内容 | 完整文字内容 |
| 图片/视频 | 完整媒体预览 |
| 互动数据 | 点赞数、评论数、分享数、收藏数 |
| 位置信息 | location |
| 审核信息 | 审核状态、审核备注 |
| 时间 | 发布时间、更新时间 |

#### 5.2 审核增强

- 审核操作增加「审核备注」输入框（必填拒绝原因，通过可选）
- 批量审核：勾选多条动态，一键通过/拒绝

#### 5.3 新增接口

```
PUT /api/admin/posts/batch-audit
请求体：{ postIds: [1,2,3], auditStatus: 1, auditRemark: "批量通过" }
```

#### 5.4 已删除动态

- 增加「已删除」Tab 页，查看逻辑删除的动态
- 超级管理员可恢复已删除动态

```
GET    /api/admin/posts/deleted        查询已删除动态
PUT    /api/admin/posts/{id}/restore   恢复动态
```

---

### 6. 评论管理（新增）

#### 6.1 评论列表

| 列 | 字段 |
|----|------|
| ID | id |
| 评论内容 | content（过长截断） |
| 所属动态 | post_id + 内容摘要 |
| 评论者 | 用户昵称 + ID |
| 回复 | parent_id / reply_to_id |
| 点赞数 | like_count |
| 状态 | 正常 / 已删除 |
| 评论时间 | created_at |

#### 6.2 筛选条件

- 关键词（评论内容）
- 动态 ID
- 状态（正常/已删除）

#### 6.3 后端接口

```
GET    /api/admin/comments              分页查询评论
DELETE /api/admin/comments/{id}         删除评论（逻辑删除）
PUT    /api/admin/comments/{id}/restore 恢复评论
```

#### 6.4 数据库

```sql
-- 评论表已有 post_comments，需添加软删除字段
ALTER TABLE post_comments ADD COLUMN deleted tinyint(4) DEFAULT 0 COMMENT '是否删除: 0-未删除 1-已删除' AFTER status;
```

---

### 7. 举报管理增强

#### 7.1 处理增强

- 处理举报时增加「处理结果」输入框
- 联动处罚选项：

| 处理动作 | 说明 |
|----------|------|
| 仅标记处理 | 更新举报状态 |
| 下架内容 | 同时将动态/评论设为删除状态 |
| 禁言用户 | 同时将用户状态设为禁用 |
| 驳回举报 | 举报无效，恢复内容 |

#### 7.2 举报详情

- 展示被举报内容（动态/评论/用户信息）
- 展示举报历史（同一目标被举报次数）

#### 7.3 新增接口

```
PUT /api/admin/reports/{id}/handle
请求体增强：
{
  "status": 1,
  "result": "违规内容已下架",
  "action": "REMOVE_CONTENT",   // NONE / REMOVE_CONTENT / BAN_USER
  "targetType": "post",
  "targetId": 123
}
```

---

### 8. 通知管理修复与增强

#### 8.1 修复广播通知

**后端实现**：

```
POST /api/admin/notifications/broadcast
请求体：{ "content": "系统通知内容", "title": "通知标题" }

逻辑：
1. 查询所有状态正常的用户（status=1）
2. 批量插入 notifications 表（type=system, from_user_id=0）
3. 返回发送数量
```

**性能考虑**：用户量大时使用分批插入（每批 500 条），避免单次插入过多数据。

#### 8.2 前端筛选

- 通知类型筛选（点赞/评论/关注/收藏/系统）
- 用户 ID 筛选
- 已读状态筛选

#### 8.3 通知模板

预设常用通知模板，快速发送：

| 模板 | 内容 |
|------|------|
| 版本更新 | "宠迹小程序已更新至 vX.X，快来看看新功能吧！" |
| 活动通知 | "限时活动：xxx，快来参与！" |
| 违规提醒 | "您发布的内容因违反社区规范已被下架" |
| 自定义 | 自由输入 |

---

### 9. 管理员管理（新增）

#### 9.1 管理员列表

| 列 | 字段 |
|----|------|
| ID | id |
| 用户名 | username |
| 昵称 | nickname |
| 角色 | role（SUPER_ADMIN / ADMIN） |
| 状态 | status |
| 最后登录 | last_login_at |
| 创建时间 | created_at |
| 操作 | 编辑 / 禁用 / 重置密码 |

#### 9.2 功能

| 功能 | 说明 | 权限 |
|------|------|------|
| 新增管理员 | 设置用户名、密码、昵称、角色 | SUPER_ADMIN |
| 编辑管理员 | 修改昵称、角色 | SUPER_ADMIN |
| 禁用/启用 | 切换管理员状态 | SUPER_ADMIN |
| 重置密码 | 重置为默认密码 | SUPER_ADMIN |
| 修改自己的密码 | 当前登录管理员修改密码 | 所有管理员 |

> ⚠️ 不能删除管理员，只能禁用；不能修改自己的角色；不能禁用自己

#### 9.3 后端接口

```
GET    /api/admin/admins                  管理员列表
POST   /api/admin/admins                  新增管理员
PUT    /api/admin/admins/{id}             编辑管理员
PUT    /api/admin/admins/{id}/status      启用/禁用
PUT    /api/admin/admins/{id}/reset-pwd   重置密码
PUT    /api/admin/admins/password         修改自己的密码
```

---

### 10. 操作日志（新增）

#### 10.1 日志表设计

```sql
CREATE TABLE IF NOT EXISTS `admin_operation_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_id` bigint(20) NOT NULL COMMENT '操作管理员ID',
  `admin_name` varchar(50) DEFAULT NULL COMMENT '操作管理员用户名',
  `module` varchar(50) NOT NULL COMMENT '模块: user/post/comment/report/notification/admin',
  `action` varchar(50) NOT NULL COMMENT '动作: create/update/delete/audit/ban/login',
  `target_id` bigint(20) DEFAULT NULL COMMENT '操作目标ID',
  `target_type` varchar(30) DEFAULT NULL COMMENT '目标类型',
  `detail` json DEFAULT NULL COMMENT '操作详情（变更前后数据）',
  `ip` varchar(50) DEFAULT NULL COMMENT '操作IP',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';
```

#### 10.2 日志列表

| 列 | 字段 |
|----|------|
| 操作人 | admin_name |
| 模块 | module |
| 动作 | action |
| 目标 | target_type + target_id |
| 详情 | detail（JSON 展开） |
| IP | ip |
| 时间 | created_at |

#### 10.3 筛选

- 操作人
- 模块
- 时间范围

#### 10.4 后端接口

```
GET /api/admin/operation-logs    分页查询操作日志
```

#### 10.5 记录方式

AOP 切面自动记录，在需要记录的操作方法上添加 `@OperationLog` 注解：

```
@OperationLog(module = "user", action = "ban", description = "禁用用户")
public Result<?> updateUserStatus(...) { ... }
```

---

### 11. 系统设置（新增）

#### 11.1 设置项

| 设置项 | Key | 默认值 | 说明 |
|--------|-----|--------|------|
| 内容审核模式 | content_audit_mode | auto | auto-自动审核 / manual-人工审核 |
| 自动审核关键词 | audit_block_words | [] | 敏感词列表，逗号分隔 |
| 小程序版本号 | app_version | 1.0.0 | 用于广播版本更新通知 |
| 通知开关 | notification_enabled | true | 全局通知开关 |
| 注册开关 | registration_enabled | true | 新用户注册开关 |

#### 11.2 设置表

```sql
CREATE TABLE IF NOT EXISTS `system_settings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `setting_key` varchar(100) NOT NULL COMMENT '设置键',
  `setting_value` text DEFAULT NULL COMMENT '设置值',
  `description` varchar(200) DEFAULT NULL COMMENT '说明',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置表';

INSERT INTO `system_settings` (`setting_key`, `setting_value`, `description`) VALUES
('content_audit_mode', 'auto', '内容审核模式: auto/manual'),
('audit_block_words', '', '自动审核屏蔽词，逗号分隔'),
('app_version', '1.0.0', '小程序版本号'),
('notification_enabled', 'true', '全局通知开关'),
('registration_enabled', 'true', '新用户注册开关');
```

#### 11.3 后端接口

```
GET  /api/admin/settings           获取所有设置
PUT  /api/admin/settings           批量更新设置
PUT  /api/admin/settings/{key}     更新单个设置
```

---

### 12. 数据导出

#### 12.1 导出功能

| 模块 | 导出格式 | 说明 |
|------|----------|------|
| 用户列表 | Excel | 导出筛选后的用户数据 |
| 动态列表 | Excel | 导出筛选后的动态数据 |
| 举报列表 | Excel | 导出筛选后的举报数据 |
| 操作日志 | Excel | 导出筛选后的日志数据 |

#### 12.2 后端接口

```
GET /api/admin/export/users?keyword=xxx&status=1    导出用户
GET /api/admin/export/posts?auditStatus=0           导出动态
GET /api/admin/export/reports?status=0              导出举报
GET /api/admin/export/logs?module=user              导出日志
```

使用 Apache POI 或 EasyExcel 生成 Excel 文件，流式下载。

---

## 三、数据库变更汇总

```sql
-- 1. 评论表添加软删除字段
ALTER TABLE post_comments ADD COLUMN deleted tinyint(4) DEFAULT 0 COMMENT '是否删除: 0-未删除 1-已删除' AFTER status;

-- 2. 操作日志表（新增）
CREATE TABLE IF NOT EXISTS `admin_operation_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_id` bigint(20) NOT NULL COMMENT '操作管理员ID',
  `admin_name` varchar(50) DEFAULT NULL COMMENT '操作管理员用户名',
  `module` varchar(50) NOT NULL COMMENT '模块',
  `action` varchar(50) NOT NULL COMMENT '动作',
  `target_id` bigint(20) DEFAULT NULL COMMENT '操作目标ID',
  `target_type` varchar(30) DEFAULT NULL COMMENT '目标类型',
  `detail` json DEFAULT NULL COMMENT '操作详情',
  `ip` varchar(50) DEFAULT NULL COMMENT '操作IP',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';

-- 3. 系统设置表（新增）
CREATE TABLE IF NOT EXISTS `system_settings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `setting_key` varchar(100) NOT NULL COMMENT '设置键',
  `setting_value` text DEFAULT NULL COMMENT '设置值',
  `description` varchar(200) DEFAULT NULL COMMENT '说明',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置表';

-- 4. 系统设置初始数据
INSERT INTO `system_settings` (`setting_key`, `setting_value`, `description`) VALUES
('content_audit_mode', 'auto', '内容审核模式: auto/manual'),
('audit_block_words', '', '自动审核屏蔽词，逗号分隔'),
('app_version', '1.0.0', '小程序版本号'),
('notification_enabled', 'true', '全局通知开关'),
('registration_enabled', 'true', '新用户注册开关');
```

---

## 四、后端新增接口汇总

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 仪表盘 | GET | /api/admin/dashboard/trend | 趋势数据 |
| 仪表盘 | GET | /api/admin/dashboard/audit-stats | 审核统计 |
| 用户 | GET | /api/admin/users/{id}/stats | 用户统计 |
| 宠物 | GET | /api/admin/pets | 宠物列表 |
| 宠物 | GET | /api/admin/pets/{id} | 宠物详情 |
| 宠物 | DELETE | /api/admin/pets/{id} | 删除宠物 |
| 动态 | PUT | /api/admin/posts/batch-audit | 批量审核 |
| 动态 | GET | /api/admin/posts/deleted | 已删除动态 |
| 动态 | PUT | /api/admin/posts/{id}/restore | 恢复动态 |
| 评论 | GET | /api/admin/comments | 评论列表 |
| 评论 | DELETE | /api/admin/comments/{id} | 删除评论 |
| 评论 | PUT | /api/admin/comments/{id}/restore | 恢复评论 |
| 举报 | PUT | /api/admin/reports/{id}/handle | 处理举报（增强） |
| 管理员 | GET | /api/admin/admins | 管理员列表 |
| 管理员 | POST | /api/admin/admins | 新增管理员 |
| 管理员 | PUT | /api/admin/admins/{id} | 编辑管理员 |
| 管理员 | PUT | /api/admin/admins/{id}/status | 启用/禁用 |
| 管理员 | PUT | /api/admin/admins/{id}/reset-pwd | 重置密码 |
| 管理员 | PUT | /api/admin/admins/password | 修改自己密码 |
| 日志 | GET | /api/admin/operation-logs | 操作日志列表 |
| 设置 | GET | /api/admin/settings | 获取设置 |
| 设置 | PUT | /api/admin/settings | 更新设置 |
| 导出 | GET | /api/admin/export/users | 导出用户 |
| 导出 | GET | /api/admin/export/posts | 导出动态 |
| 导出 | GET | /api/admin/export/reports | 导出举报 |
| 导出 | GET | /api/admin/export/logs | 导出日志 |

---

## 五、前端页面变更汇总

| 页面 | 变更类型 | 说明 |
|------|----------|------|
| Dashboard.vue | 增强 | 新增趋势图表（ECharts）、审核统计饼图 |
| Users.vue | 增强 | 详情弹窗、统计信息、时间/性别筛选 |
| Pets.vue | **新增** | 宠物列表、详情弹窗、删除 |
| Posts.vue | 增强 | 详情弹窗、审核备注、批量审核、已删除/恢复 |
| Comments.vue | **新增** | 评论列表、删除/恢复 |
| Reports.vue | 增强 | 处理结果输入、联动处罚、举报详情 |
| Notifications.vue | 修复+增强 | 修复广播、筛选、通知模板 |
| Admins.vue | **新增** | 管理员列表、新增/编辑/禁用/重置密码 |
| Logs.vue | **新增** | 操作日志列表、筛选 |
| Settings.vue | **新增** | 系统设置表单 |
| ChangePassword.vue | **新增** | 修改密码弹窗 |
| AdminLayout.vue | 增强 | 菜单权限控制、新菜单项 |

---

## 六、开发优先级

### P0 - 必须修复（安全与功能缺陷）

1. 广播通知后端实现
2. 管理员权限校验（注解 + AOP）
3. 前端 Token 过期处理
4. 前端路由守卫增强

### P1 - 核心功能（管理必需）

5. 审核备注 / 处理结果输入
6. 用户详情弹窗
7. 动态详情弹窗
8. 宠物管理模块
9. 评论管理模块
10. 管理员管理模块

### P2 - 增强功能（提升效率）

11. 仪表盘趋势图表
12. 通知筛选 + 模板
13. 批量审核
14. 已删除动态/恢复
15. 举报联动处罚

### P3 - 辅助功能（锦上添花）

16. 操作日志
17. 系统设置
18. 数据导出

---

## 七、技术选型

| 需求 | 方案 | 说明 |
|------|------|------|
| 图表 | ECharts | 已在 admin/node_modules 中（通过 element-plus 依赖） |
| Excel 导出 | EasyExcel | 阿里开源，轻量高效，适合大数据量导出 |
| 权限注解 | 自定义注解 + AOP | 轻量级，无需引入额外框架 |
| 操作日志 | 自定义注解 + AOP | 与权限注解同方案 |
| 前端权限 | 路由 meta + v-if | 基于角色的菜单和按钮级权限控制 |
