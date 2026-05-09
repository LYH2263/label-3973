<template>
  <div>
    <div class="page-toolbar">
      <div class="toolbar-left">
        <el-select v-model="selectedRecordId" placeholder="选择健康档案" style="width: 200px" @change="loadDiseases">
          <el-option
            v-for="r in records"
            :key="r.id"
            :label="r.name"
            :value="r.id"
          />
        </el-select>
        <el-button type="primary" :disabled="!selectedRecordId" @click="openDialog()">
          <el-icon><Plus /></el-icon> 添加慢病
        </el-button>
      </div>
    </div>

    <div v-if="!selectedRecordId" class="hint-card">
      <el-empty description="请先选择一个健康档案" />
    </div>

    <el-table
      v-else
      :data="diseases"
      v-loading="loading"
      style="width: 100%"
      border
      stripe
      class="data-table"
    >
      <el-table-column label="慢病类型" prop="diseaseType" width="140">
        <template #default="{ row }">
          <el-tag :type="getDiseaseTagType(row.diseaseType)">{{ row.diseaseType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="疾病名称" prop="diseaseName" />
      <el-table-column label="确诊日期" prop="diagnosisDate" width="130" />
      <el-table-column label="备注" prop="notes" show-overflow-tooltip />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑慢病' : '添加慢病'"
      width="480px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="慢病类型" prop="diseaseType">
          <el-select v-model="form.diseaseType" placeholder="请选择慢病类型" style="width: 100%">
            <el-option label="高血压" value="高血压" />
            <el-option label="糖尿病" value="糖尿病" />
            <el-option label="高血脂" value="高血脂" />
            <el-option label="冠心病" value="冠心病" />
            <el-option label="脑血管病" value="脑血管病" />
            <el-option label="慢性肾病" value="慢性肾病" />
            <el-option label="慢性肺病" value="慢性肺病" />
            <el-option label="恶性肿瘤" value="恶性肿瘤" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="疾病名称">
          <el-input v-model="form.diseaseName" placeholder="如：原发性高血压" />
        </el-form-item>
        <el-form-item label="确诊日期">
          <el-date-picker v-model="form.diagnosisDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { recordApi } from '@/api/record'
import { chronicApi } from '@/api/chronic'

const records = ref([])
const selectedRecordId = ref(null)
const diseases = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref()

const form = reactive({ recordId: null, diseaseType: '', diseaseName: '', diagnosisDate: null, notes: '' })

const rules = {
  diseaseType: [{ required: true, message: '请选择慢病类型', trigger: 'change' }]
}

const diseaseTypeColors = {
  高血压: 'danger', 糖尿病: 'warning', 高血脂: 'warning',
  冠心病: 'danger', 脑血管病: 'danger', 慢性肾病: 'info',
  慢性肺病: 'info', 恶性肿瘤: '', 其他: 'info'
}

function getDiseaseTagType(type) {
  return diseaseTypeColors[type] || 'info'
}

async function loadRecords() {
  const res = await recordApi.list()
  records.value = res.data || []
}

async function loadDiseases() {
  if (!selectedRecordId.value) return
  loading.value = true
  try {
    const res = await chronicApi.list(selectedRecordId.value)
    diseases.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openDialog(disease = null) {
  editingId.value = disease?.id || null
  if (disease) {
    Object.assign(form, {
      recordId: disease.recordId,
      diseaseType: disease.diseaseType,
      diseaseName: disease.diseaseName || '',
      diagnosisDate: disease.diagnosisDate || null,
      notes: disease.notes || ''
    })
  } else {
    Object.assign(form, { recordId: selectedRecordId.value, diseaseType: '', diseaseName: '', diagnosisDate: null, notes: '' })
  }
  dialogVisible.value = true
}

function resetForm() {
  editingId.value = null
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (editingId.value) {
      await chronicApi.update(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await chronicApi.create(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadDiseases()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除该慢病记录？', '警告', { type: 'warning' })
  await chronicApi.delete(id)
  ElMessage.success('已删除')
  loadDiseases()
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

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hint-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.data-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
</style>
