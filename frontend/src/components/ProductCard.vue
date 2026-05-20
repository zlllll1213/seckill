<template>
  <el-card class="product-card" shadow="hover" @click="$router.push(`/products/${product.id}`)">
    <el-image v-if="product.imageUrl" :src="product.imageUrl" :alt="product.name" lazy fit="cover" class="product-img">
      <template #error>
        <div class="product-img-placeholder">暂无图片</div>
      </template>
    </el-image>
    <div v-else class="product-img-placeholder">暂无图片</div>
    <div class="product-info">
      <div class="product-name">{{ product.name }}</div>
      <div class="product-price">¥{{ product.price }}</div>
    </div>
  </el-card>
</template>

<script setup>
defineProps({ product: { type: Object, required: true } })
</script>

<style scoped>
.product-card {
  cursor: pointer;
  overflow: hidden;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 18px 44px rgba(31, 41, 55, 0.08);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}
.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 26px 58px rgba(215, 25, 32, 0.14);
}
.product-card :deep(.el-card__body) { padding: 14px; }
.product-img { display: block; width: 100%; height: 210px; border-radius: 14px; }
.product-img-placeholder {
  width: 100%; height: 210px;
  background:
    linear-gradient(135deg, rgba(255, 241, 241, 0.95), rgba(248, 250, 252, 0.92));
  display: flex; align-items: center; justify-content: center;
  color: #a6adbb;
  border-radius: 14px;
  font-weight: 650;
}
.product-info { padding: 14px 2px 2px; }
.product-name { color: #172033; font-size: 16px; font-weight: 720; margin-bottom: 10px; line-height: 1.35; }
.product-price { color: #e21d2b; font-size: 22px; font-weight: 850; line-height: 1; }
</style>
