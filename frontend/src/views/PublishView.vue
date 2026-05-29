<template>
  <div class="page">
    <h2>发布视频</h2>

    <form class="publish-form" @submit.prevent>
      <div class="form-item">
        <label>视频标题：</label>
        <input v-model="title" type="text" placeholder="请输入视频标题" />
      </div>

      <div class="form-item">
        <label>视频地址：</label>
        <input v-model="videoUrl" type="text" placeholder="请输入视频URL" />
      </div>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <div class="form-actions">
        <button type="submit" class="btn-primary" @click="handlePublish">发布</button>
        <button type="button" class="btn-secondary" @click="handleBack">返回</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const title = ref('')
const videoUrl = ref('')
const errorMessage = ref('')

function handlePublish() {
  errorMessage.value = ''

  if (!title.value.trim()) {
    errorMessage.value = '视频标题不能为空'
    return
  }
  if (!videoUrl.value.trim()) {
    errorMessage.value = '视频地址不能为空'
    return
  }

  // TODO: 接入 publishVideo API。
}

function handleBack() {
  router.push('/my-videos')
}
</script>

<style scoped>
.page {
  padding: 20px;
}
.publish-form {
  max-width: 420px;
}
.form-item {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.form-item label {
  font-weight: 500;
}
.form-item input {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.error {
  color: red;
  min-height: 20px;
}
.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}
.btn-primary {
  padding: 8px 20px;
}
.btn-secondary {
  padding: 8px 20px;
}
</style>
