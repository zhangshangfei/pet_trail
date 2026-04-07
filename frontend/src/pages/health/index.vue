<template>
  <view class="health-page">
    <!-- Top nav (keep this) -->
    <view class="health-nav">
      <view class="health-statusbar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="health-nav-inner">
        <view class="health-user">
          <image class="health-user-avatar" :src="userAvatar" mode="aspectFill" />
          <text class="health-user-name">{{ userName }}</text>
        </view>
        <view class="health-nav-actions">
          <text class="health-nav-icon" @tap="goBackToBoard">‹</text>
          <text class="health-nav-icon" @tap="onMore">⋯</text>
          <text class="health-nav-icon" @tap="onSettings">⚙</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="health-scroll" :style="{ paddingTop: headerHeight + 'px' }">
      <view class="health-content">
        <!-- Pet selector -->
        <view class="pet-selector">
          <view class="pet-selector-card" @touchstart.stop="togglePetSelector">
            <view class="pet-selector-left">
              <image
                class="pet-selector-avatar"
                :src="currentPet && currentPet.avatar ? currentPet.avatar : fallbackPetAvatar"
                mode="aspectFill"
              />
              <view class="pet-selector-meta">
                <text class="pet-selector-name">{{ currentPet ? currentPet.name : "请选择宠物" }}</text>
                <text class="pet-selector-breed">{{ currentPet ? (currentPet.breed || "") : "" }}</text>
              </view>
            </view>
            <text class="pet-selector-arrow">{{ petSelectorOpen ? "⌃" : "⌄" }}</text>
          </view>

          <view v-show="petSelectorOpen" class="pet-selector-pop" @touchstart.stop="closePetSelector">
            <view class="pet-selector-search">
              <input class="pet-selector-input" v-model="searchQuery" placeholder="搜索宠物..." />
            </view>
            <scroll-view scroll-y class="pet-selector-list">
              <view
                v-for="pet in pets"
                :key="pet.id"
                class="pet-selector-item"
                @touchstart.stop="selectPet(pet)"
                v-show="!searchQuery || (pet.name && pet.name.toLowerCase().includes(searchQuery.toLowerCase())) || (pet.breed && pet.breed.toLowerCase().includes(searchQuery.toLowerCase()))"
              >
                <image
                  class="pet-selector-item-avatar"
                  :src="pet.avatar || fallbackPetAvatar"
                  mode="aspectFill"
                />
                <view class="pet-selector-item-meta">
                  <text class="pet-selector-item-name">{{ pet.name }}</text>
                  <text class="pet-selector-item-breed">{{ pet.breed || "" }}</text>
                </view>
              </view>
            </scroll-view>
          </view>
        </view>

        <!-- Core info row -->
        <scroll-view scroll-x class="core-row" show-scrollbar="false">
          <view class="core-row-inner">
            <view class="core-card">
              <text class="core-value core-blue">{{ petAgeText }}</text>
              <text class="core-label">年龄</text>
            </view>
            <view class="core-card">
              <text class="core-value core-green">{{ petGenderText }}</text>
              <text class="core-label">性别</text>
            </view>
            <view class="core-card">
              <text class="core-value core-orange">{{ petSpayedText }}</text>
              <text class="core-label">绝育</text>
            </view>
            <view class="core-card core-card-wide">
              <view class="score-circle">
                <text class="score-text">{{ petHealthScore }}%</text>
              </view>
              <text class="core-label">健康评分</text>
            </view>
          </view>
        </scroll-view>

        <!-- Tabs -->
        <view class="seg">
          <view
            v-for="tab in tabs"
            :key="tab.key"
            class="seg-item"
            :class="{ active: activeTab === tab.key }"
            @tap="activeTab = tab.key"
          >
            <text>{{ tab.label }}</text>
          </view>
        </view>

        <!-- Panels -->
        <view class="panel" v-if="activeTab === 'steps'">
          <text class="panel-label">今日步数</text>
          <view class="panel-input-row">
            <input class="panel-input-big" type="number" v-model="stepData.value" placeholder="请输入步数" />
            <text class="panel-unit">步</text>
          </view>

          <text class="panel-label">距离</text>
          <view class="panel-input-row panel-input-row-sm">
            <input class="panel-input" type="digit" v-model="stepData.distance" placeholder="请输入距离(km)" />
            <text class="panel-unit">km</text>
          </view>

          <text class="panel-label">记录时间</text>
          <view class="dt-row">
            <picker mode="date" :value="stepData.date" @change="(e) => (stepData.date = e.detail.value)">
              <view class="dt-pill">{{ stepData.date }}</view>
            </picker>
            <picker mode="time" :value="stepData.time" @change="(e) => (stepData.time = e.detail.value)">
              <view class="dt-pill">{{ stepData.time }}</view>
            </picker>
          </view>

          <view class="ghost-btn" @tap="syncDevice">
            <text class="ghost-icon">⟳</text>
            <text>同步运动设备</text>
          </view>
        </view>

        <view class="panel" v-if="activeTab === 'water'">
          <text class="section-title">快速选择</text>
          <view class="quick-row">
            <view
              v-for="opt in waterOptions"
              :key="opt.type"
              class="quick-card"
              @tap="selectWaterAmount(opt)"
            >
              <view class="quick-icon" :style="{ background: opt.bg }">
                <text>{{ opt.emoji }}</text>
              </view>
              <text class="quick-name">{{ opt.name }}</text>
              <text class="quick-amount">{{ opt.amount }}</text>
            </view>
          </view>

          <text class="section-title section-title-gap">自定义水量</text>
          <view class="input-box">
            <input
              class="input-box-value"
              type="number"
              v-model="waterData.value"
              placeholder="0"
            />
            <view class="input-box-unit" @tap="toggleWaterUnit">
              <text>{{ waterData.unit }}</text>
              <text class="unit-chevron">⌄</text>
            </view>
          </view>

          <text class="section-title section-title-gap">记录时间</text>
          <view class="time-box">
            <picker mode="date" :value="waterData.date" @change="(e) => (waterData.date = e.detail.value)">
              <view class="time-pill">{{ waterData.date }}</view>
            </picker>
            <picker mode="time" :value="waterData.time" @change="(e) => (waterData.time = e.detail.value)">
              <view class="time-pill">{{ waterData.time }}</view>
            </picker>
            <text class="time-icon">📅</text>
          </view>
        </view>

        <view class="panel" v-if="activeTab === 'weight'">
          <text class="panel-label">当前体重</text>
          <view class="panel-input-row">
            <input class="panel-input-big" type="digit" v-model="weightData.value" placeholder="0.0" />
            <text class="panel-unit">kg</text>
          </view>

          <text class="panel-label">记录时间</text>
          <view class="dt-row">
            <picker mode="date" :value="weightData.date" @change="(e) => (weightData.date = e.detail.value)">
              <view class="dt-pill">{{ weightData.date }}</view>
            </picker>
            <picker mode="time" :value="weightData.time" @change="(e) => (weightData.time = e.detail.value)">
              <view class="dt-pill">{{ weightData.time }}</view>
            </picker>
          </view>

          <text class="panel-label">近期趋势</text>
          <view class="trend-row">
            <view v-for="p in weightHistory" :key="p.date" class="trend-item">
              <view class="trend-bar" :style="{ height: p.h + 'rpx', background: p.color }"></view>
              <text class="trend-date">{{ p.date }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Bottom CTA -->
    <view class="cta">
      <view class="cta-row">
        <view class="cta-btn cta-back" @tap="goBackToBoard">
          <text class="cta-text cta-text-dark">返回看板</text>
        </view>
        <view class="cta-btn cta-main" :class="ctaClass" @tap="saveRecord">
          <text class="cta-text">{{ ctaText }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>

function todayStr() {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}
function timeStr() {
  const d = new Date();
  const hh = String(d.getHours()).padStart(2, "0");
  const mm = String(d.getMinutes()).padStart(2, "0");
  return `${hh}:${mm}`;
}

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 0,
      userAvatar: "https://ai-public.mastergo.com/ai/img_res/1774575365924a3K9mP2xQ7vN4rT8wY.jpg",
      userName: "小萌宠主人",
      fallbackPetAvatar: "https://ai-public.mastergo.com/ai/img_res/1774575365924b4L8nQ3xR6vM9wP2yZ.jpg",
      pets: [],
      currentPet: null,
      petSelectorOpen: false,
      searchQuery: "",
      activeTab: "weight",
      tabs: [
        { key: "steps", label: "步数" },
        { key: "water", label: "饮水量" },
        { key: "weight", label: "体重" }
      ],
      stepData: { value: "", distance: "", date: todayStr(), time: timeStr() },
      waterData: { value: "", unit: "ml", date: todayStr(), time: timeStr() },
      weightData: { value: "", date: todayStr(), time: timeStr() },
      waterOptions: [
        { type: "bowl", name: "狗碗", amount: "200ml", emoji: "🥣", bg: "#dbeafe" },
        { type: "bottle", name: "瓶子", amount: "500ml", emoji: "🧴", bg: "#dcfce7" },
        { type: "cup", name: "杯子", amount: "150ml", emoji: "☕", bg: "#ffedd5" }
      ],
      weightHistory: []
    };
  },
  computed: {
    petAgeText() {
      // 当前项目没 age 字段：用 birthday 推算；没有就 "-"
      const b = this.currentPet && this.currentPet.birthday;
      if (!b) return "-";
      const bd = new Date(b);
      if (Number.isNaN(bd.getTime())) return "-";
      const now = new Date();
      let age = now.getFullYear() - bd.getFullYear();
      const m = now.getMonth() - bd.getMonth();
      if (m < 0 || (m === 0 && now.getDate() < bd.getDate())) age -= 1;
      return `${Math.max(0, age)}岁`;
    },
    petGenderText() {
      const g = this.currentPet && this.currentPet.gender;
      return g === 1 ? "♂" : g === 2 ? "♀" : "❓";
    },
    petSpayedText() {
      // 当前项目没 spayed 字段：先占位
      return "-";
    },
    petHealthScore() {
      // 当前项目没 healthScore：先用固定值占位
      return 86;
    },
    ctaText() {
      if (this.activeTab === "steps") return "记录步数";
      if (this.activeTab === "water") return "添加记录";
      return "保存记录";
    },
    ctaClass() {
      if (this.activeTab === "steps") return "cta-blue";
      if (this.activeTab === "water") return "cta-green";
      return "cta-purple";
    }
  },
  onShow() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: true });
    this.loadUserInfo();
  },
  onHide() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: false });
  },
  onUnload() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: false });
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync();
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20;
      // 顶部导航高度 = 状态栏高度 + 导航栏内容高度 (约 50px: padding 16rpx*2 + 内容)
      const headerHeight = this.statusBarHeight + 50;
      this.headerHeight = headerHeight;
    } catch (e) {
      this.statusBarHeight = 20;
      this.headerHeight = 70;
    }
    this.loadPets();
    this.loadUserInfo();
  },
  methods: {
    // 加载用户信息
    async loadUserInfo() {
      // 先从本地缓存读取
      const userInfo = uni.getStorageSync('userInfo');
      const token = uni.getStorageSync('token');

      if (userInfo && userInfo.avatar) {
        this.userAvatar = userInfo.avatar;
        this.userName = userInfo.nickname || '小萌宠主人';
      } else if (!token) {
        // 没有 token 且没有缓存，使用默认值
        this.userAvatar = "https://ai-public.mastergo.com/ai/img_res/1774575365924a3K9mP2xQ7vN4rT8wY.jpg";
        this.userName = "小萌宠主人";
      }

      // 如果有 token，从后端获取最新数据
      if (token) {
        try {
          const res = await uni.$request.get('/api/users/profile');
          if (res && res.success) {
            const userData = res.data;
            this.userAvatar = userData.avatar || this.userAvatar;
            this.userName = userData.nickname || this.userName;
            // 更新缓存
            uni.setStorageSync('userInfo', userData);
          }
        } catch (e) {
          console.error('获取用户信息失败:', e);
        }
      }
    },
    async loadPets() {
      try {
        const res = await uni.$request.get('/api/pets');
        if (res && res.success && Array.isArray(res.data)) {
          this.pets = res.data;
          this.currentPet = this.currentPet || this.pets[0] || null;
          if (this.currentPet) {
            this.loadWeightTrend();
          }
        } else {
          this.pets = [];
          if (this.pets.length === 0) {
            this.currentPet = null;
          }
        }
      } catch (e) {
        console.error('加载宠物列表失败:', e);
        this.pets = [];
        if (this.pets.length === 0) {
          this.currentPet = null;
        }
      }
    },
    // 加载体重趋势
    async loadWeightTrend() {
      if (!this.currentPet || !this.currentPet.id) return;
      
      try {
        const res = await uni.$request.get(`/api/pets/${this.currentPet.id}/weight-records`);
        if (res && res.success && Array.isArray(res.data)) {
          const records = res.data.slice(0, 7).reverse(); // 取最近7条记录
          
          // 找出最大体重用于计算高度
          const weights = records.map(r => r.weight);
          const maxWeight = Math.max(...weights, 1);
          
          this.weightHistory = records.map((record, index) => {
            const date = new Date(record.recordDate);
            const dateStr = `${date.getMonth() + 1}/${date.getDate()}`;
            const height = Math.max(20, (record.weight / maxWeight) * 80); // 高度 20-80
            
            // 根据体重变化设置颜色
            let color = '#10b981'; // 默认绿色
            if (index > 0) {
              const prev = records[index - 1];
              if (record.weight > prev.weight) {
                color = '#ef4444'; // 增加 - 红色
              } else if (record.weight < prev.weight) {
                color = '#3b82f6'; // 减少 - 蓝色
              }
            }
            
            return {
              date: dateStr,
              h: height,
              color: color
            };
          });
        }
      } catch (e) {
        console.error('加载体重趋势失败:', e);
      }
    },
    togglePetSelector() {
      this.petSelectorOpen = !this.petSelectorOpen;
    },
    closePetSelector() {
      this.petSelectorOpen = false;
    },
    selectPet(pet) {
      this.currentPet = pet;
      this.petSelectorOpen = false;
      this.searchQuery = "";
      this.loadWeightTrend();
    },
    onMore() {
      uni.showToast({ title: "未实现", icon: "none" });
    },
    goBackToBoard() {
      uni.switchTab({ url: "/pages/dashboard/index" });
    },
    onSettings() {
      uni.showToast({ title: "未实现", icon: "none" });
    },
    syncDevice() {
      uni.showToast({ title: "未实现", icon: "none" });
    },
    selectWaterAmount(opt) {
      if (!opt) return;
      const m = String(opt.amount || "");
      const value = m.replace(/[^0-9.]/g, "");
      this.waterData.value = value;
      this.waterData.unit = m.toLowerCase().includes("l") && !m.toLowerCase().includes("ml") ? "L" : "ml";
    },
    toggleWaterUnit() {
      this.waterData.unit = this.waterData.unit === "ml" ? "L" : "ml";
    },
    async saveRecord() {
      if (!this.currentPet || !this.currentPet.id) {
        uni.showToast({ title: "请先选择宠物", icon: "none" });
        return;
      }
      if (this.activeTab === "weight") {
        if (!this.weightData.value) {
          uni.showToast({ title: "请输入体重", icon: "none" });
          return;
        }
        try {
          const res = await uni.$request.post(`/api/pets/${this.currentPet.id}/weight-records`, {
            weight: this.weightData.value,
            recordDate: `${this.weightData.date}`
          });
          if (res && res.success) {
            uni.showToast({ title: "保存成功", icon: "success" });
            this.weightData.value = "";
            this.loadWeightTrend(); // 重新加载体重趋势
          } else {
            uni.showToast({ title: (res && res.message) || "保存失败", icon: "none" });
          }
        } catch {
          uni.showToast({ title: "网络错误", icon: "none" });
        }
        return;
      }
      // steps/water: 后端未接入，先提示
      uni.showToast({ title: "已记录（占位）", icon: "none" });
    }
  }
};
</script>

<style lang="scss" scoped>
.health-page { min-height: 100vh; background: var(--pt-bg); }
.health-nav {
  position: fixed; top: 0; left: 0; right: 0; z-index: 30;
  background: rgba(255,255,255,0.96);
  box-shadow: 0 10rpx 30rpx rgba(17,24,39,0.06);
}
.health-statusbar { width: 100%; }
.health-nav-inner {
  padding: 16rpx 20rpx 14rpx;
  display: flex; align-items: center; justify-content: space-between;
}
.health-user { display:flex; align-items:center; gap: 14rpx; }
.health-user-avatar { width: 56rpx; height: 56rpx; border-radius: 28rpx; background:#e5e7eb; }
.health-user-name { font-size: 28rpx; font-weight: 800; color:#111827; }
.health-nav-actions { display:flex; gap: 18rpx; }
.health-nav-icon { font-size: 34rpx; color:#6b7280; padding: 6rpx 10rpx; }

.health-scroll {
  height: 100vh;
  /* padding-top 改为动态设置 */
}
.health-content {
  padding: 20rpx 20rpx 180rpx;
}

.pet-selector { position: relative; margin-bottom: 24rpx; }
.pet-selector-card {
  background: rgba(255,255,255,0.98);
  border-radius: 22rpx;
  box-shadow: var(--pt-shadow-soft);
  padding: 22rpx 20rpx;
  display:flex; align-items:center; justify-content:space-between;
}
.pet-selector-left { display:flex; align-items:center; gap: 16rpx; }
.pet-selector-avatar { width: 72rpx; height: 72rpx; border-radius: 36rpx; background:#e5e7eb; }
.pet-selector-meta { display:flex; flex-direction:column; }
.pet-selector-name { font-size: 30rpx; font-weight: 900; color:#111827; }
.pet-selector-breed { margin-top: 6rpx; font-size: 22rpx; color:#6b7280; }
.pet-selector-arrow { font-size: 30rpx; color:#6b7280; }

.pet-selector-pop {
  position: absolute; top: 100%; left: 0; right: 0;
  margin-top: 12rpx;
  background: #fff;
  border-radius: 22rpx;
  box-shadow: 0 18rpx 44rpx rgba(0,0,0,0.12);
  z-index: 50;
  overflow: hidden;
}
.pet-selector-search { padding: 16rpx; border-bottom: 1rpx solid rgba(17,24,39,0.08); }
.pet-selector-input {
  background: #f9fafb;
  border-radius: 16rpx;
  padding: 18rpx 16rpx;
  font-size: 26rpx;
}
.pet-selector-list { max-height: 360rpx; }
.pet-selector-item { display:flex; align-items:center; gap: 14rpx; padding: 16rpx; }
.pet-selector-item-avatar { width: 56rpx; height: 56rpx; border-radius: 28rpx; background:#e5e7eb; }
.pet-selector-item-meta { display:flex; flex-direction:column; }
.pet-selector-item-name { font-size: 28rpx; font-weight: 800; color:#111827; }
.pet-selector-item-breed { margin-top: 4rpx; font-size: 22rpx; color:#6b7280; }

.core-row { margin-bottom: 22rpx; }
.core-row-inner { display:flex; gap: 14rpx; padding-bottom: 10rpx; }
.core-card {
  flex-shrink: 0;
  width: 168rpx;
  background: rgba(255,255,255,0.98);
  border-radius: 22rpx;
  box-shadow: var(--pt-shadow-soft);
  padding: 20rpx 16rpx;
  display:flex; flex-direction:column; align-items:center; justify-content:center;
}
.core-card-wide { width: 210rpx; }
.core-value { font-size: 40rpx; font-weight: 900; }
.core-label { margin-top: 8rpx; font-size: 22rpx; color:#6b7280; }
.core-blue { color:#3b82f6; }
.core-green { color:#10b981; }
.core-orange { color:#f59e0b; }
.score-circle {
  width: 96rpx; height: 96rpx; border-radius: 48rpx;
  border: 6rpx solid rgba(59,130,246,0.35);
  display:flex; align-items:center; justify-content:center;
  margin-bottom: 8rpx;
}
.score-text { font-size: 24rpx; font-weight: 900; color:#2563eb; }

.seg {
  background: rgba(243,244,246,1);
  border-radius: 16rpx;
  padding: 6rpx;
  display:flex;
  margin-bottom: 18rpx;
}
.seg-item {
  flex: 1;
  text-align: center;
  padding: 14rpx 0;
  border-radius: 14rpx;
  font-size: 24rpx;
  font-weight: 800;
  color:#6b7280;
}
.seg-item.active {
  background: #3b82f6;
  color: #fff;
  box-shadow: 0 10rpx 20rpx rgba(59,130,246,0.25);
}

.panel {
  background: rgba(255,255,255,0.98);
  border-radius: 22rpx;
  box-shadow: var(--pt-shadow-soft);
  padding: 26rpx 22rpx;
}
.panel-label { display:block; font-size: 24rpx; font-weight: 800; color:#374151; margin-bottom: 12rpx; margin-top: 10rpx; }

.section-title {
  display: block;
  font-size: 24rpx;
  font-weight: 900;
  color: #374151;
  margin-bottom: 14rpx;
}

.section-title-gap {
  margin-top: 18rpx;
}

.quick-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;
}

.quick-card {
  border: 2rpx solid rgba(229,231,235,0.9);
  border-radius: 18rpx;
  padding: 16rpx 10rpx 14rpx;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.quick-icon {
  width: 74rpx;
  height: 74rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10rpx;
  font-size: 34rpx;
}

.quick-name {
  font-size: 22rpx;
  font-weight: 800;
  color: #374151;
  margin-bottom: 6rpx;
}

.quick-amount {
  font-size: 22rpx;
  color: #111827;
  font-weight: 900;
}

.input-box {
  border: 2rpx solid rgba(229,231,235,0.9);
  border-radius: 18rpx;
  padding: 10rpx 16rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.input-box-value {
  flex: 1;
  text-align: center;
  font-size: 44rpx;
  font-weight: 900;
  color: #111827;
}

.input-box-unit {
  display: flex;
  align-items: center;
  gap: 6rpx;
  color: #6b7280;
  font-size: 24rpx;
  font-weight: 800;
  padding-left: 10rpx;
}

.unit-chevron {
  font-size: 22rpx;
  color: #9ca3af;
}

.time-box {
  border: 2rpx solid rgba(229,231,235,0.9);
  border-radius: 18rpx;
  padding: 14rpx 16rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.time-pill {
  background: transparent;
  border-radius: 14rpx;
  padding: 8rpx 10rpx;
  font-size: 24rpx;
  color: #111827;
  font-weight: 800;
}

.time-icon {
  margin-left: auto;
  font-size: 26rpx;
  color: #6b7280;
}
.panel-input-row { position: relative; display:flex; align-items:center; justify-content:center; border-bottom: 2rpx solid rgba(209,213,219,0.9); padding: 10rpx 0 12rpx; }
.panel-input-row-sm { border: 2rpx solid rgba(209,213,219,0.7); border-radius: 16rpx; padding: 8rpx 12rpx; margin-top: 6rpx; }
.panel-input-big { width: 100%; text-align: center; font-size: 56rpx; font-weight: 900; color:#111827; }
.panel-input { width: 100%; text-align: center; font-size: 30rpx; font-weight: 800; color:#111827; }
.panel-unit { position: absolute; right: 0; font-size: 24rpx; color:#6b7280; }
.dt-row { display:flex; gap: 12rpx; margin-top: 8rpx; }
.dt-pill {
  background: #f9fafb;
  border: 2rpx solid rgba(209,213,219,0.7);
  border-radius: 16rpx;
  padding: 14rpx 16rpx;
  font-size: 24rpx;
  color:#374151;
  min-width: 240rpx;
  text-align: center;
}
.ghost-btn {
  margin-top: 18rpx;
  background: #f3f4f6;
  border-radius: 16rpx;
  padding: 18rpx 0;
  display:flex; align-items:center; justify-content:center; gap: 10rpx;
  color:#374151;
  font-weight: 800;
}
.ghost-icon { font-size: 28rpx; }
.unit-row { display:flex; gap: 12rpx; margin-top: 12rpx; justify-content:flex-end; }
.unit-pill {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  border: 2rpx solid rgba(209,213,219,0.7);
  color:#6b7280;
  font-size: 22rpx;
  font-weight: 800;
}
.unit-pill.active { background:#10b981; border-color:#10b981; color:#fff; }

.trend-row { display:flex; align-items:flex-end; gap: 12rpx; margin-top: 8rpx; }
.trend-item { flex: 1; display:flex; flex-direction:column; align-items:center; }
.trend-bar { width: 100%; border-radius: 6rpx; background:#e5e7eb; }
.trend-date { margin-top: 8rpx; font-size: 20rpx; color:#6b7280; }

.cta {
  position: fixed; left: 0; right: 0; bottom: 0;
  padding: 18rpx 20rpx calc(18rpx + env(safe-area-inset-bottom));
  background: rgba(255,255,255,0.98);
  border-top: 1rpx solid rgba(17,24,39,0.08);
  z-index: 40;
}
.cta-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}
.cta-btn {
  height: 96rpx;
  border-radius: 22rpx;
  display:flex; align-items:center; justify-content:center;
  box-shadow: 0 18rpx 40rpx rgba(0,0,0,0.12);
}
.cta-main { min-width: 0; }
.cta-back {
  background: #f3f4f6;
  border: 2rpx solid rgba(209,213,219,0.8);
}
.cta-text { color:#fff; font-size: 30rpx; font-weight: 900; }
.cta-text-dark { color:#374151; }
.cta-blue { background: linear-gradient(180deg, #3b82f6 0%, #4f46e5 100%); }
.cta-green { background: linear-gradient(180deg, #10b981 0%, #059669 100%); }
.cta-purple { background: linear-gradient(180deg, #8b5cf6 0%, #ec4899 100%); }
</style>

