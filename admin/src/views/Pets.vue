<template>
  <div class="page-container">
    <div class="page-header">
      <h2>宠物管理</h2>
    </div>
    <div class="filter-bar">
      <el-input v-model="keyword" placeholder="搜索宠物名称" clearable style="width: 200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="category" placeholder="类别" clearable style="width: 120px" @change="loadData">
        <el-option label="猫" :value="1" />
        <el-option label="狗" :value="2" />
        <el-option label="其他" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="handleExport">导出Excel</el-button>
    </div>
    <el-card shadow="hover">
      <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="头像" width="70">
        <template #default="{ row }">
          <el-avatar v-if="row.avatar" :src="row.avatar" :size="32" />
          <el-avatar v-else :size="32">{{ row.name?.[0] || '?' }}</el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" width="120" />
      <el-table-column prop="breed" label="品种" width="120" />
      <el-table-column label="类别" width="80">
        <template #default="{ row }">{{ categoryMap[row.category] || '其他' }}</template>
      </el-table-column>
      <el-table-column label="性别" width="70">
        <template #default="{ row }">{{ row.gender === 1 ? '♂' : row.gender === 2 ? '♀' : '?' }}</template>
      </el-table-column>
      <el-table-column prop="weight" label="体重(kg)" width="90" />
      <el-table-column prop="userId" label="主人ID" width="80" />
      <el-table-column prop="userNickname" label="主人昵称" width="120" />
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text @click="viewDetail(row)">详情</el-button>
          <el-button size="small" text type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row)" v-if="canManage">删除</el-button>
        </template>
      </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-if="total > 0" :current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
      </div>
    </el-card>

    <el-dialog v-model="showDetail" title="宠物详情" width="500px">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="品种">{{ detail.breed || '-' }}</el-descriptions-item>
        <el-descriptions-item label="类别">{{ categoryMap[detail.category] || '其他' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detail.gender === 1 ? '♂ 公' : detail.gender === 2 ? '♀ 母' : '未知' }}</el-descriptions-item>
        <el-descriptions-item label="体重">{{ detail.weight ? detail.weight + ' kg' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="毛色">{{ detail.color || '-' }}</el-descriptions-item>
        <el-descriptions-item label="绝育">{{ detail.sterilized === 1 ? '已绝育' : '未绝育' }}</el-descriptions-item>
        <el-descriptions-item label="生日">{{ detail.birthday || '-' }}</el-descriptions-item>
        <el-descriptions-item label="主人ID">{{ detail.userId }}</el-descriptions-item>
        <el-descriptions-item label="主人昵称">{{ detail.userNickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detail.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="showEdit" title="编辑宠物" width="500px" destroy-on-close>
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="名称"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="品种"><el-input v-model="editForm.breed" /></el-form-item>
        <el-form-item label="类别">
          <el-select v-model="editForm.category">
            <el-option label="猫" :value="1" /><el-option label="狗" :value="2" /><el-option label="其他" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="editForm.gender">
            <el-option label="公" :value="1" /><el-option label="母" :value="2" /><el-option label="未知" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="体重(kg)"><el-input-number v-model="editForm.weight" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="毛色"><el-input v-model="editForm.color" /></el-form-item>
        <el-form-item label="绝育">
          <el-select v-model="editForm.sterilized">
            <el-option label="已绝育" :value="1" /><el-option label="未绝育" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="生日"><el-date-picker v-model="editForm.birthday" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="头像URL"><el-input v-model="editForm.avatar" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getPetList, getPetDetail, deletePet, updatePet, exportPets } from '../api/admin'
import { useAdminStore } from '@/store/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

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
    await ElMessageBox.confirm(`确定删除宠物"${row.name}"？`, '确认')
    await deletePet(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

const handleExport = async () => {
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
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
