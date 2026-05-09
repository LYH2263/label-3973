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
    </div>

    <div v-if="!selectedRecordId" class="hint-card">
      <el-empty description="请先选择一个健康档案" />
    </div>

    <template v-else>
      <!-- 最新指标概览 -->
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
        <el-table-column label="心率(次/分)" prop="heartRate" width="110" />
        <el-table-column label="备注" prop="remark" show-overflow-tooltip />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" plain @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <!-- 录入弹窗 -->
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { recordApi } from '@/api/record'
import { indicatorApi } from '@/api/indicator'

const records = ref([])
const selectedRecordId = ref(null)
const indicators = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()

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

function bpStatus(high, low) {
  if (high >= 140 || low >= 90) return 'danger'
  if (high >= 130 || low >= 80) return 'warning'
  return 'success'
}

function sugarStatus(val) {
  if (val >= 7.0) return 'danger'
  if (val >= 6.1) return 'warning'
  return 'success'
}

const latestSummary = computed(() => {
  if (indicators.value.length === 0) return []
  const latest = indicators.value[0]
  const items = []
  if (latest.height && latest.weight) {
    const bmi = (latest.weight / ((latest.height / 100) ** 2)).toFixed(1)
    items.push({ label: 'BMI', value: bmi, icon: '⚖️', status: bmi < 18.5 || bmi >= 24 ? 'warn' : 'good' })
  }
  if (latest.bloodPressureHigh) {
    items.push({
      label: '血压', value: `${latest.bloodPressureHigh}/${latest.bloodPressureLow}`,
      icon: '🩺', status: latest.bloodPressureHigh >= 140 ? 'bad' : 'good'
    })
  }
  if (latest.bloodSugar) {
    items.push({ label: '血糖', value: latest.bloodSugar, icon: '🩸', status: latest.bloodSugar >= 7 ? 'bad' : 'good' })
  }
  if (latest.heartRate) {
    items.push({ label: '心率', value: latest.heartRate, icon: '❤️', status: latest.heartRate > 100 || latest.heartRate < 60 ? 'warn' : 'good' })
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
</style>
