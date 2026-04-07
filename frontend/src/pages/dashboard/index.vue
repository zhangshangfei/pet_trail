<template>
  <view class="board-page">
    <view class="board-nav">
      <view class="board-statusbar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="board-nav-inner">
        <view class="board-user">
          <image class="board-user-avatar" :src="userAvatar" mode="aspectFill" />
          <text class="board-user-name">{{ userName }}</text>
        </view>
        <text class="board-filter" @tap="onFilter">⏷</text>
      </view>
    </view>

    <scroll-view scroll-y class="board-scroll" :style="{ paddingTop: headerHeight + 'px' }">
      <view class="board-content">
        <view class="pet-selector">
          <view class="pet-selector-card" @touchstart.stop="togglePetSelector">
            <view class="pet-selector-left">
              <image
                class="pet-selector-avatar"
                :src="selectedPet && selectedPet.avatar ? selectedPet.avatar : fallbackPetAvatar"
                mode="aspectFill"
              />
              <view class="pet-selector-meta">
                <text class="pet-selector-name">{{ selectedPet ? selectedPet.name : "请选择宠物" }}</text>
                <text class="pet-selector-breed">{{ selectedPet ? (selectedPet.breed || "") : "" }}</text>
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

        <view class="time-row">
          <text class="time-label">统计时间</text>
          <view class="time-right">
            <text class="time-range">{{ selectedTimeRange }}</text>
            <text class="time-cal">📅</text>
          </view>
        </view>

        <view class="overview-grid">
          <view class="ov-card">
            <view class="ov-head">
              <text class="ov-ico">🐾</text>
              <text class="ov-up">+12%</text>
            </view>
            <text class="ov-value">{{ overview.steps }}</text>
            <text class="ov-label">总步数</text>
          </view>
          <view class="ov-card">
            <view class="ov-head">
              <text class="ov-ico">💧</text>
              <text class="ov-down">-8%</text>
            </view>
            <text class="ov-value">{{ overview.water }}</text>
            <text class="ov-label">饮水量</text>
          </view>
          <view class="ov-card">
            <view class="ov-head">
              <text class="ov-ico">⚖️</text>
              <text class="ov-up">{{ overview.deltaWeight }}</text>
            </view>
            <text class="ov-value">{{ overview.weight }}</text>
            <text class="ov-label">当前体重</text>
          </view>
        </view>

        <view class="chart-card">
          <view class="chart-tabs">
            <view
              v-for="tab in chartTabs"
              :key="tab"
              class="chart-tab"
              :class="{ active: selectedChartTab === tab }"
              @tap="selectChartTab(tab)"
            >
              <text>{{ tab }}</text>
            </view>
          </view>

          <view class="chart-wrap">
            <view class="grid-lines">
              <view v-for="n in 5" :key="n" class="grid-line"></view>
            </view>
            <view
              v-for="(seg, idx) in chartSegments"
              :key="'s'+idx"
              class="line-seg"
              :style="seg"
            ></view>
            <view
              v-for="(pt, idx) in chartPoints"
              :key="'p'+idx"
              class="line-dot"
              :style="pt"
            ></view>
            <view class="x-axis">
              <text v-for="d in chartDates" :key="d" class="x-item">{{ d }}</text>
            </view>
          </view>
        </view>

        <view class="record-title">详细记录</view>
        <view class="record-list">
          <view class="record-item" v-for="r in detailRecords" :key="r.date">
            <view class="record-left">
              <text class="record-date">{{ r.date }}</text>
              <view class="record-meta">
                <text class="meta-item">🐾 {{ r.steps }} 步</text>
                <text class="meta-item">💧 {{ r.water }} ml</text>
                <text class="meta-item">⚖️ {{ r.weight }} kg</text>
              </view>
            </view>
            <text class="record-arrow">›</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="fab" @tap="addRecord">
      <text>＋</text>
    </view>

    <view class="board-tabbar-safe">
      <view class="board-tabbar">
        <view class="board-tab-item" @tap="goTab('/pages/home/index', true)">
          <text class="board-tab-icon">⌂</text>
          <text class="board-tab-text">首页</text>
        </view>
        <view class="board-tab-item active">
          <text class="board-tab-icon">📈</text>
          <text class="board-tab-text">健康</text>
        </view>
        <!-- 社区功能暂时隐藏
        <view class="board-tab-item" @tap="goTab('/pages/community/index', true)">
          <text class="board-tab-icon">🐾</text>
          <text class="board-tab-text">社区</text>
        </view>
        -->
        <view class="board-tab-item" @tap="goTab('/pages/me/index', true)">
          <text class="board-tab-icon">👤</text>
          <text class="board-tab-text">我的</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 0,
      userName: "小萌宠主人",
      userAvatar: "https://ai-public.mastergo.com/ai/img_res/1774575365924a3K9mP2xQ7vN4rT8wY.jpg",
      fallbackPetAvatar: "https://ai-public.mastergo.com/ai/img_res/1774575365924b4L8nQ3xR6vM9wP2yZ.jpg",
      pets: [],
      selectedPet: null,
      petSelectorOpen: false,
      searchQuery: "",
      selectedTimeRange: "近七天",
      selectedChartTab: "步数趋势",
      chartTabs: ["步数趋势", "饮水分析", "体重变化"],
      chartDates: ["4/21", "4/22", "4/23", "4/24", "4/25", "4/26", "今天"],
      chartDataMap: {
        "步数趋势": [18.5, 21.2, 19.8, 22.5, 20.3, 21.8, 21.5],
        "饮水分析": [1.1, 1.3, 1.2, 1.35, 1.18, 1.25, 1.2],
        "体重变化": [28.5, 28.4, 28.3, 28.2, 28.4, 28.3, 28.3]
      },
      detailRecords: [
        { date: "今天", steps: "3,450", water: "280", weight: "28.3" },
        { date: "昨天", steps: "2,980", water: "320", weight: "28.5" },
        { date: "前天", steps: "4,120", water: "250", weight: "28.4" },
        { date: "4 月 26 日", steps: "3,760", water: "290", weight: "28.3" },
        { date: "4 月 25 日", steps: "5,230", water: "310", weight: "28.6" }
      ],
      overview: {
        steps: "21.5k",
        water: "1.2L",
        weight: "28.3kg",
        deltaWeight: "-0.5kg"
      }
    };
  },
  computed: {
    activeSeries() {
      return this.chartDataMap[this.selectedChartTab] || [];
    },
    chartColor() {
      if (this.selectedChartTab === "饮水分析") return "#10B981";
      if (this.selectedChartTab === "体重变化") return "#F59E0B";
      return "#3B82F6";
    },
    chartPoints() {
      const values = this.activeSeries;
      if (!values.length) return [];
      const min = Math.min(...values);
      const max = Math.max(...values);
      const diff = max - min || 1;
      const leftStart = 16;
      const step = 88;
      const h = 180;
      return values.map((v, i) => {
        const y = 24 + (1 - (v - min) / diff) * h;
        const x = leftStart + i * step;
        return {
          left: `${x}rpx`,
          top: `${y}rpx`,
          background: this.chartColor
        };
      });
    },
    chartSegments() {
      const pts = this.chartPoints;
      const list = [];
      for (let i = 0; i < pts.length - 1; i++) {
        const x1 = parseFloat(pts[i].left);
        const y1 = parseFloat(pts[i].top);
        const x2 = parseFloat(pts[i + 1].left);
        const y2 = parseFloat(pts[i + 1].top);
        const dx = x2 - x1;
        const dy = y2 - y1;
        const len = Math.sqrt(dx * dx + dy * dy);
        const deg = (Math.atan2(dy, dx) * 180) / Math.PI;
        list.push({
          left: `${x1}rpx`,
          top: `${y1 + 4}rpx`,
          width: `${len}rpx`,
          transform: `rotate(${deg}deg)`,
          background: this.chartColor
        });
      }
      return list;
    }
  },
  onShow() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: false });
    this.loadUserInfo();
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync();
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20;
      const headerHeight = this.statusBarHeight + 46;
      this.headerHeight = headerHeight;
    } catch (e) {
      this.statusBarHeight = 20;
      this.headerHeight = 66;
    }
    this.loadPets();
    this.loadUserInfo();
  },
  methods: {
    async loadUserInfo() {
      const userInfo = uni.getStorageSync('userInfo');
      const token = uni.getStorageSync('token');

      if (userInfo && userInfo.avatar) {
        this.userAvatar = userInfo.avatar;
        this.userName = userInfo.nickname || '小萌宠主人';
      } else if (!token) {
        this.userAvatar = "https://ai-public.mastergo.com/ai/img_res/1774575365924a3K9mP2xQ7vN4rT8wY.jpg";
        this.userName = "小萌宠主人";
      }

      if (token) {
        try {
          const res = await uni.$request.get('/api/users/profile');
          if (res.success) {
            const userData = res.data;
            this.userAvatar = userData.avatar || this.userAvatar;
            this.userName = userData.nickname || this.userName;
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
          if (!this.selectedPet && this.pets.length > 0) {
            this.selectedPet = this.pets[0];
          }
        }
      } catch (e) {
        console.error('加载宠物列表失败:', e);
      }
    },
    selectPet(pet) {
      this.selectedPet = pet;
      this.petSelectorOpen = false;
      this.searchQuery = "";
    },
    togglePetSelector() {
      this.petSelectorOpen = !this.petSelectorOpen;
    },
    closePetSelector() {
      this.petSelectorOpen = false;
    },
    selectChartTab(tab) {
      this.selectedChartTab = tab;
    },
    onFilter() {
      uni.showToast({ title: "筛选未实现", icon: "none" });
    },
    goTab(url, isTab) {
      if (isTab) {
        uni.switchTab({ url });
      } else {
        uni.navigateTo({ url });
      }
    },
    addRecord() {
      uni.navigateTo({ url: "/pages/health/index" });
    }
  }
};
</script>

<style lang="scss" scoped>
.board-page { min-height: 100vh; background: #f8fafc; }
.board-nav {
  position: fixed; top: 0; left: 0; right: 0; z-index: 40;
  background: rgba(255,255,255,0.98);
  box-shadow: 0 8rpx 24rpx rgba(17,24,39,0.08);
}
.board-statusbar { width: 100%; }
.board-nav-inner {
  padding: 12rpx 20rpx 14rpx;
  display: flex; align-items: center; justify-content: space-between;
}
.board-user { display:flex; align-items:center; gap: 14rpx; }
.board-user-avatar { width: 56rpx; height: 56rpx; border-radius: 28rpx; background:#e5e7eb; }
.board-user-name { font-size: 30rpx; font-weight: 800; color:#111827; }
.board-filter { font-size: 30rpx; color:#6b7280; }

.board-scroll {
  height: 100vh;
}
.board-content {
  padding: 20rpx 20rpx 220rpx;
}

.pet-selector { position: relative; margin-bottom: 20rpx; }
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

.time-row { display:flex; justify-content:space-between; align-items:center; margin-bottom: 18rpx; }
.time-label { font-size: 24rpx; color:#6b7280; }
.time-right { display:flex; align-items:center; gap: 8rpx; }
.time-range { font-size: 24rpx; color:#3b82f6; font-weight: 700; }
.time-cal { font-size: 22rpx; }

.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
  margin-bottom: 18rpx;
}
.ov-card {
  background: #fff;
  border-radius: 16rpx;
  border: 1rpx solid rgba(229,231,235,0.9);
  padding: 14rpx 12rpx;
}
.ov-head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 8rpx; }
.ov-ico { font-size: 20rpx; color:#3b82f6; }
.ov-up { font-size: 20rpx; color:#16a34a; }
.ov-down { font-size: 20rpx; color:#dc2626; }
.ov-value { display:block; font-size: 38rpx; font-weight: 900; color:#111827; line-height: 44rpx; }
.ov-label { display:block; margin-top: 2rpx; font-size: 22rpx; color:#6b7280; }

.chart-card {
  background: #fff;
  border-radius: 20rpx;
  border: 1rpx solid rgba(229,231,235,0.9);
  padding: 16rpx;
  margin-bottom: 20rpx;
}
.chart-tabs {
  background: #f3f4f6;
  border-radius: 14rpx;
  padding: 6rpx;
  display: flex;
  gap: 6rpx;
  margin-bottom: 10rpx;
}
.chart-tab {
  flex: 1;
  text-align: center;
  padding: 10rpx 6rpx;
  border-radius: 10rpx;
  font-size: 22rpx;
  color: #6b7280;
  font-weight: 700;
}
.chart-tab.active { background:#fff; color:#2563eb; }

.chart-wrap {
  position: relative;
  height: 280rpx;
  border-radius: 12rpx;
  padding: 16rpx 14rpx 38rpx;
  overflow: hidden;
}
.grid-lines { position:absolute; left:14rpx; right:14rpx; top:16rpx; bottom:38rpx; display:flex; flex-direction:column; justify-content:space-between; }
.grid-line { height: 1rpx; background: rgba(209,213,219,0.65); }
.line-seg { position:absolute; height: 4rpx; border-radius: 4rpx; transform-origin: left center; }
.line-dot { position:absolute; width: 10rpx; height: 10rpx; border-radius: 5rpx; transform: translate(-5rpx,-5rpx); }
.x-axis {
  position: absolute;
  left: 16rpx;
  right: 16rpx;
  bottom: 8rpx;
  display: flex;
  justify-content: space-between;
}
.x-item { font-size: 20rpx; color:#6b7280; }

.record-title { font-size: 28rpx; font-weight: 900; color:#111827; margin-bottom: 12rpx; }
.record-list { display:flex; flex-direction:column; gap: 10rpx; }
.record-item {
  background:#fff;
  border: 1rpx solid rgba(229,231,235,0.9);
  border-radius: 14rpx;
  padding: 14rpx;
  display:flex; align-items:center; justify-content:space-between;
}
.record-left { flex: 1; }
.record-date { display:block; font-size: 26rpx; color:#111827; font-weight: 800; margin-bottom: 8rpx; }
.record-meta { display:flex; gap: 12rpx; flex-wrap: wrap; }
.meta-item { font-size: 22rpx; color:#4b5563; }
.record-arrow { font-size: 30rpx; color:#9ca3af; padding-left: 12rpx; }

.fab {
  position: fixed;
  right: 24rpx;
  bottom: calc(env(safe-area-inset-bottom) + 250rpx);
  width: 96rpx;
  height: 96rpx;
  border-radius: 16rpx;
  background: #3b82f6;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56rpx;
  box-shadow: 0 16rpx 34rpx rgba(59,130,246,0.35);
  z-index: 45;
}

.board-tabbar-safe {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 42;
}
.board-tabbar {
  margin: 0 24rpx 18rpx;
  height: 124rpx;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 64rpx;
  box-shadow: 0 18rpx 40rpx rgba(0, 0, 0, 0.12);
  padding: 0 26rpx;
  display: flex;
  align-items: center;
  justify-content: space-around;
  backdrop-filter: blur(16rpx);
}
.board-tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8b93a6;
}
.board-tab-item.active { color: #ff6a3d; }
.board-tab-icon {
  font-size: 40rpx;
  line-height: 40rpx;
  margin-bottom: 10rpx;
}
.board-tab-text {
  font-size: 24rpx;
  line-height: 24rpx;
  font-weight: 600;
}
</style>
