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

    <!-- 筛选栏 -->
    <el-card shadow="hover" class="filter-card" :body-style="{ padding: '16px 20px' }">
      <div class="filter-bar">
        <div class="filter-left">
          <span class="card-title">评论列表</span>
          <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
        </div>
        <div class="filter-right">
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
    </el-card>

    <!-- 层级化评论列表 -->
    <div class="comment-tree-list" v-loading="loading">
      <template v-if="treeList.length > 0">
        <div v-for="(item, index) in treeList" :key="'root-' + index" class="comment-thread">
          <!-- 一级评论 -->
          <div class="comment-card comment-card--level1" :class="{ 'is-deleted': item.status === 0 }">
            <div class="cc-left">
              <el-avatar :size="40" :src="item.userAvatar" icon="UserFilled" class="cc-avatar" />
              <div class="cc-level-badge">主评</div>
            </div>
            <div class="cc-body">
              <div class="cc-header">
                <span class="cc-name">{{ item.userNickname || '未知用户' }}</span>
                <el-tag size="small" type="info" effect="plain" class="cc-id-tag">ID: {{ item.id }}</el-tag>
                <el-tag size="small" type="info" effect="plain" class="cc-post-tag">动态: {{ item.postId }}</el-tag>
                <el-tag :type="item.status === 0 ? 'danger' : 'success'" size="small" effect="light">
                  {{ item.status === 0 ? '已删除' : '正常' }}
                </el-tag>
              </div>
              <div class="cc-content">{{ item.content }}</div>
              <div class="cc-meta">
                <span class="meta-item">
                  <el-icon :size="13"><Star /></el-icon> {{ item.likeCount || 0 }}
                </span>
                <span class="meta-item">
                  <el-icon :size="13"><Clock /></el-icon> {{ item.createdAt }}
                </span>
              </div>
            </div>
            <div class="cc-actions">
              <el-button size="small" text type="primary" :icon="View" @click="viewDetail(item)">详情</el-button>
              <el-button v-if="item.status !== 0" size="small" text type="danger" :icon="Delete" @click="handleDelete(item)">删除</el-button>
              <el-button v-if="item.status === 0" size="small" text type="success" :icon="CircleCheck" @click="handleRestore(item)">恢复</el-button>
            </div>
          </div>

          <!-- 二级评论（回复） -->
          <template v-if="item.replies && item.replies.length > 0">
            <div class="replies-container">
              <div class="reply-connector"></div>
              <div class="replies-list">
                <div v-for="reply in item.replies" :key="'reply-' + reply.id"
                     class="comment-card comment-card--level2" :class="{ 'is-deleted': reply.status === 0 }">
                  <div class="cc-left">
                    <el-avatar :size="32" :src="reply.userAvatar" icon="UserFilled" class="cc-avatar cc-avatar--sm" />
                    <div class="cc-level-badge cc-level-badge--reply">回复</div>
                  </div>
                  <div class="cc-body">
                    <div class="cc-header">
                      <span class="cc-name cc-name--reply">{{ reply.userNickname || '未知用户' }}</span>
                      <el-tag size="small" type="info" effect="plain" class="cc-id-tag">ID: {{ reply.id }}</el-tag>
                      <el-tag v-if="reply.parentId" size="small" type="warning" effect="plain" class="cc-reply-to">
                        回复 #{{ reply.parentId }}
                      </el-tag>
                      <el-tag :type="reply.status === 0 ? 'danger' : 'success'" size="small" effect="light">
                        {{ reply.status === 0 ? '已删除' : '正常' }}
                      </el-tag>
                    </div>
                    <div class="cc-content cc-content--reply">{{ reply.content }}</div>
                    <div class="cc-meta">
                      <span class="meta-item">
                        <el-icon :size="13"><Star /></el-icon> {{ reply.likeCount || 0 }}
                      </span>
                      <span class="meta-item">
                        <el-icon :size="13"><Clock /></el-icon> {{ reply.createdAt }}
                      </span>
                    </div>
                  </div>
                  <div class="cc-actions cc-actions--compact">
                    <el-button size="small" text type="primary" :icon="View" @click="viewDetail(reply)">详情</el-button>
                    <el-button v-if="reply.status !== 0" size="small" text type="danger" :icon="Delete" @click="handleDelete(reply)">删除</el-button>
                    <el-button v-if="reply.status === 0" size="small" text type="success" :icon="CircleCheck" @click="handleRestore(reply)">恢复</el-button>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>
      </template>

      <!-- 空状态 -->
      <el-empty v-else description="暂无评论数据" :image-size="120" />
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > 0">
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
            <el-tag v-if="detail.parentId" size="small" type="warning" effect="light" round>
              回复评论 #{{ detail.parentId }}
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
const canExport = computed(() => adminStore.hasButton('export'))

const overviewCards = computed(() => [
  { key: 'total', label: '总评论数', value: total.value, icon: 'ChatDotRound', color: '#409eff', bg: '#ecf5ff' },
  { key: 'normal', label: '正常评论', value: list.value.filter(c => c.status !== 0).length, icon: 'CircleCheck', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'deleted', label: '已删除', value: list.value.filter(c => c.status === 0).length, icon: 'Delete', color: '#f56c6c', bg: '#fef0f0' },
  { key: 'likes', label: '总点赞数', value: list.value.reduce((sum, c) => sum + (c.likeCount || 0), 0), icon: 'Star', color: '#e6a23c', bg: '#fdf6ec' }
])

const treeList = computed(() => {
  const rootComments = []
  const childMap = new Map()
  for (const item of list.value) {
    if (!item.parentId) {
      rootComments.push({ ...item, replies: [] })
    } else {
      if (!childMap.has(item.parentId)) {
        childMap.set(item.parentId, [])
      }
      childMap.get(item.parentId).push(item)
    }
  }
  for (const root of rootComments) {
    if (childMap.has(root.id)) {
      root.replies = childMap.get(root.id)
    }
  }
  return rootComments
})

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

/* 筛选栏 */
.filter-card { margin-bottom: 16px; }
.filter-bar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.filter-left { display: flex; align-items: center; gap: 8px; }
.card-title { font-weight: 600; font-size: 15px; color: #303133; }
.filter-right { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }

/* ========== 层级化评论列表 ========== */
.comment-tree-list { min-height: 300px; }

.comment-thread {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  overflow: hidden;
  margin-bottom: 14px;
  transition: box-shadow 0.25s ease, border-color 0.25s ease;
}
.comment-thread:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-color: #d9d9d9;
}
.comment-thread.is-deleted { opacity: 0.65; }

/* 一级评论卡片 */
.comment-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 18px 20px;
  position: relative;
}
.comment-card--level1 {
  background: linear-gradient(135deg, #fafbfc 0%, #ffffff 100%);
  border-bottom: 1px solid #f0f0f0;
}
.comment-card--level1:last-child { border-bottom: none; }
.comment-card.is-deleted .cc-content { text-decoration: line-through; color: #c0c4cc; }

.cc-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  padding-top: 2px;
}
.cc-avatar {
  border: 2px solid #ecf5ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.12);
}
.cc-avatar--sm {
  border-width: 1.5px;
  box-shadow: 0 1px 4px rgba(64, 158, 255, 0.08);
}
.cc-level-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 1px 8px;
  border-radius: 8px;
  line-height: 1.6;
  letter-spacing: 0.5px;
  white-space: nowrap;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
}
.cc-level-badge--reply {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.cc-body { flex: 1; min-width: 0; }

.cc-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.cc-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.cc-name--reply { font-size: 13px; font-weight: 500; }
.cc-id-tag { font-size: 11px; transform: scale(0.9); transform-origin: left center; }
.cc-post-tag { font-size: 11px; transform: scale(0.9); transform-origin: left center; }
.cc-reply-to { font-size: 11px; transform: scale(0.9); transform-origin: left center; }

.cc-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.7;
  word-break: break-all;
  padding: 10px 14px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 3px solid #409eff;
  margin-bottom: 8px;
}
.cc-content--reply {
  font-size: 13px;
  padding: 8px 12px;
  background: #fefefe;
  border-left-color: #e6a23c;
}

.cc-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: #909399;
}

.cc-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
  padding-top: 2px;
}
.cc-actions--compact {
  flex-direction: row;
  gap: 2px;
}

/* ========== 回复区域 ========== */
.replies-container {
  display: flex;
  background: #fcfcfd;
  position: relative;
}
.reply-connector {
  width: 24px;
  flex-shrink: 0;
  position: relative;
  background: repeating-linear-gradient(
    to bottom,
    transparent,
    transparent 10px,
    #dce4ec 10px,
    #dce4ec 11px
  );
}
.reply-connector::before {
  content: '';
  position: absolute;
  top: 22px;
  left: 50%;
  width: 12px;
  height: 1px;
  background: #dce4ec;
}
.reply-connector::after {
  content: '';
  position: absolute;
  top: 19px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 5px solid #dce4ec;
  border-top: 4px solid transparent;
  border-bottom: 4px solid transparent;
}

.replies-list {
  flex: 1;
  min-width: 0;
}

.comment-card--level2 {
  padding: 12px 16px;
  border-bottom: 1px dashed #eee;
  background: #fefefe;
}
.comment-card--level2:last-child { border-bottom: none; }
.comment-card--level2:hover { background: #fbfbfc; }

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
