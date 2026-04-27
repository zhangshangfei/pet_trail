<template>
  <div class="sys-menus-page">
    <el-card shadow="hover">
      <template #header>
        <div class="page-header">
          <span class="card-title">菜单管理</span>
          <el-button type="success" @click="openCreate(0)">新增顶级菜单</el-button>
        </div>
      </template>

      <el-table :data="flatList" v-loading="loading" row-key="id" :tree-props="{ children: 'children' }" default-expand-all stripe>
        <el-table-column prop="name" label="菜单名称" min-width="180" />
        <el-table-column prop="path" label="路由路径" width="160" />
        <el-table-column prop="icon" label="图标" width="120" />
        <el-table-column prop="permission" label="权限码" width="140" />
        <el-table-column label="按钮权限" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="b in parseButtons(row.buttons)" :key="b" size="small" type="info" style="margin: 2px;">{{ b }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.path" size="small" text @click="openCreate(row.id)">添加子菜单</el-button>
            <el-button size="small" text @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑菜单' : '新增菜单'" width="550px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="菜单名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="路由路径"><el-input v-model="form.path" placeholder="如 /users，分组菜单留空" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="Element Plus 图标名" /></el-form-item>
        <el-form-item label="权限码"><el-input v-model="form.permission" placeholder="如 user:view" /></el-form-item>
        <el-form-item label="按钮权限">
          <div class="btn-config">
            <div v-for="(btn, idx) in form.btnList" :key="idx" class="btn-item">
              <el-input v-model="form.btnList[idx]" placeholder="按钮权限码" style="width: 200px" />
              <el-button type="danger" link @click="form.btnList.splice(idx, 1)">删除</el-button>
            </div>
            <el-button type="primary" link @click="form.btnList.push('')">+ 添加按钮</el-button>
          </div>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option label="启用" :value="1" /><el-option label="禁用" :value="0" /></el-select>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/admin'

const loading = ref(false)
const flatList = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({})

function parseButtons(btns) {
  if (!btns) return []
  return btns.split(',').map(b => b.trim()).filter(Boolean)
}

function flattenTree(tree) {
  const result = []
  for (const node of tree) {
    const item = { ...node }
    const children = item.children
    delete item.children
    if (children && children.length > 0) {
      result.push(item)
      result.push(...flattenTree(children))
    } else {
      result.push(item)
    }
  }
  return result
}

async function loadData() {
  loading.value = true
  try {
    const res = await getMenuTree()
    flatList.value = res.data || []
  } catch (e) {}
  loading.value = false
}

function openCreate(parentId) {
  isEdit.value = false; editId.value = null
  form.value = { parentId, name: '', path: '', icon: '', permission: '', btnList: [], sortOrder: 0, status: 1 }
  showDialog.value = true
}

function openEdit(row) {
  isEdit.value = true; editId.value = row.id
  form.value = {
    parentId: row.parentId, name: row.name, path: row.path || '', icon: row.icon || '',
    permission: row.permission || '', btnList: parseButtons(row.buttons), sortOrder: row.sortOrder || 0, status: row.status
  }
  showDialog.value = true
}

async function submitForm() {
  if (!form.value.name) { ElMessage.warning('菜单名称不能为空'); return }
  try {
    const data = {
      parentId: form.value.parentId || 0, name: form.value.name, path: form.value.path || null,
      icon: form.value.icon || null, permission: form.value.permission || null,
      buttons: form.value.btnList.filter(b => b.trim()).join(',') || null,
      sortOrder: form.value.sortOrder, status: form.value.status
    }
    if (isEdit.value) { await updateMenu(editId.value, data); ElMessage.success('更新成功') }
    else { await createMenu(data); ElMessage.success('创建成功') }
    showDialog.value = false; loadData()
  } catch (e) { ElMessage.error('操作失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除菜单"${row.name}"？`, '提示', { type: 'warning' })
    await deleteMenu(row.id); ElMessage.success('删除成功'); loadData()
  } catch (e) {}
}

onMounted(() => loadData())
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 16px; font-weight: 600; }
.btn-config { width: 100%; }
.btn-item { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
</style>
