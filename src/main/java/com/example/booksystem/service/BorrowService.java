package com.example.booksystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.booksystem.entity.Borrow;
import com.example.booksystem.vo.BorrowVO;

import java.util.List;
import java.util.Map;

/**
 * 借阅服务接口
 */
public interface BorrowService extends IService<Borrow> {

    /**
     * 借阅图书
     *
     * @param bookId 图书ID
     * @return 借阅结果
     */
    boolean borrowBook(Long bookId);

    /**
     * 归还图书
     *
     * @param borrowId 借阅ID
     * @return 归还结果
     */
    boolean returnBook(Long borrowId);

    /**
     * 续借图书
     *
     * @param borrowId 借阅ID
     * @return 续借结果
     */
    boolean renewBook(Long borrowId);

    /**
     * 分页查询当前用户的借阅记录
     *
     * @param page   分页参数
     * @param status 借阅状态
     * @return 分页结果
     */
    IPage<BorrowVO> pageMyBorrows(Page<Borrow> page, Integer status);

    /**
     * 分页查询所有借阅记录（管理员）
     *
     * @param page     分页参数
     * @param userId   用户ID
     * @param bookId   图书ID
     * @param username 用户名（模糊查询）
     * @param bookTitle 图书名称（模糊查询）
     * @param status   借阅状态
     * @return 分页结果
     */
    IPage<BorrowVO> pageAllBorrows(Page<Borrow> page, Long userId, Long bookId, String username, String bookTitle, Integer status);

    /**
     * 获取借阅详情
     *
     * @param id 借阅ID
     * @return 借阅详情
     */
    BorrowVO getBorrowDetail(Long id);

    /**
     * 缴纳罚款
     *
     * @param borrowId 借阅ID
     * @return 缴纳结果
     */
    boolean payFine(Long borrowId);

    /**
     * 获取借阅统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getBorrowStatistics();

    /**
     * 获取热门图书排行
     *
     * @param limit 限制数量
     * @return 热门图书列表
     */
    List<Map<String, Object>> getHotBooks(int limit);
}