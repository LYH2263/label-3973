<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span class="logo-icon">🌿</span>
        <span class="logo-text">中医健康管理</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="transparent"
        text-color="#c9d1d9"
        active-text-color="#ffffff"
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页概览</span>
        </el-menu-item>
        <el-menu-item index="/records">
          <el-icon><UserFilled /></el-icon>
          <span>健康档案</span>
        </el-menu-item>
        <el-menu-item index="/chronic">
          <el-icon><FirstAidKit /></el-icon>
          <span>慢病管理</span>
        </el-menu-item>
        <el-menu-item index="/indicators">
          <el-icon><TrendCharts /></el-icon>
          <span>健康指标</span>
        </el-menu-item>
        <el-menu-item index="/constitution">
          <el-icon><MagicStick /></el-icon>
          <span>体质辨识</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" class="avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const pageTitles = {
  '/dashboard': '首页概览',
  '/records': '健康档案',
  '/chronic': '慢病管理',
  '/indicators': '健康指标',
  '/constitution': '体质辨识'
}

const pageTitle = computed(() => pageTitles[route.path] || '中医健康管理')

async function handleCommand(cmd) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  background: linear-gradient(180deg, #1a2a1a 0%, #2d4a2d 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 15px;
  font-weight: 600;
  color: #a8d5a2;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  padding: 8px 0;
}

.sidebar-menu :deep(.el-menu-item) {
  border-radius: 8px;
  margin: 2px 10px;
  padding-left: 16px !important;
  transition: all 0.2s;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: rgba(168, 213, 162, 0.15) !important;
  color: #a8d5a2 !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: rgba(168, 213, 162, 0.25) !important;
  color: #ffffff !important;
}

.header {
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  height: 60px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
}

.avatar {
  background: linear-gradient(135deg, #3a7d3a, #5ca05c);
  color: white;
  font-weight: 600;
}

.username {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main-content {
  background: #f5f7fa;
  padding: 24px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
