import { defineStore } from 'pinia'
import { login, register, getUserInfo } from '@/api/user'
import { message } from 'ant-design-vue'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: '',
        userInfo: {}
    }),
    getters: {
        isLogin: (state) => !!state.token,
        isAdmin: (state) => state.userInfo.role === 1
    },
    actions: {
        // 登录
        async login(loginData) {
            try {
                const res = await login(loginData)
                this.token = res.data.token
                this.userInfo = res.data.userInfo
                message.success('登录成功')
                return true
            } catch (error) {
                // 显示后端返回的错误信息
                message.error(error.message || '登录失败，请稍后重试')
                return false
            }
        },

        // 注册
        async register(registerData) {
            try {
                const res = await register(registerData)
                message.success('注册成功')
                return true
            } catch (error) {
                message.error(error.message || '注册失败，请稍后重试')
                return false
            }
        },

        // 获取用户信息
        async getUserInfo() {
            try {
                const res = await getUserInfo()
                this.userInfo = res.data
                return true
            } catch (error) {
                return false
            }
        },

        // 退出登录
        logout() {
            this.token = ''
            this.userInfo = {}
            message.success('退出登录成功')
        }
    },
    persist: {
        key: 'book-system-user',
        storage: localStorage,
        paths: ['token', 'userInfo']
    }
}) 