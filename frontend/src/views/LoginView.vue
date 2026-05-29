<!--
  LoginView.vue — 登录 / 注册页面
  职责：提供登录表单和注册表单的切换，调用 auth API，保存 Token
  当前状态：Phase 1B 空壳，Phase 3 实现业务逻辑
-->
<template>
  <div class="page">
    <h2>{{ isLogin ? '登录' : '注册' }}</h2>

    <form @submit.prevent="handleSubmit">
      <div>
        <label>用户名：</label>
        <input v-model="form.username" type="text" placeholder="请输入用户名" />
      </div>

      <div>
        <label>密码：</label>
        <input v-model="form.password" type="password" placeholder="请输入密码" />
      </div>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <button type="submit">{{ isLogin ? '登录' : '注册' }}</button>
    </form>

    <p>
      {{ isLogin ? '没有账号？' : '已有账号？' }}
      <a href="#" @click.prevent="toggleMode">{{ isLogin ? '去注册' : '去登录' }}</a>
    </p>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '../api/auth'
import { setToken } from '../utils/token'

const router = useRouter()

// 当前是登录模式还是注册模式
const isLogin = ref(true)

// 表单数据：v-model 双向绑定到 input
const form = reactive({
  username: '',
  password: ''
})

// 错误信息：空字符串时不渲染，有值时才显示
const errorMessage = ref('')

// 切换登录/注册模式
function toggleMode() {
  isLogin.value = !isLogin.value
  errorMessage.value = ''
}

// 提交表单
async function handleSubmit() {
  // 非空校验
  if (!form.username || !form.password) {
    errorMessage.value = '用户名和密码不能为空'
    return
  }

  errorMessage.value = ''

  try {
    // 根据当前模式调 login 或 register
    const currentAuthAction = isLogin.value ? login : register
    const response = await currentAuthAction({
      username: form.username,
      password: form.password
    })

    // 后端返回 { code, message, data: { token } }
    const token = response.data.data.token
    setToken(token)
    router.push('/feed')
  } catch (error) {
    // 后端返回的错误信息在 response.data.message
    const message = error.response?.data?.message || '请求失败，请检查网络'
    errorMessage.value = message
  }
}
</script>

<style scoped>
/*
  scoped 含义：此 style 块中的 CSS 只作用于当前 .vue 文件
  实现原理：Vue 编译时会给当前组件的每个 DOM 元素加一个唯一属性（如 data-v-xxxx）
  CSS 选择器也会被改写为 .page[data-v-xxxx]，从而不污染其他页面
*/
.page {
  padding: 20px;
}
.error {
  color: red;
}
</style>
