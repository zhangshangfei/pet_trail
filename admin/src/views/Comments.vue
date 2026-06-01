<template>
  <div class="comments-page">
    <!-- 顶部统计 -->
    <div class="stats-bar">
      <div v-for="item in overviewCards" :key="item.key" class="stat-chip" :style="{ background: item.bg }">
        <div class="stat-chip-icon" :style="{ color: item.color }">
          <el-icon :size="16"><component :is="item.icon" /></el-icon>
        </div>
        <div>
          <div class="stat-chip-value">{{ item.value }}</div>
          <div class="stat-chip-label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-title">评论列表<em>{{ total }}</em></div>
      <div class="filter-controls">
        <el-input v-model="keyword" placeholder="搜索内容..." clearable prefix-icon="Search" style="width: 180px; --el-input-height: 32px;" @clear="loadData" @keyup.enter="loadData" />
        <el-input v-model="postId" placeholder="动态ID" clearable style="width: 110px; --el-input-height: 32px;" @clear="loadData" @keyup.enter="loadData" />
        <el-select v-model="deleted" placeholder="状态" clearable style="width: 100px" @change="loadData">
          <el-option label="正常" :value="0" />
          <el-option label="已删除" :value="1" />
        </el-select>
        <el-button type="primary" size="default" @click="loadData">查询</el-button>
        <el-button size="default" @click="handleExport" v-if="canExport">导出</el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list" v-loading="loading">
      <template v-if="treeList.length > 0">
        <div v-for="(thread, idx) in treeList" :key="'t-' + idx" class="comment-thread">
          <!-- 一级评论 -->
          <div class="c-item c-item--main" :class="{ 'c-item--deleted': thread.status === 0 }">
            <!-- 头像区 -->
            <div class="c-avatar-col">
              <div v-if="thread.userAvatar" class="c-avatar-img">
                <img :src="thread.userAvatar" alt="" />
              </div>
              <div v-else class="c-avatar-fallback" :style="{ background: avatarColor(thread.userNickname) }">
                {{ (thread.userNickname || '未')[0] }}
              </div>
            </div>

            <!-- 内容区 -->
            <div class="c-body-col">
              <div class="c-row-top">
                <span class="c-author">{{ thread.userNickname || '未知用户' }}</span>
                <span class="c-meta-inline">
                  <span class="c-meta-tag" @click="viewPost(thread)">动态 #{{ thread.postId }}</span>
                  <el-tag :type="thread.status === 0 ? 'danger' : 'success'" size="small" round effect="light">{{ thread.status === 0 ? '已删除' : '正常' }}</el-tag>
                  <span class="c-time"><Clock :size="12" />{{ thread.createdAt }}</span>
                </span>
              </div>
              <div class="c-text">{{ thread.content }}</div>
              <div class="c-row-bottom">
                <span class="c-like"><Star :size="13" />{{ thread.likeCount || 0 }}</span>
                <div class="c-actions-main">
                  <button class="c-btn c-btn--link" @click="viewDetail(thread)">详情</button>
                  <button class="c-btn c-btn--warn" @click="viewPost(thread)">查看动态</button>
                  <button v-if="thread.status !== 0" class="c-btn c-btn--danger" @click="handleDelete(thread)">删除</button>
                  <button v-if="thread.status === 0" class="c-btn c-btn--ok" @click="handleRestore(thread)">恢复</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 二级回复 -->
          <div v-if="thread.replies && thread.replies.length" class="c-replies">
            <div class="c-reply-line"></div>
            <div class="c-replies-inner">
              <div v-for="r in thread.replies" :key="'r-' + r.id" class="c-item c-item--reply" :class="{ 'c-item--deleted': r.status === 0 }">
                <div class="c-avatar-col c-avatar-col--sm">
                  <div v-if="r.userAvatar" class="c-avatar-img c-avatar-img--sm">
                    <img :src="r.userAvatar" alt="" />
                  </div>
                  <div v-else class="c-avatar-fallback c-avatar-fallback--sm" :style="{ background: avatarColor(r.userNickname) }">
                    {{ (r.userNickname || '未')[0] }}
                  </div>
                </div>
                <div class="c-body-col">
                  <div class="c-row-top c-row-top--sm">
                    <span class="c-author c-author--sm">{{ r.userNickname || '未知用户' }}</span>
                    <span class="c-meta-inline">
                      <span class="c-meta-tag c-meta-tag--warn">回复 #{{ r.parentId }}</span>
                      <el-tag :type="r.status === 0 ? 'danger' : 'success'" size="small" round effect="light">{{ r.status === 0 ? '已删除' : '正常' }}</el-tag>
                      <span class="c-time"><Clock :size="12" />{{ r.createdAt }}</span>
                    </span>
                  </div>
                  <div class="c-text c-text--sm">{{ r.content }}</div>
                  <div class="c-row-bottom c-row-bottom--sm">
                    <span class="c-like"><Star :size="12" />{{ r.likeCount || 0 }}</span>
                    <div class="c-actions-main c-actions-main--compact">
                      <button class="c-btn c-btn--link" @click="viewDetail(r)">详情</button>
                      <button class="c-btn c-btn--warn" @click="viewPost(r)">查看动态</button>
                      <button v-if="r.status !== 0" class="c-btn c-btn--danger" @click="handleDelete(r)">删</button>
                      <button v-if="r.status === 0" class="c-btn c-btn--ok" @click="handleRestore(r)">恢复</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
      <el-empty v-else description="暂无评论数据" :image-size="100" />
    </div>

    <!-- 分页 -->
    <div class="page-footer" v-if="total > 0">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" small @change="loadData" />
    </div>

    <!-- 评论详情弹窗 -->
    <el-dialog v-model="showDetail" title="评论详情" width="560px" destroy-on-close>
      <div v-if="detail" class="dd-wrap">
        <div class="dd-head">
          <div v-if="detail.userAvatar" class="dd-avatar-img"><img :src="detail.userAvatar" /></div>
          <div v-else class="dd-avatar-fb" :style="{ background: avatarColor(detail.userNickname) }">{{ (detail.userNickname || '未')[0] }}</div>
          <div class="dd-head-info">
            <div class="dd-name">{{ detail.userNickname || '未知用户' }}</div>
            <div class="dd-tags">
              <el-tag :type="detail.status === 0 ? 'danger' : 'success'" size="small" round effect="light">{{ detail.status === 0 ? '已删除' : '正常' }}</el-tag>
              <span v-if="detail.parentId" class="dd-tag-sub">回复 #{{ detail.parentId }}</span>
              <span class="dd-time">{{ detail.createdAt }}</span>
            </div>
          </div>
        </div>
        <div class="dd-section">
          <div class="dd-label">评论内容</div>
          <div class="dd-content">{{ detail.content }}</div>
        </div>
        <div class="dd-grid">
          <div class="dd-cell" v-for="cell in detailCells(detail)" :key="cell.label">
            <span class="dd-clabel">{{ cell.label }}</span>
            <span class="dd-cval">{{ cell.value }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 动态预览弹窗 -->
    <el-dialog v-model="showPost" title="关联动态" width="560px" destroy-on-close>
      <div v-if="postDetail" v-loading="postLoading" class="pp-wrap">
        <div class="pp-head">
          <el-avatar :size="40" :src="postDetail.userAvatar" />
          <div class="pp-head-info">
            <div class="pp-name">{{ postDetail.userNickname || '未知用户' }}</div>
            <span class="pp-time">{{ postDetail.createdAt }}</span>
          </div>
          <el-tag :type="postDetail.auditStatus === 1 ? 'success' : postDetail.auditStatus === 2 ? 'danger' : 'warning'" size="small" round effect="light">
            {{ postDetail.auditStatus === 1 ? '已通过' : postDetail.auditStatus === 2 ? '已拒绝' : '待审核' }}
          </el-tag>
        </div>
        <div class="pp-content">{{ postDetail.content }}</div>
        <div class="pp-images" v-if="postDetail.images">
          <el-image v-for="(img, i) in parsePostImages(postDetail.images)" :key="i" :src="img" fit="cover" style="width:80px;height:80px;border-radius:6px;margin-right:8px;margin-bottom:4px;" :preview-src-list="parsePostImages(postDetail.images)" preview-teleported />
        </div>
        <div class="pp-foot">
          <span><Star :size="13" />{{ postDetail.likeCount || 0 }}</span>
          <span><ChatDotRound :size="13" />{{ postDetail.commentCount || 0 }}</span>
          <span class="pp-id">ID: {{ postDetail.id }}</span>
        </div>
      </div>
      <el-empty v-else-if="!postLoading" description="动态不存在或已删除" :image-size="70" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCommentList, deleteComment, restoreComment, getPostDetail } from '../api/admin'
import { useAdminStore } from '@/store/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download, View, Clock, Star, CircleCheck, Delete, ChatDotRound } from '@element-plus/icons-vue'

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
const showPost = ref(false)
const postDetail = ref(null)
const postLoading = ref(false)

const adminStore = useAdminStore()
const canExport = computed(() => adminStore.hasButton('export'))

const avatarColors = ['#667eea', '#f093fb', '#4facfe', '#43e97b', '#fa709a', '#fee140', '#30cfd0', '#a18cd1']
const avatarColor = (name) => {
  if (!name) return avatarColors[0]
  const idx = name.charCodeAt(0) % avatarColors.length
  return avatarColors[idx]
}

const overviewCards = computed(() => [
  { key: 'total', label: '总评论数', value: total.value, icon: 'ChatDotRound', color: '#409eff', bg: '#ecf5ff' },
  { key: 'normal', label: '正常', value: list.value.filter(c => c.status !== 0).length, icon: 'CircleCheck', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'deleted', label: '已删除', value: list.value.filter(c => c.status === 0).length, icon: 'Delete', color: '#f56c6c', bg: '#fef0f0' },
  { key: 'likes', label: '总点赞', value: list.value.reduce((s, c) => s + (c.likeCount || 0), 0), icon: 'Star', color: '#e6a23c', bg: '#fdf6ec' }
])

const treeList = computed(() => {
  const roots = []
  const map = new Map()
  for (const item of list.value) {
    if (!item.parentId) { roots.push({ ...item, replies: [] }) }
    else { if (!map.has(item.parentId)) map.set(item.parentId, []); map.get(item.parentId).push(item) }
  }
  for (const r of roots) { if (map.has(r.id)) r.replies = map.get(r.id) }
  return roots
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

const viewDetail = (row) => { detail.value = { ...row }; showDetail.value = true }

const viewPost = async (row) => {
  showPost.value = true; postDetail.value = null; postLoading.value = true
  try { const res = await getPostDetail(row.postId); postDetail.value = res.data }
  catch (e) { postDetail.value = null }
  finally { postLoading.value = false }
}

const parsePostImages = (images) => { try { return JSON.parse(images) || [] } catch (e) { return [] } }

const detailCells = (row) => [
  { label: '评论ID', value: row.id }, { label: '动态ID', value: row.postId },
  { label: '用户ID', value: row.userId }, { label: '点赞', value: row.likeCount || 0 },
  { label: '父评论', value: row.parentId || '-' }, { label: '回复目标', value: row.replyToId || '-' },
  { label: '创建时间', value: row.createdAt }, { label: '更新时间', value: row.updatedAt || '-' }
]

const handleDelete = async (row) => {
  try { await ElMessageBox.confirm('确定删除该评论？', '提示', { type: 'warning' }); await deleteComment(row.id); ElMessage.success('删除成功'); loadData() } catch (e) {}
}
const handleRestore = async (row) => {
  try { await ElMessageBox.confirm('确定恢复该评论？'); await restoreComment(row.id); ElMessage.success('恢复成功'); loadData() } catch (e) {}
}
const handleExport = async () => { if (!canExport.value) { ElMessage.warning('无导出权限'); return }; ElMessage.info('导出功能开发中') }

onMounted(() => loadData())
</script>

<style scoped>
/* ====== 页面框架 ====== */
.comments-page { padding: 0; display: flex; flex-direction: column; gap: 14px; min-height: calc(100vh - 84px); }

/* ====== 统计条 ====== */
.stats-bar { display: flex; gap: 10px; flex-wrap: wrap; }
.stat-chip {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 16px; border-radius: 10px; flex: 1; min-width: 120px;
  transition: transform .15s;
}
.stat-chip:hover { transform: translateY(-1px); }
.stat-chip-icon { width: 28px; height: 28px; border-radius: 7px; display: flex; align-items: center; justify-content: center; background: rgba(255,255,255,.7); border-radius: 7px; }
.stat-chip-value { font-size: 20px; font-weight: 700; color: #303133; line-height: 1; }
.stat-chip-label { font-size: 11px; color: #909399; margin-top: 1px; }

/* ====== 筛选栏 ====== */
.filter-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 20px; background: #fff; border-radius: 10px;
  border: 1px solid #ebeef5; flex-wrap: wrap; gap: 12px;
}
.filter-title { font-size: 15px; font-weight: 600; color: #303133; white-space: nowrap; }
.filter-title em { font-style: normal; font-weight: 400; color: #909399; margin-left: 4px; font-size: 13px; }
.filter-controls { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }

/* ====== 评论列表 ====== */
.comment-list { flex: 1; }

.comment-thread {
  background: #fff; border-radius: 10px; border: 1px solid #ebeef5;
  overflow: hidden; margin-bottom: 10px;
  transition: box-shadow .2s, border-color .2s;
}
.comment-thread:hover { box-shadow: 0 2px 12px rgba(0,0,0,.06); border-color: #dcdfe6; }

/* ---- 评论项 ---- */
.c-item { display: flex; gap: 12px; padding: 14px 18px; position: relative; }
.c-item--main { border-bottom: 1px solid #f5f7fa; }
.c-item--main:last-child { border-bottom: none; }
.c-item--reply { background: #fafcff; border-bottom: 1px dashed #eee; padding: 10px 18px 10px 14px; }
.c-item--reply:last-child { border-bottom: none; }
.c-item--reply:hover { background: #f5f8ff; }
.c-item--deleted { opacity: .55; }
.c-item--deleted .c-text { text-decoration: line-through; color: #c0c4cc; }

/* 头像 */
.c-avatar-col { flex-shrink: 0; display: flex; flex-direction: column; align-items: center; gap: 4px; }
.c-avatar-col--sm { gap: 2px; }
.c-avatar-img {
  width: 40px; height: 40px; border-radius: 50%; overflow: hidden;
  border: 2px solid #ecf5ff; flex-shrink: 0;
}
.c-avatar-img--sm { width: 30px; height: 30px; border-width: 1.5px; }
.c-avatar-img img { width: 100%; height: 100%; object-fit: cover; }
.c-avatar-fallback {
  width: 40px; height: 40px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 16px; font-weight: 600; flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0,0,0,.1);
}
.c-avatar-fallback--sm { width: 30px; height: 30px; font-size: 12px; }

/* 内容列 */
.c-body-col { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px; }

/* 顶部行 */
.c-row-top { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 6px; }
.c-row-top--sm { align-items: baseline; }
.c-author { font-size: 14px; font-weight: 600; color: #303133; }
.c-author--sm { font-size: 13px; font-weight: 500; }
.c-meta-inline { display: flex; align-items: center; gap: 6px; flex-shrink: 0; }
.c-meta-tag {
  font-size: 11px; color: #409eff; cursor: pointer; padding: 1px 6px;
  border-radius: 4px; background: #ecf5ff; transition: background .15s;
}
.c-meta-tag:hover { background: #d9ecff; }
.c-meta-tag--warn { color: #e6a23c; background: #fdf6ec; }
.c-meta-tag--warn:hover { background: #faecd8; }
.c-time { display: inline-flex; align-items: center; gap: 2px; font-size: 11px; color: #c0c4cc; }

/* 内容文字 */
.c-text {
  font-size: 13px; color: #4a4a4a; line-height: 1.65;
  word-break: break-all; padding: 8px 12px;
  background: #f8f9fb; border-radius: 6px; border-left: 3px solid #409eff;
}
.c-text--sm { font-size: 12px; padding: 6px 10px; border-left-width: 2px; }

/* 底部操作行 */
.c-row-bottom { display: flex; align-items: center; justify-content: space-between; }
.c-row-bottom--sm { margin-top: 2px; }
.c-like { display: inline-flex; align-items: center; gap: 3px; font-size: 12px; color: #909399; }

.c-actions-main { display: flex; gap: 2px; }
.c-actions-main--compact { gap: 1px; }
.c-btn {
  font-size: 12px; padding: 3px 8px; border: none; border-radius: 4px;
  cursor: pointer; transition: all .15s; background: transparent; line-height: 1.6;
  white-space: nowrap;
}
.c-btn:hover { opacity: .75; }
.c-btn--link { color: #409eff; }
.c-btn--warn { color: #e6a23c; }
.c-btn--danger { color: #f56c6c; }
.c-btn--ok { color: #67c23a; }

/* ---- 回复区域 ---- */
.c-replies { display: flex; background: #fbfcff; }
.c-reply-line {
  width: 22px; flex-shrink: 0; position: relative;
  border-left: 2px dashed #dce4ec; margin-left: 19px;
  margin-top: 8px; margin-bottom: 8px;
}
.c-replies-inner { flex: 1; min-width: 0; }

/* ====== 分页 ====== */
.page-footer { display: flex; justify-content: flex-end; padding: 8px 0; }

/* ====== 详情弹窗 ====== */
.dd-wrap { padding: 4px 0; }
.dd-head { display: flex; align-items: center; gap: 14px; padding: 16px 20px; background: linear-gradient(135deg,#f5f7fa,#fff); border-radius: 10px 10px 0 0; }
.dd-avatar-img { width: 48px; height: 48px; border-radius: 50%; overflow: hidden; border: 2px solid #fff; box-shadow: 0 2px 8px rgba(0,0,0,.08); }
.dd-avatar-img img { width: 100%; height: 100%; object-fit: cover; }
.dd-avatar-fb { width: 48px; height: 48px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 18px; font-weight: 600; border: 2px solid #fff; box-shadow: 0 2px 8px rgba(0,0,0,.08); }
.dd-head-info { flex: 1; }
.dd-name { font-size: 16px; font-weight: 600; color: #303133; }
.dd-tags { display: flex; align-items: center; gap: 8px; margin-top: 4px; flex-wrap: wrap; }
.dd-tag-sub { font-size: 11px; color: #e6a23c; background: #fdf6ec; padding: 1px 8px; border-radius: 4px; }
.dd-time { font-size: 11px; color: #909399; }
.dd-section { padding: 16px 20px 0; }
.dd-label { font-size: 12px; font-weight: 600; color: #909399; margin-bottom: 6px; text-transform: uppercase; letter-spacing: .5px; }
.dd-content { font-size: 14px; color: #303133; line-height: 1.7; padding: 10px 14px; background: #fafbfc; border-radius: 6px; }
.dd-grid { display: grid; grid-template-columns: repeat(2,1fr); gap: 8px; padding: 0 20px 16px; }
.dd-cell { display: flex; flex-direction: column; gap: 2px; padding: 8px 10px; background: #fafbfc; border-radius: 6px; }
.dd-clabel { font-size: 10px; color: #909399; text-transform: uppercase; letter-spacing: .3px; }
.dd-cval { font-size: 13px; color: #303133; font-weight: 500; }

/* ====== 动态预览弹窗 ====== */
.pp-wrap { padding: 4px 0; }
.pp-head { display: flex; align-items: center; gap: 12px; padding: 14px 20px; background: linear-gradient(135deg,#f0f9eb,#fff); border-radius: 10px 10px 0 0; }
.pp-head-info { flex: 1; }
.pp-name { font-size: 14px; font-weight: 600; color: #303133; }
.pp-time { font-size: 11px; color: #909399; margin-top: 2px; }
.pp-content { padding: 14px 20px; font-size: 14px; color: #303133; line-height: 1.7; word-break: break-all; }
.pp-images { padding: 0 20px; display: flex; flex-wrap: wrap; }
.pp-foot { display: flex; align-items: center; gap: 16px; padding: 10px 20px; background: #fafbfc; border-radius: 0 0 10px 10px; font-size: 12px; color: #606266; }
.pp-id { margin-left: auto; color: #909399; }
</style>
