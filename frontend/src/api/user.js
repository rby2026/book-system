import request from '@/utils/request'

// 用户登录
export function login(data) {
    return request({
        url: '/user/login',
        method: 'post',
        data
    })
}

// 用户注册
export function register(data) {
    return request({
        url: '/user/register',
        method: 'post',
        data
    })
}

// 获取当前用户信息
export function getUserInfo() {
    return request({
        url: '/user/info',
        method: 'get'
    })
}

// 更新用户信息
export function updateUserInfo(data) {
    return request({
        url: '/user/info',
        method: 'put',
        data
    })
}

// 修改密码
export function updatePassword(oldPassword, newPassword) {
    return request({
        url: '/user/password',
        method: 'put',
        params: {
            oldPassword,
            newPassword
        }
    })
}

// 更新用户类型
export function updateUserType(userType) {
    return request({
        url: '/user/userType',
        method: 'put',
        params: {
            userType
        }
    })
}

// 获取用户列表（管理员）
export function getUserList() {
    return request({
        url: '/user/list',
        method: 'get'
    })
}

// 获取用户详情（管理员）
export function getUserById(id) {
    return request({
        url: `/user/detail/${id}`,
        method: 'get'
    })
}

// 更新用户状态（管理员）
export function updateUserStatus(userId, status) {
    return request({
        url: '/user/status',
        method: 'put',
        params: {
            userId,
            status
        }
    })
}

// 创建用户
export function createUser(data) {
    return request({
        url: '/user',
        method: 'post',
        data
    })
}

// 更新用户
export function updateUser(data) {
    return request({
        url: '/user',
        method: 'put',
        data
    })
}

// 删除用户
export function deleteUser(id) {
    return request({
        url: `/user/detail/${id}`,
        method: 'delete'
    })
} 