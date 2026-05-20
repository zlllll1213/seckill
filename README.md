# 全栈秒杀系统

一个基于 Spring Boot 3 + Vue 3 + Element Plus 的全栈秒杀商城示例项目，包含商品浏览、秒杀活动、异步秒杀下单、订单查询、后台管理、个人中心和前端本地购物车。

## 技术栈

- 后端：Spring Boot 3.2、Spring Security、JWT、MyBatis-Plus、MySQL、Redis、RabbitMQ
- 前端：Vue 3、Vite、Pinia、Vue Router、Element Plus、Electron
- 基础设施：Docker Compose

## 功能模块

- 用户认证：注册、登录、Cookie JWT 鉴权、个人资料查询、邮箱修改、密码修改
- 商品模块：商品列表、商品详情、管理员新增/更新商品
- 秒杀模块：活动列表、活动详情、Lua/Redis 扣库存、RabbitMQ 异步建单、结果轮询
- 订单模块：我的订单、订单详情、管理员订单列表
- 管理后台：商品管理、活动管理、订单管理、数据看板
- 前端购物车：本地购物车、数量增减、移除、清空、合计展示

## 快速启动

### 1. 启动基础服务

```bash
cp .env.example .env
# 编辑 .env，把 change-me-* 替换为你自己的本地密码和 JWT_SECRET
docker compose up -d
```

Docker Compose 默认暴露：

- MySQL: `127.0.0.1:3307`
- Redis: `127.0.0.1:6380`
- RabbitMQ: `127.0.0.1:5673`
- RabbitMQ 管理台: `http://127.0.0.1:15673`

### 2. 启动后端

```bash
cd backend
set -a && source ../.env && set +a
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home \
mvn spring-boot:run
```

后端地址：`http://127.0.0.1:8080`

### 3. 启动前端

```bash
cd frontend
npm install
npx vite --host 127.0.0.1
```

前端地址：`http://127.0.0.1:5173`

## 测试账号

初始化脚本会创建以下账号，密码均为 `admin123`：

| 用户名 | 角色 | 邮箱 |
| --- | --- | --- |
| admin | ADMIN | admin@flashforge.local |
| alice | USER | alice@flashforge.local |
| bob | USER | bob@flashforge.local |
| tester | USER | tester@flashforge.local |

## 目录结构

```text
backend/             Spring Boot 后端
frontend/            Vue + Electron 前端
backend/sql/init.sql 数据库初始化脚本
docs/API.md          后端接口和字段说明
docs/FRONTEND.md     前端页面与交互说明
docker-compose.yml   本地基础服务编排
```

## 文档

- [接口字段文档](docs/API.md)
- [前端页面说明](docs/FRONTEND.md)

## 说明

当前购物车是前端本地购物车，数据保存在浏览器 `localStorage` 中；秒杀订单是后端真实订单流程。若要把普通购物车接入真实购买下单，需要新增普通订单表、购物车表和结算接口。
