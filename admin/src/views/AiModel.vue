<template>
  <div class="ai-model-page">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" class="stats-row" v-if="dashboard">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="overview-card" :body-style="{ padding: '16px 20px' }">
          <div class="overview-content">
            <div class="overview-icon" style="background: #ecf5ff; color: #409eff;">
              <el-icon :size="20"><Cpu /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ dashboard.currentModel?.displayName || '未配置' }}</div>
              <div class="overview-label">当前活动模型</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="overview-card" :body-style="{ padding: '16px 20px' }">
          <div class="overview-content">
            <div class="overview-icon" style="background: #f0f9eb; color: #67c23a;">
              <el-icon :size="20"><TrendCharts /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ dashboard.totalCalls || 0 }}</div>
              <div class="overview-label">总调用次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="overview-card" :body-style="{ padding: '16px 20px' }">
          <div class="overview-content">
            <div class="overview-icon" :style="{ background: successRate >= 90 ? '#f0f9eb' : successRate >= 70 ? '#fdf6ec' : '#fef0f0', color: successRate >= 90 ? '#67c23a' : successRate >= 70 ? '#e6a23c' : '#f56c6c' }">
              <el-icon :size="20"><CircleCheck /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value" :style="{ color: successRate >= 90 ? '#67c23a' : successRate >= 70 ? '#e6a23c' : '#f56c6c' }">{{ successRate }}%</div>
              <div class="overview-label">成功率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="overview-card" :body-style="{ padding: '16px 20px' }">
          <div class="overview-content">
            <div class="overview-icon" style="background: #f4f4f5; color: #909399;">
              <el-icon :size="20"><Timer /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ Math.round(dashboard.avgResponseTime || 0) }}ms</div>
              <div class="overview-label">平均耗时</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 缓存状态 -->
    <el-card shadow="hover" class="cache-card" v-if="cacheInfo">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">模型缓存状态</span>
          </div>
          <div class="header-actions">
            <el-button size="small" :icon="Refresh" @click="loadCacheInfo">刷新</el-button>
            <el-button size="small" type="warning" :icon="Delete" @click="clearCache">清空缓存</el-button>
            <el-button size="small" type="primary" :icon="Download" @click="persistCache" v-if="canManage">持久化</el-button>
          </div>
        </div>
      </template>
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="当前模型">{{ cacheInfo.currentModelId || '未配置' }}</el-descriptions-item>
        <el-descriptions-item label="缓存模型数">{{ cacheInfo.modelCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="最后更新">{{ cacheInfo.lastUpdated || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 模型列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">模型列表</span>
            <el-tag size="small" type="info">共 {{ modelList.length }} 个</el-tag>
          </div>
          <div class="header-actions">
            <el-button :icon="Refresh" @click="loadDashboard">刷新</el-button>
            <el-button type="primary" :icon="Plus" @click="openAdd" v-if="canManage">新增模型</el-button>
          </div>
        </div>
      </template>
      <el-table :data="modelList" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="模型" min-width="200">
          <template #default="{ row }">
            <div class="model-info-cell">
              <div class="model-icon-wrap" style="background: #ecf5ff; color: #409eff;">
                <el-icon :size="20"><Cpu /></el-icon>
              </div>
              <div class="model-info">
                <div class="model-name">{{ row.displayName }}</div>
                <div class="model-id">{{ row.modelName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="provider" label="提供商" width="100" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'" effect="light" size="small">{{ row.isActive ? '活动中' : '未激活' }}</el-tag>
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
        <el-table-column label="平均耗时" width="100" align="center">
          <template #default="{ row }">{{ row.avgResponseTime ? Math.round(row.avgResponseTime) + 'ms' : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="400" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" text type="primary" :icon="EditPen" @click="openEdit(row)" v-if="canManage">编辑</el-button>
              <el-button size="small" text type="primary" :icon="Setting" @click="openParams(row)">参数</el-button>
              <el-button size="small" text type="primary" :icon="TrendCharts" @click="openStatsDetail(row)">统计</el-button>
              <el-button size="small" text type="success" :icon="Switch" @click="handleSwitch(row)" :disabled="row.isActive" v-if="canManage">切换</el-button>
              <el-button size="small" text type="danger" :icon="Delete" @click="handleDelete(row)" :disabled="row.isActive" v-if="canManage">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 模型切换日志 -->
    <el-card shadow="hover" class="table-card" style="margin-top: 16px">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">模型切换日志</span>
            <el-tag size="small" type="info">共 {{ switchLogs.length }} 条</el-tag>
          </div>
          <div class="header-actions">
            <el-button size="small" :icon="Refresh" @click="loadSwitchLogs">刷新日志</el-button>
          </div>
        </div>
      </template>
      <el-table :data="switchLogs" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="createdAt" label="时间" width="170">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon :size="12"><Clock /></el-icon>
              <span>{{ row.createdAt }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="fromModelName" label="原模型" width="150" show-overflow-tooltip />
        <el-table-column prop="toModelName" label="新模型" width="150" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
      </el-table>
      <div class="pagination-wrap" v-if="switchLogs.length > 0">
        <el-pagination small layout="prev, pager, next" :total="switchLogs.length" :page-size="10" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑模型' : '新增模型'" width="600px" destroy-on-close class="ai-dialog">
      <div v-if="isEdit && form.id" class="dialog-profile">
        <div class="dialog-profile-icon" style="background: #ecf5ff; color: #409eff;">
          <el-icon :size="28"><Cpu /></el-icon>
        </div>
        <div class="dialog-profile-info">
          <div class="dialog-profile-name">{{ form.displayName || '未命名模型' }}</div>
          <div class="dialog-profile-meta">
            <el-tag :type="form.isActive ? 'success' : 'info'" size="small" effect="light" round>{{ form.isActive ? '活动中' : '未激活' }}</el-tag>
            <span v-if="form.provider" class="dialog-profile-provider">{{ form.provider }}</span>
          </div>
        </div>
      </div>
      <el-divider v-if="isEdit && form.id" class="dialog-divider" />
      <el-form :model="form" label-width="100px" class="dialog-form">
        <el-form-item label="显示名称" required><el-input v-model="form.displayName" placeholder="请输入显示名称" /></el-form-item>
        <el-form-item label="模型名称" required><el-input v-model="form.modelName" placeholder="如 gpt-4o" /></el-form-item>
        <el-form-item label="提供商" required>
          <el-select v-model="form.provider" placeholder="选择提供商" style="width: 100%">
            <el-option label="OpenAI" value="openai" /><el-option label="Anthropic" value="anthropic" />
            <el-option label="Google" value="google" /><el-option label="Azure" value="azure" />
            <el-option label="本地" value="local" /><el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本"><el-input v-model="form.version" placeholder="请输入版本号" /></el-form-item>
        <el-form-item label="API密钥"><el-input v-model="form.apiKey" type="password" show-password placeholder="留空则保持不变" /></el-form-item>
        <el-form-item label="BaseURL"><el-input v-model="form.baseUrl" placeholder="可选" /></el-form-item>
        <el-form-item label="最大Token"><el-input-number v-model="form.maxTokens" :min="1" :max="128000" style="width: 100%" /></el-form-item>
        <el-form-item label="超时(ms)"><el-input-number v-model="form.timeout" :min="1000" :max="120000" :step="1000" style="width: 100%" /></el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.isActive" />
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入模型描述" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitModel" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 参数配置弹窗 -->
    <el-dialog v-model="showParamsDialog" title="模型参数配置" width="500px" destroy-on-close class="ai-dialog">
      <div class="params-hint">参数以 JSON 格式存储，key 为参数名，value 为默认值</div>
      <div class="params-editor">
        <div v-for="(param, index) in paramList" :key="index" class="param-item">
          <el-input v-model="param.key" placeholder="参数名" style="width: 140px" />
          <el-input v-model="param.value" placeholder="默认值" style="flex: 1" />
          <el-button type="danger" link :icon="Delete" @click="paramList.splice(index, 1)">删除</el-button>
        </div>
        <el-button type="primary" link :icon="Plus" @click="paramList.push({ key: '', value: '' })">添加参数</el-button>
      </div>
      <template #footer>
        <el-button @click="showParamsDialog = false">取消</el-button>
        <el-button type="primary" @click="submitParams" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 统计详情弹窗 -->
    <el-dialog v-model="showStatsDialog" title="调用统计详情" width="640px" destroy-on-close class="ai-dialog">
      <div v-if="statsDetail" class="sd-header">
        <div class="sd-icon" style="background: #ecf5ff; color: #409eff;">
          <el-icon :size="28"><Cpu /></el-icon>
        </div>
        <div class="sd-header-info">
          <div class="sd-name">{{ statsDetail.displayName || '未知模型' }}</div>
          <div class="sd-tags">
            <el-tag :type="statsDetail.isActive ? 'success' : 'info'" size="small" effect="light" round>{{ statsDetail.isActive ? '活动中' : '未激活' }}</el-tag>
            <span v-if="statsDetail.provider" class="sd-tag-item">{{ statsDetail.provider }}</span>
          </div>
        </div>
      </div>

      <div class="sd-body">
        <div class="sd-section">
          <div class="sd-section-title">调用统计</div>
          <div class="sd-grid">
            <div class="sd-cell">
              <span class="sd-cell-label">总调用</span>
              <span class="sd-cell-value">{{ statsDetail.callCount || 0 }}</span>
            </div>
            <div class="sd-cell">
              <span class="sd-cell-label">成功</span>
              <span class="sd-cell-value" style="color: #67c23a">{{ statsDetail.successCount || 0 }}</span>
            </div>
            <div class="sd-cell">
              <span class="sd-cell-label">失败</span>
              <span class="sd-cell-value" style="color: #f56c6c">{{ statsDetail.failCount || 0 }}</span>
            </div>
            <div class="sd-cell">
              <span class="sd-cell-label">成功率</span>
              <span class="sd-cell-value">{{ statsDetail.callCount ? Math.round((statsDetail.successCount / statsDetail.callCount) * 100) : 0 }}%</span>
            </div>
            <div class="sd-cell">
              <span class="sd-cell-label">平均耗时</span>
              <span class="sd-cell-value">{{ statsDetail.avgResponseTime ? Math.round(statsDetail.avgResponseTime) + 'ms' : '-' }}</span>
            </div>
            <div class="sd-cell">
              <span class="sd-cell-label">最大耗时</span>
              <span class="sd-cell-value">{{ statsDetail.maxResponseTime ? Math.round(statsDetail.maxResponseTime) + 'ms' : '-' }}</span>
            </div>
            <div class="sd-cell">
              <span class="sd-cell-label">最小耗时</span>
              <span class="sd-cell-value">{{ statsDetail.minResponseTime ? Math.round(statsDetail.minResponseTime) + 'ms' : '-' }}</span>
            </div>
            <div class="sd-cell">
              <span class="sd-cell-label">最后调用</span>
              <span class="sd-cell-value">{{ statsDetail.lastCallTime || '-' }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/store/admin'
import {
  getAiModelList, getAiModelDetail, createAiModel, updateAiModel, deleteAiModel,
  getAiModelDashboard, switchAiModel, getAiModelSwitchLogs, getAiModelCacheStats,
  clearAiModelAllCache, refreshAiModelCache, updateAiModelParameters
} from '../api/admin'
import { Plus, EditPen, Delete, Setting, TrendCharts, Switch, Refresh, Clock, Cpu, CircleCheck, Timer } from '@element-plus/icons-vue'

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
    const res = await getAiModelCacheStats()
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
      ...detail,
      apiKey: '',
      maxTokens: detail.maxTokens || 4096,
      timeout: detail.timeout || 30000
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
    await switchAiModel({ modelId: row.id })
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
    await updateAiModelParameters(paramModelId.value, { params })
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
    await clearAiModelAllCache()
    ElMessage.success('缓存已清空')
    loadCacheInfo()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('清空失败')
  }
}

const persistCache = async () => {
  try {
    await refreshAiModelCache()
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
.ai-model-page { padding: 0; }

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

/* 缓存卡片 */
.cache-card { margin-bottom: 16px; }

/* 表格卡片 */
.table-card { min-height: 400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.header-left { display: flex; align-items: center; gap: 8px; }
.card-title { font-weight: 600; font-size: 15px; color: #303133; }
.header-actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }

/* 模型信息 */
.model-info-cell { display: flex; align-items: center; gap: 12px; }
.model-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.model-info { display: flex; flex-direction: column; gap: 4px; }
.model-name { font-weight: 600; font-size: 14px; color: #303133; }
.model-id { font-size: 12px; color: #909399; font-family: monospace; }

/* 统计 */
.stats-cell { display: flex; flex-direction: column; font-size: 12px; line-height: 1.6; }
.text-success { color: #67c23a; }
.text-danger { color: #f56c6c; }

/* 时间 */
.time-cell { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #606266; }

/* 操作按钮 */
.action-btns {
  display: flex;
  justify-content: center;
  gap: 4px;
  flex-wrap: nowrap;
}

/* 分页 */
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }

/* 参数编辑器 */
.params-hint { margin-bottom: 12px; color: #909399; font-size: 13px; }
.params-editor { display: flex; flex-direction: column; gap: 12px; }
.param-item { display: flex; align-items: center; gap: 8px; }

/* 弹窗通用样式 */
.ai-dialog :deep(.el-dialog__body) { padding-top: 8px; }
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
.dialog-profile-provider { color: #909399; font-size: 13px; }
.dialog-divider { margin: 4px 0 16px; }
.dialog-form :deep(.el-form-item) { margin-bottom: 16px; }

/* 统计弹窗 */
.sd-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}
.sd-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.sd-header-info { flex: 1; }
.sd-name { font-size: 18px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.sd-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.sd-tag-item {
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 3px 10px;
  border-radius: 12px;
}

.sd-body { padding: 20px 24px; }

.sd-section { margin-bottom: 24px; }
.sd-section:last-child { margin-bottom: 0; }

.sd-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.sd-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 16px;
}
.sd-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: #fafbfc;
  border-radius: 8px;
}
.sd-cell-label {
  font-size: 11px;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.sd-cell-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
</style>
