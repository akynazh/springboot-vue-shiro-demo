<template>
  <div>
    <Header></Header>
    <el-table
        :data="userInfo"
        class="userInfo">
      <el-table-column
          prop="userName"
          label="用户名"
          width="180">
      </el-table-column>
      <el-table-column
          prop="userEmail"
          label="邮箱"
          width="180">
      </el-table-column>
      <el-table-column
          prop="userRole"
          label="角色">
      </el-table-column>
    </el-table>
    <br/>
    <br/>
    <el-row>
      <el-button type="primary" @click="toStudent">Student</el-button>
      <el-button type="primary" @click="toTeacher">Teacher</el-button>
      <el-button type="primary" @click="toAdminView">AdminView</el-button>
      <el-button type="primary" @click="toAdminEdit">AdminEdit</el-button>
      <el-button type="primary" @click="logout">Logout</el-button>
    </el-row>
    <Footer></Footer>
  </div>
</template>

<script>
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import {Message} from "element-ui"
import router from "@/router";

export default {
  name: "Index",
  components: {Footer, Header},
  data() {
    let user = this.$store.getters.getUser;
    return {
      userInfo: [{
        userName: user.userName,
        userEmail: user.userEmail,
        userRole: user.userRole
      }]
    }
  },
  methods: {
    toStudent() {
      const that = this
      this.$axios.get('/student/index', {
        headers: {
          "auth": localStorage.getItem("token")
        }
      }).then(res => {
        Message.success({
          message: "访问student页面成功",
          duration: 1000
        })
      })
    },
    toTeacher() {
      const that = this
      this.$axios.get('/teacher/index', {
        headers: {
          "auth": localStorage.getItem("token")
        }
      }).then(res => {
        Message.success({
          message: "访问teacher页面成功",
          duration: 1000
        })
      })
    },
    toAdminView() {
      const that = this
      this.$axios.get('/admin/index', {
        headers: {
          "auth": localStorage.getItem("token")
        }
      }).then(res => {
        Message.success({
          message: "访问admin-index页面成功",
          duration: 1000
        })
      })
    },
    toAdminEdit() {
      const that = this
      this.$axios.get('/admin/edit', {
        headers: {
          "auth": localStorage.getItem("token")
        }
      }).then(res => {
        Message.success({
          message: "访问admin-edit页面成功",
          duration: 1000
        })
      })
    },
    logout() {
      const that = this
      this.$axios.get('/logout', {
        "auth": localStorage.getItem("token")
      }).then(res => {
        localStorage.removeItem("token")
        Message.success({
          message: "注销成功",
          duration: 1000
        })
        router.replace("/login")
      })
    }
  }
}
</script>

<style scoped>
.userInfo {
  margin: 0 auto;
  max-width: 600px;
}
</style>