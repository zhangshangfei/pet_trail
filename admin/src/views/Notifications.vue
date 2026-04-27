<template>
  <div class="notifications-page">
    <el-card shadow="hover">
      <template #header>
        <div class="page-header">
          <span class="card-title">通知管理</span>
          <div class="header-actions">
            <el-select v-model="typeFilter" placeholder="类型" clearable style="width: 120px" @change="loadData">
              <el-option label="系统" value="system" />
              <el-option label="点赞" value="like" />
              <el-option label="评论" value="comment" />
              <el-option label="关注" value="follow" />
            </el-select>
            <el-select v-model="readFilter" placeholder="已读" clearable style="width: 100px" @change="loadData">
              <el-option label="未读" :value="false" />
              <el-option label="已读" :value="true" />
            </el-select>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button type="success" @click="showSendDialog">发送通知</el-button>
            <el-button type="warning" @click="showBroadcastDialog" v-if="canSend">广播通知</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="接收用户" width="100" />
        <el-table-column prop="userNickname" label="接收用户名" width="120" />
        <el-table-column prop="fromUserId" label="触发用户" width="100" />
        <el-table-column prop="fromUserNickname" label="触发用户名" width="120" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ { like: '点赞', comment: '评论', follow: '关注', favorite: '收藏', system: '系统' }[row.type] || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="isRead" label="已读" width="70">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'info' : 'danger'" size="small">{{ row.isRead ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="170" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="sendDialogVisible" title="发送系统通知" width="500px">
      <el-form :model="sendForm" label-width="100px">
        <el-form-item label="用户ID">
          <el-input v-model="sendForm.userId" placeholder="输入接收通知的用户ID" />
        </el-form-item>
        <el-form-item label="通知标题">
          <el-input v-model="sendForm.title" placeholder="通知标题（可选）" />
        </el-form-item>
        <el-form-item label="通知内容">
          <el-input v-model="sendForm.content" type="textarea" :rows="4" placeholder="输入通知内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="sending" @click="handleSend">发送</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="broadcastDialogVisible" title="广播通知" width="500px">
      <el-form :model="broadcastForm" label-width="100px">
        <el-form-item label="通知标题">
          <el-input v-model="broadcastForm.title" placeholder="广播标题（可选）" />
        </el-form-item>
        <el-form-item label="通知内容">
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

const sendForm = reactive({ userId: '', content: '', title: '' })
const broadcastForm = reactive({ content: '', title: '' })

const adminStore = useAdminStore()
const canSend = computed(() => adminStore.hasPermission('notification:send'))

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
.page-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: 600; font-size: 16px; }
.header-actions { display: flex; gap: 12px; flex-wrap: wrap; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
