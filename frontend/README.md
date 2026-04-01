# 宠迹 - 宠物生活伴侣

## 项目说明

这是一个基于 uni-app + Vue 3 + uView Plus 构建的宠物健康管理小程序。

## 技术栈

- **框架**: uni-app (Vue 3)
- **UI 库**: uView Plus
- **状态管理**: Pinia
- **图表**: echarts-for-uniapp
- **构建工具**: Vite

## 页面结构

- `/pages/dashboard/index` - 健康看板
- `/pages/pets/index` - 宠物列表
- `/pages/pets/detail` - 宠物详情
- `/pages/health/record` - 体重记录
- `/pages/health/vaccine` - 疫苗提醒
- `/pages/health/parasite` - 驱虫提醒

## 开发命令

```bash
# 安装依赖
npm install

# 运行开发服务器
npm run dev

# 运行微信小程序（开发编译）
npm run dev:mp-weixin

# 构建小程序
npm run build:mp-weixin
```

## 微信开发者工具运行

构建完成后，用微信开发者工具导入：

- 直接导入编译产物目录：`dist/build/mp-weixin`
- 或者导入项目根目录（已配置 `miniprogramRoot` 指向 `dist/build/mp-weixin`）

## 后端接口

所有 API 请求都指向 `http://localhost:8080`

## 注意事项

1. 需要配置微信小程序 AppID (wxb210d75568abe1f7)
2. 确保 node_modules 已安装
3. 需要配置后端服务地址

## 下一步

1. 安装依赖: `npm install`
2. 配置后端服务
3. 运行项目
