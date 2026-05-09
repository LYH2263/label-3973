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
            <span v-if="row.height && row.weight">{{ calcBMI(row) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="血压(mmHg)" width="130">
          <template #default="{ row }">
            <span v-if="row.bloodPressureHigh">
              <el-tag :type="checkBloodPressureAbnormal(row.bloodPressureHigh, row.bloodPressureLow) ? checkBloodPressureAbnormal(row.bloodPressureHigh, row.bloodPressureLow).type : 'success'" size="small">
                {{ row.bloodPressureHigh }}/{{ row.bloodPressureLow }}
              </el-tag>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="血糖(mmol/L)" prop="bloodSugar" width="130">
          <template #default="{ row }">
            <span v-if="row.bloodSugar">
              <el-tag :type="checkBloodSugarAbnormal(row.bloodSugar) ? checkBloodSugarAbnormal(row.bloodSugar).type : 'success'" size="small">{{ row.bloodSugar }}</el-tag>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="心率(次/分)" width="110">
          <template #default="{ row }">
            <span v-if="row.heartRate">
              <el-tag :type="checkHeartRateAbnormal(row.heartRate) ? checkHeartRateAbnormal(row.heartRate).type : 'success'" size="small">{{ row.heartRate }}</el-tag>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="异常提示" min-width="180">
          <template #default="{ row }">
            <div class="abnormal-tags" v-if="detectIndicatorAbnormalTags(row).length > 0">
              <el-tag
                v-for="(tag, idx) in detectIndicatorAbnormalTags(row)"
                :key="idx"
                :type="tag.type"
                size="small"
                class="abnormal-tag"
              >{{ tag.label }}</el-tag>
            </div>
            <span v-else class="normal-text">正常</span>
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
      <el-table :data="alerts" v-loading="alertLoading" border stripe style="width: 100%">
        <el-table-column label="预警时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.alertTime) }}</template>
        </el-table-column>
        <el-table-column label="指标类型" prop="indicatorType" width="100" />
        <el-table-column label="异常级别" width="100">
          <template #default="{ row }">
            <el-tag :type="alertLevelType(row.alertLevel)" size="small">{{ alertLevelLabel(row.alertLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预警内容" prop="alertContent" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!alertLoading && alerts.length === 0" description="暂无预警记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { recordApi } from '@/api/record'
import { indicatorApi } from '@/api/indicator'
import { alertApi } from '@/api/alert'
import { useHealthAlertDetect } from '@/composables/useHealthAlertDetect'

const {
  checkBmiAbnormal,
  checkBloodPressureAbnormal,
  checkBloodSugarAbnormal,
  checkHeartRateAbnormal,
  detectIndicatorAbnormalTags
} = useHealthAlertDetect()

const records = ref([])
const selectedRecordId = ref(null)
const indicators = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()

const alertDialogVisible = ref(false)
const alerts = ref([])
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
  const bmi = row.weight / ((row.height / 100) ** 2)
  return bmi.toFixed(1)
}

function alertLevelType(level) {
  const map = { danger: 'danger', warning: 'warning', info: 'info' }
  return map[level] || 'info'
}

function alertLevelLabel(level) {
  const map = { danger: '高风险', warning: '警告', info: '提示' }
  return map[level] || level
}

const latestSummary = computed(() => {
  if (indicators.value.length === 0) return []
  const latest = indicators.value[0]
  const items = []
  if (latest.height && latest.weight) {
    const bmi = (latest.weight / ((latest.height / 100) ** 2)).toFixed(1)
    const bmiAbnormal = checkBmiAbnormal(latest.height, latest.weight)
    items.push({ label: 'BMI', value: bmi, icon: '⚖️', status: bmiAbnormal ? (bmiAbnormal.type === 'danger' ? 'bad' : 'warn') : 'good' })
  }
  if (latest.bloodPressureHigh) {
    const bpAbnormal = checkBloodPressureAbnormal(latest.bloodPressureHigh, latest.bloodPressureLow)
    items.push({
      label: '血压', value: `${latest.bloodPressureHigh}/${latest.bloodPressureLow}`,
      icon: '🩺', status: bpAbnormal ? (bpAbnormal.type === 'danger' ? 'bad' : 'warn') : 'good'
    })
  }
  if (latest.bloodSugar) {
    const sugarAbnormal = checkBloodSugarAbnormal(latest.bloodSugar)
    items.push({ label: '血糖', value: latest.bloodSugar, icon: '🩸', status: sugarAbnormal ? (sugarAbnormal.type === 'danger' ? 'bad' : 'warn') : 'good' })
  }
  if (latest.heartRate) {
    const hrAbnormal = checkHeartRateAbnormal(latest.heartRate)
    items.push({ label: '心率', value: latest.heartRate, icon: '❤️', status: hrAbnormal ? 'warn' : 'good' })
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

async function openAlertHistory() {
  if (!selectedRecordId.value) return
  alertDialogVisible.value = true
  alertLoading.value = true
  try {
    const res = await alertApi.list(selectedRecordId.value)
    alerts.value = res.data || []
  } finally {
    alertLoading.value = false
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

onMounted(loadRecords)
</script>

<style scoped>
.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.toolbar-left { display: flex; align-items: center; gap: 12px; }

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

.abnormal-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.abnormal-tag {
  margin: 0;
}

.normal-text {
  color: #67c23a;
  font-size: 13px;
}
</style>
