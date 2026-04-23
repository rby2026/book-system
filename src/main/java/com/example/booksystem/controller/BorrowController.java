package com.example.booksystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.booksystem.common.Result;
import com.example.booksystem.entity.Borrow;
import com.example.booksystem.service.BorrowService;
import com.example.booksystem.vo.BorrowVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 借阅控制器
 */
@Api(tags = "借阅管理")
@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @ApiOperation("借阅图书")
    @PostMapping("/{bookId}")
    public Result<Boolean> borrowBook(@PathVariable Long bookId) {
        boolean result = borrowService.borrowBook(bookId);
        return Result.success(result, "借阅成功");
    }

    @ApiOperation("归还图书")
    @PutMapping("/return/{borrowId}")
    public Result<Boolean> returnBook(@PathVariable Long borrowId) {
        boolean result = borrowService.returnBook(borrowId);
        return Result.success(result, "归还成功");
    }

    @ApiOperation("续借图书")
    @PutMapping("/renew/{borrowId}")
    public Result<Boolean> renewBook(@PathVariable Long borrowId) {
        boolean result = borrowService.renewBook(borrowId);
        return Result.success(result, "续借成功");
    }

    @ApiOperation("分页查询当前用户的借阅记录")
    @GetMapping("/my/page")
    public Result<IPage<BorrowVO>> pageMyBorrows(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("借阅状态") @RequestParam(required = false) Integer status) {
        Page<Borrow> page = new Page<>(current, size);
        IPage<BorrowVO> borrowPage = borrowService.pageMyBorrows(page, status);
        return Result.success(borrowPage);
    }

    @ApiOperation("分页查询所有借阅记录（管理员）")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<BorrowVO>> pageAllBorrows(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("用户ID") @RequestParam(required = false) Long userId,
            @ApiParam("图书ID") @RequestParam(required = false) Long bookId,
            @ApiParam("用户名") @RequestParam(required = false) String username,
            @ApiParam("图书名称") @RequestParam(required = false) String bookTitle,
            @ApiParam("借阅状态") @RequestParam(required = false) Integer status) {
        Page<Borrow> page = new Page<>(current, size);
        IPage<BorrowVO> borrowPage = borrowService.pageAllBorrows(page, userId, bookId, username, bookTitle, status);
        return Result.success(borrowPage);
    }

    @ApiOperation("获取借阅详情")
    @GetMapping("/{id}")
    public Result<BorrowVO> getBorrowDetail(@PathVariable Long id) {
        BorrowVO borrowVO = borrowService.getBorrowDetail(id);
        return Result.success(borrowVO);
    }

    @ApiOperation("缴纳罚款")
    @PutMapping("/pay/{borrowId}")
    public Result<Boolean> payFine(@PathVariable Long borrowId) {
        boolean result = borrowService.payFine(borrowId);
        return Result.success(result, "缴纳成功");
    }

    @ApiOperation("获取借阅统计数据（管理员）")
    @GetMapping("/statistics")
//    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getBorrowStatistics() {
        Map<String, Object> statistics = borrowService.getBorrowStatistics();
        return Result.success(statistics);
    }

    @ApiOperation("获取热门图书排行")
    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> getHotBooks(
            @ApiParam("限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> hotBooks = borrowService.getHotBooks(limit);
        return Result.success(hotBooks);
    }

    @ApiOperation("按图书ID查询借阅记录")
    @GetMapping("/book/{bookId}")
    public Result<IPage<BorrowVO>> getBorrowsByBookId(
            @ApiParam("图书ID") @PathVariable Long bookId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size) {
        Page<Borrow> page = new Page<>(current, size);
        IPage<BorrowVO> borrowPage = borrowService.pageAllBorrows(page, null, bookId, null, null, null);
        return Result.success(borrowPage);
    }
}