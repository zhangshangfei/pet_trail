<template>
  <view class="report-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack"><view class="nav-back-arrow"></view></view>
        <text class="nav-title">举报记录</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="page-content">
        <view v-if="reportList.length" class="report-list">
          <view
            v-for="item in reportList"
            :key="item.id"
            class="report-card"
            @tap="toggleExpand(item.id)"
          >
            <view class="report-header">
              <view class="report-type-tag" :class="'type-' + item.targetType">
                <text class="type-tag-text">{{ getTypeLabel(item.targetType) }}</text>
              </view>
              <view class="report-status" :class="'status-' + item.status">
                <text class="status-text">{{ getStatusLabel(item.status) }}</text>
              </view>
            </view>

            <view class="report-body">
              <view class="report-reason-row">
                <text class="report-reason-label">举报原因：</text>
                <text class="report-reason">{{ getReasonLabel(item.reason) }}</text>
              </view>
              <text v-if="item.description" class="report-desc">{{ item.description }}</text>
            </view>

            <view class="report-meta">
              <text class="report-time">{{ formatTime(item.createdAt) }}</text>
            </view>

            <view v-if="expandedId === item.id && item.result" class="report-result">
              <view class="result-header">
                <text class="result-icon">📋</text>
                <text class="result-label">处理结果</text>
              </view>
              <text class="result-content">{{ item.result }}</text>
            </view>

            <view class="expand-hint">
              <text class="expand-text">{{ expandedId === item.id ? '收起' : '展开详情' }}</text>
              <text class="expand-arrow" :class="{ rotated: expandedId === item.id }">›</text>
            </view>
          </view>
        </view>

        <view v-else class="empty-state">
          <text class="empty-emoji">🛡️</text>
          <text class="empty-text">暂无举报记录</text>
          <text class="empty-hint">您的举报记录会在此显示</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { checkLogin } from '@/utils/index'
import * as reportApi from '@/api/report'

const REASON_MAP = {
  spam: '垃圾广告',
  porn: '色情低俗',
  fake: '虚假信息',
  illegal: '违法违规',
  abuse: '人身攻击',
  other: '其他'
}

const TYPE_MAP = {
  user: '举报用户',
  post: '举报动态',
  comment: '举报评论'
}

export default {
  data() {
    return {
      statusBarHeight: 20,
      reportList: [],
      expandedId: null
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
  onShow() {
    this.loadReportList()
  },
  methods: {
    goBack() {
      uni.navigateBack({ delta: 1 })
    },

    async loadReportList() {
      const loggedIn = await checkLogin('')
      if (!loggedIn) {
        this.reportList = []
        return
      }
      try {
        const res = await reportApi.getMyReports()
        if (res && res.success && Array.isArray(res.data)) {
          this.reportList = res.data
        } else {
          this.reportList = []
        }
      } catch (e) {
        console.error('加载举报记录失败:', e)
        this.reportList = []
      }
    },

    toggleExpand(id) {
      this.expandedId = this.expandedId === id ? null : id
    },

    getTypeLabel(type) {
      return TYPE_MAP[type] || '举报'
    },

    getReasonLabel(reason) {
      return REASON_MAP[reason] || reason || '其他'
    },

    getStatusLabel(status) {
      const map = { 0: '待处理', 1: '处理中', 2: '已处理', 3: '已驳回' }
      return map[status] || '待处理'
    },

    formatTime(dateStr) {
      if (!dateStr) return ''
      const d = new Date(typeof dateStr === 'string' ? dateStr.replace(/-/g, '/') : dateStr)
      if (Number.isNaN(d.getTime())) return ''
      const now = new Date()
      const diff = now - d
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
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

.report-page { min-height: 100vh; background: $bg; }
.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%); }
.status-bar { width: 100%; }
.nav-bar { height: 92rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 28rpx; }
.nav-back { width: 64rpx; height: 64rpx; border-radius: 32rpx; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; }
.nav-back:active { background: rgba(255,255,255,0.35); }
.nav-back-arrow { width: 18rpx; height: 18rpx; border-left: 4rpx solid #fff; border-bottom: 4rpx solid #fff; transform: rotate(45deg) translate(2rpx, -2rpx); }
.nav-title { font-size: 34rpx; font-weight: 700; color: #fff; }
.nav-placeholder { width: 64rpx; }
.page-scroll { height: 100vh; }
.page-content { padding: 20rpx 24rpx 60rpx; }

.report-list { display: flex; flex-direction: column; gap: 20rpx; }

.report-card {
  background: $card-bg; border-radius: 24rpx; padding: 28rpx 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.05);
}

.report-header {
  display: flex; flex-direction: row; align-items: center; justify-content: space-between;
  margin-bottom: 16rpx;
}

.report-type-tag {
  padding: 6rpx 18rpx; border-radius: 20rpx;
}
.type-user { background: #fef3c7; }
.type-post { background: #dbeafe; }
.type-comment { background: #f3e8ff; }
.type-tag-text { font-size: 22rpx; font-weight: 500; }
.type-user .type-tag-text { color: #d97706; }
.type-post .type-tag-text { color: #2563eb; }
.type-comment .type-tag-text { color: #7c3aed; }

.report-status { padding: 6rpx 18rpx; border-radius: 20rpx; }
.status-0 { background: #fff4e6; }
.status-1 { background: #dbeafe; }
.status-2 { background: #d1fae5; }
.status-3 { background: #f3f4f6; }
.status-text { font-size: 22rpx; font-weight: 500; }
.status-0 .status-text { color: #ff7a3d; }
.status-1 .status-text { color: #2563eb; }
.status-2 .status-text { color: #047857; }
.status-3 .status-text { color: #6b7280; }

.report-body { margin-bottom: 12rpx; }
.report-reason-row { display: flex; flex-direction: row; align-items: center; margin-bottom: 8rpx; }
.report-reason-label { font-size: 26rpx; color: $text-light; }
.report-reason { font-size: 28rpx; color: $text-primary; font-weight: 600; }
.report-desc { font-size: 26rpx; color: $text-secondary; line-height: 1.6; }

.report-meta { margin-bottom: 8rpx; }
.report-time { font-size: 22rpx; color: $text-light; }

.report-result {
  background: #f0fdf4; border-radius: 16rpx; padding: 20rpx; margin-top: 16rpx;
  border: 1rpx solid #bbf7d0;
}
.result-header { display: flex; flex-direction: row; align-items: center; margin-bottom: 10rpx; }
.result-icon { font-size: 24rpx; margin-right: 8rpx; }
.result-label { font-size: 24rpx; font-weight: 600; color: #047857; }
.result-content { font-size: 26rpx; color: $text-primary; line-height: 1.6; }

.expand-hint {
  display: flex; flex-direction: row; align-items: center; justify-content: center;
  margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #f3f4f6;
}
.expand-text { font-size: 24rpx; color: $primary; font-weight: 500; }
.expand-arrow { font-size: 28rpx; color: $primary; margin-left: 6rpx; transition: transform 0.2s; }
.expand-arrow.rotated { transform: rotate(90deg); }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 120rpx 40rpx; }
.empty-emoji { font-size: 80rpx; margin-bottom: 24rpx; }
.empty-text { font-size: 30rpx; font-weight: 600; color: #6b7280; margin-bottom: 12rpx; }
.empty-hint { font-size: 24rpx; color: #9ca3af; }
</style>
