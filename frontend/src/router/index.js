import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/token'

const routes = [
  {
    path: '/login',
    name: 'Login',
    // () => import(...) 是懒加载：只有首次访问 /login 时才下载 LoginView.vue 的 JS
    // 好处：首屏只加载当前页面需要的代码，其他页面用到时才加载，减少初始体积
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/feed',
    name: 'Feed',
    component: () => import('../views/FeedView.vue')
  },
  {
    path: '/my-videos',
    name: 'MyVideos',
    component: () => import('../views/MyVideosView.vue')
  },
  {
    path: '/publish',
    name: 'Publish',
    component: () => import('../views/PublishView.vue')
  },
  {
    path: '/',
    // 根路径 / 自动重定向到 /login，用户打开网站直接进登录页
    redirect: '/login'
  }
]

const router = createRouter({
  // createWebHistory() 使用 HTML5 History 模式（无 # 号）
  // 对比 createWebHashHistory()：URL 带 #，如 /#/login
  // History 模式 URL 更干净，但部署时需要服务端配置 fallback 到 index.html
  history: createWebHistory(),
  routes
})

// 路由守卫：每次跳转前检查 token
// 类似 Java 拦截器的 preHandle
router.beforeEach((to, from, next) => {
  const token = getToken()

  // 要去的不是 /login 且没有 token → 强制跳登录
  // [临时注释] Phase 4 开发期间跳过校验
  if (to.path !== '/login' && !token) {
    // next('/login')
    next()
  } else {
    next()
  }
})

export default router
