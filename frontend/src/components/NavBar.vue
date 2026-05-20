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
import { onMounted, onUnmounted, ref } from 'vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const scrolled = ref(false)

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
})
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
.navbar {
  background: rgba(255, 255, 255, 0.94);
  border-bottom: 1px solid rgba(228, 231, 237, 0.72);
  padding: 0 max(24px, calc((100vw - 1200px) / 2));
  height: 72px;
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(18px);
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}

.navbar-inner {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
}

.navbar-scrolled {
  border-bottom-color: transparent;
  box-shadow: 0 14px 34px rgba(31, 41, 55, 0.08);
}
.navbar-brand {
  display: flex;
  align-items: center;
  gap: 11px;
  color: #e64545;
  font-size: 22px;
  font-weight: 850;
  cursor: pointer;
  margin-right: 42px;
  white-space: nowrap;
  letter-spacing: 0;
}
.brand-mark {
  position: relative;
  width: 36px;
  height: 36px;
  border: 3px solid currentColor;
  border-radius: 50%;
  box-shadow: 0 10px 22px rgba(230, 69, 69, 0.16);
}
.brand-mark::before {
  content: "";
  position: absolute;
  width: 3px;
  height: 10px;
  left: 50%;
  top: -10px;
  border-radius: 99px;
  background: currentColor;
  transform: translateX(-50%);
}
.brand-mark::after {
  content: "";
  position: absolute;
  left: 11px;
  top: 7px;
  width: 10px;
  height: 16px;
  background: currentColor;
  clip-path: polygon(52% 0, 100% 0, 66% 42%, 100% 42%, 28% 100%, 45% 56%, 0 56%);
}

.navbar-menu {
  flex: 1;
  border-bottom: none;
  background: transparent;
}

.navbar-menu :deep(.el-menu-item),
.navbar-menu :deep(.el-sub-menu__title) {
  height: 72px;
  padding: 0 18px;
  color: #202637;
  font-size: 15px;
  font-weight: 650;
  border-bottom-width: 3px;
}

.navbar-menu :deep(.el-menu-item.is-active),
.navbar-menu :deep(.el-sub-menu.is-active .el-sub-menu__title) {
  color: #e21d2b;
}

.navbar-right { display: flex; align-items: center; gap: 8px; }
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
  background: #e21d2b;
  font-size: 11px;
  line-height: 1;
}
.username {
  color: #606266;
  font-size: 14px;
  cursor: pointer;
}
@media (max-width: 760px) {
  .navbar { height: auto; min-height: 60px; padding: 10px 16px; }
  .navbar-inner { flex-wrap: wrap; gap: 8px; }
  .navbar-brand { width: 100%; margin-right: 0; }
  .navbar-menu { width: 100%; overflow-x: auto; }
  .navbar-menu :deep(.el-menu-item),
  .navbar-menu :deep(.el-sub-menu__title) { height: 44px; padding: 0 14px; }
  .navbar-right { width: 100%; justify-content: flex-end; }
}
</style>
