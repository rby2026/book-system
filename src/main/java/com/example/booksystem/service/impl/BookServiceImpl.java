package com.example.booksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.booksystem.entity.Book;
import com.example.booksystem.exception.BusinessException;
import com.example.booksystem.mapper.BookMapper;
import com.example.booksystem.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 图书服务实现类
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Override
    public IPage<Book> pageBooks(Page<Book> page, String title, String author, String isbn) {
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (StringUtils.hasText(title)) {
            queryWrapper.like(Book::getTitle, title);
        }
        if (StringUtils.hasText(author)) {
            queryWrapper.like(Book::getAuthor, author);
        }
        if (StringUtils.hasText(isbn)) {
            queryWrapper.like(Book::getIsbn, isbn);
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(Book::getCreateTime);

        return page(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addBook(Book book) {
        // 校验ISBN是否已存在
        Book existBook = getBookByIsbn(book.getIsbn());
        if (existBook != null) {
            throw new BusinessException("ISBN已存在");
        }

        // 设置初始值
        book.setStatus(1); // 默认可借阅
        book.setAvailableCount(book.getTotalCount()); // 可借数量等于总数量
        book.setCreateTime(new Date());
        book.setUpdateTime(new Date());

        return save(book);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBook(Book book) {
        // 校验图书是否存在
        Book existBook = getById(book.getId());
        if (existBook == null) {
            throw new BusinessException("图书不存在");
        }

        // 如果修改了ISBN，需要校验新的ISBN是否已存在
        if (!existBook.getIsbn().equals(book.getIsbn())) {
            Book bookWithSameIsbn = getBookByIsbn(book.getIsbn());
            if (bookWithSameIsbn != null) {
                throw new BusinessException("ISBN已存在");
            }
        }

        // 设置默认值
        if (book.getTotalCount() == null) {
            book.setTotalCount(existBook.getTotalCount());
        }

        // 计算可借数量
        Integer borrowedCount = existBook.getTotalCount() - existBook.getAvailableCount();
        Integer newAvailableCount = book.getTotalCount() - borrowedCount;
        if (newAvailableCount < 0) {
            throw new BusinessException("总数量不能小于已借出数量");
        }

        book.setAvailableCount(newAvailableCount);
        book.setUpdateTime(new Date());

        return updateById(book);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBook(Long id) {
        // 校验图书是否存在
        Book book = getById(id);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }

        // 校验图书是否有借出
        if (book.getTotalCount() > book.getAvailableCount()) {
            throw new BusinessException("图书已被借出，无法删除");
        }

        return removeById(id);
    }

    @Override
    public Book getBookDetail(Long id) {
        Book book = getById(id);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        return book;
    }

    @Override
    public Book getBookByIsbn(String isbn) {
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Book::getIsbn, isbn);
        return getOne(queryWrapper);
    }

    @Override
    public List<Book> recommendBooks(Long userId, int limit) {
        // 这里实现智能推荐算法
        // 简化版：推荐热门图书 + 新书
        List<Book> hotBooks = getHotBooks(limit / 2);
        List<Book> newBooks = getNewBooks(limit / 2);
        
        List<Book> recommendedBooks = new ArrayList<>();
        recommendedBooks.addAll(hotBooks);
        
        // 避免重复
        for (Book book : newBooks) {
            if (!recommendedBooks.contains(book) && recommendedBooks.size() < limit) {
                recommendedBooks.add(book);
            }
        }
        
        return recommendedBooks;
    }

    @Override
    public List<Book> getHotBooks(int limit) {
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Book::getBorrowCount)
                .last("LIMIT " + limit);
        return list(queryWrapper);
    }

    @Override
    public List<Book> getNewBooks(int limit) {
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Book::getStockDate)
                .last("LIMIT " + limit);
        return list(queryWrapper);
    }
}
