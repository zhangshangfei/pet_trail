<template>
  <div class="admins-page">
    <el-card shadow="hover">
      <template #header>
        <div class="page-header">
          <span class="card-title">管理员管理</span>
          <el-button type="success" @click="openCreate">新增管理员</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column label="角色" width="130">
          <template #default="{ row }">
            <el-tag :type="roleTagMap[row.roleCode] || 'info'" size="small">{{ row.roleName || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属商户" width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.merchantName || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="openEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" text @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="info" text @click="handleResetPwd(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑管理员' : '新增管理员'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item v-if="!isEdit" label="用户名" required><el-input v-model="form.username" /></el-form-item>
        <el-form-item v-if="!isEdit" label="密码"><el-input v-model="form.password" type="password" show-password placeholder="默认 admin123" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.roleId" placeholder="选择角色" style="width: 100%" @change="onRoleChange">
            <el-option v-for="r in roleList" :key="r.id" :label="r.name" :value="r.id" :disabled="r.status === 0" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isMerchantRole" label="所属商户" required>
          <el-select v-model="form.merchantId" placeholder="选择商户" filterable style="width: 100%">
            <el-option v-for="m in merchantList" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminList, getAdminDetail, createAdmin, updateAdmin, updateAdminStatus, resetAdminPassword, getMerchantList, getAllRoles } from '@/api/admin'

const roleTagMap = { SUPER_ADMIN: 'danger', ADMIN: '', MERCHANT_ADMIN: 'warning', MERCHANT_STAFF: 'info' }

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({})
const roleList = ref([])
const merchantList = ref([])

const isMerchantRole = computed(() => {
  const role = roleList.value.find(r => r.id === form.value.roleId)
  return role && (role.code === 'MERCHANT_ADMIN' || role.code === 'MERCHANT_STAFF')
})

function onRoleChange() {
  if (!isMerchantRole.value) form.value.merchantId = null
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminList({ page: page.value, size: size.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {}
  loading.value = false
}

async function loadRoles() {
  if (roleList.value.length > 0) return
  try { const res = await getAllRoles(); roleList.value = res.data || [] } catch (e) {}
}

async function loadMerchants() {
  if (merchantList.value.length > 0) return
  try { const res = await getMerchantList({ page: 1, size: 500 }); merchantList.value = res.data?.records || [] } catch (e) {}
}

async function openCreate() {
  isEdit.value = false; editId.value = null
  form.value = { username: '', password: '', nickname: '', roleId: null, merchantId: null }
  await loadRoles(); await loadMerchants()
  showDialog.value = true
}

async function openEdit(row) {
  try {
    const res = await getAdminDetail(row.id)
    const detail = res.data || row
    isEdit.value = true; editId.value = detail.id
    form.value = { nickname: detail.nickname, roleId: detail.roleId, merchantId: detail.merchantId }
    await loadRoles(); await loadMerchants()
    showDialog.value = true
  } catch (e) {}
}

async function submitForm() {
  if (!isEdit.value && !form.value.username) { ElMessage.warning('用户名不能为空'); return }
  if (!form.value.roleId) { ElMessage.warning('请选择角色'); return }
  if (isMerchantRole.value && !form.value.merchantId) { ElMessage.warning('请选择所属商户'); return }
  try {
    const data = { nickname: form.value.nickname, roleId: form.value.roleId, merchantId: form.value.merchantId }
    if (!isEdit.value) { data.username = form.value.username; data.password = form.value.password }
    if (isEdit.value) { await updateAdmin(editId.value, data); ElMessage.success('更新成功') }
    else { await createAdmin(data); ElMessage.success('创建成功') }
    showDialog.value = false; loadData()
  } catch (e) { ElMessage.error('操作失败') }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(`确定${newStatus === 0 ? '禁用' : '启用'}管理员"${row.username}"？`, '确认')
    await updateAdminStatus(row.id, newStatus); ElMessage.success('操作成功'); loadData()
  } catch (e) {}
}

async function handleResetPwd(row) {
  try {
    await ElMessageBox.confirm(`确定重置"${row.username}"的密码？`, '确认')
    await resetAdminPassword(row.id); ElMessage.success('密码已重置为 admin123')
  } catch (e) {}
}

onMounted(() => loadData())
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 16px; font-weight: 600; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
