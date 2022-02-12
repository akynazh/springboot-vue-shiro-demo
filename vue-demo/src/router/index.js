import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login'
import Index from '../views/Index'
Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    component: Home,
  },
  {
    path: '/login',
    component: Login,
  },
  {
    path: '/index',
    component: Index,
    meta: {
      auth: true
    }
  }
]

const router = new VueRouter({
  routes
})

export default router
