import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

http.interceptors.request.use(config => {
  return config
})

http.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code === 200) return data
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(data)
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      window.location.hash = '/login'
    }
    ElMessage.error(err.response?.data?.message || '网络错误')
    return Promise.reject(err)
  }
)

// 认证
export const authApi = {
  login: (data) => http.post('/auth/login', data),
  register: (data) => http.post('/auth/register', data),
  me: () => http.get('/auth/me'),
  updateMe: (data) => http.put('/auth/me', data),
  updatePassword: (data) => http.put('/auth/password', data)
}

// 商品
export const productApi = {
  list: (page = 1, size = 10) => http.get('/products', { params: { page, size } }),
  detail: (id) => http.get(`/products/${id}`),
  create: (data) => http.post('/admin/products', data),
  update: (id, data) => http.put(`/admin/products/${id}`, data)
}

// 活动
export const activityApi = {
  list: () => http.get('/activities'),
  detail: (id) => http.get(`/activities/${id}`),
  create: (data) => http.post('/admin/activities', data),
  update: (id, data) => http.put(`/admin/activities/${id}`, data)
}

// 秒杀
export const seckillApi = {
  doSeckill: (activityId) => http.post(`/seckill/${activityId}`),
  getResult: (activityId) => http.get(`/seckill/result/${activityId}`)
}

// 订单
export const orderApi = {
  myOrders: () => http.get('/orders'),
  detail: (id) => http.get(`/orders/${id}`),
  allOrders: (page = 1, size = 20) => http.get('/admin/orders', { params: { page, size } })
}

export const adminApi = {
  stats: () => http.get('/admin/stats')
}

export default http
