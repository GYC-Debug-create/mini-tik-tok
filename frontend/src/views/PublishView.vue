<template>
  <div class="page">
    <h2 class="page-title">发布视频</h2>

    <div class="form-card publish-form">
      <form @submit.prevent="handlePublish">
        <div class="form-group">
          <label>视频标题</label>
          <input v-model="title" type="text" placeholder="请输入视频标题" />
        </div>

        <div class="form-group">
          <label>视频地址</label>
          <input v-model="videoUrl" type="text" placeholder="请输入视频URL" />
        </div>

        <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary btn-block" :disabled="submitting">
            {{ submitting ? '发布中...' : '发布' }}
          </button>
          <button type="button" class="btn btn-secondary" @click="handleBack">返回</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { publishVideo } from '../api/video'

const router = useRouter()

const title = ref('')
const videoUrl = ref('')
const errorMessage = ref('')
const submitting = ref(false)

async function handlePublish() {
  errorMessage.value = ''

  if (!title.value.trim()) {
    errorMessage.value = '视频标题不能为空'
    return
  }
  if (!videoUrl.value.trim()) {
    errorMessage.value = '视频地址不能为空'
    return
  }

  submitting.value = true
  try {
    await publishVideo({
      title: title.value.trim(),
      videoUrl: videoUrl.value.trim()
    })
    router.push('/my-videos')
  } catch (error) {
    const message = error.response?.data?.message || '发布失败，请检查网络'
    errorMessage.value = message
  } finally {
    submitting.value = false
  }
}

function handleBack() {
  router.push('/my-videos')
}
</script>

<style scoped>
.publish-form {
  max-width: 420px;
}

.form-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: var(--spacing-md);
}
</style>
