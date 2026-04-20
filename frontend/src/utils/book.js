import axios from 'axios'
import request from '@/utils/request'

/**
 * 通过ISBN获取图书信息
 * 使用后端代理的ISBN查询API，避免CORS问题
 * @param {string} isbn ISBN码
 * @returns {Promise} 图书信息
 */
export async function getBookInfoByIsbn(isbn) {
    try {
        // 使用后端代理的ISBN查询API
        const res = await request({
            url: '/api/isbn/query',
            method: 'get',
            params: {
                isbn: isbn
            }
        });

        // 检查API返回是否成功
        console.log(res)
        if (res.data.error_code !== 0) {
            console.error('获取图书信息失败:', res.message || (res.data && res.data.reason));
            return null;
        }

        const bookData = res.data.result.data;
        console.log(bookData)
        // 从关键词中提取可能的分类
        const category = mapBookCategoryFromKeyword(bookData.keyword);

        return {
            title: bookData.title,
            author: bookData.author,
            publisher: bookData.publisher,
            publishDate: bookData.pubDate,
            isbn: bookData.isbn,
            description: bookData.gist,
            cover: bookData.img,
            price: bookData.price,
            category: category
        };
    } catch (error) {
        console.error('获取图书信息失败:', error);
        return null;
    }
}

/**
 * 从关键词中映射图书分类
 * @param {string} keyword 关键词字符串
 * @returns {string} 图书分类代码
 */
function mapBookCategoryFromKeyword(keyword) {
    if (!keyword) return 'OTHER';

    // 关键词可能是以|分隔的字符串
    const keywords = keyword.split('|');

    // 确保与前端分类一致的映射
    const categoryMap = {
        '文学': 'LITERATURE',
        '小说': 'FICTION',  // 注意：这里映射到FICTION而不是LITERATURE
        '科学': 'SCIENCE',
        '历史': 'HISTORY',
        '哲学': 'PHILOSOPHY',
        '艺术': 'ART',
        '科技': 'TECHNOLOGY',
        '计算机': 'TECHNOLOGY',
        '经济': 'ECONOMICS',
        '管理': 'ECONOMICS',
        '教育': 'EDUCATION',
        '医学': 'MEDICINE',
        '法律': 'LAW',
        '政治': 'POLITICS',
        '军事': 'MILITARY',
        '体育': 'SPORTS',
        '娱乐': 'ENTERTAINMENT',
        '传记': 'BIOGRAPHY',
        '儿童': 'CHILDREN',
        '漫画': 'COMICS',
        '烹饪': 'COOKING',
        '旅游': 'TRAVEL',
        '宗教': 'RELIGION',
        '参考': 'REFERENCE'
    };

    // 遍历所有关键词，查找匹配的分类
    for (const key of keywords) {
        for (const [categoryKey, categoryValue] of Object.entries(categoryMap)) {
            if (key.includes(categoryKey)) {
                return categoryValue;
            }
        }
    }

    return 'OTHER';
}

/**
 * 验证ISBN
 * @param {string} isbn ISBN码
 * @returns {boolean} 是否有效
 */
export function validateIsbn(isbn) {
    // 移除所有非数字字符
    isbn = isbn.replace(/[^0-9X]/gi, '')

    // ISBN-10
    if (isbn.length === 10) {
        let sum = 0
        for (let i = 0; i < 9; i++) {
            sum += (10 - i) * parseInt(isbn[i])
        }
        let checksum = (11 - (sum % 11)) % 11
        let lastChar = isbn[9].toUpperCase()
        return (checksum === 10 ? 'X' : checksum.toString()) === lastChar
    }

    // ISBN-13
    if (isbn.length === 13) {
        let sum = 0
        for (let i = 0; i < 12; i++) {
            sum += (i % 2 === 0 ? 1 : 3) * parseInt(isbn[i])
        }
        let checksum = (10 - (sum % 10)) % 10
        return checksum.toString() === isbn[12]
    }

    return false
} 