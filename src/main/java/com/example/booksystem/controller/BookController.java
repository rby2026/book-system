package com.example.booksystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.booksystem.common.Result;
import com.example.booksystem.entity.Book;
import com.example.booksystem.service.BookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 图书控制器
 */
@Api(tags = "图书管理")
@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @ApiOperation("分页查询图书")
    @GetMapping("/page")
    public Result<IPage<Book>> pageBooks(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("书名") @RequestParam(required = false) String title,
            @ApiParam("作者") @RequestParam(required = false) String author,
            @ApiParam("ISBN") @RequestParam(required = false) String isbn) {
        Page<Book> page = new Page<>(current, size);
        IPage<Book> bookPage = bookService.pageBooks(page, title, author, isbn);
        return Result.success(bookPage);
    }

    @ApiOperation("添加图书（管理员）")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> addBook(@RequestBody Book book) {
        boolean result = bookService.addBook(book);
        return Result.success(result, "添加成功");
    }

    @ApiOperation("更新图书（管理员）")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> updateBook(@RequestBody Book book) {
        boolean result = bookService.updateBook(book);
        return Result.success(result, "更新成功");
    }

    @ApiOperation("删除图书（管理员）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> deleteBook(@PathVariable Long id) {
        boolean result = bookService.deleteBook(id);
        return Result.success(result, "删除成功");
    }

    @ApiOperation("获取图书详情")
    @GetMapping("/{id}")
    public Result<Book> getBookDetail(@PathVariable Long id) {
        Book book = bookService.getBookDetail(id);
        return Result.success(book);
    }

    @ApiOperation("根据ISBN查询图书")
    @GetMapping("/isbn/{isbn}")
    public Result<Book> getBookByIsbn(@PathVariable String isbn) {
        Book book = bookService.getBookByIsbn(isbn);
        return Result.success(book);
    }

    @ApiOperation("智能推荐图书")
    @GetMapping("/recommend")
    public Result recommendBooks(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(bookService.recommendBooks(userId, limit));
    }

    @ApiOperation("获取热门图书")
    @GetMapping("/hot")
    public Result getHotBooks(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(bookService.getHotBooks(limit));
    }

    @ApiOperation("获取新书")
    @GetMapping("/new")
    public Result getNewBooks(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(bookService.getNewBooks(limit));
    }
} 