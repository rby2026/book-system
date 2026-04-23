package com.example.booksystem.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.booksystem.entity.Borrow;
import com.example.booksystem.entity.BorrowRule;
import com.example.booksystem.service.BorrowRuleService;
import com.example.booksystem.service.BorrowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 借阅状态更新定时任务
 * 每分钟检查并更新借阅状态
 */
@Slf4j
@Component
public class BorrowStatusTask {

    @Autowired
    private BorrowService borrowService;
    
    @Autowired
    private BorrowRuleService borrowRuleService;

    /**
     * 每1分钟执行一次，检查借阅状态
     * 状态说明：
     * 0-借阅中，1-已归还，2-已逾期未还，3-已逾期已还
     */
    @Scheduled(fixedRate = 60000) // 60000毫秒 = 1分钟
    public void updateBorrowStatus() {
        log.info("开始执行借阅状态检查任务 - {}", new Date());
        
        try {
            // 1. 更新"借阅中"变为"已逾期未还"
            updateBorrowingToOverdue();
            
            // 2. 计算逾期未还的罚款金额
            calculateOverdueFine();
            
            log.info("借阅状态检查任务执行完成");
        } catch (Exception e) {
            log.error("借阅状态检查任务执行异常", e);
        }
    }
    
    /**
     * 更新借阅中的记录，如果超过应还时间，更新为已逾期未还
     */
    private void updateBorrowingToOverdue() {
        // 查询所有借阅中且已过应还时间的记录
        LambdaQueryWrapper<Borrow> borrowingQuery = new LambdaQueryWrapper<>();
        borrowingQuery.eq(Borrow::getStatus, 0)  // 借阅中
                      .lt(Borrow::getReturnTime, new Date()); // 应还时间小于当前时间
        
        List<Borrow> overdueList = borrowService.list(borrowingQuery);
        
        if (!overdueList.isEmpty()) {
            log.info("发现{}条逾期未还记录", overdueList.size());
            
            for (Borrow borrow : overdueList) {
                // 更新状态为已逾期未还(2)
                LambdaUpdateWrapper<Borrow> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Borrow::getId, borrow.getId())
                             .set(Borrow::getStatus, 2);
                
                borrowService.update(updateWrapper);
            }
        }
    }
    
    /**
     * 计算逾期未还的罚款金额
     */
    private void calculateOverdueFine() {
        // 查询当前启用的借阅规则
        LambdaQueryWrapper<BorrowRule> ruleQuery = new LambdaQueryWrapper<>();
        ruleQuery.eq(BorrowRule::getStatus, 1);  // 启用状态
        List<BorrowRule> rules = borrowRuleService.list(ruleQuery);
        
        if (rules == null || rules.isEmpty()) {
            log.warn("未找到启用的借阅规则，无法计算罚款");
            return;
        }
        
        // 使用第一条启用的规则
        BorrowRule rule = rules.get(0);
        
        // 查询所有已逾期未还的记录
        LambdaQueryWrapper<Borrow> overdueQuery = new LambdaQueryWrapper<>();
        overdueQuery.eq(Borrow::getStatus, 2) // 已逾期未还
                    .eq(Borrow::getFineStatus, 1); // 未缴纳罚款
        
        List<Borrow> overdueList = borrowService.list(overdueQuery);
        
        if (!overdueList.isEmpty()) {
            log.info("更新{}条逾期记录的罚款金额", overdueList.size());
            
            Date now = new Date();
            double finePerDay = rule.getFinePerDay();
            
            for (Borrow borrow : overdueList) {
                // 计算逾期天数
                long overdueMillis = now.getTime() - borrow.getReturnTime().getTime();
                int overdueDays = (int) (overdueMillis / (1000 * 60 * 60 * 24)) + 1; // 向上取整，逾期一天内也算一天
                
                // 计算罚款金额
                double fine = overdueDays * finePerDay;
                
                // 更新罚款金额
                LambdaUpdateWrapper<Borrow> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Borrow::getId, borrow.getId())
                             .set(Borrow::getFine, fine);
                
                borrowService.update(updateWrapper);
            }
        }
    }
} 