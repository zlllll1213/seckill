<template>
  <div class="product-list-page" :style="{ '--product-bg': `url(${heroImage})` }">
    <section class="market-hero">
      <div class="hero-copy">
        <span class="signal">LIVE MARKET / FLASH FORGE</span>
        <h1>今日抢购</h1>
        <p>把商品、库存和秒杀入口压进一块可扫描的交易台，所有关键动作都在第一屏完成。</p>
      </div>
      <div class="hero-timer">
        <span>下一波流量窗口</span>
        <strong>10:00</strong>
        <em>库存脉冲已同步</em>
      </div>
    </section>

    <section class="ops-strip">
      <div>
        <span>商品总数</span>
        <strong>{{ total || products.length }}</strong>
      </div>
      <div>
        <span>在售商品</span>
        <strong>{{ activeCount }}</strong>
      </div>
      <div>
        <span>库存池</span>
        <strong>{{ stockPool }}</strong>
      </div>
      <div>
        <span>检索命中</span>
        <strong>{{ filteredProducts.length }}</strong>
      </div>
    </section>

    <div class="page-header">
      <div class="title-wrap">
        <span class="title-mark" aria-hidden="true"></span>
        <h2>商品列表</h2>
      </div>
      <el-input
        v-model="keyword"
        clearable
        placeholder="搜索商品"
        class="search-input"
      />
    </div>
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="grid">
          <el-skeleton-item v-for="i in pageSize" :key="i" variant="rect" class="product-skeleton" />
        </div>
      </template>
      <template #default>
        <div v-if="filteredProducts.length" class="grid">
          <ProductCard v-for="p in filteredProducts" :key="p.id" :product="p" />
        </div>
        <div v-else class="empty-state">
          <div class="empty-box" aria-hidden="true">
            <span class="box-lid"></span>
            <span class="box-body"></span>
            <span class="box-line"></span>
          </div>
          <p>暂无商品</p>
          <el-button type="primary" class="refresh-button" @click="fetchProducts()">刷新列表</el-button>
        </div>
      </template>
    </el-skeleton>
    <el-pagination
      v-if="total > pageSize"
      :current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      class="pagination"
      @current-change="fetchProducts"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, shallowRef } from 'vue'
import { productApi } from '@/api'
import ProductCard from '@/components/ProductCard.vue'
import heroImage from '@/assets/images/generated/trading-command-concept.png'

const products = shallowRef([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')

const filteredProducts = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return products.value
  return products.value.filter(p => String(p.name || '').toLowerCase().includes(kw))
})
const activeCount = computed(() => products.value.filter(p => p.status === 1).length)
const stockPool = computed(() => products.value.reduce((sum, p) => sum + Number(p.stock || 0), 0))

async function fetchProducts(page = 1) {
  loading.value = true
  currentPage.value = page
  try {
    const res = await productApi.list(page, pageSize.value)
    products.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchProducts())
</script>

<style scoped>
.product-list-page {
  width: 100%;
  max-width: none;
  min-height: calc(100vh - 64px);
  margin: 0;
  padding: 28px max(24px, calc((100vw - 1240px) / 2)) 72px;
}

.market-hero {
  position: relative;
  min-height: 275px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  align-items: end;
  gap: 28px;
  overflow: hidden;
  margin-bottom: 18px;
  padding: 32px;
  border: 1px solid var(--line-strong);
  background:
    linear-gradient(90deg, rgba(6, 7, 8, 0.96) 0%, rgba(6, 7, 8, 0.82) 52%, rgba(6, 7, 8, 0.42) 100%),
    var(--product-bg) center / cover no-repeat;
  box-shadow: var(--shadow-panel);
}

.market-hero::after {
  content: "";
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(110deg, transparent 0 18px, rgba(226, 18, 24, 0.08) 18px 19px);
  pointer-events: none;
}

.hero-copy,
.hero-timer {
  position: relative;
  z-index: 1;
}

.signal {
  display: block;
  color: #ff3a32;
  font-size: 12px;
  font-weight: 900;
  margin-bottom: 12px;
}

.hero-copy h1 {
  font-family: var(--font-display);
  color: #fff;
  font-size: clamp(54px, 8vw, 92px);
  line-height: 0.92;
  font-weight: 900;
  text-shadow: 0 0 28px rgba(226, 18, 24, 0.32);
}

.hero-copy p {
  max-width: 560px;
  margin-top: 18px;
  color: var(--muted-2);
  font-size: 16px;
  line-height: 1.8;
}

.hero-timer {
  padding: 22px;
  border: 1px solid rgba(255, 58, 50, 0.42);
  background: rgba(6, 7, 8, 0.72);
  box-shadow: inset 0 0 28px rgba(226, 18, 24, 0.12);
}

.hero-timer span,
.hero-timer em {
  display: block;
  color: var(--muted);
  font-size: 13px;
  font-style: normal;
  font-weight: 800;
}

.hero-timer strong {
  display: block;
  margin: 9px 0;
  color: #ff3a32;
  font-family: var(--font-display);
  font-size: 62px;
  line-height: 1;
  text-shadow: 0 0 26px rgba(255, 58, 50, 0.48);
}

.ops-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin-bottom: 30px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.035);
}

.ops-strip div {
  padding: 18px 22px;
  border-right: 1px solid var(--line);
}

.ops-strip div:last-child {
  border-right: 0;
}

.ops-strip span {
  display: block;
  color: var(--muted);
  font-size: 12px;
  font-weight: 800;
  margin-bottom: 8px;
}

.ops-strip strong {
  color: var(--text);
  font-family: var(--font-display);
  font-size: 31px;
  font-weight: 900;
  font-variant-numeric: tabular-nums;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 18px;
}

.title-mark {
  width: 5px;
  height: 38px;
  border-radius: 0;
  background: #e21218;
  box-shadow: 0 0 22px rgba(226, 18, 24, 0.44);
}

.page-header h2 {
  color: var(--text);
  font-family: var(--font-display);
  font-size: 34px;
  font-weight: 900;
  letter-spacing: 0;
  line-height: 1.18;
}

.search-input {
  width: min(420px, 42vw);
  --el-input-height: 44px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--line);
  box-shadow: none;
  backdrop-filter: blur(18px);
}

.search-input :deep(.el-input__inner) {
  color: var(--text);
  font-size: 15px;
}

.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 18px; }
.product-skeleton { height: 292px; border-radius: 14px; }
.pagination { margin-top: 24px; justify-content: center; display: flex; }

.empty-state {
  width: min(340px, 100%);
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  padding: 26px 18px;
  text-align: center;
}

.empty-state p {
  margin: 18px 0 24px;
  color: var(--text);
  font-size: 23px;
  font-weight: 760;
  line-height: 1.2;
}

.empty-box {
  position: relative;
  width: 112px;
  height: 96px;
  filter: drop-shadow(0 22px 30px rgba(229, 52, 52, 0.13));
}

.box-lid,
.box-body,
.box-line {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.box-lid {
  top: 18px;
  width: 82px;
  height: 24px;
  border-radius: 8px 8px 4px 4px;
  background: linear-gradient(135deg, #ff8e8e 0%, #ff4f4f 100%);
}

.box-body {
  top: 40px;
  width: 96px;
  height: 52px;
  border-radius: 8px 8px 16px 16px;
  background: linear-gradient(135deg, #fff7f7 0%, #ffcaca 50%, #ff6b6b 100%);
}

.box-line {
  top: 44px;
  width: 8px;
  height: 46px;
  border-radius: 99px;
  background: rgba(255, 255, 255, 0.72);
}

.refresh-button {
  min-width: 132px;
  height: 46px;
  border-radius: 2px;
  font-size: 15px;
  font-weight: 740;
  box-shadow: 0 16px 34px rgba(215, 25, 32, 0.24);
}

@media (max-width: 640px) {
  .product-list-page {
    min-height: calc(100vh - 120px);
    padding: 28px 18px 48px;
  }
  .market-hero { grid-template-columns: 1fr; min-height: 430px; padding: 22px; }
  .hero-copy h1 { font-size: 54px; }
  .hero-timer strong { font-size: 44px; }
  .ops-strip { grid-template-columns: 1fr 1fr; }
  .ops-strip div:nth-child(2n) { border-right: 0; }
  .ops-strip div { padding: 14px; }
  .page-header { align-items: stretch; flex-direction: column; margin-bottom: 34px; }
  .page-header h2 { font-size: 28px; }
  .title-mark { height: 32px; }
  .search-input { width: 100%; }
  .empty-state {
    margin: 0 auto;
    min-height: 280px;
  }
}
</style>
