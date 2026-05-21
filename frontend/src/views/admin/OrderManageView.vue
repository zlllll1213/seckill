<template>
  <div class="page admin-page">
    <span class="section-code">ORDER OPS</span>
    <h2>订单管理</h2>
    <el-card class="table-card" shadow="never">
      <el-table :data="orders" v-loading="loading" stripe>
        <el-table-column prop="id" label="订单ID" width="100" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="activityId" label="活动ID" width="100" />
        <el-table-column prop="productId" label="商品ID" width="100" />
        <el-table-column prop="price" label="金额"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['warning','success','info'][row.status]">{{ ['待支付','已支付','已取消'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" min-width="170" />
      </el-table>
      <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />
    </el-card>
    <el-pagination
      v-if="total > pageSize"
      class="pagination"
      layout="prev, pager, next"
      :current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      @current-change="fetchOrders"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api'

const orders = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

async function fetchOrders(page = 1) {
  loading.value = true
  currentPage.value = page
  try {
    const res = await orderApi.allOrders(page, pageSize.value)
    orders.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

onMounted(() => fetchOrders())
</script>

<style scoped>
.admin-page { max-width: 1240px; margin: 0 auto; padding: 34px 24px 72px; }
.section-code { display: block; color: #ff3a32; font-size: 12px; font-weight: 900; margin-bottom: 9px; }
h2 { margin-bottom: 20px; color: var(--text); font-family: var(--font-display); font-size: 42px; font-weight: 900; line-height: 1; }
.table-card { border-radius: 2px; }
.pagination { margin-top: 20px; justify-content: center; display: flex; }
</style>
