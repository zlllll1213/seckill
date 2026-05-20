<template>
  <el-config-provider :locale="zhCn">
    <NavBar v-if="showNav" />
    <router-view v-slot="{ Component, route }">
      <transition name="fade-slide" mode="out-in">
        <component :is="Component" :key="route.fullPath" />
      </transition>
    </router-view>
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import NavBar from '@/components/NavBar.vue'

const route = useRoute()
const showNav = computed(() => !['login', 'register'].includes(route.name))
</script>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #f5f7fa; }
#app { min-height: 100vh; }
.fade-slide-enter-active,
.fade-slide-leave-active { transition: opacity 0.22s ease, transform 0.22s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(10px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-8px); }
</style>
