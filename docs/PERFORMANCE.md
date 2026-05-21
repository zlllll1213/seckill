# 秒杀系统性能压测文档

## 一、压测目标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 并发用户数 | 1000 VUs | 虚拟并发用户 |
| 秒杀请求 QPS | ≥ 5000 req/s | 核心秒杀接口吞吐 |
| 秒杀响应 P99 | ≤ 500ms | 99% 请求延迟上限 |
| 令牌获取 P99 | ≤ 10ms | Redis SPOP 原子操作 |
| Lua 执行 P99 | ≤ 5ms | Lua 脚本在 Redis 服务端执行 |
| MQ 消费延迟 P99 | ≤ 3s | 消息从发送到建单完成 |
| 错误率 | < 0.1% | 非预期的 5xx 错误 |

---

## 二、环境要求

### 2.1 硬件配置

| 组件 | 推荐配置 |
|------|---------|
| 应用服务器 | 4C8G × 2 实例 |
| MySQL | 4C16G，SSD，InnoDB buffer pool ≥ 2G |
| Redis | 2C4G，maxmemory 4G，noeviction |
| RabbitMQ | 2C4G，持久化队列 |

### 2.2 依赖版本

| 依赖 | 版本 | 备注 |
|------|------|------|
| JDK | 17+ | Spring Boot 3 最低要求 |
| MySQL | 8.0 | utf8mb4 |
| Redis | 7.x | 支持 Lua 脚本 |
| RabbitMQ | 3.x | 支持 DLX + TTL |
| k6 | 0.48+ | 压测工具 |

### 2.3 压测前准备

```bash
# 1. 启动依赖服务
docker-compose up -d mysql redis rabbitmq

# 2. 初始化数据库
mysql -h 127.0.0.1 -P 3307 -u seckill -pseckill123 seckill < backend/sql/init.sql

# 3. 启动应用（2 实例）
java -jar target/seckill.jar --server.port=8080
java -jar target/seckill.jar --server.port=8081

# 4. 预热活动（通过 API 触发）
curl -X POST http://localhost:8080/api/admin/activities/1/preheat \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json"
```

---

## 三、k6 压测脚本

脚本路径：`scripts/k6/seckill-load-test.js`

### 3.1 脚本功能

1. **登录** — 获取 JWT token（按 VU 分配测试用户）
2. **秒杀** — POST `/api/seckill/{activityId}`
3. **轮询** — GET `/api/seckill/result/{activityId}`（直到拿到结果）
4. **查订单** — GET `/api/orders/{orderId}`（验证幂等性）

### 3.2 运行命令

```bash
# 基础压测：100 VU 持续 60 秒
k6 run --vus 100 --duration 60s scripts/k6/seckill-load-test.js

# 阶梯加压：模拟秒杀流量激增
k6 run \
  --stage 30s:100,30s:300,30s:600,30s:1000,30s:0 \
  scripts/k6/seckill-load-test.js

# 极限压测：2000 VU 持续 120 秒（仅测试环境）
k6 run \
  --vus 2000 --duration 120s \
  -e ACTIVITY_ID=1 \
  -e BASE_URL=http://localhost:8080 \
  scripts/k6/seckill-load-test.js

# 输出 JSON 报告
k6 run --vus 500 --duration 60s \
  --out json=results.json \
  scripts/k6/seckill-load-test.js
```

### 3.3 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `BASE_URL` | `http://localhost:8080` | 后端地址 |
| `ACTIVITY_ID` | `1` | 秒杀活动 ID |
| `USERS_FILE` | `users.json` | 测试账号文件 |

---

## 四、监控指标

### 4.1 Prometheus + Grafana

```yaml
# application.yml 已暴露 Actuator 端点
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

关键 Micrometer 指标：

| 指标名 | 类型 | 含义 |
|--------|------|------|
| `seckill.requests.total` | Counter | 秒杀请求总数（tag: result） |
| `seckill.order.create.latency` | Timer | 订单创建延迟 |
| `seckill.token.acquire.latency` | Timer | 令牌获取延迟 |
| `seckill.lua.execute.latency` | Timer | Lua 执行延迟 |
| `seckill.mq.send.latency` | Timer | MQ 发送延迟 |
| `seckill.order.timeout.cancel` | Counter | 订单超时取消数 |
| `seckill.mq.deadletter` | Counter | 死信消息数 |

### 4.2 日志分析

```bash
# 统计各链路节点耗时
grep "\[SECKILL\]" app.log | awk '{print $NF}' | sort -n | tail -20

# 统计令牌耗尽次数
grep -c "\[TOKEN\].*EMPTY" app.log

# 统计死信消息
grep -c "\[DEAD-LETTER\]" app.log

# 统计订单超时取消
grep -c "\[ORDER-CANCEL\]" app.log
```

### 4.3 Redis 实时监控

```bash
# 查看令牌剩余
redis-cli SCARD seckill:token:1

# 查看库存
redis-cli GET seckill:stock:1

# 查看超时订单队列大小
redis-cli ZCARD seckill:order:timeout

# 查看待处理 fallback 消息
redis-cli KEYS "seckill:fallback:*" | wc -l
```

---

## 五、结果分析模板

### 5.1 k6 输出关键字段

```
✓ checks.....................: 100.00% ✓ 50000  ✗ 0
  http_req_duration..........: avg=85ms   min=2ms   med=45ms   max=520ms  p(90)=180ms  p(95)=280ms
  http_req_failed............: 0.02%    ✓ 10     ✗ 49990
  http_reqs..................: 5000/s
  vus........................: 500
```

### 5.2 瓶颈定位思路

| 现象 | 可能原因 | 排查方向 |
|------|---------|---------|
| P99 延迟高 | 令牌耗尽 → 大量请求走不到 Lua | 增加库存 / 提前预热 |
| 错误率上升 | DB 连接池耗尽 | 增大 Hikari max-pool-size |
| MQ 堆积 | 消费能力不足 | 增大 RabbitMQ concurrency |
| 轮询 timeout | 订单创建慢 | 检查 DB 索引、MQ 消费延迟 |
| 死信增多 | 业务异常未捕获 | 检查消费者异常处理 |

---

## 六、优化记录

| 日期 | 优化项 | 压测结果 | 提升 |
|------|--------|---------|------|
| - | 基线版本 | - | - |
| - | + 令牌机制 | - | 减少无效 Lua 调用 |
| - | + 预热接口 | - | 消除首次请求冷启动 |
| - | + 订单超时取消 | - | 库存自动回补 |
| - | + MDC TraceId | - | 全链路可追踪 |

（填写实际压测结果）
