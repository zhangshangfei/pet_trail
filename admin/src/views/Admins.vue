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
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="roleTagMap[row.role] || 'info'" size="small">{{ roleLabelMap[row.role] || row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属商户" width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.merchantName || '-' }}</template>
        </el-table-column>
        <el-table-column label="权限" min-width="200">
          <template #default="{ row }">
            <div class="perm-tags">
              <el-tag v-for="p in parsePermissions(row.permissions).slice(0, 5)" :key="p" size="small" type="info" style="margin: 2px;">{{ permLabelMap[p] || p }}</el-tag>
              <el-tag v-if="parsePermissions(row.permissions).length > 5" size="small" style="margin: 2px;">+{{ parsePermissions(row.permissions).length - 5 }}</el-tag>
            </div>
          </template>
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

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑管理员' : '新增管理员'" width="650px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item v-if="!isEdit" label="用户名" required><el-input v-model="form.username" /></el-form-item>
        <el-form-item v-if="!isEdit" label="密码"><el-input v-model="form.password" type="password" show-password placeholder="默认 admin123" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.role" style="width: 100%" @change="handleRoleChange">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="平台管理员" value="ADMIN" />
            <el-option label="商户管理员" value="MERCHANT_ADMIN" />
            <el-option label="商户员工" value="MERCHANT_STAFF" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isMerchantRole" label="所属商户" required>
          <el-select v-model="form.merchantId" placeholder="选择商户" filterable style="width: 100%">
            <el-option v-for="m in merchantList" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="权限配置">
          <div class="perm-config">
            <el-button size="small" @click="selectAllPerms">全选</el-button>
            <el-button size="small" @click="deselectAllPerms">清空</el-button>
            <div class="perm-groups">
              <div v-for="group in permGroups" :key="group.label" class="perm-group">
                <div class="perm-group-label">{{ group.label }}</div>
                <el-checkbox-group v-model="form.permList">
                  <el-checkbox v-for="p in group.items" :key="p.code" :label="p.code">{{ p.label }}</el-checkbox>
                </el-checkbox-group>
              </div>
            </div>
          </div>
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
import { getAdminList, createAdmin, updateAdmin, updateAdminStatus, resetAdminPassword, getMerchantList, getAllPermissions } from '@/api/admin'

const roleLabelMap = { SUPER_ADMIN: '超级管理员', ADMIN: '平台管理员', MERCHANT_ADMIN: '商户管理员', MERCHANT_STAFF: '商户员工' }
const roleTagMap = { SUPER_ADMIN: 'danger', ADMIN: '', MERCHANT_ADMIN: 'warning', MERCHANT_STAFF: 'info' }

const permLabelMap = {
  'dashboard': '仪表盘', 'user:view': '用户查看', 'pet:view': '宠物查看', 'pet:manage': '宠物管理',
  'post:view': '动态查看', 'post:manage': '动态管理', 'comment:view': '评论查看', 'comment:manage': '评论管理',
  'report:view': '举报查看', 'report:handle': '举报处理', 'notification:view': '通知查看', 'notification:send': '通知发送',
  'feedback:view': '反馈查看', 'feedback:reply': '反馈回复', 'admin:manage': '管理员管理', 'log:view': '操作日志',
  'setting:manage': '系统设置', 'config:manage': '系统配置', 'ai-model:view': 'AI模型查看', 'ai-model:manage': 'AI模型管理',
  'challenge:view': '挑战赛查看', 'challenge:manage': '挑战赛管理', 'product:view': '商品查看', 'product:manage': '商品管理',
  'vet-clinic:view': '医院查看', 'vet-clinic:manage': '医院管理', 'merchant:manage': '商户管理', 'export': '数据导出'
}

const permGroups = [
  { label: '基础', items: [{ code: 'dashboard', label: '仪表盘' }, { code: 'export', label: '数据导出' }] },
  { label: '用户与宠物', items: [{ code: 'user:view', label: '用户查看' }, { code: 'pet:view', label: '宠物查看' }, { code: 'pet:manage', label: '宠物管理' }] },
  { label: '内容管理', items: [{ code: 'post:view', label: '动态查看' }, { code: 'post:manage', label: '动态管理' }, { code: 'comment:view', label: '评论查看' }, { code: 'comment:manage', label: '评论管理' }] },
  { label: '举报与反馈', items: [{ code: 'report:view', label: '举报查看' }, { code: 'report:handle', label: '举报处理' }, { code: 'feedback:view', label: '反馈查看' }, { code: 'feedback:reply', label: '反馈回复' }] },
  { label: '通知', items: [{ code: 'notification:view', label: '通知查看' }, { code: 'notification:send', label: '通知发送' }] },
  { label: '商城与医院', items: [{ code: 'product:view', label: '商品查看' }, { code: 'product:manage', label: '商品管理' }, { code: 'vet-clinic:view', label: '医院查看' }, { code: 'vet-clinic:manage', label: '医院管理' }] },
  { label: '活动', items: [{ code: 'challenge:view', label: '挑战赛查看' }, { code: 'challenge:manage', label: '挑战赛管理' }] },
  { label: 'AI模型', items: [{ code: 'ai-model:view', label: 'AI模型查看' }, { code: 'ai-model:manage', label: 'AI模型管理' }] },
  { label: '系统管理', items: [{ code: 'admin:manage', label: '管理员管理' }, { code: 'log:view', label: '操作日志' }, { code: 'setting:manage', label: '系统设置' }, { code: 'config:manage', label: '系统配置' }, { code: 'merchant:manage', label: '商户管理' }] }
]

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({})
const merchantList = ref([])

const isMerchantRole = computed(() => form.value.role === 'MERCHANT_ADMIN' || form.value.role === 'MERCHANT_STAFF')

function parsePermissions(perms) {
  if (!perms) return []
  return perms.split(',').map(p => p.trim()).filter(Boolean)
}

function handleRoleChange(role) {
  const defaults = {
    SUPER_ADMIN: permGroups.flatMap(g => g.items.map(i => i.code)),
    ADMIN: permGroups.flatMap(g => g.items.map(i => i.code)).filter(c => !['admin:manage', 'setting:manage', 'config:manage', 'merchant:manage'].includes(c)),
    MERCHANT_ADMIN: ['dashboard', 'vet-clinic:view', 'vet-clinic:manage', 'product:view', 'product:manage', 'feedback:view', 'feedback:reply'],
    MERCHANT_STAFF: ['dashboard', 'vet-clinic:view', 'product:view']
  }
  form.value.permList = [...(defaults[role] || ['dashboard'])]
}

function selectAllPerms() {
  form.value.permList = permGroups.flatMap(g => g.items.map(i => i.code))
}

function deselectAllPerms() {
  form.value.permList = []
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

async function loadMerchants() {
  if (merchantList.value.length > 0) return
  try {
    const res = await getMerchantList({ page: 1, size: 500 })
    merchantList.value = res.data?.records || []
  } catch (e) {}
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  form.value = { username: '', password: '', nickname: '', role: 'ADMIN', merchantId: null, permList: [] }
  handleRoleChange('ADMIN')
  loadMerchants()
  showDialog.value = true
}

function openEdit(row) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    nickname: row.nickname,
    role: row.role,
    merchantId: row.merchantId,
    permList: parsePermissions(row.permissions)
  }
  loadMerchants()
  showDialog.value = true
}

async function submitForm() {
  if (!isEdit.value && !form.value.username) {
    ElMessage.warning('用户名不能为空')
    return
  }
  if (isMerchantRole.value && !form.value.merchantId) {
    ElMessage.warning('请选择所属商户')
    return
  }
  try {
    const data = {
      ...form.value,
      permissions: form.value.permList.join(',')
    }
    delete data.permList
    if (isEdit.value) {
      await updateAdmin(editId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createAdmin(data)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    loadData()
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}管理员"${row.username}"？`, '确认')
    await updateAdminStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {}
}

async function handleResetPwd(row) {
  try {
    await ElMessageBox.confirm(`确定重置"${row.username}"的密码为默认密码？`, '确认')
    await resetAdminPassword(row.id)
    ElMessage.success('密码已重置为 admin123')
  } catch (e) {}
}

onMounted(() => loadData())
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 16px; font-weight: 600; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.perm-tags { display: flex; flex-wrap: wrap; }
.perm-config { width: 100%; }
.perm-groups { margin-top: 12px; display: flex; flex-direction: column; gap: 12px; }
.perm-group { border: 1px solid #ebeef5; border-radius: 4px; padding: 10px 12px; }
.perm-group-label { font-size: 13px; font-weight: 600; color: #606266; margin-bottom: 8px; }
</style>
