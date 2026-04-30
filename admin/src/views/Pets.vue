<template>
  <div class="pets-page">
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

    <!-- 宠物列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">宠物列表</span>
            <el-tag size="small" type="info">共 {{ total }} 只</el-tag>
          </div>
          <div class="header-actions">
            <el-input v-model="keyword" placeholder="搜索宠物名称" clearable style="width: 200px" @clear="loadData" @keyup.enter="loadData">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-select v-model="category" placeholder="全部类别" clearable style="width: 110px" @change="loadData">
              <el-option label="猫" :value="1" />
              <el-option label="狗" :value="2" />
              <el-option label="其他" :value="0" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
            <el-button type="success" :icon="Download" @click="handleExport" v-if="canExport">导出</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="宠物信息" min-width="220">
          <template #default="{ row }">
            <div class="pet-cell">
              <el-avatar :size="40" :src="row.avatar" icon="UserFilled" />
              <div class="pet-info">
                <div class="pet-name">{{ row.name || '未命名' }}</div>
                <div class="pet-meta">
                  <span class="pet-id">ID: {{ row.id }}</span>
                  <el-divider direction="vertical" />
                  <span class="pet-gender">{{ row.gender === 1 ? '♂ 公' : row.gender === 2 ? '♀ 母' : '未知' }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="品种" width="120">
          <template #default="{ row }">{{ row.breed || '-' }}</template>
        </el-table-column>
        <el-table-column label="类别" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.category === 1 ? 'primary' : row.category === 2 ? 'success' : 'info'" size="small" effect="light">
              {{ categoryMap[row.category] || '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="体重" width="90" align="center">
          <template #default="{ row }">{{ row.weight ? row.weight + ' kg' : '-' }}</template>
        </el-table-column>
        <el-table-column label="主人" min-width="160">
          <template #default="{ row }">
            <div class="owner-cell">
              <span class="owner-name">{{ row.userNickname || '未知' }}</span>
              <span class="owner-id">ID: {{ row.userId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
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
              <el-button v-if="canManage" size="small" text :icon="Edit" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="canManage" size="small" text type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
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

    <!-- 宠物详情弹窗 -->
    <el-dialog v-model="showDetail" title="宠物详情" width="520px" destroy-on-close class="pet-detail-dialog">
      <div v-if="detail" class="pd-header">
        <div class="pd-avatar">
          <el-avatar :size="80" :src="detail.avatar" icon="UserFilled" />
        </div>
        <div class="pd-header-info">
          <div class="pd-name">{{ detail.name }}</div>
          <div class="pd-tags">
            <el-tag size="small" effect="light" round>{{ categoryMap[detail.category] || '其他' }}</el-tag>
            <span class="pd-tag-item">
              <el-icon :size="12"><component :is="detail.gender === 2 ? 'Female' : 'Male'" /></el-icon>
              {{ detail.gender === 1 ? '公' : detail.gender === 2 ? '母' : '未知' }}
            </span>
            <span class="pd-tag-item" v-if="detail.breed">
              <el-icon :size="12"><Collection /></el-icon>
              {{ detail.breed }}
            </span>
          </div>
        </div>
      </div>

      <div class="pd-body">
        <div class="pd-section">
          <div class="pd-section-title">基本信息</div>
          <div class="pd-grid">
            <div class="pd-cell">
              <span class="pd-cell-label">宠物ID</span>
              <span class="pd-cell-value">{{ detail?.id }}</span>
            </div>
            <div class="pd-cell">
              <span class="pd-cell-label">品种</span>
              <span class="pd-cell-value">{{ detail?.breed || '-' }}</span>
            </div>
            <div class="pd-cell">
              <span class="pd-cell-label">体重</span>
              <span class="pd-cell-value">{{ detail?.weight ? detail.weight + ' kg' : '-' }}</span>
            </div>
            <div class="pd-cell">
              <span class="pd-cell-label">毛色</span>
              <span class="pd-cell-value">{{ detail?.color || '-' }}</span>
            </div>
            <div class="pd-cell">
              <span class="pd-cell-label">绝育状态</span>
              <span class="pd-cell-value">
                <el-tag :type="detail?.sterilized === 1 ? 'success' : 'info'" size="small" effect="light">
                  {{ detail?.sterilized === 1 ? '已绝育' : '未绝育' }}
                </el-tag>
              </span>
            </div>
            <div class="pd-cell">
              <span class="pd-cell-label">生日</span>
              <span class="pd-cell-value">{{ detail?.birthday || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="pd-section">
          <div class="pd-section-title">主人信息</div>
          <div class="pd-grid">
            <div class="pd-cell">
              <span class="pd-cell-label">主人ID</span>
              <span class="pd-cell-value">{{ detail?.userId }}</span>
            </div>
            <div class="pd-cell">
              <span class="pd-cell-label">主人昵称</span>
              <span class="pd-cell-value">{{ detail?.userNickname || '-' }}</span>
            </div>
            <div class="pd-cell" style="grid-column: span 2;">
              <span class="pd-cell-label">创建时间</span>
              <span class="pd-cell-value">{{ detail?.createdAt }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑宠物弹窗 -->
    <el-dialog v-model="showEdit" title="编辑宠物" width="480px" destroy-on-close class="pet-edit-dialog">
      <div v-if="editForm.id" class="pe-header">
        <el-avatar :size="48" :src="editForm.avatar" icon="UserFilled" />
        <div class="pe-header-info">
          <div class="pe-name">{{ editForm.name || '未命名' }}</div>
          <div class="pe-id">ID: {{ editForm.id }}</div>
        </div>
      </div>

      <el-form :model="editForm" label-width="80px" class="pe-form">
        <el-form-item label="名称" required>
          <el-input v-model="editForm.name" placeholder="宠物名称" />
        </el-form-item>
        <el-form-item label="品种">
          <el-input v-model="editForm.breed" placeholder="品种" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="类别">
              <el-select v-model="editForm.category" style="width: 100%">
                <el-option label="猫" :value="1" /><el-option label="狗" :value="2" /><el-option label="其他" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="editForm.gender" style="width: 100%">
                <el-option label="公" :value="1" /><el-option label="母" :value="2" /><el-option label="未知" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="体重(kg)">
              <el-input-number v-model="editForm.weight" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="毛色">
              <el-input v-model="editForm.color" placeholder="毛色" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="绝育">
              <el-select v-model="editForm.sterilized" style="width: 100%">
                <el-option label="已绝育" :value="1" /><el-option label="未绝育" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生日">
              <el-date-picker v-model="editForm.birthday" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="头像URL">
          <el-input v-model="editForm.avatar" placeholder="头像图片地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 删除确认弹窗（使用 MessageBox，无自定义弹窗） -->
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getPetList, getPetDetail, deletePet, updatePet, exportPets } from '../api/admin'
import { useAdminStore } from '@/store/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download, View, Clock, Document, User, Male, Female, Collection, Edit, Delete } from '@element-plus/icons-vue'

const categoryMap = { 0: '其他', 1: '猫', 2: '狗' }
const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const keyword = ref('')
const category = ref(null)
const showDetail = ref(false)
const detail = ref(null)
const showEdit = ref(false)
const editForm = ref({})

const adminStore = useAdminStore()
const canManage = computed(() => adminStore.hasButton('pet:manage'))
const canExport = computed(() => adminStore.hasButton('export'))

// 顶部概览数据
const overviewCards = computed(() => [
  { key: 'total', label: '总宠物数', value: total.value, icon: 'UserFilled', color: '#409eff', bg: '#ecf5ff' },
  { key: 'cat', label: '猫咪', value: list.value.filter(p => p.category === 1).length, icon: 'UserFilled', color: '#e6a23c', bg: '#fdf6ec' },
  { key: 'dog', label: '狗狗', value: list.value.filter(p => p.category === 2).length, icon: 'UserFilled', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'other', label: '其他', value: list.value.filter(p => p.category === 0).length, icon: 'UserFilled', color: '#909399', bg: '#f4f4f5' }
])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPetList({ page: page.value, size: size.value, keyword: keyword.value || undefined, category: category.value ?? undefined })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {}
  loading.value = false
}

const handlePageChange = (p) => { page.value = p; loadData() }

const viewDetail = async (row) => {
  try {
    const res = await getPetDetail(row.id)
    detail.value = res.data
    showDetail.value = true
  } catch (e) {}
}

const openEdit = async (row) => {
  try {
    const res = await getPetDetail(row.id)
    editForm.value = { ...res.data }
    showEdit.value = true
  } catch (e) {}
}

const submitEdit = async () => {
  submitting.value = true
  try {
    await updatePet(editForm.value.id, editForm.value)
    ElMessage.success('编辑成功')
    showEdit.value = false
    loadData()
  } catch (e) {
    ElMessage.error('编辑失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `<div class="delete-confirm-content">
        <div class="delete-confirm-icon">⚠️</div>
        <div class="delete-confirm-title">确定删除该宠物？</div>
        <div class="delete-confirm-desc">宠物名称：<strong>${row.name}</strong></div>
        <div class="delete-confirm-hint">删除后无法恢复，请谨慎操作</div>
      </div>`,
      '确认删除',
      {
        type: 'warning',
        dangerouslyUseHTMLString: true,
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    await deletePet(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

const handleExport = async () => {
  if (!canExport.value) { ElMessage.warning('无导出权限'); return }
  try {
    const res = await exportPets({ keyword: keyword.value || undefined, category: category.value ?? undefined })
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `宠物数据_${new Date().toISOString().slice(0, 10)}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {}
}

onMounted(() => loadData())
</script>

<style scoped>
.pets-page { padding: 0; }

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

/* 宠物单元格 */
.pet-cell { display: flex; align-items: center; gap: 12px; }
.pet-info { display: flex; flex-direction: column; gap: 4px; }
.pet-name { font-size: 14px; font-weight: 500; color: #303133; }
.pet-meta { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #909399; }
.pet-meta :deep(.el-divider--vertical) { margin: 0 4px; }

/* 主人单元格 */
.owner-cell { display: flex; flex-direction: column; gap: 2px; }
.owner-name { font-size: 13px; color: #303133; }
.owner-id { font-size: 11px; color: #909399; }

/* 时间单元格 */
.time-cell { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #606266; }

/* 分页 */
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }

/* ========== 详情弹窗 ========== */
.pet-detail-dialog :deep(.el-dialog__body) { padding: 0; }
.pet-detail-dialog :deep(.el-dialog__header) { padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; margin-right: 0; }

.pd-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}
.pd-avatar :deep(.el-avatar) {
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.pd-header-info { flex: 1; }
.pd-name { font-size: 22px; font-weight: 700; color: #303133; margin-bottom: 10px; }
.pd-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.pd-tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 3px 10px;
  border-radius: 12px;
}

.pd-body { padding: 20px 24px; }

.pd-section { margin-bottom: 24px; }
.pd-section:last-child { margin-bottom: 0; }

.pd-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.pd-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 16px;
}
.pd-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: #fafbfc;
  border-radius: 8px;
}
.pd-cell-label {
  font-size: 11px;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.pd-cell-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

/* ========== 编辑弹窗 ========== */
.pet-edit-dialog :deep(.el-dialog__body) { padding-top: 16px; }

.pe-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 0 4px 16px;
  margin-bottom: 4px;
}
.pe-header :deep(.el-avatar) {
  border: 2px solid #e4e7ed;
}
.pe-header-info { flex: 1; }
.pe-name { font-size: 17px; font-weight: 600; color: #303133; margin-bottom: 2px; }
.pe-id { font-size: 12px; color: #909399; }

.pe-form :deep(.el-form-item) { margin-bottom: 14px; }
.pe-form :deep(.el-form-item__label) {
  font-size: 13px;
  color: #606266;
  padding-right: 8px;
}
.pe-form :deep(.el-input__wrapper),
.pe-form :deep(.el-input-number),
.pe-form :deep(.el-select) {
  border-radius: 6px;
}

/* ========== 删除确认弹窗样式 ========== */
:global(.delete-confirm-content) {
  text-align: center;
  padding: 8px 0;
}
:global(.delete-confirm-icon) {
  font-size: 40px;
  margin-bottom: 12px;
}
:global(.delete-confirm-title) {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
:global(.delete-confirm-desc) {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}
:global(.delete-confirm-hint) {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 8px;
}

.action-btns {
  display: flex;
  justify-content: center;
  gap: 4px;
  flex-wrap: nowrap;
}
</style>
