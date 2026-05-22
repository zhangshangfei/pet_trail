@echo off
chcp 65001 >nul
echo ========================================
echo   Pet Trail 微信小程序 快速启动脚本
echo ========================================
echo.

cd /d "%~dp0frontend"

echo [1/3] 检查依赖安装状态...
if not exist "node_modules" (
    echo ⚠️  未检测到 node_modules，正在安装依赖...
    call npm install
    if errorlevel 1 (
        echo ❌ 依赖安装失败！请检查 Node.js 是否已安装
        pause
        exit /b 1
    )
    echo ✅ 依赖安装完成！
) else (
    echo ✅ 依赖已就绪
)

echo.
echo [2/3] 开始编译微信小程序...
echo    正在执行: npm run dev:mp-weixin
echo.
call npm run dev:mp-weixin

if errorlevel 1 (
    echo.
    echo ❌ 编译失败！请检查上方错误信息
    pause
    exit /b 1
)

echo.
echo [3/3] 编译完成！
echo ========================================
echo   📱 下一步操作：
echo   1. 打开微信开发者工具
echo   2. 选择 "导入项目"
echo   3. 目录选择:
echo      %~dp0frontend\dist\dev\mp-weixin
echo   4. AppID 会自动填充
echo   5. 点击 "确定" 运行
echo ========================================
echo.
pause