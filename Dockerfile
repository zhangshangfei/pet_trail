# 构建阶段
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# 复制pom.xml并下载依赖
COPY backend/pom.xml .
RUN mvn dependency:go-offline

# 复制源代码
COPY backend/src ./src

# 构建项目
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 安装必要的工具
RUN apk add --no-cache curl bash

# 从构建阶段复制JAR文件
COPY --from=builder /app/target/pet-trail-backend-1.0.0.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs

# 暴露端口
EXPOSE 8080

# 设置时区
ENV TZ=Asia/Shanghai

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
