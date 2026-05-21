<template>
  <div class="seckill-page" v-loading="loading">
    <el-button @click="$router.back()" class="back-btn">返回商品通道</el-button>

    <!-- 活动存在 -->
    <template v-if="activity">
      <el-card class="seckill-card" shadow="never">
        <div class="seckill-header">
          <div>
            <span class="status-code">FLASH CHANNEL / {{ activity.status === 1 ? 'ACTIVE' : 'LOCKED' }}</span>
            <h1>{{ activity.name }}</h1>
          </div>
          <CountdownTimer :target-time="activity.endTime" label="距结束" @ended="onEnded" />
        </div>
        <div class="seckill-grid">
          <div class="price-cell">
            <span>秒杀价</span>
            <strong>¥{{ activity.seckillPrice }}</strong>
          </div>
          <div class="stock-cell">
            <span>剩余库存</span>
            <strong>{{ activity.stock }}</strong>
          </div>
          <div class="stock-meter">
            <i :style="{ width: `${Math.min(100, Math.max(5, Number(activity.stock || 0))) }%` }"></i>
          </div>
        </div>

        <!-- 秒杀按钮 -->
        <div class="seckill-action">
          <el-button
            v-if="resultStatus === 'idle'"
            type="danger"
            size="large"
            :disabled="ended || activity.status !== 1"
            :loading="seckilling"
            @click="handleSeckill"
          >立即秒杀</el-button>

          <el-result
            v-else-if="resultStatus === 'processing'"
            icon="loading"
            title="秒杀处理中"
            sub-title="请稍候..."
          />

          <el-result
            v-else-if="resultStatus === 'success'"
            class="success-result"
            icon="success"
            title="秒杀成功！"
            :sub-title="`订单号：${orderId}`"
          >
            <template #extra>
              <el-button type="primary" @click="$router.push('/orders')">查看订单</el-button>
            </template>
          </el-result>

          <el-result
            v-else-if="resultStatus === 'fail'"
            icon="error"
            title="秒杀失败"
            sub-title="库存不足或网络异常，请稍后再试"
          />
        </div>
      </el-card>
    </template>

    <!-- 活动不存在 / 已下架 — 背景图空状态 -->
    <div v-else-if="!loading" class="empty-state">
      <div class="empty-state-overlay">
        <div class="empty-state-icon">⚡</div>
        <h2>活动不存在</h2>
        <p>该秒杀活动可能已下架、已结束或链接无效</p>
        <el-button type="primary" size="large" round @click="$router.push('/products')">
          去逛逛其他商品
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { activityApi, seckillApi } from '@/api'
import CountdownTimer from '@/components/CountdownTimer.vue'

const route = useRoute()
const loading = ref(false)
const seckilling = ref(false)
const activity = ref(null)
const ended = ref(false)
const resultStatus = ref('idle')  // idle | processing | success | fail
const orderId = ref(null)
let pollTimer = null
let pollDelay = 1000

onMounted(async () => {
  loading.value = true
  try {
    const res = await activityApi.detail(route.params.id)
    activity.value = res.data
  } finally {
    loading.value = false
  }
})

onUnmounted(() => stopPolling())

function onEnded() { ended.value = true }

async function handleSeckill() {
  seckilling.value = true
  try {
    await seckillApi.doSeckill(route.params.id)
    resultStatus.value = 'processing'
    startPolling()
  } catch (e) {
    // 错误已由 axios 拦截器处理，保持 idle
  } finally {
    seckilling.value = false
  }
}

function startPolling() {
  stopPolling()
  pollDelay = 1000
  const poll = () => {
    pollTimer = window.setTimeout(async () => {
      let shouldContinue = true
      if (document.visibilityState !== 'visible') {
        pollDelay = Math.min(pollDelay * 1.5, 10000)
        poll()
        return
      }
  
      try {
        const res = await seckillApi.getResult(route.params.id)
        const { status, orderId: oid } = res.data
        if (status === 'success') {
          resultStatus.value = 'success'
          orderId.value = oid
          shouldContinue = false
          stopPolling()
        } else if (status === 'fail') {
          resultStatus.value = 'fail'
          shouldContinue = false
          stopPolling()
        }
      } catch {}
  
      if (shouldContinue) {
        pollDelay = Math.min(Math.round(pollDelay * 1.5), 10000)
        poll()
      }
    }, pollDelay)
  }
  poll()
}

function stopPolling() {
  if (pollTimer) { clearTimeout(pollTimer); pollTimer = null }
}
</script>

<style scoped>
.page { max-width: 700px; margin: 0 auto; padding: 24px; }
.seckill-page {
  min-height: calc(100vh - 64px);
  padding: 34px max(22px, calc((100vw - 1120px) / 2)) 72px;
  background:
    radial-gradient(circle at 82% 12%, rgba(226, 18, 24, 0.18), transparent 30%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.02), transparent);
}
.back-btn { margin-bottom: 18px; }
.seckill-card {
  position: relative;
  overflow: hidden;
  padding: 0;
  border-color: rgba(255, 58, 50, 0.36);
}
.seckill-card::before {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(rgba(255, 255, 255, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.035) 1px, transparent 1px);
  background-size: 32px 32px;
  pointer-events: none;
}
.seckill-card :deep(.el-card__body) {
  position: relative;
  padding: 32px;
}
.seckill-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
  margin-bottom: 28px;
}
.status-code {
  display: block;
  color: #ff3a32;
  font-size: 12px;
  font-weight: 900;
  margin-bottom: 12px;
}
.seckill-header h1 {
  max-width: 680px;
  color: #fff;
  font-family: var(--font-display);
  font-size: clamp(40px, 7vw, 78px);
  line-height: 0.96;
  font-weight: 900;
}
.seckill-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 16px;
  margin-bottom: 28px;
}
.price-cell,
.stock-cell {
  min-height: 150px;
  padding: 20px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.04);
}
.price-cell span,
.stock-cell span {
  display: block;
  color: var(--muted);
  font-size: 13px;
  font-weight: 800;
  margin-bottom: 14px;
}
.price-cell strong,
.stock-cell strong {
  color: #ff3a32;
  font-family: var(--font-display);
  font-size: 58px;
  line-height: 1;
  font-weight: 900;
  text-shadow: 0 0 22px rgba(226, 18, 24, 0.34);
}
.stock-cell strong {
  color: #fff;
}
.stock-meter {
  grid-column: 1 / -1;
  height: 14px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.06);
}
.stock-meter i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #e21218, #ff3a32);
  box-shadow: var(--shadow-red);
}
.seckill-action {
  text-align: center;
  padding: 28px 0 12px;
  border-top: 1px solid var(--line);
}
.seckill-action .el-button {
  min-width: 220px;
  height: 54px;
  font-size: 18px;
}
.success-result { animation: pop-in 0.3s ease; }
@keyframes pop-in {
  from { opacity: 0; transform: scale(0.96); }
  to { opacity: 1; transform: scale(1); }
}

/* ========== 空状态背景 ========== */
.empty-state {
  min-height: 480px;
  border-radius: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    linear-gradient(160deg, rgba(230, 69, 69, 0.12) 0%, rgba(255, 184, 77, 0.06) 45%, rgba(245, 247, 250, 0.50) 100%),
    radial-gradient(ellipse 320px 180px at 80% 15%, rgba(230, 69, 69, 0.14), transparent),
    radial-gradient(ellipse 260px 200px at 15% 70%, rgba(255, 184, 77, 0.09), transparent),
    url('@/assets/images/generated/trading-command-concept.png') center/cover no-repeat;
  position: relative;
  overflow: hidden;
}

/* 如果没有图片，回退到纯渐变（自动生效） */
@supports not (background-image: url('@/assets/images/flash-sale-hero.png')) {
  .empty-state {
    background:
      linear-gradient(160deg, rgba(230, 69, 69, 0.12) 0%, rgba(255, 184, 77, 0.06) 45%, rgba(245, 247, 250, 0.50) 100%),
      radial-gradient(ellipse 320px 180px at 80% 15%, rgba(230, 69, 69, 0.14), transparent),
      radial-gradient(ellipse 260px 200px at 15% 70%, rgba(255, 184, 77, 0.09), transparent);
  }
}

.empty-state-overlay {
  text-align: center;
  padding: 48px 24px;
  max-width: 400px;
  background: rgba(7, 8, 9, 0.82);
  backdrop-filter: blur(16px);
  border: 1px solid var(--line);
  border-radius: 2px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.28);
  animation: fade-up 0.45s ease;
}

.empty-state-icon { font-size: 52px; margin-bottom: 16px; }

.empty-state-overlay h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 8px;
}

.empty-state-overlay p {
  font-size: 14px;
  color: var(--muted);
  margin-bottom: 24px;
  line-height: 1.6;
}

@keyframes fade-up {
  from { opacity: 0; transform: translateY(18px); }
  to   { opacity: 1; transform: translateY(0); }
}

@media (max-width: 760px) {
  .seckill-page { min-height: calc(100vh - 120px); padding: 24px 16px 48px; }
  .seckill-header,
  .seckill-grid { grid-template-columns: 1fr; flex-direction: column; }
  .seckill-card :deep(.el-card__body) { padding: 22px; }
  .price-cell strong,
  .stock-cell strong { font-size: 42px; }
}
</style>
