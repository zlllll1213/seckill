<template>
  <div class="page">
    <h2>我的订单</h2>
    <el-card class="table-card">
      <el-skeleton :loading="loading" animated :rows="6">
        <template #default>
          <el-table v-if="orders.length" :data="orders" stripe>
            <el-table-column prop="id" label="订单ID" width="100" />
            <el-table-column prop="activityId" label="活动ID" width="100" />
            <el-table-column prop="price" label="金额">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="下单时间" min-width="170" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button text type="primary" @click="$router.push(`/orders/${row.id}`)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无订单">
            <el-button type="primary" @click="$router.push('/products')">去看看商品</el-button>
          </el-empty>
        </template>
      </el-skeleton>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api'

const orders = ref([])
const loading = ref(false)

const statusLabel = s => ['待支付', '已支付', '已取消'][s] ?? '未知'
const statusType = s => ['warning', 'success', 'info'][s] ?? ''

onMounted(async () => {
  loading.value = true
  try {
    const res = await orderApi.myOrders()
    orders.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page { max-width: 1000px; margin: 0 auto; padding: 24px; }
h2 { margin-bottom: 20px; }
.table-card { border-radius: 8px; }
</style>
