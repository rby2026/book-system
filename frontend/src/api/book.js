import request from '@/utils/request'

// 分页查询图书
export function getBookList(params) {
    return request({
        url: '/api/books/page',
        method: 'get',
        params
    })
}

// 添加图书
export function addBook(data) {
    return request({
        url: '/api/books',
        method: 'post',
        data
    })
}

// 更新图书
export function updateBook(data) {
    return request({
        url: '/api/books',
        method: 'put',
        data
    })
}

// 删除图书
export function deleteBook(id) {
    return request({
        url: `/api/books/${id}`,
        method: 'delete'
    })
}

// 获取图书详情
export function getBookDetail(id) {
    return request({
        url: `/api/books/${id}`,
        method: 'get'
    })
}

// 根据ISBN查询图书
export function getBookByIsbn(isbn) {
    return request({
        url: `/api/books/isbn/${isbn}`,
        method: 'get'
    })
}

// 获取推荐图书
export function getRecommendedBooks() {
    return request({
        url: '/api/books/recommend',
        method: 'get'
    })
}

// 获取热门图书
export function getHotBooks() {
    return request({
        url: '/api/books/hot',
        method: 'get'
    })
}

// 获取新书
export function getNewBooks() {
    return request({
        url: '/api/books/new',
        method: 'get'
    })
} 