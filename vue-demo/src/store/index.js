import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
    state: {
        token: localStorage.getItem("token"),
        user: JSON.parse(sessionStorage.getItem("user"))
    },
    mutations: {
        SET_TOKEN: (state, token) => {
            state.token = token
            localStorage.setItem("token", token)
        },
        SET_USER: (state, user) => {
            state.user = user
            sessionStorage.setItem("user", JSON.stringify(user))
        },
        REMOVE_INFO: state => {
            state.token = null
            state.user = null
            localStorage.setItem("token", null)
            sessionStorage.setItem("user", null)
        }
    },
    getters: {
        getUser: state => {
            return state.user
        }
    }
})