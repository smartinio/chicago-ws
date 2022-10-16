import Vue from 'vue'
import Router from 'vue-router'
import Wrapper from '@/views/Wrapper'

Vue.use(Router)

const routes = [
  {path: '/:key', component: Wrapper},
  {path: '/', component: Wrapper}
]
const router = new Router({
  base: '/',
  routes,
  mode: 'history'
})

export default router
