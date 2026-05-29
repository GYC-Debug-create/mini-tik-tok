import { createRouter, createWebHistory } from 'vue-router'


const routes = [
  {
    path: '/login',
    name: 'Login',
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
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  // [临时] Phase 4-5 开发期间跳过 token 校验，直接放行
  next()
})

export default router
