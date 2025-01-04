// • 作用：路由配置文件，管理应用的页面导航。
// • 功能：
// • 定义 URL 路径与组件的映射关系，实现页面跳转和嵌套路由。
// • 管理路由守卫（导航权限），在进入或离开页面前执行特定逻辑。
// • 配置懒加载，实现按需加载组件，优化性能。


// 1. 引入 Vue Router 中的 createRouter 和 createWebHistory 方法
// createRouter: 用于创建一个路由实例。
// createWebHistory: 启用 HTML5 历史模式，使 URL 显示为正常路径，不带 "#" 符号，路径更美观且利于 SEO。
import { createRouter, createWebHistory } from 'vue-router';
import {unauthorized} from "@/net/index.js";

// 2. 定义路由
// 通过 createRouter 方法创建路由实例
const router = createRouter({
    // 配置历史记录模式，使用 createWebHistory 实现前端路由，保持浏览器地址栏和页面路径一致。
    // import.meta.env.BASE_URL 是 Vite 提供的环境变量，表示应用的基本路径（默认为 '/'）。
    history: createWebHistory(import.meta.env.BASE_URL),

    // 定义路由表
    routes: [
        // 这个welcome是登录页面的底板，然后在底板上还有子页面，子页面覆盖在底板上，子页面就是children部分，
        // 也就是说如果当前在welcome页面，router又收到了一个请求，就会在children子路由里找匹配的前端页面
        {
            // 访问路径，访问 "/" 时匹配此路由，即应用根路径。
            path: '/',

            // 路由名称（可选），在编程式导航时可通过路由名称跳转，更加直观和易维护。
            name: 'welcome',

            // 懒加载组件：仅在访问该路径时加载 WelcomeView.vue 组件，减少初始加载时间。
            component: () => import('@/views/WelcomeView.vue'),

            // 嵌套路由：访问 "/" 时加载 WelcomeView.vue 内嵌的子组件。
            children: [
                {
                    // 子路由路径，表示在父路径下直接访问该页面（"/"）。
                    path: '',

                    // 子路由名称，可用于精确跳转到登录页面。
                    name: 'welcome-login',

                    // 懒加载子组件 LoginPage.vue，用户访问时动态加载，提升性能。
                    component: ()  => import('@/views/welcome/LoginPage.vue')
                }
            ]
        },
        {
            // 定义另一个访问路径 "/index"，用于访问主页或其他核心页面。
            path: '/index',

            // 路由名称，保持与路径一致，方便通过名称跳转。
            name: '/index',

            // 懒加载组件，访问 /index 时加载 IndexView.vue 页面。
            component: () => import('@/views/IndexView.vue')
        }
    ]
})

//导航守卫（beforeEach）函数，用于在每次路由切换前执行逻辑操作
// to：即将进入的路由对象。
// from：当前离开的路由对象。
// next：必须调用的回调函数，用于放行或重定向。
router.beforeEach((to , from  , next  ) => {
    // 检查用户是否未授权
    const isUnauthorized = unauthorized()

    // 如果目标路由名称以 'welcome-' 开头，且用户已授权,说明一个用户已经登录但是又要访问登录页面，我们直接导航到登录后的页面
    if(to.name.startsWith('welcome-') && !isUnauthorized) {
        // 跳转到 '/index' 页面
        next('/index')
    }
    // 如果目标路径以 '/index' 开头，且用户未授权，说明用户没有登录但是想访问登录后的页面
    else if(to.fullPath.startsWith('/index') && isUnauthorized) {
        // 跳转到根路径 '/'（通常是登录或欢迎页）
        next('/')
    }
    else {
        // 允许正常跳转到目标页面
        next()
    }
});


// 3. 导出路由实例
// 导出的 router 实例将会在 Vue 应用的入口文件中导入并挂载到 Vue 实例上，使整个应用具备路由导航功能。
export default router;
