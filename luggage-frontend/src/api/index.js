import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const api = axios.create({
    baseURL: '/api',
    timeout: 10000
})

// 请求拦截器：添加 token
api.interceptors.request.use(
    config => {
        const userStore = useUserStore()
        if (userStore.token) {
            config.headers.Authorization = `Bearer ${userStore.token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器：统一处理错误
api.interceptors.response.use(
    response => {
        const res = response.data
        // 如果 code 不是 200，说明有错误
        if (res.code !== 200) {
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || '请求失败'))
        }
        return res
    },
    error => {
        if (error.response) {
            const status = error.response.status
            if (status === 401) {
                ElMessage.error('请先登录')
                const userStore = useUserStore()
                userStore.logout()
                window.location.href = '/login'
            } else if (status === 403) {
                ElMessage.error('权限不足')
            } else if (status === 404) {
                ElMessage.error('接口不存在')
            } else {
                ElMessage.error(error.response.data?.message || '服务器错误')
            }
        } else {
            ElMessage.error('网络连接失败')
        }
        return Promise.reject(error)
    }
)

export default api