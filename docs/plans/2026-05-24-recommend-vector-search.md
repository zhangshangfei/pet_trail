# 推荐算法向量检索优化 实现方案 (Redis Vector Search)

> **Goal:** 用 Redis Vector Search 替代推荐算法中的全量计算，将推荐计算从 O(N) 降到 O(log N)，支撑 10 万级用户规模

**Architecture:** 利用项目已有的 Redis 基础设施，升级为 Redis Stack (内置 RediSearch 模块)，将用户/动态的特征编码为向量后存入 Redis；推荐时通过 RediSearch 的向量相似度搜索召回 Top-K 候选集，再对候选集做精排打分。整体采用"向量召回 + 规则精排"的两阶段架构，Redis Vector 不可用时自动降级为原有全量计算。

**Tech Stack:** Redis Stack 7.2+ (RediSearch 向量检索)、Spring Data Redis 3.x (VectorSimilarity API)、Spring Boot 3.1.5

---

## 一、当前问题分析

### 1.1 性能瓶颈

| 问题 | 代码位置 | 影响 |
|------|----------|------|
| **全量加载候选用户** | L165-168: `userMapper.selectList(candidateWrapper)` | 10 万用户 = 10 万条记录加载到 JVM |
| **逐用户计算分数** | L194-215: for 循环遍历所有候选 | O(N) 复杂度 |
| **二度关注展开** | L138-143: 嵌套循环查 followee | O(M×K) |
| **缓存失效后首请求** | 2 小时缓存过期触发全量计算 | 周期性性能尖刺 |

### 1.2 扩展性瓶颈

- 用户量 > 1 万时，`computeRecommendations()` 响应时间 > 3s
- 用户量 > 10 万时，存在 OOM 风险

---

## 二、方案设计

### 2.1 整体架构

```
                    ┌──────────────────────────────┐
                    │      RecommendService         │
                    │  (两阶段推荐：召回 + 精排)      │
                    └──────────┬───────────────────┘
                               │
              ┌────────────────┼────────────────┐
              ↓                ↓                ↓
     ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
     │  向量召回层   │ │  规则精排层   │ │  降级兜底层   │
     │(Redis Vector)│ │ (原有逻辑)   │ │ (原有全量)    │
     └──────┬───────┘ └──────────────┘ └──────────────┘
            ↓
     ┌──────────────┐
     │ Redis Stack  │
     │ (RediSearch) │
     └──────────────┘
```

**两阶段推荐流程**：

```
阶段1: 向量召回 (Redis Vector Search)
  用户请求 → 构建查询向量 → FT.SEARCH 向量搜索 → 返回 200 个候选 ID
  复杂度: O(log N)，毫秒级

阶段2: 规则精排 (原有逻辑，仅对候选集)
  200 个候选 → 原有 6 维评分 → 排序 → 返回 Top 20
  复杂度: O(K)，K=200，亚毫秒级

降级: Redis Vector 不可用时
  自动降级为原有全量计算逻辑
```

### 2.2 向量设计

#### 用户向量 (User Vector) - 64 维

| 维度范围 | 特征 | 编码方式 |
|----------|------|----------|
| 0-49 | 品种偏好 (50 个热门品种) | One-Hot：拥有该品种=1，否则=0 |
| 50-51 | 宠物类别偏好 (猫/狗) | 归一化比例：猫数量/总数, 狗数量/总数 |
| 52-56 | 活跃度特征 | 粉丝数/100, 帖子数/50, 点赞数/100, 评论数/50, 关注数/50 (均 cap 到 1.0) |
| 57-59 | 社交特征 | 关注数/100, 粉丝关注比, 二度连接数/500 |
| 60-63 | 时间特征 | 注册天数/365, 最近7天活跃度, 最近30天活跃度, 是否新用户(0/1) |

#### 动态向量 (Post Vector) - 64 维

| 维度范围 | 特征 | 编码方式 |
|----------|------|----------|
| 0-49 | 作者品种偏好 (同用户向量) | 复用作者的用户向量前 50 维 |
| 50-51 | 作者宠物类别 | 同用户向量 |
| 52-55 | 热度特征 | 点赞数/100, 评论数/50, 分享数/20, 收藏数/50 (均 cap 到 1.0) |
| 56-58 | 时间特征 | 发布小时/24, 发布天数/30 (cap 1.0), 是否 24h 内 (0/1) |
| 59-63 | 互动特征 | 作者被点赞数/100, 作者被评论数/50, 作者粉丝数/100, 作者帖子数/50, 保留位 |

### 2.3 Redis 索引设计

```
Index: idx_user_vectors
  Schema:
    - user_id    NUMERIC SORTABLE
    - status     NUMERIC FILTERABLE
    - vector     VECTOR FLAT 6 TYPE FLOAT32 DIM 64 DISTANCE_METRIC COSINE

Index: idx_post_vectors
  Schema:
    - post_id    NUMERIC SORTABLE
    - author_id  NUMERIC FILTERABLE
    - deleted    NUMERIC FILTERABLE
    - created_at NUMERIC FILTERABLE
    - vector     VECTOR FLAT 6 TYPE FLOAT32 DIM 64 DISTANCE_METRIC COSINE
```

**VECTOR 索引参数说明**：
- `FLAT`：暴力搜索（精确），适合 <100 万向量；后续可切换为 `HNSW`（近似，更快）
- `DIM 64`：向量维度
- `DISTANCE_METRIC COSINE`：余弦相似度

### 2.4 数据流

```
写入路径 (异步):
  用户注册/更新宠物/发帖/点赞/关注
    → Spring Event → VectorUpdateListener
    → 重新计算向量 → HSET 写入 Redis Hash
    → RediSearch 自动索引

读取路径 (同步):
  推荐请求 → 构建当前用户查询向量
    → FT.SEARCH 向量搜索 Top-K → 得到候选 ID 列表
    → 对候选集做规则精排 → 返回结果
```

---

## 三、Redis 镜像升级方案

### 3.1 Docker Compose 升级步骤

```bash
# Step 1: 停止现有服务
docker-compose down

# Step 2: 拉取 Redis Stack 镜像
docker pull redis/redis-stack-server:7.2.0-v11

# Step 3: 修改 docker-compose.yml 中 Redis 镜像
# redis:7-alpine → redis/redis-stack-server:7.2.0-v11

# Step 4: 启动服务 (Redis 数据卷保留，数据不丢失)
docker-compose up -d

# Step 5: 验证 RediSearch 模块已加载
docker exec pet-trail-redis redis-cli -a redis123456 MODULE LIST
# 应看到 search 模块

# Step 6: 验证向量搜索功能
docker exec pet-trail-redis redis-cli -a redis123456 FT._LIST
```

### 3.2 docker-compose.yml 变更

```yaml
# 修改前
redis:
  image: redis:7-alpine

# 修改后
redis:
  image: redis/redis-stack-server:7.2.0-v11
```

### 3.3 本地开发环境升级

```bash
# 如果本地用 Docker 跑 Redis
docker stop pet-trail-redis
docker rm pet-trail-redis
docker run -d --name pet-trail-redis \
  -p 6379:6379 \
  -v redis-data:/data \
  redis/redis-stack-server:7.2.0-v11 \
  --requirepass redis123456 --appendonly yes
```

### 3.4 兼容性说明

| 项目 | 说明 |
|------|------|
| 数据兼容 | Redis Stack 完全兼容 Redis 协议，现有缓存数据无需迁移 |
| 连接方式 | Spring Data Redis 连接配置无需修改 |
| 内存占用 | RediSearch 模块约占额外 20-50MB 内存 |
| 新增命令 | `FT.CREATE`, `FT.SEARCH`, `FT.DROPINDEX` 等 |

---

## 四、降级策略

```
Redis Vector 可用性检查 (应用启动时 + 每次推荐时 try-catch)
    │
    ├── RediSearch 模块可用 → 使用向量召回 + 规则精排
    │
    └── RediSearch 不可用 (普通 Redis / 模块未加载)
         │
         └── 自动降级为原有全量计算，零影响
```

降级为**零侵入**：
- `milvus.enabled=false` (默认) 时，所有向量相关代码不加载
- 即使配置开启，运行时 RediSearch 不可用也会自动降级
- 不升级 Redis 镜像时，项目正常编译运行，使用原有推荐逻辑

---

## 五、文件结构

### 新增文件

```
backend/src/main/java/com/pettrail/pettrailbackend/
├── config/
│   └── RedisVectorConfig.java               // Redis 向量索引配置 (条件加载)
├── service/
│   ├── VectorService.java                   // 向量计算核心服务
│   └── VectorSyncService.java               // 向量数据同步服务
└── dto/
    └── VectorSearchResult.java              // 向量检索结果 DTO
```

### 修改文件

```
backend/src/main/resources/application.yml   // 新增 vector 配置段
backend/src/main/java/.../service/RecommendService.java  // 接入向量召回
docker-compose.yml                           // Redis 镜像升级
```

---

## 六、任务分解

### Task 1: Redis 镜像升级 + 配置

**Files:**
- Modify: `docker-compose.yml`
- Modify: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/java/com/pettrail/pettrailbackend/config/RedisVectorConfig.java`

**要点:**
- docker-compose.yml 中 Redis 镜像改为 `redis/redis-stack-server:7.2.0-v11`
- application.yml 新增 `vector.enabled` 开关，默认 `false`
- `RedisVectorConfig` 使用 `@ConditionalOnProperty` 条件加载
- 启动时自动创建 `idx_user_vectors` 和 `idx_post_vectors` 索引

### Task 2: 实现向量计算服务 (VectorService)

**Files:**
- Create: `backend/src/main/java/com/pettrail/pettrailbackend/service/VectorService.java`
- Create: `backend/src/main/java/com/pettrail/pettrailbackend/dto/VectorSearchResult.java`

**要点:**
- `buildUserVector(Long userId)` — 从 DB 加载用户特征，编码为 64 维 float[]
- `buildPostVector(Long postId)` — 从 DB 加载动态特征，编码为 64 维 float[]
- `searchSimilarUsers(float[] queryVector, int topK, Set<Long> excludeIds)` — RediSearch 向量搜索
- `searchSimilarPosts(float[] queryVector, int topK, Set<Long> excludeAuthorIds)` — RediSearch 向量搜索
- 所有向量操作 try-catch，失败时返回空结果（触发降级）

### Task 3: 实现向量数据同步服务 (VectorSyncService)

**Files:**
- Create: `backend/src/main/java/com/pettrail/pettrailbackend/service/VectorSyncService.java`

**要点:**
- `syncUserVector(Long userId)` — 计算用户向量 + HSET 写入 Redis
- `syncPostVector(Long postId)` — 计算动态向量 + HSET 写入 Redis
- `syncAllUsers()` / `syncAllPosts()` — 全量同步（初始化/定时补偿）
- 监听用户/宠物/动态变更事件，异步触发增量同步
- 使用 `vector:sync:user:{userId}` 和 `vector:sync:post:{postId}` 记录同步时间戳

### Task 4: 改造 RecommendService 接入向量召回

**Files:**
- Modify: `backend/src/main/java/com/pettrail/pettrailbackend/service/RecommendService.java`

**要点:**
- `computeRecommendations()` 改为两阶段：
  1. 尝试向量召回 Top-200 候选
  2. 对候选集做原有 6 维精排
  3. Redis Vector 不可用时降级为原有全量逻辑
- `computePostRecommendations()` 同理
- 新增 `VectorService` 注入 + 可用性检查

---

## 七、性能预期

| 指标 | 优化前 (全量计算) | 优化后 (向量召回+精排) |
|------|-------------------|----------------------|
| 用户推荐延迟 (1 万用户) | ~800ms | ~50ms |
| 用户推荐延迟 (10 万用户) | ~5s / OOM | ~80ms |
| 动态推荐延迟 | ~300ms | ~40ms |
| 缓存失效后首请求 | 同上 (全量计算) | 同上 (向量召回) |
| 内存占用 | 全量加载用户对象 | 仅加载 200 个候选 |
| Redis 额外内存 | 0 | ~50MB (10 万向量 × 64 维 × 4 字节) |

---

## 八、风险与应对

| 风险 | 应对 |
|------|------|
| Redis Stack 镜像体积增大 | alpine → debian，约 150MB → 300MB，可接受 |
| RediSearch 内存占用 | 10 万向量约 50MB，远小于常规缓存占用 |
| 向量质量不够精准 | 两阶段架构：向量召回保证召回率，规则精排保证准确率 |
| 数据同步延迟 | 增量同步 < 1s；定时全量补偿兜底 |
| Redis Stack 不可用 | 自动降级到原有逻辑，用户无感知 |
| 品种向量维度膨胀 | Top-50 热门品种覆盖 95% 场景，其余归入"其他" |
