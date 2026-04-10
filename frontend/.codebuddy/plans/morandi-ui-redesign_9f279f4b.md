---
name: morandi-ui-redesign
overview: 将宠物小程序整体UI改造为治愈系莫兰迪色风格，覆盖所有页面的配色、按钮、卡片、导航栏等视觉元素，保持代码逻辑不变。
design:
  architecture:
    component: tdesign
  styleKeywords:
    - 治愈系莫兰迪
    - 低饱和度
    - 柔和圆润
    - 温暖自然
    - 毛玻璃效果
    - 呼吸感留白
    - 安静舒适
  fontSystem:
    fontFamily: PingFang SC
    heading:
      size: 34rpx
      weight: 700
    subheading:
      size: 28rpx
      weight: 600
    body:
      size: 26rpx
      weight: 400
  colorSystem:
    primary:
      - "#8BA89D"
      - "#A8C4B8"
      - "#7A9B8F"
    background:
      - "#F2EDE6"
      - "#FAF8F5"
      - rgba(255,255,255,0.92)
    text:
      - "#4A4A4A"
      - "#9B8F88"
      - "#FFFFFF"
    functional:
      - "#D4A5A5"
      - "#9BB8C4"
      - "#A8B5A0"
      - "#D4B896"
      - "#C4847C"
todos:
  - id: global-theme
    content: 重构App.vue全局CSS变量体系和基础样式类为莫兰迪色系
    status: completed
  - id: config-tabbar
    content: 更新pages.json的tabBar选中色和导航栏背景色 + custom-tab-bar/index.wxss样式
    status: completed
    dependencies:
      - global-theme
  - id: home-style
    content: 改造首页home/index.vue的全量样式(导航栏/快捷按钮/健康看板/圆环色)
    status: completed
    dependencies:
      - global-theme
  - id: dashboard-style
    content: 改造健康看板dashboard/index.vue样式(概览卡片/图表/FAB/内嵌TabBar)
    status: completed
    dependencies:
      - global-theme
  - id: health-style
    content: 改造健康记录health/index.vue样式(核心卡片/分段控制器/CTA按钮渐变)
    status: completed
    dependencies:
      - global-theme
  - id: remaining-pages
    content: 批量改造剩余页面(me/pets/detail/record/vaccine/parasite)及AddPetModal组件样式
    status: completed
    dependencies:
      - global-theme
---

## 产品概述

宠物微信小程序"宠迹"(Pet-Trail)的整体视觉风格改造，将当前橙红色调替换为治愈系莫兰迪色系，打造温暖、柔和、自然的宠物生活伴侣应用界面。仅修改样式和布局表现层，不改变任何业务逻辑代码。

## 核心功能

- **全局主题色系统重构**: 从橙红(#ff5a3d)切换到莫兰迪灰绿主色系，覆盖所有页面和组件
- **首页改版**: 导航栏、快捷操作按钮、健康看板卡片的莫兰迪风格重设计
- **健康看板页**: 概览卡片、图表、FAB按钮、内嵌TabBar的配色统一
- **健康记录页**: 核心信息卡片、分段控制器、CTA底部按钮的柔和配色
- **我的页面**: 宠物管理卡片、功能入口、Modal弹窗的风格统一
- **子页面统一步调**: 宠物列表/详情、体重记录、疫苗/驱虫提醒等全部子页面的配色同步
- **TabBar与导航栏**: 自定义TabBar选中态、页面导航栏背景色的全局对齐
- **AddPetModal组件**: 弹窗样式的莫兰迪化处理

## 技术栈

- Vue 3 + uni-app (mp-weixin) + SCSS + Pinia
- CSS变量驱动的主题体系（`:root` 变量定义在 App.vue）
- 自定义 TabBar 组件（WXML/WXSS/JS 原生实现）

## 实现方案

采用**CSS变量全局替换 + 各页面scoped样式微调**的策略，核心思路如下：

1. **CSS变量体系重构**（App.vue）: 将 `:root` 中的8个核心变量从橙红系替换为莫兰迪色系。由于所有页面均通过 `var(--pt-xxx)` 引用这些变量，此处改动会自动传播到大部分页面。

2. **硬编码颜色逐一替换**: 部分页面使用了直接写死的十六进制颜色值（非变量引用），需要逐文件扫描替换为莫兰迪对应色值。

3. **渐变色重新映射**: 所有 `linear-gradient` / `conic-gradient` 中的高饱和度渐变替换为低饱和度莫兰迪渐变组合。

4. **布局调整**: 按钮位置、圆角、间距等可按莫兰迪风格的圆润感做适当优化（如增大圆角、调整间距），但不改动DOM结构和JS逻辑。

### 莫兰迪色板最终方案

| 用途 | 原色值 | 新色值 | 色名 |
| --- | --- | --- | --- |
| 主色 primary | #ff5a3d | #8BA89D | 鼠尾草绿 |
| 主色浅 primary-2 | #ff7a3d | #A8C4B8 | 浅鼠尾草绿 |
| 全局背景 bg | #f7f3ef | #F2EDE6 | 暖灰白 |
| 卡片背景 card | rgba(255,255,255,0.96) | rgba(255,255,255,0.92) | 柔白 |
| 主文字 text | #111827 | #4A4A4A | 深灰 |
| 次文字 muted | #8b93a6 | #9B8F88 | 灰棕 |
| 强调粉 | - | #D4A5A5 | 干玫瑰粉 |
| 强调蓝 | #3b82f6 | #9BB8C4 | 雾霾蓝 |
| 成功绿 | #10b981/#4caf50 | #A8B5A0 | 莫兰迪绿 |
| 警告橙 | #f59e0b/#f5a623 | #D4B896 | 奶茶棕 |
| 危险红 | #ef4444/#f44336 | #C4847C | 铁锈红 |
| 图表蓝 | #3B82F6 | #92A8B4 | 灰蓝 |
| 图表绿 | #10B981 | #A8B5A0 | 灰绿 |
| TabBar选中 | #ff6a3d | #8BA89D | 主色同 |
| 导航栏背景 | #ff6a3d | #A8C4B8 | 主色浅 |


### 关键执行约束

- 仅修改 `<style>` 区域内的属性值，不触碰 `<template>` 和 `<script>`
- 保留所有 class 名称、动画过渡、响应式布局结构不变
- 渐变色替换遵循"同一色系深浅过渡"原则，避免跨色系的突兀跳跃

### 架构影响范围

```
App.vue (全局CSS变量) 
   ↓ 变量传播
pages/home/index.vue, pages/dashboard/index.vue, pages/me/index.vue
pages/health/index.vue, pages/pets/index.vue, pages/pets/detail.vue  
pages/health/record.vue, pages/health/vaccine.vue, pages/health/parasite.vue
components/AddPetModal.vue
custom-tab-bar/index.wxss
pages.json (tabBar/navigationBarColor配置)
```

## 目录结构

```
g:/openclaw_project/pet-trail/frontend/
├── src/
│   ├── App.vue                          # [MODIFY] 全局CSS变量 + 基础样式类
│   ├── pages.json                       # [MODIFY] tabBar/导航栏颜色配置
│   ├── custom-tab-bar/index.wxss        # [MODIFY] TabBar选中色
│   ├── pages/
│   │   ├── home/index.vue               # [MODIFY] 首页全量样式莫兰迪化
│   │   ├── dashboard/index.vue          # [MODIFY] 健康看板样式
│   │   ├── me/index.vue                 # [MODIFY] 我的页面样式
│   │   ├── health/index.vue             # [MODIFY] 健康记录样式
│   │   ├── health/record.vue            # [MODIFY] 体重记录样式
│   │   ├── health/vaccine.vue           # [MODIFY] 疫苗提醒样式
│   │   ├── health/parasite.vue          # [MODIFY] 驱虫提醒样式
│   │   ├── pets/index.vue               # [MODIFY] 宠物列表样式
│   │   └── pets/detail.vue              # [MODIFY] 宠物详情样式
│   └── components/
│       └── AddPetModal.vue              # [MODIFY] 添加宠物弹窗样式
```

## 设计风格: 治愈系莫兰迪 (Healing Morandi)

整体采用**治愈系莫兰迪**配色语言，以低饱和度的灰绿色为主色调，辅以干玫瑰粉、雾霾蓝、奶油黄等莫兰迪经典色作为功能点缀色。视觉感受追求温柔、安静、自然，契合宠物陪伴类产品的情感诉求。

### 设计原则

- **低饱和度高明度**: 所有颜色均带灰调，降低视觉刺激，营造宁静氛围
- **圆润柔软**: 大圆角(28-32rpx)、轻阴影、柔和渐变替代硬边框
- **自然呼吸感**: 适当的留白( padding/margin 增量20% )、舒适的行高
- **色彩层次**: 通过同色系深浅而非多彩对比来建立信息层级

### 页面规划 (6个核心页面)

#### 页面1 - 首页 (home)

- **顶部导航块**: 半透明白底毛玻璃效果导航栏，左侧用户头像+昵称（未登录时显示莫兰迪绿色圆形登录按钮），右侧通知铃铛图标用次文字色
- **快捷操作区**: 单个大宽度圆角按钮（一键记录健康），使用主色到主色浅的柔和渐变，配医疗图标，按钮高度100rpx圆角28rpx
- **健康看板卡片**: 白色大圆角卡片(28rpx)，内部含装饰性背景图案（降低opacity至0.08）。标题使用主文字色粗体。三列数据展示：步数圆环(雾霾蓝conic-gradient)、饮水圆环(莫兰迪绿conic-gradient)、体重趋势图。底部右侧"查看详情"链接用主色
- **底部安全区**: 为自定义TabBar预留空间

#### 页面2 - 健康看板 (dashboard)

- **顶部导航**: 毛玻璃白底导航栏，用户信息+筛选图标
- **宠物选择器**: 圆角白色卡片，头像+品种信息，箭头用次文字色
- **时间范围选择器**: 小尺寸pill标签样式，主色文字
- **三宫格概览**: 三个等宽圆角卡片，图标用莫兰迪色系，涨跌标签用成功绿/铁锈红
- **图表区**: 大圆角白色卡片，分段Tab控制器(选中态用主色填充)，纯CSS折线图(雾霾蓝线条+点)
- **详细记录列表**: 窄圆角列表项(14rpx)，日期+数据meta信息
- **悬浮FAB**: 右下角圆角方形添加按钮(16rpx圆角)，主色背景
- **内嵌TabBar**: 浮动胶囊形(64rpx圆角)，毛玻璃白底，选中项主色

#### 页面3 - 健康记录 (health)

- **顶部导航**: 返回/更多操作图标用次文字色
- **宠物选择器**: 同dashboard风格
- **核心信息横滑卡片**: 4个等宽小卡片(年龄/性别/绝育/健康评分)，数值分别用雾霾蓝/莫兰迪绿/奶茶棕，健康评分圆环边框用主色
- **分段控制器(seg)**: 圆角胶囊形容器(16rpx)，选中项主色背景+白字
- **内容面板**: 步数/饮水量/体重三个Tab面板，输入框大字号居中，快速选择卡片网格，日期时间picker pill样式
- **底部CTA固定栏**: 双按钮布局 - "返回看板"(浅灰背景+深灰字，圆角22rpx) + 主操作按钮(主色渐变，圆角22rpx)

#### 页面4 - 我的 (me)

- **顶部导航**: 头像+用户名+通知铃铛
- **宠物管理卡片**: 居中布局大卡片，标题+宠物数量副标题，横向宠物头像列表(圆形112rpx，白色边框4rpx，序号badge用主色)，虚线框添加按钮，底部双操作按钮(健康记录/喂食提醒)用主色浅和莫兰迪绿渐变
- **常用功能卡片**: 标题+九宫格功能入口(目前只显示健康监测1个)，图标背景用极浅色调

#### 页面5 - 宠物列表 (pets)

- **页面头部**: 渐变背景从主色浅过渡到透明（替代原橙红渐变），白色大标题，右侧添加按钮用主色
- **宠物卡片列表**: 左侧圆形头像(100rpx)+中间信息区(名字/品种/性别体重生日tags)+右侧箭头
- **空状态**: 居中插图+提示文字+主色按钮

#### 页面6 - 宠物详情 + 子页面组 (pets/detail, health/record, vaccine, parasite)

- **详情页头部**: 白底简洁导航，大头像(120rpx圆形)+基本信息(meta三列)+编辑/删除按钮
- **功能菜单列表**: 三个菜单项(体重记录/疫苗提醒/驱虫提醒)，圆角白色卡片
- **体重记录页**: 渐变header(主色系)、大号体重数字(主色)、记录列表、弹窗表单
- **疫苗/驱虫提醒**: 即将到期(干玫瑰粉badge)+已完成(莫兰迪绿badge)分组列表，添加按钮，状态修改弹窗
- **AddPetModal**: 底部弹出式大圆角弹窗(28rpx)，头像上传区(虚线框圆形192rpx)，表单双列网格，取消/保存按钮

## SubAgent

- **code-explorer**
- Purpose: 在实施过程中验证每个文件的样式规则位置和引用关系，确保颜色替换不遗漏
- Expected outcome: 精确定位每个文件中需要修改的CSS规则行号和具体颜色值