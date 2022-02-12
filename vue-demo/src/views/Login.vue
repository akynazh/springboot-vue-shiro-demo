<template>
  <div>
    <Header></Header>
    <h1>Login</h1>
    <el-form :model="loginForm" :rules="loginRules" ref="loginForm" class="login-form">
      <el-form-item label="邮箱">
        <el-input v-model="loginForm.email"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="loginForm.password"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm('loginForm')">登录</el-button>
      </el-form-item>
    </el-form>
    <Footer></Footer>
  </div>
</template>

<script>

import Header from "@/components/Header";
import Footer from "@/components/Footer";
import router from "@/router";
import {Message} from "element-ui"

export default {
  name: "Login",
  components: {Footer, Header},
  data() {
    return {
      loginForm: {
        email: '',
        password: ''
      },
      loginRules: {
        email: {
          required: true,
          message: '请输入邮箱',
          trigger: 'blur',
        },
        password: {
          required: true,
          message: '请输入密码',
          trigger: 'blur'
        }
      }
    }
  },
  methods: {
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const that = this;
          this.$axios.post('/login', this.loginForm).then(res => {
            const token = res.headers['auth'] // Auth被转成小写！不要用大写字母了
            const user = res.data.data
            that.$store.commit("SET_TOKEN", token)
            that.$store.commit("SET_USER", user)
            Message.success({
              message: "登录成功",
              duration: 1000
            })
            router.push("/index")
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.login-form {
  margin: 0 auto;
  max-width: 400px;
}
</style>