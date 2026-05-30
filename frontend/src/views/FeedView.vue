<template>
  <div class="page feed-page">
    <div class="video-stage">
      <video
        :src="currentVideo.videoUrl"
        controls
        autoplay
        loop
        class="video-player"
      ></video>

      <div class="video-info">
        <p class="video-author">@{{ currentVideo.publisher?.username }}</p>
        <h3 class="video-title">{{ currentVideo.title }}</h3>
      </div>

      <div class="right-actions">
        <button
          class="btn action-btn"
          :class="{ liked: likedMap[currentVideo.id] }"
          @click="handleLike"
        >
          <span class="action-icon">♥</span>
          <span class="action-label">{{ currentVideo.likeCount }}</span>
        </button>

        <button class="btn action-btn" @click="handlePrevious">
          <span class="action-icon">▲</span>
          <span class="action-label">上一条</span>
        </button>

        <button class="btn action-btn" @click="handleNext">
          <span class="action-icon">▼</span>
          <span class="action-label">下一条</span>
        </button>
      </div>
    </div>

    <button class="btn btn-secondary logout-btn" @click="handleLogout">退出</button>
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
/* ============================================
   页面容器 — 100dvh + flex column，禁止滚动
   ============================================ */
.feed-page {
  height: 100dvh;
  max-width: 100vw;
  overflow: hidden;
  padding: 0;
  display: flex;
  flex-direction: column;
  background: #000;
  position: relative;
}

/* ============================================
   视频舞台 — flex:1 占满剩余空间
   用 padding-bottom 为底部 tab-bar 留空
   确保原生 controls 不被 tab-bar 遮挡
   ============================================ */
.video-stage {
  flex: 1;
  min-height: 0;
  position: relative;
  background: #000;
  padding-bottom: var(--tab-bar-height);
  box-sizing: border-box;
}

/* ============================================
   视频元素
   ============================================ */
.video-player {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center center;
  background: #000;
  display: block;
}

/* ============================================
   左下角信息区 — 作者在上、标题在下
   bottom: 72px 避让原生 controls + tab-bar
   ============================================ */
.video-info {
  position: absolute;
  left: var(--spacing-md);
  right: 72px;
  bottom: 135px;
  pointer-events: none;
}

.video-author {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 6px;
}

.video-title {
  font-size: 14px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.88);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ============================================
   右侧操作栏
   bottom: 96px 避让原生 controls + tab-bar
   ============================================ */
.right-actions {
  position: absolute;
  right: 8px;
  bottom: 167px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  pointer-events: none;
}

.right-actions .action-btn {
  pointer-events: auto;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 6px 4px;
  background: transparent;
  border: none;
  color: #fff;
  font-size: var(--font-size-sm);
  min-width: 48px;
}

.action-icon {
  font-size: 24px;
  line-height: 1;
}

.action-label {
  font-size: 11px;
  line-height: 1;
}

.action-btn.liked {
  color: var(--color-primary);
}

/* ============================================
   退出按钮 — 右上角，高 z-index 确保可点
   ============================================ */
.logout-btn {
  position: absolute;
  top: var(--spacing-md);
  right: var(--spacing-md);
  z-index: 20;
  background: rgba(0, 0, 0, 0.4);
  color: rgba(255, 255, 255, 0.7);
  border: none;
  font-size: var(--font-size-sm);
  padding: 4px 12px;
}
</style>
