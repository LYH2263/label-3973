<template>
  <div>
    <el-tabs v-model="activeTab" type="border-card" class="constitution-tabs">
      <!-- 体质测评 -->
      <el-tab-pane label="🔬 开始测评" name="test">
        <div v-if="!showResult">
          <div class="test-intro" v-if="currentStep === 0 && !testStarted">
            <div class="intro-icon">🌿</div>
            <h2>中医体质辨识</h2>
            <p>根据中华中医药学会《中医体质分类与判定》标准，通过问卷测评您的中医体质类型，获取个性化养生建议。</p>
            <div class="constitution-types">
              <el-tag v-for="t in constitutionTypes" :key="t" class="type-tag">{{ t }}</el-tag>
            </div>
            <el-button type="primary" size="large" @click="startTest" :loading="questionsLoading">
              开始测评（{{ questions.length }} 题）
            </el-button>
          </div>

          <div v-else class="question-container">
            <div class="progress-info">
              <span>第 {{ currentStep + 1 }} / {{ questions.length }} 题</span>
              <el-progress :percentage="Math.round((currentStep / questions.length) * 100)" :stroke-width="8" />
            </div>

            <div class="question-card">
              <div class="question-type-badge">{{ questions[currentStep]?.constitutionName }}</div>
              <h3 class="question-text">{{ currentStep + 1 }}. {{ questions[currentStep]?.questionText }}</h3>

              <div class="options-grid">
                <div
                  v-for="(opt, idx) in scoreOptions"
                  :key="idx"
                  class="option-item"
                  :class="{ selected: answers[questions[currentStep]?.id] === opt.value }"
                  @click="selectAnswer(questions[currentStep]?.id, opt.value)"
                >
                  <div class="option-score">{{ opt.value }}</div>
                  <div class="option-label">{{ opt.label }}</div>
                </div>
              </div>
            </div>

            <div class="nav-buttons">
              <el-button @click="prevStep" :disabled="currentStep === 0">上一题</el-button>
              <el-button
                type="primary"
                @click="nextStep"
                :disabled="!answers[questions[currentStep]?.id]"
                v-if="currentStep < questions.length - 1"
              >
                下一题
              </el-button>
              <el-button
                type="success"
                @click="submitTest"
                :loading="submitting"
                :disabled="!answers[questions[currentStep]?.id]"
                v-else
              >
                提交测评
              </el-button>
            </div>
          </div>
        </div>

        <!-- 结果展示 -->
        <div v-else class="result-container">
          <div class="result-header">
            <div class="result-badge">{{ result.resultName }}</div>
            <div class="result-title">您的主要体质类型</div>
          </div>

          <div class="result-cards">
            <div class="result-desc-card">
              <h4>📋 体质特征</h4>
              <p>{{ result.description }}</p>
            </div>
            <div class="result-advice-card">
              <h4>💡 养生建议</h4>
              <p>{{ result.advice }}</p>
            </div>
          </div>

          <div class="score-chart">
            <h4>各体质得分（100分制）</h4>
            <div class="score-bars">
              <div v-for="(item, key) in scoreDetail" :key="key" class="score-bar-item">
                <div class="bar-label">{{ item.name }}</div>
                <div class="bar-track">
                  <div
                    class="bar-fill"
                    :class="{ primary: key === result.resultType }"
                    :style="{ width: item.score + '%' }"
                  ></div>
                </div>
                <div class="bar-score">{{ item.score }}</div>
              </div>
            </div>
          </div>

          <div class="result-actions">
            <el-button @click="resetTest">重新测评</el-button>
            <el-button type="primary" @click="activeTab = 'history'">查看历史</el-button>
          </div>
        </div>
      </el-tab-pane>

      <!-- 历史记录 -->
      <el-tab-pane label="📋 历史记录" name="history">
        <div v-loading="historyLoading">
          <div v-if="historyResults.length === 0" class="empty-tip">
            <el-empty description="暂无测评记录" />
          </div>
          <div v-else class="history-list">
            <div v-for="r in historyResults" :key="r.id" class="history-item">
              <div class="history-left">
                <div class="history-badge">{{ r.resultName }}</div>
                <div class="history-time">{{ formatDateTime(r.createdAt) }}</div>
              </div>
              <div class="history-right">
                <p class="history-desc">{{ r.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { constitutionApi } from '@/api/constitution'

const activeTab = ref('test')
const questionsLoading = ref(false)
const submitting = ref(false)
const historyLoading = ref(false)

const questions = ref([])
const answers = reactive({})
const currentStep = ref(0)
const testStarted = ref(false)
const showResult = ref(false)
const result = ref(null)
const scoreDetail = ref({})
const historyResults = ref([])

const constitutionTypes = ['平和质', '气虚质', '阳虚质', '阴虚质', '痰湿质', '湿热质', '血瘀质', '气郁质', '特禀质']

const scoreOptions = [
  { value: 1, label: '从不' },
  { value: 2, label: '很少' },
  { value: 3, label: '有时' },
  { value: 4, label: '经常' },
  { value: 5, label: '总是' }
]

function formatDateTime(str) {
  if (!str) return '-'
  return new Date(str).toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
}

async function startTest() {
  questionsLoading.value = true
  try {
    const res = await constitutionApi.getQuestions()
    questions.value = res.data || []
    testStarted.value = true
  } finally {
    questionsLoading.value = false
  }
}

function selectAnswer(qId, score) {
  answers[qId] = score
}

function nextStep() {
  if (currentStep.value < questions.value.length - 1) {
    currentStep.value++
  }
}

function prevStep() {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

async function submitTest() {
  const unanswered = questions.value.filter(q => !answers[q.id])
  if (unanswered.length > 0) {
    ElMessage.warning(`还有 ${unanswered.length} 道题未作答`)
    return
  }
  submitting.value = true
  try {
    const res = await constitutionApi.submit({ answers: { ...answers } })
    result.value = res.data
    try {
      scoreDetail.value = JSON.parse(res.data.scoreDetail || '{}')
    } catch {
      scoreDetail.value = {}
    }
    showResult.value = true
    ElMessage.success('测评完成！')
    loadHistory()
  } finally {
    submitting.value = false
  }
}

function resetTest() {
  testStarted.value = false
  showResult.value = false
  currentStep.value = 0
  Object.keys(answers).forEach(k => delete answers[k])
}

async function loadHistory() {
  historyLoading.value = true
  try {
    const res = await constitutionApi.getResults()
    historyResults.value = res.data || []
  } finally {
    historyLoading.value = false
  }
}

onMounted(loadHistory)
</script>

<style scoped>
.constitution-tabs {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.constitution-tabs :deep(.el-tabs__content) {
  padding: 24px;
}

.test-intro {
  text-align: center;
  padding: 32px 24px;
}

.intro-icon {
  font-size: 60px;
  margin-bottom: 16px;
}

.test-intro h2 {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 12px;
}

.test-intro p {
  font-size: 14px;
  color: #606266;
  max-width: 600px;
  margin: 0 auto 24px;
  line-height: 1.7;
}

.constitution-types {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-bottom: 28px;
}

.type-tag {
  font-size: 13px;
  padding: 4px 12px;
  background: #e8f5e9;
  color: #3a7d3a;
  border-color: #a8d5a2;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  font-size: 14px;
  color: #606266;
}

.progress-info .el-progress { flex: 1; }

.question-card {
  background: #f8fdf8;
  border-radius: 12px;
  padding: 28px;
  margin-bottom: 24px;
  border: 1px solid #e8f0e8;
}

.question-type-badge {
  display: inline-block;
  background: linear-gradient(135deg, #3a7d3a, #5ca05c);
  color: white;
  padding: 3px 14px;
  border-radius: 20px;
  font-size: 12px;
  margin-bottom: 12px;
}

.question-text {
  font-size: 18px;
  color: #2c3e50;
  margin-bottom: 24px;
  line-height: 1.6;
}

.options-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

@media (max-width: 600px) {
  .options-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

.option-item {
  text-align: center;
  padding: 14px 8px;
  border-radius: 10px;
  border: 2px solid #e0e0e0;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
}

.option-item:hover {
  border-color: #5ca05c;
  background: #f0f9f0;
}

.option-item.selected {
  border-color: #3a7d3a;
  background: linear-gradient(135deg, #3a7d3a, #5ca05c);
  color: white;
}

.option-score {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 4px;
}

.option-label {
  font-size: 12px;
}

.nav-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.result-container {
  max-width: 800px;
  margin: 0 auto;
}

.result-header {
  text-align: center;
  margin-bottom: 28px;
  padding: 28px;
  background: linear-gradient(135deg, #1a3a1a, #2d6a2d);
  border-radius: 14px;
  color: white;
}

.result-badge {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.result-title {
  font-size: 14px;
  opacity: 0.8;
}

.result-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

@media (max-width: 600px) {
  .result-cards { grid-template-columns: 1fr; }
}

.result-desc-card, .result-advice-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.result-desc-card h4, .result-advice-card h4 {
  font-size: 15px;
  color: #2c3e50;
  margin-bottom: 10px;
}

.result-desc-card p, .result-advice-card p {
  font-size: 13px;
  color: #4a5568;
  line-height: 1.7;
}

.score-chart {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  margin-bottom: 24px;
}

.score-chart h4 {
  font-size: 15px;
  color: #2c3e50;
  margin-bottom: 16px;
}

.score-bar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.bar-label {
  width: 60px;
  font-size: 13px;
  color: #606266;
  flex-shrink: 0;
}

.bar-track {
  flex: 1;
  height: 12px;
  background: #f0f0f0;
  border-radius: 6px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #a8d5a2;
  border-radius: 6px;
  transition: width 0.6s ease;
  min-width: 2px;
}

.bar-fill.primary { background: linear-gradient(90deg, #3a7d3a, #5ca05c); }

.bar-score {
  width: 36px;
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  display: flex;
  gap: 20px;
  padding: 18px;
  background: white;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
}

.history-badge {
  display: inline-block;
  background: linear-gradient(135deg, #3a7d3a, #5ca05c);
  color: white;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  white-space: nowrap;
}

.history-time {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.history-desc {
  font-size: 13px;
  color: #4a5568;
  line-height: 1.6;
}

.history-left {
  flex-shrink: 0;
  min-width: 100px;
}

.history-right { flex: 1; }

.empty-tip {
  padding: 40px 0;
}
</style>
