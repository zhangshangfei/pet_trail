# 文件存储迁移说明：阿里云OSS → 腾讯云COS

## 概述

已将项目的文件存储服务从**阿里云OSS**迁移到**腾讯云COS**（微信云托管推荐方案）。

## 已完成的修改

### 1. 后端代码修改

#### ✅ 依赖管理 (pom.xml)
- 移除：`aliyun-sdk-oss` 依赖
- 保留：`cos_api` 5.6.89 依赖（腾讯云COS SDK）

#### ✅ 配置类
- 删除：`OssConfig.java`
- 新增：`CosConfig.java`
  - 配置项：`secretId`, `secretKey`, `bucketName`, `region`, `domain`
  - 创建 `COSClient` Bean

#### ✅ 服务类
- 删除：`OssService.java`
- 新增：`CosService.java`
  - `uploadFile(MultipartFile file)` - 上传文件
  - `deleteFile(String fileUrl)` - 删除文件
  - 保持与原OssService相同的接口签名

#### ✅ 控制器
- 更新：`UploadController.java`
  - 将 `OssService` 引用改为 `CosService`
  - 接口保持不变，前端无需修改

### 2. 配置文件修改

#### ✅ application.yml
```yaml
# 旧配置（阿里云OSS）
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    accessKeyId: ${ALIYUN_OSS_ACCESS_KEY_ID}
    accessKeySecret: ${ALIYUN_OSS_ACCESS_KEY_SECRET}
    bucketName: pet-trail-bucket
    domain: https://pet-trail.oss-cn-hangzhou.aliyuncs.com

# 新配置（腾讯云COS）
tencent:
  cos:
    secretId: ${TENCENT_COS_SECRET_ID}
    secretKey: ${TENCENT_COS_SECRET_KEY}
    bucketName: pet-trail-bucket
    region: ap-shanghai
    domain: https://pet-trail-bucket.cos.ap-shanghai.myqcloud.com
```

#### ✅ .env.example
- 更新环境变量名称：
  - `ALIYUN_OSS_ACCESS_KEY_ID` → `TENCENT_COS_SECRET_ID`
  - `ALIYUN_OSS_ACCESS_KEY_SECRET` → `TENCENT_COS_SECRET_KEY`

#### ✅ docker-compose.yml
- 更新环境变量映射

## 需要执行的后续步骤

### 1. 创建腾讯云COS存储桶

1. 登录[腾讯云COS控制台](https://console.cloud.tencent.com/cos5)
2. 创建存储桶
   - 存储桶名称：`pet-trail-bucket`（或自定义）
   - 所属地域：`上海`（ap-shanghai）
   - 访问权限：`公有读私有写`
3. 记录以下信息：
   - SecretId
   - SecretKey
   - Bucket名称
   - Region（地域）
   - 访问域名

### 2. 配置环境变量

创建或更新 `.env` 文件：

```bash
# 腾讯云COS配置
TENCENT_COS_SECRET_ID=你的SecretId
TENCENT_COS_SECRET_KEY=你的SecretKey
```

### 3. 微信云托管环境变量配置

在微信云托管控制台添加环境变量：
- `TENCENT_COS_SECRET_ID` = 你的腾讯云SecretId
- `TENCENT_COS_SECRET_KEY` = 你的腾讯云SecretKey

### 4. 数据迁移（如需要）

如果阿里云OSS上已有文件，需要：
1. 从阿里云OSS下载文件
2. 上传到腾讯云COS
3. 更新数据库中的文件URL（如果需要）

### 5. 编译和部署

```bash
# 进入后端目录
cd backend

# 编译项目
mvn clean package

# 构建Docker镜像
docker build -t pet-trail-backend ..

# 启动服务
docker-compose up -d
```

## 配置对比

| 配置项 | 阿里云OSS | 腾讯云COS |
|--------|-----------|-----------|
| AccessKey | accessKeyId | secretId |
| SecretKey | accessKeySecret | secretKey |
| Endpoint | endpoint | region |
| Bucket | bucketName | bucketName |
| Domain | domain | domain |

## 优势

1. **微信生态集成更好** - 腾讯云COS是微信云托管的官方推荐存储方案
2. **网络延迟更低** - 云托管和COS都在腾讯云内网，速度更快
3. **成本更低** - 同地域内网流量免费，降低运营成本
4. **管理更统一** - 统一在腾讯云管理所有资源

## 注意事项

⚠️ **重要**：
- 确保腾讯云COS存储桶的访问权限设置为"公有读私有写"
- 如果使用了CDN，需要重新配置CDN加速域名
- 旧文件的URL格式可能不同，需要更新数据库记录
- 建议在正式环境部署前先在测试环境验证

## 回滚方案

如需回滚到阿里云OSS：
1. 恢复 `pom.xml` 中的OSS依赖
2. 恢复 `OssConfig.java` 和 `OssService.java`
3. 恢复 `application.yml` 中的OSS配置
4. 重新编译部署

---

迁移完成时间：2026-04-11
