<template>
  <el-card class="product-card" shadow="never" @click="$router.push(`/products/${product.id}`)">
    <div class="status-rail">
      <span>{{ product.status === 1 ? 'ON SALE' : 'OFFLINE' }}</span>
    </div>
    <div class="image-frame">
      <el-image v-if="product.imageUrl" :src="product.imageUrl" :alt="product.name" lazy fit="cover" class="product-img">
        <template #error>
          <div class="product-img-placeholder">暂无图片</div>
        </template>
      </el-image>
      <div v-else class="product-img-placeholder">暂无图片</div>
    </div>
    <div class="product-info">
      <div class="product-name">{{ product.name }}</div>
      <div class="product-meta">
        <span>库存 {{ product.stock ?? '--' }}</span>
        <span>ID {{ product.id }}</span>
      </div>
      <div class="product-price">¥{{ product.price }}</div>
    </div>
  </el-card>
</template>

<script setup>
defineProps({ product: { type: Object, required: true } })
</script>

<style scoped>
.product-card {
  position: relative;
  cursor: pointer;
  overflow: hidden;
  border-radius: 2px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.07), transparent 44%),
    rgba(12, 14, 16, 0.92);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background 0.22s ease;
}
.product-card:hover {
  border-color: rgba(255, 58, 50, 0.74);
  transform: translateY(-5px);
  box-shadow: 0 22px 60px rgba(0, 0, 0, 0.44), 0 0 30px rgba(226, 18, 24, 0.18);
}
.product-card :deep(.el-card__body) { padding: 12px; }
.status-rail {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  min-width: 82px;
  padding: 7px 10px;
  color: #fff;
  background: #d30d13;
  font-family: var(--font-display);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0;
  box-shadow: 0 0 18px rgba(226, 18, 24, 0.35);
}
.image-frame {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.09);
  background:
    linear-gradient(rgba(255, 255, 255, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.035) 1px, transparent 1px),
    #101214;
  background-size: 22px 22px;
}
.image-frame::after {
  content: "";
  position: absolute;
  inset: auto 10px 10px 10px;
  height: 3px;
  background: linear-gradient(90deg, #e21218 72%, rgba(255, 255, 255, 0.22) 72%);
}
.product-img { display: block; width: 100%; height: 212px; border-radius: 0; }
.product-img-placeholder {
  width: 100%; height: 210px;
  background: repeating-linear-gradient(135deg, rgba(255, 255, 255, 0.04) 0 8px, rgba(255, 255, 255, 0.015) 8px 16px), #111315;
  display: flex; align-items: center; justify-content: center;
  color: var(--muted);
  border-radius: 0;
  font-weight: 800;
}
.product-info { padding: 14px 2px 2px; }
.product-name { color: var(--text); font-size: 16px; font-weight: 820; margin-bottom: 10px; line-height: 1.35; }
.product-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: var(--muted);
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 12px;
}
.product-price {
  color: #ff3a32;
  font-family: var(--font-display);
  font-size: 25px;
  font-weight: 900;
  line-height: 1;
  text-shadow: 0 0 16px rgba(226, 18, 24, 0.28);
}
</style>
