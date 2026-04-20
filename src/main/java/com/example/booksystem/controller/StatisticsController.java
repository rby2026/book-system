package com.example.booksystem.controller;

import com.example.booksystem.common.Result;
import com.example.booksystem.entity.Book;
import com.example.booksystem.entity.Borrow;
import com.example.booksystem.entity.User;
import com.example.booksystem.service.BookService;
import com.example.booksystem.service.BorrowService;
import com.example.booksystem.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

/**
 * 统计分析控制器
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    /**
     * 获取借阅趋势数据
     */
    @GetMapping("/borrow-trend")
    public Result getBorrowTrend(
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        // 根据时间范围确定开始和结束日期
        if (timeRange != null) {
            LocalDate now = LocalDate.now();
            if ("week".equals(timeRange)) {
                startDate = now.minusWeeks(1);
                endDate = now;
            } else if ("month".equals(timeRange)) {
                startDate = now.minusMonths(1);
                endDate = now;
            } else if ("year".equals(timeRange)) {
                startDate = now.minusYears(1);
                endDate = now;
            }
        }

        if (startDate == null || endDate == null) {
            startDate = LocalDate.now().minusMonths(1);
            endDate = LocalDate.now();
        }

        // 获取日期范围内的所有日期
        List<String> dates = new ArrayList<String>();
        List<Integer> counts = new ArrayList<Integer>();

        LocalDate date = startDate;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        while (!date.isAfter(endDate)) {
            String dateStr = formatter.format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            dates.add(dateStr);

            // 查询当天的借阅数量
            Date dayStart = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dayEnd = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

            LambdaQueryWrapper<Borrow> queryWrapper = new LambdaQueryWrapper<Borrow>();
            queryWrapper.between(Borrow::getBorrowTime, dayStart, dayEnd);
            long countLong = borrowService.count(queryWrapper);
            int count = (int) countLong;

            counts.add(count);
            date = date.plusDays(1);
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("dates", dates);
        result.put("counts", counts);

        return Result.success(result);
    }

    /**
     * 获取分类分析数据
     */
    @GetMapping("/category-analysis")
    public Result getCategoryAnalysis() {
        // 获取所有图书
        List<Book> books = bookService.list();

        // 按分类统计
        Map<String, Long> categoryCount = new HashMap<String, Long>();
        for (Book book : books) {
            String category = book.getCategory();
            if (categoryCount.containsKey(category)) {
                categoryCount.put(category, categoryCount.get(category) + 1);
            } else {
                categoryCount.put(category, 1L);
            }
        }

        List<String> categories = new ArrayList<String>(categoryCount.keySet());
        List<Integer> values = new ArrayList<Integer>();
        for (String category : categories) {
            values.add(categoryCount.get(category).intValue());
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("categories", categories);
        result.put("values", values);

        return Result.success(result);
    }

    /**
     * 获取时段分析数据
     */
    @GetMapping("/time-slot-analysis")
    public Result getTimeSlotAnalysis() {
        // 获取所有借阅记录
        List<Borrow> borrows = borrowService.list();

        // 按小时统计
        Map<Integer, Integer> hourCount = new HashMap<Integer, Integer>();
        for (int i = 0; i < 24; i++) {
            hourCount.put(i, 0);
        }

        SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
        for (Borrow borrow : borrows) {
            if (borrow.getBorrowTime() != null) {
                int hour = Integer.parseInt(hourFormat.format(borrow.getBorrowTime()));
                hourCount.put(hour, hourCount.get(hour) + 1);
            }
        }

        List<String> hours = new ArrayList<String>();
        List<Integer> counts = new ArrayList<Integer>();

        for (int i = 0; i < 24; i++) {
            hours.add(i + ":00");
            counts.add(hourCount.get(i));
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("hours", hours);
        result.put("counts", counts);

        return Result.success(result);
    }

    /**
     * 获取采购建议
     */
    @GetMapping("/purchase-suggestions")
    public Result getPurchaseSuggestions() {
        // 获取所有图书
        List<Book> books = bookService.list();

        // 获取所有借阅记录
        List<Borrow> borrows = borrowService.list();
        
        // 获取当前未归还的借阅（状态为0-借阅中或2-已逾期未还）
        Set<Long> activeBorrowBookIds = new HashSet<Long>();
        for (Borrow borrow : borrows) {
            if (borrow.getStatus() == 0 || borrow.getStatus() == 2) {
                activeBorrowBookIds.add(borrow.getBookId());
            }
        }

        // 按分类统计图书总数、可借数量和借阅次数
        Map<String, Integer> categoryTotalCount = new HashMap<String, Integer>();     // 总数量
        Map<String, Integer> categoryAvailableCount = new HashMap<String, Integer>(); // 可借数量
        Map<String, Integer> categoryBorrowCount = new HashMap<String, Integer>();     // 借阅次数

        // 使用图书的总数量和可借数量
        for (Book book : books) {
            String category = book.getCategory();
            
            // 统计总数量
            int totalCount = book.getTotalCount() != null ? book.getTotalCount() : 1;
            categoryTotalCount.put(category, categoryTotalCount.getOrDefault(category, 0) + totalCount);
            
            // 统计可借数量（使用availableCount字段，如果为null则根据借阅情况计算）
            int availableCount;
            if (book.getAvailableCount() != null) {
                availableCount = book.getAvailableCount();
            } else {
                // 如果没有availableCount字段，则使用总数减去当前借出数量的估计值
                availableCount = totalCount - (activeBorrowBookIds.contains(book.getId()) ? 1 : 0);
            }
            categoryAvailableCount.put(category, categoryAvailableCount.getOrDefault(category, 0) + availableCount);
        }

        // 统计每个分类的历史借阅次数
        for (Borrow borrow : borrows) {
            Book book = bookService.getById(borrow.getBookId());
            if (book != null) {
                String category = book.getCategory();
                categoryBorrowCount.put(category, categoryBorrowCount.getOrDefault(category, 0) + 1);
            }
        }

        // 计算需求度和建议采购数量
        List<Map<String, Object>> suggestions = new ArrayList<Map<String, Object>>();

        for (String category : categoryTotalCount.keySet()) {
            int totalCount = categoryTotalCount.get(category);
            int availableCount = categoryAvailableCount.getOrDefault(category, 0);
            int borrowCount = categoryBorrowCount.getOrDefault(category, 0);
            
            // 计算已借出比例
            double borrowedRatio = (totalCount - availableCount) / (double) totalCount;
            
            // 改进的需求度计算：综合考虑历史借阅次数与当前借出比例
            // 需求度 = (借阅次数 / 总数量 * 60 + 已借出比例 * 40)，最大100
            int demand = Math.min(100, (int)((borrowCount * 60.0 / (totalCount > 0 ? totalCount : 1)) + (borrowedRatio * 40.0)));

            // 建议采购数量：根据需求度和当前数量计算
            int suggestedCount = 0;
            if (demand > 80) {
                suggestedCount = (int) (totalCount * 0.3);
            } else if (demand > 60) {
                suggestedCount = (int) (totalCount * 0.2);
            } else if (demand > 40) {
                suggestedCount = (int) (totalCount * 0.1);
            }
            
            // 确保建议数量至少为1
            if (demand > 40 && suggestedCount == 0) {
                suggestedCount = 1;
            }

            Map<String, Object> suggestion = new HashMap<String, Object>();
            suggestion.put("category", category);
            suggestion.put("currentCount", totalCount);
            suggestion.put("availableCount", availableCount);
            suggestion.put("borrowCount", borrowCount);
            suggestion.put("suggestedCount", suggestedCount);
            suggestion.put("demand", demand);

            suggestions.add(suggestion);
        }

        // 按需求度排序
        Collections.sort(suggestions, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> a, Map<String, Object> b) {
                return (Integer) b.get("demand") - (Integer) a.get("demand");
            }
        });

        return Result.success(suggestions);
    }

    /**
     * 获取藏书结构数据
     */
    @GetMapping("/book-structure")
    public Result getBookStructure() {
        // 获取所有图书
        List<Book> books = bookService.list();

        // 获取所有借阅记录
        List<Borrow> borrows = borrowService.list();
        Set<Long> borrowedBookIds = new HashSet<Long>();
        for (Borrow borrow : borrows) {
            if (borrow.getStatus() == 0) { // 只统计未归还的
                borrowedBookIds.add(borrow.getBookId());
            }
        }

        // 按分类统计总数和已借出数量
        Map<String, Integer> categoryTotalCount = new HashMap<String, Integer>();
        Map<String, Integer> categoryBorrowedCount = new HashMap<String, Integer>();

        for (Book book : books) {
            String category = book.getCategory();
            categoryTotalCount.put(category, categoryTotalCount.getOrDefault(category, 0) + 1);

            if (borrowedBookIds.contains(book.getId())) {
                categoryBorrowedCount.put(category, categoryBorrowedCount.getOrDefault(category, 0) + 1);
            }
        }

        List<String> categories = new ArrayList<String>(categoryTotalCount.keySet());
        List<Integer> borrowed = new ArrayList<Integer>();
        List<Integer> total = new ArrayList<Integer>();

        for (String category : categories) {
            borrowed.add(categoryBorrowedCount.getOrDefault(category, 0));
            total.add(categoryTotalCount.get(category));
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("categories", categories);
        result.put("borrowed", borrowed);
        result.put("total", total);

        return Result.success(result);
    }

    /**
     * 获取图书排行榜
     */
    @GetMapping("/book-rankings")
    public Result getBookRankings(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 获取所有图书
        List<Book> books = bookService.list();

        // 获取所有借阅记录
        List<Borrow> borrows = borrowService.list();

        // 统计每本书的借阅次数
        Map<Long, Integer> bookBorrowCount = new HashMap<Long, Integer>();
        for (Borrow borrow : borrows) {
            Long bookId = borrow.getBookId();
            bookBorrowCount.put(bookId, bookBorrowCount.getOrDefault(bookId, 0) + 1);
        }

        // 构建排行榜数据
        List<Map<String, Object>> rankings = new ArrayList<Map<String, Object>>();

        for (Book book : books) {
            int count = bookBorrowCount.getOrDefault(book.getId(), 0);
            if (count > 0) { // 只显示有借阅记录的书
                Map<String, Object> ranking = new HashMap<String, Object>();
                ranking.put("id", book.getId());
                ranking.put("title", book.getTitle());
                ranking.put("author", book.getAuthor());
                ranking.put("borrowCount", count);
                ranking.put("trend", (int) (Math.random() * 40) - 20); // 模拟环比变化

                rankings.add(ranking);
            }
        }

        // 按借阅次数排序
        Collections.sort(rankings, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> a, Map<String, Object> b) {
                return (Integer) b.get("borrowCount") - (Integer) a.get("borrowCount");
            }
        });

        // 添加排名
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).put("rank", i + 1);
        }

        // 分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, rankings.size());

        List<Map<String, Object>> pagedRankings = start < rankings.size() ? rankings.subList(start, end)
                : new ArrayList<Map<String, Object>>();

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("records", pagedRankings);
        result.put("total", rankings.size());

        return Result.success(result);
    }

    /**
     * 获取读者排行榜
     */
    @GetMapping("/reader-rankings")
    public Result getReaderRankings(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 获取所有用户
        List<User> users = userService.list();

        // 获取所有借阅记录
        List<Borrow> borrows = borrowService.list();

        // 统计每个用户的借阅情况
        Map<Long, Integer> userTotalBorrows = new HashMap<Long, Integer>();
        Map<Long, Integer> userCurrentBorrows = new HashMap<Long, Integer>();
        Map<Long, Integer> userOnTimeBorrows = new HashMap<Long, Integer>();
        Map<Long, Integer> userTotalReturnedBorrows = new HashMap<Long, Integer>();

        for (Borrow borrow : borrows) {
            Long userId = borrow.getUserId();

            // 总借阅次数
            userTotalBorrows.put(userId, userTotalBorrows.getOrDefault(userId, 0) + 1);

            // 当前借阅数量
            if (borrow.getStatus() == 0) {
                userCurrentBorrows.put(userId, userCurrentBorrows.getOrDefault(userId, 0) + 1);
            }

            // 已归还的借阅
            if (borrow.getStatus() == 1 || borrow.getStatus() == 3) {
                userTotalReturnedBorrows.put(userId, userTotalReturnedBorrows.getOrDefault(userId, 0) + 1);

                // 准时归还
                if (borrow.getStatus() == 1) {
                    userOnTimeBorrows.put(userId, userOnTimeBorrows.getOrDefault(userId, 0) + 1);
                }
            }
        }

        // 构建排行榜数据
        List<Map<String, Object>> rankings = new ArrayList<Map<String, Object>>();

        for (User user : users) {
            int totalBorrows = userTotalBorrows.getOrDefault(user.getId(), 0);
            if (totalBorrows > 0) { // 只显示有借阅记录的用户
                int currentBorrows = userCurrentBorrows.getOrDefault(user.getId(), 0);
                int totalReturned = userTotalReturnedBorrows.getOrDefault(user.getId(), 0);
                int onTimeReturns = userOnTimeBorrows.getOrDefault(user.getId(), 0);

                // 计算准时归还率
                int returnRate = totalReturned > 0 ? (int) (onTimeReturns * 100.0 / totalReturned) : 100;

                Map<String, Object> ranking = new HashMap<String, Object>();
                ranking.put("id", user.getId());
                ranking.put("username", user.getUsername());
                ranking.put("totalBorrows", totalBorrows);
                ranking.put("currentBorrows", currentBorrows);
                ranking.put("returnRate", returnRate);

                rankings.add(ranking);
            }
        }

        // 按总借阅次数排序
        Collections.sort(rankings, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> a, Map<String, Object> b) {
                return (Integer) b.get("totalBorrows") - (Integer) a.get("totalBorrows");
            }
        });

        // 添加排名
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).put("rank", i + 1);
        }

        // 分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, rankings.size());

        List<Map<String, Object>> pagedRankings = start < rankings.size() ? rankings.subList(start, end)
                : new ArrayList<Map<String, Object>>();

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("records", pagedRankings);
        result.put("total", rankings.size());

        return Result.success(result);
    }

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard")
    public Result getDashboardStatistics() {
        // 获取所有图书
        long totalBooksLong = bookService.count();
        int totalBooks = (int) totalBooksLong;

        // 获取所有用户
        long totalUsersLong = userService.count();
        int totalUsers = (int) totalUsersLong;

        // 获取所有借阅记录
        List<Borrow> borrows = borrowService.list();

        // 当前借阅数量
        int currentBorrowCount = 0;

        // 总借阅次数
        int totalBorrowCount = borrows.size();

        // 逾期数量
        int overdueCount = 0;

        // 总罚款金额
        double totalFine = 0;

        // 未缴纳罚款数量
        int unpaidFineCount = 0;

        for (Borrow borrow : borrows) {
            if (borrow.getStatus() == 0) {
                currentBorrowCount++;
            }

            if (borrow.getStatus() == 2 || borrow.getStatus() == 3) {
                overdueCount++;
            }

            if (borrow.getFine() != null && borrow.getFine() > 0) {
                totalFine += borrow.getFine();

                if (borrow.getFineStatus() != null && borrow.getFineStatus() == 0) {
                    unpaidFineCount++;
                }
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("totalBooks", totalBooks);
        result.put("totalUsers", totalUsers);
        result.put("currentBorrowCount", currentBorrowCount);
        result.put("totalBorrowCount", totalBorrowCount);
        result.put("overdueCount", overdueCount);
        result.put("totalFine", totalFine);
        result.put("unpaidFineCount", unpaidFineCount);

        return Result.success(result);
    }

    /**
     * 导出借阅统计数据
     */
    @GetMapping("/export")
    public void exportBorrowStatistics(
            HttpServletResponse response,
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate)
            throws IOException {

        // 这里实现导出功能，将数据写入响应流
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=borrow-statistics.xlsx");

        // 实际导出逻辑需要使用Excel库实现，这里只是示例
        response.getWriter().write("借阅统计数据导出示例");
    }

    /**
     * 获取图书借阅热度分析
     */
    @GetMapping("/book-heat")
    public Result getBookHeatAnalysis() {
        // 获取所有图书
        final List<Book> books = bookService.list();

        // 按借阅次数排序
        Collections.sort(books, new Comparator<Book>() {
            @Override
            public int compare(Book a, Book b) {
                int countA = a.getBorrowCount() != null ? a.getBorrowCount() : 0;
                int countB = b.getBorrowCount() != null ? b.getBorrowCount() : 0;
                return countB - countA;
            }
        });

        // 取前10本最热门的图书
        List<Book> topBooks = new ArrayList<Book>();
        for (int i = 0; i < Math.min(10, books.size()); i++) {
            topBooks.add(books.get(i));
        }

        List<String> bookTitles = new ArrayList<String>();
        List<Integer> borrowCounts = new ArrayList<Integer>();

        for (Book book : topBooks) {
            bookTitles.add(book.getTitle());
            borrowCounts.add(book.getBorrowCount() != null ? book.getBorrowCount() : 0);
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("bookTitles", bookTitles);
        result.put("borrowCounts", borrowCounts);

        return Result.success(result);
    }

    /**
     * 获取图书分类分布分析
     */
    @GetMapping("/book-category-distribution")
    public Result getBookCategoryDistribution() {
        // 获取所有图书
        List<Book> books = bookService.list();

        // 按分类统计
        Map<String, Long> categoryCount = new HashMap<String, Long>();
        for (Book book : books) {
            String category = book.getCategory();
            if (categoryCount.containsKey(category)) {
                categoryCount.put(category, categoryCount.get(category) + 1);
            } else {
                categoryCount.put(category, 1L);
            }
        }

        // 按子分类统计
        Map<String, Long> subCategoryCount = new HashMap<String, Long>();
        for (Book book : books) {
            if (book.getSubCategory() != null && !book.getSubCategory().isEmpty()) {
                String subCategory = book.getSubCategory();
                if (subCategoryCount.containsKey(subCategory)) {
                    subCategoryCount.put(subCategory, subCategoryCount.get(subCategory) + 1);
                } else {
                    subCategoryCount.put(subCategory, 1L);
                }
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("categoryCount", categoryCount);
        result.put("subCategoryCount", subCategoryCount);

        return Result.success(result);
    }

    /**
     * 获取图书价格分布分析
     */
    @GetMapping("/book-price-distribution")
    public Result getBookPriceDistribution() {
        // 获取所有图书
        List<Book> books = bookService.list();

        // 价格区间统计
        Map<String, Integer> priceRangeCount = new HashMap<String, Integer>();
        priceRangeCount.put("0-50", 0);
        priceRangeCount.put("50-100", 0);
        priceRangeCount.put("100-200", 0);
        priceRangeCount.put("200+", 0);

        for (Book book : books) {
            if (book.getPrice() != null) {
                double price = book.getPrice();
                if (price < 50) {
                    priceRangeCount.put("0-50", priceRangeCount.get("0-50") + 1);
                } else if (price < 100) {
                    priceRangeCount.put("50-100", priceRangeCount.get("50-100") + 1);
                } else if (price < 200) {
                    priceRangeCount.put("100-200", priceRangeCount.get("100-200") + 1);
                } else {
                    priceRangeCount.put("200+", priceRangeCount.get("200+") + 1);
                }
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("priceRanges", new ArrayList<String>(priceRangeCount.keySet()));
        result.put("counts", new ArrayList<Integer>(priceRangeCount.values()));

        return Result.success(result);
    }

    /**
     * 获取图书出版年份分析
     */
    @GetMapping("/book-publish-year")
    public Result getBookPublishYearAnalysis() {
        // 获取所有图书
        List<Book> books = bookService.list();

        // 按出版年份统计
        Map<Integer, Integer> yearCount = new HashMap<Integer, Integer>();

        for (Book book : books) {
            if (book.getPublishDate() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(book.getPublishDate());
                int year = calendar.get(Calendar.YEAR);
                yearCount.put(year, yearCount.getOrDefault(year, 0) + 1);
            }
        }

        // 按年份排序
        List<Integer> years = new ArrayList<Integer>(yearCount.keySet());
        Collections.sort(years);

        List<Integer> counts = new ArrayList<Integer>();
        for (int year : years) {
            counts.add(yearCount.get(year));
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("years", years);
        result.put("counts", counts);

        return Result.success(result);
    }

    /**
     * 获取图书借阅趋势分析
     */
    @GetMapping("/book-borrow-trend")
    public Result getBookBorrowTrend(
            @RequestParam(required = false) String timeRange) {

        // 根据时间范围确定开始和结束日期
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate = now;

        if (timeRange != null) {
            if ("week".equals(timeRange)) {
                startDate = now.minusWeeks(1);
            } else if ("month".equals(timeRange)) {
                startDate = now.minusMonths(1);
            } else if ("quarter".equals(timeRange)) {
                startDate = now.minusMonths(3);
            } else if ("year".equals(timeRange)) {
                startDate = now.minusYears(1);
            } else {
                startDate = now.minusMonths(1);
            }
        } else {
            startDate = now.minusMonths(1);
        }

        // 获取日期范围内的所有借阅记录
        List<Borrow> borrows = borrowService.list();

        // 按日期统计
        Map<String, Integer> dailyBorrows = new HashMap<String, Integer>();

        LocalDate date = startDate;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        while (!date.isAfter(endDate)) {
            String dateStr = formatter.format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            dailyBorrows.put(dateStr, 0);
            date = date.plusDays(1);
        }

        for (Borrow borrow : borrows) {
            if (borrow.getBorrowTime() != null) {
                LocalDate borrowDate = borrow.getBorrowTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (!borrowDate.isBefore(startDate) && !borrowDate.isAfter(endDate)) {
                    String dateStr = formatter.format(borrow.getBorrowTime());
                    dailyBorrows.put(dateStr, dailyBorrows.get(dateStr) + 1);
                }
            }
        }

        // 按日期排序
        List<String> dates = new ArrayList<String>(dailyBorrows.keySet());
        Collections.sort(dates);

        List<Integer> counts = new ArrayList<Integer>();
        for (String dateStr : dates) {
            counts.add(dailyBorrows.get(dateStr));
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("dates", dates);
        result.put("counts", counts);

        return Result.success(result);
    }

    /**
     * 获取图书库存分析
     */
    @GetMapping("/book-inventory")
    public Result getBookInventoryAnalysis() {
        // 获取所有图书
        List<Book> books = bookService.list();

        // 计算总库存和可借库存
        int totalInventory = 0;
        int availableInventory = 0;

        for (Book book : books) {
            if (book.getTotalCount() != null) {
                totalInventory += book.getTotalCount();
            }
            if (book.getAvailableCount() != null) {
                availableInventory += book.getAvailableCount();
            }
        }

        // 按分类统计库存
        Map<String, Map<String, Integer>> categoryInventory = new HashMap<String, Map<String, Integer>>();

        for (Book book : books) {
            String category = book.getCategory();
            if (!categoryInventory.containsKey(category)) {
                Map<String, Integer> inventory = new HashMap<String, Integer>();
                inventory.put("total", 0);
                inventory.put("available", 0);
                categoryInventory.put(category, inventory);
            }

            Map<String, Integer> inventory = categoryInventory.get(category);
            if (book.getTotalCount() != null) {
                inventory.put("total", inventory.get("total") + book.getTotalCount());
            }
            if (book.getAvailableCount() != null) {
                inventory.put("available", inventory.get("available") + book.getAvailableCount());
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("totalInventory", totalInventory);
        result.put("availableInventory", availableInventory);
        result.put("categoryInventory", categoryInventory);

        return Result.success(result);
    }
}