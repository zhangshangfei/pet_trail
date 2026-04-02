@echo off
echo ========================================
echo 宠迹后端 - 启动脚本
echo ========================================
echo.

REM 检查 Java 版本
echo 检查 Java 环境...
java -version
if errorlevel 1 (
    echo [错误] 未找到 Java 环境，请安装 Java 21 或更高版本
    pause
    exit /b 1
)

echo.
echo 开始编译项目...
call mvn clean package -DskipTests

if errorlevel 1 (
    echo.
    echo [错误] 编译失败，请检查错误信息
    pause
    exit /b 1
)

echo.
echo 编译成功！正在启动...
echo.

REM 启动后端
java -jar target/pet-trail-backend-1.0.0.jar

pause
