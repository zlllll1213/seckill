<template>
  <el-header class="navbar" :class="{ 'navbar-scrolled': scrolled }">
    <div class="navbar-inner">
      <div class="navbar-brand" @click="router.push('/products')">
        <span class="brand-mark" aria-hidden="true"></span>
        <span>秒杀系统</span>
      </div>
      <el-menu mode="horizontal" :router="true" :default-active="route.path" class="navbar-menu">
        <el-menu-item index="/products">商品列表</el-menu-item>
        <el-menu-item index="/cart">
          <span class="cart-menu-item">
            购物车
            <span v-if="cartStore.totalCount" class="cart-badge">{{ cartStore.totalCount }}</span>
          </span>
        </el-menu-item>
        <el-menu-item v-if="userStore.isLoggedIn" index="/orders">我的订单</el-menu-item>
        <el-menu-item index="/profile">个人中心</el-menu-item>
        <template v-if="userStore.isAdmin">
          <el-sub-menu index="admin">
            <template #title>管理后台</template>
            <el-menu-item index="/admin/dashboard">数据看板</el-menu-item>
            <el-menu-item index="/admin/products">商品管理</el-menu-item>
            <el-menu-item index="/admin/activities">秒杀活动</el-menu-item>
            <el-menu-item index="/admin/orders">订单管理</el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
      <div class="navbar-right">
        <span class="status-dot">LIVE</span>
        <span class="clock">{{ nowText }}</span>
        <template v-if="userStore.isLoggedIn">
          <span class="username" @click="router.push('/profile')">{{ userStore.username }}</span>
          <el-button type="danger" text title="退出登录" @click="handleLogout">退出</el-button>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { computed, onMounted, onUnmounted, ref } from 'vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const scrolled = ref(false)
const now = ref(new Date())
let clockTimer = null

const nowText = computed(() => now.value.toLocaleTimeString('zh-CN', { hour12: false }))

function onScroll() {
  scrolled.value = window.scrollY > 0
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })
  clockTimer = window.setInterval(() => {
    now.value = new Date()
  }, 1000)
})
onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  if (clockTimer) window.clearInterval(clockTimer)
})
</script>

<style scoped>
.navbar {
  background: rgba(5, 6, 7, 0.92);
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  padding: 0 max(24px, calc((100vw - 1200px) / 2));
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(18px) saturate(1.2);
  transition: box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.navbar-inner {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
}

.navbar-scrolled {
  background: rgba(5, 6, 7, 0.98);
  border-bottom-color: rgba(226, 18, 24, 0.32);
  box-shadow: 0 18px 48px rgba(0, 0, 0, 0.32);
}
.navbar-brand {
  display: flex;
  align-items: center;
  gap: 11px;
  color: #f4f2ed;
  font-family: var(--font-display);
  font-size: 23px;
  font-weight: 900;
  cursor: pointer;
  margin-right: 42px;
  white-space: nowrap;
  letter-spacing: 0;
}
.brand-mark {
  position: relative;
  width: 18px;
  height: 34px;
  border: 0;
  border-radius: 0;
  background: #f2171d;
  clip-path: polygon(38% 0, 100% 0, 66% 42%, 100% 42%, 16% 100%, 38% 56%, 0 56%);
  box-shadow: 0 0 24px rgba(242, 23, 29, 0.6);
}

.navbar-menu {
  flex: 1;
  border-bottom: none;
  background: transparent;
  --el-menu-active-color: #fff;
  --el-menu-hover-text-color: #fff;
  --el-menu-text-color: var(--muted);
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.05);
  --el-menu-bg-color: transparent;
}

.navbar-menu :deep(.el-menu-item),
.navbar-menu :deep(.el-sub-menu__title) {
  height: 64px;
  padding: 0 17px;
  color: var(--muted);
  font-size: 14px;
  font-weight: 760;
  border-bottom: 2px solid transparent;
  letter-spacing: 0;
}

.navbar-menu :deep(.el-menu-item.is-active),
.navbar-menu :deep(.el-sub-menu.is-active .el-sub-menu__title) {
  color: #fff !important;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.08), transparent),
    rgba(226, 18, 24, 0.88);
  border-bottom-color: #ff3a32;
  box-shadow: inset 0 0 22px rgba(255, 58, 50, 0.18);
}

.navbar-menu :deep(.el-menu-item:hover),
.navbar-menu :deep(.el-sub-menu__title:hover) {
  color: #fff;
  background: rgba(255, 255, 255, 0.05);
}

.navbar-right { display: flex; align-items: center; gap: 12px; }
.cart-menu-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.cart-badge {
  min-width: 18px;
  height: 18px;
  display: inline-grid;
  place-items: center;
  padding: 0 5px;
  border-radius: 99px;
  color: #fff;
  background: #e21218;
  font-size: 11px;
  line-height: 1;
  box-shadow: 0 0 14px rgba(226, 18, 24, 0.55);
}
.status-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--success);
  font-size: 11px;
  font-weight: 900;
}
.status-dot::before {
  content: "";
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 12px currentColor;
}
.clock {
  min-width: 72px;
  color: #fff;
  font-family: var(--font-display);
  font-size: 19px;
  font-weight: 900;
  font-variant-numeric: tabular-nums;
}
.username {
  color: var(--muted-2);
  font-size: 14px;
  cursor: pointer;
  border-left: 1px solid var(--line);
  padding-left: 12px;
}
@media (max-width: 760px) {
  .navbar { height: auto; min-height: 60px; padding: 10px 16px; }
  .navbar-inner { flex-wrap: wrap; gap: 8px; }
  .navbar-brand { width: 100%; margin-right: 0; }
  .navbar-menu { width: 100%; overflow-x: auto; }
  .navbar-menu :deep(.el-menu-item),
  .navbar-menu :deep(.el-sub-menu__title) { height: 42px; padding: 0 14px; }
  .navbar-right { width: 100%; justify-content: space-between; }
}
</style>
