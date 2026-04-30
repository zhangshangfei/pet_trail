<template>
  <div class="comments-page">
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

    <!-- 评论列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">评论列表</span>
            <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
          </div>
          <div class="header-actions">
            <el-input v-model="keyword" placeholder="搜索评论内容" clearable style="width: 200px" @clear="loadData" @keyup.enter="loadData">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-input v-model="postId" placeholder="动态ID" clearable style="width: 120px" @clear="loadData" @keyup.enter="loadData" />
            <el-select v-model="deleted" placeholder="全部状态" clearable style="width: 110px" @change="loadData">
              <el-option label="正常" :value="0" />
              <el-option label="已删除" :value="1" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
            <el-button type="success" :icon="Download" @click="handleExport" v-if="canExport">导出</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户信息" min-width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="36" :src="row.userAvatar" icon="UserFilled" />
              <div class="user-info">
                <div class="user-name">{{ row.userNickname || '未知用户' }}</div>
                <div class="user-meta">
                  <span class="user-id">ID: {{ row.userId }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="postId" label="动态ID" width="80" align="center" />
        <el-table-column prop="content" label="内容" show-overflow-tooltip min-width="200" />
        <el-table-column prop="parentId" label="回复ID" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.parentId" class="reply-id">{{ row.parentId }}</span>
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="互动" width="90" align="center">
          <template #default="{ row }">
            <div class="interact-cell">
              <el-icon :size="12"><Star /></el-icon>
              <span>{{ row.likeCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'danger' : 'success'" effect="light" size="small">
              {{ row.status === 0 ? '已删除' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评论时间" width="170">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon :size="12"><Clock /></el-icon>
              <span>{{ row.createdAt }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" text type="primary" :icon="View" @click="viewDetail(row)">详情</el-button>
              <el-button v-if="row.status !== 0 && canManage" size="small" text type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
              <el-button v-if="row.status === 0 && canManage" size="small" text type="success" :icon="CircleCheck" @click="handleRestore(row)">恢复</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 评论详情弹窗 -->
    <el-dialog v-model="showDetail" title="评论详情" width="640px" destroy-on-close class="comment-detail-dialog">
      <div v-if="detail" class="cd-header">
        <div class="cd-avatar">
          <el-avatar :size="56" :src="detail.userAvatar" icon="UserFilled" />
        </div>
        <div class="cd-header-info">
          <div class="cd-name">{{ detail.userNickname || '未知用户' }}</div>
          <div class="cd-tags">
            <el-tag :type="detail.status === 0 ? 'danger' : 'success'" size="small" effect="light" round>
              {{ detail.status === 0 ? '已删除' : '正常' }}
            </el-tag>
            <span class="cd-tag-item">
              <el-icon :size="12"><Clock /></el-icon>
              {{ detail.createdAt }}
            </span>
          </div>
        </div>
      </div>

      <div class="cd-body">
        <div class="cd-section">
          <div class="cd-section-title">评论内容</div>
          <div class="cd-content">{{ detail.content }}</div>
        </div>

        <div class="cd-section">
          <div class="cd-section-title">数据概览</div>
          <div class="cd-grid">
            <div class="cd-cell">
              <span class="cd-cell-label">评论ID</span>
              <span class="cd-cell-value">{{ detail.id }}</span>
            </div>
            <div class="cd-cell">
              <span class="cd-cell-label">动态ID</span>
              <span class="cd-cell-value">{{ detail.postId }}</span>
            </div>
            <div class="cd-cell">
              <span class="cd-cell-label">用户ID</span>
              <span class="cd-cell-value">{{ detail.userId }}</span>
            </div>
            <div class="cd-cell">
              <span class="cd-cell-label">点赞数</span>
              <span class="cd-cell-value">{{ detail.likeCount || 0 }}</span>
            </div>
            <div class="cd-cell">
              <span class="cd-cell-label">父评论ID</span>
              <span class="cd-cell-value">{{ detail.parentId || '-' }}</span>
            </div>
            <div class="cd-cell">
              <span class="cd-cell-label">回复目标ID</span>
              <span class="cd-cell-value">{{ detail.replyToId || '-' }}</span>
            </div>
            <div class="cd-cell">
              <span class="cd-cell-label">创建时间</span>
              <span class="cd-cell-value">{{ detail.createdAt }}</span>
            </div>
            <div class="cd-cell">
              <span class="cd-cell-label">更新时间</span>
              <span class="cd-cell-value">{{ detail.updatedAt || '-' }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCommentList, deleteComment, restoreComment } from '../api/admin'
import { useAdminStore } from '@/store/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download, View, Clock, Star, CircleCheck, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const keyword = ref('')
const postId = ref('')
const deleted = ref(null)
const showDetail = ref(false)
const detail = ref(null)

const adminStore = useAdminStore()
const canManage = computed(() => adminStore.hasButton('comment:manage'))
const canExport = computed(() => adminStore.hasButton('export'))

// 顶部概览数据
const overviewCards = computed(() => [
  { key: 'total', label: '总评论数', value: total.value, icon: 'ChatDotRound', color: '#409eff', bg: '#ecf5ff' },
  { key: 'normal', label: '正常评论', value: list.value.filter(c => c.status !== 0).length, icon: 'CircleCheck', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'deleted', label: '已删除', value: list.value.filter(c => c.status === 0).length, icon: 'Delete', color: '#f56c6c', bg: '#fef0f0' },
  { key: 'likes', label: '总点赞数', value: list.value.reduce((sum, c) => sum + (c.likeCount || 0), 0), icon: 'Star', color: '#e6a23c', bg: '#fdf6ec' }
])

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value
    if (postId.value) params.postId = postId.value
    if (deleted.value !== null) params.deleted = deleted.value
    const res = await getCommentList(params)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {}
  loading.value = false
}

const handlePageChange = (p) => { page.value = p; loadData() }

const viewDetail = (row) => {
  detail.value = { ...row }
  showDetail.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该评论？', '确认删除', { type: 'warning' })
    await deleteComment(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

const handleRestore = async (row) => {
  try {
    await ElMessageBox.confirm('确定恢复该评论？', '确认恢复')
    await restoreComment(row.id)
    ElMessage.success('恢复成功')
    loadData()
  } catch (e) {}
}

const handleExport = async () => {
  if (!canExport.value) { ElMessage.warning('无导出权限'); return }
  try {
    // 导出功能待后端实现
    ElMessage.info('导出功能开发中')
  } catch (e) {}
}

onMounted(() => loadData())
</script>

<style scoped>
.comments-page { padding: 0; }

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

/* 回复ID */
.reply-id { color: #409eff; font-weight: 500; }

/* 互动数据 */
.interact-cell { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #606266; }

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
.comment-detail-dialog :deep(.el-dialog__body) { padding: 0; }
.comment-detail-dialog :deep(.el-dialog__header) { padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; margin-right: 0; }

.cd-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}
.cd-avatar :deep(.el-avatar) {
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.cd-header-info { flex: 1; }
.cd-name { font-size: 18px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.cd-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.cd-tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 3px 10px;
  border-radius: 12px;
}

.cd-body { padding: 20px 24px; }

.cd-section { margin-bottom: 24px; }
.cd-section:last-child { margin-bottom: 0; }

.cd-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.cd-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.8;
  padding: 12px;
  background: #fafbfc;
  border-radius: 8px;
}

.cd-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 16px;
}
.cd-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: #fafbfc;
  border-radius: 8px;
}
.cd-cell-label {
  font-size: 11px;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.cd-cell-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
</style>
