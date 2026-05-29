import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/token'

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
  const token = getToken()

  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
