import request from '@/utils/request'

// 分页查询公告
export function getAnnouncementList(params) {
    return request({
        url: '/announcement/page',
        method: 'get',
        params
    })
}

// 添加公告
export function addAnnouncement(data) {
    return request({
        url: '/announcement',
        method: 'post',
        data
    })
}

// 更新公告
export function updateAnnouncement(data) {
    return request({
        url: '/announcement',
        method: 'put',
        data
    })
}

// 删除公告
export function deleteAnnouncement(id) {
    return request({
        url: `/announcement/${id}`,
        method: 'delete'
    })
}

// 获取公告详情
export function getAnnouncementDetail(id) {
    return request({
        url: `/announcement/${id}`,
        method: 'get'
    })
}
