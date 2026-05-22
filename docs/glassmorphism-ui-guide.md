# Pet Trail 玻璃拟态 (Glassmorphism) UI 优化指南

## 🎨 设计概述

本次优化为宠物轨迹微信小程序引入了现代化的**玻璃拟态（Glassmorphism）**设计风格，主要特点包括：

- ✨ **半透明毛玻璃效果** - 使用 `backdrop-filter: blur()` 实现背景模糊
- 🌊 **波浪形装饰** - 动态多层波浪动画增强视觉层次
- 💫 **柔和阴影系统** - 多层阴影营造轻盈通透感
- 🎯 **交互反馈** - hover上浮、按压缩放、光泽扫过等微交互
- 🌓 **主题适配** - 完整支持亮色/暗色模式切换

---

## 📦 新增组件

### 1. GlassCard 玻璃卡片组件

**文件位置**: `src/components/GlassCard.vue`

一个高度可定制的玻璃拟态卡片组件，支持多种尺寸和交互效果。

#### 基础用法

```vue
<template>
  <GlassCard size="large" :hover-effect="true">
    <view class="content">
      <text>卡片内容</text>
    </view>
  </GlassCard>
</template>

<script setup>
import GlassCard from '@/components/GlassCard.vue'
</script>
```

#### 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `size` | String | `'medium'` | 卡片尺寸：`small` / `medium` / `large` |
| `hoverEffect` | Boolean | `true` | 是否启用悬停上浮效果 |
| `pressedEffect` | Boolean | `true` | 是否启用按压缩放效果 |
| `showWave` | Boolean | `false` | 是否显示底部波浪装饰 |
| `blur` | String | `'20px'` | 背景模糊程度 |
| `opacity` | Number/String | `0.75` | 背景透明度 (0-1) |
| `borderRadius` | String | `''` | 自定义圆角大小 |

#### 示例：带波浪装饰的卡片

```vue
<GlassCard
  size="large"
  :show-wave="true"
  wave-color-1="rgba(255, 107, 107, 0.15)"
  wave-color-2="rgba(255, 160, 122, 0.1)"
>
  <view class="welcome-content">
    <text class="title">欢迎回来！</text>
    <text class="subtitle">您的宠物健康数据已更新</text>
  </view>
</GlassCard>
```

---

### 2. WaveHeader 波浪形顶部装饰

**文件位置**: `src/components/WaveHeader.vue`

用于页面顶部的装饰性波浪头部组件，包含动态光球和三层波浪动画。

#### 基础用法

```vue
<template>
  <WaveHeader variant="gradient" height="400rpx">
    <view class="header-content">
      <text class="main-title">宠物健康中心</text>
      <text class="sub-title">全方位呵护您的小伙伴</text>
    </view>
  </WaveHeader>
</template>

<script setup>
import WaveHeader from '@/components/WaveHeader.vue'
</script>
```

#### 变体类型

| 变体 | 说明 | 适用场景 |
|------|------|----------|
| `gradient` | 多色渐变背景 + 波浪 | 首页、发现页等主要页面 |
| `solid` | 纯色渐变背景 | 功能页面头部 |
| `minimal` | 柔和渐变背景 | 次要页面或模态框 |

#### 自定义颜色

```vue
<WaveHeader
  variant="gradient"
  primary-color="rgba(59, 130, 246, 0.2)"
  secondary-color="rgba(147, 51, 234, 0.15)"
>
  <!-- 内容 -->
</WaveHeader>
```

---

## 🎨 全局样式类

### 卡片样式

在 [App.vue](src/App.vue) 中定义的全局CSS类，可在任何页面直接使用：

```css
/* 基础玻璃卡片 */
class="pt-card"

/* 带hover效果的卡片 */
class="pt-card hover-effect"

/* 带按压效果的卡片 */
class="pt-card press-effect"
```

### 按钮样式

```html
<!-- 主要按钮 -->
<button class="pt-btn-primary">提交</button>

<!-- 按钮会自动应用：
     - 渐变背景
     - 光泽扫过动画
     - hover上浮+放大
     - 按压缩小效果
-->
```

### 输入框样式

```html
<input class="glass-input" placeholder="请输入..." />
```

特性：
- 半透明背景 + 模糊效果
- 聚焦时边框高亮 + 外发光
- 平滑过渡动画

### 标签样式

```html
<view class="glass-tag">标签文本</view>
```

特性：
- 胶囊形状
- 点击变色 + 缩放反馈
- 模糊背景

### 分割线

```html
<view class="glass-divider"></view>
```

渐变透明的分割线，比传统分割线更柔和。

### 动画工具类

```html
<!-- 淡入 -->
<view class="fade-in">内容</view>

<!-- 从下往上滑入 -->
<view class="slide-up">内容</view>

<!-- 脉冲发光 -->
<view class="pulse-glow">重要提示</view>
```

---

## 🎯 页面级优化详情

### 首页 (Home)

**优化内容**：

1. **动态卡片**
   - 背景：`rgba(255, 255, 255, 0.78)` 半透明白色
   - 模糊：`20px` 高斯模糊
   - 阴影：三层阴影系统（主阴影 + 内阴影 + 高光）
   - 交互：hover上浮4rpx + active缩放至99%
   - 顶部高光线：渐变高光边

2. **分段控制器**
   - 增强 backdrop-filter 至 20px
   - 活跃项添加内阴影和高光
   - 按压反馈：scale(0.96)

3. **通知栏**
   - 模糊背景 16px
   - 柔和橙色系阴影
   - 半透明边框

4. **操作按钮**
   - hover时背景变色
   - 按压缩放至92%
   - 圆角16rpx触控区域

5. **话题标签**
   - 改为胶囊形状 (border-radius: 999rpx)
   - 模糊背景 + 半透明边框
   - hover上浮 + active缩放

6. **悬浮发布按钮(FAB)**
   - 尺寸增大至108rpx
   - 三层阴影系统
   - 弹性动画曲线 (cubic-bezier)
   - 按压旋转 -5deg 效果

---

### 健康页 (Health)

**优化内容**：

1. **导航栏**
   - 渐变背景改为半透明
   - 添加 16px 模糊效果

2. **宠物选择器**
   - 玻璃卡片样式
   - hover/active 上浮效果
   - 下拉弹出框增强模糊(24px)和阴影

3. **核心信息卡片**
   - 年龄/性别/绝育/健康评分卡片
   - 统一玻璃风格
   - 交互：hover上浮4rpx + scale(1.02)

4. **表单组件**
   - 表单容器：玻璃卡片
   - 输入框：模糊背景 + 聚焦高亮
   - 选择器：统一圆角和边框样式
   - 过渡动画：0.25s ease

---

### TabBar 底部导航

**优化内容**：

- **背景**：`rgba(255, 255, 255, 0.85)` + 24px 模糊
- **阴影**：三层阴影（外阴影×2 + 内阴影上下）
- **边框**：半透明白色边框 + 顶部渐变高光线
- **活跃项**：
  - 径向渐变光晕背景
  - 发光阴影效果
  - 图标放大 1.1x
  - 文字加粗700 + 文字阴影
- **非活跃图标**：30%灰度滤镜
- **Hover效果**：浅灰背景 + 轻微放大
- **按压反馈**：缩放至92%
- **暗色主题**：完整适配深色背景

---

## 🎨 设计变量系统

所有设计令牌都在 [App.vue](src/App.vue) 的 CSS 变量中定义：

### 亮色主题

```css
:root {
  --pt-primary: #ff5a3d;           /* 主色调-橙红 */
  --pt-primary-2: #ff7a3d;         /* 主色调-浅橙 */
  --pt-bg: linear-gradient(...);   /* 页面渐变背景 */
  --pt-card: rgba(255, 255, 255, 0.75);  /* 卡片背景 */
  --pt-glass-blur: 20px;           /* 模糊程度 */
  --pt-shadow: ...;                /* 主阴影 */
  --pt-shadow-hover: ...;          /* Hover阴影 */
  --pt-radius-lg: 32rpx;           /* 大圆角 */
}
```

### 暗色主题

```css
page.dark {
  --pt-bg: linear-gradient(...);        /* 深色渐变 */
  --pt-card: rgba(30, 30, 45, 0.75);   /* 暗色卡片 */
  --pt-shadow: ...;                     /* 增强阴影 */
}
```

---

## 💡 最佳实践

### 1. 保持一致性

始终使用全局CSS变量，避免硬编码颜色值：

```css
/* ✅ 正确做法 */
.my-component {
  background: var(--pt-card);
  color: var(--pt-text);
}

/* ❌ 错误做法 */
.my-component {
  background: rgba(255, 255, 255, 0.75);
  color: #1a1a2e;
}
```

### 2. 性能优化建议

- **避免过度使用 blur**：移动端性能敏感，建议 blur 值 ≤ 24px
- **使用 will-change**：对频繁动画元素添加 `will-change: transform`
- **限制动画数量**：单屏动画元素建议 ≤ 5 个

### 3. 可访问性

- **对比度**：文字与背景对比度 ≥ 4.5:1 (WCAG AA)
- **触控区域**：可点击元素最小 44×44px
- **减少动画**：尊重 `prefers-reduced-motion` 设置

### 4. 主题切换示例

```javascript
// 切换到暗色主题
document.documentElement.classList.add('dark')

// 或者在小程序中
uni.setStorageSync('theme', 'dark')
applyTheme('dark')
```

---

## 🔧 自定义扩展

### 创建新的玻璃组件

```vue
<template>
  <view class="my-glass-component">
    <slot></slot>
  </view>
</template>

<style scoped>
.my-glass-component {
  background: var(--pt-card);
  border-radius: var(--pt-radius-lg);
  backdrop-filter: blur(var(--pt-glass-blur));
  -webkit-backdrop-filter: blur(var(--pt-glass-blur));
  border: 1rpx solid var(--pt-border);
  box-shadow: var(--pt-shadow-soft);
  
  /* 顶部高光 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1rpx;
    background: linear-gradient(90deg,
      transparent,
      rgba(255, 255, 255, 0.8),
      transparent
    );
  }
  
  /* 交互效果 */
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    box-shadow: var(--pt-shadow-hover);
    transform: translateY(-4rpx);
  }
  
  &:active {
    transform: scale(0.98);
  }
}
</style>
```

---

## 📱 浏览器兼容性

| 特性 | iOS Safari | Chrome Android | 微信内置浏览器 |
|------|-----------|----------------|----------------|
| `backdrop-filter` | ✅ 9.0+ | ✅ 76+ | ✅ 支持 |
| `-webkit-backdrop-filter` | ✅ 9.0+ | ✅ 76+ | ✅ 必需前缀 |
| CSS 变量 | ✅ 9.0+ | ✅ 49+ | ✅ 支持 |
| `cubic-bezier()` | ✅ 全版本 | ✅ 全版本 | ✅ 支持 |

> **注意**：必须同时使用 `backdrop-filter` 和 `-webkit-backdrop-filter` 以确保兼容性。

---

## 🎓 设计原理参考

### 玻璃拟态核心要素

1. **半透明背景** (Transparency)
   - 白色主题：`rgba(255, 255, 255, 0.7-0.85)`
   - 暗色主题：`rgba(30, 30, 45, 0.7-0.85)`

2. **背景模糊** (Blur)
   - 标准值：10-24px
   - 移动端推荐：≤ 20px（性能考虑）

3. **细边框** (Subtle Border)
   - 1px solid rgba(255, 255, 255, 0.3-0.6)
   - 提升边缘清晰度

4. **多层阴影** (Layered Shadows)
   ```css
   box-shadow:
     0 8px 32px rgba(31, 38, 135, 0.15),  /* 主投影 */
     0 2px 8px rgba(0, 0, 0, 0.05),       /* 细节阴影 */
     inset 0 1px 0 rgba(255, 255, 255, 0.8); /* 顶部高光 */
   ```

5. **高光线** (Highlight Line)
   - 顶部 1px 渐变线模拟玻璃反光
   - 增强质感和立体感

### 交互动效时长参考

| 动作 | 时长 | 缓动函数 |
|------|------|----------|
| Hover进入 | 250-350ms | cubic-bezier(0.4, 0, 0.2, 1) |
| Active按压 | 100-150ms | ease-out (或无缓动) |
| 弹性动画 | 300-500ms | cubic-bezier(0.34, 1.56, 0.64, 1) |
| 页面转场 | 400-600ms | ease-in-out |

---

## 📝 更新日志

### v1.0.0 (2026-05-21)

#### ✅ 已完成
- [x] 全局CSS变量系统重构（支持亮/暗主题）
- [x] GlassCard 玻璃卡片组件开发
- [x] WaveHeader 波浪形装饰组件开发
- [x] TabBar 底部导航玻璃化改造
- [x] 首页(Home)完整玻璃拟态优化
- [x] 健康页(Health)完整玻璃拟态优化
- [x] 全局交互效果库（按钮/输入框/标签/动画）
- [x] 暗色主题完整适配

#### 🎨 设计特色
- 🌈 多层渐变背景系统
- ✨ 顶部高光线条效果
- 💫 弹性交互动画
- 🌊 动态波浪装饰
- 🎯 触控反馈优化

#### 📦 技术栈
- Vue 3 Composition API
- SCSS 样式预处理器
- CSS Variables (自定义属性)
- Backdrop Filter (毛玻璃)
- CSS Animations & Transitions

---

## 📞 使用帮助

如有问题或需要进一步定制，请参考：

- [GlassCard 组件源码](src/components/GlassCard.vue)
- [WaveHeader 组件源码](src/components/WaveHeader.vue)
- [全局样式定义](src/App.vue)
- [首页实现示例](src/pages/home/index.vue)
- [健康页实现示例](src/pages/health/index.vue)

---

**享受全新的玻璃拟态体验！✨**
