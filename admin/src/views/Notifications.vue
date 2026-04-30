<template>
  <div class="notifications-page">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6" v-for="item in overviewCards" :key="item.key">
        <el-card shadow="hover" class="overview-card" :body-style="{ padding: '16px 20px' }">
          <div class="overview-content">
            <div class="overview-icon" :style="{ background: item.bg, color: item.color }">
              <el-icon :size="20"><component :is="item.icon" /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ item.value }}</div>
              <div class="overview-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 通知列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">通知列表</span>
            <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
          </div>
          <div class="header-actions">
            <el-select v-model="typeFilter" placeholder="通知类型" clearable style="width: 120px" @change="loadData">
              <el-option label="系统" value="system" />
              <el-option label="点赞" value="like" />
              <el-option label="评论" value="comment" />
              <el-option label="关注" value="follow" />
            </el-select>
            <el-select v-model="readFilter" placeholder="阅读状态" clearable style="width: 120px" @change="loadData">
              <el-option label="未读" :value="false" />
              <el-option label="已读" :value="true" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
            <el-button type="success" :icon="Promotion" @click="showSendDialog">发送通知</el-button>
            <el-button type="warning" :icon="Bell" @click="showBroadcastDialog" v-if="canSend">广播通知</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="接收用户" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :src="row.userAvatar" icon="UserFilled" />
              <div class="user-info">
                <div class="user-name">{{ row.userNickname || '未知用户' }}</div>
                <div class="user-meta">
                  <span class="user-id">ID: {{ row.userId }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="触发用户" min-width="160">
          <template #default="{ row }">
            <div v-if="row.fromUserId" class="user-cell">
              <el-avatar :size="32" :src="row.fromUserAvatar" icon="UserFilled" />
              <div class="user-info">
                <div class="user-name">{{ row.fromUserNickname || '未知用户' }}</div>
                <div class="user-meta">
                  <span class="user-id">ID: {{ row.fromUserId }}</span>
                </div>
              </div>
            </div>
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.type] || ''" effect="light" size="small">
              {{ { like: '点赞', comment: '评论', follow: '关注', favorite: '收藏', system: '系统' }[row.type] || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="已读" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'info' : 'danger'" effect="light" size="small">
              <el-icon :size="10"><component :is="row.isRead ? Check : Close" /></el-icon>
              {{ row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="通知时间" width="170">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon :size="12"><Clock /></el-icon>
              <span>{{ row.createdAt }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" text type="primary" :icon="View" @click="viewDetail(row)">详情</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 通知详情弹窗 -->
    <el-dialog v-model="showDetail" title="通知详情" width="640px" destroy-on-close class="notification-detail-dialog">
      <div v-if="detailNotification" class="nd-header">
        <div class="nd-avatar">
          <el-avatar :size="56" :src="detailNotification.userAvatar" icon="UserFilled" />
        </div>
        <div class="nd-header-info">
          <div class="nd-name">{{ detailNotification.userNickname || '未知用户' }}</div>
          <div class="nd-tags">
            <el-tag :type="typeTagMap[detailNotification.type] || ''" size="small" effect="light" round>
              {{ { like: '点赞', comment: '评论', follow: '关注', favorite: '收藏', system: '系统' }[detailNotification.type] || detailNotification.type }}
            </el-tag>
            <el-tag :type="detailNotification.isRead ? 'info' : 'danger'" size="small" effect="light" round>
              {{ detailNotification.isRead ? '已读' : '未读' }}
            </el-tag>
            <span class="nd-tag-item">
              <el-icon :size="12"><Clock /></el-icon>
              {{ detailNotification.createdAt }}
            </span>
          </div>
        </div>
      </div>

      <div class="nd-body">
        <div class="nd-section">
          <div class="nd-section-title">通知内容</div>
          <div class="nd-content">{{ detailNotification.content }}</div>
        </div>

        <div class="nd-section">
          <div class="nd-section-title">数据概览</div>
          <div class="nd-grid">
            <div class="nd-cell">
              <span class="nd-cell-label">通知ID</span>
              <span class="nd-cell-value">{{ detailNotification.id }}</span>
            </div>
            <div class="nd-cell">
              <span class="nd-cell-label">接收用户ID</span>
              <span class="nd-cell-value">{{ detailNotification.userId }}</span>
            </div>
            <div class="nd-cell">
              <span class="nd-cell-label">触发用户ID</span>
              <span class="nd-cell-value">{{ detailNotification.fromUserId || '-' }}</span>
            </div>
            <div class="nd-cell">
              <span class="nd-cell-label">关联业务ID</span>
              <span class="nd-cell-value">{{ detailNotification.bizId || '-' }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 发送通知弹窗 -->
    <el-dialog v-model="sendDialogVisible" title="发送系统通知" width="500px" destroy-on-close>
      <el-form :model="sendForm" label-width="100px">
        <el-form-item label="用户ID" required>
          <el-input v-model="sendForm.userId" placeholder="输入接收通知的用户ID" />
        </el-form-item>
        <el-form-item label="通知标题">
          <el-input v-model="sendForm.title" placeholder="通知标题（可选）" />
        </el-form-item>
        <el-form-item label="通知内容" required>
          <el-input v-model="sendForm.content" type="textarea" :rows="4" placeholder="输入通知内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="sending" @click="handleSend">发送</el-button>
      </template>
    </el-dialog>

    <!-- 广播通知弹窗 -->
    <el-dialog v-model="broadcastDialogVisible" title="广播通知" width="500px" destroy-on-close>
      <el-form :model="broadcastForm" label-width="100px">
        <el-form-item label="通知标题">
          <el-input v-model="broadcastForm.title" placeholder="广播标题（可选）" />
        </el-form-item>
        <el-form-item label="通知内容" required>
          <el-input v-model="broadcastForm.content" type="textarea" :rows="4" placeholder="输入广播内容（发送给所有用户）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="broadcastDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="broadcasting" @click="handleBroadcast">广播</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getNotificationList, sendNotification, broadcastNotification } from '../api/admin'
import { useAdminStore } from '@/store/admin'
import { Search, View, Clock, Check, Close, Promotion, Bell } from '@element-plus/icons-vue'

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const typeFilter = ref('')
const readFilter = ref(null)
const sendDialogVisible = ref(false)
const broadcastDialogVisible = ref(false)
const sending = ref(false)
const broadcasting = ref(false)
const showDetail = ref(false)
const detailNotification = ref(null)

const sendForm = reactive({ userId: '', content: '', title: '' })
const broadcastForm = reactive({ content: '', title: '' })

const adminStore = useAdminStore()
const canSend = computed(() => adminStore.hasButton('notification:send'))

const typeTagMap = { like: 'danger', comment: 'primary', follow: 'success', favorite: 'warning', system: 'info' }

// 顶部概览数据
const overviewCards = computed(() => [
  { key: 'total', label: '总通知数', value: total.value, icon: 'Bell', color: '#409eff', bg: '#ecf5ff' },
  { key: 'unread', label: '未读通知', value: tableData.value.filter(n => !n.isRead).length, icon: 'Message', color: '#f56c6c', bg: '#fef0f0' },
  { key: 'read', label: '已读通知', value: tableData.value.filter(n => n.isRead).length, icon: 'Check', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'system', label: '系统通知', value: tableData.value.filter(n => n.type === 'system').length, icon: 'Monitor', color: '#909399', bg: '#f4f4f5' }
])

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (typeFilter.value) params.type = typeFilter.value
    if (readFilter.value !== null) params.isRead = readFilter.value
    const res = await getNotificationList(params)
    if (res.data) { tableData.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch (e) {} finally { loading.value = false }
}

const viewDetail = (row) => {
  detailNotification.value = { ...row }
  showDetail.value = true
}

const showSendDialog = () => { sendForm.userId = ''; sendForm.content = ''; sendForm.title = ''; sendDialogVisible.value = true }
const showBroadcastDialog = () => { broadcastForm.content = ''; broadcastForm.title = ''; broadcastDialogVisible.value = true }

const handleSend = async () => {
  if (!sendForm.userId || !sendForm.content) { ElMessage.warning('请填写完整'); return }
  sending.value = true
  try {
    await sendNotification({ userId: Number(sendForm.userId), content: sendForm.content, title: sendForm.title || undefined })
    ElMessage.success('发送成功')
    sendDialogVisible.value = false
    loadData()
  } catch (e) {} finally { sending.value = false }
}

const handleBroadcast = async () => {
  if (!broadcastForm.content) { ElMessage.warning('请填写内容'); return }
  broadcasting.value = true
  try {
    const res = await broadcastNotification({ content: broadcastForm.content, title: broadcastForm.title || undefined })
    const count = res.data?.sentCount || 0
    ElMessage.success(`广播成功，已发送给 ${count} 位用户`)
    broadcastDialogVisible.value = false
  } catch (e) {} finally { broadcasting.value = false }
}

onMounted(loadData)
</script>

<style scoped>
.notifications-page { padding: 0; }

/* 顶部统计卡片 */
.stats-row { margin-bottom: 16px; }
.overview-card { transition: transform 0.2s; }
.overview-card:hover { transform: translateY(-2px); }
.overview-content { display: flex; align-items: center; gap: 14px; }
.overview-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.overview-info { flex: 1; min-width: 0; }
.overview-value { font-size: 22px; font-weight: 700; color: #303133; line-height: 1.2; }
.overview-label { font-size: 12px; color: #909399; margin-top: 2px; }

/* 表格卡片 */
.table-card { min-height: 500px; }
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.header-left { display: flex; align-items: center; gap: 8px; }
.card-title { font-weight: 600; font-size: 15px; color: #303133; }
.header-actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }

/* 用户单元格 */
.user-cell { display: flex; align-items: center; gap: 12px; }
.user-info { display: flex; flex-direction: column; gap: 4px; }
.user-name { font-size: 14px; font-weight: 500; color: #303133; }
.user-meta { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #909399; }

/* 时间单元格 */
.time-cell { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #606266; }

/* 操作按钮 */
.action-btns {
  display: flex;
  justify-content: center;
  gap: 4px;
  flex-wrap: nowrap;
}

/* 分页 */
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }

/* ========== 详情弹窗 ========== */
.notification-detail-dialog :deep(.el-dialog__body) { padding: 0; }
.notification-detail-dialog :deep(.el-dialog__header) { padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; margin-right: 0; }

.nd-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}
.nd-avatar :deep(.el-avatar) {
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.nd-header-info { flex: 1; }
.nd-name { font-size: 18px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.nd-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.nd-tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 3px 10px;
  border-radius: 12px;
}

.nd-body { padding: 20px 24px; }

.nd-section { margin-bottom: 24px; }
.nd-section:last-child { margin-bottom: 0; }

.nd-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.nd-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.8;
  padding: 12px;
  background: #fafbfc;
  border-radius: 8px;
}

.nd-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 16px;
}
.nd-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: #fafbfc;
  border-radius: 8px;
}
.nd-cell-label {
  font-size: 11px;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.nd-cell-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
</style>
