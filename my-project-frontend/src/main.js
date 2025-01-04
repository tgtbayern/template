// 从 Vue 框架中导入 createApp 方法，用于创建 Vue 应用实例
import { createApp } from 'vue'

// 导入应用的根组件 App.vue
import App from './App.vue'

// 导入路由配置文件，用于管理页面路由
import router from '@/router'

// 导入 Element Plus UI 框架及其样式文件
import ElementPlus from 'element-plus'
// 导入 Element Plus 的全局样式文件，确保 Element Plus 组件在项目中正确地渲染和显示样式。如果不引入该样式文件，Element Plus 的组件将没有任何样式，看起来就像普通的 HTML 标签。例如，el-button 可能只显示一个无样式的按钮。
// 全局生效：这份 CSS 文件是全局作用的，不需要每个组件单独引入，确保所有使用 Element Plus 的组件都有统一的样式。
import 'element-plus/dist/index.css'
//代码的作用是引入 Axios 库，以便在项目中使用它来处理 HTTP 请求。
//axios可以用来向服务器发送请求或接收响应。它是一个非常流行的库，通常用于前端与后端的通信。
import axios from "axios";

axios.defaults.baseURL = "http://localhost:8080"

// 创建一个 Vue 应用实例，并将根组件传递给应用实例
const app = createApp(App)

// 注册路由插件，启用应用的路由功能
app.use(router)

// 注册 Element Plus 插件，启用 UI 组件库
app.use(ElementPlus)

// 将应用挂载到页面中 id 为 "app" 的 DOM 元素上
app.mount('#app')
