package com.example.booksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.booksystem.entity.Book;
import com.example.booksystem.entity.Borrow;
import com.example.booksystem.entity.BorrowRule;
import com.example.booksystem.entity.User;
import com.example.booksystem.exception.BusinessException;
import com.example.booksystem.mapper.BorrowMapper;
import com.example.booksystem.service.BookService;
import com.example.booksystem.service.BorrowRuleService;
import com.example.booksystem.service.BorrowService;
import com.example.booksystem.service.UserService;
import com.example.booksystem.vo.BorrowVO;
import com.example.booksystem.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 借阅服务实现类
 */
@Service
public class BorrowServiceImpl extends ServiceImpl<BorrowMapper, Borrow> implements BorrowService {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowRuleService borrowRuleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean borrowBook(Long bookId) {
        // 获取当前用户
        UserVO currentUser = userService.getCurrentUser();

        // 获取图书信息
        Book book = bookService.getById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }

        // 校验图书是否可借
        if (book.getStatus() == 0) {
            throw new BusinessException("图书不可借阅");
        }
        if (book.getAvailableCount() <= 0) {
            throw new BusinessException("图书已全部借出");
        }

        // 获取借阅规则
        BorrowRule rule = borrowRuleService.getCurrentRule();

        // 校验用户借阅数量是否超过限制
        LambdaQueryWrapper<Borrow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Borrow::getUserId, currentUser.getId());
        queryWrapper.in(Borrow::getStatus, 0, 2); // 借阅中或已逾期未还
        long borrowCount = count(queryWrapper);
        if (borrowCount >= rule.getMaxBorrowCount()) {
            throw new BusinessException("借阅数量已达上限");
        }

        // 校验是否已借阅该图书
        queryWrapper.clear();
        queryWrapper.eq(Borrow::getUserId, currentUser.getId());
        queryWrapper.eq(Borrow::getBookId, bookId);
        queryWrapper.in(Borrow::getStatus, 0, 2); // 借阅中或已逾期未还
        long count = count(queryWrapper);
        if (count > 0) {
            throw new BusinessException("已借阅该图书");
        }

        // 创建借阅记录
        Borrow borrow = new Borrow();
        borrow.setUserId(currentUser.getId());
        borrow.setBookId(bookId);
        borrow.setBorrowTime(new Date());

        // 计算应还时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, rule.getBorrowDays());
        borrow.setReturnTime(calendar.getTime());

        borrow.setRenewCount(0);
        borrow.setStatus(0); // 借阅中
        borrow.setFine(0.0);
        borrow.setFineStatus(0); // 无需罚款
        borrow.setCreateTime(new Date());
        borrow.setUpdateTime(new Date());

        // 更新图书可借数量
        book.setAvailableCount(book.getAvailableCount() - 1);
        bookService.updateById(book);

        return save(borrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnBook(Long borrowId) {
        // 获取借阅记录
        Borrow borrow = getById(borrowId);
        if (borrow == null) {
            throw new BusinessException("借阅记录不存在");
        }

        // 校验借阅状态
        if (borrow.getStatus() == 1 || borrow.getStatus() == 3) {
            throw new BusinessException("图书已归还");
        }

        // 获取当前用户
        UserVO currentUser = userService.getCurrentUser();

        // 校验是否为当前用户的借阅记录或管理员
        if (!borrow.getUserId().equals(currentUser.getId()) && currentUser.getRole() != 1) {
            throw new BusinessException("无权操作");
        }

        // 获取图书信息
        Book book = bookService.getById(borrow.getBookId());
        if (book == null) {
            throw new BusinessException("图书不存在");
        }

        // 获取借阅规则
        BorrowRule rule = borrowRuleService.getCurrentRule();

        // 设置实际归还时间
        Date now = new Date();
        borrow.setActualReturnTime(now);

        // 处理不同状态的归还
        if (borrow.getStatus() == 0) { // 借阅中
            // 判断是否逾期
            boolean isOverdue = now.after(borrow.getReturnTime());

            // 计算罚款
            if (isOverdue) {
                long overdueDays = TimeUnit.DAYS.convert(
                        now.getTime() - borrow.getReturnTime().getTime(),
                        TimeUnit.MILLISECONDS);
                if (overdueDays > 0) {
                    double fine = overdueDays * rule.getFinePerDay();
                    borrow.setFine(fine);
                    borrow.setFineStatus(1); // 未缴纳
                    borrow.setStatus(3); // 已逾期已还
                } else {
                    borrow.setStatus(1); // 已归还
                }
            } else {
                borrow.setStatus(1); // 已归还
            }
        } else if (borrow.getStatus() == 2) { // 已逾期未还
            // 保持罚款金额
            borrow.setStatus(3); // 已逾期已还
            // 如果没有设置罚款金额或状态，设置默认值
            if (borrow.getFine() == null || borrow.getFine() <= 0) {
                long overdueDays = TimeUnit.DAYS.convert(
                        now.getTime() - borrow.getReturnTime().getTime(),
                        TimeUnit.MILLISECONDS);
                double fine = Math.max(1, overdueDays) * rule.getFinePerDay();
                borrow.setFine(fine);
            }
            if (borrow.getFineStatus() == null || borrow.getFineStatus() == 0) {
                borrow.setFineStatus(1); // 未缴纳
            }
        }

        borrow.setUpdateTime(now);

        // 更新图书可借数量
        book.setAvailableCount(book.getAvailableCount() + 1);
        bookService.updateById(book);

        return updateById(borrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean renewBook(Long borrowId) {
        // 获取借阅记录
        Borrow borrow = getById(borrowId);
        if (borrow == null) {
            throw new BusinessException("借阅记录不存在");
        }

        // 校验借阅状态
        if (borrow.getStatus() != 0) {
            throw new BusinessException("只能续借借阅中的图书");
        }

        // 获取当前用户
        UserVO currentUser = userService.getCurrentUser();

        // 校验是否为当前用户的借阅记录
        if (!borrow.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("无权操作");
        }

        // 获取借阅规则
        BorrowRule rule = borrowRuleService.getCurrentRule();

        // 校验续借次数
        if (borrow.getRenewCount() >= rule.getMaxRenewCount()) {
            throw new BusinessException("续借次数已达上限");
        }

        // 校验是否已逾期
        Date now = new Date();
        if (now.after(borrow.getReturnTime())) {
            throw new BusinessException("已逾期，无法续借");
        }

        // 更新续借次数和应还时间
        borrow.setRenewCount(borrow.getRenewCount() + 1);

        // 计算新的应还时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(borrow.getReturnTime());
        calendar.add(Calendar.DAY_OF_MONTH, rule.getRenewDays());
        borrow.setReturnTime(calendar.getTime());

        borrow.setUpdateTime(now);

        return updateById(borrow);
    }

    @Override
    public IPage<BorrowVO> pageMyBorrows(Page<Borrow> page, Integer status) {
        // 获取当前用户
        UserVO currentUser = userService.getCurrentUser();

        // 查询借阅记录
        LambdaQueryWrapper<Borrow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Borrow::getUserId, currentUser.getId());
        if (status != null) {
            queryWrapper.eq(Borrow::getStatus, status);
        }
        queryWrapper.orderByDesc(Borrow::getCreateTime);

        IPage<Borrow> borrowPage = page(page, queryWrapper);

        // 转换为BorrowVO
        return convertToBorrowVOPage(borrowPage);
    }

    @Override
    public IPage<BorrowVO> pageAllBorrows(Page<Borrow> page, Long userId, Long bookId, String username, String bookTitle, Integer status) {
        // 获取当前用户
        UserVO currentUser = userService.getCurrentUser();

        // 校验是否为管理员
        if (currentUser.getRole() != 1) {
            throw new BusinessException("无权操作");
        }

        // 查询借阅记录
        LambdaQueryWrapper<Borrow> queryWrapper = new LambdaQueryWrapper<>();
        // 用户ID筛选
        if (userId != null) {
            queryWrapper.eq(Borrow::getUserId, userId);
        }
        // 用户名模糊搜索
        if (StringUtils.isNotBlank(username)) {
            // 先根据用户名查询用户ID列表
            LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
            userQuery.like(User::getUsername, username);
            List<User> users = userService.list(userQuery);
            if (!users.isEmpty()) {
                List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
                queryWrapper.in(Borrow::getUserId, userIds);
            } else {
                // 如果没有匹配的用户，返回空结果
                return new Page<>(page.getCurrent(), page.getSize(), 0);
            }
        }

        // 图书ID筛选
        if (bookId != null) {
            queryWrapper.eq(Borrow::getBookId, bookId);
        }
        // 图书名称模糊搜索
        if (StringUtils.isNotBlank(bookTitle)) {
            // 先根据图书名称查询图书ID列表
            LambdaQueryWrapper<Book> bookQuery = new LambdaQueryWrapper<>();
            bookQuery.like(Book::getTitle, bookTitle);
            List<Book> books = bookService.list(bookQuery);
            if (!books.isEmpty()) {
                List<Long> bookIds = books.stream().map(Book::getId).collect(Collectors.toList());
                queryWrapper.in(Borrow::getBookId, bookIds);
            } else {
                // 如果没有匹配的图书，返回空结果
                return new Page<>(page.getCurrent(), page.getSize(), 0);
            }
        }

        // 借阅状态筛选
        if (status != null) {
            queryWrapper.eq(Borrow::getStatus, status);
        }
        
        queryWrapper.orderByDesc(Borrow::getCreateTime);

        // 执行查询
        IPage<Borrow> borrowPage = page(page, queryWrapper);

        // 转换为BorrowVO
        return convertToBorrowVOPage(borrowPage);
    }

    @Override
    public BorrowVO getBorrowDetail(Long id) {
        // 获取借阅记录
        Borrow borrow = getById(id);
        if (borrow == null) {
            throw new BusinessException("借阅记录不存在");
        }

        // 获取当前用户
        UserVO currentUser = userService.getCurrentUser();

        // 校验是否为当前用户的借阅记录或管理员
        if (!borrow.getUserId().equals(currentUser.getId()) && currentUser.getRole() != 1) {
            throw new BusinessException("无权操作");
        }

        return convertToBorrowVO(borrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payFine(Long borrowId) {
        // 获取借阅记录
        Borrow borrow = getById(borrowId);
        if (borrow == null) {
            throw new BusinessException("借阅记录不存在");
        }

        // 校验是否需要缴纳罚款
        if (borrow.getFineStatus() == 0) {
            throw new BusinessException("无需缴纳罚款");
        }
        if (borrow.getFineStatus() == 2) {
            throw new BusinessException("罚款已缴纳");
        }

        // 获取当前用户
        UserVO currentUser = userService.getCurrentUser();

        // 校验是否为当前用户的借阅记录或管理员
        if (!borrow.getUserId().equals(currentUser.getId()) && currentUser.getRole() != 1) {
            throw new BusinessException("无权操作");
        }

        // 更新罚款状态
        borrow.setFineStatus(2); // 已缴纳
        borrow.setUpdateTime(new Date());

        return updateById(borrow);
    }

    @Override
    public Map<String, Object> getBorrowStatistics() {
        // 获取当前用户
        UserVO currentUser = userService.getCurrentUser();

//        // 校验是否为管理员
//        if (currentUser.getRole() != 1) {
//            throw new BusinessException("无权操作");
//        }

        Map<String, Object> result = new HashMap<>();

        // 总借阅次数
        LambdaQueryWrapper<Borrow> queryWrapper = new LambdaQueryWrapper<>();
        if (currentUser.getRole() != 1) {
            queryWrapper.eq(Borrow::getUserId, currentUser.getId());
        }
        long totalBorrowCount = count(queryWrapper);
        result.put("totalBorrowCount", totalBorrowCount);

        // 当前借阅中的数量
        queryWrapper.clear();
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Borrow::getStatus, 0, 2); // 借阅中或已逾期未还
        if (currentUser.getRole() != 1) {
            queryWrapper.eq(Borrow::getUserId, currentUser.getId());
        }
        long currentBorrowCount = count(queryWrapper);
        result.put("currentBorrowCount", currentBorrowCount);

        // 已逾期数量
        queryWrapper.clear();
        queryWrapper.eq(Borrow::getStatus, 2); // 已逾期未还
        if (currentUser.getRole() != 1) {
            queryWrapper.eq(Borrow::getUserId, currentUser.getId());
        }
        long overdueCount = count(queryWrapper);
        result.put("overdueCount", overdueCount);

        // 未缴纳罚款数量
        queryWrapper.clear();
        queryWrapper.eq(Borrow::getFineStatus, 1); // 未缴纳
        if (currentUser.getRole() != 1) {
            queryWrapper.eq(Borrow::getUserId, currentUser.getId());
        }
        long unpaidFineCount = count(queryWrapper);
        result.put("unpaidFineCount", unpaidFineCount);

        // 罚款总金额
        queryWrapper.clear();
        queryWrapper.eq(Borrow::getFineStatus, 1); // 未缴纳
        if (currentUser.getRole() != 1) {
            queryWrapper.eq(Borrow::getUserId, currentUser.getId());
        }
        List<Borrow> unpaidBorrows = list(queryWrapper);
        double totalFine = unpaidBorrows.stream().mapToDouble(Borrow::getFine).sum();
        result.put("totalFine", totalFine);

        // 用户总数
        long newUserCount = userService.count();
        result.put("newUserCount", newUserCount);

        // 图书总数
        long totalBookCount = bookService.count();
        result.put("totalBookCount", totalBookCount);

        return result;
    }

    @Override
    public List<Map<String, Object>> getHotBooks(int limit) {
        // 查询所有借阅记录
        List<Borrow> borrows = list();

        // 统计每本书的借阅次数
        Map<Long, Long> bookBorrowCountMap = borrows.stream()
                .collect(Collectors.groupingBy(Borrow::getBookId, Collectors.counting()));

        // 排序并取前N本
        List<Map.Entry<Long, Long>> sortedBooks = bookBorrowCountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());

        // 转换为结果格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : sortedBooks) {
            Book book = bookService.getById(entry.getKey());
            if (book != null) {
                Map<String, Object> bookMap = new HashMap<>();
                bookMap.put("bookId", book.getId());
                bookMap.put("title", book.getTitle());
                bookMap.put("author", book.getAuthor());
                bookMap.put("isbn", book.getIsbn());
                bookMap.put("borrowCount", entry.getValue());
                result.add(bookMap);
            }
        }

        return result;
    }

    /**
     * 转换为BorrowVO
     */
    private BorrowVO convertToBorrowVO(Borrow borrow) {
        if (borrow == null) {
            return null;
        }

        BorrowVO borrowVO = new BorrowVO();
        BeanUtils.copyProperties(borrow, borrowVO);

        // 设置用户信息
        User user = userService.getById(borrow.getUserId());
        if (user != null) {
            borrowVO.setUsername(user.getUsername());
            borrowVO.setRealName(user.getRealName());
        }

        // 设置图书信息
        Book book = bookService.getById(borrow.getBookId());
        if (book != null) {
            borrowVO.setBookTitle(book.getTitle());
            borrowVO.setBookAuthor(book.getAuthor());
            borrowVO.setBookIsbn(book.getIsbn());
        }

        return borrowVO;
    }

    /**
     * 转换为BorrowVO分页
     */
    private IPage<BorrowVO> convertToBorrowVOPage(IPage<Borrow> borrowPage) {
        IPage<BorrowVO> borrowVOPage = new Page<>(borrowPage.getCurrent(), borrowPage.getSize(), borrowPage.getTotal());

        List<BorrowVO> borrowVOList = borrowPage.getRecords().stream()
                .map(this::convertToBorrowVO)
                .collect(Collectors.toList());

        borrowVOPage.setRecords(borrowVOList);

        return borrowVOPage;
    }
}