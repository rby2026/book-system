/**
 * 下载文件
 * @param {*} data 文件数据
 * @param {string} filename 文件名
 */
export function downloadFile(data, filename) {
    const blob = new Blob([data])
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(link.href)
}

/**
 * 获取文件名
 * @param {string} headers 响应头
 * @returns {string} 文件名
 */
export function getFileName(headers) {
    const contentDisposition = headers['content-disposition']
    if (!contentDisposition) return ''

    const matches = contentDisposition.match(/filename=(.+)/)
    if (matches && matches.length > 1) {
        return decodeURIComponent(matches[1])
    }
    return ''
} 