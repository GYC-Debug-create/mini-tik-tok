<template>
  <div id="app">
    <!--
      Vue Router 的核心出口,渲染对应 views/*.vue
    -->
    <router-view />
  </div>
</template>

<script setup>
</script>

<style>
/* 这是全局样式，影响所有页面 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: Arial, sans-serif;
  min-height: 100vh;
}
</style>
