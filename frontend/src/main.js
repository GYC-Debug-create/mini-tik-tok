// main.js — 应用入口
// 职责：创建 Vue 实例，安装 Router，挂载到 #app
// 这是整个前端的启动点，index.html 通过 <script type="module" src="/src/main.js"> 加载它
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// createApp(App)：以 App.vue 为根组件创建 Vue 应用实例
// .use(router)：安装 Vue Router 插件，使所有页面能使用路由功能
// .mount('#app')：挂载到 index.html 中的 <div id="app">
const app = createApp(App)
app.use(router)
app.mount('#app')
