import request from '@/utils/request'

export function doCheckIn() {
    return request({
        url: '/check-in/do',
        method: 'post'
    })
}

export function getTodayCheckIn() {
    return request({
        url: '/check-in/today',
        method: 'get'
    })
}

export function getCheckInCount() {
    return request({
        url: '/check-in/count',
        method: 'get'
    })
}
