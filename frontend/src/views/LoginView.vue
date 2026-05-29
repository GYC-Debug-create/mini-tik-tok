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

    const token = response.data.data.token
    setToken(token)
    router.push('/feed')
  } catch (error) {
    const message = error.response?.data?.message || '请求失败，请检查网络'
    errorMessage.value = message
  }
}
</script>

<style scoped>
.page {
  padding: 20px;
}
.error {
  color: red;
}
</style>
