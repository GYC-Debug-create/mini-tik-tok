// utils/token.js — Token 存取删
// localStorage 实现，统一入口，方便未来切换存储方案

const TOKEN_KEY = 'douyin_token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}
