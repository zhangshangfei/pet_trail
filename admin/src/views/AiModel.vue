<template>
  <div class="page-container">
    <el-card shadow="hover" class="dashboard-section" v-if="dashboard">
      <template #header>
        <div class="page-header">
          <span class="card-title">AI模型管理</span>
          <div class="header-actions">
            <el-button @click="loadDashboard" :loading="loading">刷新</el-button>
            <el-button type="primary" @click="openAdd" v-if="canManage">新增模型</el-button>
          </div>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card current-model">
            <div class="stat-label">当前活动模型</div>
            <div class="stat-value">{{ dashboard.currentModel?.displayName || '未配置' }}</div>
            <div class="stat-sub">{{ dashboard.currentModel?.provider || '' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-label">总调用次数</div>
            <div class="stat-value">{{ dashboard.totalCalls || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-label">成功率</div>
            <div class="stat-value" :class="successRate >= 90 ? 'text-success' : successRate >= 70 ? 'text-warning' : 'text-danger'">
              {{ successRate }}%
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-label">平均耗时</div>
            <div class="stat-value">{{ Math.round(dashboard.avgResponseTime || 0) }}ms</div>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="16" style="margin-top: 16px">
        <el-col :span="8">
          <div class="stat-card mini">
            <div class="stat-label">成功调用</div>
            <div class="stat-value text-success">{{ dashboard.successCalls || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card mini">
            <div class="stat-label">失败调用</div>
            <div class="stat-value text-danger">{{ dashboard.failedCalls || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card mini">
            <div class="stat-label">可用模型数</div>
            <div class="stat-value">{{ dashboard.availableModels?.length || 0 }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="hover" class="cache-section" v-if="cacheInfo">
      <div class="section-header-inline">
        <h3>模型缓存状态</h3>
        <div>
          <el-button size="small" @click="loadCacheInfo">刷新</el-button>
          <el-button size="small" type="warning" @click="clearCache">清空缓存</el-button>
          <el-button size="small" type="primary" @click="persistCache" v-if="canManage">持久化</el-button>
        </div>
      </div>
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="当前模型">{{ cacheInfo.currentModelId || '未配置' }}</el-descriptions-item>
        <el-descriptions-item label="缓存模型数">{{ cacheInfo.modelCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="最后更新">{{ cacheInfo.lastUpdated || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <div class="page-header">
          <span class="card-title">模型列表</span>
        </div>
      </template>
      <el-table :data="modelList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="模型" min-width="200">
          <template #default="{ row }">
            <div class="model-info-cell">
              <el-icon class="model-icon"><Cpu /></el-icon>
              <div>
                <div class="model-name">{{ row.displayName }}</div>
                <div class="model-id">{{ row.modelName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="provider" label="提供商" width="100" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'" size="small">{{ row.isActive ? '活动中' : '未激活' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="调用统计" width="160">
          <template #default="{ row }">
            <div class="stats-cell">
              <span>调用: {{ row.callCount || 0 }}</span>
              <span class="text-success">成功: {{ row.successCount || 0 }}</span>
              <span class="text-danger">失败: {{ row.failCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="平均耗时" width="100">
          <template #default="{ row }">{{ row.avgResponseTime ? Math.round(row.avgResponseTime) + 'ms' : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="openEdit(row)" v-if="canManage">编辑</el-button>
            <el-button size="small" text @click="openParams(row)">参数</el-button>
            <el-button size="small" text @click="openStatsDetail(row)">统计</el-button>
            <el-button size="small" text type="success" @click="handleSwitch(row)" :disabled="row.isActive" v-if="canManage">切换</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row)" :disabled="row.isActive" v-if="canManage">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="hover" style="margin-top: 16px">
      <template #header>
        <div class="page-header">
          <span class="card-title">模型切换日志</span>
          <el-button size="small" @click="loadSwitchLogs">刷新日志</el-button>
        </div>
      </template>
      <el-table :data="switchLogs" border stripe size="small">
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column prop="fromModelName" label="原模型" width="150" />
        <el-table-column prop="toModelName" label="新模型" width="150" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
      </el-table>
      <div class="pagination-wrap" v-if="switchLogs.length > 0">
        <el-pagination small layout="prev, pager, next" :total="switchLogs.length" :page-size="10" />
      </div>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑模型' : '新增模型'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="显示名称" required><el-input v-model="form.displayName" /></el-form-item>
        <el-form-item label="模型名称" required><el-input v-model="form.modelName" placeholder="如 gpt-4o" /></el-form-item>
        <el-form-item label="提供商" required>
          <el-select v-model="form.provider" placeholder="选择提供商">
            <el-option label="OpenAI" value="openai" /><el-option label="Anthropic" value="anthropic" />
            <el-option label="Google" value="google" /><el-option label="Azure" value="azure" />
            <el-option label="本地" value="local" /><el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本"><el-input v-model="form.version" /></el-form-item>
        <el-form-item label="API密钥"><el-input v-model="form.apiKey" type="password" show-password placeholder="留空则保持不变" /></el-form-item>
        <el-form-item label="BaseURL"><el-input v-model="form.baseUrl" placeholder="可选" /></el-form-item>
        <el-form-item label="最大Token"><el-input-number v-model="form.maxTokens" :min="1" :max="128000" /></el-form-item>
        <el-form-item label="超时(ms)"><el-input-number v-model="form.timeout" :min="1000" :max="120000" :step="1000" /></el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.isActive" />
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitModel" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showParamsDialog" title="模型参数配置" width="500px">
      <div class="params-hint">参数以 JSON 格式存储，key 为参数名，value 为默认值</div>
      <div class="params-editor">
        <div v-for="(param, index) in paramList" :key="index" class="param-item">
          <el-input v-model="param.key" placeholder="参数名" style="width: 140px" />
          <el-input v-model="param.value" placeholder="默认值" style="flex: 1" />
          <el-button type="danger" link @click="paramList.splice(index, 1)">删除</el-button>
        </div>
        <el-button type="primary" link @click="paramList.push({ key: '', value: '' })">+ 添加参数</el-button>
      </div>
      <template #footer>
        <el-button @click="showParamsDialog = false">取消</el-button>
        <el-button type="primary" @click="submitParams" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showStatsDialog" title="调用统计详情" width="500px">
      <el-descriptions :column="1" border v-if="statsDetail">
        <el-descriptions-item label="模型">{{ statsDetail.displayName }}</el-descriptions-item>
        <el-descriptions-item label="总调用">{{ statsDetail.callCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="成功">{{ statsDetail.successCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="失败">{{ statsDetail.failCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="成功率">{{ statsDetail.callCount ? Math.round((statsDetail.successCount / statsDetail.callCount) * 100) : 0 }}%</el-descriptions-item>
        <el-descriptions-item label="平均耗时">{{ statsDetail.avgResponseTime ? Math.round(statsDetail.avgResponseTime) + 'ms' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="最大耗时">{{ statsDetail.maxResponseTime ? Math.round(statsDetail.maxResponseTime) + 'ms' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="最小耗时">{{ statsDetail.minResponseTime ? Math.round(statsDetail.minResponseTime) + 'ms' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="最后调用">{{ statsDetail.lastCallTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/store/admin'
import {
  getAiModelList, getAiModelDetail, createAiModel, updateAiModel, deleteAiModel,
  getAiModelDashboard, switchAiModel, getAiModelSwitchLogs, getAiModelCacheInfo,
  clearAiModelCache, persistAiModelCache, updateAiModelParams
} from '../api/admin'

const adminStore = useAdminStore()
const canManage = computed(() => adminStore.hasButton('ai-model:manage'))

const loading = ref(false)
const modelList = ref([])
const dashboard = ref(null)
const switchLogs = ref([])
const cacheInfo = ref(null)

const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const form = ref({
  displayName: '', modelName: '', provider: 'openai', version: '',
  apiKey: '', baseUrl: '', maxTokens: 4096, timeout: 30000,
  isActive: false, description: ''
})

const showParamsDialog = ref(false)
const paramList = ref([])
const paramModelId = ref(null)

const showStatsDialog = ref(false)
const statsDetail = ref(null)

const successRate = computed(() => {
  const d = dashboard.value
  if (!d || !d.totalCalls) return 0
  return Math.round((d.successCalls / d.totalCalls) * 100)
})

const loadDashboard = async () => {
  loading.value = true
  try {
    const res = await getAiModelDashboard()
    dashboard.value = res.data || null
  } catch (e) {}
  loading.value = false
}

const loadModels = async () => {
  loading.value = true
  try {
    const res = await getAiModelList()
    modelList.value = res.data || []
  } catch (e) {}
  loading.value = false
}

const loadSwitchLogs = async () => {
  try {
    const res = await getAiModelSwitchLogs()
    switchLogs.value = res.data || []
  } catch (e) {}
}

const loadCacheInfo = async () => {
  try {
    const res = await getAiModelCacheInfo()
    cacheInfo.value = res.data || null
  } catch (e) {}
}

const openAdd = () => {
  isEdit.value = false; editId.value = null
  form.value = {
    displayName: '', modelName: '', provider: 'openai', version: '',
    apiKey: '', baseUrl: '', maxTokens: 4096, timeout: 30000,
    isActive: false, description: ''
  }
  showDialog.value = true
}

const openEdit = async (row) => {
  try {
    const res = await getAiModelDetail(row.id)
    const detail = res.data || row
    isEdit.value = true; editId.value = detail.id
    form.value = {
      displayName: detail.displayName || '', modelName: detail.modelName || '',
      provider: detail.provider || 'openai', version: detail.version || '',
      apiKey: '', baseUrl: detail.baseUrl || '',
      maxTokens: detail.maxTokens || 4096, timeout: detail.timeout || 30000,
      isActive: detail.isActive || false, description: detail.description || ''
    }
    showDialog.value = true
  } catch (e) {}
}

const submitModel = async () => {
  if (!form.value.displayName || !form.value.modelName || !form.value.provider) {
    ElMessage.warning('请填写必填项')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateAiModel(editId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createAiModel(form.value)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    await Promise.all([loadModels(), loadDashboard(), loadCacheInfo()])
  } catch (e) {
    ElMessage.error('保存失败')
  }
  submitting.value = false
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除模型 "${row.displayName}" 吗？`, '确认删除', { type: 'warning' })
    await deleteAiModel(row.id)
    ElMessage.success('删除成功')
    await Promise.all([loadModels(), loadDashboard(), loadCacheInfo()])
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleSwitch = async (row) => {
  try {
    await ElMessageBox.confirm(`确定切换到模型 "${row.displayName}" 吗？`, '确认切换', { type: 'warning' })
    await switchAiModel(row.id)
    ElMessage.success('切换成功')
    await Promise.all([loadDashboard(), loadModels(), loadSwitchLogs(), loadCacheInfo()])
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('切换失败')
  }
}

const openParams = async (row) => {
  paramModelId.value = row.id
  paramList.value = []
  try {
    const res = await getAiModelDetail(row.id)
    if (res.data?.params) {
      const params = typeof res.data.params === 'string' ? JSON.parse(res.data.params) : res.data.params
      paramList.value = Object.entries(params).map(([k, v]) => ({ key: k, value: String(v) }))
    }
  } catch (e) {}
  showParamsDialog.value = true
}

const submitParams = async () => {
  const params = {}
  for (const p of paramList.value) {
    if (p.key.trim()) {
      const val = p.value.trim()
      params[p.key.trim()] = isNaN(Number(val)) ? val : Number(val)
    }
  }
  submitting.value = true
  try {
    await updateAiModelParams(paramModelId.value, { params })
    ElMessage.success('参数更新成功')
    showParamsDialog.value = false
  } catch (e) {
    ElMessage.error('参数更新失败')
  }
  submitting.value = false
}

const openStatsDetail = async (row) => {
  try {
    const res = await getAiModelDetail(row.id)
    statsDetail.value = res.data || row
    showStatsDialog.value = true
  } catch (e) {}
}

const clearCache = async () => {
  try {
    await ElMessageBox.confirm('确定清空模型缓存吗？', '确认', { type: 'warning' })
    await clearAiModelCache()
    ElMessage.success('缓存已清空')
    loadCacheInfo()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('清空失败')
  }
}

const persistCache = async () => {
  try {
    await persistAiModelCache()
    ElMessage.success('持久化成功')
  } catch (e) {
    ElMessage.error('持久化失败')
  }
}

onMounted(() => {
  loadDashboard()
  loadSwitchLogs()
  loadModels()
  loadCacheInfo()
})
</script>

<style scoped>
.page-container { padding: 0; }

.dashboard-section { margin-bottom: 16px; }
.stat-card { background: #fff; border-radius: 8px; padding: 16px 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.stat-card.mini { padding: 12px 16px; }
.stat-card.mini .stat-value { font-size: 20px; }
.cache-section { margin-bottom: 16px; }
.section-header-inline { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.section-header-inline h3 { margin: 0; font-size: 15px; }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: 700; color: #303133; }
.stat-sub { font-size: 12px; color: #909399; margin-top: 4px; }
.current-model { border-left: 3px solid #409eff; }
.text-success { color: #67c23a; }
.text-warning { color: #e6a23c; }
.text-danger { color: #f56c6c; }
.text-primary { color: #409eff; font-weight: 600; }

.model-info-cell { display: flex; align-items: center; gap: 8px; }
.model-icon { font-size: 24px; }
.model-name { font-weight: 600; font-size: 14px; }
.model-id { font-size: 12px; color: #909399; font-family: monospace; }

.stats-cell { display: flex; flex-direction: column; font-size: 12px; line-height: 1.6; }

.params-editor { display: flex; flex-direction: column; gap: 12px; }
.params-hint { margin-bottom: 8px; }
.param-item { display: flex; align-items: center; gap: 8px; }
</style>
