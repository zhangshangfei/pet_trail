<template>
  <div class="page-container">
    <div class="page-header">
      <h2>AI模型管理</h2>
      <div class="header-actions">
        <el-button @click="loadDashboard" :loading="loading">刷新</el-button>
        <el-button type="primary" @click="openAdd" v-if="isSuperAdmin">新增模型</el-button>
      </div>
    </div>

    <div class="dashboard-section" v-if="dashboard">
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
            <div class="stat-label">平均响应时间</div>
            <div class="stat-value">{{ dashboard.avgResponseTime ? Math.round(dashboard.avgResponseTime) : 0 }}ms</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="cache-section" v-if="cacheStats">
      <div class="section-header-inline">
        <h3>分析缓存状态</h3>
        <div style="display: flex; gap: 8px;">
          <el-button size="small" type="primary" @click="handleRefreshCache" v-if="isSuperAdmin">刷新模型缓存</el-button>
          <el-button size="small" @click="showClearPetCache = true" v-if="isSuperAdmin">按宠物清除</el-button>
          <el-button size="small" @click="handleFlushStats" v-if="isSuperAdmin">持久化统计</el-button>
          <el-button size="small" type="danger" @click="handleClearCache" v-if="isSuperAdmin">清除全部缓存</el-button>
        </div>
      </div>
      <el-row :gutter="16">
        <el-col :span="4">
          <div class="stat-card mini">
            <div class="stat-label">缓存条目</div>
            <div class="stat-value">{{ cacheStats.activeEntries || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card mini">
            <div class="stat-label">缓存命中率</div>
            <div class="stat-value text-success">{{ cacheStats.hitRate || '0%' }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card mini">
            <div class="stat-label">命中次数</div>
            <div class="stat-value">{{ cacheStats.hitCount || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card mini">
            <div class="stat-label">未命中次数</div>
            <div class="stat-value">{{ cacheStats.missCount || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card mini">
            <div class="stat-label">过期清除</div>
            <div class="stat-value">{{ cacheStats.evictCount || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card mini">
            <div class="stat-label">缓存有效期</div>
            <div class="stat-value">{{ cacheStats.ttlMinutes || 30 }}分钟</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-table :data="models" v-loading="loading" border stripe style="margin-bottom: 24px;">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="模型" min-width="200">
        <template #default="{ row }">
          <div class="model-info-cell">
            <span class="model-icon">{{ row.icon || '🤖' }}</span>
            <div>
              <div class="model-name">{{ row.displayName }}</div>
              <div class="model-id">{{ row.modelName }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="provider" label="提供商" width="120">
        <template #default="{ row }">
          <el-tag size="small" :type="providerTagType[row.provider] || 'info'">{{ providerLabels[row.provider] || row.provider }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="版本" width="80">
        <template #default="{ row }">{{ row.modelVersion || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            @change="(val) => handleToggleStatus(row, val)"
            :disabled="!isSuperAdmin || (row.isActive && val === false)"
            active-text="启用"
            inactive-text="禁用"
          />
        </template>
      </el-table-column>
      <el-table-column label="活动" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isActive" type="success" size="small" effect="dark">当前</el-tag>
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
          <el-button size="small" type="primary" @click="openEdit(row)" v-if="isSuperAdmin">编辑</el-button>
          <el-button size="small" @click="openParams(row)">参数</el-button>
          <el-button size="small" @click="openStatsDetail(row)">统计</el-button>
          <el-button size="small" type="success" @click="handleSwitch(row)" :disabled="row.isActive" v-if="isSuperAdmin">切换</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)" :disabled="row.isActive" v-if="isSuperAdmin">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="switch-log-section">
      <div class="section-header">
        <h3>模型切换日志</h3>
        <el-button size="small" @click="loadSwitchLogs">刷新日志</el-button>
      </div>
      <el-table :data="switchLogs" border stripe size="small">
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column label="切换" min-width="200">
          <template #default="{ row }">
            <span>{{ row.fromModelName || '无' }}</span>
            <el-icon style="margin: 0 6px; color: #409eff;"><Right /></el-icon>
            <span class="text-primary">{{ row.toModelName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="switchType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.switchType === 'manual' ? '' : 'warning'">{{ row.switchType === 'manual' ? '手动' : '自动' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="reason" label="原因" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'success' ? 'success' : 'danger'">{{ row.status === 'success' ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="80">
          <template #default="{ row }">{{ row.duration ? row.duration + 'ms' : '-' }}</template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showAdd" title="新增AI模型" width="600px">
      <el-form :model="addForm" label-width="100px">
        <el-form-item label="模型标识" required>
          <el-input v-model="addForm.modelName" placeholder="如 deepseek/deepseek-chat" />
        </el-form-item>
        <el-form-item label="显示名称" required>
          <el-input v-model="addForm.displayName" placeholder="如 DeepSeek 智能分析" />
        </el-form-item>
        <el-form-item label="提供商" required>
          <el-select v-model="addForm.provider" style="width: 100%">
            <el-option label="OpenRouter" value="openrouter" />
            <el-option label="智谱AI" value="zhipu" />
            <el-option label="OpenAI" value="openai" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="API地址" required>
          <el-input v-model="addForm.baseUrl" placeholder="如 https://openrouter.ai/api/v1" />
        </el-form-item>
        <el-form-item label="API密钥" required>
          <el-input v-model="addForm.apiKey" type="password" show-password placeholder="API Key" />
        </el-form-item>
        <el-form-item label="模型版本">
          <el-input v-model="addForm.modelVersion" placeholder="如 v3" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="addForm.icon" placeholder="emoji图标，如 🧠" style="width: 100px" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="addForm.description" type="textarea" :rows="2" placeholder="模型描述" />
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

    <el-dialog v-model="showEdit" title="编辑AI模型" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="模型标识">
          <el-input v-model="editForm.modelName" disabled />
        </el-form-item>
        <el-form-item label="显示名称">
          <el-input v-model="editForm.displayName" />
        </el-form-item>
        <el-form-item label="提供商">
          <el-select v-model="editForm.provider" style="width: 100%">
            <el-option label="OpenRouter" value="openrouter" />
            <el-option label="智谱AI" value="zhipu" />
            <el-option label="OpenAI" value="openai" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="API地址">
          <el-input v-model="editForm.baseUrl" />
        </el-form-item>
        <el-form-item label="API密钥">
          <el-input v-model="editForm.apiKey" type="password" show-password placeholder="留空则不修改" />
        </el-form-item>
        <el-form-item label="模型版本">
          <el-input v-model="editForm.modelVersion" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="editForm.icon" style="width: 100px" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="2" />
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

    <el-dialog v-model="showParams" title="模型参数配置" width="600px">
      <div class="params-editor">
        <div class="params-hint">
          <el-alert type="info" :closable="false" show-icon>
            配置模型调用参数，如 temperature、max_tokens、top_p 等。参数将以JSON格式传递给API。
          </el-alert>
        </div>
        <div v-for="(param, index) in paramList" :key="index" class="param-item">
          <el-input v-model="param.key" placeholder="参数名" style="width: 200px" />
          <el-input v-model="param.value" placeholder="参数值" style="flex: 1" />
          <el-button type="danger" link @click="paramList.splice(index, 1)">删除</el-button>
        </div>
        <el-button type="primary" link @click="paramList.push({ key: '', value: '' })">+ 添加参数</el-button>
      </div>
      <template #footer>
        <el-button @click="showParams = false">取消</el-button>
        <el-button type="primary" @click="handleSaveParams">保存参数</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showStatsDetail" title="模型性能统计" width="700px">
      <div v-if="statsDetail" class="stats-detail">
        <el-row :gutter="16" style="margin-bottom: 20px;">
          <el-col :span="6">
            <div class="stat-card mini">
              <div class="stat-label">总调用</div>
              <div class="stat-value">{{ statsDetail.total_calls || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card mini">
              <div class="stat-label">成功率</div>
              <div class="stat-value text-success">{{ statsDetail.success_rate || 0 }}%</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card mini">
              <div class="stat-label">成功/失败</div>
              <div class="stat-value"><span class="text-success">{{ statsDetail.success_calls || 0 }}</span> / <span class="text-danger">{{ statsDetail.failed_calls || 0 }}</span></div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card mini">
              <div class="stat-label">平均耗时</div>
              <div class="stat-value">{{ statsDetail.avg_response_time ? Math.round(statsDetail.avg_response_time) : 0 }}ms</div>
            </div>
          </el-col>
        </el-row>
        <h4 style="margin: 0 0 12px;">每日统计</h4>
        <el-table :data="dailyStats" border stripe size="small" max-height="300">
          <el-table-column prop="stats_date" label="日期" width="120" />
          <el-table-column prop="call_count" label="调用次数" width="90" />
          <el-table-column prop="success_count" label="成功" width="70" />
          <el-table-column prop="fail_count" label="失败" width="70" />
          <el-table-column prop="success_rate" label="成功率" width="80">
            <template #default="{ row }">{{ row.success_rate }}%</template>
          </el-table-column>
          <el-table-column prop="avg_response_time" label="平均耗时" width="90">
            <template #default="{ row }">{{ row.avg_response_time ? Math.round(row.avg_response_time) + 'ms' : '-' }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog v-model="showClearPetCache" title="按宠物清除缓存" width="400px">
      <el-form label-width="80px">
        <el-form-item label="宠物ID">
          <el-input-number v-model="clearPetId" :min="1" :controls="false" style="width: 100%" placeholder="输入宠物ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showClearPetCache = false">取消</el-button>
        <el-button type="primary" @click="handleClearPetCache">确认清除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAdminStore } from '@/store/admin'
import {
  getAiModelList, getAiModelDetail, getAiModelDashboard, createAiModel, updateAiModel,
  setAiModelStatus, switchAiModel, deleteAiModel, getAiModelSwitchLogs,
  getAiModelParameters, updateAiModelParameters, getAiModelCacheStats, clearAiModelAllCache,
  clearAiModelPetCache, refreshAiModelCache,
  getAiModelStats, getAiModelDailyStats, flushAiModelStats
} from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Right } from '@element-plus/icons-vue'

const loading = ref(false)
const models = ref([])
const dashboard = ref(null)
const switchLogs = ref([])
const cacheStats = ref(null)
const showAdd = ref(false)
const showEdit = ref(false)
const showParams = ref(false)
const currentParamsModelId = ref(null)
const paramList = ref([])
const showStatsDetail = ref(false)
const statsDetail = ref(null)
const dailyStats = ref([])
const currentStatsModelId = ref(null)
const showClearPetCache = ref(false)
const clearPetId = ref(null)

const addForm = ref({
  modelName: '', displayName: '', provider: 'openrouter', baseUrl: '',
  apiKey: '', modelVersion: '', icon: '🤖', description: '', sortOrder: 0
})
const editForm = ref({
  id: null, modelName: '', displayName: '', provider: '', baseUrl: '',
  apiKey: '', modelVersion: '', icon: '', description: '', sortOrder: 0
})

const adminStore = useAdminStore()
const isSuperAdmin = computed(() => adminStore.isSuperAdmin)

const successRate = computed(() => {
  if (!dashboard.value || !dashboard.value.totalCalls) return 0
  return Math.round((dashboard.value.successCalls / dashboard.value.totalCalls) * 100)
})

const providerLabels = { openrouter: 'OpenRouter', zhipu: '智谱AI', openai: 'OpenAI', custom: '自定义' }
const providerTagType = { openrouter: 'warning', zhipu: 'success', openai: '', custom: 'info' }

const loadDashboard = async () => {
  loading.value = true
  try {
    const [listRes, dashRes, cacheRes] = await Promise.all([
      getAiModelList(),
      getAiModelDashboard(),
      getAiModelCacheStats()
    ])
    if (listRes.data) models.value = listRes.data
    if (dashRes.data) dashboard.value = dashRes.data
    if (cacheRes.data) cacheStats.value = cacheRes.data
  } catch (e) {}
  loading.value = false
}

const loadSwitchLogs = async () => {
  try {
    const res = await getAiModelSwitchLogs(20)
    if (res.data) switchLogs.value = res.data
  } catch (e) {}
}

const handleToggleStatus = async (row, val) => {
  try {
    await setAiModelStatus(row.id, val ? 1 : 0)
    row.status = val ? 1 : 0
    ElMessage.success('状态已更新')
    loadDashboard()
  } catch (e) {}
}

const openAdd = () => {
  addForm.value = {
    modelName: '', displayName: '', provider: 'openrouter', baseUrl: '',
    apiKey: '', modelVersion: '', icon: '🤖', description: '', sortOrder: 0
  }
  showAdd.value = true
}

const handleAdd = async () => {
  if (!addForm.value.modelName || !addForm.value.displayName) {
    ElMessage.warning('模型标识和显示名称不能为空')
    return
  }
  try {
    await createAiModel(addForm.value)
    ElMessage.success('新增成功')
    showAdd.value = false
    loadDashboard()
  } catch (e) {}
}

const openEdit = async (row) => {
  try {
    const res = await getAiModelDetail(row.id)
    const detail = res.data || row
    editForm.value = {
      id: detail.id, modelName: detail.modelName, displayName: detail.displayName,
      provider: detail.provider, baseUrl: detail.baseUrl, apiKey: '',
      modelVersion: detail.modelVersion || '', icon: detail.icon || '',
      description: detail.description || '', sortOrder: detail.sortOrder || 0
    }
    showEdit.value = true
  } catch (e) {
    console.error(e)
    ElMessage.error('获取模型详情失败')
  }
}

const handleEdit = async () => {
  try {
    const data = { ...editForm.value }
    delete data.id
    delete data.modelName
    if (!data.apiKey) delete data.apiKey
    await updateAiModel(editForm.value.id, data)
    ElMessage.success('编辑成功')
    showEdit.value = false
    loadDashboard()
  } catch (e) {}
}

const handleSwitch = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定切换到模型"${row.displayName}"？切换后将立即生效。`,
      '确认切换模型',
      { type: 'warning' }
    )
    const res = await switchAiModel({ modelId: row.id, reason: '管理员后台手动切换' })
    if (res.data) {
      ElMessage.success('模型切换成功')
      loadDashboard()
      loadSwitchLogs()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('模型切换失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除模型"${row.displayName}"？此操作不可恢复。`,
      '确认删除',
      { type: 'warning' }
    )
    await deleteAiModel(row.id)
    ElMessage.success('删除成功')
    loadDashboard()
  } catch (e) {}
}

const openParams = async (row) => {
  currentParamsModelId.value = row.id
  try {
    const res = await getAiModelParameters(row.id)
    const params = res.data || {}
    paramList.value = Object.entries(params).map(([key, value]) => ({
      key, value: String(value)
    }))
  } catch (e) {
    paramList.value = []
  }
  showParams.value = true
}

const handleSaveParams = async () => {
  const params = {}
  for (const p of paramList.value) {
    if (p.key) {
      const numVal = Number(p.value)
      params[p.key] = isNaN(numVal) ? p.value : numVal
    }
  }
  try {
    await updateAiModelParameters(currentParamsModelId.value, params)
    ElMessage.success('参数已保存')
    showParams.value = false
  } catch (e) {}
}

const handleClearCache = async () => {
  try {
    await ElMessageBox.confirm(
      '确定清除所有健康分析缓存？清除后下次分析将重新执行。',
      '确认清除缓存',
      { type: 'warning' }
    )
    await clearAiModelAllCache()
    ElMessage.success('缓存已清除')
    loadDashboard()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('清除缓存失败')
    }
  }
}

const handleRefreshCache = async () => {
  try {
    await ElMessageBox.confirm(
      '确定刷新模型缓存？将重新加载模型配置信息。',
      '确认刷新缓存',
      { type: 'info' }
    )
    await refreshAiModelCache()
    ElMessage.success('模型缓存已刷新')
    loadDashboard()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('刷新缓存失败')
    }
  }
}

const handleClearPetCache = async () => {
  if (!clearPetId.value) {
    ElMessage.warning('请输入宠物ID')
    return
  }
  try {
    await clearAiModelPetCache(clearPetId.value)
    ElMessage.success(`宠物${clearPetId.value}的缓存已清除`)
    showClearPetCache.value = false
    clearPetId.value = null
    loadDashboard()
  } catch (e) {
    ElMessage.error('清除缓存失败')
  }
}

const openStatsDetail = async (row) => {
  currentStatsModelId.value = row.id
  try {
    const [statsRes, dailyRes] = await Promise.all([
      getAiModelStats(row.id),
      getAiModelDailyStats(row.id, 30)
    ])
    statsDetail.value = statsRes.data || {}
    dailyStats.value = dailyRes.data || []
    showStatsDetail.value = true
  } catch (e) {
    ElMessage.error('获取统计数据失败')
  }
}

const handleFlushStats = async () => {
  try {
    await flushAiModelStats()
    ElMessage.success('统计数据已持久化到数据库')
    loadDashboard()
  } catch (e) {
    ElMessage.error('持久化失败')
  }
}

onMounted(() => {
  loadDashboard()
  loadSwitchLogs()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; align-items: center; gap: 8px; }

.dashboard-section { margin-bottom: 24px; }
.stat-card { background: #fff; border-radius: 8px; padding: 16px 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.stat-card.mini { padding: 12px 16px; }
.stat-card.mini .stat-value { font-size: 20px; }
.cache-section { background: #fff; border-radius: 8px; padding: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); margin-bottom: 24px; }
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

.switch-log-section { background: #fff; border-radius: 8px; padding: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.section-header h3 { margin: 0; font-size: 15px; }

.params-editor { display: flex; flex-direction: column; gap: 12px; }
.params-hint { margin-bottom: 8px; }
.param-item { display: flex; align-items: center; gap: 8px; }
</style>
