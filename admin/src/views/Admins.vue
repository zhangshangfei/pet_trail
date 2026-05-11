<template>
  <div class="admins-page">
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

    <!-- 管理员列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">管理员列表</span>
            <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
          </div>
          <div class="header-actions">
            <el-button type="success" :icon="Plus" @click="openCreate">新增管理员</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column label="角色" min-width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="roleTagMap[row.roleCode] || 'info'" effect="light" size="small">{{ row.roleName || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属商户" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.merchantName || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="2FA" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.totpBound" type="success" effect="dark" size="small" round>已绑定</el-tag>
            <el-tag v-else type="info" effect="light" size="small" round>未绑定</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="170">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon :size="12"><Clock /></el-icon>
              <span>{{ row.lastLoginAt || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="480" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" text type="primary" :icon="EditPen" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" text :icon="row.status === 1 ? CircleClose : CircleCheck" @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
              <el-button size="small" type="info" text :icon="Key" @click="handleResetPwd(row)">重置密码</el-button>
              <el-button size="small" type="warning" text :icon="Lock" @click="openChangePwd(row)">改密码</el-button>
              <el-button v-if="row.totpBound" size="small" type="danger" text :icon="CircleClose" @click="handleTotp(row)">解绑2FA</el-button>
              <el-button v-else size="small" type="success" text :icon="CircleCheck" @click="handleTotp(row)">绑定2FA</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑管理员' : '新增管理员'" width="500px" destroy-on-close class="admin-dialog">
      <div v-if="isEdit && form.id" class="dialog-profile">
        <div class="dialog-profile-icon" style="background: #ecf5ff; color: #409eff;">
          <el-icon :size="28"><UserFilled /></el-icon>
        </div>
        <div class="dialog-profile-info">
          <div class="dialog-profile-name">{{ form.nickname || form.username || '未命名' }}</div>
          <div class="dialog-profile-meta">
            <el-tag :type="form.status === 1 ? 'success' : 'danger'" size="small" effect="light" round>{{ form.status === 1 ? '正常' : '已禁用' }}</el-tag>
          </div>
        </div>
      </div>
      <el-divider v-if="isEdit && form.id" class="dialog-divider" />
      <el-form :model="form" label-width="100px" class="dialog-form">
        <el-form-item v-if="!isEdit" label="用户名" required><el-input v-model="form.username" placeholder="请输入用户名" /></el-form-item>
        <el-form-item v-if="!isEdit" label="密码"><el-input v-model="form.password" type="password" show-password placeholder="默认 admin123" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" placeholder="请输入昵称" /></el-form-item>
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

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="showChangePwd" title="修改密码" width="420px" destroy-on-close>
      <el-form :model="changePwdForm" label-width="90px">
        <el-form-item label="管理员">{{ changePwdUsername }}</el-form-item>
        <el-form-item label="原密码" required><el-input v-model="changePwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" /></el-form-item>
        <el-form-item label="新密码" required><el-input v-model="changePwdForm.newPassword" type="password" show-password placeholder="请输入新密码" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePwd = false">取消</el-button>
        <el-button type="primary" @click="submitChangePwd">确定</el-button>
      </template>
    </el-dialog>

    <!-- 2FA绑定弹窗 -->
    <el-dialog v-model="showTotp" :title="totpBound ? '解绑2FA' : '绑定2FA'" width="420px" destroy-on-close>
      <div v-if="!totpBound && totpQrCode" style="text-align: center;">
        <p style="margin-bottom: 12px; color: #606266;">请使用 Google Authenticator 扫描二维码</p>
        <img :src="totpQrCode" style="width: 200px; height: 200px; border-radius: 8px;" />
        <p style="margin-top: 12px; font-size: 12px; color: #909399; word-break: break-all;">密钥：{{ totpSecret }}</p>
        <el-form :model="totpForm" label-width="90px" style="margin-top: 16px;">
          <el-form-item label="验证码" required><el-input v-model="totpForm.code" placeholder="输入6位验证码" maxlength="6" /></el-form-item>
        </el-form>
      </div>
      <div v-if="totpBound" style="text-align: center;">
        <p style="margin-bottom: 16px; color: #606266;">解绑后登录将不再需要验证码，确认解绑？</p>
        <el-form :model="totpForm" label-width="90px">
          <el-form-item label="验证码" required><el-input v-model="totpForm.code" placeholder="输入6位验证码" maxlength="6" /></el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showTotp = false">取消</el-button>
        <el-button v-if="!totpBound" type="primary" @click="submitBindTotp">确认绑定</el-button>
        <el-button v-if="totpBound" type="danger" @click="submitUnbindTotp">确认解绑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, EditPen, CircleCheck, CircleClose, Clock, Key, UserFilled, Lock } from '@element-plus/icons-vue'
import { getAdminList, getAdminDetail, createAdmin, updateAdmin, updateAdminStatus, resetAdminPassword, changeAdminPassword, bindTotp, unbindTotp, verifyTotp, getMerchantList, getAllRoles } from '@/api/admin'

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

const showChangePwd = ref(false)
const changePwdId = ref(null)
const changePwdUsername = ref('')
const changePwdForm = ref({ oldPassword: '', newPassword: '' })

const showTotp = ref(false)
const totpId = ref(null)
const totpBound = ref(false)
const totpQrCode = ref('')
const totpSecret = ref('')
const totpForm = ref({ code: '' })

const isMerchantRole = computed(() => {
  const role = roleList.value.find(r => r.id === form.value.roleId)
  return role && (role.code === 'MERCHANT_ADMIN' || role.code === 'MERCHANT_STAFF')
})

const overviewCards = computed(() => [
  { key: 'total', label: '总管理员', value: total.value, icon: 'UserFilled', color: '#409eff', bg: '#ecf5ff' },
  { key: 'super', label: '超级管理员', value: list.value.filter(a => a.roleCode === 'SUPER_ADMIN').length, icon: 'Star', color: '#f56c6c', bg: '#fef0f0' },
  { key: 'normal', label: '正常状态', value: list.value.filter(a => a.status === 1).length, icon: 'CircleCheck', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'disabled', label: '已禁用', value: list.value.filter(a => a.status === 0).length, icon: 'CircleClose', color: '#909399', bg: '#f4f4f5' }
])

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
    form.value = { ...detail }
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

function openChangePwd(row) {
  changePwdId.value = row.id
  changePwdUsername.value = row.username
  changePwdForm.value = { oldPassword: '', newPassword: '' }
  showChangePwd.value = true
}

async function submitChangePwd() {
  if (!changePwdForm.value.oldPassword || !changePwdForm.value.newPassword) {
    ElMessage.warning('请填写完整'); return
  }
  try {
    await changeAdminPassword(changePwdId.value, changePwdForm.value.oldPassword, changePwdForm.value.newPassword)
    ElMessage.success('密码修改成功')
    showChangePwd.value = false
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  }
}

async function handleTotp(row) {
  totpId.value = row.id
  totpBound.value = row.totpBound
  totpForm.value = { code: '' }
  totpQrCode.value = ''
  totpSecret.value = ''
  if (!row.totpBound) {
    try {
      const res = await bindTotp(row.id)
      if (res.success && res.data) {
        totpQrCode.value = res.data.qrCode || ''
        totpSecret.value = res.data.secret || ''
      }
    } catch (e) {
      ElMessage.error('生成2FA失败'); return
    }
  }
  showTotp.value = true
}

async function submitBindTotp() {
  if (!totpForm.value.code) { ElMessage.warning('请输入验证码'); return }
  try {
    const res = await verifyTotp(totpId.value, Number(totpForm.value.code))
    if (res.success && res.data === true) {
      ElMessage.success('2FA绑定成功')
      showTotp.value = false
      loadData()
    } else {
      ElMessage.error('验证码错误')
    }
  } catch (e) {
    ElMessage.error('验证失败')
  }
}

async function submitUnbindTotp() {
  if (!totpForm.value.code) { ElMessage.warning('请输入验证码'); return }
  try {
    await unbindTotp(totpId.value, Number(totpForm.value.code))
    ElMessage.success('2FA已解绑')
    showTotp.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '解绑失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.admins-page { padding: 0; }

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

.table-card { min-height: 500px; }
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.header-left { display: flex; align-items: center; gap: 8px; }
.card-title { font-weight: 600; font-size: 15px; color: #303133; }
.header-actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }

.time-cell { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #606266; }

.action-btns {
  display: flex;
  justify-content: center;
  gap: 4px;
  flex-wrap: nowrap;
}

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }

/* 弹窗样式 */
.admin-dialog :deep(.el-dialog__body) { padding-top: 8px; }
.dialog-profile {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 4px 12px;
}
.dialog-profile-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.dialog-profile-info { flex: 1; }
.dialog-profile-name { font-size: 18px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.dialog-profile-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.dialog-divider { margin: 4px 0 16px; }
.dialog-form :deep(.el-form-item) { margin-bottom: 16px; }
</style>
