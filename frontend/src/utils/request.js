// utils/request.js — Axios 实例 + 拦截器
import axios from 'axios'
import { getToken, removeToken } from './token'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

// 请求拦截器：发请求前自动注入 Token
request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：收到 401 清 Token 跳登录
request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      removeToken()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
