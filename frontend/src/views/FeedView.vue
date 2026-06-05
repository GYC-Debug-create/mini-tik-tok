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

      <div v-if="!currentVideo.id && !loading" class="empty-tip">
        {{ emptyMessage || '暂无推荐视频' }}
      </div>
      <div v-if="loading" class="empty-tip">加载中...</div>
      <div v-if="toastMessage" class="toast-tip">{{ toastMessage }}</div>

      <div v-if="currentVideo.id" class="video-info">
        <p class="video-author">@{{ currentVideo.publisher?.username || '匿名' }}</p>
        <h3 class="video-title">{{ currentVideo.title }}</h3>
      </div>

      <div class="right-actions">
        <button
          class="btn action-btn"
          :class="{ liked: likedMap[currentVideo.id] }"
          :disabled="!currentVideo.id"
          @click="handleLike"
        >
          <span class="action-icon">♥</span>
          <span class="action-label">{{ currentVideo.likeCount || 0 }}</span>
        </button>

        <button class="btn action-btn" :disabled="!currentVideo.id || loading" @click="handlePrevious">
          <span class="action-icon">▲</span>
          <span class="action-label">上一条</span>
        </button>

        <button class="btn action-btn" :disabled="loading" @click="handleNext">
          <span class="action-icon">▼</span>
          <span class="action-label">下一条</span>
        </button>
      </div>
    </div>

    <button class="btn btn-secondary logout-btn" @click="handleLogout">退出</button>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onBeforeUnmount, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { removeToken } from '../utils/token'
import { getForwardVideo, getNextVideo, getPreviousVideo } from '../api/feed'
import { likeVideo } from '../api/video'

const currentVideo = ref({
  id: null,
  title: '',
  videoUrl: '',
  likeCount: 0,
  publisher: { username: '' }
})
const loading = ref(false)
const emptyMessage = ref('')
const toastMessage = ref('')
const history = ref([])
const historyIndex = ref(-1)
const serverHistoryMode = ref(false)
const likedMap = reactive({})
let toastTimer = null

const canGoPrevious = computed(() => historyIndex.value > 0)
const canGoForward = computed(() => historyIndex.value >= 0 && historyIndex.value < history.value.length - 1)

const router = useRouter()

function setCurrent(video) {
  currentVideo.value = video
  emptyMessage.value = ''
  toastMessage.value = ''
}

function showToast(message) {
  toastMessage.value = message
  if (toastTimer) {
    clearTimeout(toastTimer)
  }
  toastTimer = setTimeout(() => {
    toastMessage.value = ''
    toastTimer = null
  }, 2200)
}

async function loadNext() {
  if (canGoForward.value) {
    historyIndex.value += 1
    setCurrent(history.value[historyIndex.value])
    return
  }

  loading.value = true
  emptyMessage.value = ''
  try {
    const res = await getNextVideo()
    const video = res.data?.data
    if (!video?.id) {
      if (currentVideo.value.id) {
        showToast(res.data?.message || '暂无更多推荐视频')
      } else {
        emptyMessage.value = res.data?.message || '暂无更多推荐视频'
      }
      return
    }
    // 兜底返回了当前正在看的视频 → 说明已是最后一个
    if (currentVideo.value.id && video.id === currentVideo.value.id) {
      showToast('已是最后一个视频')
      return
    }
    history.value.push(video)
    historyIndex.value = history.value.length - 1
    serverHistoryMode.value = false
    setCurrent(video)
  } catch (error) {
    const message = error.response?.data?.message || '推荐流加载失败'
    if (currentVideo.value.id) {
      showToast(message)
    } else {
      emptyMessage.value = message
    }
  } finally {
    loading.value = false
  }
}

async function handlePrevious() {
  if (!currentVideo.value.id || loading.value) return
  if (canGoPrevious.value) {
    historyIndex.value -= 1
    setCurrent(history.value[historyIndex.value])
    return
  }

  loading.value = true
  try {
    const res = await getPreviousVideo(currentVideo.value.id)
    const video = res.data?.data
    if (!video?.id) {
      showToast(res.data?.message || '已是第一个视频')
      return
    }
    serverHistoryMode.value = true
    setCurrent(video)
  } catch (error) {
    showToast(error.response?.data?.message || '上一条加载失败')
  } finally {
    loading.value = false
  }
}

function handleNext() {
  if (serverHistoryMode.value && currentVideo.value.id && !canGoForward.value) {
    loadForward()
    return
  }
  loadNext()
}

async function loadForward() {
  loading.value = true
  try {
    const res = await getForwardVideo(currentVideo.value.id)
    const video = res.data?.data
    if (!video?.id) {
      serverHistoryMode.value = false
      showToast(res.data?.message || '已是最近浏览的视频')
      return
    }
    setCurrent(video)
    const localCurrent = history.value[historyIndex.value]
    if (localCurrent?.id === video.id) {
      serverHistoryMode.value = false
    }
  } catch (error) {
    showToast(error.response?.data?.message || '下一条加载失败')
  } finally {
    loading.value = false
  }
}

async function handleLike() {
  const videoId = currentVideo.value.id
  if (!videoId) return
  if (likedMap[videoId]) {
    showToast('已经点过赞了')
    return
  }

  try {
    await likeVideo(videoId)
    likedMap[videoId] = true
    currentVideo.value.likeCount = (currentVideo.value.likeCount || 0) + 1
  } catch (error) {
    showToast(error.response?.data?.message || '点赞失败')
  }
}

function handleLogout() {
  removeToken()
  router.push('/login')
}

onMounted(() => {
  loadNext()
})

onBeforeUnmount(() => {
  if (toastTimer) {
    clearTimeout(toastTimer)
  }
})
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

.empty-tip {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: rgba(255, 255, 255, 0.72);
  font-size: 14px;
  text-align: center;
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 8px;
}

.toast-tip {
  position: absolute;
  top: 64px;
  left: 50%;
  z-index: 30;
  max-width: calc(100% - 48px);
  transform: translateX(-50%);
  padding: 8px 14px;
  border-radius: 18px;
  background: rgba(22, 24, 35, 0.72);
  color: #fff;
  font-size: 13px;
  line-height: 1.35;
  text-align: center;
  pointer-events: none;
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

.action-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
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
