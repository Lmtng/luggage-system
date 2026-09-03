import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
    // 公开路由
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

    // 用户路由
    {
        path: '/',
        name: 'UserHome',
        component: () => import('../views/UserHome.vue'),
        meta: { requiresAuth: true, role: 'USER' }
    },
    {
        path: '/orders',
        name: 'Orders',
        component: () => import('../views/Orders.vue'),
        meta: { requiresAuth: true, role: 'USER' }
    },
    {
        path: '/order/:id',
        name: 'OrderDetail',
        component: () => import('../views/OrderDetail.vue'),
        meta: { requiresAuth: true, role: 'USER' }
    },
    {
        path: '/pickup',
        name: 'Pickup',
        component: () => import('../views/Pickup.vue'),
        meta: { requiresAuth: true, role: 'USER' }
    },

    // 管理员路由
    {
        path: '/admin',
        name: 'AdminHome',
        component: () => import('../views/admin/AdminHome.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
        path: '/admin/orders',
        name: 'OrderManage',
        component: () => import('../views/admin/OrderManage.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
        path: '/admin/price-rules',
        name: 'PriceRule',
        component: () => import('../views/admin/PriceRule.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
        path: '/admin/statistics',
        name: 'Statistics',
        component: () => import('../views/admin/Statistics.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()

    // 公开路由直接放行
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

        // 检查角色权限
        if (to.meta.role && userStore.role !== to.meta.role) {
            // 权限不足，跳转到对应首页
            if (userStore.role === 'ADMIN') {
                next('/admin')
            } else {
                next('/')
            }
            return
        }
    }

    next()
})

export default router