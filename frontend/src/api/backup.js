import request from '@/utils/request'

// 获取备份列表
export function getBackupList(params) {
    return request({
        url: '/backup/list',
        method: 'get',
        params
    })
}

// 创建备份
export function createBackup() {
    return request({
        url: '/backup/create',
        method: 'post'
    })
}

// 恢复数据
export function restoreBackup(backupId) {
    return request({
        url: `/backup/restore/${backupId}`,
        method: 'post'
    })
}

// 删除备份
export function deleteBackup(backupId) {
    return request({
        url: `/backup/${backupId}`,
        method: 'delete'
    })
}

// 下载备份文件
export function downloadBackup(backupId) {
    return request({
        url: `/backup/download/${backupId}`,
        method: 'get',
        responseType: 'blob'
    })
}

// 上传备份文件
export function uploadBackup(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/backup/upload',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 获取备份配置
export function getBackupConfig() {
    return request({
        url: '/backup/config',
        method: 'get'
    })
}

// 更新备份配置
export function updateBackupConfig(data) {
    return request({
        url: '/backup/config',
        method: 'put',
        data
    })
} 