import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/components/MainLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue')
      },
      {
        path: 'records',
        name: 'Records',
        component: () => import('@/views/HealthRecordView.vue')
      },
      {
        path: 'chronic',
        name: 'Chronic',
        component: () => import('@/views/ChronicDiseaseView.vue')
      },
      {
        path: 'indicators',
        name: 'Indicators',
        component: () => import('@/views/HealthIndicatorView.vue')
      },
      {
        path: 'constitution',
        name: 'Constitution',
        component: () => import('@/views/ConstitutionView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (!to.meta.public && !userStore.token) {
    return '/login'
  }
  if (to.meta.public && userStore.token && (to.path === '/login' || to.path === '/register')) {
    return '/dashboard'
  }
})

export default router
