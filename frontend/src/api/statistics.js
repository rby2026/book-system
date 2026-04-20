import request from '@/utils/request'

// 获取借阅趋势数据
export function getBorrowTrend(params) {
    return request({
        url: '/statistics/borrow-trend',
        method: 'get',
        params
    })
}

// 获取分类分析数据
export function getCategoryAnalysis() {
    return request({
        url: '/statistics/category-analysis',
        method: 'get'
    })
}

// 获取时段分析数据
export function getTimeSlotAnalysis() {
    return request({
        url: '/statistics/time-slot-analysis',
        method: 'get'
    })
}

// 获取采购建议
export function getPurchaseSuggestions() {
    return request({
        url: '/statistics/purchase-suggestions',
        method: 'get'
    })
}

// 获取藏书结构数据
export function getBookStructure() {
    return request({
        url: '/statistics/book-structure',
        method: 'get'
    })
}

// 获取图书排行榜
export function getBookRankings(params) {
    return request({
        url: '/statistics/book-rankings',
        method: 'get',
        params
    })
}

// 获取读者排行榜
export function getReaderRankings(params) {
    return request({
        url: '/statistics/reader-rankings',
        method: 'get',
        params
    })
}

// 导出借阅统计数据
export function exportBorrowStatistics(params) {
    return request({
        url: '/statistics/export',
        method: 'get',
        params,
        responseType: 'blob'
    })
}

// 获取仪表盘统计数据
export function getDashboardStatistics() {
    return request({
        url: '/statistics/dashboard',
        method: 'get'
    })
} 