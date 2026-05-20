import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  { path: '/', redirect: '/products' },
  { path: '/login', redirect: to => ({ path: '/profile', query: { ...to.query, mode: 'login' } }) },
  { path: '/register', redirect: to => ({ path: '/profile', query: { ...to.query, mode: 'register' } }) },
  { path: '/profile', name: 'profile', component: () => import('@/views/ProfileView.vue') },
  { path: '/products', name: 'products', component: () => import('@/views/ProductListView.vue') },
  { path: '/products/:id', name: 'product-detail', component: () => import('@/views/ProductDetailView.vue') },
  { path: '/cart', name: 'cart', component: () => import('@/views/CartView.vue') },
  { path: '/seckill/:id', name: 'seckill', component: () => import('@/views/SeckillView.vue'), meta: { requiresAuth: true } },
  { path: '/orders', name: 'orders', component: () => import('@/views/OrderListView.vue'), meta: { requiresAuth: true } },
  { path: '/orders/:id', name: 'order-detail', component: () => import('@/views/OrderDetailView.vue'), meta: { requiresAuth: true } },
  {
    path: '/admin',
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: 'dashboard', name: 'admin-dashboard', component: () => import('@/views/admin/DashboardView.vue') },
      { path: 'products', name: 'admin-products', component: () => import('@/views/admin/ProductManageView.vue') },
      { path: 'activities', name: 'admin-activities', component: () => import('@/views/admin/ActivityManageView.vue') },
      { path: 'orders', name: 'admin-orders', component: () => import('@/views/admin/OrderManageView.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return { name: 'profile', query: { mode: 'login', redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && userStore.role !== 'ADMIN') {
    return { name: 'products' }
  }
})

export default router
