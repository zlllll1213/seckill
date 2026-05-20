<template>
  <div class="page product-list-page" :style="{ '--product-bg': `url(${heroImage})` }">
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
import heroImage from '@/assets/images/generated/flash-sale-bg-v2.png'

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
  min-height: calc(100vh - 72px);
  margin: 0;
  padding: 46px max(32px, calc((100vw - 1200px) / 2)) 72px;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 255, 255, 0.9) 35%, rgba(255, 250, 250, 0.48) 66%, rgba(255, 245, 245, 0.2) 100%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.12) 0%, rgba(248, 250, 252, 0.7) 100%),
    var(--product-bg) center center / cover no-repeat;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 72px;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 18px;
}

.title-mark {
  width: 5px;
  height: 38px;
  border-radius: 99px;
  background: linear-gradient(180deg, #ff3b30 0%, #d71920 100%);
  box-shadow: 0 12px 26px rgba(215, 25, 32, 0.28);
}

.page-header h2 {
  color: #172033;
  font-size: 34px;
  font-weight: 850;
  letter-spacing: 0;
  line-height: 1.18;
}

.search-input {
  width: min(420px, 42vw);
  --el-input-height: 48px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(225, 231, 240, 0.92);
  box-shadow: 0 18px 45px rgba(143, 46, 46, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(18px);
}

.search-input :deep(.el-input__inner) {
  color: #172033;
  font-size: 15px;
}

.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; }
.product-skeleton { height: 292px; border-radius: 14px; }
.pagination { margin-top: 24px; justify-content: center; display: flex; }

.empty-state {
  width: min(340px, 100%);
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-left: clamp(120px, 20vw, 260px);
  padding: 26px 18px;
  text-align: center;
}

.empty-state p {
  margin: 18px 0 24px;
  color: #172033;
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
  border-radius: 14px;
  font-size: 15px;
  font-weight: 740;
  box-shadow: 0 16px 34px rgba(215, 25, 32, 0.24);
}

@media (max-width: 640px) {
  .product-list-page {
    min-height: calc(100vh - 120px);
    padding: 28px 18px 48px;
    background:
      linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 255, 255, 0.82) 50%, rgba(255, 247, 247, 0.68) 100%),
      var(--product-bg) 65% bottom / auto 74% no-repeat;
  }
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
