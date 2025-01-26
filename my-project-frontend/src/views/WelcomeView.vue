<script setup>
// 当前脚本未包含逻辑代码，但可以在此处引入需要的组件、工具函数等
</script>

<template>
  <!-- 页面主容器，设置全屏视图 -->
  <div style="width: 100vw; height: 100vh; overflow: hidden; display: flex">
    <!-- 左侧区域，黑色背景覆盖整个页面 -->
    <div style="flex: 1; background-color: white">
      <!-- 背景图片铺满整个左侧区域 -->
      <!-- `el-image` 是 Element Plus 提供的图片组件，设置图片填充方式为 cover -->
      <el-image
          style="width: 100%; height: 100%"
          fit="cover"
          src="/picture/329520.JPG"
      />
      <!-- 提供的备选图片路径作为注释保留 -->
      <!-- @/assets/picture/329492.JPG -->
      <!-- 远程图片地址备选：https://pic3.zhimg.com/v2-5fb13110e1de13d4c11e6e7f5b8026da_r.jpg -->
    </div>

    <!-- 右侧标题和介绍部分 -->
    <div class="welcome-title">
      <!-- 欢迎语，设置大字体加粗 -->
      <div style="font-size: 60px; font-weight: bold">
        欢迎来到猫猫的监控系统
      </div>
      <!-- 学习介绍，增加一些额外文字 -->
      <div style="font-size: 20px; margin-top: 20px">
        猫猫在凝视你
      </div>
    </div>

    <!-- 右侧附加卡片区域 -->
    <div class="right-card">
      <!-- 动态路由视图：支持切换显示不同的页面组件 -->
      <!-- 使用 Vue Router 的 router-view 插槽实现，提供动态渲染组件 -->
      <router-view :key="$route.fullPath" v-slot="{ Component }">
        <!-- 淡入淡出效果：使用 Element Plus 的 `el-fade-in-linear` 过渡动画 -->
        <transition name="el-fade-in-linear" mode="out-in">
          <!-- 动态渲染子组件 -->
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<style scoped>
/* 定义欢迎语的样式 */
.welcome-title {
  position: absolute; /* 定位到页面的左下角 */
  bottom: 120px; /* 距离底部的距离 */
  left: 30px; /* 距离左侧的距离 */
  color: white; /* 白色字体 */
  text-shadow: 0 0 10px black; /* 字体阴影效果，增加对比度 */
}

/* 定义右侧卡片样式 */
.right-card {
  width: 400px; /* 固定宽度 */
  z-index: 1; /* 提高层级，防止被其他元素遮盖 */
  background-color: var(--el-bg-color); /* 设置element ui自带的背景颜色,当变成深色模式的时候,会自动切换颜色 */
}
</style>
