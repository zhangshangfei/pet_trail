# 宠迹项目 - 腾讯云 Docker 部署文档

## 一、服务器信息

| 项目             | 值             |
| -------------- | ------------- |
| 云服务商           | 腾讯云           |
| 服务器 IP         | 124.222.51.71 |
| 操作系统           | CentOS        |
| Docker         | 已安装           |
| Docker Compose | 已安装           |
| 域名             | 审批中，暂用 IP 访问  |

## 二、部署架构

```
腾讯云服务器 (124.222.51.71)
├── pet-trail-nginx (80) ← 对外暴露
│   └── 反向代理 → pet-trail-backend (8080)
│       ├── pet-trail-mysql (3306)
│       ├── pet-trail-redis (6379)
│       └── pet-trail-rabbitmq (5672/15672)
```

## 三、项目目录结构

```
/opt/pet_trail/
├── docker-compose.yml      # Docker Compose 编排文件
├── .env                    # 环境变量（敏感信息，不提交 Git）
├── .env.example            # 环境变量模板
├── Dockerfile              # 根目录 Dockerfile（备用）
├── backend/
│   ├── Dockerfile          # 后端构建 Dockerfile
│   └── src/main/resources/
│       ├── application.yml # Spring Boot 配置（环境变量注入）
│       └── init.sql        # 数据库初始化脚本
├── nginx/
│   ├── nginx.conf          # Nginx 配置
│   └── ssl/                # SSL 证书目录（待配置）
└── rabbitmq/
    └── Dockerfile          # RabbitMQ 自定义镜像（含延迟消息插件）
```

## 四、首次部署步骤

### 4.1 上传代码

```bash
ssh root@124.222.51.71
cd /opt
git clone <仓库地址> pet_trail
cd pet_trail
```

### 4.2 配置环境变量

```bash
cp .env.example .env
vim .env
```

必须填写的变量：

```env
# 数据库
MYSQL_PASSWORD=你的MySQL密码
MYSQL_ROOT_PASSWORD=你的MySQL密码（需与上面一致）

# Redis
REDIS_PASSWORD=你的Redis密码

# JWT（生成命令：openssl rand -base64 32）
JWT_SECRET=你的JWT密钥

# 微信小程序
WECHAT_MINIAPP_APP_ID=你的AppID
WECHAT_MINIAPP_APP_SECRET=你的AppSecret

# 腾讯云 COS
TENCENT_COS_SECRET_ID=你的SecretId
TENCENT_COS_SECRET_KEY=你的SecretKey
TENCENT_COS_BUCKET_NAME=你的Bucket名称

# AI（按需填写）
AI_API_KEY=你的AI密钥
AI_GLM_API_KEY=你的GLM密钥
```

### 4.3 停掉宿主机 Nginx（避免端口冲突）

```bash
systemctl stop nginx
systemctl disable nginx
```

### 4.4 启动所有服务

```bash
docker-compose up -d
```

### 4.5 验证部署

```bash
# 检查容器状态
docker-compose ps

# 健康检查
curl http://localhost/health

# 预期返回
# {"success":true,"data":{"status":"UP","timestamp":"..."}}
```

## 五、访问方式

### 5.1 当前（HTTP + IP 访问）

| 用途            | 地址                                  |
| ------------- | ----------------------------------- |
| 健康检查          | <http://124.222.51.71/health>       |
| 后端 API        | <http://124.222.51.71/api/xxx>      |
| 直连后端（调试用）     | <http://124.222.51.71:8080/api/xxx> |
| RabbitMQ 管理后台 | <http://124.222.51.71:15672>        |

### 5.2 域名审批后（HTTPS + 域名访问）

| 用途            | 地址                                |
| ------------- | --------------------------------- |
| 健康检查          | <https://你的域名/health>             |
| 后端 API        | <https://你的域名/api/xxx>            |
| RabbitMQ 管理后台 | <http://124.222.51.71:15672（不对外）> |

## 六、切换 HTTPS 步骤（域名审批后执行）

### 6.1 申请 SSL 证书

在腾讯云控制台申请免费 SSL 证书：

- 腾讯云 → SSL 证书 → 申请免费证书
- 填写域名，选择 DNS 验证
- 验证通过后下载 Nginx 格式证书

### 6.2 上传证书到服务器

```bash
# 在服务器上创建证书目录
mkdir -p /opt/pet_trail/nginx/ssl

# 上传证书文件（在本地电脑执行）
scp cert.pem root@124.222.51.71:/opt/pet_trail/nginx/ssl/
scp key.pem root@124.222.51.71:/opt/pet_trail/nginx/ssl/
```

### 6.3 修改 nginx.conf

编辑 `/opt/pet_trail/nginx/nginx.conf`，取消 HTTPS 部分的注释，修改域名：

```nginx
# 把 server_name 改为你的域名
server_name your-domain.com;

# 取消注释并确认证书路径
ssl_certificate /etc/nginx/ssl/cert.pem;
ssl_certificate_key /etc/nginx/ssl/key.pem;
```

同时在 HTTP server 块中开启强制跳转 HTTPS：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

### 6.4 重启 Nginx

```bash
docker-compose restart nginx
```

### 6.5 配置微信小程序服务器域名

微信公众平台 → 开发管理 → 服务器域名：

- request 合法域名：`https://你的域名`
- uploadFile 合法域名：`https://你的域名`

### 6.6 腾讯云安全组调整

HTTPS 启用后，可以关闭 8080 端口的外网访问：

| 端口       | 协议      | 来源            | 用途                 |
| -------- | ------- | ------------- | ------------------ |
| 80       | TCP     | 0.0.0.0/0     | HTTP（跳转 HTTPS）     |
| 443      | TCP     | 0.0.0.0/0     | HTTPS              |
| ~~8080~~ | ~~TCP~~ | ~~0.0.0.0/0~~ | ~~关闭，通过 Nginx 代理~~ |

**不要开放**：3306（MySQL）、6379（Redis）、5672（RabbitMQ）

## 七、常用运维命令

### 7.1 服务管理

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 重启某个服务
docker-compose restart backend

# 代码更新后重新部署后端
git pull
docker-compose up -d --build backend

# 只启动部分服务（不用 nginx）
docker-compose up -d mysql redis rabbitmq backend
```

### 7.2 日志查看

```bash
# 实时查看后端日志
docker-compose logs -f backend

# 最近 100 行
docker-compose logs --tail=100 backend

# 查看所有服务日志
docker-compose logs -f
```

### 7.3 容器调试

```bash
# 进入后端容器
docker exec -it pet-trail-backend bash

# 进入 MySQL
docker exec -it pet-trail-mysql mysql -u root -p

# 测试 Redis 连接
docker exec -it pet-trail-redis redis-cli -a 你的密码 ping
```

### 7.4 数据库操作

```bash
# 备份数据库
docker exec pet-trail-mysql mysqldump -u root -p你的密码 pet_trail > backup.sql

# 恢复数据库
docker exec -i pet-trail-mysql mysql -u root -p你的密码 pet_trail < backup.sql

# 重建数据库（⚠️ 清空所有数据）
docker-compose down
docker volume rm pet_trail_mysql-data
docker-compose up -d
```

## 八、环境变量说明

| 变量名                          | 说明                | 默认值         |
| ---------------------------- | ----------------- | ----------- |
| MYSQL\_PASSWORD              | MySQL root 密码     | root123456  |
| MYSQL\_DATABASE              | 数据库名              | pet\_trail  |
| REDIS\_PASSWORD              | Redis 密码          | redis123456 |
| JWT\_SECRET                  | JWT 签名密钥          | -           |
| WECHAT\_MINIAPP\_APP\_ID     | 微信小程序 AppID       | -           |
| WECHAT\_MINIAPP\_APP\_SECRET | 微信小程序 AppSecret   | -           |
| TENCENT\_COS\_SECRET\_ID     | 腾讯云 COS SecretId  | -           |
| TENCENT\_COS\_SECRET\_KEY    | 腾讯云 COS SecretKey | -           |
| TENCENT\_COS\_BUCKET\_NAME   | COS Bucket 名称     | -           |
| TENCENT\_COS\_REGION         | COS 地域            | ap-shanghai |
| AI\_ENABLED                  | AI 功能开关           | true        |
| AI\_API\_KEY                 | OpenRouter API 密钥 | -           |
| AI\_GLM\_API\_KEY            | 智谱 GLM API 密钥     | -           |
| RABBITMQ\_ENABLED            | RabbitMQ 开关       | false       |
| RABBITMQ\_USER               | RabbitMQ 用户名      | guest       |
| RABBITMQ\_PASSWORD           | RabbitMQ 密码       | guest       |
| SERVER\_PORT                 | 后端服务端口            | 8080        |

## 九、故障排查

| 问题          | 排查命令                                                   | 常见原因                 |
| ----------- | ------------------------------------------------------ | -------------------- |
| 后端启动失败      | `docker-compose logs backend`                          | MySQL/Redis 未就绪、密码错误 |
| 数据库连不上      | `docker exec -it pet-trail-mysql mysql -u root -p`     | 密码不对、容器未启动           |
| Redis 连不上   | `docker exec -it pet-trail-redis redis-cli -a 密码 ping` | 密码不对                 |
| Nginx 502   | `docker-compose ps`                                    | 后端未启动或未 healthy      |
| 端口访问不了      | `curl localhost:8080/health`                           | 安全组未放行、防火墙拦截         |
| 80 端口冲突     | `lsof -i :80`                                          | 宿主机 nginx 未停         |
| MySQL 初始化失败 | `docker-compose logs mysql`                            | init.sql 语法错误        |
| 构建失败        | `docker-compose build backend`                         | Maven 依赖下载失败         |

## 十、一键部署脚本

```bash
cat > /opt/pet_trail/deploy.sh << 'SCRIPT'
#!/bin/bash
cd /opt/pet_trail

echo "===== 拉取最新代码 ====="
git pull

echo "===== 重新构建并启动后端 ====="
docker-compose up -d --build backend

echo "===== 等待启动 ====="
sleep 30

echo "===== 检查状态 ====="
docker-compose ps
echo ""
echo "===== 健康检查 ====="
curl -s http://localhost/health || curl -s http://localhost:8080/health
echo ""
echo "===== 部署完成 ====="
SCRIPT

chmod +x /opt/pet_trail/deploy.sh
```

后续更新只需执行：

```bash
./deploy.sh
```

