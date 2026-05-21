<template>
  <div class="page order-detail-page">
    <el-button @click="$router.back()" class="back-btn">返回</el-button>
    <el-skeleton :loading="loading" animated :rows="5">
      <template #default>
        <el-card v-if="order" class="detail-card" shadow="never">
          <template #header><span>订单详情</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单ID">{{ order.id }}</el-descriptions-item>
            <el-descriptions-item label="活动ID">{{ order.activityId }}</el-descriptions-item>
            <el-descriptions-item label="商品ID">{{ order.productId }}</el-descriptions-item>
            <el-descriptions-item label="金额">¥{{ order.price }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusType(order.status)">{{ statusLabel(order.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ order.createdAt }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { orderApi } from '@/api'

const route = useRoute()
const loading = ref(false)
const order = ref(null)

const statusLabel = s => ['待支付', '已支付', '已取消'][s] ?? '未知'
const statusType = s => ['warning', 'success', 'info'][s] ?? ''

onMounted(async () => {
  loading.value = true
  try {
    const res = await orderApi.detail(route.params.id)
    order.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.order-detail-page { max-width: 760px; margin: 0 auto; padding: 34px 24px 72px; }
.back-btn { margin-bottom: 16px; }
.detail-card { border-radius: 2px; }
.detail-card :deep(.el-card__header) {
  color: var(--text);
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 900;
  border-bottom-color: var(--line);
}
</style>
