<template>
  <view class="video-card" :style="{ height: height + 'rpx' }">
    <!-- 视频播放器 -->
    <video
      :id="'video-' + videoId"
      class="video-player"
      :src="src"
      :autoplay="autoplay"
      :muted="isMuted"
      :loop="false"
      :show-play-btn="isFullScreen"
      :show-center-play-btn="isFullScreen"
      :show-fullscreen-btn="isFullScreen"
      :controls="isFullScreen"
      :enable-progress-gesture="isFullScreen"
      :enable-play-gesture="isFullScreen"
      :page-gesture="false"
      :show-loading="true"
      :object-fit="isFullScreen ? 'contain' : objectFit"
      :poster="poster"
      @play="onPlay"
      @pause="onPause"
      @ended="onEnded"
      @click="onVideoClick"
      @fullscreenchange="onFullScreenChange"
      @loadedmetadata="onLoadedMetadata"
    />

    <!-- 非全屏时的覆盖层 -->
    <template v-if="!isFullScreen">
      <!-- 静音/取消静音按钮 -->
      <view class="mute-btn" @click.stop="toggleMute">
        <text class="mute-icon">{{ isMuted ? '🔇' : '🔊' }}</text>
      </view>

      <!-- 播放按钮（未播放或已播完时显示） -->
      <view v-if="!isPlaying" class="play-overlay" @click.stop="onVideoClick">
        <view class="play-btn-wrap">
          <view class="play-triangle"></view>
        </view>
      </view>

      <!-- 视频时长显示 -->
      <view v-if="duration > 0 && !isPlaying" class="duration-tag">
        <text class="duration-text">{{ formatDuration(duration) }}</text>
      </view>
    </template>
  </view>
</template>

<script>
let videoIdCounter = 0

export default {
  name: 'VideoCard',
  props: {
    src: { type: String, required: true },
    poster: { type: String, default: '' },
    autoplay: { type: Boolean, default: false },
    muted: { type: Boolean, default: true },
    objectFit: { type: String, default: 'cover' },
    height: { type: Number, default: 420 }
  },
  data() {
    return {
      videoId: ++videoIdCounter,
      isMuted: this.muted,
      isPlaying: false,
      isFullScreen: false,
      duration: 0,
      videoContext: null
    }
  },
  mounted() {
    this.videoContext = uni.createVideoContext('video-' + this.videoId, this)
  },
  beforeUnmount() {
    if (this.videoContext && this.isFullScreen) {
      this.videoContext.exitFullScreen()
    }
    if (this.videoContext) {
      this.videoContext.pause()
    }
  },
  methods: {
    onVideoClick() {
      if (!this.videoContext) return
      if (this.isFullScreen) {
        this.videoContext.exitFullScreen()
        return
      }
      // 进入全屏播放：取消静音，显示系统控制栏
      this.isMuted = false
      this.isFullScreen = true
      this.$nextTick(() => {
        this.videoContext.requestFullScreen({ direction: 0 })
        this.videoContext.play()
      })
    },

    onFullScreenChange(e) {
      if (e.detail && e.detail.fullScreen) {
        this.isFullScreen = true
        this.isMuted = false
      } else {
        // 退出全屏：暂停视频，恢复静音
        this.isFullScreen = false
        this.isMuted = true
        if (this.videoContext) {
          this.videoContext.pause()
        }
        this.isPlaying = false
      }
    },

    toggleMute() {
      this.isMuted = !this.isMuted
    },

    onPlay() {
      this.isPlaying = true
    },

    onPause() {
      this.isPlaying = false
    },

    onEnded() {
      this.isPlaying = false
    },

    onLoadedMetadata(e) {
      if (e.detail && e.detail.duration) {
        this.duration = Math.floor(e.detail.duration)
      }
    },

    formatDuration(seconds) {
      const m = Math.floor(seconds / 60)
      const s = seconds % 60
      return `${m}:${s < 10 ? '0' : ''}${s}`
    },

    pause() {
      if (this.videoContext) {
        this.videoContext.pause()
      }
    },

    play() {
      if (this.videoContext) {
        this.videoContext.play()
      }
    }
  }
}
</script>

<style scoped>
.video-card {
  position: relative;
  width: 100%;
  border-radius: 16rpx;
  overflow: hidden;
  background: #000;
}

.video-player {
  width: 100%;
  height: 100%;
}

.mute-btn {
  position: absolute;
  right: 16rpx;
  bottom: 16rpx;
  width: 56rpx;
  height: 56rpx;
  border-radius: 28rpx;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
}

.mute-icon {
  font-size: 28rpx;
  line-height: 1;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}

.play-btn-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: 44rpx;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.2);
}

.play-triangle {
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 18rpx 0 18rpx 30rpx;
  border-color: transparent transparent transparent #333;
  margin-left: 6rpx;
}

.duration-tag {
  position: absolute;
  right: 16rpx;
  top: 16rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2;
}

.duration-text {
  color: #fff;
  font-size: 22rpx;
}
</style>
