import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'
import Layout from '../components/Layout.vue'

const routes = [
    // 公开路由（不使用 Layout）
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: { public: true }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue'),
        meta: { public: true }
    },

    // 需要登录的路由（使用 Layout）
    {
        path: '/',
        component: Layout,
        meta: { requiresAuth: true },
        children: [
            {
                path: '',
                name: 'UserHome',
                component: () => import('../views/UserHome.vue')
            },
            {
                path: 'orders',
                name: 'Orders',
                component: () => import('../views/Orders.vue')
            },
            {
                path: 'order/:id',
                name: 'OrderDetail',
                component: () => import('../views/OrderDetail.vue')
            },
            {
                path: 'pickup',
                name: 'Pickup',
                component: () => import('../views/Pickup.vue')
            }
        ]
    },

    // 管理员路由（使用 Layout，需要 admin 权限）
    {
        path: '/admin',
        component: Layout,
        meta: { requiresAuth: true, admin: true },
        children: [
            {
                path: '',
                name: 'AdminHome',
                component: () => import('../views/admin/AdminHome.vue')
            },
            {
                path: 'orders',
                name: 'OrderManage',
                component: () => import('../views/admin/OrderManage.vue')
            },
            {
                path: 'price-rules',
                name: 'PriceRule',
                component: () => import('../views/admin/PriceRule.vue')
            },
            {
                path: 'statistics',
                name: 'Statistics',
                component: () => import('../views/admin/Statistics.vue')
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    userStore.restore()

    // 公开路由
    if (to.meta.public) {
        next()
        return
    }

    // 需要登录
    if (to.meta.requiresAuth) {
        if (!userStore.isLogin) {
            next('/login')
            return
        }

        // 管理员权限检查
        if (to.meta.admin && !userStore.isAdmin) {
            next('/')
            return
        }
    }

    next()
})

export default router