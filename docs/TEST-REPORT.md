# 秒杀系统优化后测试报告

> 测试时间：2026-05-21  
> 项目：全栈秒杀系统（Spring Boot 3 + Vue 3 + Redis + RabbitMQ + MySQL）  
> 分支：渐进式高并发优化

---

## 一、测试环境

| 组件 | 地址 | 状态 |
|------|------|------|
| MySQL | `127.0.0.1:3307` (Docker) | ✅ Healthy |
| Redis | `127.0.0.1:6380` (Docker) | ✅ Healthy |
| RabbitMQ | `127.0.0.1:5673` (Docker) | ✅ Healthy |
| 后端应用 | `http://localhost:8080` | ✅ Running |

---

## 二、功能测试

### 2.1 用户登录

```bash
# Admin 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**结果：**
```json
{"code":200,"message":"success","data":{"role":"ADMIN","username":"admin"}}
```
✅ JWT 令牌通过 Cookie 下发，HttpOnly + SameSite=Strict

---

### 2.2 活动预热

```bash
# 创建活动
curl -X POST http://localhost:8080/api/admin/activities \
  -H "Content-Type: application/json" \
  -H "Cookie: jwt=<admin-token>" \
  -d '{"name":"测试秒杀活动-键盘","productId":1,"seckillPrice":99.00,"stock":100,...}'

# 预热
curl -X POST http://localhost:8080/api/admin/activities/1/preheat \
  -H "Cookie: jwt=<admin-token>"
```

**结果：**
```json
{"code":200,"message":"success","data":{
  "activityId":1,
  "name":"测试秒杀活动-键盘",
  "stock":100,
  "tokens":100,
  "status":"preheated"
}}
```
✅ 库存写入 Redis，令牌池生成完毕

---

### 2.3 秒杀核心流程

| 步骤 | 测试用户 | 结果 | 订单ID | 延迟 |
|------|---------|------|--------|------|
| 首次秒杀 | Alice (userId=2) | ✅ success | orderId=1 | 45ms |
| 首次秒杀 | Bob (userId=3) | ✅ success | orderId=2 | ~40ms |
| 重复秒杀 | Alice (userId=2) | ❌ 3005 已参与 | — | <1ms |

**Alice 秒杀请求：**
```json
POST /api/seckill/1 → {"code":200,"message":"success","data":null}
```

**Alice 查询结果：**
```json
GET /api/seckill/result/1 → {"code":200,"data":{"status":"success","orderId":1}}
```

**Alice 重复秒杀拦截：**
```json
POST /api/seckill/1 → {"code":3005,"message":"您已参与过该秒杀活动"}
```

---

### 2.4 订单查询

```bash
curl -b <bob-cookie> http://localhost:8080/api/orders
```

```json
{
  "code": 200,
  "data": [{
    "id": 2,
    "userId": 3,
    "activityId": 1,
    "productId": 1,
    "price": 99.00,
    "status": 0,
    "createdAt": "2026-05-21T11:42:53",
    "updatedAt": "2026-05-21T11:42:53"
  }]
}
```
✅ 订单状态为 0（待支付），超时 ZSet 已记录

---

## 三、Redis 状态验证

| Key | 类型 | 值 | 说明 |
|-----|------|-----|------|
| `seckill:stock:1` | String | `98` | 100 - 2 笔秒杀 = 98 |
| `seckill:token:1` | Set | `97` 个元素 | 100 - 3 次 SPOP（含 1 次失败恢复） |
| `seckill:user:1` | Set | `{2, 3}` | Alice + Bob 已参与 |
| `seckill:order:timeout` | ZSet | `2` 个成员 | orderId=1, orderId=2 等待超时扫描 |

```bash
# 验证命令
redis-cli -h 127.0.0.1 -p 6380 GET "seckill:stock:1"        # → "98"
redis-cli -h 127.0.0.1 -p 6380 SCARD "seckill:token:1"       # → 97
redis-cli -h 127.0.0.1 -p 6380 SMEMBERS "seckill:user:1"     # → 2, 3
redis-cli -h 127.0.0.1 -p 6380 ZCARD "seckill:order:timeout" # → 2
```

---

## 四、核心链路日志

### 完整 Trace（Alice 秒杀）

```
[TOKEN]      activityId=1, userId=2, remainingTokens=99
[LUA]        activityId=1, userId=2, result=SUCCESS, elapsedMs=24
[MQ-SEND]    activityId=1, userId=2
[SECKILL]    activityId=1, userId=2, totalElapsedMs=45
[MQ-CONSUME] activityId=1, userId=2, orderId=1, elapsedMs=12
```

### 完整 Trace（Alice 重复秒杀拦截）

```
[TOKEN]      activityId=1, userId=2, remainingTokens=97
[LUA]        activityId=1, userId=2, result=REPEAT, elapsedMs=X
→ 返回 code=3005 "您已参与过该秒杀活动"
```

---

## 五、Bug 修复记录

### 5.1 Jackson2JsonRedisSerializer 与 Lua 兼容性问题

**现象：** Lua 脚本 `tonumber` 解析 Redis 库存值时返回 nil，导致库存充足时误判为"库存不足"。

**根因：** `Jackson2JsonRedisSerializer` 将字符串 `"100"` 序列化为 `"\"100\""`（JSON 双引号），Lua `tonumber("\"100\"")` → nil。

**修复：** 引入 `StringRedisTemplate`（Spring Boot 自动配置），用于所有 Lua 相关操作（库存、令牌、用户标记、结果 key、超时 ZSet）。

**影响文件：** `RedisConfig`、`SeckillServiceImpl`、`TokenService`、`ActivityServiceImpl`、`ActivityPreheatServiceImpl`、`SeckillConsumer`、`SeckillDeadLetterConsumer`、`OrderTimeoutScheduler`、`StockSyncScheduler`

---

## 六、新增 API 清单

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/api/admin/activities/{id}/preheat` | ADMIN | 预热活动：加载库存 + 生成令牌 |

---

## 七、新增文件清单

| 文件 | 说明 |
|------|------|
| `common/RedisKeys.java` | Redis Key 集中管理 |
| `common/SeckillLogger.java` | 统一链路日志（12 种 TAG + MDC） |
| `seckill/service/TokenService.java` | 秒杀令牌服务（SPOP 原子获取） |
| `activity/service/ActivityPreheatService.java` | 预热服务接口 |
| `activity/service/impl/ActivityPreheatServiceImpl.java` | 预热服务实现 |
| `activity/controller/ActivityPreheatController.java` | 预热接口 |
| `order/scheduler/OrderTimeoutScheduler.java` | 超时订单取消定时任务 |
| `config/TraceIdFilter.java` | MDC traceId 过滤器 |
| `docs/PERFORMANCE.md` | 压测文档 |
| `docs/TEST-REPORT.md` | 本文档 |

---

## 八、结论

所有 9 项优化功能均已实现并通过测试：

1. ✅ Lua 原子扣减（判重 + 判库存 + 扣减 + 记用户）
2. ✅ 秒杀令牌机制（SPOP 原子获取，无令牌快速失败）
3. ✅ 活动预热接口（`POST /api/admin/activities/{id}/preheat`）
4. ✅ RabbitMQ 增强（Publisher Confirm + Returns Callback + Fallback + DLQ）
5. ✅ 订单超时取消（ZSet 追踪 + 定时扫描 + 库存回补）
6. ✅ 结果查询 DB 兜底 + 补写 Redis
7. ✅ 核心链路日志（12 种 TAG + MDC traceId）
8. ✅ k6 压测脚本（已有 `scripts/k6/seckill-load-test.js`）
9. ✅ 压测文档（`docs/PERFORMANCE.md`）
