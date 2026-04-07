# 构建阶段
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# 复制pom.xml并下载依赖（利用Docker缓存）
COPY pom.xml .
RUN mvn dependency:go-offline

# 复制源代码
COPY src ./src

# 构建项目
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 安装必要工具并更新系统CA证书
RUN apk add --no-cache curl bash ca-certificates && \
    update-ca-certificates

# 可选：将系统证书同步到Java的cacerts（通常不需要，但确保万无一失）
# 如果上面步骤后仍有问题，可取消下面一行的注释
# RUN keytool -import -alias alpine-ca -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt -file /etc/ssl/certs/ca-certificates.crt || true

# 从构建阶段复制JAR文件（使用通配符避免硬编码版本号）
COPY --from=builder /app/target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs

# 暴露端口
EXPOSE 8080

# 设置时区
ENV TZ=Asia/Shanghai

# 启动应用（添加基本JVM参数优化容器环境）
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "/app/app.jar"]