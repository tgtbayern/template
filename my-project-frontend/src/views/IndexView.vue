<script setup>
import { logout } from "@/net";  // 从 @/net 模块导入 logout 函数
import router from "@/router";  // 导入 Vue Router 实例，用于路由跳转

// 定义一个名为 userLogout 的方法，处理用户退出登录逻辑
function userLogout() {
  // 调用 logout 函数，并传入一个回调函数，在成功退出后执行跳转
  logout(() => router.push('/'));  // 退出后跳转到首页 '/'
}
</script>

<template>
  <div>
    <!-- el-button 点击事件绑定到 userLogout 方法，点击按钮触发退出操作 -->
    <el-button @click="userLogout">退出登录</el-button>
  </div>
</template>

<style scoped>
/* 样式定义在此处，scoped 表示样式仅对当前组件生效 */
</style>
