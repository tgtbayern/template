<!--•	作用：一个具体的页面组件，通常作为首页或欢迎页。-->
<!--•	功能：-->
<!--•	定义页面的模板和逻辑。-->
<!--•	通过路由进行访问和展示。-->

<script setup>

</script>

<template>
  <!-- 页面主容器，设置全屏视图 -->
  <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">

    <!-- 左侧区域，黑色背景覆盖整个页面 -->
    <div style="flex: 1;background-color: white">

      <!-- 背景图片铺满整个左侧区域 -->
      <el-image style="width: 100%;height: 100%" fit="cover"
                src=" /picture/329520.JPG"/>
<!--      @/assets/picture/329492.JPG-->
<!--                https://pic3.zhimg.com/v2-5fb13110e1de13d4c11e6e7f5b8026da_r.jpg-->
    </div>

    <!-- 右侧标题和介绍部分 -->
    <div class="welcome-title">

      <!-- 欢迎语 -->
      <div style="font-size: 60px;font-weight: bold">欢迎来到猫猫的监控系统</div>

      <!-- 学习介绍 -->
      <div style="font-size: 20px;margin-top: 20px">猫猫在凝视你</div>
    </div>

    <!-- 右侧附加卡片区域 -->
    <div class="right-card">
      <router-view/>
    </div>
  </div>
</template>

<style scoped>
.welcome-title {
  position: absolute;
  bottom: 120px;
  left: 30px;
  color: white;
  text-shadow: 0 0 10px black;
}

.right-card {
  width: 400px;
  z-index: 1;
  background-color: white;
}

</style>