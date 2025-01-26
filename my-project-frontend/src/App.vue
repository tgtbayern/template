<!--•	作用：Vue 应用的根组件，所有页面都会在其基础上进行渲染。-->
<!--•	功能：-->
<!--•	充当应用的整体布局和容器。-->
<!--•	包含导航栏、头部或底部等全局组件。-->
<!--•	使用 <router-view /> 动态加载路由对应的子组件。-->

<script setup>
import { useDark, useToggle } from '@vueuse/core';

useDark({
  selector: 'html',
  attribute: 'class',
  valueDark: 'dark',
  valueLight: 'light'
});

useToggle(dark);
</script>

<template>
  <div>
    <router-view/>
  </div>
</template>

<style scoped>

</style>
