# 前端页面与交互说明

前端使用 Vue 3 + Vite + Pinia + Vue Router + Element Plus。

## 页面路由

| 路由 | 页面 | 说明 |
| --- | --- | --- |
| `/products` | 商品列表 | 商品浏览、搜索 |
| `/products/:id` | 商品详情 | 商品信息、数量选择、加入购物车、立即购买、秒杀活动入口 |
| `/cart` | 购物车 | 本地购物车，支持增减数量、移除、清空、合计 |
| `/profile` | 个人中心 | 登录/注册、头像信息、邮箱修改、密码修改 |
| `/orders` | 我的订单 | 当前用户秒杀订单 |
| `/orders/:id` | 订单详情 | 单个订单详情 |
| `/seckill/:id` | 秒杀页 | 提交秒杀请求、轮询结果 |
| `/admin/dashboard` | 数据看板 | 管理员统计 |
| `/admin/products` | 商品管理 | 管理员维护商品 |
| `/admin/activities` | 活动管理 | 管理员维护秒杀活动 |
| `/admin/orders` | 订单管理 | 管理员查看订单 |

## 状态管理

### 用户状态 `src/stores/user.js`

保存：

- `username`
- `role`
- `isLoggedIn`
- `isAdmin`

数据持久化在 `localStorage`。

### 购物车状态 `src/stores/cart.js`

保存：

- `items`
- `totalCount`
- `totalAmount`

购物车数据保存在 `localStorage` 的 `seckill-cart-items` 中。

购物车条目字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 商品 ID |
| name | string | 商品名称 |
| price | number | 商品价格 |
| imageUrl | string | 商品图片 |
| stock | number | 库存 |
| quantity | number | 购买数量 |

## 登录与鉴权

未登录访问需要权限的页面时，会跳转到：

```text
/profile?mode=login&redirect=<原目标地址>
```

登录成功后跳回原目标地址。

## 购物车说明

当前购物车是前端本地购物车：

- 点击商品详情页“加入购物车”会把商品加入本地购物车
- 点击“立即购买”会加入购物车并进入购物车页
- 购物车页的“去结算”目前是交互占位

后端当前真实订单模型是秒杀订单 `seckill_order`。若需要普通购买下单，需要新增普通订单接口与数据表。

## 视觉资源

项目包含两张由 Image Gen 生成的前端背景图：

- `frontend/src/assets/images/generated/flash-sale-bg-v2.png`
- `frontend/src/assets/images/generated/auth-bg-v2.png`

它们分别用于商品列表页和个人/登录页背景。
