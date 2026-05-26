<template>
  <view class="health-page">
    <user-top-bar
      :status-bar-height="statusBarHeight"
      :avatar="userAvatar"
      :name="userName"
      :unread-count="0"
      :show-discover="false"
      :show-bell="false"
      @userTap="goBackToBoard"
    />

    <scroll-view scroll-y class="health-scroll" :style="{ paddingTop: (statusBarHeight + 54) + 'px' }">
      <view class="health-content">
        <!-- 宠物切换（保留原逻辑） -->
        <view class="pet-selector glass-section">
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
            <scroll-view scroll-y class="pet-selector-list">
              <view
                v-for="pet in pets"
                :key="pet.id"
                class="pet-selector-item"
                @touchstart.stop="selectPet(pet)"
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
            <view class="core-card glass-stat-card">
              <text class="core-value core-blue">{{ petAgeText }}</text>
              <text class="core-label">年龄</text>
            </view>
            <view class="core-card glass-stat-card">
              <text class="core-value core-green">{{ petGenderText }}</text>
              <text class="core-label">性别</text>
            </view>
            <view class="core-card glass-stat-card">
              <text class="core-value core-orange">{{ petSpayedText }}</text>
              <text class="core-label">绝育</text>
            </view>
            <view class="core-card core-card-wide glass-stat-card score-card-wrapper">
              <view class="score-circle" :class="scoreCircleClass">
                <text class="score-text">{{ petHealthScore }}</text>
              </view>
              <text class="core-label">健康评分</text>
            </view>
          </view>
        </scroll-view>

        <!-- Tab：对齐 pages/test/add -->
        <view class="tab-container">
          <view class="tab-wrapper glass-tabbar">
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
        <view v-show="currentTab === 0" class="form-section fade-in">
          <view class="form-card glass-card-form">
            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">📅</text>
                <text class="label-text">记录日期</text>
              </view>
              <picker mode="date" :value="weightForm.date" @change="onWeightDateChange">
                <view class="picker-value glass-picker">
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
                class="form-input glass-input"
                type="digit"
                v-model="weightForm.weight"
                placeholder="请输入体重"
              />
            </view>

            <view v-if="lastWeightRecord" class="form-group">
              <view class="last-record glass-hint">
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
                class="form-textarea glass-textarea"
                v-model="weightForm.remark"
                placeholder="记录饮食、运动等情况..."
                maxlength="200"
              />
            </view>
          </view>
        </view>

        <!-- 驱虫 -->
        <view v-show="currentTab === 1" class="form-section fade-in">
          <view class="form-card glass-card-form">
            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">💊</text>
                <text class="label-text">驱虫类型</text>
              </view>
              <picker mode="selector" :range="dewormTypes" :value="dewormForm.typeIndex" @change="onDewormTypeChange">
                <view class="picker-value glass-picker selected">
                  <text class="value-text">{{ dewormTypes[dewormForm.typeIndex] }}</text>
                  <text class="picker-arrow">▼</text>
                </view>
              </picker>
            </view>

            <view v-if="dewormForm.typeIndex === dewormTypes.length - 1" class="form-group">
              <view class="form-label">
                <text class="label-emoji">✏️</text>
                <text class="label-text">自定义驱虫名称</text>
              </view>
              <input class="form-input glass-input" type="text" v-model="dewormForm.customName" placeholder="请输入自定义驱虫名称" maxlength="30" />
            </view>

            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">📅</text>
                <text class="label-text">执行日期</text>
              </view>
              <picker mode="date" :value="dewormForm.date" @change="onDewormDateChange">
                <view class="picker-value glass-picker">
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
              <input class="form-input glass-input" type="text" v-model="dewormForm.medicine" placeholder="请输入药品名称" />
            </view>

            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">📝</text>
                <text class="label-text">备注 (可选)</text>
              </view>
              <textarea
                class="form-textarea glass-textarea"
                v-model="dewormForm.remark"
                placeholder="记录用药反应、下次计划时间等..."
                maxlength="200"
              />
            </view>
          </view>
        </view>

        <!-- 疫苗 -->
        <view v-show="currentTab === 2" class="form-section fade-in">
          <view class="form-card glass-card-form">
            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">💉</text>
                <text class="label-text">疫苗名称</text>
              </view>
              <picker mode="selector" :range="vaccineTypes" :value="vaccineForm.typeIndex" @change="onVaccineTypeChange">
                <view class="picker-value glass-picker selected">
                  <text class="value-text">{{ vaccineTypes[vaccineForm.typeIndex] }}</text>
                  <text class="picker-arrow">▼</text>
                </view>
              </picker>
            </view>

            <view v-if="vaccineForm.typeIndex === vaccineTypes.length - 1" class="form-group">
              <view class="form-label">
                <text class="label-emoji">✏️</text>
                <text class="label-text">自定义疫苗名称</text>
              </view>
              <input class="form-input glass-input" type="text" v-model="vaccineForm.customName" placeholder="请输入自定义疫苗名称" maxlength="30" />
            </view>

            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">📅</text>
                <text class="label-text">接种日期</text>
              </view>
              <picker mode="date" :value="vaccineForm.date" @change="onVaccineDateChange">
                <view class="picker-value glass-picker">
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
              <input class="form-input glass-input" type="text" v-model="vaccineForm.hospital" placeholder="请输入医院名称" />
            </view>

            <view class="form-group">
              <view class="form-label">
                <text class="label-emoji">📝</text>
                <text class="label-text">备注 (可选)</text>
              </view>
              <textarea
                class="form-textarea glass-textarea"
                v-model="vaccineForm.remark"
                placeholder="记录疫苗批次、反应情况等..."
                maxlength="200"
              />
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="cta-footer glass-footer">
      <button class="btn-back glass-btn-secondary" hover-class="btn-back-hover" @tap="goBackToBoard">
        <text class="btn-back-text">返回</text>
      </button>
      <button class="btn-submit glass-btn-primary" :disabled="submitting" hover-class="btn-submit-hover" @tap="onSubmitCurrent">
        <text class="btn-submit-text">{{ submitting ? "提交中..." : submitButtonText }}</text>
      </button>
    </view>
  </view>
</template>

<script>
import { checkLogin, getUserAvatar, getPetAvatar, DEFAULT_USER_AVATAR, DEFAULT_PET_AVATAR_URL, loadWxSubscribeTemplates, requestWxSubscribe } from '@/utils/index'
import UserTopBar from '@/components/UserTopBar.vue'
import * as authApi from '@/api/auth'
import * as petApi from '@/api/pet'
import * as healthApi from '@/api/health'

function todayStr() {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

export default {
  components: { UserTopBar },
  data() {
    return {
      statusBarHeight: 20,
      userAvatar: DEFAULT_USER_AVATAR,
      userName: "",
      fallbackPetAvatar: DEFAULT_PET_AVATAR_URL,
      pets: [],
      currentPet: null,
      petSelectorOpen: false,
      searchQuery: "",
      currentTab: 0,
      entryTabs: [
        { type: "weight", name: "体重", emoji: "⚖️" },
        { type: "deworm", name: "驱虫", emoji: "💊" },
        { type: "vaccine", name: "疫苗", emoji: "💉" }
      ],
      weightForm: {
        date: todayStr(),
        weight: "",
        remark: ""
      },
      lastWeightRecord: null,
      vaccineTypes: ["狂犬疫苗", "猫三联", "犬五联", "犬八联", "猫瘟疫苗", "犬瘟热疫苗", "自定义"],
      vaccineForm: {
        typeIndex: 0,
        customName: "",
        date: todayStr(),
        hospital: "",
        remark: ""
      },
      dewormTypes: ["体内驱虫", "体外驱虫", "内外同驱", "自定义"],
      dewormForm: {
        typeIndex: 0,
        customName: "",
        date: todayStr(),
        medicine: "",
        remark: ""
      },
      healthScore: 0,
      submitting: false
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
      return this.healthScore;
    },
    scoreCircleClass() {
      if (this.healthScore >= 90) return 'score-excellent';
      if (this.healthScore >= 75) return 'score-good';
      if (this.healthScore >= 60) return 'score-fair';
      return 'score-poor';
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
      if (this.currentTab === 1) return "保存驱虫记录 🐾";
      return "保存疫苗记录 🐾";
    }
  },
  onShow() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: true });
    this.loadUserInfo();
    this.loadPets();

    uni.$on('loginSuccess', () => {
      this.loadUserInfo()
      this.loadPets()
    })
  },
  onHide() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: false });
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
    loadWxSubscribeTemplates();
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

      if (userInfo) {
        this.userAvatar = getUserAvatar(userInfo.id, userInfo.avatar);
        this.userName = userInfo.nickname || "";
      } else if (!token) {
        this.userAvatar = DEFAULT_USER_AVATAR;
        this.userName = "";
      }

      if (token) {
        try {
          const res = await authApi.getProfile();
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
        const res = await petApi.getPetList();
        if (res && res.success && Array.isArray(res.data)) {
          this.pets = res.data.map(pet => ({
            ...pet,
            avatar: getPetAvatar(pet.id, pet.avatar)
          }));
          if (!this.pets.length) {
            this.currentPet = null;
            this.lastWeightRecord = null;
            return;
          }
          const still = this.currentPet && this.pets.find((p) => p.id === this.currentPet.id);
          this.currentPet = still || this.pets[0];
          await this.loadLastWeightRecord();
          await this.loadHealthScore();
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
        const res = await healthApi.getWeightRecords(this.currentPet.id);
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
    async loadHealthScore() {
      if (!this.currentPet || !this.currentPet.id) {
        this.healthScore = 0;
        return;
      }
      try {
        const res = await healthApi.getHealthAnalysis(this.currentPet.id);
        if (res && res.success && res.data) {
          this.healthScore = res.data.score || 0;
        } else {
          this.healthScore = 0;
        }
      } catch (e) {
        console.error("加载健康评分失败:", e);
        this.healthScore = 0;
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
        this.loadHealthScore();
      });
    },
    goBackToBoard() {
      uni.switchTab({ url: "/pages/dashboard/index" });
    },
    onSettings() {
      uni.showToast({ title: "未实现", icon: "none" });
    },
    onSubmitCurrent() {
      if (this.submitting) return;
      if (this.currentTab === 0) return this.submitWeight();
      if (this.currentTab === 1) return this.submitDeworm();
      return this.submitVaccine();
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
      this.submitting = true;
      try {
        const res = await healthApi.createWeightRecord(this.currentPet.id, this.weightForm.weight, this.weightForm.date);
        if (res && res.success) {
          uni.showToast({ title: "保存成功", icon: "success" });
          this.weightForm.weight = "";
          this.weightForm.remark = "";
          setTimeout(() => { uni.switchTab({ url: "/pages/dashboard/index" }) }, 1500);
        } else {
          uni.showToast({ title: (res && res.message) || "保存失败", icon: "none" });
        }
      } catch (e) {
        uni.showToast({ title: "网络不给力，请稍后重试", icon: "none" });
      } finally {
        this.submitting = false;
      }
    },
    async submitVaccine() {
      if (!this.currentPet || !this.currentPet.id) {
        uni.showToast({ title: "请先选择宠物", icon: "none" });
        return;
      }
      requestWxSubscribe(['vaccine']);
      const loggedIn = await checkLogin('请先登录后再保存记录')
      if (!loggedIn) return
      const isCustom = this.vaccineForm.typeIndex === this.vaccineTypes.length - 1;
      const vaccineName = isCustom ? this.vaccineForm.customName.trim() : this.vaccineTypes[this.vaccineForm.typeIndex];
      if (!vaccineName) {
        uni.showToast({ title: isCustom ? "请输入自定义疫苗名称" : "请选择疫苗名称", icon: "none" });
        return;
      }
      this.submitting = true;
      try {
        const res = await petApi.createVaccineReminder(this.currentPet.id, {
          vaccineName,
          nextDate: this.vaccineForm.date,
          note: this.vaccineForm.remark || null
        });
        if (res && res.success) {
          uni.showToast({ title: "保存成功", icon: "success" });
          this.vaccineForm.typeIndex = 0;
          this.vaccineForm.customName = "";
          this.vaccineForm.hospital = "";
          this.vaccineForm.remark = "";
          this.vaccineForm.date = todayStr();
          setTimeout(() => { uni.switchTab({ url: "/pages/dashboard/index" }) }, 1500);
        } else {
          uni.showToast({ title: (res && res.message) || "保存失败", icon: "none" });
        }
      } catch (e) {
        uni.showToast({ title: "网络不给力，请稍后重试", icon: "none" });
      } finally {
        this.submitting = false;
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
      requestWxSubscribe(['parasite']);
      const loggedIn = await checkLogin('请先登录后再保存记录')
      if (!loggedIn) return
      const isCustom = this.dewormForm.typeIndex === this.dewormTypes.length - 1;
      const customName = isCustom ? this.dewormForm.customName.trim() : "";
      if (isCustom && !customName) {
        uni.showToast({ title: "请输入自定义驱虫名称", icon: "none" });
        return;
      }
      const type = isCustom ? 0 : this.dewormTypeToApiType(this.dewormForm.typeIndex);
      this.submitting = true;
      try {
        const res = await petApi.createParasiteReminder(this.currentPet.id, {
          type,
          customName: customName || null,
          nextDate: this.dewormForm.date,
          productName: this.dewormForm.medicine || null,
          note: this.dewormForm.remark || null
        });
        if (res && res.success) {
          uni.showToast({ title: "保存成功", icon: "success" });
          this.dewormForm.typeIndex = 0;
          this.dewormForm.customName = "";
          this.dewormForm.medicine = "";
          this.dewormForm.remark = "";
          this.dewormForm.date = todayStr();
          setTimeout(() => { uni.switchTab({ url: "/pages/dashboard/index" }) }, 1500);
        } else {
          uni.showToast({ title: (res && res.message) || "保存失败", icon: "none" });
        }
      } catch (e) {
        uni.showToast({ title: "网络不给力，请稍后重试", icon: "none" });
      } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
/* ============================================
   健康页 - 玻璃拟态风格 (Glassmorphism)
   保持原逻辑不变，全面升级视觉体验
   ============================================ */

.health-page {
  min-height: 100vh;
  background: transparent;
}

/* ====== 滚动区域 ====== */
.health-scroll {
  height: calc(100vh - 280rpx);
  box-sizing: border-box;
}

.health-content {
  padding: 20rpx 24rpx calc(200rpx + env(safe-area-inset-bottom));
}

/* ====== 玻璃通用类 ====== */
.glass-section {
  margin-bottom: 24rpx;
  animation: sectionFadeIn 0.5s ease-out both;
}

@keyframes sectionFadeIn {
  from { opacity: 0; transform: translateY(20rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.glass-card-hover {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-card-hover:hover,
.glass-card-hover:active {
  transform: translateY(-4rpx);
  box-shadow:
    0 16rpx 48rpx rgba(31, 38, 135, 0.18),
    inset 0 1rpx 0 rgba(255, 255, 255, 1);
}

/* ====== 宠物选择器 ====== */
.pet-selector {
  position: relative;
  z-index: 50;
}

.pet-selector-card {
  background: rgba(255, 255, 255, 0.88);
  border-radius: 28rpx;
  box-shadow:
    0 8rpx 32rpx rgba(31, 38, 135, 0.12),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.95),
    inset 0 -1rpx 0 rgba(0, 0, 0, 0.04);
  padding: 26rpx 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  backdrop-filter: blur(24px);
  border: 1rpx solid rgba(255, 255, 255, 0.65);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 12%;
    right: 12%;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 1), transparent);
    pointer-events: none;
  }
}

.pet-selector-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.pet-selector-avatar {
  width: 76rpx;
  height: 76rpx;
  border-radius: 38rpx;
  background: linear-gradient(135deg, #e5e7eb 0%, #f3f4f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.empty-avatar {
  background: linear-gradient(135deg, rgba(255, 122, 61, 0.15) 0%, rgba(255, 160, 122, 0.1) 100%);
}

.empty-text {
  font-size: 36rpx;
}

.pet-selector-meta {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.pet-selector-name {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--pt-text, #111827);
  letter-spacing: 0.5rpx;
}

.pet-selector-breed {
  font-size: 24rpx;
  color: var(--pt-secondary, #666666);
  font-weight: 500;
}

.pet-selector-arrow {
  font-size: 28rpx;
  color: var(--pt-muted, #718096);
  transition: transform 0.3s ease;
}

.pet-selector-pop {
  position: absolute;
  top: calc(100% + 12rpx);
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.94);
  border-radius: 28rpx;
  box-shadow:
    0 20rpx 48rpx rgba(31, 38, 135, 0.2),
    0 4rpx 12rpx rgba(0, 0, 0, 0.08),
    inset 0 1rpx 0 rgba(255, 255, 255, 1);
  z-index: 50;
  overflow: hidden;
  backdrop-filter: blur(24px);
  border: 1rpx solid rgba(255, 255, 255, 0.72);
  animation: slideDown 0.28s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-12rpx) scale(0.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.pet-selector-search {
  padding: 18rpx;
  border-bottom: 1rpx solid rgba(209, 213, 219, 0.3);
  backdrop-filter: blur(8px);
}

.pet-selector-input {
  background: rgba(249, 250, 251, 0.8);
  border-radius: 16rpx;
  padding: 18rpx 20rpx;
  font-size: 26rpx;
  backdrop-filter: blur(8px);
  border: 1rpx solid rgba(209, 213, 219, 0.3);
  transition: all 0.25s ease;
}

.pet-selector-input:focus {
  border-color: var(--pt-primary, #ff6a3d);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 0 4rpx rgba(255, 106, 61, 0.1);
}

.pet-selector-list {
  max-height: 360rpx;
}

.pet-selector-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 18rpx 16rpx;
  transition: all 0.2s ease;

  &:active {
    background: rgba(255, 106, 61, 0.06);
    transform: scale(0.98);
  }
}

.pet-selector-item-avatar {
  width: 58rpx;
  height: 58rpx;
  border-radius: 29rpx;
  background: linear-gradient(135deg, #e5e7eb 0%, #f3f4f6 100%);
}

.pet-selector-item-meta {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.pet-selector-item-name {
  font-size: 29rpx;
  font-weight: 600;
  color: var(--pt-text, #111827);
}

.pet-selector-item-breed {
  font-size: 23rpx;
  color: var(--pt-secondary, #666666);
}

/* ====== 核心信息卡片 ====== */
.core-row {
  margin-bottom: 22rpx;
}

.core-row-inner {
  display: flex;
  gap: 14rpx;
  padding-bottom: 10rpx;
  overflow-x: auto;
}

.glass-stat-card {
  flex-shrink: 0;
  width: 168rpx;
  background: rgba(255, 255, 255, 0.82);
  border-radius: 26rpx;
  box-shadow:
    0 6rpx 24rpx rgba(31, 38, 135, 0.08),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.92);
  padding: 24rpx 18rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(18px);
  border: 1rpx solid rgba(255, 255, 255, 0.62);
  transition: all 0.32s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 15%;
    right: 15%;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.85), transparent);
    pointer-events: none;
  }

  &:hover,
  &:active {
    transform: translateY(-6rpx) scale(1.03);
    box-shadow:
      0 14rpx 36rpx rgba(31, 38, 135, 0.15),
      inset 0 1rpx 0 rgba(255, 255, 255, 1);
  }

  &:active {
    transform: translateY(-2rpx) scale(0.97);
  }
}

.core-card-wide {
  width: 210rpx;
}

.score-card-wrapper {
  background: linear-gradient(
    135deg,
    rgba(255, 122, 61, 0.08) 0%,
    rgba(255, 77, 79, 0.05) 50%,
    rgba(255, 107, 107, 0.06) 100%
  );
}

.core-value {
  font-size: 42rpx;
  font-weight: 800;
  letter-spacing: -0.5rpx;
}

.core-label {
  margin-top: 10rpx;
  font-size: 23rpx;
  color: var(--pt-muted, #718096);
  font-weight: 500;
}

.core-blue { color: #3b82f6; text-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.2); }
.core-green { color: #10b981; text-shadow: 0 2rpx 8rpx rgba(16, 185, 129, 0.2); }
.core-orange { color: #ff7a3d; text-shadow: 0 2rpx 8rpx rgba(255, 122, 61, 0.25); }

.score-circle {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50rpx;
  border: 5rpx solid rgba(255, 122, 61, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10rpx;
  background: radial-gradient(circle at center, rgba(255,122,61,0.1) 0%, transparent 70%);
  box-shadow: 0 4rpx 16rpx rgba(255, 106, 61, 0.1);
  transition: all 0.35s ease;
}

.score-excellent {
  border-color: rgba(52, 199, 89, 0.45);
  background: radial-gradient(circle at center, rgba(52,199,89,0.12) 0%, transparent 70%);
  .score-text { color: #34c759; text-shadow: 0 2rpx 8rpx rgba(52, 199, 89, 0.3); }
}
.score-good {
  border-color: rgba(74, 144, 217, 0.45);
  background: radial-gradient(circle at center, rgba(74,144,217,0.12) 0%, transparent 70%);
  .score-text { color: #4a90d9; text-shadow: 0 2rpx 8rpx rgba(74, 144, 217, 0.3); }
}
.score-fair {
  border-color: rgba(255, 149, 0, 0.45);
  background: radial-gradient(circle at center, rgba(255,149,0,0.12) 0%, transparent 70%);
  .score-text { color: #ff9500; text-shadow: 0 2rpx 8rpx rgba(255, 149, 0, 0.3); }
}
.score-poor {
  border-color: rgba(255, 59, 48, 0.45);
  background: radial-gradient(circle at center, rgba(255,59,48,0.12) 0%, transparent 70%);
  .score-text { color: #ff3b30; text-shadow: 0 2rpx 8rpx rgba(255, 59, 48, 0.3); }
}

.score-text {
  font-size: 28rpx;
  font-weight: 800;
  color: #ff6a3d;
}

/* ====== Tab切换栏 ====== */
.tab-container {
  margin-bottom: 28rpx;
}

.glass-tabbar {
  display: flex;
  flex-direction: row;
  background: rgba(255, 255, 255, 0.75);
  border-radius: 22rpx;
  box-shadow:
    0 4rpx 20rpx rgba(31, 38, 135, 0.08),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.85);
  padding: 8rpx;
  backdrop-filter: blur(16px);
  border: 1rpx solid rgba(255, 255, 255, 0.55);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  padding: 22rpx 12rpx;
  border-radius: 16rpx;
  transition: all 0.32s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  gap: 8rpx;
}

.tab-item.active {
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  box-shadow:
    0 6rpx 20rpx rgba(255, 106, 61, 0.3),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.3);
  transform: scale(1.02);
}

.tab-emoji {
  font-size: 30rpx;
}

.tab-text {
  font-size: 27rpx;
  font-weight: 600;
  color: var(--pt-secondary, #666666);
  letter-spacing: 0.5rpx;
  transition: all 0.25s ease;
}

.tab-item.active .tab-text {
  color: #fff;
  font-weight: 700;
  text-shadow: 0 2rpx 8rpx rgba(180, 30, 10, 0.3);
}

/* ====== 表单区域 ====== */
.form-section {
  margin-bottom: 24rpx;
}

.glass-card-form {
  position: relative;
  background: rgba(255, 255, 255, 0.84);
  border-radius: 30rpx;
  box-shadow:
    0 8rpx 32rpx rgba(31, 38, 135, 0.1),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.92),
    inset 0 -1rpx 0 rgba(0, 0, 0, 0.03);
  padding: 38rpx 30rpx;
  backdrop-filter: blur(22px);
  border: 1rpx solid rgba(255, 255, 255, 0.62);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    right: 10%;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.92), transparent);
    pointer-events: none;
  }
}

.form-group {
  margin-bottom: 36rpx;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 18rpx;
  gap: 10rpx;
}

.label-emoji {
  font-size: 30rpx;
}

.label-text {
  font-size: 29rpx;
  font-weight: 600;
  color: var(--pt-text, #111827);
  letter-spacing: 0.5rpx;
}

/* 输入框 */
.glass-input {
  width: 100%;
  box-sizing: border-box;
  background: rgba(249, 250, 251, 0.65);
  border: 1.5rpx solid rgba(209, 213, 219, 0.35);
  border-radius: 20rpx;
  padding: 24rpx 28rpx;
  min-height: 88rpx;
  font-size: 29rpx;
  color: var(--pt-text, #374151);
  backdrop-filter: blur(10px);
  transition: all 0.28s ease;
}

.glass-input:focus,
.glass-input:focus-within {
  border-color: var(--pt-primary, #ff6a3d);
  box-shadow:
    0 0 0 5rpx rgba(255, 106, 61, 0.12),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.55);
  background: rgba(255, 255, 255, 0.88);
  outline: none;
}

/* 选择器 */
.glass-picker {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  background: rgba(249, 250, 251, 0.65);
  border: 1.5rpx solid rgba(209, 213, 219, 0.35);
  border-radius: 20rpx;
  padding: 24rpx 28rpx;
  backdrop-filter: blur(10px);
  transition: all 0.28s ease;
}

.glass-picker:hover,
.glass-picker:active {
  border-color: var(--pt-primary, #ff6a3d);
  background: rgba(255, 255, 255, 0.88);
  transform: scale(0.99);
}

.glass-picker.selected {
  background: rgba(255, 255, 255, 0.82);
  border: 2rpx solid rgba(255, 122, 61, 0.28);
  box-shadow: 0 2rpx 12rpx rgba(255, 106, 61, 0.08);
}

.value-text {
  font-size: 29rpx;
  color: var(--pt-text, #374151);
  font-weight: 500;
}

.picker-arrow {
  font-size: 22rpx;
  color: var(--pt-muted, #9ca3af);
  transition: transform 0.25s ease;
}

/* 文本域 */
.glass-textarea {
  width: 100%;
  box-sizing: border-box;
  background: rgba(249, 250, 251, 0.65);
  border: 1.5rpx solid rgba(209, 213, 219, 0.35);
  border-radius: 20rpx;
  padding: 24rpx 28rpx;
  font-size: 29rpx;
  color: var(--pt-text, #374151);
  min-height: 170rpx;
  line-height: 1.65;
  backdrop-filter: blur(10px);
  transition: all 0.28s ease;
}

.glass-textarea:focus {
  border-color: var(--pt-primary, #ff6a3d);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 0 0 5rpx rgba(255, 106, 61, 0.1);
}

/* 上次记录提示 */
.glass-hint {
  background: linear-gradient(135deg, rgba(255,122,61,0.07) 0%, rgba(255,77,79,0.05) 100%);
  border-radius: 18rpx;
  padding: 22rpx 26rpx;
  border: 1rpx solid rgba(255, 122, 61, 0.12);
  backdrop-filter: blur(8px);
  margin-top: 16rpx;
}

.last-label {
  display: block;
  font-size: 23rpx;
  color: var(--pt-muted, #718096);
  margin-bottom: 12rpx;
  font-weight: 500;
}

.last-details {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
}

.last-value {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--pt-text, #111827);
}

.last-date {
  font-size: 23rpx;
  color: var(--pt-light, #999999);
}

.weight-change {
  font-size: 25rpx;
  font-weight: 600;
  padding: 4rpx 12rpx;
  border-radius: 10rpx;
}

.weight-change.up {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
}

.weight-change.down {
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.1);
}

/* 表单切换动画 */
.fade-in {
  animation: formFadeIn 0.4s ease-out both;
}

@keyframes formFadeIn {
  from {
    opacity: 0;
    transform: translateX(20rpx);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* ====== 底部操作栏 ====== */
.cta-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 40;
  padding: 18rpx 28rpx calc(18rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(24px);
  box-shadow:
    0 -8rpx 32rpx rgba(31, 38, 135, 0.12),
    0 -2rpx 8rpx rgba(0, 0, 0, 0.04);
  display: flex;
  gap: 20rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.6);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 20%;
    right: 20%;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
    pointer-events: none;
  }
}

.glass-btn-secondary {
  width: 170rpx;
  height: 96rpx;
  line-height: 96rpx;
  border-radius: 48rpx;
  border: none;
  padding: 0;
  margin: 0;
  background: rgba(249, 250, 251, 0.9);
  flex-shrink: 0;
  box-shadow:
    0 4rpx 16rpx rgba(0, 0, 0, 0.06),
    inset 0 1rpx 0 rgba(255, 255, 255, 1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1rpx solid rgba(209, 213, 219, 0.4);

  &:active {
    transform: scale(0.95);
    background: rgba(233, 234, 236, 0.95);
  }
}

.glass-btn-secondary::after {
  border: none;
}

.btn-back-text {
  font-size: 29rpx;
  font-weight: 600;
  color: var(--pt-secondary, #666666);
}

.glass-btn-primary {
  flex: 1;
  height: 96rpx;
  line-height: 96rpx;
  border-radius: 48rpx;
  border: none;
  padding: 0;
  margin: 0;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  box-shadow:
    0 10rpx 32rpx rgba(255, 90, 61, 0.4),
    0 4rpx 12rpx rgba(255, 90, 61, 0.25),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.3),
    inset 0 -2rpx 0 rgba(180, 50, 20, 0.2);
  transition: all 0.32s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.35), transparent);
    transition: left 0.55s ease;
  }

  &:active {
    opacity: 0.95;
    transform: scale(0.97);
    box-shadow:
      0 6rpx 20rpx rgba(255, 90, 61, 0.35),
      inset 0 4rpx 12rpx rgba(140, 30, 10, 0.2);
  }

  &:active::after {
    left: 100%;
  }
}

.glass-btn-primary::after {
  border: none;
}

.glass-btn-primary[disabled] {
  opacity: 0.6;
  transform: none;
  box-shadow: 0 4rpx 12rpx rgba(255, 90, 61, 0.2);
}

.btn-submit-text {
  font-size: 31rpx;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 3rpx;
  text-shadow:
    0 2rpx 8rpx rgba(180, 30, 10, 0.35),
    0 1rpx 2rpx rgba(0, 0, 0, 0.15);
}

/* ====== 暗色模式适配 ====== */
page.dark .pet-selector-card {
  background: rgba(40, 40, 55, 0.86);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow:
    0 8rpx 32rpx rgba(0, 0, 0, 0.35),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.12);
}

page.dark .pet-selector-pop {
  background: rgba(40, 40, 55, 0.93);
  border-color: rgba(255, 255, 255, 0.1);
}

page.dark .glass-stat-card {
  background: rgba(40, 40, 55, 0.78);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 6rpx 24rpx rgba(0, 0, 0, 0.25),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.1);
}

page.dark .glass-tabbar {
  background: rgba(40, 40, 55, 0.7);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.25);
}

page.dark .glass-card-form {
  background: rgba(40, 40, 55, 0.8);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 8rpx 32rpx rgba(0, 0, 0, 0.3),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.1);
}

page.dark .glass-input,
.page.dark .glass-picker,
.page.dark .glass-textarea {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.1);
  color: #e5e5e5;
}

page.dark .glass-hint {
  background: rgba(255, 106, 61, 0.08);
  border-color: rgba(255, 106, 61, 0.15);
}

page.dark .cta-footer {
  background: rgba(30, 30, 42, 0.9);
  border-top-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 -8rpx 32rpx rgba(0, 0, 0, 0.4),
    0 -2rpx 8rpx rgba(0, 0, 0, 0.15);
}

page.dark .glass-btn-secondary {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.1);
}

page.dark .btn-back-text {
  color: #aaaaaa;
}
</style>
