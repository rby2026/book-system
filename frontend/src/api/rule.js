import request from '@/utils/request'

// 获取当前生效的借阅规则
export function getCurrentRule() {
    return request({
        url: '/rule/current',
        method: 'get'
    })
}

// 获取所有借阅规则
export function getRuleList() {
    return request({
        url: '/rule/list',
        method: 'get'
    })
}

// 添加借阅规则
export function addRule(data) {
    return request({
        url: '/rule',
        method: 'post',
        data
    })
}

// 更新借阅规则
export function updateRule(data) {
    return request({
        url: '/rule',
        method: 'put',
        data
    })
}

// 启用借阅规则
export function enableRule(id) {
    return request({
        url: `/rule/enable/${id}`,
        method: 'put'
    })
}

// 禁用借阅规则
export function disableRule(id) {
    return request({
        url: `/rule/disable/${id}`,
        method: 'put'
    })
} 