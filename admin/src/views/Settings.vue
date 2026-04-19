<template>
  <div class="page-container">
    <div class="page-header"><h2>系统设置</h2></div>
    <div v-loading="loading" style="max-width: 600px;">
      <el-form label-width="140px">
        <el-form-item label="内容审核模式">
          <el-radio-group v-model="form.content_audit_mode">
            <el-radio value="auto">自动审核</el-radio>
            <el-radio value="manual">人工审核</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="屏蔽词列表">
          <el-input v-model="form.audit_block_words" type="textarea" :rows="3" placeholder="敏感词，逗号分隔" />
        </el-form-item>
        <el-form-item label="小程序版本号">
          <el-input v-model="form.app_version" placeholder="如 1.0.0" />
        </el-form-item>
        <el-form-item label="全局通知开关">
          <el-switch v-model="form.notification_enabled" active-value="true" inactive-value="false" />
        </el-form-item>
        <el-form-item label="新用户注册开关">
          <el-switch v-model="form.registration_enabled" active-value="true" inactive-value="false" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存设置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSettings, updateSettings } from '../api/admin'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const form = ref({
  content_audit_mode: 'auto',
  audit_block_words: '',
  app_version: '1.0.0',
  notification_enabled: 'true',
  registration_enabled: 'true'
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSettings()
    if (res.data && Array.isArray(res.data)) {
      res.data.forEach(item => {
        if (form.value.hasOwnProperty(item.settingKey)) {
          form.value[item.settingKey] = item.settingValue
        }
      })
    }
  } catch (e) {}
  loading.value = false
}

const handleSave = async () => {
  saving.value = true
  try {
    const settings = Object.entries(form.value).map(([key, value]) => ({ settingKey: key, settingValue: value }))
    await updateSettings({ settings })
    ElMessage.success('保存成功')
  } catch (e) {}
  saving.value = false
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
</style>
