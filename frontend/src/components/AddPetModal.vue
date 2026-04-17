<template>
  <view class="apm-mask" @tap="emitClose" @touchmove.stop.prevent>
    <view class="apm-sheet" @tap.stop>
      <view class="apm-header">
        <text class="apm-title">{{ localForm.id ? '编辑宠物' : '添加宠物' }}</text>
        <view class="apm-close" @tap="emitClose">
          <text class="apm-close-icon">✕</text>
        </view>
      </view>

      <scroll-view class="apm-body" scroll-y="true" @tap.stop>
        <view class="apm-avatar-section" @tap="openCameraModal">
          <view class="apm-avatar-wrap">
            <view v-if="!localForm.avatar" class="apm-avatar-empty">
              <text class="apm-avatar-placeholder">宠</text>
            </view>
            <image v-else class="apm-avatar-img" :src="localForm.avatar" mode="aspectFill" />
          </view>
          <view class="apm-avatar-badge">
            <text class="apm-avatar-badge-icon">📷</text>
          </view>
        </view>

        <view class="apm-form">
          <view class="apm-field">
            <text class="apm-label">宠物昵称 <text class="apm-required">*</text></text>
            <input class="apm-input" v-model="localForm.name" placeholder="给宠物起个名字吧" />
          </view>

          <view class="apm-field">
            <text class="apm-label">宠物类别</text>
            <picker :range="categoryOptions" range-key="label" :value="categoryIndex" @change="onCategoryPick">
              <view class="apm-select">
                <text :class="categoryIndex > 0 ? 'apm-select-val' : 'apm-placeholder'">
                  {{ categoryIndex >= 0 ? categoryOptions[categoryIndex].label : '请选择' }}
                </text>
                <text class="apm-chevron">›</text>
              </view>
            </picker>
          </view>

          <view class="apm-field">
            <text class="apm-label">宠物品种</text>
            <input class="apm-input" v-model="localForm.breed" placeholder="如：英短蓝猫" />
          </view>

          <view class="apm-field">
            <text class="apm-label">性别</text>
            <picker :range="genderOptions" range-key="label" :value="genderIndex" @change="onGenderPick">
              <view class="apm-select">
                <text :class="genderIndex > 0 ? 'apm-select-val' : 'apm-placeholder'">
                  {{ genderIndex >= 0 ? genderOptions[genderIndex].label : '请选择' }}
                </text>
                <text class="apm-chevron">›</text>
              </view>
            </picker>
          </view>

          <view class="apm-field">
            <text class="apm-label">生日</text>
            <picker mode="date" :value="localForm.birthday" @change="onBirthdayPick">
              <view class="apm-select">
                <text :class="localForm.birthday ? 'apm-select-val' : 'apm-placeholder'">
                  {{ localForm.birthday || '请选择' }}
                </text>
                <text class="apm-chevron">›</text>
              </view>
            </picker>
          </view>

          <view class="apm-field">
            <text class="apm-label">体重 (kg)</text>
            <input class="apm-input" type="digit" v-model="localForm.weight" placeholder="0.00" />
          </view>

          <view class="apm-field">
            <text class="apm-label">毛色</text>
            <input class="apm-input" v-model="localForm.color" placeholder="如：橘色、黑白" />
          </view>

          <view class="apm-field">
            <text class="apm-label">是否绝育</text>
            <picker :range="sterilizedOptions" range-key="label" :value="sterilizedIndex" @change="onSterilizedPick">
              <view class="apm-select">
                <text :class="sterilizedIndex > 0 ? 'apm-select-val' : 'apm-placeholder'">
                  {{ sterilizedIndex >= 0 ? sterilizedOptions[sterilizedIndex].label : '请选择' }}
                </text>
                <text class="apm-chevron">›</text>
              </view>
            </picker>
          </view>
        </view>

        <view class="apm-actions">
          <button class="apm-btn apm-btn-cancel" @tap="emitClose">取消</button>
          <button class="apm-btn apm-btn-save" @tap="emitSave">保存</button>
        </view>
      </scroll-view>
    </view>

    <view v-if="showCameraModal" class="apm-camera-mask" @tap="onCameraMaskTap" @touchmove.stop.prevent>
      <view class="apm-camera-sheet" @tap.stop>
        <view class="apm-camera-head">
          <text class="apm-camera-title">选择头像</text>
          <text class="apm-camera-close" @tap="closeCameraModal">✕</text>
        </view>
        <view class="apm-camera-list">
          <view class="apm-camera-item" @tap="chooseFromGallery">
            <text class="apm-camera-item-icon">🖼️</text>
            <text class="apm-camera-item-text">从相册选择</text>
          </view>
          <view class="apm-camera-item" @tap="takePhoto">
            <text class="apm-camera-item-icon">📷</text>
            <text class="apm-camera-item-text">拍照</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import api from '@/api'

export default {
  name: "AddPetModal",
  props: {
    initialForm: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      showCameraModal: false,
      uploading: false,
      localForm: this.normalizeForm(this.initialForm)
    };
  },
  computed: {
    genderOptions() {
      return [
        { label: "请选择", value: -1 },
        { label: "♂ 公", value: 1 },
        { label: "♀ 母", value: 2 },
        { label: "未知", value: 0 }
      ];
    },
    categoryOptions() {
      return [
        { label: "请选择", value: -1 },
        { label: "🐱 猫咪", value: 1 },
        { label: "🐶 狗狗", value: 2 },
        { label: "🐾 其他", value: 0 }
      ];
    },
    sterilizedOptions() {
      return [
        { label: "请选择", value: -1 },
        { label: "已绝育", value: 1 },
        { label: "未绝育", value: 0 }
      ];
    },
    genderIndex() {
      const idx = this.genderOptions.findIndex((x) => x.value === this.localForm.gender);
      return idx === -1 ? 0 : idx;
    },
    categoryIndex() {
      const idx = this.categoryOptions.findIndex((x) => x.value === this.localForm.category);
      return idx === -1 ? 0 : idx;
    },
    sterilizedIndex() {
      const idx = this.sterilizedOptions.findIndex((x) => x.value === this.localForm.sterilized);
      return idx === -1 ? 0 : idx;
    }
  },
  watch: {
    initialForm: {
      deep: true,
      handler(v) {
        this.localForm = this.normalizeForm(v);
      }
    }
  },
  methods: {
    emitClose() {
      this.$emit("close");
    },
    normalizeForm(v) {
      const src = v || {};
      return {
        id: src.id || null,
        name: src.name || "",
        breed: src.breed || "",
        gender: typeof src.gender === "number" ? src.gender : 0,
        birthday: src.birthday || "",
        weight: src.weight || "",
        color: src.color || "",
        avatar: src.avatar || "",
        category: typeof src.category === "number" ? src.category : 0,
        sterilized: typeof src.sterilized === "number" ? src.sterilized : 0
      };
    },
    emitSave() {
      if (!this.localForm.name.trim()) {
        uni.showToast({ title: "请输入宠物昵称", icon: "none" });
        return;
      }
      this.$emit("save", { ...this.localForm });
    },
    openCameraModal() {
      this.showCameraModal = true;
    },
    closeCameraModal() {
      this.showCameraModal = false;
    },
    onCameraMaskTap(e) {
      if (e && e.target && e.currentTarget && e.target !== e.currentTarget) return;
      this.closeCameraModal();
    },
    onGenderPick(e) {
      const idx = Number(e.detail.value);
      const opt = this.genderOptions[idx];
      if (!opt || opt.value === -1) return;
      this.localForm.gender = opt.value;
    },
    onBirthdayPick(e) {
      this.localForm.birthday = e.detail.value;
    },
    onCategoryPick(e) {
      const idx = Number(e.detail.value);
      const opt = this.categoryOptions[idx];
      if (!opt || opt.value === -1) return;
      this.localForm.category = opt.value;
    },
    onSterilizedPick(e) {
      const idx = Number(e.detail.value);
      const opt = this.sterilizedOptions[idx];
      if (!opt || opt.value === -1) return;
      this.localForm.sterilized = opt.value;
    },
    chooseFromGallery() {
      uni.chooseImage({
        count: 1,
        sourceType: ["album"],
        success: (res) => {
          const file = res.tempFilePaths && res.tempFilePaths[0];
          if (file) this.uploadImageToServer(file);
          this.showCameraModal = false;
        },
        fail: () => { this.showCameraModal = false; }
      });
    },
    takePhoto() {
      uni.chooseImage({
        count: 1,
        sourceType: ["camera"],
        success: (res) => {
          const file = res.tempFilePaths && res.tempFilePaths[0];
          if (file) this.uploadImageToServer(file);
          this.showCameraModal = false;
        },
        fail: () => { this.showCameraModal = false; }
      });
    },
    async uploadImageToServer(filePath) {
      this.uploading = true;
      uni.showLoading({ title: '上传中...' });
      try {
        const res = await api.pet.uploadImage(filePath);
        if (res.success && res.data && res.data.url) {
          this.localForm.avatar = res.data.url;
          uni.showToast({ title: '上传成功', icon: 'success' });
        } else {
          throw new Error(res.message || '上传失败');
        }
      } catch (error) {
        uni.showToast({ title: error.message || '上传失败', icon: 'none' });
      } finally {
        this.uploading = false;
        uni.hideLoading();
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.apm-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 20000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.apm-sheet {
  width: 100%;
  max-height: 85vh;
  background: #fff;
  border-top-left-radius: 36rpx;
  border-top-right-radius: 36rpx;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.apm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 36rpx 16rpx;
}

.apm-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #111827;
}

.apm-close {
  width: 56rpx;
  height: 56rpx;
  border-radius: 28rpx;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.apm-close-icon {
  font-size: 26rpx;
  color: #6b7280;
  font-weight: 700;
}

.apm-body {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.apm-avatar-section {
  display: flex;
  justify-content: center;
  padding: 16rpx 0 28rpx;
  position: relative;
}

.apm-avatar-wrap {
  width: 180rpx;
  height: 180rpx;
  border-radius: 90rpx;
  border: 6rpx solid #fff;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e5e7eb;
}

.apm-avatar-empty {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
}

.apm-avatar-placeholder {
  font-size: 72rpx;
  color: #fff;
  font-weight: 700;
}

.apm-avatar-img {
  width: 100%;
  height: 100%;
}

.apm-avatar-badge {
  position: absolute;
  bottom: 24rpx;
  right: calc(50% - 100rpx);
  width: 52rpx;
  height: 52rpx;
  border-radius: 26rpx;
  background: #ff7a3d;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid #fff;
  box-shadow: 0 4rpx 12rpx rgba(255, 122, 61, 0.4);
}

.apm-avatar-badge-icon {
  font-size: 24rpx;
}

.apm-form {
  background: #fff;
  border-radius: 24rpx;
  margin: 0 36rpx;
  padding: 0 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.apm-field {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f3f4f6;
}

.apm-field:last-child {
  border-bottom: none;
}

.apm-label {
  font-size: 28rpx;
  color: #374151;
  font-weight: 600;
  flex-shrink: 0;
  width: 160rpx;
}

.apm-required {
  color: #ff4d4f;
}

.apm-input {
  flex: 1;
  text-align: right;
  font-size: 28rpx;
  color: #111827;
  min-width: 0;
}

.apm-select {
  flex: 1;
  text-align: right;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.apm-select-val {
  font-size: 28rpx;
  color: #111827;
}

.apm-placeholder {
  color: #9ca3af;
  font-size: 28rpx;
}

.apm-chevron {
  color: #d1d5db;
  font-size: 28rpx;
  font-weight: 700;
  margin-left: 8rpx;
}

.apm-actions {
  display: flex;
  gap: 20rpx;
  margin: 32rpx 36rpx 0;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.apm-btn {
  flex: 1;
  border-radius: 999rpx;
  padding: 22rpx 0;
  font-size: 28rpx;
  font-weight: 700;
  border: none;
  line-height: 1.4;
}

.apm-btn::after {
  border: none;
}

.apm-btn-cancel {
  background: #f3f4f6;
  color: #6b7280;
}

.apm-btn-save {
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(255, 77, 79, 0.3);
}

.apm-camera-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 20010;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.apm-camera-sheet {
  width: 100%;
  background: #fff;
  border-top-left-radius: 36rpx;
  border-top-right-radius: 36rpx;
  padding: 28rpx 28rpx 44rpx;
}

.apm-camera-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.apm-camera-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #111827;
}

.apm-camera-close {
  font-size: 36rpx;
  color: #9ca3af;
  padding: 8rpx 12rpx;
}

.apm-camera-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.apm-camera-item {
  display: flex;
  align-items: center;
  padding: 24rpx 20rpx;
  border-radius: 20rpx;
  background: #f9fafb;
}

.apm-camera-item-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.apm-camera-item-text {
  font-size: 28rpx;
  color: #111827;
  font-weight: 600;
}
</style>
