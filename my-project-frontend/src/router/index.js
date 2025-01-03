// •	作用：路由配置文件，管理应用的页面导航。
// •	功能：
// •	定义 URL 路径与组件的映射关系。
// •	实现页面跳转和嵌套路由。
// •	管理路由守卫（导航权限）。



// 1. 引入 Vue Router 中的 createRouter 和 createWebHistory 方法
// createRouter: 用于创建一个路由实例
// createWebHistory: 用于启用 HTML5 的历史模式（不带 "#" 的路径）
import { createRouter, createWebHistory } from 'vue-router';

// 2. 定义路由
// 通过 createRouter 方法创建路由实例
const router = createRouter({
    // 配置历史记录模式，这里使用 createWebHistory 方法，传入 BASE_URL 作为基础路径
    history: createWebHistory(import.meta.env.BASE_URL),

    // 定义路由表
    routes: [
        {
            // 定义路由路径，当访问 "/" 时会匹配此路由（localhost5137，后面什么都不加）
            path: '/',

            // 路由的名字（可以用来标识路由，或者用于路由跳转）
            name: 'welcome',

            // 懒加载组件：只有当用户访问这个路由时，才会加载对应的组件
            component: () => import('@/views/WelcomeView.vue'),

            // 子路由：当前为空数组，表示没有子路由
            children: []
        }
    ]
});

// 3. 导出路由实例
// 这个 router 实例将会在 Vue 应用中使用
export default router;
