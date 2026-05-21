<template>
  <div class="page admin-page">
    <div class="page-header">
      <div>
        <span class="section-code">CONTROL ROOM</span>
        <h2>数据看板</h2>
      </div>
    </div>
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-num">{{ stats.totalOrders }}</div>
          <div class="stat-label">总订单数</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-num">{{ stats.totalProducts }}</div>
          <div class="stat-label">商品数量</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-num">{{ stats.activeActivities }}</div>
          <div class="stat-label">进行中活动</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="never">
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
.admin-page { max-width: 1200px; margin: 0 auto; padding: 34px 24px 72px; }
.page-header { margin-bottom: 22px; }
.section-code { display: block; color: #ff3a32; font-size: 12px; font-weight: 900; margin-bottom: 9px; }
h2 {
  color: var(--text);
  font-family: var(--font-display);
  font-size: 44px;
  font-weight: 900;
  line-height: 1;
}
.stats-row { margin-bottom: 24px; }
.stat-card {
  position: relative;
  overflow: hidden;
  min-height: 150px;
  padding: 16px 0;
}
.stat-card::after {
  content: "";
  position: absolute;
  inset: auto 18px 18px 18px;
  height: 3px;
  background: linear-gradient(90deg, #e21218 68%, rgba(255, 255, 255, 0.22) 68%);
}
.stat-num {
  color: #ff3a32;
  font-family: var(--font-display);
  font-size: 42px;
  font-weight: 900;
  text-shadow: 0 0 18px rgba(226, 18, 24, 0.28);
}
.stat-label { color: var(--muted); margin-top: 8px; font-weight: 800; }
</style>
