<template>
  <view class="health-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBackToBoard">
          <text class="nav-back-icon">←</text>
        </view>
        <text class="nav-title">新增记录</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="health-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="health-content">
        <!-- 宠物切换（保留原逻辑） -->
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

          <view v-if="petSelectorOpen" class="pet-selector-pop" @touchstart.stop>
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

        <!-- 核心信息（保留原展示） -->
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

        <!-- Tab：对齐 pages/test/add -->
        <view class="tab-container">
          <view class="tab-wrapper">
            <view
              v-for="(tab, index) in entryTabs"
              :key="tab.type"
              class="tab-item"
              :class="{ active: currentTab === index }"
              @tap="onTabChange(index)"
            >
              <text class="tab-emoji">{{ tab.emoji }}</text>
              <text class="tab-text">{{ tab.name }}</text>
            </view>
          </view>
        </view>

        <!-- 体重 -->
        <view v-show="currentTab === 0" class="form-section">
          <view class="form-card">
            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">📅</text>
                <text class="label-text">记录日期</text>
              </view>
              <picker mode="date" :value="weightForm.date" @change="onWeightDateChange">
                <view class="picker-value">
                  <text class="value-text">{{ weightForm.date }}</text>
                  <text class="picker-arrow">▼</text>
                </view>
              </picker>
            </view>

            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">⚖️</text>
                <text class="label-text">体重 (kg)</text>
              </view>
              <input
                class="form-input"
                type="digit"
                v-model="weightForm.weight"
                placeholder="请输入体重"
              />
            </view>

            <view v-if="lastWeightRecord" class="form-group">
              <view class="last-record">
                <text class="last-label">上次记录</text>
                <view class="last-details">
                  <text class="last-value">{{ lastWeightRecord.weight }} kg</text>
                  <text class="last-date">({{ lastWeightRecord.date }})</text>
                  <text
                    v-if="weightInputDelta !== null"
                    class="weight-change"
                    :class="weightInputDelta >= 0 ? 'up' : 'down'"
                  >
                    {{ weightInputDelta >= 0 ? "+" : "" }}{{ weightInputDelta }} kg {{ weightInputDelta >= 0 ? "↑" : "↓" }}
                  </text>
                </view>
              </view>
            </view>

            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">📝</text>
                <text class="label-text">备注 (可选)</text>
              </view>
              <textarea
                class="form-textarea"
                v-model="weightForm.remark"
                placeholder="记录饮食、运动等情况..."
                maxlength="200"
              />
            </view>
          </view>
        </view>

        <!-- 疫苗 -->
        <view v-show="currentTab === 1" class="form-section">
          <view class="form-card">
            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">💉</text>
                <text class="label-text">疫苗名称</text>
              </view>
              <picker mode="selector" :range="vaccineTypes" :value="vaccineForm.typeIndex" @change="onVaccineTypeChange">
                <view class="picker-value selected">
                  <text class="value-text">{{ vaccineTypes[vaccineForm.typeIndex] }}</text>
                  <text class="picker-arrow">▼</text>
                </view>
              </picker>
            </view>

            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">📅</text>
                <text class="label-text">接种日期</text>
              </view>
              <picker mode="date" :value="vaccineForm.date" @change="onVaccineDateChange">
                <view class="picker-value">
                  <text class="value-text">{{ vaccineForm.date }}</text>
                  <text class="picker-arrow">▼</text>
                </view>
              </picker>
            </view>

            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">🏥</text>
                <text class="label-text">接种医院 (可选)</text>
              </view>
              <input class="form-input" type="text" v-model="vaccineForm.hospital" placeholder="请输入医院名称" />
            </view>

            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">📝</text>
                <text class="label-text">备注 (可选)</text>
              </view>
              <textarea
                class="form-textarea"
                v-model="vaccineForm.remark"
                placeholder="记录疫苗批次、反应情况等..."
                maxlength="200"
              />
            </view>
          </view>
        </view>

        <!-- 驱虫 -->
        <view v-show="currentTab === 2" class="form-section">
          <view class="form-card">
            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">💊</text>
                <text class="label-text">驱虫类型</text>
              </view>
              <picker mode="selector" :range="dewormTypes" :value="dewormForm.typeIndex" @change="onDewormTypeChange">
                <view class="picker-value selected">
                  <text class="value-text">{{ dewormTypes[dewormForm.typeIndex] }}</text>
                  <text class="picker-arrow">▼</text>
                </view>
              </picker>
            </view>

            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">📅</text>
                <text class="label-text">执行日期</text>
              </view>
              <picker mode="date" :value="dewormForm.date" @change="onDewormDateChange">
                <view class="picker-value">
                  <text class="value-text">{{ dewormForm.date }}</text>
                  <text class="picker-arrow">▼</text>
                </view>
              </picker>
            </view>

            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">💊</text>
                <text class="label-text">药品名称 (可选)</text>
              </view>
              <input class="form-input" type="text" v-model="dewormForm.medicine" placeholder="请输入药品名称" />
            </view>

            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">📝</text>
                <text class="label-text">备注 (可选)</text>
              </view>
              <textarea
                class="form-textarea"
                v-model="dewormForm.remark"
                placeholder="记录用药反应、下次计划时间等..."
                maxlength="200"
              />
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="cta-footer">
      <button class="btn-submit" hover-class="btn-submit-hover" @tap="onSubmitCurrent">
        <text class="btn-submit-text">{{ submitButtonText }}</text>
      </button>
    </view>
  </view>
</template>

<script>
import { checkLogin, getUserAvatar, getPetAvatar, DEFAULT_USER_AVATAR, DEFAULT_PET_AVATAR_URL } from '@/utils/index'

function todayStr() {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

export default {
  data() {
    return {
      statusBarHeight: 20,
      userAvatar: DEFAULT_USER_AVATAR,
      userName: "小萌宠主人",
      fallbackPetAvatar: DEFAULT_PET_AVATAR_URL,
      pets: [],
      currentPet: null,
      petSelectorOpen: false,
      searchQuery: "",
      currentTab: 0,
      entryTabs: [
        { type: "weight", name: "体重", emoji: "⚖️" },
        { type: "vaccine", name: "疫苗", emoji: "💉" },
        { type: "deworm", name: "驱虫", emoji: "💊" }
      ],
      weightForm: {
        date: todayStr(),
        weight: "",
        remark: ""
      },
      lastWeightRecord: null,
      vaccineTypes: ["狂犬疫苗", "猫三联", "犬五联", "犬八联", "猫瘟疫苗", "犬瘟热疫苗"],
      vaccineForm: {
        typeIndex: 0,
        date: todayStr(),
        hospital: "",
        remark: ""
      },
      dewormTypes: ["体内驱虫", "体外驱虫", "内外同驱"],
      dewormForm: {
        typeIndex: 0,
        date: todayStr(),
        medicine: "",
        remark: ""
      }
    };
  },
  computed: {
    petAgeText() {
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
      return "-";
    },
    petHealthScore() {
      return 86;
    },
    weightInputDelta() {
      const w = parseFloat(this.weightForm.weight);
      if (!this.lastWeightRecord || Number.isNaN(w)) return null;
      const prev = parseFloat(this.lastWeightRecord.weight);
      if (Number.isNaN(prev)) return null;
      return Math.round((w - prev) * 100) / 100;
    },
    submitButtonText() {
      if (this.currentTab === 0) return "保存体重记录 🐾";
      if (this.currentTab === 1) return "保存疫苗记录 🐾";
      return "保存驱虫记录 🐾";
    }
  },
  onShow() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: true });
    this.loadUserInfo();
    this.loadPets();

    uni.$on('loginSuccess', () => {
      this.loadUserInfo()
    })
  },
  onHide() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: false });
    uni.$off('loginSuccess')
  },
  onUnload() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: false });
    uni.$off('loginSuccess')
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync();
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20;
    } catch (e) {
      this.statusBarHeight = 20;
    }
    this.resetFormDates();
    this.loadPets();
    this.loadUserInfo();
  },
  methods: {
    formatDateYMD(d) {
      if (!d) return "-";
      const x = new Date(d);
      if (Number.isNaN(x.getTime())) return "-";
      return `${x.getFullYear()}-${String(x.getMonth() + 1).padStart(2, "0")}-${String(x.getDate()).padStart(2, "0")}`;
    },
    resetFormDates() {
      const t = todayStr();
      this.weightForm.date = t;
      this.vaccineForm.date = t;
      this.dewormForm.date = t;
    },
    onTabChange(index) {
      this.currentTab = index;
    },
    onWeightDateChange(e) {
      this.weightForm.date = e.detail.value;
    },
    onVaccineTypeChange(e) {
      const v = parseInt(e.detail.value, 10);
      this.vaccineForm.typeIndex = Number.isNaN(v) ? 0 : v;
    },
    onVaccineDateChange(e) {
      this.vaccineForm.date = e.detail.value;
    },
    onDewormTypeChange(e) {
      const v = parseInt(e.detail.value, 10);
      this.dewormForm.typeIndex = Number.isNaN(v) ? 0 : v;
    },
    onDewormDateChange(e) {
      this.dewormForm.date = e.detail.value;
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
          if (res && res.success) {
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
            this.currentPet = null;
            this.lastWeightRecord = null;
            return;
          }
          const still = this.currentPet && this.pets.find((p) => p.id === this.currentPet.id);
          this.currentPet = still || this.pets[0];
          await this.loadLastWeightRecord();
        } else {
          this.pets = [];
          this.currentPet = null;
          this.lastWeightRecord = null;
        }
      } catch (e) {
        console.error("加载宠物列表失败:", e);
        this.pets = [];
        this.currentPet = null;
        this.lastWeightRecord = null;
      }
    },
    async loadLastWeightRecord() {
      if (!this.currentPet || !this.currentPet.id) {
        this.lastWeightRecord = null;
        return;
      }
      try {
        const res = await uni.$request.get(`/api/pets/${this.currentPet.id}/weight-records`);
        if (res && res.success && Array.isArray(res.data) && res.data.length) {
          const r = res.data[0];
          this.lastWeightRecord = {
            weight: r.weight,
            date: this.formatDateYMD(r.recordDate)
          };
        } else {
          this.lastWeightRecord = null;
        }
      } catch (e) {
        console.error("加载上次体重失败:", e);
        this.lastWeightRecord = null;
      }
    },
    togglePetSelector() {
      // 使用防抖，避免快速点击导致多次渲染
      if (this._toggleTimer) {
        clearTimeout(this._toggleTimer);
      }
      this._toggleTimer = setTimeout(() => {
        this.petSelectorOpen = !this.petSelectorOpen;
      }, 50);
    },
    selectPet(pet) {
      // 批量更新状态，避免多次渲染导致闪屏
      this.petSelectorOpen = false;
      this.searchQuery = "";
      
      // 使用 $nextTick 确保 DOM 更新后再加载数据
      this.$nextTick(() => {
        this.currentPet = pet;
        this.loadLastWeightRecord();
      });
    },
    goBackToBoard() {
      uni.switchTab({ url: "/pages/dashboard/index" });
    },
    onSettings() {
      uni.showToast({ title: "未实现", icon: "none" });
    },
    onSubmitCurrent() {
      if (this.currentTab === 0) return this.submitWeight();
      if (this.currentTab === 1) return this.submitVaccine();
      return this.submitDeworm();
    },
    async submitWeight() {
      if (!this.currentPet || !this.currentPet.id) {
        uni.showToast({ title: "请先选择宠物", icon: "none" });
        return;
      }
      const loggedIn = await checkLogin('请先登录后再保存记录')
      if (!loggedIn) return
      if (!this.weightForm.weight) {
        uni.showToast({ title: "请输入体重", icon: "none" });
        return;
      }
      const w = parseFloat(this.weightForm.weight);
      if (Number.isNaN(w) || w <= 0) {
        uni.showToast({ title: "请输入有效体重", icon: "none" });
        return;
      }
      try {
        const res = await uni.$request.post(`/api/pets/${this.currentPet.id}/weight-records`, {
          weight: this.weightForm.weight,
          recordDate: this.weightForm.date,
          note: this.weightForm.remark || null
        });
        if (res && res.success) {
          uni.showToast({ title: "保存成功", icon: "success" });
          this.weightForm.weight = "";
          this.weightForm.remark = "";
          await this.loadLastWeightRecord();
        } else {
          uni.showToast({ title: (res && res.message) || "保存失败", icon: "none" });
        }
      } catch (e) {
        uni.showToast({ title: "网络错误", icon: "none" });
      }
    },
    async submitVaccine() {
      if (!this.currentPet || !this.currentPet.id) {
        uni.showToast({ title: "请先选择宠物", icon: "none" });
        return;
      }
      const loggedIn = await checkLogin('请先登录后再保存记录')
      if (!loggedIn) return
      const vaccineName = this.vaccineTypes[this.vaccineForm.typeIndex];
      try {
        const res = await uni.$request.post(`/api/pets/${this.currentPet.id}/vaccine-reminders`, {
          vaccineName,
          nextDate: this.vaccineForm.date,
          note: this.vaccineForm.remark || null
        });
        if (res && res.success) {
          uni.showToast({ title: "保存成功", icon: "success" });
          this.vaccineForm.typeIndex = 0;
          this.vaccineForm.hospital = "";
          this.vaccineForm.remark = "";
          this.vaccineForm.date = todayStr();
        } else {
          uni.showToast({ title: (res && res.message) || "保存失败", icon: "none" });
        }
      } catch (e) {
        uni.showToast({ title: "网络错误", icon: "none" });
      }
    },
    dewormTypeToApiType(index) {
      if (index === 0) return 1;
      if (index === 1) return 2;
      return 3;
    },
    async submitDeworm() {
      if (!this.currentPet || !this.currentPet.id) {
        uni.showToast({ title: "请先选择宠物", icon: "none" });
        return;
      }
      const loggedIn = await checkLogin('请先登录后再保存记录')
      if (!loggedIn) return
      const type = this.dewormTypeToApiType(this.dewormForm.typeIndex);
      try {
        const res = await uni.$request.post(`/api/pets/${this.currentPet.id}/parasite-reminders`, {
          type,
          nextDate: this.dewormForm.date,
          note: this.dewormForm.remark || null
        });
        if (res && res.success) {
          uni.showToast({ title: "保存成功", icon: "success" });
          this.dewormForm.typeIndex = 0;
          this.dewormForm.medicine = "";
          this.dewormForm.remark = "";
          this.dewormForm.date = todayStr();
        } else {
          uni.showToast({ title: (res && res.message) || "保存失败", icon: "none" });
        }
      } catch (e) {
        uni.showToast({ title: "网络错误", icon: "none" });
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.health-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
}

.status-bar {
  width: 100%;
}

.nav-bar {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16rpx;
}

.nav-back {
  width: 68rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-back-icon {
  font-size: 36rpx;
  color: #fff;
  font-weight: 600;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #fff;
}

.nav-placeholder {
  width: 68rpx;
}

.health-scroll {
  height: 100vh;
  box-sizing: border-box;
}
.health-content {
  padding: 20rpx 24rpx calc(200rpx + env(safe-area-inset-bottom));
}

.pet-selector {
  position: relative;
  margin-bottom: 24rpx;
  z-index: 50;
}
.pet-selector-card {
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
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
  font-weight: 700;
  color: #111827;
}
.pet-selector-breed {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #6b7280;
}
.pet-selector-arrow {
  font-size: 30rpx;
  color: #9ca3af;
}

.pet-selector-pop {
  position: absolute;
  top: calc(100% + 12rpx);
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 18rpx 44rpx rgba(0, 0, 0, 0.12);
  z-index: 50;
  overflow: hidden;
  animation: slideDown 0.2s ease-out;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10rpx); }
  to { opacity: 1; transform: translateY(0); }
}
.pet-selector-search {
  padding: 16rpx;
  border-bottom: 1rpx solid #f3f4f6;
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

  &:active {
    background: #f9fafb;
  }
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
  font-weight: 600;
  color: #111827;
}
.pet-selector-item-breed {
  margin-top: 4rpx;
  font-size: 22rpx;
  color: #6b7280;
}

.core-row {
  margin-bottom: 22rpx;
}
.core-row-inner {
  display: flex;
  gap: 14rpx;
  padding-bottom: 10rpx;
}
.core-card {
  flex-shrink: 0;
  width: 168rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  padding: 20rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.core-card-wide {
  width: 210rpx;
}
.core-value {
  font-size: 40rpx;
  font-weight: 800;
}
.core-label {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #6b7280;
}
.core-blue { color: #3b82f6; }
.core-green { color: #10b981; }
.core-orange { color: #ff7a3d; }
.score-circle {
  width: 96rpx;
  height: 96rpx;
  border-radius: 48rpx;
  border: 6rpx solid rgba(255, 122, 61, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8rpx;
  background: linear-gradient(135deg, rgba(255,122,61,0.08) 0%, rgba(255,77,79,0.05) 100%);
}
.score-text {
  font-size: 24rpx;
  font-weight: 800;
  color: #ff6a3d;
}

.tab-container {
  margin-bottom: 28rpx;
}

.tab-wrapper {
  display: flex;
  flex-direction: row;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  padding: 8rpx;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  padding: 20rpx 12rpx;
  border-radius: 16rpx;
}

.tab-item.active {
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  box-shadow: 0 4rpx 14rpx rgba(255, 106, 61, 0.25);
}

.tab-emoji {
  font-size: 30rpx;
  margin-right: 8rpx;
}

.tab-text {
  font-size: 26rpx;
  font-weight: 500;
  color: #6b7280;
}

.tab-item.active .tab-text {
  color: #fff;
  font-weight: 600;
}

.form-section {
  margin-bottom: 24rpx;
}

.form-card {
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  padding: 32rpx 24rpx;
}

.form-group {
  margin-bottom: 32rpx;
}
.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 16rpx;
}

.label-emoji {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.label-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
}

.form-input {
  width: 100%;
  box-sizing: border-box;
  background: #f9fafb;
  border: none;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  min-height: 80rpx;
  font-size: 28rpx;
  color: #374151;
}

.picker-value {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  background: #f9fafb;
  border: none;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
}

.picker-value.selected {
  background: #fff;
  border: 2rpx solid rgba(255, 122, 61, 0.25);
}

.value-text {
  font-size: 28rpx;
  color: #374151;
}

.picker-arrow {
  font-size: 20rpx;
  color: #9ca3af;
}

.form-textarea {
  width: 100%;
  box-sizing: border-box;
  background: #f9fafb;
  border: none;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  color: #374151;
  min-height: 160rpx;
  line-height: 1.6;
}

.last-record {
  background: linear-gradient(135deg, rgba(255,122,61,0.06) 0%, rgba(255,77,79,0.04) 100%);
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  border: 1rpx solid rgba(255, 122, 61, 0.1);
}

.last-label {
  display: block;
  font-size: 22rpx;
  color: #6b7280;
  margin-bottom: 12rpx;
}

.last-details {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-wrap: wrap;
  gap: 8rpx;
}

.last-value {
  font-size: 30rpx;
  font-weight: 700;
  color: #111827;
}

.last-date {
  font-size: 22rpx;
  color: #9ca3af;
}

.weight-change {
  font-size: 24rpx;
  font-weight: 600;
}

.weight-change.up {
  color: #10b981;
}

.weight-change.down {
  color: #ff4d4f;
}

.cta-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 40;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.btn-submit {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  border-radius: 999rpx;
  border: none;
  padding: 0;
  margin: 0;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  box-shadow: 0 4rpx 12rpx rgba(255, 106, 61, 0.3);

  &:active {
    opacity: 0.92;
    transform: scale(0.98);
  }
}

.btn-submit::after {
  border: none;
}

.btn-submit-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
}
</style>
