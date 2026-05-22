<template>
  <view class="publish-page">
    <view class="status-bar glass-status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    <view class="nav-bar glass-nav-bar">
      <view class="nav-back glass-nav-btn" @click="onCancel">
        <view class="nav-back-arrow"></view>
      </view>
      <text class="nav-title">发布动态</text>
      <view class="nav-placeholder"></view>
    </view>

    <scroll-view scroll-y class="publish-scroll glass-scroll-container" :style="{ top: navHeight + 'px', paddingBottom: bottomBarHeight + 'px' }">
      <view class="feed-list">
        <!-- 宠物选择器 - 玻璃卡片 -->
        <view class="card glass-card">
          <view class="card-label glass-label">
            <text class="card-label-icon">🐾</text>
            <text class="card-label-text">选择宠物</text>
          </view>
          <view class="pet-selector glass-selector" @click="showPetPicker = true">
            <view v-if="selectedPet" class="selected-pet">
              <image class="pet-avatar glass-avatar" :src="selectedPet.avatar" mode="aspectFill" />
              <text class="pet-name">{{ selectedPet.name }}</text>
            </view>
            <view v-else class="placeholder-pet">
              <text class="placeholder-text">点击选择宠物（可选）</text>
            </view>
            <text class="selector-arrow">›</text>
          </view>
        </view>

        <!-- 动态内容 - 玻璃卡片 -->
        <view class="card glass-card">
          <view class="card-label glass-label">
            <text class="card-label-icon">✏️</text>
            <text class="card-label-text">动态内容</text>
          </view>
          <textarea
            class="content-input glass-textarea"
            v-model="content"
            placeholder="分享今日宠物瞬间..."
            maxlength="500"
            :auto-height="true"
          />
          <view class="input-counter">
            <text class="counter-text" :class="{ 'counter-warn': content.length > 450 }">{{ content.length }}/500</text>
          </view>
        </view>

        <!-- 已上传媒体 - 玻璃网格 -->
        <view v-if="mediaList.length > 0" class="card glass-card">
          <view class="card-label glass-label">
            <text class="card-label-icon">📷</text>
            <text class="card-label-text">图片</text>
          </view>
          <view class="media-grid">
            <view
              v-for="(media, index) in mediaList"
              :key="index"
              class="media-item glass-media-item"
            >
              <image
                v-if="media.type === 'image'"
                class="media-thumb"
                :src="media.path"
                mode="aspectFill"
                @click="previewImage(index)"
              />
              <view v-else class="video-thumb" @click="previewVideo(index)">
                <image v-if="media.thumb" class="video-thumb-img" :src="media.thumb" mode="aspectFill" />
                <view class="video-play-icon glass-play-icon">▶</view>
              </view>
              <view class="remove-media glass-remove-btn" @click="removeMedia(index)">
                <text class="remove-media-icon">✕</text>
              </view>
            </view>
            <view v-if="mediaList.length < 9" class="media-add glass-add-btn" @click="chooseMedia">
              <text class="media-add-icon">+</text>
              <text class="media-add-text">添加</text>
            </view>
          </view>
        </view>

        <!-- 添加媒体入口 - 玻璃区域 -->
        <view v-if="mediaList.length === 0" class="card glass-card">
          <view class="add-media-area glass-upload-area" @click="chooseMedia">
            <text class="add-media-emoji">📷</text>
            <text class="add-media-text">添加图片</text>
            <text class="add-media-hint">最多9个媒体文件</text>
          </view>
        </view>

        <!-- 更多选项 - 玻璃卡片 -->
        <view class="card glass-card">
          <view class="card-label glass-label">
            <text class="card-label-icon">✨</text>
            <text class="card-label-text">更多选项</text>
          </view>
          <view class="option-list glass-option-list">
            <view class="option-item glass-option-item" @click="showStickerPicker = true">
              <text class="option-icon">🎨</text>
              <text class="option-text">{{ selectedStickers.length > 0 ? `已选${selectedStickers.length}个贴纸` : '添加贴纸' }}</text>
              <text class="option-arrow">›</text>
            </view>
            <view class="option-item glass-option-item" @click="showBubbleEditor = true">
              <text class="option-icon">💬</text>
              <text class="option-text">{{ bubbleText ? '编辑文字气泡' : '添加文字气泡' }}</text>
              <text class="option-arrow">›</text>
            </view>
            <view class="option-item glass-option-item" @click="chooseLocation">
              <text class="option-icon">📍</text>
              <text class="option-text">{{ selectedLocation || '添加位置' }}</text>
              <text v-if="selectedLocation" class="option-clear" @click.stop="clearLocation">✕</text>
              <text v-else class="option-arrow">›</text>
            </view>
            <view class="option-item glass-option-item" @click="showChallengePicker = true">
              <text class="option-icon">🏆</text>
              <text class="option-text">{{ selectedChallenge || '添加挑战赛话题' }}</text>
              <text class="option-arrow">›</text>
            </view>
            <view class="option-item glass-option-item" @click="showTagPicker = true">
              <text class="option-icon">#️⃣</text>
              <text class="option-text">{{ selectedTags.length > 0 ? '已选' + selectedTags.length + '个标签' : '添加话题标签' }}</text>
              <text class="option-arrow">›</text>
            </view>
          </view>
        </view>

        <!-- 已选标签 - 玻璃卡片 -->
        <view v-if="selectedTags.length > 0" class="card glass-card">
          <view class="card-label glass-label">
            <text class="card-label-icon">#️⃣</text>
            <text class="card-label-text">已选标签</text>
          </view>
          <view class="selected-tags-wrap">
            <view v-for="(tag, idx) in selectedTags" :key="idx" class="selected-tag-chip glass-tag-chip">
              <text class="selected-tag-text">#{{ tag }}</text>
              <text class="selected-tag-remove" @click="removeTag(idx)">✕</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 - 玻璃拟态 -->
    <view class="bottom-bar glass-bottom-bar" :style="{ paddingBottom: safeAreaBottom + 'px' }">
      <text class="cancel-btn glass-btn-cancel" @click="onCancel">取消</text>
      <text
        class="submit-btn glass-btn-submit"
        :class="{ disabled: !canSubmit || submitting }"
        @click="onSubmit"
      >
        {{ submitting ? '发布中...' : '发布' }}
      </text>
    </view>

    <!-- 宠物选择弹窗 - 玻璃模态框 -->
    <view v-if="showPetPicker" class="modal-mask glass-modal-mask" @click="showPetPicker = false">
      <view class="modal-card glass-modal-card" @click.stop>
        <view class="modal-header glass-modal-header">
          <text class="modal-title">选择宠物</text>
          <text class="modal-close glass-modal-close" @click="showPetPicker = false">✕</text>
        </view>
        <view class="pet-list glass-list-container">
          <view
            v-for="pet in petList"
            :key="pet.id"
            class="pet-item glass-list-item"
            :class="{ selected: selectedPet && selectedPet.id === pet.id }"
            @click="selectPet(pet)"
          >
            <image class="pet-item-avatar glass-list-avatar" :src="pet.avatar" mode="aspectFill" />
            <text class="pet-item-name">{{ pet.name }}</text>
            <text v-if="selectedPet && selectedPet.id === pet.id" class="pet-item-check glass-check-icon">✓</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 挑战赛选择弹窗 - 玻璃模态框 -->
    <view v-if="showChallengePicker" class="modal-mask glass-modal-mask" @click="showChallengePicker = false">
      <view class="modal-card glass-modal-card" @click.stop>
        <view class="modal-header glass-modal-header">
          <text class="modal-title">选择挑战赛话题</text>
          <text class="modal-close glass-modal-close" @click="showChallengePicker = false">✕</text>
        </view>
        <view class="challenge-list glass-list-container">
          <view
            v-for="challenge in challengeList"
            :key="challenge.id"
            class="challenge-item glass-list-item"
            :class="{ selected: selectedChallenge === challenge.name }"
            @click="selectChallenge(challenge)"
          >
            <text class="challenge-item-icon">🏆</text>
            <view class="challenge-item-info">
              <text class="challenge-item-name">{{ challenge.name }}</text>
              <text class="challenge-item-desc">{{ challenge.description }}</text>
            </view>
            <text v-if="selectedChallenge === challenge.name" class="challenge-item-check glass-check-icon">✓</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 贴纸选择弹窗 - 玻璃模态框 -->
    <view v-if="showStickerPicker" class="modal-mask glass-modal-mask" @click="showStickerPicker = false">
      <view class="modal-card sticker-modal glass-modal-card-large" @click.stop>
        <view class="modal-header glass-modal-header">
          <text class="modal-title">选择贴纸</text>
          <text class="modal-close glass-modal-close" @click="showStickerPicker = false">✕</text>
        </view>
        <view class="sticker-category glass-category-tabs">
          <view
            v-for="cat in stickerCategories"
            :key="cat.key"
            class="sticker-cat-item glass-tab-chip"
            :class="{ active: currentStickerCat === cat.key }"
            @click="currentStickerCat = cat.key"
          >
            <text class="sticker-cat-text">{{ cat.label }}</text>
          </view>
        </view>
        <view class="sticker-grid glass-sticker-grid">
          <view
            v-for="sticker in currentStickers"
            :key="sticker"
            class="sticker-item glass-sticker-cell"
            :class="{ selected: selectedStickers.includes(sticker) }"
            @click="toggleSticker(sticker)"
          >
            <text class="sticker-emoji">{{ sticker }}</text>
          </view>
        </view>
        <view v-if="selectedStickers.length > 0" class="sticker-selected-bar glass-selected-bar">
          <scroll-view scroll-x class="sticker-selected-scroll">
            <view class="sticker-selected-list">
              <view v-for="(s, i) in selectedStickers" :key="i" class="sticker-selected-item glass-selected-chip">
                <text class="sticker-selected-emoji">{{ s }}</text>
                <view class="sticker-remove glass-mini-remove" @click="removeSticker(i)">
                  <text class="sticker-remove-icon">✕</text>
                </view>
              </view>
            </view>
          </scroll-view>
          <text class="sticker-confirm-btn glass-confirm-btn" @click="showStickerPicker = false">确定</text>
        </view>
      </view>
    </view>

    <!-- 文字气泡编辑弹窗 - 玻璃模态框 -->
    <view v-if="showBubbleEditor" class="modal-mask glass-modal-mask" @click="showBubbleEditor = false">
      <view class="modal-card bubble-modal glass-modal-card" @click.stop>
        <view class="modal-header glass-modal-header">
          <text class="modal-title">文字气泡</text>
          <text class="modal-close glass-modal-close" @click="showBubbleEditor = false">✕</text>
        </view>
        <view class="bubble-editor">
          <view class="bubble-preview glass-bubble-preview" :style="{ background: bubbleColor }">
            <text class="bubble-preview-text" :style="{ color: bubbleTextColor }">{{ bubbleText || '输入文字预览...' }}</text>
          </view>
          <textarea
            class="bubble-input glass-bubble-input"
            v-model="bubbleText"
            placeholder="输入气泡文字..."
            maxlength="30"
          />
          <view class="bubble-color-row">
            <text class="bubble-color-label">气泡颜色</text>
            <view class="bubble-color-options">
              <view
                v-for="c in bubbleColors"
                :key="c.bg"
                class="bubble-color-dot glass-color-dot"
                :class="{ active: bubbleColor === c.bg }"
                :style="{ background: c.bg }"
                @click="bubbleColor = c.bg; bubbleTextColor = c.text"
              />
            </view>
          </view>
          <view v-if="bubbleText" class="bubble-actions">
            <text class="bubble-clear-btn glass-btn-secondary-small" @click="bubbleText = ''">清除</text>
            <text class="bubble-confirm-btn glass-btn-primary-small" @click="showBubbleEditor = false">确定</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 标签选择弹窗 - 玻璃模态框 -->
    <view v-if="showTagPicker" class="modal-mask glass-modal-mask" @click="showTagPicker = false">
      <view class="modal-card glass-modal-card-large" @click.stop>
        <view class="modal-header glass-modal-header">
          <text class="modal-title">选择话题标签</text>
          <text class="modal-close glass-modal-close" @click="showTagPicker = false">✕</text>
        </view>
        <view class="tag-search-bar glass-search-box">
          <input class="tag-search-input glass-search-input" v-model="tagSearchKeyword" placeholder="搜索标签..." @input="onTagSearch" />
        </view>
        <view class="tag-grid glass-tag-grid">
          <view
            v-for="tag in tagList"
            :key="tag.id || tag.name"
            class="tag-chip glass-tag-chip-selectable"
            :class="{ active: selectedTags.includes(tag.name) }"
            @click="toggleTag(tag.name)"
          >
            <text class="tag-chip-text">#{{ tag.name }}</text>
            <text v-if="tag.usageCount" class="tag-chip-count">{{ tag.usageCount }}</text>
          </view>
        </view>
        <view v-if="selectedTags.length > 0" class="tag-selected-bar glass-selected-bar">
          <scroll-view scroll-x class="tag-selected-scroll">
            <view class="tag-selected-list">
              <view v-for="(t, i) in selectedTags" :key="i" class="tag-selected-item glass-selected-chip">
                <text class="tag-selected-text">#{{ t }}</text>
                <view class="tag-remove glass-mini-remove" @click="removeTag(i)">
                  <text class="tag-remove-icon">✕</text>
                </view>
              </view>
            </view>
          </scroll-view>
          <text class="tag-confirm-btn glass-confirm-btn" @click="showTagPicker = false">确定</text>
        </view>
      </view>
    </view>

    <!-- 视频播放器 - 玻璃遮罩 -->
    <view v-if="showVideoPlayer" class="video-player-mask glass-video-mask" @click="closeVideoPlayer">
      <view class="video-player-container glass-video-container" @click.stop>
        <view class="video-player-close glass-video-close" @click="closeVideoPlayer">✕</view>
        <video
          class="video-player-video"
          :src="currentVideoUrl"
          :autoplay="true"
          :show-play-btn="true"
          :show-center-play-btn="true"
          :enable-progress-gesture="true"
          :show-fullscreen-btn="true"
          object-fit="contain"
        />
      </view>
    </view>
  </view>
</template>

<script>
import * as postApi from '@/api/post'
import * as petApi from '@/api/pet'
import * as tagApi from '@/api/tag'
import { checkLogin } from '@/utils/index'

export default {
  data() {
    return {
      statusBarHeight: 20,
      navHeight: 64,
      bottomBarHeight: 100,
      safeAreaBottom: 0,

      content: '',
      selectedPet: null,
      mediaList: [],
      selectedChallenge: '',
      selectedStickers: [],
      bubbleText: '',
      bubbleColor: '#ff6a3d',
      bubbleTextColor: '#ffffff',
      selectedLocation: '',

      showPetPicker: false,
      showChallengePicker: false,
      showStickerPicker: false,
      showBubbleEditor: false,
      showTagPicker: false,
      currentStickerCat: 'pet',

      selectedTags: [],
      tagSearchKeyword: '',
      tagList: [],

      stickerCategories: [
        { key: 'pet', label: '🐾 宠物' },
        { key: 'mood', label: '😊 心情' },
        { key: 'food', label: '🍖 美食' },
        { key: 'nature', label: '🌸 自然' },
        { key: 'deco', label: '✨ 装饰' }
      ],

      stickerMap: {
        pet: ['🐱', '🐶', '🐰', '🐹', '🐦', '🐟', '🐢', '🦎', '🐍', '🐸', '🦜', '🐩', '🐈', '🦮', '🐕‍🦺', '🐈‍⬛'],
        mood: ['😊', '😂', '🥰', '😍', '😎', '🤩', '😜', '🥳', '😴', '🤗', '😭', '😤', '🥺', '😱', '🤯', '😇'],
        food: ['🍖', '🦴', '🥩', '🍗', '🥓', '🍔', '🍕', '🌭', '🥕', '🥦', '🍎', '🍌', '🥛', '🍼', '☕', '🧃'],
        nature: ['🌸', '🌺', '🌻', '🌹', '🌷', '🍀', '🌿', '🌴', '🌈', '☀️', '🌙', '⭐', '❄️', '🌊', '🍂', '🌾'],
        deco: ['✨', '💫', '⭐', '🌟', '💖', '💝', '🎀', '🎉', '🎊', '🎈', '🎁', '🏆', '👑', '💎', '🦋', '🐝']
      },

      bubbleColors: [
        { bg: '#ff6a3d', text: '#ffffff' },
        { bg: '#10b981', text: '#ffffff' },
        { bg: '#3b82f6', text: '#ffffff' },
        { bg: '#8b5cf6', text: '#ffffff' },
        { bg: '#f59e0b', text: '#ffffff' },
        { bg: '#ec4899', text: '#ffffff' },
        { bg: '#1f2937', text: '#ffffff' },
        { bg: '#ffffff', text: '#111827' }
      ],

      petList: [],
      challengeList: [
        { id: 1, name: '#萌宠日常', description: '分享你和宠物的日常瞬间' },
        { id: 2, name: '#宠物才艺', description: '展示宠物的特长和技能' },
        { id: 3, name: '#宠物穿搭', description: '宠物的时尚搭配' },
        { id: 4, name: '#宠物旅行', description: '带宠物去看世界' }
      ],

      submitting: false,
      showVideoPlayer: false,
      currentVideoUrl: ''
    }
  },
  computed: {
    canSubmit() {
      return this.content.trim().length > 0 || this.mediaList.length > 0
    },
    currentStickers() {
      return this.stickerMap[this.currentStickerCat] || []
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
      this.navHeight = this.statusBarHeight + 44
      this.safeAreaBottom = sys.safeAreaInsets ? sys.safeAreaInsets.bottom : 0
    } catch (e) {
      this.navHeight = 64
      this.safeAreaBottom = 0
    }

    this.loadPetList()
    this.loadHotTags()
  },
  methods: {
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

    onCancel() {
      if (!this.content.trim() && this.mediaList.length === 0) {
        uni.navigateBack()
        return
      }
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

    async onSubmit() {
      if (!this.canSubmit || this.submitting) return

      const loggedIn = await checkLogin('请先登录后再发布动态')
      if (!loggedIn) return

      this.submitting = true
      uni.showLoading({ title: '发布中...', mask: true })

      try {
        const imageFiles = this.mediaList.filter(m => m.type === 'image')
        const videoFiles = this.mediaList.filter(m => m.type === 'video')
        const totalFiles = imageFiles.length + videoFiles.length
        let uploadedCount = 0

        const uploadedImageUrls = []
        for (let i = 0; i < imageFiles.length; i++) {
          const media = imageFiles[i]
          uploadedCount++
          uni.showLoading({
            title: `上传 ${uploadedCount}/${totalFiles}...`,
            mask: true
          })

          try {
            const uploadRes = await petApi.uploadFile(media.path)
            if (uploadRes.success && uploadRes.data && uploadRes.data.url) {
              uploadedImageUrls.push(uploadRes.data.url)
            } else {
              throw new Error(uploadRes.message || '上传失败')
            }
          } catch (uploadError) {
            console.error(`图片 ${i + 1} 上传失败:`, uploadError)
            uni.hideLoading()
            uni.showToast({ title: `图片 ${i + 1} 上传失败`, icon: 'none' })
            this.submitting = false
            return
          }
        }

        const uploadedVideoUrls = []
        for (let i = 0; i < videoFiles.length; i++) {
          const media = videoFiles[i]
          uploadedCount++
          uni.showLoading({
            title: `上传中 ${uploadedCount}/${totalFiles}...`,
            mask: true
          })

          try {
            const uploadRes = await petApi.uploadFile(media.path)
            if (uploadRes.success && uploadRes.data && uploadRes.data.url) {
              uploadedVideoUrls.push(uploadRes.data.url)
            } else {
              throw new Error(uploadRes.message || '上传失败')
            }
          } catch (uploadError) {
            console.error(`视频 ${i + 1} 上传失败:`, uploadError)
            uni.hideLoading()
            uni.showToast({ title: `视频 ${i + 1} 上传失败`, icon: 'none' })
            this.submitting = false
            return
          }
        }

        uni.showLoading({ title: '发布中...', mask: true })

        const postData = {
          content: this.content,
          petId: this.selectedPet ? this.selectedPet.id : undefined,
          images: uploadedImageUrls.length > 0 ? uploadedImageUrls : undefined,
          videos: uploadedVideoUrls.length > 0 ? uploadedVideoUrls : undefined,
          challengeTag: this.selectedChallenge || undefined,
          tags: this.selectedTags.length > 0 ? this.selectedTags : undefined,
          stickers: this.selectedStickers.length > 0 ? this.selectedStickers : undefined,
          bubble: this.bubbleText ? { text: this.bubbleText, bgColor: this.bubbleColor, textColor: this.bubbleTextColor } : undefined,
          location: this.selectedLocation || undefined
        }

        const res = await postApi.createPost(postData)

        uni.hideLoading()

        if (res.success) {
          uni.showToast({ title: '发布成功', icon: 'success' })
          this.content = ''
          this.selectedPet = null
          this.mediaList = []
          this.selectedChallenge = ''
          this.selectedStickers = []
          this.selectedTags = []
          this.bubbleText = ''
          this.bubbleColor = '#ff6a3d'
          this.bubbleTextColor = '#ffffff'
          this.selectedLocation = ''
          setTimeout(() => {
            uni.switchTab({ url: '/pages/home/index' })
          }, 1500)
        }
      } catch (error) {
        uni.hideLoading()
        console.error('发布失败:', error)
        uni.showToast({ title: error.message || '发布失败，请重试', icon: 'none' })
      } finally {
        this.submitting = false
      }
    },

    selectPet(pet) {
      this.selectedPet = pet
      this.showPetPicker = false
    },

    addNewPet() {
      this.showPetPicker = false
      uni.navigateTo({ url: '/pages/pets/add' })
    },

    selectChallenge(challenge) {
      this.selectedChallenge = challenge.name
      this.showChallengePicker = false
    },

    chooseMedia() {
      const remaining = 9 - this.mediaList.length
      if (remaining <= 0) {
        uni.showToast({ title: '最多添加9个媒体文件', icon: 'none' })
        return
      }

      uni.showActionSheet({
        itemList: ['拍照', '从相册选择图片'],
        success: (res) => {
          if (res.tapIndex === 0) {
            this.chooseFromCamera()
          } else if (res.tapIndex === 1) {
            this.chooseImage()
          }
        }
      })
    },

    chooseFromCamera() {
      uni.chooseMedia({
        count: 1,
        mediaType: ['image'],
        sourceType: ['camera'],
        sizeType: ['compressed'],
        success: (res) => {
          res.tempFiles.forEach(file => {
            if (this.mediaList.length >= 9) return
            this.mediaList.push({
              type: 'image',
              path: file.tempFilePath
            })
          })
        }
      })
    },

    chooseImage() {
      const remaining = 9 - this.mediaList.length
      if (remaining <= 0) return
      uni.chooseMedia({
        count: remaining,
        mediaType: ['image'],
        sourceType: ['album'],
        sizeType: ['compressed'],
        success: (res) => {
          res.tempFiles.forEach(file => {
            if (this.mediaList.length >= 9) return
            this.mediaList.push({
              type: 'image',
              path: file.tempFilePath
            })
          })
        }
      })
    },

    chooseVideo() {
      const remaining = 9 - this.mediaList.length
      if (remaining <= 0) return
      uni.chooseVideo({
        sourceType: ['album', 'camera'],
        compressed: false,
        maxDuration: 60,
        success: (res) => {
          if (this.mediaList.length >= 9) return
          const item = {
            type: 'video',
            path: res.tempFilePath
          }
          if (res.thumbTempFilePath) {
            item.thumb = res.thumbTempFilePath
          }
          this.mediaList.push(item)
        }
      })
    },

    previewImage(index) {
      const imagePaths = this.mediaList
        .filter(m => m.type === 'image')
        .map(m => m.path)
      if (imagePaths.length > 0) {
        uni.previewImage({ urls: imagePaths, current: index })
      }
    },

    previewVideo(index) {
      const video = this.mediaList[index]
      if (video && video.path) {
        this.currentVideoUrl = video.path
        this.showVideoPlayer = true
      }
    },

    closeVideoPlayer() {
      this.showVideoPlayer = false
      this.currentVideoUrl = ''
    },

    removeMedia(index) {
      this.mediaList.splice(index, 1)
    },

    toggleSticker(sticker) {
      const idx = this.selectedStickers.indexOf(sticker)
      if (idx >= 0) {
        this.selectedStickers.splice(idx, 1)
      } else if (this.selectedStickers.length < 10) {
        this.selectedStickers.push(sticker)
      } else {
        uni.showToast({ title: '最多选择10个贴纸', icon: 'none' })
      }
    },

    removeSticker(index) {
      this.selectedStickers.splice(index, 1)
    },

    chooseLocation() {
      uni.chooseLocation({
        success: (res) => {
          this.selectedLocation = res.name || res.address
        },
        fail: (err) => {
          if (err && err.errMsg && err.errMsg.indexOf('auth deny') !== -1) {
            uni.showModal({
              title: '位置权限',
              content: '需要获取您的位置信息，请在设置中开启',
              success: (modalRes) => {
                if (modalRes.confirm) {
                  uni.openSetting()
                }
              }
            })
          }
        }
      })
    },

    clearLocation() {
      this.selectedLocation = ''
    },

    async loadHotTags() {
      try {
        const res = await tagApi.getHotTags(30)
        if (res && res.success && Array.isArray(res.data)) {
          this.tagList = res.data
        }
      } catch (e) {
        console.error('加载热门标签失败:', e)
      }
    },

    async onTagSearch() {
      try {
        const keyword = this.tagSearchKeyword.trim()
        if (!keyword) {
          await this.loadHotTags()
          return
        }
        const res = await tagApi.searchTags(keyword, 20)
        if (res && res.success && Array.isArray(res.data)) {
          this.tagList = res.data
        }
      } catch (e) {
        console.error('搜索标签失败:', e)
      }
    },

    toggleTag(name) {
      const idx = this.selectedTags.indexOf(name)
      if (idx >= 0) {
        this.selectedTags.splice(idx, 1)
      } else if (this.selectedTags.length < 5) {
        this.selectedTags.push(name)
      } else {
        uni.showToast({ title: '最多选择5个标签', icon: 'none' })
      }
    },

    removeTag(index) {
      this.selectedTags.splice(index, 1)
    }
  }
}
</script>

<style lang="scss" scoped>
/* ============================================
   发布动态页 - 玻璃拟态风格 (Glassmorphism)
   保持原逻辑不变，全面升级视觉体验
   ============================================ */

.publish-page {
  min-height: 100vh;
  background: transparent;
}

/* ====== 导航栏 - 渐变玻璃 ====== */
.glass-status-bar {
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
}

.glass-nav-bar {
  height: 92rpx;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx;
}

.glass-nav-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(16px);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 4rpx 16rpx rgba(0, 0, 0, 0.12),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.35);
  border: 1rpx solid rgba(255, 255, 255, 0.35);
  transition: all 0.28s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:active {
    background: rgba(255, 255, 255, 0.4);
    transform: scale(0.92);
  }
}

.nav-back-arrow {
  width: 18rpx;
  height: 18rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg) translate(2rpx, -2rpx);
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 2rpx 8rpx rgba(180, 30, 10, 0.25);
}

.nav-placeholder {
  width: 64rpx;
}

/* ====== 滚动容器 ====== */
.glass-scroll-container {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(255, 245, 243, 0.95) 0%, rgba(250, 250, 252, 0.98) 100%);
}

.feed-list {
  padding: 16rpx 24rpx;
  padding-bottom: 40rpx;
}

/* ====== 卡片 - 统一玻璃样式 ====== */
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
  border: 1rpx solid rgba(255, 255, 255, 0.62);

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
}

/* ====== 标签 ====== */
.glass-label {
  display: flex;
  align-items: center;
  margin-bottom: 22rpx;
}

.card-label-icon {
  font-size: 32rpx;
  margin-right: 10rpx;
}

.card-label-text {
  font-size: 29rpx;
  font-weight: 700;
  color: var(--pt-text, #111827);
  letter-spacing: 0.5rpx;
}

/* ====== 选择器 ====== */
.glass-selector {
  display: flex;
  align-items: center;
  padding: 22rpx;
  background: rgba(249, 250, 251, 0.7);
  border-radius: 18rpx;
  backdrop-filter: blur(8px);
  border: 1rpx solid rgba(209, 213, 219, 0.25);
  transition: all 0.3s ease;

  &:active {
    background: rgba(255, 255, 255, 0.88);
    transform: scale(0.99);
  }
}

.selected-pet,
.placeholder-pet {
  display: flex;
  align-items: center;
  flex: 1;
}

.glass-avatar {
  width: 68rpx;
  height: 68rpx;
  border-radius: 50%;
  margin-right: 18rpx;
  background: linear-gradient(135deg, #e5e7eb 0%, #f3f4f6 100%);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
}

.pet-name {
  font-size: 29rpx;
  font-weight: 700;
  color: var(--pt-text, #111827);
}

.placeholder-text {
  font-size: 27rpx;
  color: var(--pt-muted, #9ca3af);
  font-weight: 500;
}

.selector-arrow {
  font-size: 40rpx;
  color: var(--pt-muted, #9ca3af);
}

/* ====== 输入区域 ====== */
.glass-textarea {
  width: 100%;
  min-height: 220rpx;
  background: rgba(249, 250, 251, 0.65);
  border-radius: 18rpx;
  padding: 26rpx;
  font-size: 29rpx;
  line-height: 1.65;
  color: var(--pt-text, #374151);
  box-sizing: border-box;
  backdrop-filter: blur(10px);
  border: 1.5rpx solid rgba(209, 213, 219, 0.3);
  transition: all 0.28s ease;

  &:focus {
    border-color: var(--pt-primary, #ff6a3d);
    background: rgba(255, 255, 255, 0.88);
    box-shadow: 0 0 0 5rpx rgba(255, 106, 61, 0.1);
  }
}

.input-counter {
  display: flex;
  justify-content: flex-end;
  margin-top: 14rpx;
}

.counter-text {
  font-size: 23rpx;
  color: var(--pt-muted, #9ca3af);
  font-weight: 500;
}

.counter-warn {
  color: #ff4d4f;
  font-weight: 700;
}

/* ====== 媒体网格 ====== */
.media-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;
}

.glass-media-item {
  position: relative;
  width: 100%;
  height: 200rpx;
  border-radius: 18rpx;
  overflow: hidden;
  background: rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(6px);
  border: 1rpx solid rgba(209, 213, 219, 0.15);
  transition: all 0.3s ease;

  &:hover,
  &:active {
    transform: scale(0.97);
    box-shadow: 0 4rpx 14rpx rgba(31, 38, 135, 0.1);
  }
}

.media-thumb {
  width: 100%;
  height: 100%;
}

.video-thumb {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.video-thumb-img {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

.glass-play-icon {
  position: absolute;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(8px);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  z-index: 1;
  box-shadow: 0 4rpx 14rpx rgba(0, 0, 0, 0.2);
}

.glass-remove-btn {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 46rpx;
  height: 46rpx;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(8px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s ease;

  &:active {
    transform: scale(0.9);
    background: rgba(255, 77, 79, 0.85);
  }
}

.remove-media-icon {
  font-size: 23rpx;
  color: #fff;
  font-weight: 600;
}

/* ====== 添加按钮 ====== */
.glass-add-btn {
  width: 100%;
  height: 200rpx;
  border: 2rpx dashed rgba(209, 213, 219, 0.5);
  border-radius: 18rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(249, 250, 251, 0.5);
  backdrop-filter: blur(8px);
  transition: all 0.32s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.96);
    background: rgba(255, 122, 61, 0.08);
    border-color: var(--pt-primary, #ff6a3d);
  }
}

.media-add-icon {
  font-size: 52rpx;
  color: var(--pt-muted, #9ca3af);
  margin-bottom: 10rpx;
}

.media-add-text {
  font-size: 24rpx;
  color: var(--pt-muted, #9ca3af);
  font-weight: 500;
}

/* ====== 上传区域 ====== */
.glass-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56rpx;
  background: linear-gradient(135deg, rgba(255, 245, 240, 0.75) 0%, rgba(255, 250, 247, 0.68) 100%);
  border-radius: 18rpx;
  border: 2rpx dashed rgba(255, 122, 61, 0.35);
  backdrop-filter: blur(10px);
  transition: all 0.32s ease;

  &:active {
    transform: scale(0.97);
    background: linear-gradient(135deg, rgba(255, 235, 230, 0.85) 0%, rgba(255, 245, 240, 0.78) 100%);
    border-color: var(--pt-primary, #ff6a3d);
  }
}

.add-media-emoji {
  font-size: 66rpx;
  margin-bottom: 14rpx;
}

.add-media-text {
  font-size: 29rpx;
  color: var(--pt-secondary, #6b7280);
  font-weight: 600;
  margin-bottom: 10rpx;
}

.add-media-hint {
  font-size: 23rpx;
  color: var(--pt-muted, #9ca3af);
  font-weight: 500;
}

/* ====== 选项列表 ====== */
.glass-option-list {
  border-radius: 18rpx;
  overflow: hidden;
  backdrop-filter: blur(8px);
}

.glass-option-item {
  display: flex;
  align-items: center;
  padding: 26rpx 18rpx;
  border-bottom: 1rpx solid rgba(243, 244, 246, 0.6);
  transition: all 0.28s ease;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: rgba(255, 122, 61, 0.05);
    transform: translateX(6rpx);
  }
}

.option-icon {
  font-size: 34rpx;
  margin-right: 18rpx;
}

.option-text {
  flex: 1;
  font-size: 28rpx;
  color: var(--pt-text, #374151);
  font-weight: 500;
}

.option-arrow {
  font-size: 32rpx;
  color: var(--pt-muted, #d1d5db);
}

.option-clear {
  font-size: 26rpx;
  color: var(--pt-muted, #9ca3af);
  padding: 6rpx 10rpx;
  background: rgba(209, 213, 219, 0.3);
  border-radius: 12rpx;
}

/* ====== 底部操作栏 ====== */
.glass-bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(24px);
  box-shadow:
    0 -8rpx 32rpx rgba(31, 38, 135, 0.12),
    0 -2rpx 8rpx rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  gap: 22rpx;
  padding: 22rpx 28rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.6);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 20%;
    right: 20%;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.85), transparent);
    pointer-events: none;
  }
}

.glass-btn-cancel {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 29rpx;
  font-weight: 700;
  color: var(--pt-secondary, #6b7280);
  background: rgba(249, 250, 251, 0.9);
  border-radius: 44rpx;
  backdrop-filter: blur(10px);
  border: 1rpx solid rgba(209, 213, 219, 0.35);
  box-shadow:
    0 4rpx 14rpx rgba(0, 0, 0, 0.05),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.85);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.96);
    background: rgba(233, 234, 236, 0.95);
  }
}

.glass-btn-submit {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 31rpx;
  font-weight: 800;
  color: #fff;
  letter-spacing: 3rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  border-radius: 44rpx;
  box-shadow:
    0 10rpx 32rpx rgba(255, 90, 61, 0.4),
    0 4rpx 12rpx rgba(255, 90, 61, 0.25),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.3),
    inset 0 -2rpx 0 rgba(180, 50, 20, 0.2);
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

  &:active:not(.disabled) {
    transform: scale(0.97);
    opacity: 0.95;
  }

  &:active:not(.disabled)::after {
    left: 100%;
  }

  &.disabled {
    color: #b8bcc4;
    background: linear-gradient(135deg, #e5e7eb 0%, #d1d5db 100%);
    box-shadow: none;
  }
}

/* ====== 模态框系统 - 玻璃拟态 ====== */
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
  width: 100%;
  max-height: 70vh;
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

.glass-modal-card-large {
  max-height: 82vh;
}

.glass-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

/* ====== 列表容器 ====== */
.glass-list-container {
  padding: 18rpx;
  max-height: 50vh;
  overflow-y: auto;
}

.glass-list-item {
  display: flex;
  align-items: center;
  padding: 22rpx 18rpx;
  border-radius: 18rpx;
  margin-bottom: 10rpx;
  background: rgba(249, 250, 251, 0.5);
  backdrop-filter: blur(6px);
  border: 1rpx solid rgba(209, 213, 219, 0.15);
  transition: all 0.28s ease;

  &.selected {
    background: linear-gradient(135deg, rgba(255, 245, 240, 0.9) 0%, rgba(255, 250, 247, 0.85) 100%);
    border-color: rgba(255, 122, 61, 0.3);
    box-shadow: 0 4rpx 14rpx rgba(255, 106, 61, 0.1);
  }

  &:active {
    transform: scale(0.98);
  }
}

.glass-list-avatar {
  width: 68rpx;
  height: 68rpx;
  border-radius: 50%;
  margin-right: 18rpx;
  background: linear-gradient(135deg, #e5e7eb 0%, #f3f4f6 100%);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
}

.pet-item-name,
.challenge-item-name {
  flex: 1;
  font-size: 28rpx;
  color: var(--pt-text, #374151);
  font-weight: 600;
}

.challenge-item-desc {
  font-size: 23rpx;
  color: var(--pt-muted, #9ca3af);
  margin-top: 4rpx;
}

.glass-check-icon {
  font-size: 30rpx;
  color: var(--pt-primary, #ff6a3d);
  font-weight: 800;
}

.challenge-item-icon {
  font-size: 36rpx;
  margin-right: 18rpx;
}

.challenge-item-info {
  flex: 1;
}

/* ====== 视频播放器 ====== */
.glass-video-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(12px);
  z-index: 19999;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: videoFadeIn 0.3s ease-out both;
}

@keyframes videoFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.glass-video-container {
  position: relative;
  width: 100%;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow:
    0 24rpx 60rpx rgba(0, 0, 0, 0.4),
    0 8rpx 24rpx rgba(0, 0, 0, 0.2);
}

.glass-video-close {
  position: absolute;
  top: -80rpx;
  right: 20rpx;
  width: 68rpx;
  height: 68rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(12px);
  color: #fff;
  font-size: 34rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.2);
  transition: all 0.28s ease;

  &:active {
    transform: scale(0.9) rotate(90deg);
    background: rgba(255, 77, 79, 0.5);
  }
}

.video-player-video {
  width: 100%;
  height: 420rpx;
}

/* ====== 贴纸选择器 ====== */
.glass-category-tabs {
  display: flex;
  padding: 18rpx 26rpx;
  gap: 14rpx;
  border-bottom: 1rpx solid rgba(243, 244, 246, 0.6);
  overflow-x: auto;
  backdrop-filter: blur(8px);
}

.glass-tab-chip {
  flex-shrink: 0;
  padding: 12rpx 26rpx;
  border-radius: 28rpx;
  background: rgba(249, 250, 251, 0.8);
  backdrop-filter: blur(6px);
  border: 1rpx solid rgba(209, 213, 219, 0.25);
  transition: all 0.32s cubic-bezier(0.4, 0, 0.2, 1);

  &.active {
    background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
    border-color: transparent;
    box-shadow:
      0 4rpx 14rpx rgba(255, 106, 61, 0.3),
      inset 0 1rpx 0 rgba(255, 255, 255, 0.3);
    transform: scale(1.03);
  }

  &:active {
    transform: scale(0.96);
  }
}

.sticker-cat-text {
  font-size: 25rpx;
  color: var(--pt-text, #374151);
  font-weight: 600;

  .glass-tab-chip.active & {
    color: #fff;
    font-weight: 800;
    text-shadow: 0 1rpx 4rpx rgba(180, 30, 10, 0.3);
  }
}

.glass-sticker-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14rpx;
  padding: 22rpx 26rpx;
  max-height: 42vh;
  overflow-y: auto;
}

.glass-sticker-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 110rpx;
  border-radius: 18rpx;
  background: rgba(249, 250, 251, 0.7);
  backdrop-filter: blur(6px);
  border: 2rpx solid transparent;
  transition: all 0.28s ease;

  &.selected {
    border-color: var(--pt-primary, #ff6a3d);
    background: linear-gradient(135deg, rgba(255, 245, 240, 0.9) 0%, rgba(255, 250, 247, 0.85) 100%);
    box-shadow: 0 4rpx 14rpx rgba(255, 106, 61, 0.15);
    transform: scale(1.02);
  }

  &:active {
    transform: scale(0.95);
  }
}

.sticker-emoji {
  font-size: 52rpx;
}

/* ====== 选中栏 ====== */
.glass-selected-bar {
  display: flex;
  align-items: center;
  padding: 18rpx 26rpx;
  border-top: 1rpx solid rgba(243, 244, 246, 0.6);
  gap: 18rpx;
  backdrop-filter: blur(8px);
}

.sticker-selected-scroll,
.tag-selected-scroll {
  flex: 1;
  white-space: nowrap;
}

.sticker-selected-list,
.tag-selected-list {
  display: inline-flex;
  gap: 14rpx;
}

.glass-selected-chip {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 70rpx;
  height: 70rpx;
  background: rgba(249, 250, 251, 0.8);
  border-radius: 16rpx;
  backdrop-filter: blur(6px);
  border: 1rpx solid rgba(209, 213, 219, 0.2);
}

.sticker-selected-emoji,
.tag-selected-text {
  font-size: 38rpx;
}

.tag-selected-text {
  font-size: 25rpx;
  color: var(--pt-primary, #ff6a3d);
  font-weight: 600;
  padding: 8rpx 22rpx;
}

.tag-selected-item {
  padding: 8rpx 22rpx;
  border-radius: 26rpx;
  background: linear-gradient(135deg, rgba(255, 245, 240, 0.9) 0%, rgba(255, 250, 247, 0.85) 100%);
  border: 1rpx solid rgba(255, 122, 61, 0.2);
}

.glass-mini-remove {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 30rpx;
  height: 30rpx;
  background: rgba(255, 77, 79, 0.85);
  backdrop-filter: blur(8px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx rgba(255, 59, 48, 0.3);
  transition: all 0.25s ease;

  &:active {
    transform: scale(0.9);
  }
}

.sticker-remove-icon,
.tag-remove-icon {
  font-size: 18rpx;
  color: #fff;
  font-weight: 700;
}

.glass-confirm-btn {
  flex-shrink: 0;
  padding: 14rpx 36rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  color: #fff;
  font-size: 27rpx;
  font-weight: 700;
  border-radius: 28rpx;
  letter-spacing: 1rpx;
  box-shadow:
    0 6rpx 18rpx rgba(255, 106, 61, 0.3),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.3);
  transition: all 0.28s ease;

  &:active {
    transform: scale(0.95);
  }
}

/* ====== 气泡编辑器 ====== */
.glass-bubble-preview {
  padding: 28rpx 36rpx;
  border-radius: 24rpx;
  margin-bottom: 26rpx;
  min-height: 90rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 4rpx 16rpx rgba(0, 0, 0, 0.08),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(8px);
  border: 1rpx solid rgba(255, 255, 255, 0.3);
}

.bubble-preview-text {
  font-size: 32rpx;
  font-weight: 700;
  text-align: center;
}

.glass-bubble-input {
  width: 100%;
  height: 130rpx;
  background: rgba(249, 250, 251, 0.7);
  border-radius: 18rpx;
  padding: 22rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  margin-bottom: 24rpx;
  backdrop-filter: blur(8px);
  border: 1.5rpx solid rgba(209, 213, 219, 0.3);
  transition: all 0.28s ease;

  &:focus {
    border-color: var(--pt-primary, #ff6a3d);
    background: rgba(255, 255, 255, 0.88);
  }
}

.bubble-color-row {
  display: flex;
  align-items: center;
  margin-bottom: 28rpx;
}

.bubble-color-label {
  font-size: 27rpx;
  color: var(--pt-secondary, #6b7280);
  margin-right: 22rpx;
  font-weight: 600;
}

.bubble-color-options {
  display: flex;
  gap: 18rpx;
  flex-wrap: wrap;
}

.glass-color-dot {
  width: 58rpx;
  height: 58rpx;
  border-radius: 50%;
  border: 4rpx solid transparent;
  transition: all 0.28s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

  &.active {
    border-color: var(--pt-text, #111827);
    transform: scale(1.2);
    box-shadow:
      0 6rpx 20rpx rgba(0, 0, 0, 0.2),
      0 0 0 4rpx rgba(255, 255, 255, 0.5);
  }

  &:active {
    transform: scale(0.93);
  }
}

.bubble-actions {
  display: flex;
  gap: 20rpx;
  justify-content: flex-end;
}

.glass-btn-secondary-small {
  padding: 16rpx 36rpx;
  font-size: 26rpx;
  color: var(--pt-secondary, #6b7280);
  background: rgba(249, 250, 251, 0.9);
  border-radius: 28rpx;
  font-weight: 600;
  backdrop-filter: blur(6px);
  border: 1rpx solid rgba(209, 213, 219, 0.3);
  transition: all 0.28s ease;

  &:active {
    transform: scale(0.95);
    background: rgba(233, 234, 236, 0.95);
  }
}

.glass-btn-primary-small {
  padding: 16rpx 36rpx;
  font-size: 26rpx;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  border-radius: 28rpx;
  letter-spacing: 1rpx;
  box-shadow:
    0 6rpx 18rpx rgba(255, 106, 61, 0.3),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.3);
  transition: all 0.28s ease;

  &:active {
    transform: scale(0.95);
  }
}

/* ====== 标签相关 ====== */
.glass-tag-chip {
  display: flex;
  align-items: center;
  padding: 10rpx 24rpx;
  border-radius: 26rpx;
  background: linear-gradient(135deg, rgba(255, 245, 240, 0.9) 0%, rgba(255, 250, 247, 0.85) 100%);
  backdrop-filter: blur(6px);
  border: 1rpx solid rgba(255, 122, 61, 0.2);
  transition: all 0.28s ease;

  &:active {
    transform: scale(0.95);
  }
}

.selected-tag-text {
  font-size: 25rpx;
  color: var(--pt-primary, #ff6a3d);
  font-weight: 600;
}

.selected-tag-remove {
  font-size: 22rpx;
  color: var(--pt-primary, #ff6a3d);
  margin-left: 10rpx;
  font-weight: 700;
}

/* ====== 搜索框 ====== */
.glass-search-box {
  padding: 18rpx 26rpx;
  backdrop-filter: blur(8px);
}

.glass-search-input {
  width: 100%;
  height: 72rpx;
  background: rgba(249, 250, 251, 0.8);
  border-radius: 36rpx;
  padding: 0 28rpx;
  font-size: 27rpx;
  backdrop-filter: blur(8px);
  border: 1.5rpx solid rgba(209, 213, 219, 0.3);
  transition: all 0.28s ease;

  &:focus {
    border-color: var(--pt-primary, #ff6a3d);
    background: rgba(255, 255, 255, 0.95);
    box-shadow: 0 0 0 5rpx rgba(255, 106, 61, 0.1);
  }
}

.glass-tag-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  padding: 10rpx 26rpx;
  max-height: 420rpx;
  overflow-y: auto;
}

.glass-tag-chip-selectable {
  display: flex;
  align-items: center;
  padding: 14rpx 26rpx;
  border-radius: 26rpx;
  background: rgba(249, 250, 251, 0.8);
  backdrop-filter: blur(6px);
  border: 1.5rpx solid transparent;
  transition: all 0.28s cubic-bezier(0.4, 0, 0.2, 1);

  &.active {
    background: linear-gradient(135deg, rgba(255, 245, 240, 0.9) 0%, rgba(255, 250, 247, 0.85) 100%);
    border-color: var(--pt-primary, #ff6a3d);
    box-shadow: 0 4rpx 14rpx rgba(255, 106, 61, 0.12);
    transform: scale(1.03);
  }

  &:active {
    transform: scale(0.96);
  }
}

.tag-chip-text {
  font-size: 25rpx;
  color: var(--pt-text, #374151);
  font-weight: 600;

  .glass-tag-chip-selectable.active & {
    color: var(--pt-primary, #ff6a3d);
  }
}

.tag-chip-count {
  font-size: 21rpx;
  color: var(--pt-muted, #9ca3af);
  margin-left: 10rpx;
  font-weight: 500;
}

/* ====== 暗色模式适配 ====== */
page.dark .glass-status-bar,
page.dark .glass-nav-bar {
  background: linear-gradient(180deg, rgba(80, 80, 110, 0.95) 0%, rgba(100, 60, 90, 0.95) 100%);
}

page.dark .glass-scroll-container {
  background: linear-gradient(180deg, rgba(40, 40, 55, 0.98) 0%, rgba(30, 30, 42, 0.99) 100%);
}

page.dark .glass-card {
  background: rgba(40, 40, 55, 0.84);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 6rpx 24rpx rgba(0, 0, 0, 0.3),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.1);
}

page.dark .glass-selector,
.page.dark .glass-textarea,
.page.dark .glass-add-btn,
.page.dark .glass-upload-area {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.1);
}

page.dark .glass-textarea:focus {
  border-color: var(--pt-primary, #ff6a3d);
  background: rgba(255, 255, 255, 0.08);
}

page.dark .glass-bottom-bar {
  background: rgba(30, 30, 42, 0.92);
  border-top-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 -8rpx 32rpx rgba(0, 0, 0, 0.4),
    0 -2rpx 8rpx rgba(0, 0, 0, 0.15);
}

page.dark .glass-btn-cancel {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.1);
}

page.dark .glass-btn-cancel .text {
  color: #aaaaaa;
}

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

page.dark .glass-list-item {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.08);

  &.selected {
    background: linear-gradient(135deg, rgba(255, 120, 80, 0.2) 0%, rgba(255, 100, 80, 0.15) 100%);
    border-color: rgba(255, 122, 61, 0.3);
  }
}

page.dark .pet-item-name,
page.dark .challenge-item-name {
  color: #e5e5e5;
}

page.dark .glass-option-item {
  border-bottom-color: rgba(255, 255, 255, 0.06);

  &:active {
    background: rgba(255, 106, 61, 0.08);
  }
}

page.dark .option-text {
  color: #e5e5e5;
}

page.dark .glass-tab-chip {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.1);

  &.active {
    background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  }
}

page.dark .sticker-cat-text {
  color: #aaaaaa;

  .glass-tab-chip.active & {
    color: #fff;
  }
}

page.dark .glass-sticker-cell {
  background: rgba(255, 255, 255, 0.04);

  &.selected {
    background: linear-gradient(135deg, rgba(255, 120, 80, 0.2) 0%, rgba(255, 100, 80, 0.15) 100%);
    border-color: rgba(255, 122, 61, 0.4);
  }
}

page.dark .glass-selected-chip,
page.dark .glass-tag-chip,
page-dark .tag-selected-item {
  background: rgba(255, 120, 80, 0.15);
  border-color: rgba(255, 122, 61, 0.25);
}

page.dark .glass-bubble-preview {
  border-color: rgba(255, 255, 255, 0.1);
}

page.dark .glass-bubble-input,
.page.dark .glass-search-input {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.1);
}

page.dark .glass-bubble-input:focus,
.page.dark .glass-search-input:focus {
  background: rgba(255, 255, 255, 0.08);
}

page.dark .glass-tag-chip-selectable {
  background: rgba(255, 255, 255, 0.04);

  &.active {
    background: linear-gradient(135deg, rgba(255, 120, 80, 0.2) 0%, rgba(255, 100, 80, 0.15) 100%);
    border-color: rgba(255, 122, 61, 0.35);
  }
}

page.dark .tag-chip-text {
  color: #e5e5e5;

  .glass-tag-chip-selectable.active & {
    color: #ff9966;
  }
}
</style>
