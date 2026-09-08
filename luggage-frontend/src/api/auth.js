import api from './index'

export const authApi = {
    // 注册
    register(data) {
        return api.post('/auth/register', data)
    },

    // 登录
    login(data) {
        return api.post('/auth/login', data)
    },

    // 获取当前用户
    getMe() {
        return api.get('/auth/me')
    },

    // 退出登录，同时销毁后端会话
    logout() {
        return api.post('/auth/logout')
    }
}
