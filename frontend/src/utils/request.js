import axios from 'axios'
import { message } from 'ant-design-vue'

// 创建axios实例
const service = axios.create({
    baseURL: '',
    timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 从localStorage中获取token
        const token = localStorage.getItem('book-system-user') ? JSON.parse(localStorage.getItem('book-system-user')).token : ''
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }

        // 添加详细日志
        console.log(`[请求] ${config.method.toUpperCase()} ${config.url}`, config.params || config.data || {})

        return config
    },
    error => {
        console.error('请求错误：', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data

        // 添加详细日志
        console.log(`[响应] ${response.config.method.toUpperCase()} ${response.config.url}`, res)

        // 如果返回的状态码不是200，说明接口请求有误
        if (res.code !== 200) {
            // 不要在这里显示错误消息，让调用方处理
            return Promise.reject(res)
        }
        return res
    },
    error => {
        console.error('响应错误：', error)
        let errorMessage = '网络错误，请稍后重试'

        if (error.response) {
            const { data, status, config } = error.response

            // 添加详细错误日志
            console.error(`[错误响应] ${config.method.toUpperCase()} ${config.url}`, {
                status,
                data,
                message: error.message
            })

            // 优先使用后端返回的错误信息
            if (data && data.message) {
                // 处理Spring Security的Bad credentials错误
                if (data.message.includes('Bad credentials')) {
                    errorMessage = '用户名或密码错误'
                } else {
                    errorMessage = data.message
                }
            } else {
                switch (status) {
                    case 401:
                        errorMessage = '登录已过期，请重新登录'
                        // 清除localStorage中的用户信息
                        localStorage.removeItem('book-system-user')
                        window.location.href = '/login'
                        break
                    case 403:
                        errorMessage = '没有权限访问'
                        break
                    case 404:
                        errorMessage = '请求的资源不存在'
                        break
                    case 500:
                        // 处理Spring Security的Bad credentials错误
                        if (error.response.data && error.response.data.message && error.response.data.message.includes('Bad credentials')) {
                            errorMessage = '用户名或密码错误'
                        } else {
                            errorMessage = '服务器错误'
                        }
                        break
                    default:
                        errorMessage = `请求失败(${status})`
                }
            }
        } else if (error.request) {
            errorMessage = '服务器无响应'
            console.error('[请求无响应]', error.request)
        }

        // 显示错误消息
        message.error(errorMessage)
        return Promise.reject({
            code: error.response?.status || 500,
            message: errorMessage
        })
    }
)

export default service 