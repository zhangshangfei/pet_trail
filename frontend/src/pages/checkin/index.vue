<template>
  <view class="checkin-page">
    <view class="page-header">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="header-bar">
        <view class="header-user" @tap="goProfile">
          <image class="header-avatar" :src="userAvatar" mode="aspectFill" />
          <text class="header-name">{{ userName }}</text>
        </view>
        <view class="header-actions">
          <view class="header-action-btn" @tap="goNotifications">
            <text class="header-action-icon">🔔</text>
            <view v-if="unreadCount > 0" class="header-badge">
              <text class="header-badge-text">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: headerHeight + 'px' }">
      <view class="page-content">

        <view class="pet-card" v-if="pet">
          <view class="pet-card-bg"></view>
          <view class="pet-card-content">
            <view class="pet-main">
              <image class="pet-avatar" :src="petAvatar" mode="aspectFill" />
              <view class="pet-info">
                <text class="pet-name">{{ pet.name || '我的宠物' }}</text>
                <text class="pet-breed">{{ petDetail }}</text>
              </view>
              <picker v-if="pets.length > 1" class="pet-picker" :range="petNames" :value="petIndex" @change="onPetChange">
                <view class="pet-switch-btn">
                  <text class="pet-switch-text">切换</text>
                </view>
              </picker>
            </view>
            <view class="pet-stats">
              <view class="stat-item">
                <text class="stat-value">{{ streakDays }}</text>
                <text class="stat-label">连续天数</text>
              </view>
              <view class="stat-divider"></view>
              <view class="stat-item">
                <text class="stat-value">{{ todayCompleted }}/{{ visibleItems.length }}</text>
                <text class="stat-label">今日完成</text>
              </view>
              <view class="stat-divider"></view>
              <view class="stat-item">
                <text class="stat-value">{{ totalCheckins }}</text>
                <text class="stat-label">累计打卡</text>
              </view>
            </view>
          </view>
        </view>

        <view class="empty-pet-card" v-else @tap="goAddPet">
          <text class="empty-pet-icon">🐾</text>
          <text class="empty-pet-text">添加你的第一只宠物</text>
          <text class="empty-pet-arrow">→</text>
        </view>

        <view class="section">
          <view class="section-header">
            <view class="section-title-group">
              <text class="section-title">今日打卡</text>
              <text class="section-date">{{ today }}</text>
            </view>
            <view class="section-add-btn" @tap="goAddItem">
              <text class="section-add-icon">＋</text>
              <text class="section-add-text">添加</text>
            </view>
          </view>

          <view class="section-toolbar">
            <view class="toolbar-item" @tap="goReport">
              <view class="toolbar-icon-wrap">
                <text class="toolbar-icon">📊</text>
              </view>
              <text class="toolbar-label">报表</text>
            </view>
            <view class="toolbar-divider"></view>
            <view class="toolbar-item" @tap="goReminder">
              <view class="toolbar-icon-wrap">
                <text class="toolbar-icon">⏰</text>
              </view>
              <text class="toolbar-label">提醒</text>
            </view>
            <view class="toolbar-divider"></view>
            <view class="toolbar-item" @tap="goManageItems">
              <view class="toolbar-icon-wrap">
                <text class="toolbar-icon">⚙️</text>
              </view>
              <text class="toolbar-label">管理</text>
            </view>
          </view>

          <view class="checkin-grid" v-if="visibleItems.length">
            <view
              v-for="item in visibleItems"
              :key="item.id"
              class="checkin-item"
              :class="{ checked: item.checked }"
              @tap="onCheckIn(item)"
            >
              <view class="item-icon-box">
                <text class="item-emoji">{{ item.emoji || '📋' }}</text>
                <view v-if="item.checked" class="item-check-mark">
                  <text class="check-mark-icon">✓</text>
                </view>
              </view>
              <text class="item-name">{{ item.label }}</text>
              <text v-if="item.checked" class="item-time">{{ item.checkTime }}</text>
              <text v-else class="item-time-hint">点击打卡</text>
            </view>
          </view>

          <view class="empty-checkin" v-else>
            <text class="empty-checkin-text">暂无打卡项，点击右上角添加</text>
          </view>
        </view>

        <view class="section">
          <view class="section-header">
            <view class="section-title-group">
              <text class="section-title">打卡记录</text>
            </view>
          </view>

          <view v-if="recentRecords.length" class="records-list">
            <view v-for="record in recentRecords" :key="record.date" class="record-card">
              <view class="record-header">
                <text class="record-date">{{ record.displayDate }}</text>
                <view class="record-progress">
                  <view class="progress-bar">
                    <view class="progress-fill" :style="{ width: record.progress + '%' }"></view>
                  </view>
                  <text class="progress-text">{{ record.completedCount }}/{{ record.totalCount }}</text>
                </view>
              </view>
              <view class="record-items">
                <view
                  v-for="recordItem in record.items"
                  :key="record.date + '-' + recordItem.id"
                  class="record-item"
                  :class="{ done: recordItem.checked }"
                >
                  <text class="record-item-icon">{{ recordItem.emoji || '📋' }}</text>
                  <text class="record-item-name">{{ recordItem.label }}</text>
                </view>
              </view>
            </view>
          </view>

          <view v-else class="empty-records">
            <text class="empty-records-icon">📝</text>
            <text class="empty-records-text">暂无打卡记录</text>
          </view>
        </view>

        <view class="page-bottom-safe"></view>
      </view>
    </scroll-view>

    <view class="checkin-animation" :class="{ show: showSuccessAnimation }">
      <view class="success-bubble">
        <text class="success-emoji">🎉</text>
        <text class="success-text">打卡成功！</text>
      </view>
    </view>
  </view>
</template>

<script>
import { checkLogin, getUserAvatar, DEFAULT_USER_AVATAR } from '@/utils/index'

const DEFAULT_PET_AVATAR = '/static/images/default-pet-avatar.png'

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 70,
      userName: '萌宠主人',
      userAvatar: DEFAULT_USER_AVATAR,
      unreadCount: 0,
      petId: null,
      pets: [],
      pet: null,
      petAvatar: DEFAULT_PET_AVATAR,
      today: '',
      streakDays: 0,
      totalCheckins: 0,
      allItems: [],
      visibleItems: [],
      allRecords: [],
      recentRecords: [],
      showSuccessAnimation: false,
      animationTimer: null,
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
    },
    todayCompleted() {
      return this.visibleItems.filter((item) => item.checked).length
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
  async onShow() {
    await this.loadPets()
    await this.loadCheckinItems()
    await this.refreshPageData()
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
          if (this.pet) uni.setStorageSync('petInfo', this.pet)
        }
      } catch (error) {
        console.error('加载宠物信息失败:', error)
        this.syncPetFromList()
      }
    },
    async loadCheckinItems() {
      try {
        const res = await uni.$request.get('/api/checkin/items')
        if (res && res.success && Array.isArray(res.data)) {
          this.allItems = res.data.map((item) => ({
            id: item.id,
            name: item.name,
            label: item.name,
            emoji: item.icon || '📋',
            icon: item.icon || '📋',
            type: item.type || 1,
            isCustom: item.isDefault === 0,
            isDefault: item.isDefault === 1,
            hidden: item.hidden === true,
            checked: false,
            checkTime: ''
          }))
          this.visibleItems = this.allItems.filter((item) => !item.hidden)
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
        requests.push({ year: date.getFullYear(), month: date.getMonth() + 1 })
      }
      return requests
    },
    filterPetRecords(records) {
      const map = new Map()
      records.forEach((item) => {
        if (!item || item.status !== 1) return
        if (this.petId && item.petId && String(item.petId) !== String(this.petId)) return
        const key = item.id || `${item.itemId}-${item.recordDate}-${item.createdAt || ''}`
        if (!map.has(key)) map.set(key, item)
      })
      return Array.from(map.values())
    },
    syncPageState() {
      try {
        const groupedRecords = this.groupRecordsByDate(this.allRecords)
        const todayKey = this.getDateKey(new Date())
        const todayRecord = groupedRecords[todayKey] || []

        this.visibleItems = this.allItems
          .filter((item) => !item.hidden)
          .map((item) => {
            const matched = todayRecord.find((record) => String(record.itemId) === String(item.id))
            return {
              ...item,
              checked: !!matched,
              checkTime: matched ? this.getTimeText(matched.createdAt) : ''
            }
          })

        this.totalCheckins = this.allRecords.length

        const visibleItemIds = new Set(this.visibleItems.map((i) => String(i.id)))

        this.recentRecords = Object.keys(groupedRecords)
          .sort((a, b) => {
            const left = this.normalizeDate(a)
            const right = this.normalizeDate(b)
            if (!left && !right) return 0
            if (!left) return 1
            if (!right) return -1
            return right.getTime() - left.getTime()
          })
          .slice(0, 7)
          .map((date) => {
            const records = groupedRecords[date]
            const items = this.visibleItems.map((item) => ({
              id: item.id,
              label: item.label,
              emoji: item.emoji,
              checked: records.some((record) => String(record.itemId) === String(item.id))
            }))
            const completedCount = items.filter((item) => item.checked).length
            return {
              date,
              displayDate: this.formatRecordDate(date),
              items,
              completedCount,
              totalCount: items.length,
              progress: items.length > 0 ? Math.round((completedCount / items.length) * 100) : 0
            }
          })

        this.streakDays = this.calculateStreak(Object.keys(groupedRecords))
      } catch (error) {
        console.error('syncPageState failed:', error)
        uni.showToast({ title: '数据处理失败', icon: 'none' })
      }
    },
    groupRecordsByDate(records) {
      return records.reduce((acc, item) => {
        const key = this.getDateKey(item.recordDate || item.createdAt)
        if (!key) return acc
        if (!acc[key]) acc[key] = []
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
        if (diff === 1) streak += 1
        else if (diff > 1) break
      }
      return streak
    },
    async onCheckIn(item) {
      if (!this.petId) {
        uni.showToast({ title: '请先选择宠物', icon: 'none' })
        return
      }
      if (item.checked) return
      if (!item.id) {
        uni.showToast({ title: '打卡项未配置', icon: 'none' })
        return
      }
      const loggedIn = await checkLogin('请先登录后再打卡')
      if (!loggedIn) return

      try {
        const res = await uni.$request.post('/api/checkin', {
          petId: this.petId,
          itemId: item.id
        })
        if (!res || res.success !== true) {
          uni.showToast({ title: (res && res.message) || '打卡失败', icon: 'none' })
          return
        }
        try { uni.vibrateShort({ type: 'medium' }) } catch (e) { /* ignore */ }
        this.showSuccessFeedback()
        await this.loadCalendarRecords()
        this.syncPageState()
      } catch (error) {
        console.error('打卡失败:', error)
        uni.showToast({ title: (error && error.message) || '打卡失败', icon: 'none' })
      }
    },
    goManageItems() {
      uni.navigateTo({ url: '/pages/checkin/manage-items' })
    },
    goAddItem() {
      uni.navigateTo({ url: '/pages/checkin/add-item' })
    },
    goReport() {
      uni.navigateTo({ url: '/pages/checkin/report' })
    },
    goReminder() {
      uni.navigateTo({ url: '/pages/checkin/reminder' })
    },
    showSuccessFeedback() {
      this.showSuccessAnimation = true
      if (this.animationTimer) clearTimeout(this.animationTimer)
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
    goProfile() {
      uni.navigateTo({ url: '/pages/user/profile' })
    },
    goNotifications() {
      uni.navigateTo({ url: '/pages/notification/index' })
    },
    goAddPet() {
      uni.navigateTo({ url: '/pages/pet/edit' })
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
          if (userInfo.avatar) this.userAvatar = getUserAvatar(userInfo.id, userInfo.avatar)
        }
        const token = uni.getStorageSync('token')
        if (!token) return
        const res = await uni.$request.get('/api/users/profile')
        if (res && res.success && res.data) {
          if (res.data.nickname) this.userName = res.data.nickname
          this.userAvatar = getUserAvatar(res.data.id, res.data.avatar)
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
      if (now.getDate() < date.getDate()) months -= 1
      if (months < 0) { years -= 1; months += 12 }
      return years > 0 ? `${years}岁` : `${Math.max(months, 1)}个月`
    },
    getDateKey(input) {
      const date = this.normalizeDate(input)
      if (!date) return ''
      const y = date.getFullYear()
      const m = `${date.getMonth() + 1}`.padStart(2, '0')
      const d = `${date.getDate()}`.padStart(2, '0')
      return `${y}-${m}-${d}`
    },
    getTimeText(input) {
      const date = this.normalizeDate(input)
      if (!date) return ''
      return `${`${date.getHours()}`.padStart(2, '0')}:${`${date.getMinutes()}`.padStart(2, '0')}`
    },
    formatRecordDate(dateString) {
      const date = this.normalizeDate(dateString)
      if (!date) return dateString
      const today = new Date()
      const todayKey = this.getDateKey(today)
      const yesterday = new Date(today.getTime() - 86400000)
      const yesterdayKey = this.getDateKey(yesterday)
      const key = this.getDateKey(date)
      if (key === todayKey) return '今天'
      if (key === yesterdayKey) return '昨天'
      return `${date.getMonth() + 1}月${date.getDate()}日`
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
$primary: #ff6a3d;
$primary-light: #fff0ea;
$green: #52c41a;
$green-light: #f0fff0;
$bg: #f5f5f5;
$card-bg: #ffffff;
$text-primary: #1a1a1a;
$text-secondary: #666666;
$text-light: #999999;
$radius: 24rpx;

.checkin-page { min-height: 100vh; background: $bg; }

.page-header {
  position: fixed; top: 0; left: 0; right: 0; z-index: 30;
  background: linear-gradient(135deg, #ff8a5c 0%, $primary 100%);
}
.status-bar { width: 100%; }
.header-bar {
  height: 92rpx; display: flex; align-items: center;
  justify-content: space-between; padding: 0 28rpx;
}
.header-user { display: flex; align-items: center; }
.header-avatar {
  width: 60rpx; height: 60rpx; border-radius: 50%;
  margin-right: 16rpx; border: 2rpx solid rgba(255,255,255,0.6);
}
.header-name { font-size: 30rpx; font-weight: 600; color: #fff; }
.header-actions { display: flex; align-items: center; gap: 16rpx; }
.header-action-btn {
  width: 56rpx; height: 56rpx; border-radius: 28rpx;
  background: rgba(255,255,255,0.2); display: flex;
  align-items: center; justify-content: center; position: relative;
}
.header-action-icon { font-size: 28rpx; }
.header-badge {
  position: absolute; top: -4rpx; right: -4rpx;
  min-width: 28rpx; height: 28rpx; border-radius: 14rpx;
  background: #ff4d4f; display: flex; align-items: center;
  justify-content: center; padding: 0 6rpx;
}
.header-badge-text { font-size: 18rpx; color: #fff; font-weight: 600; }

.page-scroll { height: 100vh; }
.page-content { padding: 24rpx 24rpx 0; }

.pet-card {
  position: relative; border-radius: $radius;
  overflow: hidden; margin-bottom: 24rpx;
}
.pet-card-bg {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(135deg, #ffe8d6 0%, #ffecd2 100%);
}
.pet-card-content { position: relative; padding: 28rpx; }
.pet-main { display: flex; align-items: center; margin-bottom: 24rpx; }
.pet-avatar {
  width: 100rpx; height: 100rpx; border-radius: 50%;
  border: 4rpx solid #fff; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.1);
  margin-right: 20rpx; flex-shrink: 0;
}
.pet-info { flex: 1; min-width: 0; }
.pet-name {
  display: block; font-size: 34rpx; font-weight: 700;
  color: $text-primary; margin-bottom: 4rpx;
}
.pet-breed { display: block; font-size: 24rpx; color: $text-secondary; }
.pet-picker { flex-shrink: 0; }
.pet-switch-btn {
  padding: 12rpx 24rpx; border-radius: 30rpx;
  background: rgba(255,106,61,0.12);
}
.pet-switch-text { font-size: 24rpx; color: $primary; font-weight: 500; }
.pet-stats {
  display: flex; align-items: center;
  background: rgba(255,255,255,0.7); border-radius: 20rpx; padding: 20rpx 0;
}
.stat-item { flex: 1; display: flex; flex-direction: column; align-items: center; }
.stat-value { font-size: 32rpx; font-weight: 700; color: $primary; margin-bottom: 4rpx; }
.stat-label { font-size: 22rpx; color: $text-secondary; }
.stat-divider { width: 1rpx; height: 40rpx; background: #e0dcd8; }

.empty-pet-card {
  display: flex; align-items: center; justify-content: center;
  padding: 40rpx; background: $card-bg; border-radius: $radius; margin-bottom: 24rpx;
}
.empty-pet-icon { font-size: 36rpx; margin-right: 12rpx; }
.empty-pet-text { font-size: 28rpx; color: $text-secondary; margin-right: 8rpx; }
.empty-pet-arrow { font-size: 28rpx; color: $primary; }

.section { margin-bottom: 24rpx; }
.section-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20rpx; padding: 0 4rpx;
}
.section-title-group { display: flex; align-items: baseline; gap: 12rpx; }
.section-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.section-date { font-size: 24rpx; color: $text-light; }
.section-actions { display: flex; align-items: center; gap: 12rpx; }
.section-add-btn {
  display: flex; align-items: center; gap: 6rpx;
  padding: 10rpx 24rpx; border-radius: 24rpx;
  background: linear-gradient(135deg, #ff6a3d, #ff8a5c);
  box-shadow: 0 4rpx 12rpx rgba(255,106,61,0.3);
}
.section-add-icon { font-size: 28rpx; color: #fff; font-weight: 700; }
.section-add-text { font-size: 24rpx; color: #fff; font-weight: 600; }

.section-toolbar {
  display: flex; align-items: center;
  background: $card-bg; border-radius: 20rpx;
  padding: 16rpx 0; margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.03);
}
.toolbar-item {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; gap: 6rpx;
}
.toolbar-icon-wrap {
  width: 64rpx; height: 64rpx; border-radius: 16rpx;
  background: #f5f5f5; display: flex;
  align-items: center; justify-content: center;
}
.toolbar-icon { font-size: 32rpx; }
.toolbar-label { font-size: 22rpx; color: $text-secondary; font-weight: 500; }
.toolbar-divider { width: 1rpx; height: 48rpx; background: #e5e7eb; }

.checkin-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16rpx; }
.checkin-item {
  background: $card-bg; border-radius: 20rpx; padding: 24rpx 8rpx;
  display: flex; flex-direction: column; align-items: center;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); transition: all 0.25s ease;
  position: relative; overflow: hidden;
}
.checkin-item:active { transform: scale(0.94); }
.checkin-item.checked { background: $green-light; }
.checkin-item.checked::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0;
  height: 4rpx; background: $green;
}
.item-icon-box {
  width: 80rpx; height: 80rpx; display: flex;
  align-items: center; justify-content: center;
  margin-bottom: 12rpx; position: relative;
}
.item-emoji { font-size: 48rpx; }
.item-check-mark {
  position: absolute; top: -4rpx; right: -4rpx;
  width: 32rpx; height: 32rpx; background: $green;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
}
.check-mark-icon { color: #fff; font-size: 20rpx; font-weight: bold; }
.item-name {
  font-size: 24rpx; font-weight: 500; color: $text-primary;
  margin-bottom: 4rpx; text-align: center; max-width: 100%;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.item-time { font-size: 20rpx; color: $green; }
.item-time-hint { font-size: 20rpx; color: $text-light; }

.empty-checkin {
  padding: 48rpx; text-align: center;
  background: $card-bg; border-radius: $radius;
}
.empty-checkin-text { font-size: 28rpx; color: $text-light; }

.records-list { display: flex; flex-direction: column; gap: 16rpx; }
.record-card {
  background: $card-bg; border-radius: $radius; padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.record-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 16rpx;
}
.record-date { font-size: 28rpx; font-weight: 600; color: $text-primary; }
.record-progress { display: flex; align-items: center; gap: 12rpx; }
.progress-bar {
  width: 120rpx; height: 8rpx; background: #f0f0f0;
  border-radius: 4rpx; overflow: hidden;
}
.progress-fill { height: 100%; background: $primary; border-radius: 4rpx; transition: width 0.3s ease; }
.progress-text { font-size: 22rpx; color: $text-light; }
.record-items { display: flex; flex-wrap: wrap; gap: 12rpx; }
.record-item {
  display: flex; align-items: center; padding: 8rpx 16rpx;
  border-radius: 20rpx; background: #f5f5f5; opacity: 0.5;
}
.record-item.done { background: $green-light; opacity: 1; }
.record-item-icon { font-size: 24rpx; margin-right: 6rpx; }
.record-item-name { font-size: 22rpx; color: $text-primary; }

.empty-records {
  display: flex; flex-direction: column; align-items: center;
  padding: 64rpx 0; background: $card-bg; border-radius: $radius;
}
.empty-records-icon { font-size: 64rpx; margin-bottom: 16rpx; }
.empty-records-text { font-size: 28rpx; color: $text-light; }
.page-bottom-safe { height: calc(24rpx + env(safe-area-inset-bottom)); }

.checkin-animation {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  display: flex; align-items: center; justify-content: center;
  pointer-events: none; opacity: 0; z-index: 200; transition: opacity 0.3s ease;
}
.checkin-animation.show { opacity: 1; }
.success-bubble {
  background: $green; padding: 40rpx 64rpx; border-radius: 32rpx;
  display: flex; flex-direction: column; align-items: center;
  box-shadow: 0 16rpx 48rpx rgba(82,196,26,0.4);
  animation: bubblePop 0.6s cubic-bezier(0.68,-0.55,0.265,1.55);
}
.success-emoji { font-size: 64rpx; margin-bottom: 12rpx; animation: emojiCelebrate 0.8s ease infinite; }
.success-text { font-size: 32rpx; font-weight: 700; color: #fff; }

@keyframes bubblePop {
  0% { transform: scale(0); opacity: 0; }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}
@keyframes emojiCelebrate {
  0%, 100% { transform: rotate(0deg) scale(1); }
  25% { transform: rotate(-15deg) scale(1.1); }
  75% { transform: rotate(15deg) scale(1.1); }
}
</style>
