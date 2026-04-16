<template>
  <view class="board-page">
    <user-top-bar
      :status-bar-height="statusBarHeight"
      :avatar="userAvatar"
      :name="userName"
      right-icon="⏷"
      @rightTap="onFilter"
      @userTap="onTopUserTap"
    />

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

        <!-- 体重趋势（对齐 pages/test/health 布局） -->
        <view class="dash-section">
          <view class="section-header">
            <text class="section-title">⚖️ 体重趋势</text>
            <view class="chart-switch">
              <text class="switch-text">近7天</text>
            </view>
          </view>

          <view class="dash-card chart-card-inner">
            <view v-if="weightChartHasData" class="chart-wrap chart-wrap--weight">
              <view class="y-axis">
                <text v-for="(t, i) in weightYLabels" :key="'y'+i" class="y-tick">{{ t }}</text>
              </view>
              <view class="chart-plot">
                <view class="grid-lines">
                  <view v-for="n in 5" :key="n" class="grid-line"></view>
                </view>
                <view
                  v-for="(seg, idx) in weightChartSegments"
                  :key="'s'+idx"
                  class="line-seg line-seg--weight"
                  :style="seg"
                ></view>
                <view
                  v-for="(pt, idx) in weightChartPoints"
                  :key="'p'+idx"
                  class="line-dot line-dot--hollow"
                  :style="pt"
                ></view>
                <view class="x-axis">
                  <text v-for="d in chartDates" :key="d" class="x-item">{{ d }}</text>
                </view>
              </view>
            </view>
            <view v-else class="chart-empty">
              <text class="chart-empty-text">近7天暂无体重记录</text>
            </view>

            <view class="chart-stats">
              <view class="stat-item">
                <text class="stat-label">当前体重</text>
                <text class="stat-value">{{ weightStats.current }}</text>
              </view>
              <view class="stat-divider"></view>
              <view class="stat-item">
                <text class="stat-label">平均体重</text>
                <text class="stat-value">{{ weightStats.avg }}</text>
              </view>
              <view class="stat-divider"></view>
              <view class="stat-item">
                <text class="stat-label">变化</text>
                <text
                  class="stat-value"
                  :class="weightStats.deltaNum >= 0 ? 'up' : 'down'"
                >{{ weightStats.delta }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 疫苗提醒（对齐 pages/test/health 布局） -->
        <view class="dash-section">
          <view class="section-header">
            <text class="section-title">💉 疫苗提醒</text>
          </view>

          <view v-if="vaccineCards.length" class="vaccine-list">
            <view
              v-for="item in vaccineCards"
              :key="item.id"
              class="dash-card vaccine-card"
              :class="{ urgent: item.isUrgent }"
            >
              <view class="vaccine-header">
                <view class="vaccine-info">
                  <text class="vaccine-name">{{ item.name }}</text>
                  <text class="vaccine-date">计划日期: {{ item.date }}</text>
                </view>
                <view class="vaccine-countdown">
                  <text class="countdown-number">{{ item.daysLeft }}</text>
                  <text class="countdown-unit">天</text>
                </view>
              </view>

              <view class="vaccine-progress">
                <view class="progress-bar">
                  <view class="progress-fill" :style="{ width: item.progressPercent + '%' }"></view>
                </view>
                <text class="progress-text">{{ item.progressPercent }}%</text>
              </view>

              <view class="vaccine-actions">
                <button
                  class="btn-vaccine"
                  :class="{ completed: item.isCompleted }"
                  :disabled="item.isCompleted"
                  @tap="onMarkVaccineDone(item)"
                >
                  <text class="btn-text">{{ item.isCompleted ? "已完成" : "标记完成" }}</text>
                </button>
              </view>
            </view>
          </view>
          <view v-else class="dash-card vaccine-empty">
            <text class="vaccine-empty-text">暂无疫苗提醒</text>
          </view>
        </view>

        <!-- 驱虫提醒（对齐 pages/test/health 布局） -->
        <view class="dash-section">
          <view class="section-header">
            <text class="section-title">💊 驱虫提醒</text>
          </view>

          <view v-if="parasiteCards.length" class="vaccine-list">
            <view
              v-for="item in parasiteCards"
              :key="item.id"
              class="dash-card vaccine-card"
              :class="{ urgent: item.isUrgent }"
            >
              <view class="vaccine-header">
                <view class="vaccine-info">
                  <text class="vaccine-name">{{ item.name }}</text>
                  <text class="vaccine-date">计划日期: {{ item.date }}</text>
                </view>
                <view class="vaccine-countdown">
                  <text class="countdown-number">{{ item.daysLeft }}</text>
                  <text class="countdown-unit">天</text>
                </view>
              </view>

              <view class="vaccine-progress">
                <view class="progress-bar">
                  <view class="progress-fill" :style="{ width: item.progressPercent + '%' }"></view>
                </view>
                <text class="progress-text">{{ item.progressPercent }}%</text>
              </view>

              <view class="vaccine-actions">
                <button
                  class="btn-vaccine"
                  :class="{ completed: item.isCompleted }"
                  :disabled="item.isCompleted"
                  @tap="onMarkParasiteDone(item)"
                >
                  <text class="btn-text">{{ item.isCompleted ? "已完成" : "标记完成" }}</text>
                </button>
              </view>
            </view>
          </view>
          <view v-else class="dash-card vaccine-empty">
            <text class="vaccine-empty-text">暂无驱虫提醒</text>
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
        <view class="board-tab-item" @tap="goTab('/pages/me/index', true)">
          <text class="board-tab-icon">👤</text>
          <text class="board-tab-text">我的</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import UserTopBar from '@/components/UserTopBar.vue'
import { checkLogin, getUserAvatar, getPetAvatar, DEFAULT_USER_AVATAR, DEFAULT_PET_AVATAR_URL } from '@/utils/index'

export default {
  components: {
    UserTopBar
  },
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 0,
      userName: "小萌宠主人",
      userAvatar: DEFAULT_USER_AVATAR,
      fallbackPetAvatar: DEFAULT_PET_AVATAR_URL,
      pets: [],
      selectedPet: null,
      petSelectorOpen: false,
      searchQuery: "",
      chartDates: [],
      weightSeriesRaw: [],
      weightSeriesFilled: [],
      weightStats: {
        current: "--",
        avg: "--",
        delta: "--",
        deltaNum: 0
      },
      vaccineReminders: [],
      parasiteReminders: []
    };
  },
  computed: {
    weightChartHasData() {
      return (this.weightSeriesFilled || []).some((v) => v != null && !Number.isNaN(v));
    },
    weightChartPoints() {
      const values = this.weightSeriesFilled.filter((v) => v != null && !Number.isNaN(v));
      if (!values.length) return [];
      const min = Math.min(...values);
      const max = Math.max(...values);
      const pad = (max - min) * 0.08 || 0.2;
      const lo = min - pad;
      const hi = max + pad;
      const diff = hi - lo || 1;
      const leftStart = 8;
      const n = this.weightSeriesFilled.length;
      const step = n > 1 ? 520 / (n - 1) : 0;
      const h = 180;
      return this.weightSeriesFilled.map((v, i) => {
        if (v == null || Number.isNaN(v)) return null;
        const y = 24 + (1 - (v - lo) / diff) * h;
        const x = leftStart + i * step;
        return {
          left: `${x}rpx`,
          top: `${y}rpx`,
          borderColor: "#10B981",
          background: "#fff"
        };
      }).filter(Boolean);
    },
    weightChartSegments() {
      const pts = this.weightChartPoints;
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
          background: "#10B981"
        });
      }
      return list;
    },
    weightYLabels() {
      const values = this.weightSeriesFilled.filter((v) => v != null && !Number.isNaN(v));
      if (!values.length) return ["", "", "", "", ""];
      const min = Math.min(...values);
      const max = Math.max(...values);
      const pad = (max - min) * 0.08 || 0.2;
      const lo = min - pad;
      const hi = max + pad;
      const step = (hi - lo) / 4;
      const labels = [];
      for (let i = 4; i >= 0; i--) {
        labels.push((lo + step * i).toFixed(1));
      }
      return labels;
    },
    vaccineCards() {
      const petId = this.selectedPet && this.selectedPet.id;
      if (!petId || !Array.isArray(this.vaccineReminders)) return [];
      const now = new Date();
      return this.vaccineReminders.map((r) => {
        const next = r.nextDate ? new Date(r.nextDate) : null;
        const rawDays = next && !Number.isNaN(next.getTime())
          ? Math.ceil((next - now) / (86400000))
          : 0;
        const done = Number(r.status) === 1;
        return {
          id: r.id,
          name: r.vaccineName || "疫苗",
          date: this.formatDateYMD(r.nextDate),
          daysLeft: Math.max(0, rawDays),
          progressPercent: done ? 100 : 0,
          isCompleted: done,
          isUrgent: !done && rawDays <= 7
        };
      });
    },
    parasiteCards() {
      const petId = this.selectedPet && this.selectedPet.id;
      if (!petId || !Array.isArray(this.parasiteReminders)) return [];
      const now = new Date();
      const typeMap = { 1: "体内驱虫", 2: "体外驱虫", 3: "内外同驱" };
      return this.parasiteReminders.map((r) => {
        const next = r.nextDate ? new Date(r.nextDate) : null;
        const rawDays = next && !Number.isNaN(next.getTime())
          ? Math.ceil((next - now) / (86400000))
          : 0;
        const done = Number(r.status) === 1;
        return {
          id: r.id,
          name: typeMap[r.type] || "驱虫",
          date: this.formatDateYMD(r.nextDate),
          daysLeft: Math.max(0, rawDays),
          progressPercent: done ? 100 : 0,
          isCompleted: done,
          isUrgent: !done && rawDays <= 7
        };
      });
    }
  },
  onShow() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: false });
    this.loadUserInfo();
    this.loadPets();
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync();
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20;
      this.headerHeight = this.statusBarHeight + 46;
    } catch (e) {
      this.statusBarHeight = 20;
      this.headerHeight = 66;
    }
    this.loadPets();
    this.loadUserInfo();

    uni.$on('loginSuccess', () => {
      this.loadUserInfo()
    })
  },
  onHide() {
    uni.$off('loginSuccess')
  },
  onUnload() {
    uni.$off('loginSuccess')
  },
  methods: {
    formatDateYMD(date) {
      if (!date) return "-";
      const d = new Date(date);
      if (Number.isNaN(d.getTime())) return "-";
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
    },
    async loadUserInfo() {
      const userInfo = uni.getStorageSync("userInfo");
      const token = uni.getStorageSync("token");

      if (userInfo && userInfo.avatar) {
        this.userAvatar = getUserAvatar(userInfo.id, userInfo.avatar);
        this.userName = userInfo.nickname || "小萌宠主人";
      } else if (!token) {
        this.userAvatar = DEFAULT_USER_AVATAR;
        this.userName = "小萌宠主人";
      }

      if (token) {
        try {
          const res = await uni.$request.get("/api/users/profile");
          if (res.success) {
            const userData = res.data;
            this.userAvatar = getUserAvatar(userData.id, userData.avatar);
            this.userName = userData.nickname || this.userName;
            uni.setStorageSync("userInfo", userData);
          }
        } catch (e) {
          console.error("获取用户信息失败:", e);
        }
      }
    },
    async loadPets() {
      try {
        const res = await uni.$request.get("/api/pets");
        if (res && res.success && Array.isArray(res.data)) {
          this.pets = res.data;
          if (!this.pets.length) {
            this.selectedPet = null;
            this.clearBoardData();
            return;
          }
          const still = this.selectedPet && this.pets.find((p) => p.id === this.selectedPet.id);
          this.selectedPet = still || this.pets[0];
          this.loadDashboardData();
        }
      } catch (e) {
        console.error("加载宠物列表失败:", e);
      }
    },
    clearBoardData() {
      this.chartDates = [];
      this.weightSeriesRaw = [];
      this.weightSeriesFilled = [];
      this.weightStats = { current: "--", avg: "--", delta: "--", deltaNum: 0 };
      this.vaccineReminders = [];
      this.parasiteReminders = [];
    },
    async loadDashboardData() {
      if (!this.selectedPet || !this.selectedPet.id) {
        this.clearBoardData();
        return;
      }
      const petId = this.selectedPet.id;

      try {
        const [weightRes, vaccineRes, parasiteRes] = await Promise.all([
          uni.$request.get(`/api/pets/${petId}/weight-records`),
          uni.$request.get(`/api/pets/${petId}/vaccine-reminders`),
          uni.$request.get(`/api/pets/${petId}/parasite-reminders`)
        ]);

        if (vaccineRes && vaccineRes.success && Array.isArray(vaccineRes.data)) {
          this.vaccineReminders = vaccineRes.data;
        } else {
          this.vaccineReminders = [];
        }

        if (parasiteRes && parasiteRes.success && Array.isArray(parasiteRes.data)) {
          this.parasiteReminders = parasiteRes.data;
        } else {
          this.parasiteReminders = [];
        }

        if (!weightRes || !weightRes.success || !Array.isArray(weightRes.data)) {
          this.chartDates = [];
          this.weightSeriesRaw = [];
          this.weightSeriesFilled = [];
          this.weightStats = { current: "--", avg: "--", delta: "--", deltaNum: 0 };
          return;
        }

        const weightRecords = [...weightRes.data].sort((a, b) => {
          const da = new Date(a.recordDate).getTime();
          const db = new Date(b.recordDate).getTime();
          return db - da;
        });

        const dates = [];
        const raw = [];
        const today = new Date();

        for (let i = 6; i >= 0; i--) {
          const date = new Date(today);
          date.setDate(date.getDate() - i);
          const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;

          if (i === 0) dates.push("今天");
          else if (i === 1) dates.push("昨天");
          else dates.push(`${date.getMonth() + 1}/${date.getDate()}`);

          const record = weightRecords.find((r) => {
            const recordDate = new Date(r.recordDate);
            return recordDate.toISOString().split("T")[0] === dateStr;
          });
          raw.push(record ? Number(record.weight) : null);
        }

        let last = null;
        const filled = raw.map((v) => {
          if (v != null && !Number.isNaN(v)) {
            last = v;
            return v;
          }
          return last;
        });

        this.chartDates = dates;
        this.weightSeriesRaw = raw;
        this.weightSeriesFilled = filled;

        const nums = raw.filter((v) => v != null && !Number.isNaN(v));
        if (weightRecords.length > 0) {
          const latest = weightRecords[0];
          this.weightStats.current = `${latest.weight} kg`;
        } else {
          this.weightStats.current = "--";
        }

        if (nums.length) {
          const sum = nums.reduce((a, b) => a + b, 0);
          this.weightStats.avg = `${(sum / nums.length).toFixed(2)} kg`;
        } else {
          this.weightStats.avg = "--";
        }

        if (weightRecords.length > 1) {
          const latest = Number(weightRecords[0].weight);
          const prev = Number(weightRecords[1].weight);
          const d = latest - prev;
          this.weightStats.deltaNum = d;
          this.weightStats.delta = `${d > 0 ? "+" : ""}${d.toFixed(1)} kg`;
        } else {
          this.weightStats.deltaNum = 0;
          this.weightStats.delta = "--";
        }
      } catch (e) {
        console.error("加载看板数据失败:", e);
      }
    },
    async onMarkVaccineDone(item) {
      if (!item || item.isCompleted || !this.selectedPet) return;
      
      const loggedIn = await checkLogin('请先登录后再操作')
      if (!loggedIn) return

      uni.showModal({
        title: "确认完成",
        content: `确定要将"${item.name}"标记为已完成吗？`,
        confirmText: "确定",
        cancelText: "取消",
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await uni.$request.put(
                `/api/pets/${this.selectedPet.id}/vaccine-reminders/${item.id}/status`,
                { status: 1 }
              );
              if (result.success) {
                uni.showToast({ title: "已标记完成", icon: "success" });
                this.loadDashboardData();
              }
            } catch (e) {
              console.error("更新疫苗状态失败:", e);
              uni.showToast({ title: "操作失败", icon: "none" });
            }
          }
        }
      });
    },
    async onMarkParasiteDone(item) {
      if (!item || item.isCompleted || !this.selectedPet) return;
      
      const loggedIn = await checkLogin('请先登录后再操作')
      if (!loggedIn) return

      uni.showModal({
        title: "确认完成",
        content: `确定要将"${item.name}"标记为已完成吗？`,
        confirmText: "确定",
        cancelText: "取消",
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await uni.$request.put(
                `/api/pets/${this.selectedPet.id}/parasite-reminders/${item.id}/status`,
                { status: 1 }
              );
              if (result.success) {
                uni.showToast({ title: "已标记完成", icon: "success" });
                this.loadDashboardData();
              }
            } catch (e) {
              console.error("更新驱虫状态失败:", e);
              uni.showToast({ title: "操作失败", icon: "none" });
            }
          }
        }
      });
    },
    selectPet(pet) {
      this.selectedPet = pet;
      this.petSelectorOpen = false;
      this.searchQuery = "";
      this.loadDashboardData();
    },
    togglePetSelector() {
      this.petSelectorOpen = !this.petSelectorOpen;
    },
    closePetSelector() {
      this.petSelectorOpen = false;
    },
    onFilter() {
      uni.showToast({ title: "筛选未实现", icon: "none" });
    },
    onTopUserTap() {
      const token = uni.getStorageSync("token");
      if (!token) {
        uni.showToast({ title: "请先登录", icon: "none" });
        return;
      }
      const userInfo = uni.getStorageSync("userInfo");
      if (userInfo && userInfo.id) {
        uni.navigateTo({ url: `/pages/user/detail?id=${userInfo.id}` });
      }
    },
    goTab(url, isTab) {
      if (isTab) {
        uni.switchTab({ url });
      } else {
        uni.navigateTo({ url });
      }
    },
    async addRecord() {
      const loggedIn = await checkLogin('请先登录后再添加记录')
      if (!loggedIn) return
      uni.navigateTo({ url: "/pages/health/index" });
    }
  }
};
</script>

<style lang="scss" scoped>
.board-page {
  min-height: 100vh;
  background: var(--pt-bg, #f7f3ef);
}

.board-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 40;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 8rpx 24rpx rgba(17, 24, 39, 0.08);
}
.board-statusbar {
  width: 100%;
}
.board-nav-inner {
  padding: 12rpx 20rpx 14rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.board-user {
  display: flex;
  align-items: center;
  gap: 14rpx;
}
.board-user-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 28rpx;
  background: #e5e7eb;
}
.board-user-name {
  font-size: 30rpx;
  font-weight: 800;
  color: #111827;
}
.board-filter {
  font-size: 30rpx;
  color: #6b7280;
}

.board-scroll {
  height: 100vh;
  box-sizing: border-box;
}
.board-content {
  padding: 20rpx 20rpx 220rpx;
}

.pet-selector {
  position: relative;
  margin-bottom: 24rpx;
}
.pet-selector-card {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 22rpx;
  box-shadow: var(--pt-shadow-soft);
  padding: 22rpx 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.pet-selector-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.pet-selector-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 36rpx;
  background: #e5e7eb;
}
.pet-selector-meta {
  display: flex;
  flex-direction: column;
}
.pet-selector-name {
  font-size: 30rpx;
  font-weight: 900;
  color: #111827;
}
.pet-selector-breed {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #6b7280;
}
.pet-selector-arrow {
  font-size: 30rpx;
  color: #6b7280;
}

.pet-selector-pop {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 12rpx;
  background: #fff;
  border-radius: 22rpx;
  box-shadow: 0 18rpx 44rpx rgba(0, 0, 0, 0.12);
  z-index: 50;
  overflow: hidden;
}
.pet-selector-search {
  padding: 16rpx;
  border-bottom: 1rpx solid rgba(17, 24, 39, 0.08);
}
.pet-selector-input {
  background: #f9fafb;
  border-radius: 16rpx;
  padding: 18rpx 16rpx;
  font-size: 26rpx;
}
.pet-selector-list {
  max-height: 360rpx;
}
.pet-selector-item {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 16rpx;
}
.pet-selector-item-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 28rpx;
  background: #e5e7eb;
}
.pet-selector-item-meta {
  display: flex;
  flex-direction: column;
}
.pet-selector-item-name {
  font-size: 28rpx;
  font-weight: 800;
  color: #111827;
}
.pet-selector-item-breed {
  margin-top: 4rpx;
  font-size: 22rpx;
  color: #6b7280;
}

/* —— pages/test/health 风格区块 —— */
.dash-section {
  margin-bottom: 32rpx;
}

.section-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
  padding: 0 8rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #111827;
}

.chart-switch {
  background: #fff4e6;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
}

.switch-text {
  font-size: 24rpx;
  color: #ea580c;
  font-weight: 500;
}

.dash-card {
  background: #fff;
  border-radius: 24rpx;
  box-shadow: var(--pt-shadow-soft);
}

.chart-card-inner {
  padding: 24rpx;
}

.chart-wrap--weight {
  display: flex;
  flex-direction: row;
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.y-axis {
  width: 56rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 16rpx 0 38rpx;
}

.y-tick {
  font-size: 18rpx;
  color: #9ca3af;
  text-align: right;
}

.chart-plot {
  position: relative;
  flex: 1;
  height: 280rpx;
  border-radius: 12rpx;
  padding: 16rpx 8rpx 38rpx;
  overflow: hidden;
  background: #fafafa;
}

.chart-empty {
  height: 280rpx;
  border-radius: 12rpx;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}

.chart-empty-text {
  font-size: 26rpx;
  color: #9ca3af;
}

.grid-lines {
  position: absolute;
  left: 8rpx;
  right: 8rpx;
  top: 16rpx;
  bottom: 38rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.grid-line {
  height: 1rpx;
  background: rgba(209, 213, 219, 0.65);
}

.line-seg {
  position: absolute;
  height: 4rpx;
  border-radius: 4rpx;
  transform-origin: left center;
}

.line-dot {
  position: absolute;
  width: 14rpx;
  height: 14rpx;
  border-radius: 7rpx;
  transform: translate(-7rpx, -7rpx);
  box-sizing: border-box;
}

.line-dot--hollow {
  border-width: 3rpx;
  border-style: solid;
}

.x-axis {
  position: absolute;
  left: 8rpx;
  right: 8rpx;
  bottom: 8rpx;
  display: flex;
  justify-content: space-between;
}
.x-item {
  font-size: 20rpx;
  color: #6b7280;
}

.chart-stats {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-around;
  padding-top: 24rpx;
  border-top: 1rpx solid rgba(17, 24, 39, 0.08);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-label {
  font-size: 22rpx;
  color: #6b7280;
  margin-bottom: 8rpx;
}

.stat-value {
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
}

.stat-value.up {
  color: #10b981;
}

.stat-value.down {
  color: #ff6b6b;
}

.stat-divider {
  width: 1rpx;
  height: 60rpx;
  background: rgba(17, 24, 39, 0.08);
}

.vaccine-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.vaccine-card {
  padding: 24rpx;
}

.vaccine-card.urgent {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
  border: 2rpx solid #ffd0d0;
}

.vaccine-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.vaccine-info {
  flex: 1;
  padding-right: 16rpx;
}

.vaccine-name {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
  margin-bottom: 8rpx;
}

.vaccine-date {
  display: block;
  font-size: 22rpx;
  color: #6b7280;
}

.vaccine-countdown {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  background: #d1fae5;
  padding: 12rpx 20rpx;
  border-radius: 30rpx;
  flex-shrink: 0;
}

.vaccine-card.urgent .vaccine-countdown {
  background: #ff6b6b;
}

.countdown-number {
  font-size: 36rpx;
  font-weight: 700;
  color: #111827;
  margin-right: 4rpx;
}

.vaccine-card.urgent .countdown-number,
.vaccine-card.urgent .countdown-unit {
  color: #ffffff;
}

.countdown-unit {
  font-size: 22rpx;
  color: #6b7280;
}

.vaccine-progress {
  margin-bottom: 20rpx;
}

.progress-bar {
  height: 12rpx;
  background: #fff4e6;
  border-radius: 6rpx;
  overflow: hidden;
  margin-bottom: 8rpx;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #d1fae5 0%, #10b981 100%);
  border-radius: 6rpx;
}

.progress-text {
  font-size: 20rpx;
  color: #9ca3af;
  text-align: right;
  display: block;
}

.vaccine-actions {
  display: flex;
  justify-content: center;
}

.btn-vaccine {
  background: linear-gradient(135deg, #ea580c 0%, #fb923c 100%);
  color: #ffffff;
  border: none;
  border-radius: 999rpx;
  padding: 16rpx 48rpx;
  font-size: 26rpx;
  font-weight: 500;
  line-height: 1.2;
}

.btn-vaccine.completed {
  background: #d1fae5;
  opacity: 0.9;
}

.btn-text {
  color: #ffffff;
}

.btn-vaccine.completed .btn-text {
  color: #047857;
}

.vaccine-empty {
  padding: 48rpx 24rpx;
  text-align: center;
}

.vaccine-empty-text {
  font-size: 26rpx;
  color: #9ca3af;
}

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
  box-shadow: 0 16rpx 34rpx rgba(59, 130, 246, 0.35);
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
.board-tab-item.active {
  color: #ff6a3d;
}
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
