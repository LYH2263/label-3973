<template>
  <div>
    <div class="page-toolbar">
      <div class="toolbar-left">
        <el-select v-model="selectedRecordId" placeholder="选择健康档案" style="width: 200px" @change="loadIndicators">
          <el-option v-for="r in records" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
        <el-button type="primary" :disabled="!selectedRecordId" @click="dialogVisible = true">
          <el-icon><Plus /></el-icon> 录入指标
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="warning" :disabled="!selectedRecordId" @click="openAlertHistory">
          <el-icon><Bell /></el-icon> 预警历史
        </el-button>
      </div>
    </div>

    <div v-if="!selectedRecordId" class="hint-card">
      <el-empty description="请先选择一个健康档案" />
    </div>

    <template v-else>
      <el-row :gutter="16" class="indicator-summary" v-if="indicators.length > 0">
        <el-col :xs="12" :sm="8" :md="4" v-for="item in latestSummary" :key="item.label">
          <div class="summary-card" :class="item.status">
            <div class="summary-icon">{{ item.icon }}</div>
            <div class="summary-value">{{ item.value }}</div>
            <div class="summary-label">{{ item.label }}</div>
          </div>
        </el-col>
      </el-row>

      <el-table
        :data="indicators"
        v-loading="loading"
        style="width: 100%"
        border
        stripe
        class="data-table"
      >
        <el-table-column label="记录时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="身高(cm)" prop="height" width="100" />
        <el-table-column label="体重(kg)" prop="weight" width="100" />
        <el-table-column label="BMI" width="90">
          <template #default="{ row }">
            <span v-if="row.height && row.weight">
              <el-tag :type="getBMIType(row)" size="small">{{ calcBMI(row) }}</el-tag>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="血压(mmHg)" width="130">
          <template #default="{ row }">
            <span v-if="row.bloodPressureHigh">
              <el-tag :type="bpStatus(row.bloodPressureHigh, row.bloodPressureLow)" size="small">
                {{ row.bloodPressureHigh }}/{{ row.bloodPressureLow }}
              </el-tag>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="血糖(mmol/L)" prop="bloodSugar" width="130">
          <template #default="{ row }">
            <span v-if="row.bloodSugar">
              <el-tag :type="sugarStatus(row.bloodSugar)" size="small">{{ row.bloodSugar }}</el-tag>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="心率(次/分)" width="110">
          <template #default="{ row }">
            <span v-if="row.heartRate">
              <el-tag :type="heartRateStatus(row.heartRate)" size="small">{{ row.heartRate }}</el-tag>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="异常提示" min-width="180">
          <template #default="{ row }">
            <div class="alert-tags">
              <el-tag
                v-for="(alert, idx) in getAbnormalAlerts(row)"
                :key="idx"
                :type="alert.type"
                size="small"
                style="margin-right: 4px; margin-bottom: 4px;"
              >
                {{ alert.label }}
              </el-tag>
              <span v-if="getAbnormalAlerts(row).length === 0" class="no-alert">无异常</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" show-overflow-tooltip />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" plain @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <el-dialog v-model="dialogVisible" title="录入健康指标" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="身高(cm)">
              <el-input-number v-model="form.height" :min="50" :max="250" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)">
              <el-input-number v-model="form.weight" :min="1" :max="300" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="收缩压(mmHg)">
              <el-input-number v-model="form.bloodPressureHigh" :min="60" :max="250" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="舒张压(mmHg)">
              <el-input-number v-model="form.bloodPressureLow" :min="40" :max="180" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="血糖(mmol/L)">
              <el-input-number v-model="form.bloodSugar" :min="0" :max="30" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="心率(次/分)">
              <el-input-number v-model="form.heartRate" :min="30" :max="250" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="如：晨起空腹" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="alertDialogVisible" title="预警历史" width="700px">
      <el-table :data="alertHistory" v-loading="alertLoading" border stripe>
        <el-table-column label="预警时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.alertTime) }}</template>
        </el-table-column>
        <el-table-column label="指标类型" width="100" prop="indicatorType" />
        <el-table-column label="预警级别" width="100">
          <template #default="{ row }">
            <el-tag :type="row.alertLevel" size="small">
              {{ getAlertLevelText(row.alertLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预警内容" prop="alertContent" />
      </el-table>
      <div v-if="alertHistory.length === 0 && !alertLoading" class="empty-alert">
        <el-empty description="暂无预警记录" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Bell } from '@element-plus/icons-vue'
import { recordApi } from '@/api/record'
import { indicatorApi } from '@/api/indicator'
import { alertApi } from '@/api/alert'
import {
  calcBMIValue,
  detectBMIStatus,
  checkHeartRateStatus,
  checkBloodPressureStatus,
  checkBloodSugarStatus,
  detectAbnormalIndicators
} from '@/composables/useHealthDetection'

const records = ref([])
const selectedRecordId = ref(null)
const indicators = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const alertDialogVisible = ref(false)
const alertHistory = ref([])
const alertLoading = ref(false)

const form = reactive({
  recordId: null, height: null, weight: null,
  bloodPressureHigh: null, bloodPressureLow: null,
  bloodSugar: null, heartRate: null, remark: ''
})

function formatDateTime(str) {
  if (!str) return '-'
  return new Date(str).toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
}

function calcBMI(row) {
  return calcBMIValue(row.height, row.weight)
}

function getBMIType(row) {
  const bmi = calcBMIValue(row.height, row.weight)
  return detectBMIStatus(bmi).type
}

function bpStatus(high, low) {
  return checkBloodPressureStatus(high, low).type
}

function sugarStatus(val) {
  return checkBloodSugarStatus(val).type
}

function heartRateStatus(val) {
  return checkHeartRateStatus(val).type
}

function getAbnormalAlerts(row) {
  return detectAbnormalIndicators(row)
}

function getAlertLevelText(level) {
  const map = { success: '正常', warning: '警告', danger: '危险', info: '提示' }
  return map[level] || level
}

const latestSummary = computed(() => {
  if (indicators.value.length === 0) return []
  const latest = indicators.value[0]
  const items = []
  if (latest.height && latest.weight) {
    const bmi = calcBMIValue(latest.height, latest.weight)
    const bmiStatus = detectBMIStatus(bmi)
    items.push({ label: 'BMI', value: bmi, icon: '⚖️', status: bmiStatus.type === 'success' ? 'good' : 'warn' })
  }
  if (latest.bloodPressureHigh) {
    const bpStatus = checkBloodPressureStatus(latest.bloodPressureHigh, latest.bloodPressureLow)
    items.push({
      label: '血压', value: `${latest.bloodPressureHigh}/${latest.bloodPressureLow}`,
      icon: '🩺', status: bpStatus.type === 'danger' ? 'bad' : bpStatus.type === 'warning' ? 'warn' : 'good'
    })
  }
  if (latest.bloodSugar) {
    const sugarStatus = checkBloodSugarStatus(latest.bloodSugar)
    items.push({
      label: '血糖', value: latest.bloodSugar, icon: '🩸',
      status: sugarStatus.type === 'danger' ? 'bad' : sugarStatus.type === 'warning' ? 'warn' : 'good'
    })
  }
  if (latest.heartRate) {
    const heartStatus = checkHeartRateStatus(latest.heartRate)
    items.push({
      label: '心率', value: latest.heartRate, icon: '❤️',
      status: heartStatus.type === 'success' ? 'good' : 'warn'
    })
  }
  return items
})

async function loadRecords() {
  const res = await recordApi.list()
  records.value = res.data || []
}

async function loadIndicators() {
  if (!selectedRecordId.value) return
  loading.value = true
  try {
    const res = await indicatorApi.list(selectedRecordId.value)
    indicators.value = res.data || []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    recordId: null, height: null, weight: null,
    bloodPressureHigh: null, bloodPressureLow: null,
    bloodSugar: null, heartRate: null, remark: ''
  })
  formRef.value?.resetFields()
}

async function handleSubmit() {
  submitting.value = true
  try {
    form.recordId = selectedRecordId.value
    await indicatorApi.create(form)
    ElMessage.success('录入成功')
    dialogVisible.value = false
    loadIndicators()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除该指标记录？', '警告', { type: 'warning' })
  await indicatorApi.delete(id)
  ElMessage.success('已删除')
  loadIndicators()
}

async function openAlertHistory() {
  if (!selectedRecordId.value) return
  alertDialogVisible.value = true
  alertLoading.value = true
  try {
    const res = await alertApi.list(selectedRecordId.value)
    alertHistory.value = res.data || []
  } finally {
    alertLoading.value = false
  }
}

onMounted(loadRecords)
</script>

<style scoped>
.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.toolbar-left, .toolbar-right { display: flex; align-items: center; gap: 12px; }

.hint-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.indicator-summary {
  margin-bottom: 16px;
}

.summary-card {
  background: white;
  border-radius: 10px;
  padding: 14px 12px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(0,0,0,0.06);
  margin-bottom: 16px;
  border-top: 3px solid #e0e0e0;
}

.summary-card.good { border-top-color: #67c23a; }
.summary-card.warn { border-top-color: #e6a23c; }
.summary-card.bad  { border-top-color: #f56c6c; }

.summary-icon { font-size: 20px; margin-bottom: 4px; }
.summary-value { font-size: 18px; font-weight: 700; color: #2c3e50; }
.summary-label { font-size: 12px; color: #909399; margin-top: 2px; }

.data-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.alert-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.no-alert {
  color: #67c23a;
  font-size: 12px;
}

.empty-alert {
  margin-top: 20px;
}
</style>
