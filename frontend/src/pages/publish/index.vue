<template>
  <view class="publish-page">
    <!-- 顶部标题栏 -->
    <view class="publish-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="publish-header-inner">
        <text class="publish-title">发布动态</text>
      </view>
    </view>

    <!-- 主内容区 -->
    <scroll-view scroll-y class="publish-content" :style="{ paddingTop: headerHeight + 'px', paddingBottom: bottomBarHeight + 'px' }">
      <view class="publish-form">
        <!-- 选择宠物 -->
        <view class="form-section">
          <view class="pet-selector" @click="showPetPicker = true">
            <view v-if="selectedPet" class="selected-pet">
              <image class="pet-avatar" :src="selectedPet.avatar" mode="aspectFill" />
              <text class="pet-name">{{ selectedPet.name }}</text>
            </view>
            <view v-else class="placeholder-pet">
              <text class="pet-icon">🐾</text>
              <text class="pet-text">选择宠物</text>
            </view>
            <text class="selector-arrow">›</text>
          </view>
        </view>

        <!-- 内容输入框 -->
        <view class="form-section">
          <textarea
            class="content-input"
            v-model="content"
            placeholder="分享今日宠物瞬间..."
            maxlength="500"
            :auto-height="true"
          />
          <view class="input-counter">
            <text class="counter-text">{{ content.length }}/500</text>
          </view>
        </view>

        <!-- 已选图片/视频 -->
        <view v-if="mediaList.length > 0" class="media-preview">
          <view class="media-grid">
            <view
              v-for="(media, index) in mediaList"
              :key="index"
              class="media-item"
            >
              <image
                v-if="media.type === 'image'"
                class="media-thumb"
                :src="media.path"
                mode="aspectFill"
              />
              <view v-else class="video-thumb">
                <text class="video-icon">🎬</text>
              </view>
              <text class="remove-media" @click="removeMedia(index)">✕</text>
            </view>
          </view>
        </view>

        <!-- 添加图片/视频按钮 -->
        <view class="form-section">
          <view class="add-media-btn" @click="chooseMedia">
            <text class="add-icon">📷</text>
            <text class="add-text">添加图片/视频</text>
          </view>
        </view>

        <!-- 功能按钮区 -->
        <view class="form-section">
          <view class="feature-buttons">
            <view class="feature-btn" @click="addSticker">
              <text class="feature-icon">🎨</text>
              <text class="feature-text">添加贴纸</text>
            </view>
            <view class="feature-btn" @click="addTextBubble">
              <text class="feature-icon">💬</text>
              <text class="feature-text">添加文字气泡</text>
            </view>
            <view class="feature-btn" @click="addLocation">
              <text class="feature-icon">📍</text>
              <text class="feature-text">位置</text>
            </view>
          </view>
        </view>

        <!-- 挑战赛话题 -->
        <view class="form-section">
          <view class="challenge-topic" @click="showChallengePicker = true">
            <text class="challenge-icon">🏆</text>
            <text class="challenge-text">{{ selectedChallenge || '添加挑战赛话题' }}</text>
            <text class="selector-arrow">›</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar" :style="{ paddingBottom: safeAreaBottom + 'px' }">
      <text class="cancel-btn" @click="onCancel">取消</text>
      <text
        class="publish-submit-btn"
        :class="{ disabled: !canSubmit || submitting }"
        @click="onSubmit"
      >
        发布
      </text>
    </view>

    <!-- 宠物选择弹窗 -->
    <view v-if="showPetPicker" class="modal-mask" @click="showPetPicker = false">
      <view class="modal-card" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择宠物</text>
          <text class="modal-close" @click="showPetPicker = false">✕</text>
        </view>
        <view class="pet-list">
          <view
            v-for="pet in petList"
            :key="pet.id"
            class="pet-item"
            :class="{ selected: selectedPet && selectedPet.id === pet.id }"
            @click="selectPet(pet)"
          >
            <image class="pet-item-avatar" :src="pet.avatar" mode="aspectFill" />
            <text class="pet-item-name">{{ pet.name }}</text>
          </view>
          <view class="pet-item add-pet" @click="addNewPet">
            <text class="add-pet-icon">+</text>
            <text class="pet-item-name">添加宠物</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 挑战赛话题选择弹窗 -->
    <view v-if="showChallengePicker" class="modal-mask" @click="showChallengePicker = false">
      <view class="modal-card" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择挑战赛话题</text>
          <text class="modal-close" @click="showChallengePicker = false">✕</text>
        </view>
        <view class="challenge-list">
          <view
            v-for="challenge in challengeList"
            :key="challenge.id"
            class="challenge-item"
            :class="{ selected: selectedChallenge === challenge.name }"
            @click="selectChallenge(challenge)"
          >
            <text class="challenge-item-icon">🏆</text>
            <view class="challenge-item-info">
              <text class="challenge-item-name">{{ challenge.name }}</text>
              <text class="challenge-item-desc">{{ challenge.description }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import * as postApi from '@/api/post'
import * as petApi from '@/api/pet'

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 0,
      bottomBarHeight: 100,
      safeAreaBottom: 0,

      // 表单数据
      content: '',
      selectedPet: null,
      mediaList: [],
      selectedChallenge: '',

      // 弹窗控制
      showPetPicker: false,
      showChallengePicker: false,

      // 数据列表
      petList: [],
      challengeList: [
        { id: 1, name: '#萌宠日常', description: '分享你和宠物的日常瞬间' },
        { id: 2, name: '#宠物才艺', description: '展示宠物的特长和技能' },
        { id: 3, name: '#宠物穿搭', description: '宠物的时尚搭配' },
        { id: 4, name: '#宠物旅行', description: '带宠物去看世界' }
      ],

      submitting: false
    }
  },
  computed: {
    canSubmit() {
      return this.content.trim().length > 0
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
      this.headerHeight = this.statusBarHeight + 88
      this.safeAreaBottom = sys.safeAreaInsets ? sys.safeAreaInsets.bottom : 0
    } catch (e) {
      this.statusBarHeight = 20
      this.headerHeight = 108
      this.safeAreaBottom = 0
    }

    this.loadPetList()
  },
  methods: {
    // 加载宠物列表
    async loadPetList() {
      const token = uni.getStorageSync('token')
      if (!token) return
      
      try {
        const res = await petApi.getPetList()
        if (res.success && Array.isArray(res.data)) {
          this.petList = res.data
        }
      } catch (error) {
        console.error('加载宠物列表失败:', error)
      }
    },
    
    // 取消发布
    onCancel() {
      uni.showModal({
        title: '提示',
        content: '确定要放弃发布吗？',
        success: (res) => {
          if (res.confirm) {
            uni.navigateBack()
          }
        }
      })
    },
    
    // 提交发布
    async onSubmit() {
      if (!this.canSubmit || this.submitting) return

      const token = uni.getStorageSync('token')
      if (!token) {
        uni.showModal({
          title: '提示',
          content: '请先登录',
          showCancel: false
        })
        return
      }

      this.submitting = true
      uni.showLoading({ title: '发布中...', mask: true })

      try {
        // 1. 先上传图片
        const uploadedUrls = []
        const imageFiles = this.mediaList.filter(m => m.type === 'image')
        
        for (let i = 0; i < imageFiles.length; i++) {
          const media = imageFiles[i]
          uni.showLoading({ 
            title: `上传图片 ${i + 1}/${imageFiles.length}...`, 
            mask: true 
          })
          
          try {
            const uploadRes = await petApi.uploadImage(media.path)
            if (uploadRes.success && uploadRes.data && uploadRes.data.url) {
              uploadedUrls.push(uploadRes.data.url)
            } else {
              throw new Error(uploadRes.message || '上传失败')
            }
          } catch (uploadError) {
            console.error(`图片 ${i + 1} 上传失败:`, uploadError)
            uni.hideLoading()
            uni.showToast({
              title: `图片 ${i + 1} 上传失败`,
              icon: 'none'
            })
            this.submitting = false
            return
          }
        }

        // 2. 发布动态
        uni.showLoading({ title: '发布中...', mask: true })
        
        const postData = {
          content: this.content,
          petId: this.selectedPet ? this.selectedPet.id : undefined,
          images: uploadedUrls.length > 0 ? uploadedUrls : undefined,
          challengeTag: this.selectedChallenge || undefined
        }

        const res = await postApi.createPost(postData)

        uni.hideLoading()
        
        if (res.success) {
          uni.showToast({
            title: '发布成功',
            icon: 'success'
          })

          // 清空表单
          this.content = ''
          this.selectedPet = null
          this.mediaList = []
          this.selectedChallenge = ''

          // 返回首页
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        }
      } catch (error) {
        uni.hideLoading()
        console.error('发布失败:', error)
        uni.showToast({
          title: error.message || '发布失败，请重试',
          icon: 'none'
        })
      } finally {
        this.submitting = false
      }
    },
    
    // 选择宠物
    selectPet(pet) {
      this.selectedPet = pet
      this.showPetPicker = false
    },
    
    // 添加新宠物
    addNewPet() {
      this.showPetPicker = false
      uni.navigateTo({
        url: '/pages/pets/add'
      })
    },
    
    // 选择挑战赛话题
    selectChallenge(challenge) {
      this.selectedChallenge = challenge.name
      this.showChallengePicker = false
    },
    
    // 选择图片/视频
    chooseMedia() {
      const remaining = 9 - this.mediaList.length
      if (remaining <= 0) {
        uni.showToast({
          title: '最多添加9个媒体文件',
          icon: 'none'
        })
        return
      }
      
      uni.chooseMedia({
        count: remaining,
        mediaType: ['image', 'video'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          res.tempFiles.forEach(file => {
            this.mediaList.push({
              type: file.fileType,
              path: file.tempFilePath
            })
          })
        }
      })
    },
    
    // 移除媒体
    removeMedia(index) {
      this.mediaList.splice(index, 1)
    },
    
    // 添加贴纸
    addSticker() {
      uni.showToast({
        title: '贴纸功能开发中',
        icon: 'none'
      })
    },
    
    // 添加文字气泡
    addTextBubble() {
      uni.showToast({
        title: '文字气泡功能开发中',
        icon: 'none'
      })
    },
    
    // 添加位置
    addLocation() {
      uni.chooseLocation({
        success: (res) => {
          uni.showToast({
            title: `已选择: ${res.name}`,
            icon: 'none'
          })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.publish-page {
  min-height: 100vh;
  background: var(--pt-bg);
}

/* 顶部导航栏 */
.publish-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.publish-header-inner {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 24rpx;
}

.publish-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
}

/* 主内容区 */
.publish-content {
  height: 100vh;
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1001;
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 30rpx;
  padding-bottom: 60rpx;
}

.cancel-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #666666;
  background-color: #f5f5f5;
  border-radius: 999rpx;
}

.publish-submit-btn {
  flex: 1 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  height: 88rpx !important;
  font-size: 28rpx !important;
  font-weight: 600 !important;
  color: #ffffff !important;
  background: linear-gradient(135deg, #4A90E2 0%, #357ABD 100%) !important;
  box-shadow: 0 4rpx 12rpx rgba(74, 144, 226, 0.3) !important;
  border-radius: 999rpx !important;
  transition: all 0.3s ease !important;

  &:active:not(.disabled) {
    transform: scale(0.98);
    box-shadow: 0 2rpx 8rpx rgba(74, 144, 226, 0.2);
  }

  &.disabled {
    color: #999999 !important;
    background: #e5e5e5 !important;
    box-shadow: none !important;
  }
}

.publish-form {
  padding: 20rpx;
  padding-bottom: 40rpx;
}

.form-section {
  margin-bottom: 20rpx;
}

/* 宠物选择 */
.pet-selector {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #fff;
  border-radius: 16rpx;
  position: relative;
}

.selected-pet,
.placeholder-pet {
  display: flex;
  align-items: center;
  flex: 1;
}

.pet-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  margin-right: 16rpx;
}

.pet-icon {
  font-size: 40rpx;
  margin-right: 12rpx;
}

.pet-text {
  font-size: 28rpx;
  color: #999;
}

.pet-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
}

.selector-arrow {
  font-size: 40rpx;
  color: #999;
}

/* 内容输入 */
.content-input {
  width: 100%;
  min-height: 200rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 28rpx;
  line-height: 1.6;
  box-sizing: border-box;
}

.input-counter {
  display: flex;
  justify-content: flex-end;
  margin-top: 12rpx;
  padding-right: 8rpx;
}

.counter-text {
  font-size: 22rpx;
  color: #999;
}

/* 媒体预览 */
.media-preview {
  margin-bottom: 20rpx;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
}

.media-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 12rpx;
  overflow: hidden;
  background: #f5f5f5;
}

.media-thumb,
.video-thumb {
  width: 100%;
  height: 100%;
}

.video-thumb {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e5e7eb;
}

.video-icon {
  font-size: 60rpx;
}

.remove-media {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 48rpx;
  height: 48rpx;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}

/* 添加媒体按钮 */
.add-media-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48rpx;
  background: #fff;
  border: 2rpx dashed #e0e0e0;
  border-radius: 16rpx;
}

.add-icon {
  font-size: 60rpx;
  margin-bottom: 12rpx;
}

.add-text {
  color: #999;
  font-size: 26rpx;
}

/* 功能按钮 */
.feature-buttons {
  display: flex;
  gap: 16rpx;
}

.feature-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24rpx 12rpx;
  background: #fff;
  border-radius: 16rpx;
}

.feature-icon {
  font-size: 40rpx;
  margin-bottom: 8rpx;
}

.feature-text {
  font-size: 22rpx;
  color: #6b7280;
}

/* 挑战赛话题 */
.challenge-topic {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: linear-gradient(135deg, rgba(255, 122, 61, 0.1) 0%, rgba(255, 77, 79, 0.1) 100%);
  border-radius: 16rpx;
}

.challenge-icon {
  font-size: 36rpx;
  margin-right: 12rpx;
}

.challenge-text {
  flex: 1;
  font-size: 26rpx;
  color: var(--pt-primary);
  font-weight: 600;
}

/* 弹窗样式 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.modal-card {
  width: 100%;
  max-height: 70vh;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #333;
}

.modal-close {
  font-size: 40rpx;
  color: #999;
  padding: 8rpx;
}

/* 宠物列表 */
.pet-list {
  padding: 24rpx;
  max-height: 50vh;
  overflow-y: auto;
}

.pet-item {
  display: flex;
  align-items: center;
  padding: 16rpx;
  margin-bottom: 12rpx;
  border-radius: 12rpx;
  background: #f9f9f9;
  
  &.selected {
    background: rgba(255, 90, 61, 0.1);
  }
}

.pet-item-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 16rpx;
}

.pet-item-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
}

.add-pet {
  justify-content: center;
  background: #f5f5f5;
  border: 2rpx dashed #e0e0e0;
}

.add-pet-icon {
  font-size: 48rpx;
  color: #999;
  margin-right: 12rpx;
}

/* 挑战赛列表 */
.challenge-list {
  padding: 24rpx;
  max-height: 50vh;
  overflow-y: auto;
}

.challenge-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  margin-bottom: 12rpx;
  border-radius: 12rpx;
  background: #f9f9f9;
  
  &.selected {
    background: rgba(255, 90, 61, 0.1);
  }
}

.challenge-item-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.challenge-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.challenge-item-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
  margin-bottom: 6rpx;
}

.challenge-item-desc {
  font-size: 22rpx;
  color: #999;
}
</style>
