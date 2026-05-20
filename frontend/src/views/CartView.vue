<template>
  <div class="cart-page">
    <div class="cart-header">
      <div>
        <h1>购物车</h1>
        <p>先把想买的商品放进来，后续可以继续扩展成真实结算流程。</p>
      </div>
      <el-button v-if="cart.items.length" text type="danger" @click="cart.clearCart()">清空购物车</el-button>
    </div>

    <div v-if="cart.items.length" class="cart-layout">
      <section class="cart-list">
        <article v-for="item in cart.items" :key="item.id" class="cart-item">
          <el-image v-if="item.imageUrl" :src="item.imageUrl" :alt="item.name" fit="cover" class="item-image">
            <template #error>
              <div class="item-image-placeholder">暂无图片</div>
            </template>
          </el-image>
          <div v-else class="item-image-placeholder">暂无图片</div>

          <div class="item-main">
            <h2>{{ item.name }}</h2>
            <p>库存 {{ item.stock }}</p>
            <strong>¥{{ item.price }}</strong>
          </div>

          <el-input-number
            :model-value="item.quantity"
            :min="1"
            :max="item.stock || 999"
            controls-position="right"
            @update:model-value="value => cart.updateQuantity(item.id, value)"
          />
          <el-button type="danger" text @click="cart.removeItem(item.id)">移除</el-button>
        </article>
      </section>

      <aside class="cart-summary">
        <span>合计</span>
        <strong>¥{{ cart.totalAmount.toFixed(2) }}</strong>
        <p>共 {{ cart.totalCount }} 件商品</p>
        <el-button type="primary" size="large" @click="checkout">去结算</el-button>
      </aside>
    </div>

    <div v-else class="cart-empty">
      <div class="empty-icon">🛒</div>
      <h2>购物车还是空的</h2>
      <p>去商品列表挑几件秒杀好物吧。</p>
      <el-button type="primary" @click="$router.push('/products')">去逛商品</el-button>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart'

const cart = useCartStore()

function checkout() {
  ElMessage.info('普通结算后端还未接入，当前购物车先作为前端选购清单使用')
}
</script>

<style scoped>
.cart-page {
  min-height: calc(100vh - 72px);
  padding: 42px max(24px, calc((100vw - 1200px) / 2)) 72px;
  background:
    radial-gradient(circle at 88% 12%, rgba(226, 29, 43, 0.1), transparent 28%),
    linear-gradient(180deg, #fff 0%, #f8fafc 100%);
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 18px;
  margin-bottom: 28px;
}

.cart-header h1 {
  color: #172033;
  font-size: 36px;
  font-weight: 850;
  margin-bottom: 10px;
}

.cart-header p,
.cart-summary p,
.item-main p,
.cart-empty p {
  color: #6b7280;
}

.cart-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 24px;
  align-items: start;
}

.cart-list {
  display: grid;
  gap: 14px;
}

.cart-item,
.cart-summary,
.cart-empty {
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 20px 58px rgba(31, 41, 55, 0.07);
}

.cart-item {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr) auto auto;
  gap: 18px;
  align-items: center;
  padding: 16px;
}

.item-image,
.item-image-placeholder {
  width: 120px;
  height: 96px;
  border-radius: 14px;
}

.item-image-placeholder {
  display: grid;
  place-items: center;
  color: #a6adbb;
  background: #f1f5f9;
}

.item-main h2 {
  color: #172033;
  font-size: 17px;
  margin-bottom: 8px;
}

.item-main strong {
  display: inline-block;
  margin-top: 8px;
  color: #e21d2b;
  font-size: 20px;
}

.cart-summary {
  position: sticky;
  top: 96px;
  padding: 24px;
}

.cart-summary span {
  color: #6b7280;
  font-weight: 650;
}

.cart-summary strong {
  display: block;
  color: #e21d2b;
  font-size: 34px;
  margin: 12px 0 8px;
}

.cart-summary .el-button {
  width: 100%;
  margin-top: 18px;
}

.cart-empty {
  min-height: 380px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 12px;
  text-align: center;
}

.empty-icon { font-size: 52px; }
.cart-empty h2 { color: #172033; font-size: 24px; }

@media (max-width: 760px) {
  .cart-page { min-height: calc(100vh - 120px); padding: 28px 18px 48px; }
  .cart-header { align-items: flex-start; flex-direction: column; }
  .cart-header h1 { font-size: 30px; }
  .cart-layout { grid-template-columns: 1fr; }
  .cart-item { grid-template-columns: 88px 1fr; }
  .item-image,
  .item-image-placeholder { width: 88px; height: 76px; }
  .cart-item .el-input-number,
  .cart-item .el-button { grid-column: 2; justify-self: start; }
  .cart-summary { position: static; }
}
</style>
