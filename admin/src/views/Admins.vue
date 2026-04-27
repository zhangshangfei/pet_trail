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
        <el-table-column label="菜单权限" min-width="200">
          <template #default="{ row }">
            <div class="perm-tags">
              <el-tag v-for="p in parsePermissions(row.permissions).slice(0, 5)" :key="p" size="small" type="info" style="margin: 2px;">{{ p }}</el-tag>
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
        <el-form-item v-if="form.role !== 'SUPER_ADMIN'" label="菜单权限">
          <div class="menu-tree-wrap">
            <div style="margin-bottom: 8px;">
              <el-button size="small" @click="selectAllMenus">全选</el-button>
              <el-button size="small" @click="deselectAllMenus">清空</el-button>
            </div>
            <el-tree
              ref="menuTreeRef"
              :data="menuTree"
              show-checkbox
              node-key="id"
              :default-checked-keys="form.checkedMenuIds"
              :props="{ label: 'name', children: 'children' }"
              @check="onMenuCheck"
            />
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
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminList, createAdmin, updateAdmin, updateAdminStatus, resetAdminPassword, getMerchantList, getMenuTree } from '@/api/admin'

const roleLabelMap = { SUPER_ADMIN: '超级管理员', ADMIN: '平台管理员', MERCHANT_ADMIN: '商户管理员', MERCHANT_STAFF: '商户员工' }
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
const merchantList = ref([])
const menuTree = ref([])
const menuTreeRef = ref(null)

const isMerchantRole = computed(() => form.value.role === 'MERCHANT_ADMIN' || form.value.role === 'MERCHANT_STAFF')

function parsePermissions(perms) {
  if (!perms) return []
  return perms.split(',').map(p => p.trim()).filter(Boolean)
}

function collectAllIds(nodes) {
  let ids = []
  for (const node of nodes) {
    if (node.permission) ids.push(node.id)
    if (node.children) ids = ids.concat(collectAllIds(node.children))
  }
  return ids
}

function collectIdsByPerms(nodes, permSet) {
  let ids = []
  for (const node of nodes) {
    if (node.permission && permSet.has(node.permission)) ids.push(node.id)
    if (node.children) ids = ids.concat(collectIdsByPerms(node.children, permSet))
  }
  return ids
}

function permsToMenuIds(perms) {
  const permSet = new Set(parsePermissions(perms))
  return collectIdsByPerms(menuTree.value, permSet)
}

const rolePermDefaults = {
  SUPER_ADMIN: null,
  ADMIN: 'dashboard,user:view,user:manage,pet:view,pet:manage,post:view,post:manage,comment:view,comment:manage,report:view,report:handle,notification:view,notification:send,feedback:view,feedback:reply,log:view,ai-model:view,ai-model:manage,challenge:view,challenge:manage,product:view,product:manage,vet-clinic:view,vet-clinic:manage,export',
  MERCHANT_ADMIN: 'dashboard,vet-clinic:view,vet-clinic:manage,product:view,product:manage,feedback:view,feedback:reply',
  MERCHANT_STAFF: 'dashboard,vet-clinic:view,product:view'
}

function handleRoleChange(role) {
  if (role === 'SUPER_ADMIN') {
    form.value.checkedMenuIds = collectAllIds(menuTree.value)
  } else {
    form.value.checkedMenuIds = permsToMenuIds(rolePermDefaults[role])
  }
  nextTick(() => {
    if (menuTreeRef.value) {
      menuTreeRef.value.setCheckedKeys(form.value.checkedMenuIds || [])
    }
  })
}

function selectAllMenus() {
  if (menuTreeRef.value) {
    menuTreeRef.value.setCheckedKeys(collectAllIds(menuTree.value))
  }
}

function deselectAllMenus() {
  if (menuTreeRef.value) {
    menuTreeRef.value.setCheckedKeys([])
  }
}

function onMenuCheck() {
}

function getCheckedPermissions() {
  if (!menuTreeRef.value) return ''
  const checkedNodes = menuTreeRef.value.getCheckedNodes(false, true)
  const perms = []
  for (const node of checkedNodes) {
    if (node.permission) perms.push(node.permission)
  }
  return [...new Set(perms)].join(',')
}

async function loadMenuTree() {
  if (menuTree.value.length > 0) return
  try {
    const res = await getMenuTree()
    menuTree.value = res.data || []
  } catch (e) {}
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

async function openCreate() {
  isEdit.value = false
  editId.value = null
  form.value = { username: '', password: '', nickname: '', role: 'ADMIN', merchantId: null, checkedMenuIds: [] }
  await loadMenuTree()
  await loadMerchants()
  handleRoleChange('ADMIN')
  showDialog.value = true
}

async function openEdit(row) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    nickname: row.nickname,
    role: row.role,
    merchantId: row.merchantId,
    checkedMenuIds: []
  }
  await loadMenuTree()
  await loadMerchants()
  if (row.role === 'SUPER_ADMIN') {
    form.value.checkedMenuIds = collectAllIds(menuTree.value)
  } else {
    form.value.checkedMenuIds = permsToMenuIds(row.permissions)
  }
  showDialog.value = true
  nextTick(() => {
    if (menuTreeRef.value) {
      menuTreeRef.value.setCheckedKeys(form.value.checkedMenuIds || [])
    }
  })
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
    const permissions = form.value.role === 'SUPER_ADMIN' ? '' : getCheckedPermissions()
    const data = {
      nickname: form.value.nickname,
      role: form.value.role,
      merchantId: form.value.merchantId,
      permissions
    }
    if (!isEdit.value) {
      data.username = form.value.username
      data.password = form.value.password
    }
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
.menu-tree-wrap { width: 100%; border: 1px solid #ebeef5; border-radius: 4px; padding: 12px; max-height: 400px; overflow-y: auto; }
</style>
