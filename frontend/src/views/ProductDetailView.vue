<template>
  <div class="page detail-page">
    <el-button @click="$router.back()" icon="ArrowLeft" class="back-btn">返回</el-button>
    <el-skeleton :loading="loading" animated>
      <template #template>
        <el-skeleton-item variant="rect" class="detail-skeleton" />
      </template>
      <template #default>
    <template v-if="product">
      <el-card class="detail-card" shadow="never">
        <div class="detail-body">
          <el-image v-if="product.imageUrl" :src="product.imageUrl" :alt="product.name" lazy fit="cover" class="detail-img">
            <template #error>
              <div class="detail-img-placeholder">暂无图片</div>
            </template>
          </el-image>
          <div v-else class="detail-img-placeholder">暂无图片</div>
          <div class="detail-info">
            <span class="detail-code">PRODUCT NODE / {{ product.status === 1 ? 'READY' : 'OFFLINE' }}</span>
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
        <el-card v-for="act in activities" :key="act.id" class="act-card" shadow="never">
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
    radial-gradient(circle at 84% 18%, rgba(226, 18, 24, 0.16), transparent 30%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.02) 0%, transparent 100%);
}
.back-btn { margin-bottom: 16px; }
.detail-card {
  margin-bottom: 24px;
  border-radius: 2px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.06), transparent 48%),
    var(--panel);
  box-shadow: var(--shadow-panel);
}
.detail-card :deep(.el-card__body) { padding: 28px; }
.detail-body { display: flex; gap: 36px; }
.detail-img,
.detail-img-placeholder {
  width: 420px;
  height: 360px;
  border: 1px solid var(--line);
  border-radius: 2px;
  flex-shrink: 0;
  background:
    linear-gradient(rgba(255, 255, 255, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.035) 1px, transparent 1px),
    #111315;
  background-size: 24px 24px;
}
.detail-img-placeholder { display: flex; align-items: center; justify-content: center; color: var(--muted); }
.detail-info { flex: 1; }
.detail-code {
  display: block;
  color: #ff3a32;
  font-size: 12px;
  font-weight: 900;
  margin-bottom: 12px;
}
.detail-info h1 {
  color: var(--text);
  font-family: var(--font-display);
  font-size: clamp(36px, 5vw, 58px);
  line-height: 0.98;
  margin-bottom: 14px;
}
.detail-price {
  color: #ff3a32;
  font-family: var(--font-display);
  font-size: 48px;
  font-weight: 900;
  margin: 18px 0 12px;
  text-shadow: 0 0 18px rgba(226, 18, 24, 0.3);
}
.detail-desc { color: var(--muted-2); line-height: 1.8; margin-bottom: 18px; }
.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--muted);
  margin-bottom: 22px;
}
.purchase-panel {
  display: grid;
  gap: 14px;
  padding: 20px;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--line);
}
.purchase-panel label {
  color: var(--text);
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
  border-radius: 2px;
}
.activities h3 {
  margin-bottom: 12px;
  color: var(--text);
  font-family: var(--font-display);
  font-size: 28px;
}
.act-card { margin-bottom: 12px; border-radius: 2px; }
.act-info { display: flex; justify-content: space-between; align-items: center; }
.act-name { color: var(--text); font-weight: 800; font-size: 16px; margin-bottom: 4px; }
.act-price { color: var(--muted-2); }
.act-price span { color: #ff3a32; font-size: 22px; font-weight: 900; }
.act-time { color: var(--muted); font-size: 13px; margin-top: 4px; }
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
