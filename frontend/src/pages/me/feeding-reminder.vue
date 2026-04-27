<template>
  <view class="reminder-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack"><view class="nav-back-arrow"></view></view>
        <text class="nav-title">喂食提醒</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px', paddingBottom: '160rpx' }">
      <view class="page-content">

        <view v-if="pets.length === 0" class="empty-pet">
          <text class="empty-pet-icon">🐾</text>
          <text class="empty-pet-text">请先添加宠物</text>
          <view class="empty-pet-btn" @tap="goAddPet">
            <text class="empty-pet-btn-text">去添加</text>
          </view>
        </view>

        <view v-else>
          <view class="pet-tabs">
            <scroll-view scroll-x class="pet-tabs-scroll">
              <view
                v-for="pet in pets"
                :key="pet.id"
                class="pet-tab"
                :class="{ active: currentPetId === pet.id }"
                @tap="switchPet(pet.id)"
              >
                <image class="pet-tab-avatar" :src="pet.avatar || defaultPetAvatar" mode="aspectFill" />
                <text class="pet-tab-name">{{ pet.name }}</text>
              </view>
            </scroll-view>
          </view>

          <view v-if="activeReminders.length" class="section">
            <view class="section-header">
              <view class="section-label">
                <text class="section-icon">🔔</text>
                <text class="section-text">进行中</text>
              </view>
              <view class="badge"><text class="badge-text">{{ activeReminders.length }}</text></view>
            </view>
            <view class="reminder-list">
              <view v-for="reminder in activeReminders" :key="reminder.id" class="reminder-card">
                <view class="reminder-main">
                  <view class="reminder-icon-wrap">
                    <text class="reminder-emoji">{{ getMealEmoji(reminder.mealType) }}</text>
                  </view>
                  <view class="reminder-info">
                    <text class="reminder-name">{{ getMealName(reminder.mealType) }}</text>
                    <text class="reminder-time">{{ reminder.time }}</text>
                    <text v-if="reminder.note" class="reminder-note">{{ reminder.note }}</text>
                  </view>
                  <view class="reminder-status">
                    <switch :checked="reminder.enabled" @change="onToggleReminder(reminder)" color="#ff6a3d" />
                  </view>
                </view>
                <view class="reminder-actions">
                  <view class="action-btn" @tap="onEditReminder(reminder)"><text class="action-text">编辑</text></view>
                  <view class="action-btn action-btn-del" @tap="onDeleteReminder(reminder)"><text class="action-text">删除</text></view>
                </view>
              </view>
            </view>
          </view>

          <view v-if="!activeReminders.length && currentPetId" class="empty-section">
            <text class="empty-icon">🍽️</text>
            <text class="empty-text">暂无喂食提醒</text>
            <text class="empty-hint">点击下方按钮创建提醒</text>
          </view>

          <view class="tips-card">
            <text class="tips-title">💡 喂食小贴士</text>
            <view class="tips-list">
              <text class="tips-item">• 幼犬每日3-4餐，成犬每日2餐</text>
              <text class="tips-item">• 幼猫每日3-4餐，成猫每日2-3餐</text>
              <text class="tips-item">• 定时定量喂食有助于宠物消化</text>
              <text class="tips-item">• 避免喂食人类食物和零食过量</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="bottom-bar">
      <view class="bottom-bar-inner" :style="{ paddingBottom: 'max(16rpx, env(safe-area-inset-bottom))' }">
        <view class="add-btn" @tap="showAddModal = true">
          <text class="add-btn-text">＋ 添加喂食提醒</text>
        </view>
      </view>
    </view>

    <view v-if="showAddModal" class="popup-mask" @tap="showAddModal = false">
      <view class="popup-content" @tap.stop>
        <view class="popup-header">
          <text class="popup-title">{{ editingReminder ? '编辑提醒' : '添加喂食提醒' }}</text>
          <view class="popup-close" @tap="closeModal"><text class="popup-close-icon">✕</text></view>
        </view>
        <scroll-view scroll-y class="popup-scroll">
          <view class="form-group">
            <text class="form-label">餐次类型</text>
            <view class="meal-options">
              <view
                v-for="meal in mealOptions"
                :key="meal.value"
                class="meal-chip"
                :class="{ active: form.mealType === meal.value }"
                @tap="form.mealType = meal.value"
              >
                <text class="meal-emoji">{{ meal.emoji }}</text>
                <text class="meal-name">{{ meal.label }}</text>
              </view>
            </view>
          </view>
          <view class="form-group">
            <text class="form-label">提醒时间</text>
            <picker mode="time" :value="form.time" @change="onTimeChange">
              <view class="time-picker">
                <text class="time-value">{{ form.time || '请选择时间' }}</text>
                <text class="time-arrow">›</text>
              </view>
            </picker>
          </view>
          <view class="form-group">
            <text class="form-label">重复</text>
            <view class="repeat-options">
              <view
                v-for="opt in repeatOptions"
                :key="opt.value"
                class="repeat-chip"
                :class="{ active: form.repeat === opt.value }"
                @tap="form.repeat = opt.value"
              >
                <text class="repeat-text">{{ opt.label }}</text>
              </view>
            </view>
          </view>
          <view class="form-group">
            <text class="form-label">备注（选填）</text>
            <input class="form-input" v-model="form.note" placeholder="如：狗粮50g" maxlength="30" />
          </view>
        </scroll-view>
        <view class="popup-footer" :style="{ paddingBottom: 'max(24rpx, env(safe-area-inset-bottom))' }">
          <view class="popup-btn cancel" @tap="closeModal"><text class="popup-btn-text">取消</text></view>
          <view class="popup-btn confirm" @tap="onSaveReminder"><text class="popup-btn-text confirm-text">保存</text></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { DEFAULT_PET_AVATAR_URL, requestWxSubscribe } from '@/utils/index'

const MEAL_OPTIONS = [
  { value: 'breakfast', label: '早餐', emoji: '🌅' },
  { value: 'lunch', label: '午餐', emoji: '☀️' },
  { value: 'dinner', label: '晚餐', emoji: '🌙' },
  { value: 'snack', label: '加餐', emoji: '🍪' }
]

const REPEAT_OPTIONS = [
  { value: 'daily', label: '每天' },
  { value: 'weekday', label: '工作日' },
  { value: 'weekend', label: '周末' }
]

export default {
  data() {
    return {
      statusBarHeight: 20,
      defaultPetAvatar: DEFAULT_PET_AVATAR_URL,
      pets: [],
      currentPetId: null,
      reminders: [],
      showAddModal: false,
      editingReminder: null,
      form: { mealType: 'breakfast', time: '08:00', repeat: 'daily', note: '' },
      mealOptions: MEAL_OPTIONS,
      repeatOptions: REPEAT_OPTIONS,
      saving: false
    }
  },
  computed: {
    activeReminders() {
      return this.reminders.filter(r => String(r.petId) === String(this.currentPetId))
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
  },
  async onShow() {
    await this.loadPets()
    this.loadReminders()
  },
  methods: {
    goBack() { uni.navigateBack({ delta: 1 }) },
    goAddPet() { uni.navigateTo({ url: '/pages/pets/index' }) },

    async loadPets() {
      try {
        const res = await uni.$request.get('/api/pets')
        if (res && res.success && Array.isArray(res.data)) {
          this.pets = res.data
          if (this.pets.length && !this.currentPetId) {
            this.currentPetId = this.pets[0].id
          }
          if (this.currentPetId && !this.pets.find(p => String(p.id) === String(this.currentPetId))) {
            this.currentPetId = this.pets.length ? this.pets[0].id : null
          }
        }
      } catch (e) {
        console.error('加载宠物失败:', e)
      }
    },

    async loadReminders() {
      try {
        const res = await uni.$request.get('/api/feeding-reminders')
        if (res && res.success && Array.isArray(res.data)) {
          this.reminders = res.data
        } else {
          this.reminders = []
        }
      } catch (e) {
        console.error('加载喂食提醒失败:', e)
        this.reminders = []
      }
    },

    switchPet(petId) {
      this.currentPetId = petId
    },

    getMealEmoji(type) {
      const meal = MEAL_OPTIONS.find(m => m.value === type)
      return meal ? meal.emoji : '🍽️'
    },

    getMealName(type) {
      const meal = MEAL_OPTIONS.find(m => m.value === type)
      return meal ? meal.label : '喂食'
    },

    onTimeChange(e) {
      this.form.time = e.detail.value
    },

    async onToggleReminder(reminder) {
      try {
        const res = await uni.$request.put(`/api/feeding-reminders/${reminder.id}/toggle`)
        if (res && res.success) {
          reminder.enabled = res.data.enabled
        }
      } catch (e) {
        console.error('切换提醒状态失败:', e)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },

    onEditReminder(reminder) {
      this.editingReminder = reminder
      this.form = {
        mealType: reminder.mealType,
        time: reminder.time,
        repeat: reminder.repeatType || reminder.repeat || 'daily',
        note: reminder.note || ''
      }
      this.showAddModal = true
    },

    onDeleteReminder(reminder) {
      const self = this
      uni.showModal({
        title: '删除提醒',
        content: `确定删除"${self.getMealName(reminder.mealType)} ${reminder.time}"的提醒吗？`,
        confirmColor: '#ff6a3d',
        async success(res) {
          if (!res.confirm) return
          try {
            const result = await uni.$request.delete(`/api/feeding-reminders/${reminder.id}`)
            if (result && result.success) {
              self.reminders = self.reminders.filter(r => r.id !== reminder.id)
              uni.showToast({ title: '已删除', icon: 'success' })
            }
          } catch (e) {
            console.error('删除提醒失败:', e)
            uni.showToast({ title: '删除失败', icon: 'none' })
          }
        }
      })
    },

    closeModal() {
      this.showAddModal = false
      this.editingReminder = null
      this.form = { mealType: 'breakfast', time: '08:00', repeat: 'daily', note: '' }
    },

    async onSaveReminder() {
      if (!this.form.time) {
        uni.showToast({ title: '请选择时间', icon: 'none' })
        return
      }
      if (!this.currentPetId) {
        uni.showToast({ title: '请先选择宠物', icon: 'none' })
        return
      }
      if (this.saving) return
      this.saving = true

      requestWxSubscribe(['feeding'])

      try {
        let res
        if (this.editingReminder) {
          res = await uni.$request.put(`/api/feeding-reminders/${this.editingReminder.id}`, {
            mealType: this.form.mealType,
            time: this.form.time,
            repeat: this.form.repeat,
            note: this.form.note || null
          })
        } else {
          res = await uni.$request.post('/api/feeding-reminders', {
            petId: this.currentPetId,
            mealType: this.form.mealType,
            time: this.form.time,
            repeat: this.form.repeat,
            note: this.form.note || null
          })
        }

        if (res && res.success) {
          uni.showToast({ title: '保存成功', icon: 'success' })
          this.closeModal()
          await this.loadReminders()
        } else {
          uni.showToast({ title: (res && res.message) || '保存失败', icon: 'none' })
        }
      } catch (e) {
        console.error('保存喂食提醒失败:', e)
        uni.showToast({ title: '网络错误', icon: 'none' })
      } finally {
        this.saving = false
      }
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

.reminder-page { min-height: 100vh; background: $bg; }
.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: #fff; }
.status-bar { width: 100%; }
.nav-bar { height: 92rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 28rpx; border-bottom: 1rpx solid #f0f0f0; }
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-back-arrow { width: 20rpx; height: 20rpx; border-left: 4rpx solid $text-primary; border-bottom: 4rpx solid $text-primary; transform: rotate(45deg); }
.nav-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.nav-placeholder { width: 60rpx; }
.page-scroll { height: 100vh; }
.page-content { padding: 24rpx; }

.empty-pet {
  display: flex; flex-direction: column; align-items: center;
  padding: 120rpx 0; background: $card-bg; border-radius: 24rpx;
}
.empty-pet-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-pet-text { font-size: 28rpx; color: $text-secondary; margin-bottom: 24rpx; }
.empty-pet-btn { padding: 16rpx 48rpx; border-radius: 32rpx; background: $primary; }
.empty-pet-btn-text { font-size: 28rpx; color: #fff; font-weight: 600; }

.pet-tabs { margin-bottom: 24rpx; }
.pet-tabs-scroll { white-space: nowrap; }
.pet-tab {
  display: inline-flex; flex-direction: column; align-items: center;
  margin-right: 24rpx; padding: 12rpx;
}
.pet-tab-avatar {
  width: 80rpx; height: 80rpx; border-radius: 50%;
  border: 3rpx solid #e5e7eb; margin-bottom: 8rpx;
}
.pet-tab.active .pet-tab-avatar { border-color: $primary; }
.pet-tab-name { font-size: 24rpx; color: $text-secondary; }
.pet-tab.active .pet-tab-name { color: $primary; font-weight: 600; }

.section { margin-bottom: 24rpx; }
.section-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 16rpx; padding: 0 4rpx;
}
.section-label { display: flex; align-items: center; gap: 8rpx; }
.section-icon { font-size: 28rpx; }
.section-text { font-size: 28rpx; font-weight: 600; color: $text-primary; }
.badge {
  min-width: 36rpx; height: 36rpx; border-radius: 18rpx;
  background: $primary; display: flex; align-items: center;
  justify-content: center; padding: 0 10rpx;
}
.badge-text { font-size: 22rpx; color: #fff; font-weight: 600; }

.reminder-list { display: flex; flex-direction: column; gap: 16rpx; }
.reminder-card {
  background: $card-bg; border-radius: 20rpx; padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.reminder-main { display: flex; align-items: center; }
.reminder-icon-wrap {
  width: 72rpx; height: 72rpx; border-radius: 50%;
  background: $primary-light; display: flex;
  align-items: center; justify-content: center; margin-right: 20rpx; flex-shrink: 0;
}
.reminder-emoji { font-size: 36rpx; }
.reminder-info { flex: 1; }
.reminder-name { display: block; font-size: 28rpx; font-weight: 600; color: $text-primary; }
.reminder-time { display: block; font-size: 24rpx; color: $primary; margin-top: 4rpx; }
.reminder-note { display: block; font-size: 22rpx; color: $text-light; margin-top: 4rpx; }
.reminder-actions {
  display: flex; gap: 16rpx; margin-top: 16rpx; padding-top: 16rpx;
  border-top: 1rpx solid #f5f5f5;
}
.action-btn {
  padding: 8rpx 24rpx; border-radius: 20rpx; background: #f5f5f5;
}
.action-btn-del { background: #fef2f2; }
.action-text { font-size: 24rpx; color: $text-secondary; }
.action-btn-del .action-text { color: #ef4444; }

.empty-section {
  display: flex; flex-direction: column; align-items: center;
  padding: 80rpx 0; background: $card-bg; border-radius: 24rpx; margin-bottom: 24rpx;
}
.empty-icon { font-size: 64rpx; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; color: $text-secondary; margin-bottom: 8rpx; }
.empty-hint { font-size: 24rpx; color: $text-light; }

.tips-card {
  background: $card-bg; border-radius: 24rpx; padding: 28rpx; margin-bottom: 40rpx;
}
.tips-title { display: block; font-size: 28rpx; font-weight: 600; color: $text-primary; margin-bottom: 16rpx; }
.tips-list { display: flex; flex-direction: column; gap: 12rpx; }
.tips-item { font-size: 26rpx; color: $text-secondary; line-height: 1.6; }

.popup-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: flex-end;
}
.popup-content {
  width: 100%; background: #fff; border-radius: 32rpx 32rpx 0 0; max-height: 80vh;
}
.popup-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 32rpx 32rpx 16rpx;
}
.popup-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.popup-close {
  width: 56rpx; height: 56rpx; display: flex; align-items: center;
  justify-content: center; border-radius: 28rpx; background: #f5f5f5;
}
.popup-close-icon { font-size: 28rpx; color: $text-secondary; }
.popup-scroll { max-height: 55vh; padding: 16rpx 32rpx; }

.form-group { margin-bottom: 28rpx; }
.form-label { display: block; font-size: 28rpx; font-weight: 600; color: $text-primary; margin-bottom: 16rpx; }
.form-input {
  width: 100%; height: 80rpx; border: 2rpx solid #e8e8e8; border-radius: 16rpx;
  padding: 0 24rpx; font-size: 28rpx; color: $text-primary; box-sizing: border-box;
}

.meal-options { display: flex; gap: 16rpx; }
.meal-chip {
  flex: 1; display: flex; flex-direction: column; align-items: center;
  padding: 16rpx 8rpx; border-radius: 16rpx; background: #f5f5f5;
  border: 2rpx solid transparent; transition: all 0.2s;
}
.meal-chip.active { border-color: $primary; background: $primary-light; }
.meal-emoji { font-size: 32rpx; margin-bottom: 4rpx; }
.meal-name { font-size: 24rpx; color: $text-secondary; }
.meal-chip.active .meal-name { color: $primary; font-weight: 600; }

.time-picker {
  display: flex; align-items: center; justify-content: space-between;
  height: 80rpx; border: 2rpx solid #e8e8e8; border-radius: 16rpx;
  padding: 0 24rpx;
}
.time-value { font-size: 28rpx; color: $text-primary; }
.time-arrow { font-size: 32rpx; color: #d1d5db; }

.repeat-options { display: flex; gap: 16rpx; }
.repeat-chip {
  flex: 1; height: 72rpx; display: flex; align-items: center;
  justify-content: center; border-radius: 16rpx; background: #f5f5f5;
  border: 2rpx solid transparent; transition: all 0.2s;
}
.repeat-chip.active { border-color: $primary; background: $primary-light; }
.repeat-text { font-size: 26rpx; color: $text-secondary; }
.repeat-chip.active .repeat-text { color: $primary; font-weight: 600; }

.popup-footer { display: flex; gap: 16rpx; padding: 16rpx 32rpx; }
.popup-btn {
  flex: 1; height: 88rpx; display: flex; align-items: center;
  justify-content: center; border-radius: 44rpx;
}
.popup-btn.cancel { background: #f5f5f5; }
.popup-btn.confirm { background: $primary; }
.popup-btn-text { font-size: 30rpx; color: $text-secondary; }
.confirm-text { color: #fff; font-weight: 600; }

.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0; z-index: 30;
  background: #fff; border-top: 1rpx solid #f0f0f0;
}
.bottom-bar-inner { padding: 16rpx 32rpx; }
.add-btn {
  height: 96rpx; border-radius: 48rpx; display: flex;
  align-items: center; justify-content: center; background: $primary;
  box-shadow: 0 8rpx 24rpx rgba(255,106,61,0.3);
}
.add-btn:active { opacity: 0.9; }
.add-btn-text { font-size: 30rpx; font-weight: 600; color: #fff; }
</style>
