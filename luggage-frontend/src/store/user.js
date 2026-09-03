import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
    state: () => ({
        userId: null,
        username: '',
        nickname: '',
        role: '',
        token: ''
    }),

    getters: {
        isLogin: (state) => !!state.token,
        isAdmin: (state) => state.role === 'ADMIN'
    },

    actions: {
        // 登录
        login(userData) {
            this.userId = userData.userId
            this.username = userData.username
            this.role = userData.role
            this.token = userData.token

            // 保存到 localStorage
            localStorage.setItem('user', JSON.stringify({
                userId: this.userId,
                username: this.username,
                role: this.role,
                token: this.token
            }))
        },

        // 设置用户信息
        setUser(userData) {
            this.userId = userData.userId
            this.username = userData.username
            this.nickname = userData.nickname
            this.role = userData.role
        },

        // 登出
        logout() {
            this.userId = null
            this.username = ''
            this.nickname = ''
            this.role = ''
            this.token = ''
            localStorage.removeItem('user')
        },

        // 从 localStorage 恢复
        restore() {
            const data = localStorage.getItem('user')
            if (data) {
                const user = JSON.parse(data)
                this.userId = user.userId
                this.username = user.username
                this.role = user.role
                this.token = user.token
                return true
            }
            return false
        }
    }
})