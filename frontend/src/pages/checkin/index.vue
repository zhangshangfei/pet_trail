<template>
  <view class="checkin-page">
    <user-top-bar
      :status-bar-height="statusBarHeight"
      :avatar="userAvatar"
      :name="userName"
      right-icon="🔔"
      @rightTap="onTopBarBell"
    />
    <scroll-view scroll-y class="checkin-scroll" :style="{ paddingTop: headerHeight + 'px' }">
      <view class="container">
    <view class="pet-card card" v-if="pet">
      <view class="pet-header">
        <image class="pet-avatar" :src="petAvatar" mode="aspectFill"></image>
        <view class="pet-info">
          <text class="pet-name">{{ pet.name || '我的宠物' }}</text>
          <text class="pet-detail">{{ petDetail }}</text>
        </view>
        <view class="pet-edit" @tap="onEditPet">
          <text class="edit-icon">✏️</text>
        </view>
      </view>

      <view class="pet-meta-row">
        <view class="streak-badge">
          <text class="streak-icon">🔥</text>
          <text class="streak-text">连续打卡 {{ streakDays }} 天</text>
        </view>
        <picker v-if="pets.length > 1" class="pet-picker" :range="petNames" :value="petIndex" @change="onPetChange">
          <view class="pet-switcher">
            <text class="pet-switcher-label">{{ pet.name || '切换宠物' }}</text>
            <text class="pet-switcher-arrow">⌄</text>
          </view>
        </picker>
      </view>
    </view>

    <view class="empty-card card" v-else>
      <text class="empty-card-text">请先添加宠物</text>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">📋 今日打卡</text>
        <text class="section-date">{{ today }}</text>
      </view>

      <view class="checkin-grid">
        <view
          v-for="item in checkinItems"
          :key="item.id || item.code"
          class="checkin-item"
          :class="{ checked: item.checked }"
          @tap="onCheckIn(item)"
        >
          <view class="checkin-icon-wrapper">
            <text class="checkin-emoji">{{ item.emoji }}</text>
            <view class="check-success" :class="{ show: item.checked }">
              <text class="check-icon">✓</text>
            </view>
          </view>
          <text class="checkin-label">{{ item.label }}</text>
          <text v-if="item.checked" class="checkin-time">{{ item.checkTime }}</text>
          <text v-else class="checkin-time-placeholder">未打卡</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">📝 打卡记录</text>
      </view>

      <view v-if="recentRecords.length" class="records-list">
        <view v-for="record in recentRecords" :key="record.date" class="record-card card">
          <view class="record-header">
            <text class="record-date">{{ record.displayDate }}</text>
            <text class="record-count">完成 {{ record.completedCount }}/{{ record.totalCount }}</text>
          </view>
          <view class="record-items">
            <view
              v-for="recordItem in record.items"
              :key="record.date + '-' + (recordItem.id || recordItem.code)"
              class="record-item"
              :class="{ done: recordItem.checked }"
            >
              <text class="record-item-emoji">{{ recordItem.emoji }}</text>
              <text class="record-item-label">{{ recordItem.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <view v-else class="empty-records">
        <text class="empty-records-text">暂无打卡记录</text>
      </view>
    </view>

    <view class="checkin-animation" :class="{ show: showSuccessAnimation }">
      <view class="success-bubble">
        <text class="success-text">打卡成功！</text>
        <text class="success-emoji">🎉</text>
      </view>
    </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import UserTopBar from '@/components/UserTopBar.vue'

const DISPLAY_ITEM_PRESETS = [
  { code: 'feed', label: '喂食', emoji: '🍖' },
  { code: 'walk', label: '遛弯', emoji: '🚶' },
  { code: 'clean', label: '铲屎', emoji: '🧹' },
  { code: 'medicine', label: '喂药', emoji: '💊' }
]
const DEFAULT_PET_AVATAR = '/static/images/default-pet-avatar.png'

export default {
  components: {
    UserTopBar
  },
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 70,
      userName: '萌宠主人',
      userAvatar: 'https://ai-public.mastergo.com/ai/img_res/1774575365924a3K9mP2xQ7vN4rT8wY.jpg',
      petId: null,
      pets: [],
      pet: null,
      petAvatar: DEFAULT_PET_AVATAR,
      today: '',
      streakDays: 0,
      checkinItems: DISPLAY_ITEM_PRESETS.map((item, index) => ({
        ...item,
        uiIndex: index,
        id: null,
        checked: false,
        checkTime: ''
      })),
      allRecords: [],
      recentRecords: [],
      showSuccessAnimation: false,
      animationTimer: null
    }
  },
  computed: {
    petNames() {
      return this.pets.map((item) => item.name || '未命名宠物')
    },
    petIndex() {
      if (!this.petId) return 0
      const index = this.pets.findIndex((item) => String(item.id) === String(this.petId))
      return index > -1 ? index : 0
    },
    petDetail() {
      if (!this.pet) return '未填写信息'
      const breed = this.pet.breed || '未填写品种'
      const age = this.getAgeText(this.pet.birthday)
      return age ? `${breed} · ${age}` : breed
    }
  },
  onLoad(options) {
    this.initNavMetrics()
    this.loadUserProfile()
    if (options.petId || options.id) {
      this.petId = options.petId || options.id
    }
    this.today = this.getTodayString()
    this.initPage()
  },
  onShow() {
    if (this.petId || this.pets.length) {
      this.refreshPageData()
    }
  },
  onUnload() {
    if (this.animationTimer) {
      clearTimeout(this.animationTimer)
      this.animationTimer = null
    }
  },
  methods: {
    async initPage() {
      await this.loadPets()
      await this.loadCheckinItems()
      await this.refreshPageData()
    },
    async refreshPageData() {
      if (!this.petId && this.pets.length) {
        this.petId = this.pets[0].id
      }
      if (!this.petId) return
      await this.loadPetInfo()
      await this.loadCalendarRecords()
      this.syncPageState()
    },
    async loadPets() {
      try {
        const res = await uni.$request.get('/api/pets')
        if (res && res.success && Array.isArray(res.data)) {
          this.pets = res.data
          if (!this.pets.length) {
            this.pet = null
            this.petId = null
            return
          }
          const matched = this.pets.some((item) => String(item.id) === String(this.petId))
          this.petId = matched ? this.petId : this.pets[0].id
          this.syncPetFromList()
        }
      } catch (error) {
        console.error('加载宠物列表失败:', error)
      }
    },
    syncPetFromList() {
      if (!this.petId || !this.pets.length) return
      const selected = this.pets.find((item) => String(item.id) === String(this.petId))
      if (selected) {
        this.pet = selected
        this.petAvatar = selected.avatar || DEFAULT_PET_AVATAR
      }
    },
    async loadPetInfo() {
      if (!this.petId) return
      try {
        const res = await uni.$request.get(`/api/pets/${this.petId}`)
        if (res && res.success) {
          this.pet = res.data || null
          this.petAvatar = (this.pet && this.pet.avatar) || DEFAULT_PET_AVATAR
          if (this.pet) {
            uni.setStorageSync('petInfo', this.pet)
          }
        }
      } catch (error) {
        console.error('加载宠物信息失败:', error)
        this.syncPetFromList()
      }
    },
    async loadCheckinItems() {
      try {
        const res = await uni.$request.get('/api/checkin/items')
        if (res && res.success && Array.isArray(res.data) && res.data.length) {
          this.checkinItems = DISPLAY_ITEM_PRESETS.map((preset, index) => {
            const apiItem = res.data[index] || {}
            return {
              ...preset,
              uiIndex: index,
              id: apiItem.id || null,
              backendName: apiItem.name || preset.label,
              backendIcon: apiItem.icon || preset.emoji,
              checked: false,
              checkTime: ''
            }
          })
        }
      } catch (error) {
        console.error('加载打卡项失败:', error)
      }
    },
    async loadCalendarRecords() {
      const requests = this.getMonthRequests(3)
      const result = []
      for (const item of requests) {
        try {
          const res = await uni.$request.get('/api/checkin/calendar', item)
          if (res && res.success && Array.isArray(res.data)) {
            result.push(...res.data)
          }
        } catch (error) {
          console.error('加载打卡记录失败:', error)
        }
      }
      this.allRecords = this.filterPetRecords(result)
    },
    getMonthRequests(count) {
      const requests = []
      const now = new Date()
      for (let i = 0; i < count; i += 1) {
        const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
        requests.push({
          year: date.getFullYear(),
          month: date.getMonth() + 1
        })
      }
      return requests
    },
    filterPetRecords(records) {
      const map = new Map()
      records.forEach((item) => {
        if (!item || item.status !== 1) return
        if (this.petId && item.petId && String(item.petId) !== String(this.petId)) return
        const key = item.id || `${item.itemId}-${item.recordDate}-${item.createdAt || ''}`
        if (!map.has(key)) {
          map.set(key, item)
        }
      })
      return Array.from(map.values())
    },
    syncPageState() {
      try {
        const groupedRecords = this.groupRecordsByDate(this.allRecords)
        const todayKey = this.getDateKey(new Date())
        const todayRecord = groupedRecords[todayKey] || []

        this.checkinItems = this.checkinItems.map((item) => {
          const matched = todayRecord.find((record) => String(record.itemId) === String(item.id))
          return {
            ...item,
            checked: !!matched,
            checkTime: matched ? this.getTimeText(matched.createdAt) : ''
          }
        })

        this.recentRecords = Object.keys(groupedRecords)
          .sort((a, b) => {
            const left = this.normalizeDate(a)
            const right = this.normalizeDate(b)
            if (!left && !right) return 0
            if (!left) return 1
            if (!right) return -1
            return right.getTime() - left.getTime()
          })
          .slice(0, 5)
          .map((date) => {
            const records = groupedRecords[date]
            const items = this.checkinItems.map((item) => ({
              ...item,
              checked: records.some((record) => String(record.itemId) === String(item.id))
            }))
            return {
              date,
              displayDate: this.formatRecordDate(date),
              items,
              completedCount: items.filter((item) => item.checked).length,
              totalCount: items.length
            }
          })

        this.streakDays = this.calculateStreak(Object.keys(groupedRecords))
      } catch (error) {
        console.error('syncPageState failed:', error)
        uni.showToast({
          title: '页面数据处理失败',
          icon: 'none'
        })
      }
    },
    groupRecordsByDate(records) {
      return records.reduce((acc, item) => {
        const key = this.getDateKey(item.recordDate || item.createdAt)
        if (!key) return acc
        if (!acc[key]) {
          acc[key] = []
        }
        acc[key].push(item)
        return acc
      }, {})
    },
    calculateStreak(dates) {
      if (!dates.length) return 0
      const sorted = dates
        .map((date) => this.normalizeDate(date))
        .filter(Boolean)
        .sort((a, b) => b.getTime() - a.getTime())
      if (!sorted.length) return 0
      let streak = 1
      for (let i = 1; i < sorted.length; i += 1) {
        const diff = Math.round((sorted[i - 1].getTime() - sorted[i].getTime()) / 86400000)
        if (diff === 1) {
          streak += 1
        } else if (diff > 1) {
          break
        }
      }
      return streak
    },
    async onCheckIn(item) {
      if (!this.petId) {
        uni.showToast({
          title: '请先选择宠物',
          icon: 'none'
        })
        return
      }
      if (item.checked) {
        return
      }
      if (!item.id) {
        uni.showToast({
          title: '打卡项未配置',
          icon: 'none'
        })
        return
      }

      try {
        const res = await uni.$request.post('/api/checkin', {
          petId: this.petId,
          itemId: item.id
        })

        if (!res || res.success !== true) {
          uni.showToast({
            title: (res && res.message) || '打卡失败',
            icon: 'none'
          })
          return
        }

        try {
          uni.vibrateShort({ type: 'medium' })
        } catch (e) {
          // ignore
        }

        this.showSuccessFeedback()
        await this.loadCalendarRecords()
        this.syncPageState()
      } catch (error) {
        console.error('打卡失败:', error)
        uni.showToast({
          title: (error && error.message) || '打卡失败',
          icon: 'none'
        })
      }
    },
    showSuccessFeedback() {
      this.showSuccessAnimation = true
      if (this.animationTimer) {
        clearTimeout(this.animationTimer)
      }
      this.animationTimer = setTimeout(() => {
        this.showSuccessAnimation = false
      }, 1200)
    },
    async onPetChange(event) {
      const index = Number(event.detail.value || 0)
      const pet = this.pets[index]
      if (!pet || !pet.id) return
      this.petId = pet.id
      this.syncPetFromList()
      await this.refreshPageData()
    },
    onEditPet() {
      uni.showToast({
        title: '编辑宠物信息',
        icon: 'none'
      })
    },
    onTopBarBell() {
      uni.showToast({
        title: '通知未实现',
        icon: 'none'
      })
    },
    initNavMetrics() {
      try {
        const sys = uni.getSystemInfoSync()
        this.statusBarHeight = (sys && sys.statusBarHeight) || 20
      } catch (e) {
        this.statusBarHeight = 20
      }
      this.headerHeight = this.statusBarHeight + 50
    },
    async loadUserProfile() {
      try {
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo) {
          if (userInfo.nickname) this.userName = userInfo.nickname
          if (userInfo.avatar) this.userAvatar = userInfo.avatar
        }
        const token = uni.getStorageSync('token')
        if (!token) return
        const res = await uni.$request.get('/api/users/profile')
        if (res && res.success && res.data) {
          if (res.data.nickname) this.userName = res.data.nickname
          if (res.data.avatar) this.userAvatar = res.data.avatar
          uni.setStorageSync('userInfo', res.data)
        }
      } catch (error) {
        console.error('loadUserProfile failed:', error)
      }
    },
    getTodayString() {
      const now = new Date()
      const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return `${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`
    },
    getAgeText(birthday) {
      const date = this.normalizeDate(birthday)
      if (!date) return ''
      const now = new Date()
      let years = now.getFullYear() - date.getFullYear()
      let months = now.getMonth() - date.getMonth()
      if (now.getDate() < date.getDate()) {
        months -= 1
      }
      if (months < 0) {
        years -= 1
        months += 12
      }
      if (years > 0) {
        return `${years}岁`
      }
      return `${Math.max(months, 1)}个月`
    },
    getDateKey(input) {
      const date = this.normalizeDate(input)
      if (!date) return ''
      const year = date.getFullYear()
      const month = `${date.getMonth() + 1}`.padStart(2, '0')
      const day = `${date.getDate()}`.padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    getTimeText(input) {
      const date = this.normalizeDate(input)
      if (!date) return ''
      const hours = `${date.getHours()}`.padStart(2, '0')
      const minutes = `${date.getMinutes()}`.padStart(2, '0')
      return `${hours}:${minutes}`
    },
    formatRecordDate(dateString) {
      const date = this.normalizeDate(dateString)
      if (!date) return dateString
      const year = date.getFullYear()
      const month = `${date.getMonth() + 1}`.padStart(2, '0')
      const day = `${date.getDate()}`.padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    normalizeDate(input) {
      if (!input) return null
      if (input instanceof Date) return input
      const normalized = typeof input === 'string' ? input.replace(' ', 'T') : input
      const date = new Date(normalized)
      return Number.isNaN(date.getTime()) ? null : date
    }
  }
}
</script>

<style lang="scss" scoped>
.checkin-page {
  min-height: 100vh;
  background: #f7f3ef;
}

.checkin-scroll {
  height: 100vh;
}

.container {
  min-height: 100vh;
  padding: 32rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  background: #f7f3ef;
  --color-warm-yellow: #ffd76a;
  --color-warm-yellow-light: #ffe8a3;
  --color-light-green: #dff3d4;
  --color-light-green-dark: #c5e9af;
  --color-success: #8bc34a;
  --color-accent: #ff8f3d;
  --color-bg-card: rgba(255, 255, 255, 0.96);
  --color-border: #ece7df;
  --color-text-primary: #2f2a24;
  --color-text-secondary: #7b746c;
  --color-text-light: #b8b0a6;
  --radius-md: 24rpx;
  --radius-lg: 32rpx;
  --shadow-card: 0 12rpx 28rpx rgba(17, 24, 39, 0.08);
}

.card {
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}

.pet-card {
  background: linear-gradient(135deg, var(--color-warm-yellow) 0%, var(--color-warm-yellow-light) 100%);
  padding: 32rpx;
  margin-bottom: 32rpx;
}

.pet-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 24rpx;
}

.pet-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  margin-right: 24rpx;
}

.pet-info {
  flex: 1;
  min-width: 0;
}

.pet-name {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 8rpx;
}

.pet-detail {
  display: block;
  font-size: 24rpx;
  color: var(--color-text-secondary);
}

.pet-edit {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  transition: all 0.2s ease;
}

.pet-edit:active {
  transform: scale(0.9);
  background: rgba(255, 255, 255, 0.8);
}

.edit-icon {
  font-size: 28rpx;
}

.pet-picker {
  display: inline-block;
}

.pet-meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.streak-badge {
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  background: rgba(255, 255, 255, 0.8);
  padding: 12rpx 24rpx;
  border-radius: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.streak-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.streak-text {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--color-accent);
}

.pet-switcher {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 22rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.pet-switcher-label {
  font-size: 24rpx;
  color: var(--color-text-primary);
  font-weight: 600;
  max-width: 220rpx;
}

.pet-switcher-arrow {
  font-size: 20rpx;
  color: var(--color-text-secondary);
}

.section {
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
  color: var(--color-text-primary);
}

.section-date {
  font-size: 24rpx;
  color: var(--color-text-secondary);
}

.checkin-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.checkin-item {
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  padding: 32rpx 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: var(--shadow-card);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.checkin-item:active {
  transform: scale(0.95);
}

.checkin-item.checked {
  background: linear-gradient(135deg, var(--color-light-green) 0%, var(--color-light-green-dark) 100%);
}

.checkin-item.checked::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 60rpx;
  height: 60rpx;
  background: var(--color-success);
  border-radius: 0 0 0 60rpx;
}

.checkin-icon-wrapper {
  width: 100rpx;
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
  position: relative;
}

.checkin-emoji {
  font-size: 56rpx;
}

.check-success {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 40rpx;
  height: 40rpx;
  background: var(--color-success);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0);
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.check-success.show {
  opacity: 1;
  transform: scale(1);
}

.check-icon {
  color: #ffffff;
  font-size: 24rpx;
  font-weight: bold;
}

.checkin-label {
  font-size: 28rpx;
  font-weight: 500;
  color: var(--color-text-primary);
  margin-bottom: 8rpx;
}

.checkin-time {
  font-size: 22rpx;
  color: var(--color-text-secondary);
}

.checkin-time-placeholder {
  font-size: 22rpx;
  color: var(--color-text-light);
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.record-card {
  padding: 24rpx;
}

.record-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid var(--color-border);
}

.record-date {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--color-text-primary);
}

.record-count {
  font-size: 24rpx;
  color: var(--color-accent);
  font-weight: 500;
}

.record-items {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 16rpx;
}

.record-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 12rpx 20rpx;
  background: var(--color-warm-yellow-light);
  border-radius: 30rpx;
  opacity: 0.5;
}

.record-item.done {
  background: var(--color-light-green);
  opacity: 1;
}

.record-item-emoji {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.record-item-label {
  font-size: 24rpx;
  color: var(--color-text-primary);
}

.empty-card,
.empty-records {
  padding: 48rpx 32rpx;
  text-align: center;
}

.empty-card-text,
.empty-records-text {
  font-size: 28rpx;
  color: var(--color-text-secondary);
}

.checkin-animation {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  opacity: 0;
  z-index: 1000;
  transition: opacity 0.3s ease;
}

.checkin-animation.show {
  opacity: 1;
}

.success-bubble {
  background: var(--color-success);
  padding: 40rpx 60rpx;
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 16rpx 48rpx rgba(139, 195, 74, 0.4);
  animation: bubblePop 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.success-text {
  font-size: 36rpx;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 16rpx;
}

.success-emoji {
  font-size: 64rpx;
  animation: emojiCelebrate 0.8s ease infinite;
}

@keyframes bubblePop {
  0% {
    transform: scale(0);
    opacity: 0;
  }

  50% {
    transform: scale(1.1);
  }

  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes emojiCelebrate {
  0%,
  100% {
    transform: rotate(0deg) scale(1);
  }

  25% {
    transform: rotate(-15deg) scale(1.1);
  }

  75% {
    transform: rotate(15deg) scale(1.1);
  }
}
</style>
