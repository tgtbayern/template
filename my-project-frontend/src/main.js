// •	作用：Vue 应用的入口文件。
// •	功能：
// •	创建 Vue 实例并挂载到 HTML 页面。
// •	导入全局组件、插件和样式。
// •	初始化路由、状态管理等。

import { createApp } from 'vue'  // 从 vue 导入 createApp 方法，用于创建 Vue 应用实例
import App from './App.vue'      // 导入根组件 App.vue
import router from '@/router'    // 导入路由配置文件

// 创建 Vue 应用实例，传入根组件 App
const app = createApp(App)

// 使用路由插件，将 router 注入到 Vue 实例中
app.use(router)

// 将应用挂载到页面中 id 为 app 的 DOM 元素上
app.mount('#app')  