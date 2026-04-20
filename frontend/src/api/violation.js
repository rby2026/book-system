import request from '@/utils/request'

// 分页查询违章信息
export function getViolationList(params) {
    return request({
        url: '/violation/page',
        method: 'get',
        params
    })
}

// 添加违章信息
export function addViolation(data) {
    return request({
        url: '/violation',
        method: 'post',
        data
    })
}

// 更新违章信息
export function updateViolation(data) {
    return request({
        url: '/violation',
        method: 'put',
        data
    })
}

// 删除违章信息
export function deleteViolation(id) {
    return request({
        url: `/violation/${id}`,
        method: 'delete'
    })
}

// 获取违章信息详情
export function getViolationDetail(id) {
    return request({
        url: `/violation/${id}`,
        method: 'get'
    })
}
