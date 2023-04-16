import { createRouter, createWebHashHistory } from 'vue-router'
import Wrapper from '@/views/Wrapper.vue'

const routes = [
  {path: '/:key?', component: Wrapper},
  // {path: '/', component: Wrapper}
]

const router = createRouter({
  routes,
  history: createWebHashHistory()
})

export default router
