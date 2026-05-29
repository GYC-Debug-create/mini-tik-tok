<!--
  MyVideosView.vue — 我的视频管理页
  职责：分页展示当前用户发布的视频列表，支持删除操作和跳转发布页
  当前状态：Phase 1B 空壳，Phase 5 实现业务逻辑
  核心状态（Phase 5）：
    - myVideos:   视频列表数组
    - page:       当前页码（从1开始）
    - pageSize:   每页数量（默认10）
    - total:      视频总数
    - loading:    加载状态
-->
<template>
  <div class="page">
    <h2>我的视频</h2>
    <!-- Phase 5 将在此处添加：
         视频列表（v-for 渲染）、
         每条视频的删除按钮（调用 deleteVideo 后刷新列表）、
         分页控件（上一页/下一页，展示 total）、
         跳转发布页的按钮（router.push('/publish')）
    -->
  </div>
</template>

<script setup>
// Phase 5 将引入：
//   - ref/reactive 管理列表和分页状态
//   - onMounted 生命周期钩子，进入页面时自动请求第一页数据
//   - 调用 api/video.js（getMyVideos / deleteVideo）
//   - userRouter 跳转 /publish
</script>

<style scoped>
.page {
  padding: 20px;
}
</style>
