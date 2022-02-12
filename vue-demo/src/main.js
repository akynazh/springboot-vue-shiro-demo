import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
// npm install element-ui
import Element, {Message} from 'element-ui'
import "element-ui/lib/theme-chalk/index.css"
// npm install axios
import axios from "axios"

Vue.config.productionTip = false
Vue.prototype.$axios = axios
Vue.use(Element)
Vue.use(axios)

// axios请求前置拦截
axios.defaults.baseURL = "http://localhost:8080"
axios.interceptors.response.use(
    response => {
        if (response.status === 200) {
            console.log(response.data)
            return Promise.resolve(response)
        } else {
            return Promise.reject(response)
        }
    },
    error => {
        let response = error.response
        Message.error({
            message: response.data.message,
            duration: 1500,
        })
        return Promise.reject(response)
    }
)

// router前置拦截
router.beforeEach((to, from, next) => {
    if (to.matched.some(record => record.meta.auth)) {
        const token = localStorage.getItem("token")
        if (token) {
            next()
        } else {
            Element.Message({
                message: "请先登录哦",
                duration: 1000
            })
            next({
                path: '/login'
            })
        }
    } else {
        next()
    }
})

// 创建vue实例
new Vue({
    router,
    store,
    render: h => h(App)
}).$mount('#app')
