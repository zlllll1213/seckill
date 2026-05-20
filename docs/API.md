# 接口字段文档

后端统一响应结构：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | number | `200` 表示成功，其他值表示业务错误 |
| message | string | 响应信息 |
| data | any | 业务数据 |

鉴权方式：登录成功后后端写入 `HttpOnly` Cookie `jwt`，前端请求自动携带。

## 用户认证

### POST `/api/auth/login`

登录。

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| username | string | 用户名 |
| role | string | `USER` / `ADMIN` |

### POST `/api/auth/register`

注册普通用户。

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | string | 是 | 用户名，唯一 |
| password | string | 是 | 8-20 位，需包含字母和数字 |
| email | string | 否 | 邮箱 |

### GET `/api/auth/me`

获取当前登录用户信息。需要登录。

响应 `data` 为 `User`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 用户 ID |
| username | string | 用户名 |
| email | string | 邮箱 |
| role | string | 用户角色 |
| createdAt | string | 创建时间 |
| updatedAt | string | 更新时间 |

### PUT `/api/auth/me`

更新当前用户资料。需要登录。

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| email | string | 否 | 邮箱 |

### PUT `/api/auth/password`

修改当前用户密码。需要登录。

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| oldPassword | string | 是 | 原密码 |
| newPassword | string | 是 | 新密码，8-20 位，需包含字母和数字 |

## 商品

### GET `/api/products`

分页查询商品列表。公开接口。

查询参数：

| 参数 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| page | number | 1 | 页码 |
| size | number | 10 | 每页数量，最大 100 |

响应 `data.records[]` 为 `Product`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 商品 ID |
| name | string | 商品名称 |
| description | string | 商品描述 |
| price | number | 原价 |
| imageUrl | string | 商品图片 URL |
| stock | number | 库存 |
| status | number | `1` 上架，`0` 下架 |
| createdAt | string | 创建时间 |
| updatedAt | string | 更新时间 |

### GET `/api/products/{id}`

查询商品详情。公开接口。

### POST `/api/admin/products`

新增商品。需要 `ADMIN`。

请求体字段同 `Product`，不需要传 `id`、`createdAt`、`updatedAt`。

### PUT `/api/admin/products/{id}`

更新商品。需要 `ADMIN`。

## 秒杀活动

### GET `/api/activities`

查询所有秒杀活动。公开接口。

响应 `data[]` 为 `SeckillActivity`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 活动 ID |
| productId | number | 商品 ID |
| name | string | 活动名称 |
| seckillPrice | number | 秒杀价 |
| stock | number | 秒杀库存 |
| startTime | string | 开始时间 |
| endTime | string | 结束时间 |
| status | number | `0` 待开始，`1` 进行中，`2` 已结束 |
| createdAt | string | 创建时间 |
| updatedAt | string | 更新时间 |

### GET `/api/activities/{id}`

查询秒杀活动详情。公开接口。

### POST `/api/admin/activities`

新增秒杀活动。需要 `ADMIN`。

### PUT `/api/admin/activities/{id}`

更新秒杀活动。需要 `ADMIN`。

## 秒杀

### POST `/api/seckill/{activityId}`

提交秒杀请求。需要登录。

响应 `code=200` 表示请求进入处理流程，前端应继续轮询结果。

### GET `/api/seckill/result/{activityId}`

轮询秒杀结果。需要登录。

响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| status | string | `processing` / `success` / `fail` |
| orderId | number | 成功时返回订单 ID |

## 订单

### GET `/api/orders`

查询当前用户订单。需要登录。

响应 `data[]` 为 `SeckillOrder`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 订单 ID |
| userId | number | 用户 ID |
| activityId | number | 秒杀活动 ID |
| productId | number | 商品 ID |
| price | number | 成交价 |
| status | number | `0` 待支付，`1` 已支付，`2` 已取消 |
| createdAt | string | 创建时间 |
| updatedAt | string | 更新时间 |

### GET `/api/orders/{id}`

查询当前用户的订单详情。需要登录，且订单必须属于当前用户。

### GET `/api/admin/orders`

查询所有订单。需要 `ADMIN`。

查询参数：

| 参数 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| page | number | 1 | 页码 |
| size | number | 20 | 每页数量，最大 100 |

## 后台统计

### GET `/api/admin/stats`

查询后台数据看板统计。需要 `ADMIN`。

## 错误码

| code | 说明 |
| --- | --- |
| 400 | 请求参数错误 |
| 401 | 未登录或登录已过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 1001 | 用户不存在 |
| 1002 | 用户名已被占用 |
| 1003 | 密码错误 |
| 2001 | 商品不存在 |
| 2002 | 商品已下架 |
| 3001 | 秒杀活动不存在 |
| 3002 | 秒杀活动尚未开始 |
| 3003 | 秒杀活动已结束 |
| 3004 | 库存不足 |
| 3005 | 已参与过该秒杀活动 |
| 3006 | 秒杀处理中 |
