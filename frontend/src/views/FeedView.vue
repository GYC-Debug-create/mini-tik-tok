<template>
  <div class="page">
    <h2>视频推荐</h2>

    <div class="video-section">
      <video
        :src="currentVideo.videoUrl"
        controls
        autoplay
        loop
        width="360"
      ></video>
    </div>

    <div class="video-detail">
      <h3>{{ currentVideo.title }}</h3>
      <p>发布者：{{ currentVideo.publisher?.username }}</p>
    </div>

    <div class="actions">
      <button @click="handlePrevious">上一条</button>
      <button
        @click="handleLike"
        :class="{ liked: likedMap[currentVideo.id] }"
      >
        点赞 ({{ currentVideo.likeCount }})
      </button>
      <button @click="handleNext">下一条</button>
    </div>

    <hr />

    <button @click="handleLogout">退出登录</button>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { removeToken } from '../utils/token'

const currentVideo = ref({
  id: null,
  title: '加载中...',
  videoUrl: '',
  likeCount: 0,
  publisher: { username: '' }
})

// TODO: 联调后替换为真实推荐流接口。
const mockVideos = [
  {
    id: 1,
    title: '扣篮集锦 — Slam Dunk',
    videoUrl: 'https://assets.mixkit.co/videos/2289/2289-720.mp4',
    likeCount: 328,
    publisher: { username: '篮球少年' }
  },
  {
    id: 2,
    title: '一对一单挑 — One on One',
    videoUrl: 'https://assets.mixkit.co/videos/44468/44468-720.mp4',
    likeCount: 156,
    publisher: { username: '街球手' }
  },
  {
    id: 3,
    title: '投篮训练 — Shooting Practice',
    videoUrl: 'https://assets.mixkit.co/videos/746/746-720.mp4',
    likeCount: 89,
    publisher: { username: '投篮教练' }
  }
]
let mockIndex = 0

onMounted(() => {
  currentVideo.value = mockVideos[0]
})

const router = useRouter()

function handlePrevious() {
  if (mockIndex > 0) {
    mockIndex--
  } else {
    mockIndex = mockVideos.length - 1
  }
  currentVideo.value = mockVideos[mockIndex]
}

function handleNext() {
  if (mockIndex < mockVideos.length - 1) {
    mockIndex++
    currentVideo.value = mockVideos[mockIndex]
  } else {
    mockIndex = 0
    currentVideo.value = mockVideos[0]
  }
}

const likedMap = reactive({})

function handleLike() {
  const videoId = currentVideo.value.id

  if (likedMap[videoId]) {
    alert('已经点过赞了')
    return
  }

  likedMap[videoId] = true
  currentVideo.value.likeCount++
}

function handleLogout() {
  removeToken()
  router.push('/login')
}
</script>

<style scoped>
.page {
  padding: 20px;
}
.video-section {
  margin-bottom: 12px;
}
.video-detail {
  margin-bottom: 12px;
}
.actions {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}
.liked {
  background-color: #ff4d4f;
  color: white;
  border-color: #ff4d4f;
}
</style>
