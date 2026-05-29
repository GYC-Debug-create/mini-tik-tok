// api/auth.js — 认证相关接口
import request from '../utils/request'

// 注册：{ username, password }
export function register(data) {
  return request.post('/api/auth/register', data)
}

// 登录：{ username, password }，返回 token
export function login(data) {
  return request.post('/api/auth/login', data)
}

// 登出
export function logout() {
  return request.post('/api/auth/logout')
}
