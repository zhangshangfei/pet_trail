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
              <avatar-view class="pet-tab-avatar" :src="pet.avatar || ''" :name="pet.name || ''" :id="pet.id" :size="72" />
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
          <view class="legend-item"><view class="dot dot-checkin"></view><text class="legend-text">打卡</text></view>
          <view class="legend-item"><view class="dot dot-weight"></view><text class="legend-text">体重</text></view>
          <view class="legend-item"><view class="dot dot-vaccine"></view><text class="legend-text">疫苗</text></view>
          <view class="legend-item"><view class="dot dot-parasite"></view><text class="legend-text">驱虫</text></view>
        </view>

        <view v-if="selectedDate" class="day-detail">
          <text class="detail-title">{{ selectedDate }} 记录</text>

          <view v-if="dayCheckins.length" class="detail-section">
            <text class="detail-section-title">✅ 打卡记录</text>
            <view class="detail-list">
              <view v-for="item in dayCheckins" :key="item.id" class="detail-item">
                <text class="detail-item-icon">{{ item.icon || '📋' }}</text>
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
                <text class="detail-item-name">{{ item.productName || item.type }}</text>
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
        const res = await uni.$request.get('/api/pets')
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
          uni.$request.get('/api/checkin/calendar', { year: this.currentYear, month: this.currentMonth }),
          uni.$request.get(`/api/pets/${this.currentPetId}/weight-records/range`, {
            startDate: `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}-01`,
            endDate: this.getLastDayStr()
          }),
          uni.$request.get(`/api/pets/${this.currentPetId}/vaccine-reminders`),
          uni.$request.get(`/api/pets/${this.currentPetId}/parasite-reminders`)
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
$primary: #ff6a3d;
$bg: #f5f5f5;
$card-bg: #ffffff;
$text-primary: #1a1a1a;
$text-secondary: #666666;
$text-light: #999999;

.calendar-page { min-height: 100vh; background: $bg; }
.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: #fff; }
.status-bar { width: 100%; }
.nav-bar { height: 92rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 28rpx; border-bottom: 1rpx solid #f0f0f0; }
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-back-arrow { width: 20rpx; height: 20rpx; border-left: 4rpx solid $text-primary; border-bottom: 4rpx solid $text-primary; transform: rotate(45deg); }
.nav-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.nav-placeholder { width: 60rpx; }
.page-scroll { height: 100vh; }
.page-content { padding: 24rpx; }

.pet-tabs { margin-bottom: 20rpx; }
.pet-tabs-scroll { white-space: nowrap; }
.pet-tab { display: inline-flex; flex-direction: column; align-items: center; margin-right: 24rpx; padding: 12rpx; }
.pet-tab-avatar { margin-bottom: 6rpx; border: 3rpx solid #e5e7eb; border-radius: 50%; }
.pet-tab.active .pet-tab-avatar { border-color: $primary; }
.pet-tab-name { font-size: 22rpx; color: $text-secondary; }
.pet-tab.active .pet-tab-name { color: $primary; font-weight: 600; }

.calendar-card { background: $card-bg; border-radius: 24rpx; padding: 24rpx; margin-bottom: 20rpx; }
.calendar-header { display: flex; align-items: center; justify-content: center; margin-bottom: 20rpx; gap: 32rpx; }
.month-arrow { width: 56rpx; height: 56rpx; display: flex; align-items: center; justify-content: center; border-radius: 28rpx; background: #f5f5f5; }
.arrow-icon { font-size: 36rpx; color: $primary; font-weight: 700; }
.month-text { font-size: 30rpx; font-weight: 600; color: $text-primary; }

.weekday-row { display: flex; margin-bottom: 12rpx; }
.weekday-text { flex: 1; text-align: center; font-size: 24rpx; color: $text-light; font-weight: 500; }

.days-grid { display: flex; flex-wrap: wrap; }
.day-cell {
  width: calc(100% / 7); aspect-ratio: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; border-radius: 12rpx;
  position: relative; transition: background 0.2s;
}
.day-cell:active { background: #f5f5f5; }
.day-empty { pointer-events: none; }
.day-today .day-number { color: $primary; font-weight: 700; }
.day-selected { background: $primary !important; }
.day-selected .day-number { color: #fff !important; }
.day-number { font-size: 26rpx; color: $text-primary; }
.day-dots { display: flex; gap: 4rpx; margin-top: 4rpx; height: 10rpx; }
.dot { width: 8rpx; height: 8rpx; border-radius: 4rpx; }
.dot-checkin { background: #52c41a; }
.dot-weight { background: #1890ff; }
.dot-vaccine { background: #faad14; }
.dot-parasite { background: #722ed1; }
.day-selected .dot-checkin { background: #b7eb8f; }
.day-selected .dot-weight { background: #91d5ff; }
.day-selected .dot-vaccine { background: #ffe58f; }
.day-selected .dot-parasite { background: #d3adf7; }

.legend { display: flex; gap: 24rpx; justify-content: center; margin-bottom: 24rpx; }
.legend-item { display: flex; align-items: center; gap: 6rpx; }
.legend-text { font-size: 22rpx; color: $text-light; }

.day-detail { background: $card-bg; border-radius: 24rpx; padding: 24rpx; margin-bottom: 24rpx; }
.detail-title { display: block; font-size: 28rpx; font-weight: 600; color: $text-primary; margin-bottom: 20rpx; }
.detail-section { margin-bottom: 20rpx; }
.detail-section-title { display: block; font-size: 26rpx; font-weight: 500; color: $text-secondary; margin-bottom: 12rpx; }
.detail-list { display: flex; flex-wrap: wrap; gap: 12rpx; }
.detail-item { display: flex; align-items: center; gap: 8rpx; padding: 8rpx 16rpx; background: #f5f5f5; border-radius: 12rpx; }
.detail-item-icon { font-size: 24rpx; }
.detail-item-name { font-size: 24rpx; color: $text-primary; }
.detail-value { font-size: 28rpx; color: $primary; font-weight: 600; }
.detail-empty { padding: 24rpx 0; text-align: center; }
.detail-empty-text { font-size: 26rpx; color: $text-light; }
</style>
