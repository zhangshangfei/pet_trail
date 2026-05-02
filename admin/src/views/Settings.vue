<template>
  <div class="settings-page">
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

    <!-- 设置分组 -->
    <el-card shadow="hover" class="settings-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">系统设置</span>
            <el-tag size="small" type="info">共 {{ totalSettings }} 项</el-tag>
          </div>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" size="small" @click="addSetting">新增设置项</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <el-card shadow="hover" v-for="group in settingGroups" :key="group.key" class="group-card">
          <template #header>
            <div class="group-header">
              <span class="group-title">{{ group.label }}</span>
              <el-tag size="small" type="info">{{ group.items.length }} 项</el-tag>
            </div>
          </template>
          <el-form label-width="160px">
            <el-form-item v-for="item in group.items" :key="item.settingKey" :label="item.label || item.settingKey">
              <div class="setting-item">
                <el-switch v-if="item.type === 'switch'" v-model="settingsMap[item.settingKey]" active-value="true" inactive-value="false" />
                <el-radio-group v-else-if="item.type === 'radio'" v-model="settingsMap[item.settingKey]">
                  <el-radio v-for="opt in item.options" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
                </el-radio-group>
                <el-input-number v-else-if="item.type === 'number'" v-model="settingsMap[item.settingKey]" :min="item.min" :max="item.max" :precision="item.precision || 0" />
                <el-input v-else v-model="settingsMap[item.settingKey]" :type="item.type === 'textarea' ? 'textarea' : 'text'" :rows="item.rows || 3" :placeholder="item.placeholder || ''" />
                <el-button size="small" text type="danger" :icon="Delete" @click="removeSetting(item.settingKey)" v-if="!item.builtin" class="delete-btn">删除</el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
        <el-button type="primary" :icon="Check" @click="handleSave" :loading="saving" class="save-btn">保存设置</el-button>
      </div>
    </el-card>

    <!-- 新增设置项弹窗 -->
    <el-dialog v-model="showAddDialog" title="新增设置项" width="450px" destroy-on-close class="setting-dialog">
      <el-form :model="addForm" label-width="80px" class="dialog-form">
        <el-form-item label="键名" required><el-input v-model="addForm.key" placeholder="如 site_name" /></el-form-item>
        <el-form-item label="值"><el-input v-model="addForm.value" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="addForm.label" placeholder="显示名称（可选）" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="addForm.type">
            <el-option label="文本" value="text" /><el-option label="文本域" value="textarea" />
            <el-option label="开关" value="switch" /><el-option label="数字" value="number" />
          </el-select>
        </el-form-item>
        <el-form-item label="分组">
          <el-select v-model="addForm.group">
            <el-option v-for="g in settingGroups" :key="g.key" :label="g.label" :value="g.key" />
            <el-option label="新建分组" value="__new__" />
          </el-select>
        </el-form-item>
        <el-form-item label="新分组名" v-if="addForm.group === '__new__'">
          <el-input v-model="addForm.newGroup" placeholder="分组名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getSettings, updateSettings } from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Check } from '@element-plus/icons-vue'

const loading = ref(false)
const saving = ref(false)
const settingsMap = reactive({})
const dynamicSettings = ref([])
const showAddDialog = ref(false)
const addForm = ref({ key: '', value: '', label: '', type: 'text', group: 'general', newGroup: '' })

const builtinSettings = [
  { settingKey: 'content_audit_mode', label: '内容审核模式', type: 'radio', group: 'content', options: [{ label: '自动审核', value: 'auto' }, { label: '人工审核', value: 'manual' }] },
  { settingKey: 'audit_block_words', label: '屏蔽词列表', type: 'textarea', group: 'content', placeholder: '敏感词，逗号分隔' },
  { settingKey: 'app_version', label: '小程序版本号', type: 'text', group: 'general', placeholder: '如 1.0.0' },
  { settingKey: 'notification_enabled', label: '全局通知开关', type: 'switch', group: 'general' },
  { settingKey: 'registration_enabled', label: '新用户注册开关', type: 'switch', group: 'general' }
]

const builtinKeys = new Set(builtinSettings.map(s => s.settingKey))

const groupLabels = { general: '基本设置', content: '内容审核' }

const settingGroups = ref([])

const totalSettings = computed(() => settingGroups.value.reduce((sum, g) => sum + g.items.length, 0))

const overviewCards = computed(() => [
  { key: 'total', label: '设置项数', value: totalSettings.value, icon: 'Setting', color: '#409eff', bg: '#ecf5ff' },
  { key: 'builtin', label: '内置项', value: builtinSettings.length, icon: 'Lock', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'dynamic', label: '自定义项', value: dynamicSettings.value.length, icon: 'Edit', color: '#e6a23c', bg: '#fdf6ec' },
  { key: 'groups', label: '分组数', value: settingGroups.value.length, icon: 'Grid', color: '#f56c6c', bg: '#fef0f0' }
])

const buildGroups = () => {
  const groupMap = {}
  const allSettings = [...builtinSettings, ...dynamicSettings.value]
  for (const s of allSettings) {
    const g = s.group || 'general'
    if (!groupMap[g]) groupMap[g] = { key: g, label: groupLabels[g] || g, items: [] }
    groupMap[g].items.push(s)
  }
  settingGroups.value = Object.values(groupMap)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSettings()
    if (res.data && Array.isArray(res.data)) {
      dynamicSettings.value = []
      res.data.forEach(item => {
        settingsMap[item.settingKey] = item.settingValue
        if (!builtinKeys.has(item.settingKey)) {
          dynamicSettings.value.push({
            settingKey: item.settingKey,
            label: item.settingKey,
            type: 'text',
            group: 'general',
            builtin: false
          })
        }
      })
      builtinSettings.forEach(s => {
        if (!(s.settingKey in settingsMap)) {
          settingsMap[s.settingKey] = s.type === 'switch' ? 'true' : ''
        }
      })
      buildGroups()
    }
  } catch (e) {}
  loading.value = false
}

const handleSave = async () => {
  saving.value = true
  try {
    const settings = Object.entries(settingsMap).map(([key, value]) => ({ settingKey: key, settingValue: value }))
    await updateSettings({ settings })
    ElMessage.success('保存成功')
  } catch (e) {}
  saving.value = false
}

const addSetting = () => {
  addForm.value = { key: '', value: '', label: '', type: 'text', group: 'general', newGroup: '' }
  showAddDialog.value = true
}

const confirmAdd = () => {
  if (!addForm.value.key) {
    ElMessage.warning('请输入键名')
    return
  }
  const group = addForm.value.group === '__new__' ? addForm.value.newGroup : addForm.value.group
  settingsMap[addForm.value.key] = addForm.value.value || ''
  dynamicSettings.value.push({
    settingKey: addForm.value.key,
    label: addForm.value.label || addForm.value.key,
    type: addForm.value.type,
    group: group,
    builtin: false
  })
  if (addForm.value.group === '__new__' && addForm.value.newGroup) {
    groupLabels[addForm.value.newGroup] = addForm.value.newGroup
  }
  buildGroups()
  showAddDialog.value = false
  ElMessage.success('已添加，请点击保存')
}

const removeSetting = async (key) => {
  try {
    await ElMessageBox.confirm(`确定删除设置项"${key}"？`, '提示', { type: 'warning' })
    delete settingsMap[key]
    dynamicSettings.value = dynamicSettings.value.filter(s => s.settingKey !== key)
    buildGroups()
    ElMessage.success('已移除，请点击保存')
  } catch (e) {}
}

onMounted(() => loadData())
</script>

<style scoped>
.settings-page { padding: 0; }

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

.settings-card { min-height: 500px; }
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.header-left { display: flex; align-items: center; gap: 8px; }
.card-title { font-weight: 600; font-size: 15px; color: #303133; }
.header-actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }

.group-card { margin-bottom: 16px; }
.group-card:last-child { margin-bottom: 0; }
.group-header { display: flex; justify-content: space-between; align-items: center; }
.group-title { font-weight: 600; font-size: 14px; color: #303133; }

.setting-item {
  display: flex;
  gap: 8px;
  width: 100%;
  align-items: center;
}
.setting-item :deep(.el-input),
.setting-item :deep(.el-input-number) { flex: 1; }
.delete-btn { flex-shrink: 0; }

.save-btn { margin-top: 16px; }

/* 弹窗样式 */
.setting-dialog :deep(.el-dialog__body) { padding-top: 8px; }
.dialog-form :deep(.el-form-item) { margin-bottom: 16px; }
</style>
