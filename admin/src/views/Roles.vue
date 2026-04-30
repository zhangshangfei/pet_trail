<template>
  <div class="roles-page">
    <el-card shadow="hover">
      <template #header>
        <div class="page-header">
          <span class="card-title">角色管理</span>
          <el-button type="success" @click="openCreate">新增角色</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="code" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="primary" text @click="openPermission(row)">权限配置</el-button>
            <el-button v-if="row.status === 1" type="warning" size="small" text @click="changeStatus(row.id, 0)">禁用</el-button>
            <el-button v-if="row.status === 0" type="success" size="small" text @click="changeStatus(row.id, 1)">启用</el-button>
            <el-button type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑角色' : '新增角色'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="角色名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="角色编码" required><el-input v-model="form.code" :disabled="isEdit" placeholder="如 ADMIN, MERCHANT_ADMIN" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option label="启用" :value="1" /><el-option label="禁用" :value="0" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPermission" title="权限配置" width="650px" destroy-on-close>
      <div v-if="currentRole" style="margin-bottom: 16px;">
        <el-tag type="info">{{ currentRole.name }}</el-tag>
      </div>
      <div class="perm-tree-wrap">
        <div style="margin-bottom: 8px;">
          <el-button size="small" @click="selectAll">全选菜单</el-button>
          <el-button size="small" @click="deselectAll">清空</el-button>
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
        <el-button type="primary" @click="savePermission">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
    form.value = { name: detail.name, code: detail.code, description: detail.description, status: detail.status }
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

async function changeStatus(id, status) {
  try { await updateRoleStatus(id, status); ElMessage.success('状态更新成功'); loadData() } catch (e) { ElMessage.error('操作失败') }
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
.page-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 16px; font-weight: 600; }
.perm-tree-wrap { border: 1px solid #ebeef5; border-radius: 4px; padding: 12px; max-height: 500px; overflow-y: auto; }
.tree-node { display: flex; align-items: center; justify-content: space-between; flex: 1; }
.node-buttons { display: flex; gap: 4px; margin-left: 12px; }
</style>
