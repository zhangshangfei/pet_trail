<template>
  <view class="calendar-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack"><view class="nav-back-arrow"></view></view>
        <text class="nav-title">宠物日历</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="page-content">
        <view class="pet-tabs" v-if="pets.length > 1">
          <scroll-view scroll-x class="pet-tabs-scroll">
            <view
              v-for="pet in pets"
              :key="pet.id"
              class="pet-tab"
              :class="{ active: currentPetId === pet.id }"
              @tap="switchPet(pet.id)"
            >
              <view class="pet-tab-avatar" :class="{ active: currentPetId === pet.id }">
                <avatar-view :src="pet.avatar || ''" :name="pet.name || ''" :id="pet.id" :size="66" />
              </view>
              <text class="pet-tab-name">{{ pet.name }}</text>
            </view>
          </scroll-view>
        </view>

        <view class="calendar-card">
          <view class="calendar-header">
            <view class="month-arrow" @tap="prevMonth"><text class="arrow-icon">‹</text></view>
            <text class="month-text">{{ currentYear }}年{{ currentMonth }}月</text>
            <view class="month-arrow" @tap="nextMonth"><text class="arrow-icon">›</text></view>
          </view>

          <view class="weekday-row">
            <text class="weekday-text" v-for="w in weekdays" :key="w">{{ w }}</text>
          </view>

          <view class="days-grid">
            <view
              v-for="(day, idx) in calendarDays"
              :key="idx"
              class="day-cell"
              :class="{
                'day-empty': !day.day,
                'day-today': day.isToday,
                'day-selected': day.dateStr === selectedDate,
                'day-has-checkin': day.hasCheckin,
                'day-has-weight': day.hasWeight,
                'day-has-vaccine': day.hasVaccine,
                'day-has-parasite': day.hasParasite
              }"
              @tap="onDayTap(day)"
            >
              <text class="day-number">{{ day.day || '' }}</text>
              <view v-if="day.day" class="day-dots">
                <view v-if="day.hasCheckin" class="dot dot-checkin"></view>
                <view v-if="day.hasWeight" class="dot dot-weight"></view>
                <view v-if="day.hasVaccine" class="dot dot-vaccine"></view>
                <view v-if="day.hasParasite" class="dot dot-parasite"></view>
              </view>
            </view>
          </view>
        </view>

        <view class="legend">
          <view class="legend-item"><view class="legend-dot legend-dot--checkin"></view><text class="legend-text">打卡</text></view>
          <view class="legend-item"><view class="legend-dot legend-dot--weight"></view><text class="legend-text">体重</text></view>
          <view class="legend-item"><view class="legend-dot legend-dot--vaccine"></view><text class="legend-text">疫苗</text></view>
          <view class="legend-item"><view class="legend-dot legend-dot--parasite"></view><text class="legend-text">驱虫</text></view>
        </view>

        <view v-if="selectedDate" class="day-detail">
          <text class="detail-title">{{ selectedDate }} 记录</text>

          <view v-if="dayCheckins.length" class="detail-section">
            <text class="detail-section-title">✅ 打卡记录</text>
            <view class="detail-list">
              <view v-for="item in dayCheckins" :key="item.id" class="detail-item">
                <text class="detail-item-icon">{{ item.itemIcon || '📋' }}</text>
                <text class="detail-item-name">{{ item.itemName || '打卡' }}</text>
              </view>
            </view>
          </view>

          <view v-if="dayWeight" class="detail-section">
            <text class="detail-section-title">⚖️ 体重记录</text>
            <text class="detail-value">{{ dayWeight }} kg</text>
          </view>

          <view v-if="dayVaccines.length" class="detail-section">
            <text class="detail-section-title">💉 疫苗提醒</text>
            <view class="detail-list">
              <view v-for="item in dayVaccines" :key="item.id" class="detail-item">
                <text class="detail-item-name">{{ item.vaccineName }}</text>
              </view>
            </view>
          </view>

          <view v-if="dayParasites.length" class="detail-section">
            <text class="detail-section-title">🐛 驱虫提醒</text>
            <view class="detail-list">
              <view v-for="item in dayParasites" :key="item.id" class="detail-item">
                <text class="detail-item-name">{{ item.productName || item.typeName || '驱虫' }}</text>
              </view>
            </view>
          </view>

          <view v-if="!dayCheckins.length && !dayWeight && !dayVaccines.length && !dayParasites.length" class="detail-empty">
            <text class="detail-empty-text">当日暂无记录</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { DEFAULT_PET_AVATAR_URL } from '@/utils/index'
import * as petApi from '@/api/pet'
import * as checkinApi from '@/api/checkin'
import * as healthApi from '@/api/health'
import AvatarView from '@/components/AvatarView.vue'

export default {
  components: { AvatarView },
  data() {
    return {
      statusBarHeight: 20,
      defaultPetAvatar: DEFAULT_PET_AVATAR_URL,
      pets: [],
      currentPetId: null,
      currentYear: new Date().getFullYear(),
      currentMonth: new Date().getMonth() + 1,
      weekdays: ['日', '一', '二', '三', '四', '五', '六'],
      calendarDays: [],
      selectedDate: '',
      checkinMap: {},
      weightMap: {},
      vaccineMap: {},
      parasiteMap: {},
      dayCheckins: [],
      dayWeight: null,
      dayVaccines: [],
      dayParasites: []
    }
  },
  onLoad(options) {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
    if (options.petId) {
      this.currentPetId = Number(options.petId)
    }
  },
  async onShow() {
    await this.loadPets()
    this.loadCalendarData()
  },
  methods: {
    goBack() { uni.navigateBack({ delta: 1 }) },

    async loadPets() {
      try {
        const res = await petApi.getPetList()
        if (res && res.success && Array.isArray(res.data)) {
          this.pets = res.data
          if (this.pets.length && !this.currentPetId) {
            this.currentPetId = this.pets[0].id
          }
        }
      } catch (e) {
        console.error('加载宠物失败:', e)
      }
    },

    switchPet(petId) {
      this.currentPetId = petId
      this.loadCalendarData()
    },

    prevMonth() {
      if (this.currentMonth === 1) { this.currentMonth = 12; this.currentYear-- }
      else { this.currentMonth-- }
      this.loadCalendarData()
    },

    nextMonth() {
      const now = new Date()
      if (this.currentYear === now.getFullYear() && this.currentMonth === now.getMonth() + 1) return
      if (this.currentMonth === 12) { this.currentMonth = 1; this.currentYear++ }
      else { this.currentMonth++ }
      this.loadCalendarData()
    },

    async loadCalendarData() {
      if (!this.currentPetId) return
      this.selectedDate = ''
      this.dayCheckins = []
      this.dayWeight = null
      this.dayVaccines = []
      this.dayParasites = []

      try {
        const [checkinRes, weightRes, vaccineRes, parasiteRes] = await Promise.all([
          checkinApi.getCalendar(this.currentYear, this.currentMonth, this.currentPetId),
          healthApi.getWeightRecordsByRange(this.currentPetId,
            `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}-01`,
            this.getLastDayStr()
          ),
          petApi.getVaccineReminders(this.currentPetId),
          petApi.getParasiteReminders(this.currentPetId)
        ])

        this.checkinMap = {}
        if (checkinRes && checkinRes.success && Array.isArray(checkinRes.data)) {
          checkinRes.data.forEach(r => {
            const dateStr = this.formatDateStr(r.recordDate)
            if (!this.checkinMap[dateStr]) this.checkinMap[dateStr] = []
            this.checkinMap[dateStr].push(r)
          })
        }

        this.weightMap = {}
        if (weightRes && weightRes.success && Array.isArray(weightRes.data)) {
          weightRes.data.forEach(r => {
            this.weightMap[this.formatDateStr(r.recordDate)] = r.weight
          })
        }

        this.vaccineMap = {}
        if (vaccineRes && vaccineRes.success && Array.isArray(vaccineRes.data)) {
          vaccineRes.data.forEach(r => {
            if (r.nextDate) {
              const dateStr = this.formatDateStr(r.nextDate)
              if (!this.vaccineMap[dateStr]) this.vaccineMap[dateStr] = []
              this.vaccineMap[dateStr].push(r)
            }
          })
        }

        this.parasiteMap = {}
        if (parasiteRes && parasiteRes.success && Array.isArray(parasiteRes.data)) {
          parasiteRes.data.forEach(r => {
            if (r.nextDate) {
              const dateStr = this.formatDateStr(r.nextDate)
              if (!this.parasiteMap[dateStr]) this.parasiteMap[dateStr] = []
              this.parasiteMap[dateStr].push(r)
            }
          })
        }

        this.buildCalendar()
      } catch (e) {
        console.error('加载日历数据失败:', e)
        this.buildCalendar()
      }
    },

    getLastDayStr() {
      const lastDay = new Date(this.currentYear, this.currentMonth, 0).getDate()
      return `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`
    },

    formatDateStr(date) {
      if (!date) return ''
      const d = new Date(date)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },

    buildCalendar() {
      const firstDay = new Date(this.currentYear, this.currentMonth - 1, 1).getDay()
      const daysInMonth = new Date(this.currentYear, this.currentMonth, 0).getDate()
      const today = new Date()
      const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

      const days = []
      for (let i = 0; i < firstDay; i++) {
        days.push({ day: 0 })
      }
      for (let d = 1; d <= daysInMonth; d++) {
        const dateStr = `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}-${String(d).padStart(2, '0')}`
        days.push({
          day: d,
          dateStr,
          isToday: dateStr === todayStr,
          hasCheckin: !!this.checkinMap[dateStr],
          hasWeight: !!this.weightMap[dateStr],
          hasVaccine: !!this.vaccineMap[dateStr],
          hasParasite: !!this.parasiteMap[dateStr]
        })
      }
      this.calendarDays = days
    },

    onDayTap(day) {
      if (!day.day) return
      this.selectedDate = day.dateStr
      this.dayCheckins = this.checkinMap[day.dateStr] || []
      this.dayWeight = this.weightMap[day.dateStr] || null
      this.dayVaccines = this.vaccineMap[day.dateStr] || []
      this.dayParasites = this.parasiteMap[day.dateStr] || []
    }
  }
}
</script>

<style lang="scss" scoped>
/* ============================================
   宠物日历 - 高饱和活力设计 v6.0
   与挑战赛/设置/相册风格统一
   ============================================ */

/* ========== 设计变量 ========== */
$primary: #ff5500;
$primary-soft: #ff7a3d;

$checkin: #16a34a;
$checkin-bg: #dcfce7;
$weight: #2563eb;
$weight-bg: #dbeafe;
$vaccine: #d97706;
$vaccine-bg: #fef3c7;
$parasite: #9333ea;
$parasite-bg: #f3e8ff;

$bg: #fff5f0;
$white: #ffffff;
$text-dark: #1c1917;
$text-mid: #44403c;
$text-light: #a8a29e;

/* ========== 页面基础 ========== */
.calendar-page {
  min-height: 100vh;
  background: $bg;
}

/* ========== 导航栏 ========== */
.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: $white; }
.status-bar { width: 100%; }

.nav-bar {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  border-bottom: 2rpx solid #f5ebe5;
}

.nav-back {
  width: 64rpx; height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 32rpx;
  background: #f5f5f4;

  &:active { transform: scale(0.9); background: #e7e5e4; }
}

.nav-back-arrow {
  width: 18rpx; height: 18rpx;
  border-left: 3rpx solid $text-dark;
  border-bottom: 3rpx solid $text-dark;
  transform: rotate(45deg);
  margin-left: -3rpx;
}

.nav-title { font-size: 36rpx; font-weight: 900; color: $text-dark; letter-spacing: 0.5rpx; }
.nav-placeholder { width: 64rpx; }

.page-scroll { height: 100vh; }
.page-content { padding: 24rpx 28rpx 120rpx; }

/* ========== 宠物切换标签 ========== */
.pet-tabs { margin-bottom: 28rpx; }
.pet-tabs-scroll { white-space: nowrap; }

.pet-tab {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  margin-right: 20rpx;
  padding: 14rpx 18rpx;
  border-radius: 22rpx;
  background: $white;
  border: 2rpx solid transparent;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 2rpx 12rpx rgba(255, 85, 0, 0.06);

  &.active {
    background: linear-gradient(135deg, #fff7f2, #fff);
    border-color: $primary-soft;
    box-shadow: 0 4rpx 20rpx rgba(255, 85, 0, 0.15);
  }
}

.pet-tab-avatar {
  width: 76rpx; height: 76rpx;
  border-radius: 50%;
  border: 3rpx solid #e7e5e4;
  margin-bottom: 8rpx;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  transition: border-color 0.25s ease;

  .pet-tab.active & { border-color: $primary; box-shadow: 0 0 0 3rpx rgba(255, 85, 0, 0.12); }
}

.pet-tab-name { font-size: 23rpx; color: $text-light; font-weight: 500; }
.pet-tab.active .pet-tab-name { color: $primary; font-weight: 800; }

/* ========== 日历卡片 ========== */
.calendar-card {
  background: $white;
  border-radius: 28rpx;
  padding: 28rpx 24rpx 24rpx;
  margin-bottom: 24rpx;
  box-shadow:
    0 4rpx 24rpx rgba(255, 85, 0, 0.08),
    0 1rpx 4rpx rgba(255, 85, 0, 0.03);
  border: 1.5rpx solid rgba(255, 122, 61, 0.08);
}

/* 日历头部：月份切换 */
.calendar-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 26rpx;
  gap: 36rpx;
}

.month-arrow {
  width: 60rpx; height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 30rpx;
  background: linear-gradient(135deg, #fff5f2, #fff);
  border: 1.5rpx solid #ffe8dd;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:active {
    transform: scale(0.88);
    background: linear-gradient(135deg, #ffe8dd, #fff5f2);
    box-shadow: 0 2rpx 10rpx rgba(255, 85, 0, 0.15);
  }
}

.arrow-icon { font-size: 38rpx; color: $primary; font-weight: 900; line-height: 1; }

.month-text {
  font-size: 34rpx;
  font-weight: 900;
  color: $text-dark;
  letter-spacing: 1rpx;
  min-width: 200rpx;
  text-align: center;
}

/* 星期行 */
.weekday-row { display: flex; margin-bottom: 14rpx; padding: 0 4rpx; }

.weekday-text {
  flex: 1;
  text-align: center;
  font-size: 24rpx;
  color: $text-light;
  font-weight: 700;
  letter-spacing: 0.5rpx;
}

/* 日期网格 */
.days-grid { display: flex; flex-wrap: wrap; gap: 2rpx; }

.day-cell {
  width: calc((100% - 12rpx) / 7);
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  position: relative;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  background: transparent;

  &:active:not(.day-empty) {
    transform: scale(0.92);
    background: #fef3e8;
  }
}

.day-empty { pointer-events: none; }

.day-today {
  .day-number {
    color: $primary;
    font-weight: 900;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -2rpx;
      left: 50%;
      transform: translateX(-50%);
      width: 28rpx;
      height: 4rpx;
      border-radius: 2rpx;
      background: $primary;
    }
  }
}

.day-selected {
  background: $primary !important;
  box-shadow: 0 4rpx 16rpx rgba(255, 85, 0, 0.35);

  .day-number { color: #fff !important; font-weight: 900; }
}

.day-number {
  font-size: 28rpx;
  color: $text-dark;
  font-weight: 600;
  line-height: 1.2;
  transition: all 0.2s ease;
}

/* 日期底部圆点标记 */
.day-dots {
  display: flex;
  gap: 4rpx;
  margin-top: 5rpx;
  height: 11rpx;
  align-items: center;
  justify-content: center;
}

.dot {
  width: 9rpx; height: 9rpx;
  border-radius: 5rpx;
  box-shadow: 0 1rpx 3rpx rgba(0, 0, 0, 0.15);
}

.dot-checkin { background: $checkin; box-shadow: 0 1rpx 4rpx rgba(22, 163, 74, 0.35); }
.dot-weight { background: $weight; box-shadow: 0 1rpx 4rpx rgba(37, 99, 235, 0.35); }
.dot-vaccine { background: $vaccine; box-shadow: 0 1rpx 4rpx rgba(217, 119, 6, 0.35); }
.dot-parasite { background: $parasite; box-shadow: 0 1rpx 4rpx rgba(147, 51, 234, 0.35); }

.day-selected {
  .dot-checkin { background: #bbf7d0; box-shadow: none; }
  .dot-weight { background: #bfdbfe; box-shadow: none; }
  .dot-vaccine { background: #fde68a; box-shadow: none; }
  .dot-parasite { background: #e9d5ff; box-shadow: none; }
}

/* ========== 图例 ========== */
.legend {
  display: flex;
  gap: 20rpx;
  justify-content: center;
  margin-bottom: 28rpx;
  padding: 18rpx 24rpx;
  background: $white;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(255, 85, 0, 0.05);
  border: 1.5rpx solid rgba(255, 122, 61, 0.06);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.legend-dot {
  width: 14rpx; height: 14rpx;
  border-radius: 7rpx;
  box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.12);

  &--checkin { background: $checkin; }
  &--weight { background: $weight; }
  &--vaccine { background: $vaccine; }
  &--parasite { background: $parasite; }
}

.legend-text { font-size: 23rpx; color: $text-mid; font-weight: 600; }

/* ========== 当日详情面板 ========== */
.day-detail {
  background: $white;
  border-radius: 28rpx;
  padding: 28rpx 26rpx 26rpx;
  margin-bottom: 24rpx;
  box-shadow:
    0 4rpx 24rpx rgba(255, 85, 0, 0.08),
    0 1rpx 4rpx rgba(255, 85, 0, 0.03);
  border: 1.5rpx solid rgba(255, 122, 61, 0.08);
  animation: detailSlideUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes detailSlideUp {
  from { opacity: 0; transform: translateY(16rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.detail-title {
  display: block;
  font-size: 30rpx;
  font-weight: 900;
  color: $text-dark;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 2rpx solid #f5ebe5;
  letter-spacing: 0.5rpx;
}

.detail-section { margin-bottom: 22rpx; }

.detail-section-title {
  display: block;
  font-size: 27rpx;
  font-weight: 800;
  color: $text-mid;
  margin-bottom: 14rpx;
  letter-spacing: 0.3rpx;
}

.detail-list { display: flex; flex-wrap: wrap; gap: 12rpx; }

.detail-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 12rpx 20rpx;
  background: $checkin-bg;
  border-radius: 16rpx;
  border: 1.5rpx solid rgba(22, 163, 74, 0.12);
  transition: all 0.2s ease;

  &:active { transform: scale(0.96); }
}

.detail-item-icon { font-size: 28rpx; }
.detail-item-name { font-size: 26rpx; color: $text-dark; font-weight: 700; }

.detail-value {
  display: block;
  font-size: 32rpx;
  color: $weight;
  font-weight: 900;
  letter-spacing: 0.5rpx;
}

.detail-empty {
  padding: 40rpx 0;
  text-align: center;
}
.detail-empty-text { font-size: 27rpx; color: $text-light; font-weight: 500; }
</style>
