<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">🌿</div>
        <h1 class="auth-title">中医药健康管理系统</h1>
        <p class="auth-subtitle">传承中医智慧，守护健康未来</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="auth-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            size="large"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            size="large"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          class="submit-btn"
          :loading="loading"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>

      <div class="auth-footer">
        还没有账号？
        <router-link to="/register" class="link">立即注册</router-link>
      </div>

      <el-divider>演示账号</el-divider>
      <div class="demo-accounts">
        <el-button size="small" plain @click="fillDemo('demo', 'demo123')">演示用户 demo/demo123</el-button>
        <el-button size="small" plain @click="fillDemo('admin', 'admin123')">管理员 admin/admin123</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function fillDemo(username, password) {
  form.username = username
  form.password = password
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await authApi.login(form)
    userStore.setAuth(res.data.token, res.data.user)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 50%, #a5d6a7 100%);
}

.auth-card {
  width: 420px;
  background: #ffffff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-logo {
  font-size: 48px;
  margin-bottom: 12px;
}

.auth-title {
  font-size: 22px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 6px;
}

.auth-subtitle {
  font-size: 13px;
  color: #909399;
}

.auth-form {
  margin-bottom: 16px;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.submit-btn {
  width: 100%;
  border-radius: 8px;
  font-size: 16px;
  height: 44px;
  background: linear-gradient(135deg, #3a7d3a, #5ca05c);
  border-color: transparent;
  letter-spacing: 2px;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #2e6b2e, #4a8a4a);
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
  margin-bottom: 16px;
}

.link {
  color: #3a7d3a;
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}

.demo-accounts {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}
</style>
