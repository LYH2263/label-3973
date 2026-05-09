<template>
  <div class="dashboard">
    <div class="welcome-banner">
      <div class="welcome-text">
        <h2>你好，{{ userStore.userInfo?.nickname || userStore.userInfo?.username }} 👋</h2>
        <p>欢迎使用中医药特色健康管理系统</p>
      </div>
      <div class="welcome-icon">🌿</div>
    </div>

    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :lg="6" v-for="card in statCards" :key="card.title">
        <div class="stat-card" :style="{ borderLeftColor: card.color }">
          <div class="stat-icon" :style="{ background: card.bg }">{{ card.icon }}</div>
          <div class="stat-info">
            <div class="stat-value">
              <span v-if="!loading">{{ card.value }}</span>
              <el-skeleton-item v-else variant="text" style="width: 40px" />
            </div>
            <div class="stat-label">{{ card.title }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :xs="24" :lg="14">
        <div class="card">
          <div class="card-header">
            <span>最近健康档案</span>
            <router-link to="/records" class="view-all">查看全部</router-link>
          </div>
          <el-skeleton :loading="loading" animated>
            <template #template>
              <el-skeleton-item v-for="i in 3" :key="i" variant="p" style="margin-bottom: 12px" />
            </template>
            <template #default>
              <div v-if="records.length === 0" class="empty-tip">暂无健康档案，<router-link to="/records">去创建</router-link></div>
              <div v-for="r in records.slice(0, 5)" :key="r.id" class="record-item">
                <div class="record-avatar">{{ r.name.charAt(0) }}</div>
                <div class="record-info">
                  <span class="record-name">{{ r.name }}</span>
                  <span class="record-meta">
                    {{ r.gender === 1 ? '男' : '女' }} · {{ r.age }}岁 · {{ r.bloodType || '未知' }}型血
                  </span>
                </div>
                <el-tag size="small" type="success">档案正常</el-tag>
              </div>
            </template>
          </el-skeleton>
        </div>
      </el-col>

      <el-col :xs="24" :lg="10">
        <div class="card">
          <div class="card-header">
            <span>体质辨识结果</span>
            <router-link to="/constitution" class="view-all">去测评</router-link>
          </div>
          <el-skeleton :loading="loading" animated>
            <template #template>
              <el-skeleton-item variant="p" style="margin-bottom: 12px" />
            </template>
            <template #default>
              <div v-if="constitutionResults.length === 0" class="empty-tip">
                尚未进行体质辨识，<router-link to="/constitution">立即测评</router-link>
              </div>
              <div v-else>
                <div class="constitution-result-card">
                  <div class="constitution-badge">{{ constitutionResults[0].resultName }}</div>
                  <p class="constitution-desc">{{ constitutionResults[0].description }}</p>
                  <el-divider />
                  <p class="constitution-advice">💡 {{ constitutionResults[0].advice }}</p>
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { recordApi } from '@/api/record'
import { chronicApi } from '@/api/chronic'
import { constitutionApi } from '@/api/constitution'

const userStore = useUserStore()
const loading = ref(true)
const records = ref([])
const chronicCount = ref(0)
const constitutionResults = ref([])

const statCards = computed(() => [
  { title: '健康档案', value: records.value.length, icon: '📋', color: '#3a7d3a', bg: '#e8f5e9' },
  { title: '慢病记录', value: chronicCount.value, icon: '💊', color: '#e67e22', bg: '#fef5ec' },
  { title: '体质测评次数', value: constitutionResults.value.length, icon: '🔬', color: '#3498db', bg: '#ecf4fb' },
  { title: '健康评级', value: '良好', icon: '⭐', color: '#9b59b6', bg: '#f5eefb' }
])

onMounted(async () => {
  try {
    const [recordRes, constRes] = await Promise.all([
      recordApi.list(),
      constitutionApi.getResults()
    ])
    records.value = recordRes.data || []
    constitutionResults.value = constRes.data || []

    if (records.value.length > 0) {
      let total = 0
      for (const r of records.value) {
        const res = await chronicApi.list(r.id)
        total += (res.data || []).length
      }
      chronicCount.value = total
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}

.welcome-banner {
  background: linear-gradient(135deg, #2d4a2d, #3a7d3a);
  border-radius: 12px;
  padding: 28px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  color: white;
}

.welcome-text h2 {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 6px;
}

.welcome-text p {
  font-size: 14px;
  opacity: 0.85;
}

.welcome-icon {
  font-size: 56px;
  opacity: 0.8;
}

.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border-left: 4px solid #3a7d3a;
  transition: transform 0.2s, box-shadow 0.2s;
  margin-bottom: 20px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.10);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.section-row {
  margin-bottom: 24px;
}

.card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
}

.view-all {
  font-size: 13px;
  color: #3a7d3a;
  text-decoration: none;
}

.view-all:hover { text-decoration: underline; }

.record-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.record-item:last-child { border-bottom: none; }

.record-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3a7d3a, #5ca05c);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.record-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.record-name {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
}

.record-meta {
  font-size: 12px;
  color: #909399;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 32px 0;
  font-size: 14px;
}

.empty-tip a { color: #3a7d3a; }

.constitution-result-card {
  background: linear-gradient(135deg, #f0f9f0, #e8f5e9);
  border-radius: 10px;
  padding: 16px;
}

.constitution-badge {
  display: inline-block;
  background: linear-gradient(135deg, #3a7d3a, #5ca05c);
  color: white;
  padding: 4px 16px;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 10px;
}

.constitution-desc {
  font-size: 13px;
  color: #4a5568;
  line-height: 1.6;
}

.constitution-advice {
  font-size: 13px;
  color: #2d6a2d;
  line-height: 1.6;
}
</style>
