<template>
  <view class="checkin-page">
    <!-- 顶部导航栏 -->
    <user-top-bar
      :status-bar-height="statusBarHeight"
      :avatar="userAvatar"
      :name="userName"
      :unread-count="unreadCount"
      :show-discover="false"
      :show-bell="false"
      @userTap="goProfile"
      @loginTap="goProfile"
      @rightTap="goNotifications"
    />

    <!-- 主内容滚动区 - 玻璃容器 -->
    <scroll-view scroll-y class="page-scroll glass-scroll-container" :style="{ paddingTop: headerHeight + 'px' }">
      <view class="page-content">

        <!-- 宠物信息卡片 - 玻璃卡片 -->
        <view v-if="pet" class="pet-card glass-card">
          <view class="pet-main" @click="showPetSelector = true">
            <image class="pet-avatar glass-avatar" :src="petAvatar" mode="aspectFill" @error="onPetAvatarError" />
            <view class="pet-info">
              <text class="pet-name">{{ pet.name || '我的宠物' }}</text>
              <text class="pet-breed">{{ petDetail }}</text>
            </view>
            <view v-if="pets.length > 1" class="pet-switch-btn glass-switcher">
              <text class="switch-icon">🔄</text>
              <text class="switch-text">切换</text>
              <text class="switch-arrow">›</text>
            </view>
          </view>
          <view class="pet-stats glass-stats-bar">
            <view class="stat-item">
              <text class="stat-value stat-highlight">{{ streakDays }}</text>
              <text class="stat-label">连续天数</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
              <text class="stat-value stat-highlight">{{ todayCompleted }}/{{ visibleItems.length }}</text>
              <text class="stat-label">今日完成</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
              <text class="stat-value stat-highlight">{{ totalCheckins }}</text>
              <text class="stat-label">累计打卡</text>
            </view>
          </view>
        </view>

        <!-- 宠物选择弹窗 - 玻璃拟态 -->
        <view v-if="showPetSelector && pets.length > 1" class="modal-mask glass-modal-mask" @click="showPetSelector = false">
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
                :class="{ active: pet && pet.id === petItem.id }"
                @click="selectPetDirectly(petItem)"
              >
                <image class="option-avatar glass-option-avatar" :src="petItem.avatar || ''" mode="aspectFill" />
                <view class="option-info">
                  <text class="option-name">{{ petItem.name }}</text>
                  <text class="option-breed">{{ petItem.breed || '未设置品种' }}</text>
                </view>
                <view v-if="pet && pet.id === petItem.id" class="option-check">✓</view>
              </view>
            </view>
          </view>
        </view>

        <!-- 空宠物状态 - 玻璃卡片 -->
        <view class="empty-pet-card glass-empty-card" v-if="!pet" @tap="goAddPet">
          <text class="empty-pet-icon">🐾</text>
          <text class="empty-pet-text">添加你的第一只宠物</text>
          <text class="empty-pet-arrow">→</text>
        </view>

        <!-- 今日打卡区域 - 玻璃卡片 -->
        <view class="section glass-section-card">
          <view class="section-header glass-section-header">
            <view class="section-title-group">
              <text class="section-title glass-title-text">📋 今日打卡</text>
              <text class="section-date glass-date-badge">{{ today }}</text>
            </view>
            <view class="section-add-btn glass-add-btn" @tap="goAddItem">
              <text class="section-add-icon">＋</text>
              <text class="section-add-text">添加</text>
            </view>
          </view>

          <view class="section-toolbar glass-toolbar">
            <view class="toolbar-item glass-toolbar-item" @tap="goReport">
              <view class="toolbar-icon-wrap glass-toolbar-icon-wrap">
                <text class="toolbar-icon">📊</text>
              </view>
              <text class="toolbar-label">报表</text>
            </view>
            <view class="toolbar-divider glass-toolbar-divider"></view>
            <view class="toolbar-item glass-toolbar-item" @tap="goReminder">
              <view class="toolbar-icon-wrap glass-toolbar-icon-wrap">
                <text class="toolbar-icon">⏰</text>
              </view>
              <text class="toolbar-label">提醒</text>
            </view>
            <view class="toolbar-divider glass-toolbar-divider"></view>
            <view class="toolbar-item glass-toolbar-item" @tap="goManageItems">
              <view class="toolbar-icon-wrap glass-toolbar-icon-wrap">
                <text class="toolbar-icon">⚙️</text>
              </view>
              <text class="toolbar-label">管理</text>
            </view>
          </view>

          <view class="checkin-grid" v-if="visibleItems.length">
            <view
              v-for="item in visibleItems"
              :key="item.id"
              class="checkin-item glass-checkin-item"
              :class="{ checked: item.checked }"
              @tap="onCheckIn(item)"
            >
              <view class="item-icon-box glass-icon-box">
                <text class="item-emoji">{{ item.emoji || '📋' }}</text>
                <view v-if="item.checked" class="item-check-mark glass-check-mark">
                  <text class="check-mark-icon">✓</text>
                </view>
              </view>
              <text class="item-name glass-item-name">{{ item.label }}</text>
              <text v-if="item.checked" class="item-time glass-time-done">{{ item.checkTime }}</text>
              <text v-else class="item-time-hint glass-time-hint">点击打卡</text>
            </view>
          </view>

          <view class="empty-checkin glass-empty-state" v-else>
            <text class="empty-checkin-text">暂无打卡项，点击右上角添加</text>
          </view>
        </view>

        <!-- 打卡记录区域 - 玻璃卡片 -->
        <view class="section glass-section-card">
          <view class="section-header glass-section-header">
            <view class="section-title-group">
              <text class="section-title glass-title-text">📝 打卡记录</text>
            </view>
          </view>

          <view v-if="recentRecords.length" class="records-list glass-records-list">
            <view v-for="group in recordGroups" :key="group.label" class="record-group glass-record-group">
              <view class="record-group-header glass-group-header">
                <text class="record-group-label glass-group-label">{{ group.label }}</text>
                <text class="record-group-range glass-group-range">{{ group.dateRange }}</text>
              </view>
              <view v-for="record in group.records" :key="record.date" class="record-card glass-record-card">
                <view class="record-header glass-record-header">
                  <text class="record-date glass-record-date">{{ record.displayDate }}</text>
                  <view class="record-progress glass-progress-wrapper">
                    <view class="progress-bar glass-progress-track">
                      <view class="progress-fill glass-progress-fill" :style="{ width: record.progress + '%' }"></view>
                    </view>
                    <text class="progress-text glass-progress-text">{{ record.completedCount }}/{{ record.totalCount }}</text>
                  </view>
                </view>
                <view class="record-items glass-items-row">
                  <view
                    v-for="recordItem in record.items"
                    :key="record.date + '-' + recordItem.id"
                    class="record-item glass-record-tag"
                    :class="{ done: recordItem.checked }"
                  >
                    <text class="record-item-icon">{{ recordItem.emoji || '📋' }}</text>
                    <text class="record-item-name">{{ recordItem.label }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <view v-else class="empty-records glass-empty-state-large">
            <text class="empty-records-icon">📝</text>
            <text class="empty-records-text">暂无打卡记录</text>
          </view>

          <view v-if="recentRecords.length && !showAllRecords" class="view-all-btn glass-view-all-btn" @tap="onViewAll">
            <text class="view-all-text">查看全部</text>
            <text class="view-all-arrow">›</text>
          </view>

          <view v-if="showAllRecords && hasMoreMonths" class="load-more-btn glass-load-more-btn" @tap="onLoadMore">
            <text class="load-more-text">{{ loadingMore ? '加载中...' : '加载更多' }}</text>
          </view>
        </view>

        <view class="page-bottom-safe"></view>
      </view>
    </scroll-view>

    <!-- 打卡成功动画 - 玻璃效果 -->
    <view class="checkin-animation glass-animation-overlay" :class="{ show: showSuccessAnimation }">
      <view class="success-bubble glass-success-bubble">
        <text class="success-emoji">🎉</text>
        <text class="success-text">打卡成功！</text>
      </view>
    </view>
  </view>
</template>

<script>
import { checkLogin, getUserAvatar, DEFAULT_USER_AVATAR } from '@/utils/index'
import * as authApi from '@/api/auth'
import * as petApi from '@/api/pet'
import * as checkinApi from '@/api/checkin'
import UserTopBar from '@/components/UserTopBar.vue'

const DEFAULT_PET_AVATAR = '/static/images/default-pet-avatar.png'

export default {
  components: { UserTopBar },
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 70,
      userName: '',
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
      recordGroups: [],
      showAllRecords: false,
      loadedMonths: 3,
      loadingMore: false,
      showSuccessAnimation: false,
      showPetSelector: false,
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
    },
    hasMoreMonths() {
      const now = new Date()
      const earliest = new Date(now.getFullYear(), now.getMonth() - this.loadedMonths + 1, 1)
      const limit = new Date(now.getFullYear() - 2, 0, 1)
      return earliest > limit
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
    this.loadUserProfile()
    await this.loadPets()
    await this.loadCheckinItems()
    await this.refreshPageData()

    uni.$on('loginSuccess', () => {
      this.loadUserProfile()
      this.loadPets()
      this.loadCheckinItems()
      this.refreshPageData()
    })
  },
  onUnload() {
    if (this.animationTimer) {
      clearTimeout(this.animationTimer)
      this.animationTimer = null
    }
    uni.$off('loginSuccess')
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
        const res = await petApi.getPetList()
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
        const res = await petApi.getPetDetail(this.petId)
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
        const res = await checkinApi.getCheckinItems()
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
    async loadCalendarRecords(forceRefresh = false) {
      const requests = this.getMonthRequests(3)
      try {
        const responses = await Promise.all(
          requests.map(item => checkinApi.getCalendar(item.year, item.month, forceRefresh).catch(() => null))
        )
        const result = []
        for (const res of responses) {
          if (res && res.success && Array.isArray(res.data)) {
            result.push(...res.data)
          }
        }
        this.allRecords = this.filterPetRecords(result)
      } catch (error) {
        console.error('加载打卡记录失败:', error)
      }
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
          .slice(0, this.showAllRecords ? 9999 : 7)
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
        this.buildRecordGroups()
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
        const res = await checkinApi.checkin({ petId: this.petId, itemIds: [item.id], date: this.getISODate() })
        if (!res || res.success !== true) {
          uni.showToast({ title: (res && res.message) || '打卡失败', icon: 'none' })
          return
        }
        try { uni.vibrateShort({ type: 'medium' }) } catch (e) { /* ignore */ }
        this.showSuccessFeedback()

        if (res.data && Array.isArray(res.data)) {
          for (const record of res.data) {
            if (record && record.itemId) {
              this.allRecords.push(record)
            }
          }
        }
        this.syncPageState()

        await this.loadCalendarRecords(true)
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
    onViewAll() {
      this.showAllRecords = true
      this.buildRecordGroups()
    },
    async onLoadMore() {
      if (this.loadingMore) return
      this.loadingMore = true
      this.loadedMonths += 1
      try {
        const date = new Date()
        const target = new Date(date.getFullYear(), date.getMonth() - this.loadedMonths + 1, 1)
        const year = target.getFullYear()
        const month = target.getMonth() + 1
        const res = await checkinApi.getCalendar(year, month)
        if (res && res.success && Array.isArray(res.data)) {
          const newRecords = this.filterPetRecords(res.data)
          this.allRecords = [...this.allRecords, ...newRecords]
          this.syncPageState()
        }
      } catch (error) {
        console.error('加载更多记录失败:', error)
        this.loadedMonths -= 1
      } finally {
        this.loadingMore = false
      }
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
    selectPetDirectly(petItem) {
      if (!petItem || !petItem.id) return
      this.petId = petItem.id
      this.showPetSelector = false
      this.syncPetFromList()
      this.refreshPageData()
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
    onPetAvatarError() {
      this.petAvatar = DEFAULT_PET_AVATAR
    },
    initNavMetrics() {
      try {
        const sys = uni.getSystemInfoSync()
        this.statusBarHeight = (sys && sys.statusBarHeight) || 20
      } catch (e) {
        this.statusBarHeight = 20
      }
      this.headerHeight = this.statusBarHeight + 54
    },
    async loadUserProfile() {
      try {
        const userInfo = uni.getStorageSync('userInfo')
        const token = uni.getStorageSync('token')
        if (!token) {
          this.userName = ''
          this.userAvatar = DEFAULT_USER_AVATAR
          return
        }
        if (userInfo) {
          if (userInfo.nickname) this.userName = userInfo.nickname
          if (userInfo.avatar) this.userAvatar = getUserAvatar(userInfo.id, userInfo.avatar)
        }
        const res = await authApi.getProfile()
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
    getISODate() {
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
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
    formatDateRange(from, to) {
      if (!from || !to) return ''
      const fromStr = `${from.getMonth() + 1}月${from.getDate()}日`
      const toStr = `${to.getMonth() + 1}月${to.getDate()}日`
      return fromStr === toStr ? fromStr : `${fromStr} - ${toStr}`
    },
    buildRecordGroups() {
      if (!this.recentRecords.length) { this.recordGroups = []; return }

      if (this.showAllRecords) {
        const monthMap = new Map()
        for (const record of this.recentRecords) {
          const d = this.normalizeDate(record.date)
          if (!d) continue
          const monthKey = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
          if (!monthMap.has(monthKey)) monthMap.set(monthKey, [])
          monthMap.get(monthKey).push(record)
        }
        const groups = []
        const monthNames = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
        for (const [monthKey, records] of monthMap) {
          const [yearStr, monthStr] = monthKey.split('-')
          const year = parseInt(yearStr, 10)
          const month = parseInt(monthStr, 10)
          const now = new Date()
          let label = `${year}年${monthNames[month - 1]}`
          if (year === now.getFullYear() && month === now.getMonth() + 1) {
            label = '本月'
          }
          const sorted = records.slice().sort((a, b) => (a.date > b.date ? -1 : 1))
          const range = this.formatDateRange(this.normalizeDate(sorted[0].date), this.normalizeDate(sorted[sorted.length - 1].date))
          groups.push({ label, dateRange: range, records })
        }
        this.recordGroups = groups
        return
      }

      const now = new Date()
      const dayOfWeek = now.getDay() || 7
      const weekStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - dayOfWeek + 1)
      const weekEnd = new Date(weekStart.getTime() + 6 * 86400000)
      const weekStartKey = this.getDateKey(weekStart)
      const weekEndKey = this.getDateKey(weekEnd)

      const thisWeek = []
      const earlier = []
      for (const record of this.recentRecords) {
        const key = record.date
        if (key >= weekStartKey && key <= weekEndKey) {
          thisWeek.push(record)
        } else {
          earlier.push(record)
        }
      }

      const groups = []
      if (thisWeek.length) {
        const sorted = thisWeek.slice().sort((a, b) => (a.date > b.date ? -1 : 1))
        const range = this.formatDateRange(this.normalizeDate(sorted[0].date), this.normalizeDate(sorted[sorted.length - 1].date))
        groups.push({ label: '本周', dateRange: range, records: thisWeek })
      }
      if (earlier.length) {
        const sorted = earlier.slice().sort((a, b) => (a.date > b.date ? -1 : 1))
        const range = this.formatDateRange(this.normalizeDate(sorted[0].date), this.normalizeDate(sorted[sorted.length - 1].date))
        groups.push({ label: '更早', dateRange: range, records: earlier })
      }
      this.recordGroups = groups
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
/* ============================================
   打卡页面 - 玻璃拟态风格 (Glassmorphism)
   保持原逻辑不变，全面升级视觉体验
   ============================================ */

/* ====== 全局容器 ====== */
.checkin-page {
  min-height: 100vh;
  background: linear-gradient(180deg, rgba(255, 248, 245, 0.98) 0%, rgba(250, 250, 252, 0.99) 100%);
}

.glass-scroll-container {
  height: 100vh;
}

.page-content {
  padding: 20rpx 24rpx 40rpx;
}

/* ====== 统一卡片样式 ====== */
.glass-section-card {
  position: relative;
  background: rgba(255, 255, 255, 0.86);
  border-radius: 26rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow:
    0 8rpx 28rpx rgba(31, 38, 135, 0.08),
    0 3rpx 10rpx rgba(0, 0, 0, 0.03),
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

/* ====== 宠物信息卡片 - 基础版 ====== */
.pet-card {
  margin-bottom: 24rpx;
}

.pet-main {
  display: flex;
  align-items: center;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid rgba(200, 210, 220, 0.2);
  margin-bottom: 18rpx;
}

.glass-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(255, 255, 255, 0.8);
  box-shadow:
    0 6rpx 20rpx rgba(255, 122, 61, 0.15),
    0 3rpx 10rpx rgba(0, 0, 0, 0.06),
    inset 0 2rpx 4rpx rgba(255, 255, 255, 0.5);
  margin-right: 18rpx;
  flex-shrink: 0;
}

.pet-info {
  flex: 1;
  min-width: 0;
}

.pet-name {
  font-size: 32rpx;
  font-weight: 800;
  color: var(--pt-text, #111827);
  display: block;
  margin-bottom: 6rpx;
  letter-spacing: 0.5rpx;
}

.pet-breed {
  font-size: 25rpx;
  color: var(--pt-secondary, #6b7280);
  display: block;
  font-weight: 500;
}

.pet-picker {
  flex-shrink: 0;
}

.glass-switcher {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 14rpx 24rpx;
  background: linear-gradient(135deg, rgba(255, 245, 240, 0.85) 0%, rgba(255, 250, 247, 0.78) 100%);
  border-radius: 999rpx;
  backdrop-filter: blur(10px);
  border: 1.5rpx solid rgba(255, 122, 61, 0.2);
  box-shadow:
    0 4rpx 14rpx rgba(255, 106, 61, 0.1),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.6);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.94);
    background: linear-gradient(135deg, rgba(255, 235, 230, 0.92) 0%, rgba(255, 245, 240, 0.88) 100%);
    border-color: rgba(255, 122, 61, 0.35);
  }
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

/* ====== 统计面板 - 基础版 ====== */
.glass-stats-bar {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.75);
  border-radius: 22rpx;
  padding: 20rpx 0;
  backdrop-filter: blur(12px);
  border: 1rpx solid rgba(255, 255, 255, 0.6);
  box-shadow:
    0 4rpx 16rpx rgba(31, 38, 135, 0.04),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.85);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 34rpx;
  font-weight: 900;
  color: var(--pt-primary, #ff7a3d);
  margin-bottom: 6rpx;
  text-shadow: 0 2rpx 8rpx rgba(255, 122, 61, 0.15);
}

.stat-label {
  font-size: 23rpx;
  color: var(--pt-muted, #9ca3af);
  font-weight: 600;
}

.stat-divider {
  width: 1.5rpx;
  height: 44rpx;
  background: linear-gradient(180deg, transparent, rgba(200, 210, 220, 0.5), transparent);
}

/* ====== 空宠物卡片 ====== */
.glass-empty-card {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48rpx;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.88) 0%, rgba(250, 250, 252, 0.85) 100%);
  backdrop-filter: blur(20px);
  border: 1rpx solid rgba(200, 210, 220, 0.35);
  box-shadow:
    0 6rpx 24rpx rgba(31, 38, 135, 0.06),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.92);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.97);
    background: linear-gradient(145deg, rgba(255, 250, 245, 0.95) 0%, rgba(255, 248, 245, 0.92) 100%);
  }
}

.empty-pet-icon {
  font-size: 40rpx;
  margin-right: 14rpx;
}

.empty-pet-text {
  font-size: 29rpx;
  color: var(--pt-secondary, #6b7280);
  margin-right: 10rpx;
  font-weight: 600;
}

.empty-pet-arrow {
  font-size: 30rpx;
  color: var(--pt-primary, #ff7a3d);
  font-weight: 700;
}

/* ====== 区域头部 ====== */
.glass-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22rpx;
  padding: 0 4rpx;
}

.glass-title-text {
  font-size: 33rpx;
  font-weight: 800;
  color: var(--pt-text, #111827);
  letter-spacing: 0.5rpx;
}

.glass-date-badge {
  font-size: 24rpx;
  color: var(--pt-muted, #9ca3af);
  font-weight: 600;
  padding: 6rpx 16rpx;
  background: rgba(249, 250, 251, 0.85);
  border-radius: 999rpx;
  backdrop-filter: blur(6px);
}

/* ====== 添加按钮 ====== */
.glass-add-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 26rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  box-shadow:
    0 8rpx 24rpx rgba(255, 90, 61, 0.35),
    0 4rpx 12rpx rgba(255, 90, 61, 0.2),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
  transition: all 0.32s cubic-bezier(0.34, 1.56, 0.64, 1);

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
    transform: scale(0.95);
    opacity: 0.95;
  }

  &:active::after {
    left: 100%;
  }
}

.section-add-icon {
  font-size: 28rpx;
  color: #fff;
  font-weight: 800;
}

.section-add-text {
  font-size: 25rpx;
  color: #fff;
  font-weight: 700;
  letter-spacing: 1rpx;
}

/* ====== 工具栏 ====== */
.glass-toolbar {
  display: flex;
  align-items: center;
  background: rgba(249, 250, 251, 0.75);
  border-radius: 22rpx;
  padding: 18rpx 0;
  margin-bottom: 22rpx;
  backdrop-filter: blur(10px);
  border: 1rpx solid rgba(209, 213, 219, 0.25);
  box-shadow:
    0 3rpx 12rpx rgba(31, 38, 135, 0.04),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.85);
}

.glass-toolbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  transition: all 0.28s ease;

  &:active {
    transform: scale(0.93);
  }
}

.glass-toolbar-icon-wrap {
  width: 68rpx;
  height: 68rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, rgba(255, 245, 240, 0.9) 0%, rgba(255, 250, 247, 0.85) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1rpx solid rgba(255, 122, 61, 0.15);
  box-shadow: 0 3rpx 10rpx rgba(255, 106, 61, 0.08);
  transition: all 0.3s ease;

  .glass-toolbar-item:active & {
    transform: scale(0.92);
    background: linear-gradient(135deg, rgba(255, 235, 230, 0.98) 0%, rgba(255, 245, 240, 0.95) 100%);
    border-color: rgba(255, 122, 61, 0.3);
  }
}

.toolbar-icon {
  font-size: 33rpx;
}

.toolbar-label {
  font-size: 23rpx;
  color: var(--pt-secondary, #6b7280);
  font-weight: 600;
}

.glass-toolbar-divider {
  width: 1.5rpx;
  height: 52rpx;
  background: linear-gradient(180deg, transparent, rgba(200, 210, 220, 0.45), transparent);
}

/* ====== 打卡网格 ====== */
.checkin-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18rpx;
}

.glass-checkin-item {
  background: rgba(255, 255, 255, 0.75);
  border-radius: 22rpx;
  padding: 24rpx 10rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  backdrop-filter: blur(8px);
  border: 1.5rpx solid rgba(209, 213, 219, 0.25);
  box-shadow:
    0 3rpx 14rpx rgba(31, 38, 135, 0.05),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.88);
  transition: all 0.32s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  &:active {
    transform: scale(0.94);
    background: rgba(255, 245, 240, 0.88);
    border-color: rgba(255, 122, 61, 0.3);
  }

  &.checked {
    background: linear-gradient(145deg, rgba(232, 255, 236, 0.95) 0%, rgba(245, 255, 248, 0.92) 100%);
    border-color: rgba(16, 185, 129, 0.35);
    box-shadow:
      0 4rpx 16rpx rgba(16, 185, 129, 0.15),
      inset 0 1rpx 0 rgba(255, 255, 255, 0.95);

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 5rpx;
      background: linear-gradient(90deg, #52c41a, #73d13d);
      border-radius: 22rpx 22rpx 0 0;
    }
  }
}

.glass-icon-box {
  width: 84rpx;
  height: 84rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14rpx;
  position: relative;
}

.item-emoji {
  font-size: 50rpx;
}

.glass-check-mark {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  width: 36rpx;
  height: 36rpx;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 14rpx rgba(82, 196, 26, 0.35);
  border: 2rpx solid rgba(255, 255, 255, 0.9);
  animation: checkMarkPop 0.35s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

@keyframes checkMarkPop {
  from {
    opacity: 0;
    transform: scale(0.3);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.check-mark-icon {
  color: #fff;
  font-size: 22rpx;
  font-weight: 900;
}

.glass-item-name {
  font-size: 25rpx;
  font-weight: 600;
  color: var(--pt-text, #374151);
  margin-bottom: 6rpx;
  text-align: center;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.glass-time-done {
  font-size: 21rpx;
  color: #52c41a;
  font-weight: 600;
}

.glass-time-hint {
  font-size: 21rpx;
  color: var(--pt-muted, #9ca3af);
  font-weight: 500;
}

/* ====== 空状态 ====== */
.glass-empty-state {
  padding: 56rpx;
  text-align: center;
  background: linear-gradient(145deg, rgba(249, 250, 251, 0.7) 0%, rgba(255, 255, 255, 0.65) 100%);
  backdrop-filter: blur(8px);
  border: 1.5rpx dashed rgba(209, 213, 219, 0.35);
  border-radius: 22rpx;
}

.empty-checkin-text {
  font-size: 28rpx;
  color: var(--pt-muted, #9ca3af);
  font-weight: 500;
}

/* ====== 记录列表 ====== */
.glass-records-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.glass-record-group {
  margin-bottom: 10rpx;
}

.glass-group-header {
  display: flex;
  align-items: baseline;
  gap: 14rpx;
  margin-bottom: 14rpx;
  padding-left: 4rpx;
}

.glass-group-label {
  font-size: 29rpx;
  font-weight: 800;
  color: var(--pt-text, #111827);
  letter-spacing: 0.5rpx;
}

.glass-group-range {
  font-size: 23rpx;
  color: var(--pt-muted, #9ca3af);
  font-weight: 600;
  padding: 5rpx 14rpx;
  background: rgba(249, 250, 251, 0.8);
  border-radius: 999rpx;
}

.glass-record-card {
  background: rgba(255, 255, 255, 0.72);
  border-radius: 24rpx;
  padding: 26rpx;
  backdrop-filter: blur(10px);
  border: 1rpx solid rgba(209, 213, 219, 0.2);
  box-shadow:
    0 3rpx 14rpx rgba(31, 38, 135, 0.05),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.85);
  transition: all 0.3s ease;

  &:active {
    transform: translateY(-2rpx);
    box-shadow:
      0 6rpx 20rpx rgba(31, 38, 135, 0.08),
      inset 0 1rpx 0 rgba(255, 255, 255, 0.9);
  }
}

.glass-record-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;
}

.glass-record-date {
  font-size: 29rpx;
  font-weight: 700;
  color: var(--pt-text, #111827);
}

.glass-progress-wrapper {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.glass-progress-track {
  width: 130rpx;
  height: 10rpx;
  background: rgba(243, 244, 246, 0.8);
  border-radius: 5rpx;
  overflow: hidden;
  backdrop-filter: blur(4px);
  border: 1rpx solid rgba(209, 213, 219, 0.2);
}

.glass-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff7a3d 0%, #ff4d4f 100%);
  border-radius: 5rpx;
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2rpx 8rpx rgba(255, 90, 61, 0.25);
}

.glass-progress-text {
  font-size: 23rpx;
  color: var(--pt-muted, #9ca3af);
  font-weight: 600;
}

.glass-items-row {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.glass-record-tag {
  display: flex;
  align-items: center;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(249, 250, 251, 0.8);
  backdrop-filter: blur(6px);
  border: 1rpx solid rgba(209, 213, 219, 0.25);
  opacity: 0.55;
  transition: all 0.28s ease;

  &.done {
    background: linear-gradient(135deg, rgba(232, 255, 236, 0.9) 0%, rgba(245, 255, 248, 0.85) 100%);
    border-color: rgba(16, 185, 129, 0.3);
    opacity: 1;
    box-shadow: 0 2rpx 8rpx rgba(16, 185, 129, 0.1);
  }
}

.record-item-icon {
  font-size: 25rpx;
  margin-right: 7rpx;
}

.record-item-name {
  font-size: 23rpx;
  color: var(--pt-text, #374151);
  font-weight: 600;
}

/* ====== 大型空状态 ====== */
.glass-empty-state-large {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;
  background: linear-gradient(145deg, rgba(249, 250, 251, 0.65) 0%, rgba(255, 255, 255, 0.58) 100%);
  backdrop-filter: blur(8px);
  border: 1.5rpx dashed rgba(209, 213, 219, 0.3);
  border-radius: 24rpx;
}

.empty-records-icon {
  font-size: 72rpx;
  margin-bottom: 18rpx;
  filter: grayscale(15%) brightness(1.05);
}

.empty-records-text {
  font-size: 29rpx;
  color: var(--pt-muted, #9ca3af);
  font-weight: 600;
}

/* ====== 查看全部按钮 ====== */
.glass-view-all-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 92rpx;
  border-radius: 46rpx;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.9) 0%, rgba(250, 250, 252, 0.86) 100%);
  margin-top: 18rpx;
  backdrop-filter: blur(12px);
  border: 1rpx solid rgba(200, 210, 220, 0.35);
  box-shadow:
    0 3rpx 14rpx rgba(31, 38, 135, 0.05),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.97);
    background: linear-gradient(145deg, rgba(255, 250, 245, 0.95) 0%, rgba(255, 248, 245, 0.92) 100%);
  }
}

.view-all-text {
  font-size: 29rpx;
  font-weight: 600;
  color: var(--pt-primary, #ff7a3d);
  letter-spacing: 1rpx;
}

.view-all-arrow {
  font-size: 34rpx;
  color: var(--pt-primary, #ff7a3d);
  font-weight: 700;
  margin-left: 8rpx;
}

/* ====== 加载更多按钮 ====== */
.glass-load-more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 92rpx;
  border-radius: 46rpx;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.88) 0%, rgba(250, 250, 252, 0.84) 100%);
  margin-top: 18rpx;
  backdrop-filter: blur(12px);
  border: 1rpx solid rgba(200, 210, 220, 0.3);
  box-shadow:
    0 3rpx 14rpx rgba(31, 38, 135, 0.04),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.88);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.97);
    background: rgba(243, 244, 246, 0.95);
  }
}

.load-more-text {
  font-size: 28rpx;
  font-weight: 500;
  color: var(--pt-secondary, #6b7280);
}

.page-bottom-safe {
  height: calc(120rpx + env(safe-area-inset-bottom));
}

/* ====== 宠物选择弹窗 - 玻璃拟态（与体重记录页一致） ====== */
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
  background: rgba(255, 255, 255, 0.94);
  border-radius: 28rpx;
  overflow: hidden;
  backdrop-filter: blur(24px);
  border: 1rpx solid rgba(255, 255, 255, 0.72);
  box-shadow:
    0 24rpx 60rpx rgba(0, 0, 0, 0.2),
    0 8rpx 24rpx rgba(0, 0, 0, 0.1),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.95);
  animation: modalSlideUp 0.32s cubic-bezier(0.34, 1.56, 0.64, 1) both;
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

.glass-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 28rpx;
  border-bottom: 1rpx solid rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(8px);
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

.glass-list-container {
  padding: 18rpx;
  max-height: 55vh;
  overflow-y: auto;
}

.glass-pet-option {
  display: flex;
  align-items: center;
  padding: 22rpx 18rpx;
  border-radius: 20rpx;
  margin-bottom: 12rpx;
  background: rgba(249, 250, 251, 0.6);
  backdrop-filter: blur(8px);
  border: 2rpx solid transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.97);
  }

  &.active {
    background: linear-gradient(135deg, rgba(255, 245, 240, 0.92) 0%, rgba(255, 250, 247, 0.88) 100%);
    border-color: var(--pt-primary, #ff7a3d);
    box-shadow:
      0 6rpx 20rpx rgba(255, 106, 61, 0.15),
      inset 0 1rpx 0 rgba(255, 255, 255, 0.7);

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

.switch-arrow {
  font-size: 32rpx;
  color: var(--pt-primary, #ff7a3d);
  font-weight: 600;
  margin-left: 6rpx;
  transition: transform 0.28s ease;

  .glass-switcher:active & {
    transform: translateX(4rpx);
  }
}

/* ====== 成功动画 - 玻璃效果 ====== */
.glass-animation-overlay {
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
  z-index: 200;
  transition: opacity 0.35s ease;
  background: rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(6px);

  &.show {
    opacity: 1;
  }
}

.glass-success-bubble {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  padding: 48rpx 72rpx;
  border-radius: 36rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow:
    0 20rpx 56rpx rgba(82, 196, 26, 0.45),
    0 8rpx 24rpx rgba(82, 196, 26, 0.25),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.3),
    inset 0 -2rpx 0 rgba(0, 0, 0, 0.1);
  border: 2rpx solid rgba(255, 255, 255, 0.4);
  animation: bubblePopEnhanced 0.65s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  backdrop-filter: blur(16px);
}

@keyframes bubblePopEnhanced {
  0% {
    transform: scale(0) rotate(-10deg);
    opacity: 0;
  }
  50% {
    transform: scale(1.12) rotate(3deg);
    opacity: 1;
  }
  100% {
    transform: scale(1) rotate(0deg);
    opacity: 1;
  }
}

.success-emoji {
  font-size: 68rpx;
  margin-bottom: 14rpx;
  animation: emojiCelebrateEnhanced 0.9s ease infinite;
}

@keyframes emojiCelebrateEnhanced {
  0%, 100% {
    transform: rotate(0deg) scale(1) translateY(0);
  }
  25% {
    transform: rotate(-18deg) scale(1.15) translateY(-6rpx);
  }
  75% {
    transform: rotate(18deg) scale(1.15) translateY(-6rpx);
  }
}

.success-text {
  font-size: 34rpx;
  font-weight: 800;
  color: #fff;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
  letter-spacing: 2rpx;
}

/* ====== 暗色模式适配 ====== */
page.dark .checkin-page {
  background: linear-gradient(180deg, rgba(40, 40, 55, 0.98) 0%, rgba(30, 30, 42, 0.99) 100%);
}

page.dark .glass-section-card {
  background: rgba(45, 45, 60, 0.84);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow:
    0 8rpx 28rpx rgba(0, 0, 0, 0.3),
    0 3rpx 10rpx rgba(0, 0, 0, 0.12),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.08);

  &::before {
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.08), transparent);
  }

  &::after {
    background: linear-gradient(90deg,
      transparent,
      rgba(255, 122, 61, 0.2) 30%,
      rgba(255, 122, 61, 0.3) 50%,
      rgba(255, 122, 61, 0.2) 70%,
      transparent
    );
  }
}

/* ====== 暗色模式 - 宠物信息卡片基础版 ====== */
page.dark .glass-avatar {
  border-color: rgba(255, 255, 255, 0.15);
  box-shadow:
    0 6rpx 20rpx rgba(255, 106, 61, 0.1),
    0 3rpx 10rpx rgba(0, 0, 0, 0.25),
    inset 0 2rpx 4rpx rgba(255, 255, 255, 0.08);
}

page.dark .pet-name {
  color: #e5e5e5;
}

page.dark .pet-breed {
  color: #888888;
}

page.dark .glass-switcher {
  background: linear-gradient(135deg, rgba(255, 120, 80, 0.12) 0%, rgba(255, 100, 80, 0.08) 100%);
  border-color: rgba(255, 122, 61, 0.25);
  box-shadow:
    0 4rpx 14rpx rgba(255, 106, 61, 0.08),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.05);

  &:active {
    background: linear-gradient(135deg, rgba(255, 120, 80, 0.18) 0%, rgba(255, 100, 80, 0.14) 100%);
    border-color: rgba(255, 122, 61, 0.4);
  }
}

page.dark .switch-text {
  color: #ff9966;
}

page.dark .glass-stats-bar {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 4rpx 16rpx rgba(0, 0, 0, 0.15),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.05);
}

page.dark .stat-value {
  color: #ff9966;
  text-shadow: 0 2rpx 8rpx rgba(255, 150, 100, 0.15);
}

page.dark .stat-label {
  color: #aaaaaa;
}

page.dark .stat-divider {
  background: linear-gradient(180deg, transparent, rgba(255, 255, 255, 0.1), transparent);
}

page.dark .glass-empty-card {
  background: linear-gradient(145deg, rgba(50, 50, 65, 0.88) 0%, rgba(45, 45, 58, 0.85) 100%);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow:
    0 6rpx 24rpx rgba(0, 0, 0, 0.25),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.06);
}

page.dark .empty-pet-text {
  color: #aaaaaa;
}

page.dark .glass-title-text {
  color: #e5e5e5;
}

page.dark .glass-date-badge {
  background: rgba(255, 255, 255, 0.06);
  color: #aaaaaa;
}

page.dark .glass-toolbar {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 3rpx 12rpx rgba(0, 0, 0, 0.15),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.03);
}

page.dark .glass-toolbar-icon-wrap {
  background: linear-gradient(135deg, rgba(255, 120, 80, 0.15) 0%, rgba(255, 100, 80, 0.1) 100%);
  border-color: rgba(255, 122, 61, 0.2);
  box-shadow: 0 3rpx 10rpx rgba(255, 106, 61, 0.08);
}

page.dark .toolbar-label {
  color: #aaaaaa;
}

page.dark .glass-toolbar-divider {
  background: linear-gradient(180deg, transparent, rgba(255, 255, 255, 0.1), transparent);
}

page.dark .glass-checkin-item {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 3rpx 14rpx rgba(0, 0, 0, 0.15),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.03);

  &.checked {
    background: linear-gradient(145deg, rgba(40, 80, 50, 0.3) 0%, rgba(45, 90, 55, 0.25) 100%);
    border-color: rgba(82, 196, 26, 0.3);
    box-shadow:
      0 4rpx 16rpx rgba(82, 196, 26, 0.12),
      inset 0 1rpx 0 rgba(255, 255, 255, 0.05);

    &::before {
      background: linear-gradient(90deg, #52c41a, #73d13d);
    }
  }
}

page.dark .glass-item-name {
  color: #e5e5e5;
}

page.dark .glass-time-hint {
  color: #888888;
}

page.dark .glass-empty-state {
  background: linear-gradient(145deg, rgba(50, 50, 62, 0.4) 0%, rgba(45, 45, 55, 0.35) 100%);
  border-color: rgba(255, 255, 255, 0.06);
}

page.dark .empty-checkin-text {
  color: #888888;
}

page.dark .glass-group-label {
  color: #e5e5e5;
}

page.dark .glass-group-range {
  background: rgba(255, 255, 255, 0.06);
  color: #aaaaaa;
}

page.dark .glass-record-card {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 3rpx 14rpx rgba(0, 0, 0, 0.15),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.03);
}

page.dark .glass-record-date {
  color: #e5e5e5;
}

page.dark .glass-progress-track {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.08);
}

page.dark .glass-progress-text {
  color: #888888;
}

page.dark .glass-record-tag {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.08);

  &.done {
    background: linear-gradient(135deg, rgba(40, 80, 50, 0.25) 0%, rgba(45, 90, 55, 0.2) 100%);
    border-color: rgba(82, 196, 26, 0.25);
  }
}

page.dark .record-item-name {
  color: #e5e5e5;
}

page.dark .glass-empty-state-large {
  background: linear-gradient(145deg, rgba(50, 50, 62, 0.4) 0%, rgba(45, 45, 55, 0.35) 100%);
  border-color: rgba(255, 255, 255, 0.06);
}

page.dark .empty-records-text {
  color: #888888;
}

page.dark .glass-view-all-btn {
  background: linear-gradient(145deg, rgba(50, 50, 62, 0.88) 0%, rgba(45, 45, 58, 0.84) 100%);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow:
    0 3rpx 14rpx rgba(0, 0, 0, 0.2),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.04);
}

page.dark .view-all-text {
  color: #ff9966;
}

page.dark .view-all-arrow {
  color: #ff9966;
}

page.dark .glass-load-more-btn {
  background: linear-gradient(145deg, rgba(50, 50, 62, 0.86) 0%, rgba(45, 45, 58, 0.82) 100%);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 3rpx 14rpx rgba(0, 0, 0, 0.18),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.03);
}

page.dark .load-more-text {
  color: #aaaaaa;
}

page.dark .glass-success-bubble {
  background: linear-gradient(135deg, rgba(82, 196, 26, 0.9) 0%, rgba(115, 209, 61, 0.85) 100%);
  box-shadow:
    0 20rpx 56rpx rgba(82, 196, 26, 0.3),
    0 8rpx 24rpx rgba(82, 196, 26, 0.15);
  border-color: rgba(255, 255, 255, 0.15);
}

/* ====== 暗色模式 - 宠物选择弹窗 ====== */
page.dark .glass-modal-mask {
  background: rgba(0, 0, 0, 0.65);
}

page.dark .glass-modal-card {
  background: rgba(40, 40, 55, 0.95);
  border-color: rgba(255, 255, 255, 0.1);
}

page.dark .glass-modal-header {
  border-bottom-color: rgba(255, 255, 255, 0.08);
}

page.dark .modal-title {
  color: #e5e5e5;
}

page.dark .glass-pet-option {
  background: rgba(255, 255, 255, 0.04);

  &.active {
    background: linear-gradient(135deg, rgba(255, 120, 80, 0.2) 0%, rgba(255, 100, 80, 0.15) 100%);
    border-color: rgba(255, 122, 61, 0.4);
    box-shadow:
      0 6rpx 20rpx rgba(255, 106, 61, 0.12),
      inset 0 1rpx 0 rgba(255, 255, 255, 0.08);

    .option-name {
      color: #ff9966;
    }
  }
}

page.dark .glass-option-avatar {
  background: linear-gradient(135deg, rgba(255, 150, 100, 0.25) 0%, rgba(255, 120, 80, 0.15) 100%);
  border-color: rgba(255, 122, 61, 0.3);
}

page.dark .option-name {
  color: #e5e5e5;

  .glass-pet-option.active & {
    color: #ff9966;
  }
}

page.dark .option-breed {
  color: #888888;
}

page.dark .option-check {
  background: linear-gradient(135deg, #ff9966 0%, #ff7766 100%);
  box-shadow:
    0 4rpx 14rpx rgba(255, 120, 80, 0.35),
    inset 0 1.5rpx 0 rgba(255, 255, 255, 0.2);
}

page.dark .success-text {
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.25);
}
</style>
