<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">🌿</div>
        <h1 class="auth-title">创建账号</h1>
        <p class="auth-subtitle">加入中医药健康管理系统</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="auth-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" size="large" placeholder="用户名（3-20位）" prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" size="large" type="password" placeholder="密码（6-30位）" prefix-icon="Lock" show-password clearable />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" size="large" type="password" placeholder="确认密码" prefix-icon="Lock" show-password clearable />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" size="large" placeholder="昵称（可选）" prefix-icon="Avatar" clearable />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" size="large" placeholder="手机号（可选）" prefix-icon="Phone" clearable />
        </el-form-item>
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleRegister">
          注 册
        </el-button>
      </el-form>

      <div class="auth-footer">
        已有账号？<router-link to="/login" class="link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '', confirmPassword: '', nickname: '', phone: '' })

const validateConfirmPwd = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度6-30位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authApi.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      phone: form.phone
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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
  margin-bottom: 28px;
}

.auth-logo { font-size: 40px; margin-bottom: 10px; }
.auth-title { font-size: 22px; font-weight: 700; color: #2c3e50; margin-bottom: 6px; }
.auth-subtitle { font-size: 13px; color: #909399; }

.auth-form { margin-bottom: 16px; }
.auth-form :deep(.el-input__wrapper) { border-radius: 8px; }

.submit-btn {
  width: 100%;
  border-radius: 8px;
  font-size: 16px;
  height: 44px;
  background: linear-gradient(135deg, #3a7d3a, #5ca05c);
  border-color: transparent;
  letter-spacing: 2px;
}

.auth-footer { text-align: center; font-size: 14px; color: #909399; }
.link { color: #3a7d3a; text-decoration: none; font-weight: 500; }
.link:hover { text-decoration: underline; }
</style>
