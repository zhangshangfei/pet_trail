<template>
  <div class="page-container">
    <div class="page-header">
      <h2>系统配置管理</h2>
      <div class="header-actions">
        <el-select v-model="selectedCategory" placeholder="按分类筛选" clearable style="width: 160px; margin-right: 12px;" @change="loadData">
          <el-option v-for="cat in categories" :key="cat" :label="categoryLabels[cat] || cat" :value="cat" />
        </el-select>
        <el-button @click="handleRefreshCache" :loading="refreshing">刷新缓存</el-button>
        <el-button type="primary" @click="openAdd">新增配置</el-button>
      </div>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="configName" label="配置名称" width="150" />
      <el-table-column prop="configKey" label="配置键名" width="200">
        <template #default="{ row }">
          <el-text type="primary" size="small" style="font-family: monospace;">{{ row.configKey }}</el-text>
        </template>
      </el-table-column>
      <el-table-column label="配置值" min-width="200">
        <template #default="{ row }">
          <template v-if="isToggleConfig(row.configKey)">
            <el-switch
              :model-value="row.configValue === 'true' || row.configValue === '1'"
              @change="(val) => handleToggle(row, val)"
              active-text="启用"
              inactive-text="禁用"
            />
          </template>
          <template v-else-if="isSecretConfig(row.configKey)">
            <div class="secret-value">
              <span v-if="!row._showSecret">{{ maskValue(row.configValue) }}</span>
              <span v-else style="word-break: break-all;">{{ row.configValue }}</span>
              <el-button link type="primary" size="small" @click="row._showSecret = !row._showSecret">
                {{ row._showSecret ? '隐藏' : '显示' }}
              </el-button>
            </div>
          </template>
          <template v-else>
            <span style="word-break: break-all;">{{ row.configValue }}</span>
          </template>
        </template>
      </el-table-column>
      <el-table-column prop="configDesc" label="描述" min-width="180" show-overflow-tooltip />
      <el-table-column label="分类" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="categoryTagType[row.category] || 'info'">{{ categoryLabels[row.category] || row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="70" />
      <el-table-column prop="updatedAt" label="更新时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showAdd" title="新增配置项" width="550px">
      <el-form :model="addForm" label-width="100px">
        <el-form-item label="配置名称" required>
          <el-input v-model="addForm.configName" placeholder="如：AI功能开关" />
        </el-form-item>
        <el-form-item label="配置键名" required>
          <el-input v-model="addForm.configKey" placeholder="如：ai.enabled" />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input v-model="addForm.configValue" placeholder="配置项的值" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="addForm.configDesc" type="textarea" :rows="2" placeholder="配置项说明" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="addForm.category" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat" :label="categoryLabels[cat] || cat" :value="cat" />
            <el-option label="+ 新建分类" value="__new__" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="addForm.category === '__new__'" label="新分类名">
          <el-input v-model="addForm.newCategory" placeholder="输入新分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="addForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEdit" title="编辑配置项" width="550px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="配置名称">
          <el-input v-model="editForm.configName" />
        </el-form-item>
        <el-form-item label="配置键名">
          <el-input v-model="editForm.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input v-model="editForm.configValue" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.configDesc" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.category" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat" :label="categoryLabels[cat] || cat" :value="cat" />
            <el-option label="+ 新建分类" value="__new__" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="editForm.category === '__new__'" label="新分类名">
          <el-input v-model="editForm.newCategory" placeholder="输入新分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sortOrder" :min="0" :max="999" />
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
import { getConfigList, getConfigDetail, getConfigCategories, createConfig, updateConfig, deleteConfig, refreshConfigCache } from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const refreshing = ref(false)
const list = ref([])
const categories = ref([])
const selectedCategory = ref('')

const showAdd = ref(false)
const showEdit = ref(false)
const addForm = ref({ configName: '', configKey: '', configValue: '', configDesc: '', category: 'system', sortOrder: 0, newCategory: '' })
const editForm = ref({ id: null, configName: '', configKey: '', configValue: '', configDesc: '', category: '', sortOrder: 0, newCategory: '' })

const categoryLabels = {
  ai: 'AI配置',
  content: '内容管理',
  notification: '通知配置',
  registration: '注册配置',
  system: '系统基础'
}

const categoryTagType = {
  ai: 'warning',
  content: 'danger',
  notification: 'success',
  registration: '',
  system: 'info'
}

const toggleKeys = ['ai.enabled', 'notification.enabled', 'registration.enabled']
const secretKeys = ['ai.api-key']

const isToggleConfig = (key) => toggleKeys.includes(key)
const isSecretConfig = (key) => secretKeys.includes(key)

const maskValue = (val) => {
  if (!val || val.length <= 4) return '****'
  return val.substring(0, 4) + '****'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (selectedCategory.value) params.category = selectedCategory.value
    const res = await getConfigList(params)
    list.value = (res.data || []).map(item => ({ ...item, _showSecret: false }))
  } catch (e) {}
  loading.value = false
}

const loadCategories = async () => {
  try {
    const res = await getConfigCategories()
    categories.value = res.data || []
  } catch (e) {}
}

const handleToggle = async (row, val) => {
  try {
    await updateConfig(row.id, { configValue: val ? 'true' : 'false' })
    row.configValue = val ? 'true' : 'false'
    ElMessage.success('配置已更新')
  } catch (e) {}
}

const openAdd = () => {
  addForm.value = { configName: '', configKey: '', configValue: '', configDesc: '', category: 'system', sortOrder: 0, newCategory: '' }
  showAdd.value = true
}

const handleAdd = async () => {
  if (!addForm.value.configName || !addForm.value.configKey) {
    ElMessage.warning('配置名称和键名不能为空')
    return
  }
  try {
    const data = { ...addForm.value }
    if (data.category === '__new__' && data.newCategory) {
      data.category = data.newCategory
    }
    delete data.newCategory
    await createConfig(data)
    ElMessage.success('新增成功')
    showAdd.value = false
    loadCategories()
    loadData()
  } catch (e) {}
}

const openEdit = async (row) => {
  try {
    const res = await getConfigDetail(row.id)
    const detail = res.data || row
    editForm.value = {
      id: detail.id,
      configName: detail.configName,
      configKey: detail.configKey,
      configValue: detail.configValue || '',
      configDesc: detail.configDesc || '',
      category: detail.category || 'system',
      sortOrder: detail.sortOrder || 0,
      newCategory: ''
    }
    showEdit.value = true
  } catch (e) {}
}

const handleEdit = async () => {
  try {
    const data = { ...editForm.value }
    if (data.category === '__new__' && data.newCategory) {
      data.category = data.newCategory
    }
    delete data.newCategory
    delete data.id
    await updateConfig(editForm.value.id, data)
    ElMessage.success('编辑成功')
    showEdit.value = false
    loadCategories()
    loadData()
  } catch (e) {}
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除配置"${row.configName}"（${row.configKey}）？此操作不可恢复。`, '确认删除', { type: 'warning' })
    await deleteConfig(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

const handleRefreshCache = async () => {
  refreshing.value = true
  try {
    await refreshConfigCache()
    ElMessage.success('缓存已刷新')
  } catch (e) {}
  refreshing.value = false
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; align-items: center; }
.secret-value { display: flex; align-items: center; gap: 8px; }
</style>
