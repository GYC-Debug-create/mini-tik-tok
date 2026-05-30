<template>
  <div class="page">
    <div class="login-logo">抖音</div>
    <div class="login-subtitle">记录美好生活</div>

    <form class="form-card" @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>用户名：</label>
        <input v-model="form.username" type="text" placeholder="请输入用户名" />
      </div>

      <div class="form-group">
        <label>密码：</label>
        <input v-model="form.password" type="password" placeholder="请输入密码" />
      </div>

      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

      <button type="submit" class="btn btn-primary btn-block">
        {{ isLogin ? '登录' : '注册' }}
      </button>
    </form>

    <p class="toggle-link">
      {{ isLogin ? '没有账号？' : '已有账号？' }}
      <a href="#" @click.prevent="toggleMode">{{ isLogin ? '去注册' : '去登录' }}</a>
    </p>

    <div class="other-login">
      <div class="other-login-text">其他方式登录</div>
      <div class="other-login-icons">
        <div class="other-login-icon">微</div>
        <div class="other-login-icon">Q</div>
        <div class="other-login-icon">博</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '../api/auth'
import { setToken } from '../utils/token'

const router = useRouter()

const isLogin = ref(true)

const form = reactive({
  username: '',
  password: ''
})

const errorMessage = ref('')

function toggleMode() {
  isLogin.value = !isLogin.value
  errorMessage.value = ''
}

async function handleSubmit() {
  if (!form.username || !form.password) {
    errorMessage.value = '用户名和密码不能为空'
    return
  }

  errorMessage.value = ''

  try {
    const currentAuthAction = isLogin.value ? login : register
    const response = await currentAuthAction({
      username: form.username,
      password: form.password
    })

    const token = response.data?.data?.token
    if (!token) {
      errorMessage.value = '登录响应缺少 token'
      return
    }
    setToken(token)
    router.push('/feed')
  } catch (error) {
    const message = error.response?.data?.message || '请求失败，请检查网络'
    errorMessage.value = message
  }
}
</script>

<style scoped>
/* === 页面：居中偏上 === */
.page {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 12vh;
}

/* === Logo 区 === */
.login-logo {
  font-size: 38px;
  font-weight: 800;
  color: #161823;
  margin-bottom: 8px;
  letter-spacing: 2px;
}
.login-subtitle {
  font-size: 14px;
  color: #8a8f99;
  margin-bottom: 40px;
}

/* === 表单卡片 === */
.form-card {
  width: 100%;
  max-width: 380px;
  padding: 0 24px;
  box-shadow: none;
  background: transparent;
}

/* === 字段间距 === */
.form-group {
  margin-bottom: 22px;
}

/* === label 弱化 === */
.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #161823;
  margin-bottom: 8px;
}

/* === 输入框：轻量灰底 + 底部细线 === */
.form-group input {
  height: 48px;
  padding: 0 14px;
  font-size: 15px;
  color: #161823;
  background: #f7f7f8;
  border: none;
  border-bottom: 1px solid #e6e6e6;
  border-radius: 8px 8px 0 0;
}
.form-group input:focus {
  background: #fff;
  border-bottom-color: #fe2c55;
  box-shadow: none;
  outline: none;
}
.form-group input::placeholder {
  color: #a8a8a8;
  font-size: 15px;
}

/* === 按钮：胶囊形 48px 高 === */
.btn-primary {
  height: 48px;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  background: #fe2c55;
  color: #fff;
  margin-top: 28px;
}
.btn-primary:hover {
  background: #f01f4b;
}

/* === 切换链接 === */
.toggle-link {
  margin-top: 24px;
  font-size: 14px;
  color: #fe2c55;
}

/* === 其他登录方式 === */
.other-login {
  margin-top: 96px;
  padding-bottom: 40px;
  text-align: center;
}
.other-login-text {
  font-size: 14px;
  color: #8a8f99;
  margin-bottom: 28px;
}
.other-login-icons {
  display: flex;
  justify-content: center;
  gap: 32px;
}
.other-login-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: transparent;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #8a8f99;
}
</style>
