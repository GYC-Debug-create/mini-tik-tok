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
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: Arial, sans-serif;
  min-height: 100vh;
  padding-bottom: 56px;
}

.tab-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 56px;
  display: flex;
  justify-content: space-around;
  align-items: center;
  border-top: 1px solid #ddd;
  background: #fff;
}

.tab-bar a {
  color: #333;
  text-decoration: none;
}

.tab-bar a.router-link-active {
  font-weight: bold;
}
</style>
