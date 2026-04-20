package com.example.booksystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.booksystem.entity.Book;

import java.util.List;

/**
 * 图书服务接口
 */
public interface BookService extends IService<Book> {

    /**
     * 分页查询图书
     *
     * @param page   分页参数
     * @param title  书名
     * @param author 作者
     * @param isbn   ISBN
     * @return 分页结果
     */
    IPage<Book> pageBooks(Page<Book> page, String title, String author, String isbn);

    /**
     * 添加图书
     *
     * @param book 图书信息
     * @return 添加结果
     */
    boolean addBook(Book book);

    /**
     * 更新图书
     *
     * @param book 图书信息
     * @return 更新结果
     */
    boolean updateBook(Book book);

    /**
     * 删除图书
     *
     * @param id 图书ID
     * @return 删除结果
     */
    boolean deleteBook(Long id);

    /**
     * 获取图书详情
     *
     * @param id 图书ID
     * @return 图书详情
     */
    Book getBookDetail(Long id);

    /**
     * 根据ISBN查询图书
     *
     * @param isbn ISBN
     * @return 图书信息
     */
    Book getBookByIsbn(String isbn);

    /**
     * 智能推荐图书
     *
     * @param userId 用户ID
     * @param limit  推荐数量
     * @return 推荐图书列表
     */
    List<Book> recommendBooks(Long userId, int limit);

    /**
     * 获取热门图书
     *
     * @param limit 数量限制
     * @return 热门图书列表
     */
    List<Book> getHotBooks(int limit);

    /**
     * 获取新书
     *
     * @param limit 数量限制
     * @return 新书列表
     */
    List<Book> getNewBooks(int limit);
}
