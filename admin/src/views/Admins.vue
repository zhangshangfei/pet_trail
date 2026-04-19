<template>
  <div class="page-container">
    <div class="page-header">
      <h2>管理员管理</h2>
      <el-button type="primary" @click="showAdd = true">新增管理员</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="130" />
      <el-table-column prop="nickname" label="昵称" width="130" />
      <el-table-column label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="row.role === 'SUPER_ADMIN' ? 'danger' : 'info'" size="small">{{ row.role === 'SUPER_ADMIN' ? '超级管理员' : '管理员' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginAt" label="最后登录" width="170" />
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button size="small" type="info" @click="handleResetPwd(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="total > 0" :current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" style="margin-top: 16px; justify-content: flex-end;" />

    <el-dialog v-model="showAdd" title="新增管理员" width="450px">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="addForm.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="addForm.password" type="password" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="addForm.nickname" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="addForm.role" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="超级管理员" value="SUPER_ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEdit" title="编辑管理员" width="450px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称"><el-input v-model="editForm.nickname" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="超级管理员" value="SUPER_ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" @click="handleEdit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminList, createAdmin, updateAdmin, updateAdminStatus, resetAdminPassword } from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const showAdd = ref(false)
const showEdit = ref(false)
const addForm = ref({ username: '', password: '', nickname: '', role: 'ADMIN' })
const editForm = ref({ id: null, nickname: '', role: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminList({ page: page.value, size: size.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {}
  loading.value = false
}

const handlePageChange = (p) => { page.value = p; loadData() }

const handleAdd = async () => {
  if (!addForm.value.username || !addForm.value.password) {
    ElMessage.warning('用户名和密码不能为空')
    return
  }
  try {
    await createAdmin(addForm.value)
    ElMessage.success('新增成功')
    showAdd.value = false
    addForm.value = { username: '', password: '', nickname: '', role: 'ADMIN' }
    loadData()
  } catch (e) {}
}

const openEdit = (row) => {
  editForm.value = { id: row.id, nickname: row.nickname, role: row.role }
  showEdit.value = true
}

const handleEdit = async () => {
  try {
    await updateAdmin(editForm.value.id, { nickname: editForm.value.nickname, role: editForm.value.role })
    ElMessage.success('编辑成功')
    showEdit.value = false
    loadData()
  } catch (e) {}
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}管理员"${row.username}"？`, '确认')
    await updateAdminStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {}
}

const handleResetPwd = async (row) => {
  try {
    await ElMessageBox.confirm(`确定重置"${row.username}"的密码为默认密码？`, '确认')
    await resetAdminPassword(row.id)
    ElMessage.success('密码已重置为 admin123')
  } catch (e) {}
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
</style>
