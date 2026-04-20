import request from '@/utils/request'

// 分页查询日志
export function getLogList(params) {
    return request({
        url: '/log/page',
        method: 'get',
        params
    })
}

// 删除单条日志
export function deleteLog(id) {
    return request({
        url: `/log/${id}`,
        method: 'delete'
    })
}

// 批量删除日志
export function batchDeleteLogs(ids) {
    return request({
        url: '/log/batch',
        method: 'delete',
        data: ids
    })
}

// 获取操作日志列表
export function getOperationLogs(params) {
    return request({
        url: '/logs/operation',
        method: 'get',
        params
    })
}

// 获取系统日志列表
export function getSystemLogs(params) {
    return request({
        url: '/logs/system',
        method: 'get',
        params
    })
}

// 导出操作日志
export function exportOperationLogs(params) {
    return request({
        url: '/logs/operation/export',
        method: 'get',
        params,
        responseType: 'blob'
    })
}

// 导出系统日志
export function exportSystemLogs(params) {
    return request({
        url: '/logs/system/export',
        method: 'get',
        params,
        responseType: 'blob'
    })
}

// 清理日志
export function cleanLogs(params) {
    return request({
        url: '/logs/clean',
        method: 'post',
        data: params
    })
} 