import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    children: [
      { path: '', redirect: '/agent' },
      { path: 'agent', component: () => import('@/views/AgentView.vue') },
      { path: 'dashboard', component: () => import('@/views/DashboardView.vue') },
      { path: 'analytics', component: () => import('@/views/AnalyticsView.vue') },
      { path: 'forecast', component: () => import('@/views/ForecastView.vue') },
      { path: 'anomaly', component: () => import('@/views/AnomalyView.vue') },
      { path: 'recent', component: () => import('@/views/RecentDataView.vue') },
      { path: 'application', component: () => import('@/views/ApplicationView.vue') },
      { path: 'system', component: () => import('@/views/SystemStatusView.vue') },
      { path: 'about', component: () => import('@/views/AboutView.vue') },
    ],
  },
]

export default createRouter({
  history: createWebHashHistory(),
  routes,
})
