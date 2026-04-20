import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { message } from 'ant-design-vue'

// 布局组件
import MainLayout from '@/layouts/MainLayout.vue'

// 路由配置
const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: { title: '登录', requiresAuth: false }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/register/index.vue'),
        meta: { requiresAuth: false }
    },
    {
        title: '扫码页面',
        name: 'ScanCodePage',
        path: '/ScanCodePage',
        component: () => import('@/views/ScanCodePage.vue')
    },
    {
        path: '/',
        component: MainLayout,
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/dashboard/index.vue'),
                meta: { title: '首页', requiresAuth: true }
            },
            {
                path: 'books',
                name: 'Books',
                component: () => import('@/views/books/index.vue'),
                meta: { title: '图书管理', requiresAuth: true }
            },
            // {
            //     path: 'book/detail/:id',
            //     name: 'BookDetail',
            //     component: () => import('@/views/books/detail.vue'),
            //     meta: { requiresAuth: true, title: '图书详情' }
            // },
            {
                path: 'borrows',
                name: 'Borrows',
                component: () => import('@/views/borrows/index.vue'),
                meta: { title: '借阅管理', requiresAuth: true }
            },
            // {
            //     path: 'borrow/detail/:id',
            //     name: 'BorrowDetail',
            //     component: () => import('@/views/borrows/detail.vue'),
            //     meta: { requiresAuth: true, title: '借阅详情' }
            // },
            {
                path: 'rules',
                name: 'Rules',
                component: () => import('@/views/rules/index.vue'),
                meta: { title: '规则管理', requiresAuth: true, requiresAdmin: true }
            },
            {
                path: 'users',
                name: 'Users',
                component: () => import('@/views/users/index.vue'),
                meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/profile/index.vue'),
                meta: { title: '个人中心', requiresAuth: true }
            },
            {
                path: 'statistics',
                name: 'Statistics',
                component: () => import('@/views/statistics/index.vue'),
                meta: { requiresAuth: true, title: '统计分析', requiresAdmin: true }
            },
            {
                path: 'logs',
                name: 'logs',
                component: () => import('@/views/logs/index.vue'),
                meta: { requiresAuth: true, title: '日志管理', requiresAdmin: true }
            },
            {
                path: 'announcements',
                name: 'Announcements',
                component: () => import('@/views/announcements/index.vue'),
                meta: { title: '公告管理', requiresAuth: true, requiresAdmin: true }
            },
            {
                path: 'violations',
                name: 'Violations',
                component: () => import('@/views/violations/index.vue'),
                meta: { title: '违章管理', requiresAuth: true, requiresAdmin: true }
            }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/error/404.vue'),
        meta: { title: '404 Not Found', requiresAuth: false }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - 图书管理系统` : '图书管理系统'

    const userStore = useUserStore()

    // 检查是否需要登录
    if (to.meta.requiresAuth) {
        if (!userStore.isLogin) {
            message.warning('请先登录')
            next({ name: 'Login', query: { redirect: to.fullPath } })
            return
        }

        // 检查是否需要管理员权限
        if (to.meta.requiresAdmin && !userStore.isAdmin) {
            message.error('您没有权限访问该页面')
            next({ name: 'Dashboard' })
            return
        }
    } else if (to.path === '/login' && userStore.isLogin) {
        // 已登录用户访问登录页，重定向到首页
        next({ name: 'Dashboard' })
        return
    }

    next()
})

export default router 