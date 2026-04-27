<template>
  <div class="page-container">
    <div class="page-header">
      <h2>系统设置</h2>
      <el-button type="primary" size="small" @click="addSetting">新增设置项</el-button>
    </div>
    <div v-loading="loading">
      <el-card shadow="hover" v-for="group in settingGroups" :key="group.key" style="margin-bottom: 16px;">
        <template #header>
          <span style="font-weight: 600;">{{ group.label }}</span>
        </template>
        <el-form label-width="160px">
          <el-form-item v-for="item in group.items" :key="item.settingKey" :label="item.label || item.settingKey">
            <div style="display: flex; gap: 8px; width: 100%; align-items: center;">
              <el-switch v-if="item.type === 'switch'" v-model="settingsMap[item.settingKey]" active-value="true" inactive-value="false" />
              <el-radio-group v-else-if="item.type === 'radio'" v-model="settingsMap[item.settingKey]">
                <el-radio v-for="opt in item.options" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
              </el-radio-group>
              <el-input-number v-else-if="item.type === 'number'" v-model="settingsMap[item.settingKey]" :min="item.min" :max="item.max" :precision="item.precision || 0" />
              <el-input v-else v-model="settingsMap[item.settingKey]" :type="item.type === 'textarea' ? 'textarea' : 'text'" :rows="item.rows || 3" :placeholder="item.placeholder || ''" />
              <el-button size="small" text type="danger" @click="removeSetting(item.settingKey)" v-if="!item.builtin" style="flex-shrink: 0;">删除</el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
      <el-button type="primary" @click="handleSave" :loading="saving" style="margin-top: 8px;">保存设置</el-button>
    </div>

    <el-dialog v-model="showAddDialog" title="新增设置项" width="450px">
      <el-form :model="addForm" label-width="80px">
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
import { ref, reactive, onMounted } from 'vue'
import { getSettings, updateSettings } from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

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
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
</style>
