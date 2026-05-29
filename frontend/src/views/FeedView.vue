<!--
  FeedView.vue — 视频推荐主页
  职责：展示当前视频、支持上一条/下一条/点赞、管理 currentVideo 状态
-->
<template>
  <div class="page">
    <h2>视频推荐</h2>

    <!--视频播放区域-->
    <div class="video-section">
      <!--
        :src="currentVideo.videoUrl" → 绑定到 script 中 currentVideo.value.videoUrl
        controls → 显示浏览器原生播放控件（播放/暂停/音量/进度条）
        autoplay → 自动播放
        loop    → 循环播放
      -->
      <video
        :src="currentVideo.videoUrl"
        controls
        autoplay
        loop
        width="360"
      ></video>
    </div>
    <!--视频信息-->
    <div class="video-detail">
      <h3>{{ currentVideo.title }}</h3>
      <p>发布者：{{ currentVideo.publisher?.username }}</p>
    </div>

    <div class="actions">
      <button @click="handlePrevious">上一条</button>
      <!--
        :class="{ liked: likedMap[currentVideo.id] }"
          → likedMap[currentVideo.id] 为 true 时，添加 .liked 类（按钮变红）
          → 为 false/undefined 时，不加
      -->
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
// [后端就绪后取消注释] import { getNextVideo } from '../api/feed'

/*
  ref({...}) 创建一个响应式对象容器。
  模板里写 currentVideo.title，Vue 自动解包成 currentVideo.value.title。
  当 currentVideo.value 被整体替换时，页面自动重新渲染。
*/
const currentVideo = ref({
  id: null,
  title: '加载中...',
  videoUrl: '',
  likeCount: 0,
  publisher: { username: '' }
})

// ---- mock 数据（后端就绪后删除此段，恢复 onMounted 中的 API 调用） ----
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

/*
  onMounted(() => {...}) — 组件挂载到 DOM 后执行回调。
  类似 Java 的 @PostConstruct 或构造函数执行完后自动调用的初始化方法。
  此时 template 的 DOM 元素已创建完毕，可以安全操作数据。
*/
onMounted(() => {
  /*
    [mock] 模拟 getNextVideo() 返回值。
    后端就绪后替换为：
      const res = await getNextVideo()
      currentVideo.value = res.data.data
  */
  currentVideo.value = mockVideos[0]
})

const router = useRouter()

// ---- 按钮处理函数（骨架阶段为空，后续步骤逐个实现） ----

/*
  上一条按钮逻辑：
    调 getPreviousVideo(currentVideoId) → 从浏览历史回退
    如果返回 null → 说明已经是第一条记录

  mock 模式：用 mockVideos 数组模拟。后端就绪后替换为 async/await + API 调用。
*/
function handlePrevious() {
  if (mockIndex > 0) {
    // [mock] 模拟 getPreviousVideo 返回了数据 → 回退到历史中更早的一条
    mockIndex--
  } else {
    // [mock] 模拟 getPreviousVideo 返回 null → 降级回到最后一个
    // 后端就绪后替换为：
    //   const res = await getPreviousVideo(currentVideo.value.id)
    //   if (res.data.data) { currentVideo.value = res.data.data }
    //   else { alert('已经是第一个视频') }
    mockIndex = mockVideos.length - 1
  }
  currentVideo.value = mockVideos[mockIndex]
}

/*
  下一条按钮逻辑：
    1. 先调 getForwardVideo(currentVideoId) → 从浏览历史前进
    2. 如果返回 null（没有更新的历史），降级调 getNextVideo() → 拉新推荐

  mock 模式：用 mockVideos 数组模拟。后端就绪后替换为 async/await + API 调用。
*/
function handleNext() {
  if (mockIndex < mockVideos.length - 1) {
    // [mock] 模拟 getForwardVideo 返回了数据 → 前进到历史中更新的一条
    mockIndex++
    currentVideo.value = mockVideos[mockIndex]
  } else {
    // [mock] 模拟 getForwardVideo 返回 null → 降级调 getNextVideo → 拉到新视频
    // 后端就绪后替换为：
    //   const res = await getForwardVideo(currentVideo.value.id)
    //   if (res.data.data) { currentVideo.value = res.data.data }
    //   else { const nextRes = await getNextVideo(); currentVideo.value = nextRes.data.data }
    mockIndex = 0
    currentVideo.value = mockVideos[0]
  }
}

/*
  点赞按钮逻辑：
    调 likeVideo(currentVideo.id) → POST /api/videos/{id}/like
    后端判断是否已点赞 → 返回新的 likeCount
    前端以后端返回值为准更新 currentVideo.likeCount，不本地 +1

  为什么不能本地直接 +1？
    1. 用户狂点 5 次，本地 +5，后端只认第一次 → 数据对不上
    2. 后端判断重复点赞直接返回 200，实际计数没变

  mock 模式：用 Set 记录已点赞视频 ID 模拟后端去重。
*/
/*
  likedMap: 记录每个视频是否已点赞，模拟后端点赞记录表。
  用 reactive({}) 而非 Set，因为 reactive 对象的属性变更会通知模板重新渲染。
  模板中 likedMap[currentVideo.id] 读取该视频的点赞状态。
*/
const likedMap = reactive({})

function handleLike() {
  const videoId = currentVideo.value.id

  // [mock] 模拟后端判断：这个视频你点过赞了吗？
  if (likedMap[videoId]) {
    alert('已经点过赞了')
    return
  }

  // [mock] 模拟后端点赞成功，返回新的 likeCount
  // 后端就绪后替换为：
  //   const res = await likeVideo(videoId)
  //   currentVideo.value.likeCount = res.data.data.likeCount
  likedMap[videoId] = true
  currentVideo.value.likeCount++
}

function handleLogout() {
  /*
    removeToken() → 清除 localStorage 中的 token
    router.push('/login') → 跳转登录页
    跳转时触发 router.beforeEach 守卫，此时已无 token，守卫放行 /login
  */
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
