<template>
  <div class="page detail-page">
    <el-button @click="$router.back()" icon="ArrowLeft" class="back-btn">返回</el-button>
    <el-skeleton :loading="loading" animated>
      <template #template>
        <el-skeleton-item variant="rect" class="detail-skeleton" />
      </template>
      <template #default>
    <template v-if="product">
      <el-card class="detail-card">
        <div class="detail-body">
          <el-image v-if="product.imageUrl" :src="product.imageUrl" :alt="product.name" lazy fit="cover" class="detail-img">
            <template #error>
              <div class="detail-img-placeholder">暂无图片</div>
            </template>
          </el-image>
          <div v-else class="detail-img-placeholder">暂无图片</div>
          <div class="detail-info">
            <h1>{{ product.name }}</h1>
            <div class="detail-price">¥{{ product.price }}</div>
            <p class="detail-desc">{{ product.description }}</p>
            <div class="meta-row">
              <el-tag :type="product.status === 1 ? 'success' : 'danger'">
                {{ product.status === 1 ? '在售' : '已下架' }}
              </el-tag>
              <span>库存 {{ product.stock }}</span>
            </div>

            <div class="purchase-panel">
              <label>购买数量</label>
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="product.stock || 999"
                controls-position="right"
              />
              <div class="purchase-actions">
                <el-button :disabled="!canBuy" size="large" @click="handleAddToCart">加入购物车</el-button>
                <el-button type="primary" :disabled="!canBuy" size="large" @click="handleBuyNow">立即购买</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <div v-if="activities.length" class="activities">
        <h3>秒杀活动</h3>
        <el-card v-for="act in activities" :key="act.id" class="act-card">
          <div class="act-info">
            <div>
              <div class="act-name">{{ act.name }}</div>
              <div class="act-price">秒杀价：<span>¥{{ act.seckillPrice }}</span></div>
              <div class="act-time">{{ act.startTime }} ~ {{ act.endTime }}</div>
            </div>
            <el-button type="danger" @click="$router.push(`/seckill/${act.id}`)">去秒杀</el-button>
          </div>
        </el-card>
      </div>
    </template>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productApi, activityApi } from '@/api'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const loading = ref(false)
const product = ref(null)
const activities = ref([])
const quantity = ref(1)

const canBuy = computed(() => product.value?.status === 1 && Number(product.value?.stock || 0) > 0)

onMounted(async () => {
  loading.value = true
  try {
    const [pRes, aRes] = await Promise.all([
      productApi.detail(route.params.id),
      activityApi.list()
    ])
    product.value = pRes.data
    activities.value = aRes.data.filter(a => a.productId == route.params.id)
  } finally {
    loading.value = false
  }
})

function handleAddToCart() {
  if (!product.value) return
  cartStore.addItem(product.value, quantity.value)
  ElMessage.success('已加入购物车')
}

function handleBuyNow() {
  handleAddToCart()
  router.push('/cart')
}
</script>

<style scoped>
.detail-page {
  min-height: calc(100vh - 72px);
  max-width: none;
  margin: 0;
  padding: 32px max(24px, calc((100vw - 1100px) / 2)) 72px;
  background:
    radial-gradient(circle at 84% 18%, rgba(226, 29, 43, 0.1), transparent 30%),
    linear-gradient(180deg, #fff 0%, #f8fafc 100%);
}
.back-btn { margin-bottom: 16px; }
.detail-card {
  margin-bottom: 24px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 24px 70px rgba(31, 41, 55, 0.1);
}
.detail-card :deep(.el-card__body) { padding: 28px; }
.detail-body { display: flex; gap: 36px; }
.detail-img,
.detail-img-placeholder { width: 420px; height: 360px; border-radius: 18px; flex-shrink: 0; }
.detail-img-placeholder { display: flex; align-items: center; justify-content: center; color: #c0c4cc; background: #f5f7fa; }
.detail-info { flex: 1; }
.detail-info h1 {
  color: #172033;
  font-size: 34px;
  line-height: 1.18;
  margin-bottom: 14px;
}
.detail-price { font-size: 34px; color: #e21d2b; font-weight: 850; margin: 12px 0; }
.detail-desc { color: #606266; line-height: 1.8; margin-bottom: 18px; }
.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #6b7280;
  margin-bottom: 22px;
}
.purchase-panel {
  display: grid;
  gap: 14px;
  padding: 20px;
  border-radius: 18px;
  background: rgba(248, 250, 252, 0.86);
  border: 1px solid rgba(226, 232, 240, 0.88);
}
.purchase-panel label {
  color: #172033;
  font-size: 14px;
  font-weight: 760;
}
.purchase-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.purchase-actions .el-button {
  min-width: 138px;
  border-radius: 14px;
}
.activities h3 { margin-bottom: 12px; }
.act-card { margin-bottom: 12px; border-radius: 16px; }
.act-info { display: flex; justify-content: space-between; align-items: center; }
.act-name { font-weight: 500; font-size: 16px; margin-bottom: 4px; }
.act-price span { color: #f56c6c; font-size: 20px; font-weight: bold; }
.act-time { color: #909399; font-size: 13px; margin-top: 4px; }
.detail-skeleton { height: 340px; border-radius: 8px; }
@media (max-width: 760px) {
  .detail-page { min-height: calc(100vh - 120px); padding: 24px 18px 48px; }
  .detail-body { flex-direction: column; }
  .detail-img,
  .detail-img-placeholder { width: 100%; }
  .detail-card :deep(.el-card__body) { padding: 18px; }
  .detail-info h1 { font-size: 28px; }
  .purchase-actions .el-button { flex: 1; min-width: 0; }
  .act-info { align-items: flex-start; flex-direction: column; gap: 12px; }
}
</style>
