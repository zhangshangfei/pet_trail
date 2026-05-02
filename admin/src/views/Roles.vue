<template>
  <div class="roles-page">
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

    <!-- 角色列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">角色管理</span>
            <el-tag size="small" type="info">共 {{ list.length }} 个</el-tag>
          </div>
          <div class="header-actions">
            <el-button type="success" :icon="Plus" @click="openCreate">新增角色</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="角色名称" min-width="150" />
        <el-table-column prop="code" label="角色编码" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="360" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" text type="primary" :icon="EditPen" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" text type="primary" :icon="Key" @click="openPermission(row)">权限配置</el-button>
              <el-button v-if="row.status === 1" size="small" text type="warning" :icon="CircleClose" @click="handleStatus(row, 0)">禁用</el-button>
              <el-button v-if="row.status === 0" size="small" text type="success" :icon="CircleCheck" @click="handleStatus(row, 1)">启用</el-button>
              <el-button size="small" text type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑角色' : '新增角色'" width="500px" destroy-on-close class="role-dialog">
      <div v-if="isEdit && form.id" class="dialog-profile">
        <div class="dialog-profile-icon" style="background: #ecf5ff; color: #409eff;">
          <el-icon :size="28"><UserFilled /></el-icon>
        </div>
        <div class="dialog-profile-info">
          <div class="dialog-profile-name">{{ form.name || '未命名角色' }}</div>
          <div class="dialog-profile-meta">
            <el-tag :type="form.status === 1 ? 'success' : 'danger'" size="small" effect="light" round>{{ form.status === 1 ? '启用' : '禁用' }}</el-tag>
            <span v-if="form.code" class="dialog-profile-code">{{ form.code }}</span>
          </div>
        </div>
      </div>
      <el-divider v-if="isEdit && form.id" class="dialog-divider" />
      <el-form :model="form" label-width="100px" class="dialog-form">
        <el-form-item label="角色名称" required><el-input v-model="form.name" placeholder="请输入角色名称" /></el-form-item>
        <el-form-item label="角色编码" required><el-input v-model="form.code" :disabled="isEdit" placeholder="如 ADMIN, MERCHANT_ADMIN" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入角色描述" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option label="启用" :value="1" /><el-option label="禁用" :value="0" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限配置弹窗 -->
    <el-dialog v-model="showPermission" title="权限配置" width="650px" destroy-on-close class="perm-dialog">
      <div v-if="currentRole" class="perm-header">
        <div class="perm-role-icon" style="background: #ecf5ff; color: #409eff;">
          <el-icon :size="24"><UserFilled /></el-icon>
        </div>
        <div class="perm-role-info">
          <div class="perm-role-name">{{ currentRole.name }}</div>
          <div class="perm-role-meta">
            <el-tag type="info" size="small" effect="light" round>{{ currentRole.code }}</el-tag>
            <el-tag :type="currentRole.status === 1 ? 'success' : 'danger'" size="small" effect="light" round>{{ currentRole.status === 1 ? '启用' : '禁用' }}</el-tag>
          </div>
        </div>
      </div>
      <el-divider class="dialog-divider" />
      <div class="perm-tree-wrap">
        <div class="perm-tree-actions">
          <el-button size="small" :icon="Check" @click="selectAll">全选菜单</el-button>
          <el-button size="small" :icon="Close" @click="deselectAll">清空</el-button>
        </div>
        <el-tree ref="permTreeRef" :data="menuTree" show-checkbox node-key="id" default-expand-all :default-checked-keys="checkedMenuIds" :props="{ label: 'name', children: 'children' }" @check="onTreeCheck">
          <template #default="{ node, data }">
            <div class="tree-node">
              <span>{{ data.name }}</span>
              <div v-if="data.buttons && data.buttons.length > 0" class="node-buttons" @click.stop>
                <el-checkbox v-for="btn in data.buttons" :key="btn" v-model="buttonState[data.id + '_' + btn]" :label="btnLabelMap[btn] || btn" size="small" @change="onButtonChange" />
              </div>
            </div>
          </template>
        </el-tree>
      </div>
      <template #footer>
        <el-button @click="showPermission = false">取消</el-button>
        <el-button type="primary" :icon="Check" @click="savePermission">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, EditPen, Delete, CircleCheck, CircleClose, Key, UserFilled, Check, Close } from '@element-plus/icons-vue'
import { getRoleList, getRoleDetail, createRole, updateRole, deleteRole, updateRoleStatus, getRoleMenus, saveRoleMenus, getMenuTree } from '@/api/admin'
import { useAdminStore } from '@/store/admin'

const adminStore = useAdminStore()

const btnLabelMap = {
  'user:manage': '管理', 'pet:manage': '管理', 'post:manage': '管理', 'comment:manage': '管理',
  'report:handle': '处理', 'notification:send': '发送', 'feedback:reply': '回复',
  'challenge:manage': '管理', 'product:manage': '管理', 'vet-clinic:manage': '管理',
  'ai-model:manage': '管理', 'merchant:manage': '管理', 'export': '导出'
}

const loading = ref(false)
const list = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({})
const showPermission = ref(false)
const currentRole = ref(null)
const menuTree = ref([])
const checkedMenuIds = ref([])
const permTreeRef = ref(null)
const buttonState = ref({})

const overviewCards = computed(() => [
  { key: 'total', label: '总角色数', value: list.value.length, icon: 'UserFilled', color: '#409eff', bg: '#ecf5ff' },
  { key: 'enabled', label: '已启用', value: list.value.filter(r => r.status === 1).length, icon: 'CircleCheck', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'disabled', label: '已禁用', value: list.value.filter(r => r.status === 0).length, icon: 'CircleClose', color: '#f56c6c', bg: '#fef0f0' },
  { key: 'withPerm', label: '已配置权限', value: list.value.filter(r => r.menuCount > 0).length, icon: 'Key', color: '#e6a23c', bg: '#fdf6ec' }
])

async function loadData() {
  loading.value = true
  try {
    const res = await getRoleList({ page: 1, size: 100 })
    list.value = res.data?.records || []
  } catch (e) {}
  loading.value = false
}

function openCreate() {
  isEdit.value = false; editId.value = null
  form.value = { name: '', code: '', description: '', status: 1 }
  showDialog.value = true
}

async function openEdit(row) {
  try {
    const res = await getRoleDetail(row.id)
    const detail = res.data?.role || row
    isEdit.value = true; editId.value = detail.id
    form.value = { id: detail.id, name: detail.name, code: detail.code, description: detail.description, status: detail.status }
    showDialog.value = true
  } catch (e) {}
}

async function submitForm() {
  if (!form.value.name || !form.value.code) { ElMessage.warning('请填写必填项'); return }
  try {
    if (isEdit.value) { await updateRole(editId.value, form.value); ElMessage.success('更新成功') }
    else { await createRole(form.value); ElMessage.success('创建成功') }
    showDialog.value = false; loadData()
  } catch (e) { ElMessage.error('操作失败') }
}

async function handleStatus(row, status) {
  const action = status === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}角色"${row.name}"吗？`, '确认', { type: 'warning' })
    await updateRoleStatus(row.id, status)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除角色"${row.name}"？`, '提示', { type: 'warning' })
    await deleteRole(row.id); ElMessage.success('删除成功'); loadData()
  } catch (e) {}
}

async function openPermission(row) {
  currentRole.value = row
  buttonState.value = {}
  checkedMenuIds.value = []
  try {
    const [treeRes, menuRes] = await Promise.all([getMenuTree(), getRoleMenus(row.id)])
    menuTree.value = treeRes.data || []
    const roleMenus = menuRes.data || []

    const allMenuIds = new Set()
    for (const rm of roleMenus) {
      allMenuIds.add(rm.menuId)
      if (rm.buttons) {
        const btnArr = Array.isArray(rm.buttons) ? rm.buttons : String(rm.buttons).split(',')
        for (const b of btnArr) {
          const trimmed = String(b).trim()
          if (trimmed) buttonState.value[rm.menuId + '_' + trimmed] = true
        }
      }
    }

    const parentIds = new Set()
    const collectParentIds = (nodes) => {
      for (const n of nodes) {
        if (n.children && n.children.length > 0) {
          parentIds.add(n.id)
          collectParentIds(n.children)
        }
      }
    }
    collectParentIds(menuTree.value)

    checkedMenuIds.value = [...allMenuIds].filter(id => !parentIds.has(id))
    showPermission.value = true
  } catch (e) { ElMessage.error('加载权限失败') }
}

function selectAll() {
  if (permTreeRef.value) {
    const leafIds = []
    const collectLeaves = (nodes) => { for (const n of nodes) { if (n.children && n.children.length > 0) { collectLeaves(n.children) } else { leafIds.push(n.id) } } }
    collectLeaves(menuTree.value)
    permTreeRef.value.setCheckedKeys(leafIds)
  }
}

function deselectAll() {
  if (permTreeRef.value) permTreeRef.value.setCheckedKeys([])
  buttonState.value = {}
}

function onTreeCheck() {}

function onButtonChange() {}

async function savePermission() {
  if (!permTreeRef.value || !currentRole.value) return
  const checkedKeys = permTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = permTreeRef.value.getHalfCheckedKeys()
  const allKeys = [...new Set([...checkedKeys, ...halfCheckedKeys])]

  const nodeMap = new Map()
  const collectNodes = (nodes) => {
    for (const n of nodes) {
      nodeMap.set(n.id, n)
      if (n.children) collectNodes(n.children)
    }
  }
  collectNodes(menuTree.value)

  const menuPerms = []
  for (const key of allKeys) {
    const node = nodeMap.get(key)
    if (!node) continue
    const btns = []
    if (node.buttons && node.buttons.length > 0) {
      for (const b of node.buttons) {
        if (buttonState.value[node.id + '_' + b]) btns.push(b)
      }
    }
    menuPerms.push({ menuId: node.id, buttons: btns.join(',') })
  }
  try {
    await saveRoleMenus(currentRole.value.id, menuPerms)
    ElMessage.success('权限保存成功')
    showPermission.value = false
    adminStore.fetchProfile()
  } catch (e) { ElMessage.error('保存失败') }
}

onMounted(() => loadData())
</script>

<style scoped>
.roles-page { padding: 0; }

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

.action-btns {
  display: flex;
  justify-content: center;
  gap: 4px;
  flex-wrap: nowrap;
}

/* 弹窗通用样式 */
.role-dialog :deep(.el-dialog__body) { padding-top: 8px; }
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
.dialog-profile-code { color: #909399; font-size: 13px; }
.dialog-divider { margin: 4px 0 16px; }
.dialog-form :deep(.el-form-item) { margin-bottom: 16px; }

/* 权限配置弹窗 */
.perm-dialog :deep(.el-dialog__body) { padding-top: 8px; }
.perm-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 4px 12px;
}
.perm-role-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.perm-role-info { flex: 1; }
.perm-role-name { font-size: 18px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.perm-role-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }

.perm-tree-wrap { border: 1px solid #ebeef5; border-radius: 8px; padding: 12px; max-height: 500px; overflow-y: auto; }
.perm-tree-actions { margin-bottom: 12px; }
.tree-node { display: flex; align-items: center; justify-content: space-between; flex: 1; }
.node-buttons { display: flex; gap: 4px; margin-left: 12px; }
</style>
