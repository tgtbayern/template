<script setup>
import { useDark, useToggle } from '@vueuse/core';

// 创建一个响应式的 dark 变量，useDark() 返回的是一个 ref
const dark = useDark({
  selector: 'html',
  attribute: 'class',
  valueDark: 'dark',
  valueLight: 'light'
});

// useToggle 需要一个 ref 变量
const toggleDark = useToggle(dark);
</script>

<template>
  <div>
    <!-- 添加按钮，点击切换暗黑模式 -->
    <button @click="toggleDark()">切换主题</button>
    <router-view />
  </div>
</template>

<style scoped>
/* 可以在这里自定义暗黑模式的样式 */
</style>
