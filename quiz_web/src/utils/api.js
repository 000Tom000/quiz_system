import axios from 'axios'
import { showToast } from 'vant'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' },
})

// 请求拦截器：自动携带 token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一解包 & 错误处理
api.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body.code === 200) {
      return body.data
    }
    // 业务错误
    showToast(body.message || '请求失败')
    return Promise.reject(new Error(body.message || '请求失败'))
  },
  (err) => {
    const msg = err.response?.data?.message || err.message || '网络异常'
    showToast(msg)
    return Promise.reject(err)
  },
)

export default api
