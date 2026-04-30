<template>
  <div class="sys-menus-page">
    <div class="page-header-bar">
      <div class="header-left">
        <h2 class="page-title">菜单管理</h2>
        <span class="page-subtitle">管理系统导航菜单结构、权限码及按钮权限配置，支持拖拽排序</span>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate(0)">新增顶级菜单</el-button>
    </div>

    <div class="menu-tree-wrapper">
      <el-tree
        ref="treeRef"
        :data="treeList"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
        :allow-drop="allowDrop"
        draggable
        class="menu-tree"
        @node-drop="onNodeDrop"
      >
        <template #default="{ node, data }">
          <div class="menu-tree-node" :class="{ 'is-group': !data.path, 'is-page': data.path, 'is-disabled': Number(data.status ?? 1) !== 1 }">
            <div class="node-main">
              <div class="node-icon" :class="{ 'is-group': !data.path }">
                <el-icon v-if="data.icon" :size="16">
                  <component :is="data.icon" />
                </el-icon>
                <el-icon v-else :size="16">
                  <Folder v-if="!data.path" />
                  <Document v-else />
                </el-icon>
              </div>
              <div class="node-content">
                <div class="node-title-row">
                  <span class="node-name">{{ data.name }}</span>
                  <el-tag v-if="!data.path" size="small" type="warning" effect="light" class="type-tag">目录</el-tag>
                  <el-tag v-else size="small" type="success" effect="light" class="type-tag">页面</el-tag>
                  <el-switch
                    :model-value="Number(data.status ?? 1) === 1"
                    size="small"
                    inline-prompt
                    active-text="启"
                    inactive-text="停"
                    @change="(val) => handleToggleStatus(data, val)"
                  />
                </div>
                <div class="node-meta-row">
                  <span v-if="data.path" class="meta-item">
                    <el-icon><Link /></el-icon>
                    <span class="meta-text">{{ data.path }}</span>
                  </span>
                  <span v-if="data.permission" class="meta-item">
                    <el-icon><Lock /></el-icon>
                    <span class="meta-text">{{ data.permission }}</span>
                  </span>
                  <span v-if="parseButtons(data.buttons).length" class="meta-item">
                    <el-icon><Mouse /></el-icon>
                    <div class="btn-tag-list">
                      <el-tag
                        v-for="b in parseButtons(data.buttons)"
                        :key="b"
                        size="small"
                        type="info"
                        effect="plain"
                      >{{ b }}</el-tag>
                    </div>
                  </span>
                  <span class="meta-item">
                    <el-icon><Sort /></el-icon>
                    <span class="meta-text">排序 {{ data.sortOrder }}</span>
                  </span>
                </div>
              </div>
            </div>
            <div class="node-actions">
              <el-tooltip v-if="!data.path" content="添加子菜单" placement="top">
                <el-button circle size="small" @click="openCreate(data.id)">
                  <el-icon><Plus /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <el-button circle size="small" type="primary" plain @click="openEdit(data)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button circle size="small" type="danger" plain @click="handleDelete(data)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-tree>
    </div>

    <el-empty v-if="!loading && !treeList.length" description="暂无菜单数据" />

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
import { Plus, Edit, Delete, Folder, Document, Link, Lock, Mouse, Sort } from '@element-plus/icons-vue'
import { getMenuTree, getMenuDetail, createMenu, updateMenu, deleteMenu, batchSortMenus, updateMenuStatus } from '@/api/admin'

const loading = ref(false)
const treeList = ref([])
const treeRef = ref(null)
const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({})

function parseButtons(btns) {
  if (!btns) return []
  if (Array.isArray(btns)) return btns
  return String(btns).split(',').map(b => b.trim()).filter(Boolean)
}

function allowDrop(draggingNode, dropNode, type) {
  if (type === 'inner') {
    return !dropNode.data.path
  }
  return true
}

function collectSortData(nodes, parentId) {
  const result = []
  if (!nodes) return result
  nodes.forEach((node, index) => {
    result.push({ id: node.id, sortOrder: index, parentId })
    if (node.children && node.children.length > 0) {
      result.push(...collectSortData(node.children, node.id))
    }
  })
  return result
}

async function onNodeDrop() {
  try {
    const sortData = collectSortData(treeList.value, 0)
    await batchSortMenus(sortData)
    ElMessage.success('排序已保存')
  } catch (e) {
    ElMessage.error('排序保存失败')
    loadData()
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getMenuTree()
    treeList.value = res.data || []
  } catch (e) {}
  loading.value = false
}

function openCreate(parentId) {
  isEdit.value = false; editId.value = null
  form.value = { parentId, name: '', path: '', icon: '', permission: '', btnList: [], sortOrder: 0, status: 1 }
  showDialog.value = true
}

async function openEdit(row) {
  try {
    const res = await getMenuDetail(row.id)
    const detail = res.data || row
    isEdit.value = true; editId.value = detail.id
    form.value = {
      parentId: detail.parentId, name: detail.name, path: detail.path || '', icon: detail.icon || '',
      permission: detail.permission || '', btnList: parseButtons(detail.buttons), sortOrder: detail.sortOrder || 0, status: detail.status
    }
    showDialog.value = true
  } catch (e) {}
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

async function handleToggleStatus(row, val) {
  try {
    await updateMenuStatus(row.id, val ? 1 : 0)
    row.status = val ? 1 : 0
    ElMessage.success(val ? '已启用' : '已禁用')
  } catch (e) {
    ElMessage.error('状态更新失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.sys-menus-page {
  padding: 20px;
}

.page-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.page-subtitle {
  font-size: 13px;
  color: #909399;
}

.menu-tree-wrapper {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  padding: 16px;
}

.menu-tree :deep(.el-tree-node__content) {
  height: auto;
  padding: 8px 0;
}

.menu-tree :deep(.el-tree-node__expand-icon) {
  font-size: 16px;
  color: #909399;
}

.menu-tree :deep(.is-dragging) {
  opacity: 0.5;
}

.menu-tree :deep(.el-tree-node.is-drop-inner > .el-tree-node__content) {
  background-color: #ecf5ff;
}

.menu-tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 10px 12px;
  border-radius: 8px;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.menu-tree-node:hover {
  background: #f5f7fa;
  border-color: #e4e7ed;
}

.menu-tree-node.is-group {
  background: #fafbfc;
}

.menu-tree-node.is-group:hover {
  background: #f0f2f5;
  border-color: #dcdfe6;
}

.menu-tree-node.is-disabled {
  opacity: 0.55;
}

.menu-tree-node.is-disabled .node-name {
  text-decoration: line-through;
  color: #909399;
}

.menu-tree-node.is-disabled .node-icon {
  background: #c0c4cc;
}

.menu-tree-node.is-disabled .node-icon.is-group {
  background: #c0c4cc;
}

.node-main {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
}

.node-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409eff 0%, #1677ff 100%);
  border-radius: 8px;
  color: #fff;
  flex-shrink: 0;
  margin-top: 2px;
}

.node-icon.is-group {
  background: linear-gradient(135deg, #e6a23c 0%, #f56c6c 100%);
}

.node-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.node-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.node-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.type-tag {
  font-size: 11px;
  height: 20px;
  padding: 0 6px;
}

.status-tag {
  font-size: 11px;
  height: 20px;
  padding: 0 6px;
}

.node-meta-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 13px;
}

.meta-item .el-icon {
  color: #909399;
  font-size: 14px;
}

.meta-text {
  font-family: 'Courier New', monospace;
  color: #606266;
}

.btn-tag-list {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.node-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s;
  padding-left: 16px;
}

.menu-tree-node:hover .node-actions {
  opacity: 1;
}

.btn-config { width: 100%; }
.btn-item { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
</style>
