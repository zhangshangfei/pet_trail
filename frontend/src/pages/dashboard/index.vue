<template>
  <view class="board-page">
    <user-top-bar
      :status-bar-height="statusBarHeight"
      :avatar="userAvatar"
      :name="userName"
      :unread-count="0"
      :show-discover="false"
      :show-bell="false"
      @rightTap="onBellTap"
      @userTap="onTopUserTap"
      @discoverTap="onDiscoverTap"
    />

    <scroll-view scroll-y class="board-scroll" :style="{ paddingTop: (statusBarHeight + 54) + 'px' }">
      <view class="board-content">
        <!-- 宠物选择器 - 与体重记录页完全一致的玻璃拟态设计 -->
        <view v-if="selectedPet" class="pet-info-card glass-card pet-selector-enhanced">
          <view class="pet-selector-main" @click="showPetSelector = true">
            <image class="pet-info-avatar glass-avatar-pet" :src="selectedPet.avatar || ''" mode="aspectFill" />
            <view class="pet-info-meta">
              <text class="pet-info-name">{{ selectedPet.name }}</text>
              <text class="pet-info-breed">{{ selectedPet.breed || '未设置品种' }}</text>
            </view>
            <view class="pet-switch-area glass-pet-switcher">
              <view class="switch-indicator">
                <text class="switch-icon">🔄</text>
                <text class="switch-text">切换</text>
              </view>
              <text class="switch-arrow">›</text>
            </view>
          </view>
        </view>

        <!-- 宠物选择弹窗 - 与体重记录页完全一致 -->
        <view v-if="showPetSelector" class="modal-mask glass-modal-mask" @click="showPetSelector = false">
          <view class="modal-content pet-select-modal glass-modal-card" @click.stop>
            <view class="modal-header glass-modal-header">
              <text class="modal-title">选择宠物</text>
              <text class="modal-close glass-modal-close" @click="showPetSelector = false">✕</text>
            </view>
            <view class="pet-list-container glass-list-container">
              <view
                v-for="(petItem, idx) in pets"
                :key="petItem.id"
                class="pet-option-item glass-pet-option"
                :class="{ active: selectedPet && selectedPet.id === petItem.id }"
                @click="selectPetDirectly(petItem)"
              >
                <image class="option-avatar glass-option-avatar" :src="petItem.avatar || ''" mode="aspectFill" />
                <view class="option-info">
                  <text class="option-name">{{ petItem.name }}</text>
                  <text class="option-breed">{{ petItem.breed || '未设置品种' }}</text>
                </view>
                <view v-if="selectedPet && selectedPet.id === petItem.id" class="option-check">✓</view>
              </view>
            </view>
          </view>
        </view>

        <!-- AI 健康分析入口 - 玻璃渐变增强 -->
        <view class="ai-entry glass-ai-card" @tap="goAiAnalysis">
          <view class="ai-entry-left">
            <text class="ai-entry-icon">🤖</text>
            <view class="ai-entry-text">
              <view class="ai-entry-title-row">
                <text class="ai-entry-title">AI 健康分析</text>
                <view class="ai-generated-tag glass-ai-badge">
                  <text class="ai-generated-tag-icon">✨</text>
                  <text class="ai-generated-tag-text">AI生成</text>
                </view>
              </view>
              <text v-if="aiSummary" class="ai-entry-desc">综合评分 {{ aiSummary.score }} · {{ aiSummary.level }}</text>
              <text v-else class="ai-entry-desc">智能评估宠物健康状况</text>
            </view>
          </view>
          <view v-if="aiSummary && aiSummary.warningCount > 0" class="ai-entry-badge glass-warning-badge">
            <text class="ai-entry-badge-text">{{ aiSummary.warningCount }}</text>
          </view>
          <text class="ai-entry-arrow">›</text>
        </view>

        <!-- 体重趋势 - 玻璃卡片 -->
        <view class="dash-section">
          <view class="section-header">
            <text class="section-title">⚖️ 体重趋势</text>
            <view class="chart-switch glass-chip-group">
              <view
                v-for="opt in chartRangeOptions"
                :key="opt.value"
                class="switch-chip glass-chip"
                :class="{ active: chartRange === opt.value }"
                @tap="onChartRangeChange(opt.value)"
              >
                <text class="switch-chip-text">{{ opt.label }}</text>
              </view>
              <view class="switch-chip glass-chip" @tap="goRecordList">
                <text class="switch-chip-text">记录</text>
              </view>
            </view>
          </view>

          <view class="dash-card chart-card-inner glass-chart-card">
            <view v-if="weightChartHasData" class="chart-wrap chart-wrap--weight">
              <canvas
                type="2d"
                id="weightChartCanvas"
                class="weight-canvas"
              ></canvas>
            </view>
            <view v-else class="chart-empty glass-chart-empty">
              <text class="chart-empty-text">近{{ chartRange }}天暂无体重记录</text>
            </view>

            <view class="chart-stats glass-stats-bar">
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

        <!-- 驱虫提醒 - 玻璃卡片列表 -->
        <view class="dash-section">
          <view class="section-header">
            <text class="section-title">💊 驱虫提醒</text>
            <view v-if="parasiteCards.length > parasiteCardsLimited.length" class="view-all-btn glass-view-all-btn" @tap="goParasiteList">
              <text class="view-all-text">查看全部</text>
              <text class="view-all-arrow">›</text>
            </view>
          </view>

          <view v-if="parasiteCardsLimited.length" class="vaccine-list">
            <view
              v-for="item in parasiteCardsLimited"
              :key="item.id"
              class="dash-card vaccine-card glass-reminder-card"
              :class="{ urgent: item.isUrgent }"
            >
              <view class="vaccine-header">
                <view class="vaccine-info">
                  <text class="vaccine-name">{{ item.name }}</text>
                  <text class="vaccine-date">计划日期: {{ item.date }}</text>
                </view>
                <view class="vaccine-countdown glass-countdown-badge" :class="{ countdownUrgent: item.isUrgent }">
                  <text class="countdown-number">{{ item.daysLeft }}</text>
                  <text class="countdown-unit">天</text>
                </view>
              </view>

              <view class="vaccine-progress">
                <view class="progress-bar glass-progress-track">
                  <view class="progress-fill glass-progress-fill" :style="{ width: item.progressPercent + '%' }"></view>
                </view>
                <text class="progress-text">{{ item.progressPercent }}%</text>
              </view>

              <view class="vaccine-actions">
                <button
                  class="btn-vaccine glass-action-btn"
                  :class="{ completed: item.isCompleted }"
                  :disabled="item.isCompleted"
                  @tap="onMarkParasiteDone(item)"
                >
                  <text class="btn-text">{{ item.isCompleted ? "已完成" : "标记完成" }}</text>
                </button>
              </view>
            </view>
          </view>
          <view v-else class="dash-card vaccine-empty glass-empty-state">
            <text class="vaccine-empty-icon">💊</text>
            <text class="vaccine-empty-text">暂无驱虫提醒</text>
            <text class="vaccine-empty-hint">点击右下角 + 添加驱虫记录</text>
          </view>
        </view>

        <!-- 疫苗提醒 - 玻璃卡片列表 -->
        <view class="dash-section">
          <view class="section-header">
            <text class="section-title">💉 疫苗提醒</text>
            <view v-if="vaccineCards.length > vaccineCardsLimited.length" class="view-all-btn glass-view-all-btn" @tap="goVaccineList">
              <text class="view-all-text">查看全部</text>
              <text class="view-all-arrow">›</text>
            </view>
          </view>

          <view v-if="vaccineCardsLimited.length" class="vaccine-list">
            <view
              v-for="item in vaccineCardsLimited"
              :key="item.id"
              class="dash-card vaccine-card glass-reminder-card"
              :class="{ urgent: item.isUrgent }"
            >
              <view class="vaccine-header">
                <view class="vaccine-info">
                  <text class="vaccine-name">{{ item.name }}</text>
                  <text class="vaccine-date">计划日期: {{ item.date }}</text>
                </view>
                <view class="vaccine-countdown glass-countdown-badge" :class="{ countdownUrgent: item.isUrgent }">
                  <text class="countdown-number">{{ item.daysLeft }}</text>
                  <text class="countdown-unit">天</text>
                </view>
              </view>

              <view class="vaccine-progress">
                <view class="progress-bar glass-progress-track">
                  <view class="progress-fill glass-progress-fill" :style="{ width: item.progressPercent + '%' }"></view>
                </view>
                <text class="progress-text">{{ item.progressPercent }}%</text>
              </view>

              <view class="vaccine-actions">
                <button
                  class="btn-vaccine glass-action-btn"
                  :class="{ completed: item.isCompleted }"
                  :disabled="item.isCompleted"
                  @tap="onMarkVaccineDone(item)"
                >
                  <text class="btn-text">{{ item.isCompleted ? "已完成" : "标记完成" }}</text>
                </button>
              </view>
            </view>
          </view>
          <view v-else class="dash-card vaccine-empty glass-empty-state">
            <text class="vaccine-empty-icon">💉</text>
            <text class="vaccine-empty-text">暂无疫苗提醒</text>
            <text class="vaccine-empty-hint">点击右下角 + 添加疫苗记录</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- FAB悬浮按钮 - 玻璃增强 -->
    <view class="fab glass-fab" @tap="addRecord">
      <view class="fab-inner glass-fab-inner">
        <view class="fab-icon-wrapper">
          <view class="fab-hbar"></view>
          <view class="fab-vbar"></view>
        </view>
      </view>
    </view>

    <!-- 底部TabBar - 玻璃拟态 -->
    <view class="board-tabbar-safe">
      <view class="board-tabbar glass-tabbar">
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
import { checkLogin, getUserAvatar, getPetAvatar, DEFAULT_USER_AVATAR, DEFAULT_PET_AVATAR_URL, loadWxSubscribeTemplates, requestWxSubscribe } from '@/utils/index'
import UserTopBar from '@/components/UserTopBar.vue'
import AvatarView from '@/components/AvatarView.vue'
import * as authApi from '@/api/auth'
import * as petApi from '@/api/pet'
import * as healthApi from '@/api/health'

export default {
  components: { UserTopBar, AvatarView },
  data() {
    return {
      statusBarHeight: 20,
      userName: "",
      userAvatar: DEFAULT_USER_AVATAR,
      fallbackPetAvatar: DEFAULT_PET_AVATAR_URL,
      pets: [],
      selectedPet: null,
      petSelectorOpen: false,
      showPetSelector: false,
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
      parasiteReminders: [],
      chartRange: 7,
      chartRangeOptions: [
        { value: 7, label: '7天' },
        { value: 30, label: '30天' }
      ],
      aiSummary: null,
      _chartData: null
    };
  },
  computed: {
    weightChartHasData() {
      return (this.weightSeriesRaw || []).some((v) => v != null && !Number.isNaN(v));
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
    vaccineCardsLimited() {
      return this.vaccineCards.filter(c => !c.isCompleted).slice(0, 3);
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
    },
    parasiteCardsLimited() {
      return this.parasiteCards.filter(c => !c.isCompleted).slice(0, 3);
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
    } catch (e) {
      this.statusBarHeight = 20;
    }
    this.loadPets();
    this.loadUserInfo();
    loadWxSubscribeTemplates();

    uni.$on('loginSuccess', () => {
      this.loadUserInfo()
      loadWxSubscribeTemplates()
    })
  },
  onHide() {
  },
  onUnload() {
    uni.$off('loginSuccess')
  },
  onPullDownRefresh() {
    this.loadUserInfo()
    this.loadPets()
    this.loadDashboardData()
    setTimeout(() => { uni.stopPullDownRefresh() }, 800)
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
        const res = await petApi.getPetList();
        if (res && res.success && Array.isArray(res.data)) {
          this.pets = res.data.map(pet => ({
            ...pet,
            avatar: getPetAvatar(pet.id, pet.avatar)
          }));
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
      this.aiSummary = null;
    },
    async loadDashboardData() {
      if (!this.selectedPet || !this.selectedPet.id) {
        this.clearBoardData();
        return;
      }
      const petId = this.selectedPet.id;

      try {
        const [weightRes, vaccineRes, parasiteRes] = await Promise.all([
          healthApi.getWeightTrend(petId, this.chartRange),
          petApi.getVaccineReminders(petId),
          petApi.getParasiteReminders(petId)
        ]);

        this.loadAiSummary(petId);

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
          return da - db;
        });

        const chartData = weightRecords.map((r) => ({
          date: this.formatDateYMD(r.recordDate),
          weight: Number(r.weight),
          rawDate: r.recordDate
        }));

        this.weightSeriesRaw = chartData.map((d) => d.weight);
        this.weightSeriesFilled = this.weightSeriesRaw.slice();
        this.chartDates = chartData.map((d) => d.date);

        const nums = chartData.map((d) => d.weight);
        if (chartData.length > 0) {
          const latest = chartData[chartData.length - 1];
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

        if (chartData.length > 1) {
          const latest = chartData[chartData.length - 1].weight;
          const prev = chartData[chartData.length - 2].weight;
          const d = latest - prev;
          this.weightStats.deltaNum = d;
          this.weightStats.delta = `${d > 0 ? "+" : ""}${d.toFixed(1)} kg`;
        } else {
          this.weightStats.deltaNum = 0;
          this.weightStats.delta = "--";
        }

        this._chartData = chartData;
        this.$nextTick(() => {
          setTimeout(() => this.drawWeightChart(), 100);
        });
      } catch (e) {
        console.error("加载看板数据失败:", e);
      }

      if (this.vaccineCardsLimited.length > 0 || this.parasiteCardsLimited.length > 0) {
        loadWxSubscribeTemplates();
      }
    },
    drawWeightChart() {
      const data = this._chartData;
      if (!data || !data.length) return;

      const query = uni.createSelectorQuery().in(this);
      query.select('#weightChartCanvas').node().exec((res) => {
        const canvas = res[0] && res[0].node;
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const dpr = uni.getSystemInfoSync().pixelRatio || 2;
        const canvasW = 650 * dpr;
        const canvasH = 280 * dpr;
        canvas.width = canvasW;
        canvas.height = canvasH;

        const padLeft = 50 * dpr;
        const padRight = 20 * dpr;
        const padTop = 24 * dpr;
        const padBottom = 36 * dpr;
        const plotW = canvasW - padLeft - padRight;
        const plotH = canvasH - padTop - padBottom;

        const weights = data.map((d) => d.weight);
        const minW = Math.min(...weights);
        const maxW = Math.max(...weights);
        const range = maxW - minW || 1;
        const pad = range * 0.15 || 0.5;
        const lo = minW - pad;
        const hi = maxW + pad;
        const diff = hi - lo;

        ctx.clearRect(0, 0, canvasW, canvasH);

        for (let i = 0; i <= 4; i++) {
          const y = padTop + (plotH / 4) * i;
          ctx.beginPath();
          ctx.moveTo(padLeft, y);
          ctx.lineTo(canvasW - padRight, y);
          ctx.strokeStyle = 'rgba(209,213,219,0.35)';
          ctx.lineWidth = 1 * dpr;
          ctx.stroke();

          const val = hi - (diff / 4) * i;
          ctx.font = `${10 * dpr}px sans-serif`;
          ctx.fillStyle = '#9ca3af';
          ctx.textAlign = 'right';
          ctx.fillText(val.toFixed(1), padLeft - 8 * dpr, y + 3.5 * dpr);
        }

        const points = data.map((d, i) => {
          const x = data.length === 1
            ? padLeft + plotW / 2
            : padLeft + (plotW / (data.length - 1)) * i;
          const y = padTop + (1 - (d.weight - lo) / diff) * plotH;
          return { x, y, weight: d.weight, date: d.date };
        });

        if (points.length > 1) {
          ctx.beginPath();
          ctx.moveTo(points[0].x, points[0].y);
          for (let i = 1; i < points.length; i++) {
            ctx.lineTo(points[i].x, points[i].y);
          }
          ctx.strokeStyle = '#10B981';
          ctx.lineWidth = 2.5 * dpr;
          ctx.lineCap = 'round';
          ctx.lineJoin = 'round';
          ctx.stroke();

          ctx.beginPath();
          ctx.moveTo(points[0].x, points[0].y);
          for (let i = 1; i < points.length; i++) {
            ctx.lineTo(points[i].x, points[i].y);
          }
          ctx.lineTo(points[points.length - 1].x, padTop + plotH + 6 * dpr);
          ctx.lineTo(points[0].x, padTop + plotH + 6 * dpr);
          ctx.closePath();

          const grad = ctx.createLinearGradient(0, padTop, 0, padTop + plotH);
          grad.addColorStop(0, 'rgba(16,185,129,0.22)');
          grad.addColorStop(1, 'rgba(16,185,129,0.02)');
          ctx.fillStyle = grad;
          ctx.fill();
        }

        points.forEach((pt) => {
          ctx.beginPath();
          ctx.arc(pt.x, pt.y, 5 * dpr, 0, Math.PI * 2);
          ctx.fillStyle = '#10B981';
          ctx.fill();
          ctx.beginPath();
          ctx.arc(pt.x, pt.y, 2.5 * dpr, 0, Math.PI * 2);
          ctx.fillStyle = '#ffffff';
          ctx.fill();

          ctx.font = `bold ${11 * dpr}px sans-serif`;
          ctx.fillStyle = '#059669';
          ctx.textAlign = 'center';
          ctx.fillText(String(pt.weight), pt.x, pt.y - 12 * dpr);
        });

        const maxLabels = data.length <= 7 ? data.length : Math.min(7, data.length);
        const labelStep = Math.max(1, Math.floor(data.length / maxLabels));
        ctx.font = `${9 * dpr}px sans-serif`;
        ctx.fillStyle = '#9ca3af';
        ctx.textAlign = 'center';
        for (let i = 0; i < data.length; i++) {
          if (i === 0 || i === data.length - 1 || i % labelStep === 0) {
            const label = data[i].date.substring(5);
            ctx.fillText(label, points[i].x, canvasH - 12 * dpr);
          }
        }
      });
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
              const result = await petApi.updateVaccineReminderStatus(this.selectedPet.id, item.id, { status: 1 });
              if (result.success) {
                uni.showToast({ title: "已标记完成", icon: "success" });
                requestWxSubscribe(['vaccine']);
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
              const result = await petApi.updateParasiteReminderStatus(this.selectedPet.id, item.id, { status: 1 });
              if (result.success) {
                uni.showToast({ title: "已标记完成", icon: "success" });
                requestWxSubscribe(['parasite']);
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
    selectPetDirectly(petItem) {
      if (!petItem || !petItem.id) return;
      this.selectedPet = petItem;
      this.showPetSelector = false;
      this.loadDashboardData();
    },
    togglePetSelector() {
      this.petSelectorOpen = !this.petSelectorOpen;
    },
    closePetSelector() {
      this.petSelectorOpen = false;
    },
    onBellTap() {
      uni.navigateTo({ url: '/pages/notification/index' })
    },
    onDiscoverTap() {
      uni.navigateTo({ url: '/pages/discover/index' })
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
    },
    goVaccineList() {
      if (!this.selectedPet || !this.selectedPet.id) return
      uni.navigateTo({ url: `/pages/health/vaccine-list?petId=${this.selectedPet.id}` })
    },
    goParasiteList() {
      if (!this.selectedPet || !this.selectedPet.id) return
      uni.navigateTo({ url: `/pages/health/parasite-list?petId=${this.selectedPet.id}` })
    },
    goRecordList() {
      if (!this.selectedPet || !this.selectedPet.id) return
      uni.navigateTo({ url: `/pages/health/record?petId=${this.selectedPet.id}` })
    },
    goAiAnalysis() {
      if (!this.selectedPet || !this.selectedPet.id) return
      uni.navigateTo({ url: `/pages/health/analysis?petId=${this.selectedPet.id}` });
    },
    onChartRangeChange(range) {
      this.chartRange = range
      this.loadDashboardData()
    },
    async loadAiSummary(petId) {
      try {
        const res = await healthApi.getHealthAnalysis(petId);
        if (res && res.success && res.data) {
          this.aiSummary = {
            score: res.data.score || 0,
            level: res.data.level || '',
            warningCount: (res.data.warnings && res.data.warnings.length) || 0
          };
        } else {
          this.aiSummary = null;
        }
      } catch (e) {
        this.aiSummary = null;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
/* ============================================
   健康看板页面 - 玻璃拟态风格 (Glassmorphism)
   保持原逻辑不变，全面升级视觉体验
   ============================================ */

.board-page {
  min-height: 100vh;
  background: transparent;
}

/* ====== 滚动区域 ====== */
.board-scroll {
  height: 100vh;
  box-sizing: border-box;
}

.board-content {
  padding: 20rpx 24rpx 240rpx;
}

/* ====== 宠物选择器 - 玻璃卡片（最高层级） ====== */
.glass-section {
  position: relative;
  margin-bottom: 24rpx;
  animation: sectionFadeIn 0.45s ease-out both;
  z-index: 100; /* 确保选择器在最上层 */
}

@keyframes sectionFadeIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ====== 宠物选择器 - 与体重记录页完全一致的玻璃拟态设计 ====== */
.glass-card {
  position: relative;
  background: rgba(255, 255, 255, 0.86);
  border-radius: 26rpx;
  padding: 26rpx;
  margin-bottom: 22rpx;
  box-shadow:
    0 6rpx 24rpx rgba(31, 38, 135, 0.08),
    0 2rpx 8rpx rgba(0, 0, 0, 0.03),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.92),
    inset 0 -1rpx 0 rgba(0, 0, 0, 0.02);
  backdrop-filter: blur(20px);
  border: 1rpx solid rgba(200, 210, 220, 0.45);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    right: 10%;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.88), transparent);
    pointer-events: none;
  }

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 5%;
    right: 5%;
    height: 2rpx;
    background: linear-gradient(90deg,
      transparent 0%,
      rgba(255, 122, 61, 0.35) 30%,
      rgba(255, 122, 61, 0.5) 50%,
      rgba(255, 122, 61, 0.35) 70%,
      transparent 100%
    );
    border-radius: 0 0 4rpx 4rpx;
    pointer-events: none;
  }
}

.pet-info-card {
  display: flex;
  align-items: center;
}

.pet-selector-enhanced {
  cursor: pointer;
  transition: all 0.32s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.98);
    box-shadow:
      0 8rpx 28rpx rgba(31, 38, 135, 0.12),
      0 3rpx 10rpx rgba(0, 0, 0, 0.04),
      inset 0 1rpx 0 rgba(255, 255, 255, 0.92);
  }
}

.pet-selector-main {
  display: flex;
  align-items: center;
  width: 100%;
}

.glass-avatar-pet {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  margin-right: 22rpx;
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  flex-shrink: 0;
  box-shadow:
    0 8rpx 24rpx rgba(255, 122, 61, 0.15),
    0 4rpx 12rpx rgba(0, 0, 0, 0.06),
    inset 0 2rpx 4rpx rgba(255, 255, 255, 0.5);
  border: 3rpx solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;

  .pet-selector-enhanced:active & {
    transform: scale(0.95) rotate(-3deg);
    box-shadow:
      0 6rpx 18rpx rgba(255, 122, 61, 0.2),
      0 2rpx 8rpx rgba(0, 0, 0, 0.08),
      inset 0 1rpx 2rpx rgba(255, 255, 255, 0.6);
  }
}

.pet-info-meta {
  flex: 1;
  min-width: 0;
}

.pet-info-name {
  font-size: 32rpx;
  font-weight: 800;
  color: var(--pt-text, #111827);
  display: block;
  letter-spacing: 0.8rpx;
  margin-bottom: 6rpx;
}

.pet-info-breed {
  font-size: 25rpx;
  color: var(--pt-muted, #9ca3af);
  display: block;
  font-weight: 500;
}

.glass-pet-switcher {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 14rpx 24rpx;
  background: linear-gradient(135deg, rgba(255, 245, 240, 0.85) 0%, rgba(255, 250, 247, 0.78) 100%);
  border-radius: 999rpx;
  backdrop-filter: blur(10px);
  border: 1.5rpx solid rgba(255, 122, 61, 0.2);
  box-shadow:
    0 4rpx 14rpx rgba(255, 106, 61, 0.1),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.6);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;

  &:active {
    transform: scale(0.94);
    background: linear-gradient(135deg, rgba(255, 235, 230, 0.92) 0%, rgba(255, 245, 240, 0.88) 100%);
    box-shadow:
      0 6rpx 18rpx rgba(255, 106, 61, 0.18),
      inset 0 1rpx 2rpx rgba(255, 122, 61, 0.15);
    border-color: rgba(255, 122, 61, 0.35);
  }
}

.switch-indicator {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.switch-icon {
  font-size: 26rpx;
  animation: switchRotate 2s linear infinite;
}

@keyframes switchRotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.switch-text {
  font-size: 25rpx;
  color: var(--pt-primary, #ff7a3d);
  font-weight: 700;
  letter-spacing: 1rpx;
}

.switch-arrow {
  font-size: 32rpx;
  color: var(--pt-primary, #ff7a3d);
  font-weight: 600;
  transition: transform 0.28s ease;

  .glass-pet-switcher:active & {
    transform: translateX(4rpx);
  }
}

/* ====== 宠物选择弹窗 - 与体重记录页完全一致 ====== */
.glass-modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  animation: modalFadeIn 0.25s ease-out both;
}

@keyframes modalFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.glass-modal-card {
  width: 80%;
  max-height: 80vh;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.98) 0%, rgba(252, 250, 250, 0.96) 100%);
  border-radius: 32rpx;
  overflow: hidden;
  backdrop-filter: blur(28px);
  border: 2rpx solid rgba(200, 210, 220, 0.45);
  box-shadow:
    0 28rpx 68rpx rgba(31, 38, 135, 0.22),
    0 12rpx 32rpx rgba(0, 0, 0, 0.12),
    0 4rpx 12rpx rgba(0, 0, 0, 0.06),
    inset 0 2rpx 0 rgba(255, 255, 255, 1),
    inset 0 -1rpx 0 rgba(180, 190, 200, 0.15);
  animation: modalSlideUpEnhanced 0.35s cubic-bezier(0.34, 1.56, 0.64, 1) both;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 5%;
    right: 5%;
    height: 3rpx;
    background: linear-gradient(90deg,
      transparent 0%,
      rgba(255, 122, 61, 0.6) 20%,
      rgba(255, 122, 61, 0.8) 50%,
      rgba(255, 122, 61, 0.6) 80%,
      transparent 100%
    );
    border-radius: 0 0 6rpx 6rpx;
    pointer-events: none;
    z-index: 10;
  }
}

@keyframes modalSlideUpEnhanced {
  from {
    opacity: 0;
    transform: translateY(50rpx) scale(0.94);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.pet-select-modal {
  max-height: 70vh;
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(40rpx) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.glass-list-container {
  padding: 20rpx;
  max-height: 55vh;
  overflow-y: auto;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.5) 0%, transparent 100%);
}

.glass-pet-option {
  display: flex;
  align-items: center;
  padding: 24rpx 20rpx;
  border-radius: 22rpx;
  margin-bottom: 14rpx;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(10px);
  border: 1.5rpx solid rgba(210, 220, 230, 0.35);
  box-shadow:
    0 2rpx 8rpx rgba(31, 38, 135, 0.04),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.9);
  transition: all 0.32s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.97);
    background: rgba(255, 245, 240, 0.85);
    border-color: rgba(255, 122, 61, 0.3);
  }

  &.active {
    background: linear-gradient(135deg, rgba(255, 250, 245, 0.98) 0%, rgba(255, 248, 245, 0.95) 100%);
    border: 2rpx solid var(--pt-primary, #ff7a3d);
    box-shadow:
      0 6rpx 22rpx rgba(255, 106, 61, 0.18),
      0 2rpx 8rpx rgba(255, 106, 61, 0.08),
      inset 0 1rpx 0 rgba(255, 255, 255, 1),
      inset 0 -1rpx 0 rgba(255, 122, 61, 0.08);

    .option-name {
      color: var(--pt-primary, #ff7a3d);
      font-weight: 800;
    }

    .option-check {
      display: flex;
      opacity: 1;
      transform: scale(1);
    }
  }

  &:last-child {
    margin-bottom: 0;
  }
}

.glass-option-avatar {
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  margin-right: 18rpx;
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(255, 122, 61, 0.12);
  border: 2rpx solid rgba(255, 255, 255, 0.8);
}

.option-info {
  flex: 1;
  min-width: 0;
}

.option-name {
  font-size: 29rpx;
  font-weight: 700;
  color: var(--pt-text, #374151);
  display: block;
  margin-bottom: 4rpx;
  transition: all 0.28s ease;
}

.option-breed {
  font-size: 24rpx;
  color: var(--pt-muted, #9ca3af);
  display: block;
  font-weight: 500;
}

.option-check {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  color: #fff;
  font-size: 26rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 14rpx rgba(255, 90, 61, 0.3);
  opacity: 0;
  transform: scale(0.5);
  transition: all 0.32s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.glass-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 28rpx;
  background: linear-gradient(180deg, rgba(255, 248, 245, 0.8) 0%, rgba(255, 255, 255, 0.4) 100%);
  border-bottom: 1.5rpx solid rgba(210, 220, 230, 0.35);
  backdrop-filter: blur(10px);
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: -1rpx;
    left: 15%;
    right: 15%;
    height: 2rpx;
    background: linear-gradient(90deg,
      transparent,
      rgba(255, 122, 61, 0.25),
      transparent
    );
    pointer-events: none;
  }
}

.modal-title {
  font-size: 33rpx;
  font-weight: 800;
  color: var(--pt-text, #111827);
  letter-spacing: 0.5rpx;
}

.glass-modal-close {
  font-size: 34rpx;
  color: var(--pt-muted, #9ca3af);
  padding: 10rpx;
  border-radius: 50%;
  transition: all 0.25s ease;

  &:active {
    background: rgba(255, 77, 79, 0.1);
    color: #ff4d4f;
    transform: rotate(90deg);
  }
}

/* ====== AI入口卡片 - 渐变玻璃增强 ====== */
.glass-ai-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.92) 0%, rgba(118, 75, 162, 0.92) 100%);
  border-radius: 26rpx;
  padding: 26rpx 28rpx;
  margin-bottom: 24rpx;
  backdrop-filter: blur(24px);
  border: 1rpx solid rgba(255, 255, 255, 0.25);
  box-shadow:
    0 10rpx 40rpx rgba(102, 126, 234, 0.3),
    0 4rpx 16rpx rgba(118, 75, 162, 0.2),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.3),
    inset 0 -1rpx 0 rgba(0, 0, 0, 0.15);
  position: relative;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle at center, rgba(255, 255, 255, 0.08), transparent 70%);
    pointer-events: none;
    animation: aiCardGlow 8s ease-in-out infinite alternate;
  }

  &:active {
    transform: scale(0.97);
    opacity: 0.95;
  }
}

@keyframes aiCardGlow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.ai-entry-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
  position: relative;
  z-index: 1;
}

.ai-entry-icon {
  font-size: 46rpx;
}

.ai-entry-text {
  display: flex;
  flex-direction: column;
}

.ai-entry-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.ai-entry-title {
  font-size: 31rpx;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
}

.glass-ai-badge {
  display: flex;
  align-items: center;
  gap: 6rpx;
  background: rgba(255, 255, 255, 0.22);
  border: 1rpx solid rgba(255, 255, 255, 0.45);
  border-radius: 18rpx;
  padding: 6rpx 16rpx;
  backdrop-filter: blur(8px);
}

.ai-generated-tag-icon {
  font-size: 22rpx;
}

.ai-generated-tag-text {
  font-size: 21rpx;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 1rpx;
}

.ai-entry-desc {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.82);
  margin-top: 6rpx;
  font-weight: 500;
}

.glass-warning-badge {
  width: 40rpx;
  height: 40rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #ff3b30 0%, #ff5544 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10rpx;
  box-shadow: 0 4rpx 14rpx rgba(255, 59, 48, 0.4);
  position: relative;
  z-index: 1;
}

.ai-entry-badge-text {
  font-size: 22rpx;
  font-weight: 800;
  color: #fff;
}

.ai-entry-arrow {
  font-size: 42rpx;
  color: rgba(255, 255, 255, 0.85);
  position: relative;
  z-index: 1;
}

/* ====== 区块头部 ====== */
.dash-section {
  margin-bottom: 32rpx;
}

.section-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22rpx;
  padding: 0 8rpx;
}

.section-title {
  font-size: 33rpx;
  font-weight: 700;
  color: var(--pt-text, #111827);
  letter-spacing: 0.5rpx;
}

/* ====== 查看全部按钮 - 玻璃标签 ====== */
.glass-view-all-btn {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 10rpx 24rpx;
  background: linear-gradient(135deg, rgba(255, 122, 61, 0.1) 0%, rgba(255, 77, 79, 0.08) 100%);
  border-radius: 30rpx;
  backdrop-filter: blur(8px);
  border: 1rpx solid rgba(255, 122, 61, 0.15);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
    background: linear-gradient(135deg, rgba(255, 122, 61, 0.15) 0%, rgba(255, 77, 79, 0.12) 100%);
  }
}

.view-all-text {
  font-size: 25rpx;
  color: var(--pt-primary, #ff7a3d);
  font-weight: 600;
}

.view-all-arrow {
  font-size: 29rpx;
  color: var(--pt-primary, #ff7a3d);
  margin-left: 6rpx;
  font-weight: 700;
}

/* ====== 图表切换器 - 玻璃芯片组 ====== */
.glass-chip-group {
  display: flex;
  gap: 8rpx;
  background: rgba(249, 250, 251, 0.65);
  padding: 8rpx 12rpx;
  border-radius: 30rpx;
  backdrop-filter: blur(10px);
  border: 1rpx solid rgba(209, 213, 219, 0.25);
}

.glass-chip {
  padding: 8rpx 20rpx;
  border-radius: 22rpx;
  transition: all 0.32s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(6px);

  &.active {
    background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
    box-shadow:
      0 4rpx 14rpx rgba(255, 106, 61, 0.3),
      inset 0 1rpx 0 rgba(255, 255, 255, 0.3);
    transform: scale(1.02);
  }

  &:active {
    transform: scale(0.95);
  }
}

.switch-chip-text {
  font-size: 23rpx;
  color: var(--pt-primary, #ff7a3d);
  font-weight: 600;
}

.switch-chip.active .switch-chip-text {
  color: #ffffff;
  font-weight: 700;
  text-shadow: 0 1rpx 4rpx rgba(180, 30, 10, 0.3);
}

/* ====== 图表卡片 - 玻璃容器 ====== */
.glass-chart-card {
  position: relative;
  background: rgba(255, 255, 255, 0.84);
  border-radius: 28rpx;
  box-shadow:
    0 8rpx 32rpx rgba(31, 38, 135, 0.08),
    0 2rpx 8rpx rgba(0, 0, 0, 0.03),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.92),
    inset 0 -1rpx 0 rgba(0, 0, 0, 0.02);
  backdrop-filter: blur(22px);
  border: 1rpx solid rgba(255, 255, 255, 0.62);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    right: 10%;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.9), transparent);
    pointer-events: none;
  }
}

.chart-card-inner {
  padding: 26rpx;
}

/* 图表包装 */
.chart-wrap--weight {
  display: flex;
  flex-direction: row;
  justify-content: center;
  margin-bottom: 26rpx;
}

.weight-canvas {
  width: 650rpx;
  height: 280rpx;
}

/* 空状态 - 玻璃提示 */
.glass-chart-empty {
  height: 280rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, rgba(249, 250, 251, 0.6) 0%, rgba(255, 255, 255, 0.4) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 26rpx;
  backdrop-filter: blur(8px);
  border: 1rpx dashed rgba(209, 213, 219, 0.4);
}

.chart-empty-text {
  font-size: 27rpx;
  color: var(--pt-muted, #9ca3af);
  font-weight: 500;
}

/* 统计栏 - 玻璃分割条 */
.glass-stats-bar {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-around;
  padding-top: 26rpx;
  border-top: 1rpx solid rgba(17, 24, 39, 0.06);
  backdrop-filter: blur(4px);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-label {
  font-size: 23rpx;
  color: var(--pt-secondary, #6b7280);
  margin-bottom: 10rpx;
  font-weight: 600;
}

.stat-value {
  font-size: 33rpx;
  font-weight: 800;
  color: var(--pt-text, #111827);
  letter-spacing: -0.5rpx;
}

.stat-value.up {
  color: #10b981;
  text-shadow: 0 2rpx 8rpx rgba(16, 185, 129, 0.2);
}

.stat-value.down {
  color: #ff4d4f;
  text-shadow: 0 2rpx 8rpx rgba(255, 77, 79, 0.2);
}

.stat-divider {
  width: 1rpx;
  height: 64rpx;
  background: linear-gradient(180deg, transparent, rgba(17, 24, 39, 0.08), transparent);
}

/* ====== 提醒卡片列表 ====== */
.vaccine-list {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
}

/* 提醒卡片 - 玻璃拟态 */
.glass-reminder-card {
  position: relative;
  background: rgba(255, 255, 255, 0.84);
  border-radius: 26rpx;
  box-shadow:
    0 6rpx 24rpx rgba(31, 38, 135, 0.08),
    0 2rpx 8rpx rgba(0, 0, 0, 0.03),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.92);
  padding: 26rpx;
  backdrop-filter: blur(20px);
  border: 1rpx solid rgba(255, 255, 255, 0.62);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    right: 10%;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.88), transparent);
    pointer-events: none;
  }

  &:hover,
  &:active {
    transform: translateY(-4rpx);
    box-shadow:
      0 14rpx 36rpx rgba(31, 38, 135, 0.15),
      inset 0 1rpx 0 rgba(255, 255, 255, 1);
  }

  &.urgent {
    background: linear-gradient(
      135deg,
      rgba(255, 245, 245, 0.88) 0%,
      rgba(255, 232, 232, 0.82) 100%
    );
    border-color: rgba(255, 77, 79, 0.25);
    box-shadow:
      0 6rpx 24rpx rgba(255, 77, 79, 0.12),
      inset 0 1rpx 0 rgba(255, 255, 255, 0.92);
  }
}

.vaccine-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22rpx;
}

.vaccine-info {
  flex: 1;
  padding-right: 18rpx;
}

.vaccine-name {
  display: block;
  font-size: 31rpx;
  font-weight: 700;
  color: var(--pt-text, #111827);
  margin-bottom: 8rpx;
  letter-spacing: 0.3rpx;
}

.vaccine-date {
  display: block;
  font-size: 23rpx;
  color: var(--pt-secondary, #6b7280);
  font-weight: 500;
}

/* 倒计时徽章 - 玻璃样式 */
.glass-countdown-badge {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  background: linear-gradient(135deg, rgba(209, 250, 229, 0.9) 0%, rgba(167, 243, 208, 0.85) 100%);
  padding: 14rpx 24rpx;
  border-radius: 32rpx;
  flex-shrink: 0;
  backdrop-filter: blur(8px);
  border: 1rpx solid rgba(16, 185, 129, 0.2);
  box-shadow: 0 4rpx 12rpx rgba(16, 185, 129, 0.15);
  transition: all 0.3s ease;

  &.countdownUrgent {
    background: linear-gradient(135deg, #ff4d4f 0%, #ff6b6b 100%);
    border-color: rgba(255, 77, 79, 0.4);
    box-shadow: 0 6rpx 20rpx rgba(255, 77, 79, 0.35);
  }
}

.countdown-number {
  font-size: 38rpx;
  font-weight: 800;
  color: var(--pt-text, #111827);
  margin-right: 4rpx;
  letter-spacing: -0.5rpx;
}

.countdownUrgent .countdown-number,
.countdownUrgent .countdown-unit {
  color: #ffffff;
  text-shadow: 0 2rpx 6rpx rgba(180, 30, 10, 0.3);
}

.countdown-unit {
  font-size: 23rpx;
  color: var(--pt-secondary, #6b7280);
  font-weight: 600;
}

/* 进度条 - 玻璃轨道 */
.vaccine-progress {
  margin-bottom: 22rpx;
}

.glass-progress-track {
  height: 14rpx;
  background: rgba(255, 244, 230, 0.6);
  border-radius: 7rpx;
  overflow: hidden;
  margin-bottom: 10rpx;
  backdrop-filter: blur(4px);
  border: 1rpx solid rgba(255, 183, 77, 0.15);
  box-shadow: inset 0 1rpx 3rpx rgba(0, 0, 0, 0.05);
}

.glass-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #34c759 0%, #5dd87a 100%);
  border-radius: 7rpx;
  position: relative;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2rpx 8rpx rgba(52, 199, 89, 0.3);

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 50%;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.4), transparent);
    border-radius: 7rpx 7rpx 0 0;
  }
}

.progress-text {
  font-size: 22rpx;
  color: var(--pt-muted, #9ca3af);
  text-align: right;
  display: block;
  font-weight: 600;
}

/* 操作按钮 - 玻璃样式 */
.vaccine-actions {
  display: flex;
  justify-content: center;
}

.glass-action-btn {
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  color: #ffffff;
  border: none;
  border-radius: 48rpx;
  padding: 18rpx 56rpx;
  font-size: 27rpx;
  font-weight: 700;
  line-height: 1.2;
  box-shadow:
    0 8rpx 24rpx rgba(255, 106, 61, 0.35),
    0 2rpx 8rpx rgba(255, 106, 61, 0.2),
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
    transform: scale(0.96);
    box-shadow:
      0 4rpx 14rpx rgba(255, 106, 61, 0.25),
      inset 0 4rpx 12rpx rgba(140, 30, 10, 0.2);
  }

  &:active::after {
    left: 100%;
  }

  &.completed {
    background: linear-gradient(135deg, rgba(209, 250, 229, 0.9) 0%, rgba(167, 243, 208, 0.85) 100%);
    box-shadow: 0 4rpx 12rpx rgba(16, 185, 129, 0.15);
  }
}

.btn-text {
  color: #ffffff;
  font-weight: 700;
  letter-spacing: 1rpx;
}

.glass-action-btn.completed .btn-text {
  color: #047857;
  text-shadow: none;
}

/* 空状态 - 玻璃容器 */
.glass-empty-state {
  padding: 52rpx 28rpx;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  background: rgba(249, 250, 251, 0.5);
  backdrop-filter: blur(8px);
  border: 1rpx dashed rgba(209, 213, 219, 0.4);
}

.vaccine-empty-icon {
  font-size: 60rpx;
  margin-bottom: 10rpx;
}

.vaccine-empty-text {
  font-size: 29rpx;
  color: var(--pt-secondary, #6b7280);
  font-weight: 600;
}

.vaccine-empty-hint {
  font-size: 24rpx;
  color: var(--pt-muted, #9ca3af);
  font-weight: 500;
}

/* ====== FAB悬浮按钮 - 与首页一致 ====== */
.glass-fab {
  position: fixed;
  right: 28rpx;
  bottom: calc(env(safe-area-inset-bottom) + 260rpx);
  z-index: 45;
}

.glass-fab-inner {
  width: 108rpx; /* 与首页一致 */
  height: 108rpx;
  border-radius: 32rpx; /* 与首页一致 */
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 12rpx 40rpx rgba(255, 77, 79, 0.45),
    0 4rpx 12rpx rgba(255, 77, 79, 0.25),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(16px);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); /* 与首页一致的缓动函数 */
  border: 1rpx solid rgba(255, 255, 255, 0.2); /* 与首页一致的边框 */

  &:active {
    transform: scale(0.9) rotate(-5deg); /* ✨ 与首页一致：缩放+轻微旋转 */
    box-shadow:
      0 6rpx 20rpx rgba(255, 77, 79, 0.35), /* 与首页一致的阴影 */
      inset 0 2rpx 8rpx rgba(0, 0, 0, 0.15); /* 与首页一致的内阴影 */
  }
}

.fab-icon-wrapper {
  position: relative;
  width: 40rpx;
  height: 40rpx;
  z-index: 1;
}

.fab-hbar,
.fab-vbar {
  position: absolute;
  background: #ffffff;
  border-radius: 5rpx;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.15);
}

.fab-hbar {
  top: 50%;
  left: 0;
  width: 40rpx;
  height: 6rpx;
  transform: translateY(-50%);
}

.fab-vbar {
  left: 50%;
  top: 0;
  width: 6rpx;
  height: 40rpx;
  transform: translateX(-50%);
}

/* ====== 底部TabBar - 玻璃拟态 ====== */
.board-tabbar-safe {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 42;
}

.glass-tabbar {
  margin: 0 28rpx 20rpx;
  height: 130rpx;
  background: rgba(255, 255, 255, 0.88);
  border-radius: 68rpx;
  box-shadow:
    0 20rpx 48rpx rgba(0, 0, 0, 0.12),
    0 6rpx 16rpx rgba(0, 0, 0, 0.06),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.95),
    inset 0 -1rpx 0 rgba(0, 0, 0, 0.03);
  padding: 0 28rpx;
  display: flex;
  align-items: center;
  justify-content: space-around;
  backdrop-filter: blur(24px);
  border: 1rpx solid rgba(255, 255, 255, 0.65);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 15%;
    right: 15%;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.95), transparent);
    pointer-events: none;
  }
}

.board-tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--pt-muted, #8b93a6);
  transition: all 0.3s ease;
  cursor: pointer;

  &:active {
    transform: scale(0.93);
  }
}

.board-tab-item.active {
  color: var(--pt-primary, #ff7a3d);
}

.board-tab-icon {
  font-size: 44rpx;
  line-height: 44rpx;
  margin-bottom: 12rpx;
  filter: grayscale(100%);
  transition: all 0.3s ease;
}

.board-tab-item.active .board-tab-icon {
  filter: grayscale(0%);
  transform: scale(1.1);
}

.board-tab-text {
  font-size: 25rpx;
  line-height: 25rpx;
  font-weight: 700;
  letter-spacing: 0.5rpx;
}

/* ====== 暗色模式适配 - 宠物选择器与体重记录页完全一致 ====== */
page.dark .glass-card {
  background: rgba(40, 40, 55, 0.84);
  border: 1rpx solid rgba(255, 255, 255, 0.15);
  box-shadow:
    0 6rpx 24rpx rgba(0, 0, 0, 0.3),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.1);

  &::before {
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.12), transparent);
  }

  &::after {
    background: linear-gradient(90deg,
      transparent 0%,
      rgba(255, 122, 61, 0.25) 30%,
      rgba(255, 122, 61, 0.4) 50%,
      rgba(255, 122, 61, 0.25) 70%,
      transparent 100%
    );
  }
}

page.dark .glass-avatar-pet {
  background: linear-gradient(135deg, rgba(255, 150, 100, 0.3) 0%, rgba(255, 120, 80, 0.2) 100%);
  border-color: rgba(255, 122, 61, 0.3);
  box-shadow:
    0 8rpx 24rpx rgba(255, 106, 61, 0.1),
    inset 0 2rpx 4rpx rgba(255, 255, 255, 0.08);

  .pet-selector-enhanced:active & {
    transform: scale(0.95) rotate(-3deg);
    box-shadow:
      0 6rpx 18rpx rgba(255, 122, 61, 0.15),
      inset 0 1rpx 2rpx rgba(255, 255, 255, 0.1);
  }
}

page.dark .glass-switcher {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.1);

  &:active {
    background: rgba(255, 106, 61, 0.12);
  }
}

page.dark .glass-pet-switcher {
  background: linear-gradient(135deg, rgba(255, 120, 80, 0.12) 0%, rgba(255, 100, 80, 0.08) 100%);
  border-color: rgba(255, 122, 61, 0.25);
  box-shadow:
    0 4rpx 14rpx rgba(255, 106, 61, 0.08),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.05);

  &:active {
    background: linear-gradient(135deg, rgba(255, 120, 80, 0.18) 0%, rgba(255, 100, 80, 0.14) 100%);
    border-color: rgba(255, 122, 61, 0.4);
    box-shadow:
      0 6rpx 18rpx rgba(255, 106, 61, 0.15),
      inset 0 1rpx 2rpx rgba(255, 122, 61, 0.12);
  }
}

page.dark .pet-info-name {
  color: #e5e5e5;
}

page.dark .pet-info-breed {
  color: #888888;
}

page.dark .glass-modal-card {
  background: linear-gradient(145deg, rgba(50, 50, 68, 0.98) 0%, rgba(45, 45, 60, 0.96) 100%);
  border: 2rpx solid rgba(255, 255, 255, 0.18);
  box-shadow:
    0 28rpx 68rpx rgba(0, 0, 0, 0.5),
    0 12rpx 32rpx rgba(0, 0, 0, 0.3),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.1),
    inset 0 -1rpx 0 rgba(255, 122, 61, 0.08);

  &::before {
    background: linear-gradient(90deg,
      transparent 0%,
      rgba(255, 122, 61, 0.4) 20%,
      rgba(255, 122, 61, 0.6) 50%,
      rgba(255, 122, 61, 0.4) 80%,
      transparent 100%
    );
  }
}

page.dark .glass-pet-option {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.12);
  box-shadow:
    0 2rpx 8rpx rgba(0, 0, 0, 0.15),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.05);

  &:active {
    background: rgba(255, 120, 80, 0.12);
    border-color: rgba(255, 122, 61, 0.25);
  }

  &.active {
    background: linear-gradient(135deg, rgba(255, 120, 80, 0.25) 0%, rgba(255, 100, 80, 0.2) 100%);
    border: 2rpx solid rgba(255, 122, 61, 0.5);
    box-shadow:
      0 6rpx 22rpx rgba(255, 106, 61, 0.2),
      0 2rpx 8rpx rgba(255, 106, 61, 0.1),
      inset 0 1rpx 0 rgba(255, 255, 255, 0.1),
      inset 0 -1rpx 0 rgba(255, 122, 61, 0.15);

    .option-name {
      color: #ff9966;
    }
  }
}

page.dark .glass-option-avatar {
  background: linear-gradient(135deg, rgba(255, 150, 100, 0.25) 0%, rgba(255, 120, 80, 0.15) 100%);
  border-color: rgba(255, 122, 61, 0.3);
}

page.dark .glass-modal-header,
page.dark .glass-modal-footer {
  background: linear-gradient(180deg, rgba(255, 120, 80, 0.12) 0%, rgba(255, 100, 80, 0.06) 100%);
  border-bottom-color: rgba(255, 255, 255, 0.15);
  border-top-color: rgba(255, 255, 255, 0.15);

  &::after {
    background: linear-gradient(90deg,
      transparent,
      rgba(255, 122, 61, 0.3),
      transparent
    );
  }
}

page.dark .modal-title {
  color: #e5e5e5;
}

page.dark .glass-ai-card {
  background: linear-gradient(135deg, rgba(80, 100, 180, 0.88) 0%, rgba(90, 60, 130, 0.88) 100%);
  border-color: rgba(255, 255, 255, 0.15);
}

page.dark .glass-chart-card {
  background: rgba(40, 40, 55, 0.78);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 8rpx 32rpx rgba(0, 0, 0, 0.3),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.1);
}

page.dark .glass-chart-empty {
  background: linear-gradient(135deg, rgba(60, 60, 75, 0.5) 0%, rgba(50, 50, 65, 0.4) 100%);
  border-color: rgba(255, 255, 255, 0.08);
}

page.dark .glass-reminder-card {
  background: rgba(40, 40, 55, 0.78);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 6rpx 24rpx rgba(0, 0, 0, 0.25),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.1);
}

page.dark .glass-reminder-card.urgent {
  background: linear-gradient(135deg, rgba(255, 80, 60, 0.2) 0%, rgba(255, 100, 80, 0.15) 100%);
  border-color: rgba(255, 77, 79, 0.25);
}

page.dark .glass-countdown-badge:not(.countdownUrgent) {
  background: linear-gradient(135deg, rgba(80, 180, 100, 0.25) 0%, rgba(60, 160, 80, 0.2) 100%);
  border-color: rgba(16, 185, 129, 0.2);
}

page.dark .countdown-number {
  color: #e5e5e5;
}

page.dark .glass-progress-track {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 183, 77, 0.1);
}

page.dark .glass-action-btn.completed {
  background: linear-gradient(135deg, rgba(80, 180, 100, 0.3) 0%, rgba(60, 160, 80, 0.25) 100%);
  box-shadow: 0 4rpx 12rpx rgba(16, 185, 129, 0.15);
}

page.dark .glass-empty-state {
  background: rgba(60, 60, 75, 0.3);
  border-color: rgba(255, 255, 255, 0.08);
}

page.dark .glass-tabbar {
  background: rgba(30, 30, 42, 0.9);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow:
    0 20rpx 48rpx rgba(0, 0, 0, 0.4),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.12);
}

page.dark .section-title {
  color: #e5e5e5;
}

page.dark .vaccine-name {
  color: #e5e5e5;
}

page.dark .stat-value:not(.up):not(.down) {
  color: #e5e5e5;
}

page.dark .view-all-text {
  color: #ff9966;
}
</style>
