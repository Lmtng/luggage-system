import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: null,
    username: '',
    nickname: '',
    role: ''
  }),

  getters: {
    isLogin: state => !!state.userId,
    isAdmin: state => state.role === 'ADMIN'
  },

  actions: {
    // 登录成功后保存用户信息
    login(userData) {
      this.userId = userData.id ?? userData.userId
      this.username = userData.username ?? ''
      this.nickname = userData.nickname ?? ''
      this.role = userData.role ?? ''

      this.saveToLocalStorage()
    },

    // 更新当前用户信息
    setUser(userData) {
      this.userId = userData.id ?? userData.userId
      this.username = userData.username ?? ''
      this.nickname = userData.nickname ?? ''
      this.role = userData.role ?? ''

      this.saveToLocalStorage()
    },

    // 保存到浏览器
    saveToLocalStorage() {
      localStorage.setItem('user', JSON.stringify({
        userId: this.userId,
        username: this.username,
        nickname: this.nickname,
        role: this.role
      }))
    },

    // 退出登录
    logout() {
      this.userId = null
      this.username = ''
      this.nickname = ''
      this.role = ''

      localStorage.removeItem('user')
    },

    // 刷新页面后恢复用户信息
    restore() {
      const data = localStorage.getItem('user')

      if (!data) {
        return false
      }

      try {
        const user = JSON.parse(data)

        this.userId = user.userId ?? null
        this.username = user.username ?? ''
        this.nickname = user.nickname ?? ''
        this.role = user.role ?? ''

        return !!this.userId
      } catch (error) {
        console.error('恢复用户信息失败：', error)
        this.logout()
        return false
      }
    }
  }
})