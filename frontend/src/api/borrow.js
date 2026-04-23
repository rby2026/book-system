import request from '@/utils/request'

// 借阅图书
export function borrowBook(bookId) {
    return request({
        url: `/borrow/${bookId}`,
        method: 'post'
    })
}

// 归还图书
export function returnBook(borrowId) {
    return request({
        url: `/borrow/return/${borrowId}`,
        method: 'put'
    })
}

// 续借图书
export function renewBook(borrowId) {
    return request({
        url: `/borrow/renew/${borrowId}`,
        method: 'put'
    })
}

// 分页查询当前用户的借阅记录
export function getMyBorrowList(params) {
    return request({
        url: '/borrow/my/page',
        method: 'get',
        params
    })
}

// 分页查询所有借阅记录（管理员）
export function getAllBorrowList(params) {
    return request({
        url: '/borrow/page',
        method: 'get',
        params
    })
}

// 获取借阅详情
export function getBorrowDetail(id) {
    return request({
        url: `/borrow/${id}`,
        method: 'get'
    })
}

// 缴纳罚款
export function payFine(borrowId) {
    return request({
        url: `/borrow/pay/${borrowId}`,
        method: 'put'
    })
}

// 获取借阅统计数据（管理员）
export function getBorrowStatistics() {
    return request({
        url: '/borrow/statistics',
        method: 'get'
    })
}

// 获取热门图书排行
export function getHotBooks(limit = 10) {
    return request({
        url: '/borrow/hot',
        method: 'get',
        params: { limit }
    })
}

// 按图书ID查询借阅记录
export function getBorrowListByBookId(bookId, params = {}) {
    return request({
        url: `/borrow/book/${bookId}`,
        method: 'get',
        params: {
            current: 1,
            size: 10,
            ...params
        }
    })
} 