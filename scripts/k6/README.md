# k6 压测脚本

默认压测登录、当前用户、商品列表、活动列表和订单列表。

先跑快速自检：

```bash
SMOKE=true k6 run scripts/k6/seckill-load-test.js
```

正式压测：

```bash
k6 run scripts/k6/seckill-load-test.js
```

常用参数：

```bash
BASE_URL=http://127.0.0.1:8080 \
USERNAME=admin \
PASSWORD=admin123 \
k6 run scripts/k6/seckill-load-test.js
```

默认每个 VU 只登录一次并复用 Cookie，避免被后端登录限流挡住。专门压登录接口时再开启：

```bash
LOGIN_EACH_ITERATION=true k6 run scripts/k6/seckill-load-test.js
```

如需压秒杀提交接口，显式开启：

```bash
INCLUDE_SECKILL=true ACTIVITY_ID=1 k6 run scripts/k6/seckill-load-test.js
```

注意：秒杀接口有“一人一次”和库存扣减语义，开启后可能产生业务失败或改变测试数据。
