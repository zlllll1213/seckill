<template>
  <div class="page">
    <h2>数据看板</h2>
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.totalOrders }}</div>
          <div class="stat-label">总订单数</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.totalProducts }}</div>
          <div class="stat-label">商品数量</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.activeActivities }}</div>
          <div class="stat-label">进行中活动</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-num">¥{{ stats.totalRevenue }}</div>
          <div class="stat-label">总收入</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api'

const stats = ref({ totalOrders: 0, totalProducts: 0, activeActivities: 0, totalRevenue: 0 })

onMounted(async () => {
  const res = await adminApi.stats()
  stats.value = res.data
})
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; padding: 24px; }
h2 { margin-bottom: 24px; }
.stats-row { margin-bottom: 24px; }
.stat-card { text-align: center; padding: 16px 0; }
.stat-num { font-size: 36px; font-weight: bold; color: #e64545; }
.stat-label { color: #909399; margin-top: 8px; }
</style>
