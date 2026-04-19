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
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
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
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)" v-if="isSuperAdmin">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="total > 0" :current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" style="margin-top: 16px; justify-content: flex-end;" />

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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getPetList, getPetDetail, deletePet } from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const categoryMap = { 0: '其他', 1: '猫', 2: '狗' }
const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const keyword = ref('')
const category = ref(null)
const showDetail = ref(false)
const detail = ref(null)

const adminInfo = JSON.parse(localStorage.getItem('admin_info') || '{}')
const isSuperAdmin = computed(() => adminInfo.role === 'SUPER_ADMIN')

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

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除宠物"${row.name}"？`, '确认')
    await deletePet(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
</style>
