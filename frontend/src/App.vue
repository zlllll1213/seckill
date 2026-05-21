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
* { margin: 0; padding: 0; }
#app { min-height: 100vh; isolation: isolate; }
.fade-slide-enter-active,
.fade-slide-leave-active { transition: opacity 0.18s ease, transform 0.18s ease, filter 0.18s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(12px); filter: blur(5px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-8px); filter: blur(3px); }
</style>
