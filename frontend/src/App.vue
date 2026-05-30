<template>
  <div id="app">
    <router-view />

    <nav v-if="showTabBar" class="tab-bar">
      <router-link to="/feed">推荐</router-link>
      <router-link to="/my-videos">我的视频</router-link>
      <router-link to="/publish">发布</router-link>
    </nav>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const showTabBar = computed(() => route.path !== '/login')
</script>

<style>
#app {
  min-height: 100vh;
}
</style>
