# Java版本更新说明

## 更新内容

已将项目从 Java 17 升级到 **Java 21**

## 更新的文件

### 1. pom.xml
- `java.version`: 17 → 21

### 2. Dockerfile
- 构建阶段：`maven:3.9-eclipse-temurin-17` → `maven:3.9-eclipse-temurin-21`
- 运行阶段：`eclipse-temurin:17-jre-alpine` → `eclipse-temurin:21-jre-alpine`

### 3. README.md
- 后端技术栈：Java 17+ → Java 21

### 4. docs/快速启动指南.md
- 后端技术栈：Java 17 → Java 21
- 环境要求：JDK 17+ → JDK 21+

## Java 21 的优势

1. **性能提升**
   - 更快的启动速度
   - 更低的内存占用
   - 更好的并发性能

2. **新特性**
   - **Sealed Classes（密封类）**：限制继承层次
   - **Pattern Matching for switch**：更简洁的模式匹配
   - **Record Patterns**：更简洁的记录模式匹配
   - **Virtual Threads（虚拟线程）**：轻量级并发
   - **String Templates**：更安全的字符串模板
   - **Unnamed Patterns and Variables**：未命名模式和变量

3. **兼容性**
   - Spring Boot 3.2.0 完全支持 Java 21
   - Spring Security 6.x 支持 Java 21
   - MyBatis-Plus 3.5.5 支持 Java 21

## 环境要求

### 必需软件版本

- **JDK**: 21 或更高版本
- **Maven**: 3.6 或更高版本
- **MySQL**: 8.0 或更高版本
- **Redis**: 6.0 或更高版本
- **Docker**: 20.10 或更高版本

### 检查当前版本

```bash
# 检查Java版本
java -version

# 检查Maven版本
mvn -version

# 检查Docker版本
docker --version
```

## 升级步骤

### 如果已有 Java 17 环境

1. **安装 Java 21**
   ```bash
   # 使用包管理器安装
   sudo apt update
   sudo apt install openjdk-21-jdk

   # 或使用 SDKMAN
   sdk install java 21.0.2-tem
   ```

2. **设置 JAVA_HOME**
   ```bash
   # Linux/Mac
   export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
   export PATH=$JAVA_HOME/bin:$PATH

   # Windows
   setx JAVA_HOME "C:\Program Files\Java\jdk-21"
   setx PATH "%JAVA_HOME%\bin;%PATH%"
   ```

3. **验证安装**
   ```bash
   java -version
   # 应显示：openjdk version "21.0.2" 或更高
   ```

4. **重新编译项目**
   ```bash
   cd backend
   mvn clean install
   ```

5. **启动项目**
   ```bash
   mvn spring-boot:run
   ```

### 如果是全新安装

直接按照 `docs/快速启动指南.md` 进行安装即可。

## 注意事项

1. **IDE 配置**
   - IntelliJ IDEA: File → Project Structure → Project SDK → 选择 Java 21
   - VS Code: 安装 Extension Pack for Java，确保使用 Java 21

2. **Maven 配置**
   - 确保 `~/.m2/settings.xml` 中的 Java 版本设置为 21
   - 可以通过 `-Djava.version=21` 参数指定

3. **Docker 配置**
   - Dockerfile 已经更新为 Java 21
   - 使用 `docker-compose up -d` 启动即可

4. **依赖更新**
   - 部分依赖可能需要更新到支持 Java 21 的版本
   - 如果遇到兼容性问题，可以尝试升级依赖版本

## 测试验证

### 1. 编译测试
```bash
cd backend
mvn clean compile
```

### 2. 运行测试
```bash
mvn test
```

### 3. 启动服务
```bash
mvn spring-boot:run
```

### 4. 健康检查
```bash
curl http://localhost:8080/api/health
```

## 常见问题

### Q1: Maven 编译失败
**A**: 检查 Java 版本是否正确设置
```bash
java -version
mvn -version
```

### Q2: Docker 启动失败
**A**: 重新构建镜像
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### Q3: IDE 报错
**A**: 更新 IDE 的 Java SDK 到 21

## 下一步

升级完成后，可以按照 `docs/开发流程.md` 开始正式开发。

## 参考资料

- [Java 21 Release Notes](https://openjdk.org/projects/jdk/21/)
- [Spring Boot 3.2.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.2-Release-Notes)
- [Java 21 Migration Guide](https://docs.oracle.com/en/java/javase/21/migrate/)
