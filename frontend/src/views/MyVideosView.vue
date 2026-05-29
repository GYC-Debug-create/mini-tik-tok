<template>
  <div class="page">
    <h2>我的视频</h2>

    <div class="toolbar">
      <button class="btn-primary" @click="goToPublish">发布视频</button>
    </div>

    <!-- 空状态 -->
    <p v-if="myVideos.length === 0" class="empty-hint">暂无视频</p>

    <!-- 视频列表 -->
    <div v-else class="video-list">
      <div v-for="video in myVideos" :key="video.id" class="video-card">
        <span class="video-title">{{ video.title }}</span>
        <button class="btn-danger" @click="handleDelete(video.id)">删除</button>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <button :disabled="page <= 1" @click="goToPage(page - 1)">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页（共 {{ total }} 条）</span>
      <button :disabled="page >= totalPages" @click="goToPage(page + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const myVideos = ref([])

const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

// ---- mock 数据（后端就绪后删除此段，恢复 API 调用） ----
const mockStore = [
  { id: 1, title: '扣篮集锦 — Slam Dunk' },
  { id: 2, title: '一对一单挑 — One on One' },
  { id: 3, title: '投篮训练 — Shooting Practice' },
  { id: 4, title: '街球花式 — Street Moves' },
  { id: 5, title: '三分大赛 — Three Point Contest' },
  { id: 6, title: '火锅大帽 — Block Party' },
  { id: 7, title: '空中接力 — Alley Oop' },
  { id: 8, title: '背后运球 — Behind the Back' },
  { id: 9, title: '欧洲步上篮 — Euro Step' },
  { id: 10, title: '后仰跳投 — Fadeaway' },
  { id: 11, title: '变向过人 — Crossover' },
  { id: 12, title: '打板三分 — Bank Shot' }
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
  // 如果当前页删空了且不是第一页，回退一页
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
.page {
  padding: 20px;
}
.toolbar {
  margin-bottom: 16px;
}
.btn-primary {
  padding: 8px 20px;
}
.empty-hint {
  color: #999;
}
.video-list {
  margin-bottom: 16px;
}
.video-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}
.video-title {
  font-weight: 500;
}
.btn-danger {
  padding: 4px 12px;
  color: red;
  border-color: red;
}
.pagination {
  display: flex;
  align-items: center;
  gap: 12px;
}
.pagination span {
  font-size: 14px;
  color: #666;
}
</style>
