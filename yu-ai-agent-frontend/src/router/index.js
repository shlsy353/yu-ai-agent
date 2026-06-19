import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
  },
  {
    path: '/love-master',
    name: 'LoveMaster',
    component: () => import('../views/LoveMaster.vue'),
  },
  {
    path: '/manus',
    name: 'Manus',
    component: () => import('../views/Manus.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router