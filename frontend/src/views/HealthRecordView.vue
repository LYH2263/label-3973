<template>
  <div>
    <div class="page-toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon> 新增档案
        </el-button>
      </div>
      <div class="toolbar-right">
        <span class="total-tip">共 {{ records.length }} 条档案</span>
      </div>
    </div>

    <div class="card-grid" v-loading="loading">
      <div v-if="records.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无健康档案">
          <el-button type="primary" @click="openDialog()">创建第一个档案</el-button>
        </el-empty>
      </div>
      <div v-for="r in records" :key="r.id" class="record-card">
        <div class="record-card-header">
          <div class="record-avatar-lg">{{ r.name.charAt(0) }}</div>
          <div class="record-basic">
            <div class="record-name">{{ r.name }}</div>
            <div class="record-tags">
              <el-tag size="small" :type="r.gender === 1 ? 'primary' : 'danger'">
                {{ r.gender === 1 ? '男' : '女' }}
              </el-tag>
              <el-tag size="small" type="info" v-if="r.age">{{ r.age }}岁</el-tag>
              <el-tag size="small" type="warning" v-if="r.bloodType">{{ r.bloodType }}型血</el-tag>
            </div>
          </div>
        </div>
        <div class="record-card-body">
          <div class="info-row" v-if="r.allergy">
            <span class="info-label">过敏史</span>
            <span class="info-value">{{ r.allergy }}</span>
          </div>
          <div class="info-row" v-if="r.medicalHistory">
            <span class="info-label">既往病史</span>
            <span class="info-value">{{ r.medicalHistory }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">建档时间</span>
            <span class="info-value">{{ formatDate(r.createdAt) }}</span>
          </div>
        </div>
        <div class="record-card-footer">
          <el-button size="small" @click="openDialog(r)">
            <el-icon><Edit /></el-icon> 编辑
          </el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(r.id)">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑健康档案' : '新增健康档案'"
      width="520px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
                <el-option label="男" :value="1" />
                <el-option label="女" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="1" :max="150" placeholder="年龄" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="血型" prop="bloodType">
          <el-select v-model="form.bloodType" placeholder="请选择血型" style="width: 100%">
            <el-option label="A型" value="A" />
            <el-option label="B型" value="B" />
            <el-option label="O型" value="O" />
            <el-option label="AB型" value="AB" />
            <el-option label="未知" value="" />
          </el-select>
        </el-form-item>
        <el-form-item label="过敏史">
          <el-input v-model="form.allergy" type="textarea" rows="2" placeholder="如：青霉素过敏、花粉过敏（无则填无）" />
        </el-form-item>
        <el-form-item label="既往病史">
          <el-input v-model="form.medicalHistory" type="textarea" rows="2" placeholder="如：高血压、糖尿病（无则填无）" />
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

const loading = ref(false)
const records = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref()

const form = reactive({
  name: '', gender: null, age: null, bloodType: '', allergy: '', medicalHistory: ''
})

const rules = {
  name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }]
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

async function loadRecords() {
  loading.value = true
  try {
    const res = await recordApi.list()
    records.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openDialog(record = null) {
  editingId.value = record?.id || null
  if (record) {
    Object.assign(form, {
      name: record.name,
      gender: record.gender,
      age: record.age,
      bloodType: record.bloodType || '',
      allergy: record.allergy || '',
      medicalHistory: record.medicalHistory || ''
    })
  } else {
    resetForm()
  }
  dialogVisible.value = true
}

function resetForm() {
  editingId.value = null
  Object.assign(form, { name: '', gender: null, age: null, bloodType: '', allergy: '', medicalHistory: '' })
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (editingId.value) {
      await recordApi.update(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await recordApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadRecords()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除该健康档案？', '警告', { type: 'warning' })
  await recordApi.delete(id)
  ElMessage.success('已删除')
  loadRecords()
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

.total-tip {
  font-size: 14px;
  color: #909399;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.record-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  overflow: hidden;
  transition: box-shadow 0.2s, transform 0.2s;
}

.record-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
  transform: translateY(-2px);
}

.record-card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px 12px;
  background: linear-gradient(135deg, #f0f9f0, #e8f5e9);
  border-bottom: 1px solid #e8f0e8;
}

.record-avatar-lg {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3a7d3a, #5ca05c);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  flex-shrink: 0;
}

.record-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 6px;
}

.record-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.record-card-body {
  padding: 14px 20px;
}

.info-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}

.info-label {
  color: #909399;
  flex-shrink: 0;
  min-width: 60px;
}

.info-value {
  color: #4a5568;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-card-footer {
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 8px;
}

.empty-state {
  grid-column: 1 / -1;
  padding: 60px 0;
}
</style>
