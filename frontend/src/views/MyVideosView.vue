<template>
  <div class="page my-videos-page">
    <!-- 顶部 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">我的视频</h2>
        <p class="page-subtitle">管理你发布过的视频</p>
      </div>
      <button class="publish-btn" @click="goToPublish">+ 发布视频</button>
    </div>

    <!-- 空状态 -->
    <p v-if="myVideos.length === 0" class="empty-state">暂无视频</p>

    <!-- 视频卡片网格 -->
    <div v-else class="video-grid">
      <div v-for="video in myVideos" :key="video.id" class="video-card">
        <div class="card-cover">
          <video
            v-if="video.videoUrl"
            :src="video.videoUrl"
            muted
            preload="metadata"
          />
          <span v-else class="play-icon">▶</span>
        </div>
        <div class="card-info">
          <p class="card-title">{{ video.title }}</p>
          <button class="delete-btn" @click="handleDelete(video.id)">删除</button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="myVideos.length > 0" class="pagination">
      <button class="btn btn-secondary" :disabled="page <= 1" @click="goToPage(page - 1)">上一页</button>
      <span class="pagination-info">第 {{ page }} / {{ totalPages }} 页（共 {{ total }} 条）</span>
      <button class="btn btn-secondary" :disabled="page >= totalPages" @click="goToPage(page + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const myVideos = ref([])

const page = ref(1)
const pageSize = ref(9)
const total = ref(0)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

// ---- mock 数据（后端就绪后删除此段，恢复 API 调用） ----
const mockStore = [
  { id: 1, title: '扣篮集锦 — Slam Dunk', videoUrl: 'https://cdn.coverr.co/videos/coverr-slow-mo-basketball-hoop-5187/1080p.mp4' },
  { id: 2, title: '一对一单挑 — One on One', videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4' },
  { id: 3, title: '投篮训练 — Shooting Practice', videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4' },
  { id: 4, title: '街球花式 — Street Moves', videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4' },
  { id: 5, title: '三分大赛 — Three Point Contest', videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4' },
  { id: 6, title: '火锅大帽 — Block Party', videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4' },
  { id: 7, title: '空中接力 — Alley Oop', videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4' },
  { id: 8, title: '背后运球 — Behind the Back', videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4' },
  { id: 9, title: '欧洲步上篮 — Euro Step', videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4' },
  { id: 10, title: '后仰跳投 — Fadeaway', videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4' },
  { id: 11, title: '变向过人 — Crossover', videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4' },
  { id: 12, title: '打板三分 — Bank Shot', videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4' }
]

function fetchPage() {
  // [mock] 模拟 getMyVideos({ page, pageSize }) 返回值
  // 后端就绪后替换为：
  //   const res = await getMyVideos({ page: page.value, pageSize: pageSize.value })
  //   myVideos.value = res.data.data.records
  //   total.value = res.data.data.total
  const start = (page.value - 1) * pageSize.value
  myVideos.value = mockStore.slice(start, start + pageSize.value)
  total.value = mockStore.length
}

onMounted(() => {
  fetchPage()
})

function goToPage(newPage) {
  page.value = newPage
  fetchPage()
}

function handleDelete(videoId) {
  // [mock] 模拟 deleteVideo(videoId) 成功
  // 后端就绪后替换为：
  //   await deleteVideo(videoId)
  //   fetchPage()
  const index = mockStore.findIndex(v => v.id === videoId)
  if (index !== -1) {
    mockStore.splice(index, 1)
  }
  const maxPage = Math.max(1, Math.ceil(mockStore.length / pageSize.value))
  if (page.value > maxPage) {
    page.value = maxPage
  }
  fetchPage()
}

function goToPublish() {
  router.push('/publish')
}
</script>

<style scoped>
/* 页面整体 */
.my-videos-page {
  max-width: 960px;
  background: #f7f7f8;
}

/* 顶部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-subtitle {
  margin-top: 6px;
  font-size: 14px;
  color: var(--color-text-muted);
}

.publish-btn {
  height: 44px;
  padding: 0 24px;
  border-radius: 22px;
  border: none;
  background: var(--color-primary);
  color: var(--color-white);
  font-size: var(--font-size-base);
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.publish-btn:hover {
  opacity: 0.85;
}

/* 卡片网格 */
.video-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin-bottom: 24px;
}

/* 卡片 */
.video-card {
  background: var(--color-white);
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
}

/* 封面区 */
.card-cover {
  aspect-ratio: 16 / 9;
  background: #000;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-cover video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.play-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.6);
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 卡片信息 */
.card-info {
  padding: 12px 14px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.4;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 8px;
}

/* 删除按钮弱化 — color-mix 基于 token 派生，改主色自动同步 */
.delete-btn {
  flex-shrink: 0;
  height: 32px;
  padding: 0 14px;
  border-radius: 16px;
  border: 1px solid color-mix(in srgb, var(--color-primary) 35%, transparent);
  color: var(--color-primary);
  background: var(--color-white);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.delete-btn:hover {
  background: color-mix(in srgb, var(--color-primary) 8%, transparent);
}

/* 响应式 */
@media (max-width: 900px) {
  .video-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 600px) {
  .video-grid {
    grid-template-columns: 1fr;
  }
}
</style>
