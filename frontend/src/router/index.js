import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
  {
    path: '/', component: () => import('../layout/MainLayout.vue'), redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/DashboardView.vue'), meta: { title: '全局态势' } },
      { path: 'basic-data', component: () => import('../views/BasicDataView.vue'), meta: { title: '基础数据' } },
      { path: 'rules', component: () => import('../views/RulesView.vue'), meta: { title: '风险规则' } },
      { path: 'state-data', component: () => import('../views/StateDataView.vue'), meta: { title: '模拟状态数据' } },
      { path: 'alerts', component: () => import('../views/AlertsView.vue'), meta: { title: '风险告警' } },
      { path: 'dispatch', component: () => import('../views/DispatchView.vue'), meta: { title: '应急调度' } },
      { path: 'users', component: () => import('../views/UsersView.vue'), meta: { title: '用户权限', roles: ['ADMIN'] } },
      { path: 'configs', component: () => import('../views/ConfigsView.vue'), meta: { title: '系统配置', roles: ['ADMIN'] } },
      { path: 'logs', component: () => import('../views/LogsView.vue'), meta: { title: '操作日志', roles: ['ADMIN'] } }
    ]
  },
  { path: '/:pathMatch(.*)*', component: () => import('../views/NotFoundView.vue') }
]

const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.isLoggedIn) return '/login'
  if (to.path === '/login' && auth.isLoggedIn) return '/dashboard'
  if (to.meta.roles && !to.meta.roles.includes(auth.user?.role)) return '/dashboard'
})
export default router
