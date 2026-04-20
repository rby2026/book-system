package com.example.booksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.booksystem.entity.BorrowRule;
import com.example.booksystem.exception.BusinessException;
import com.example.booksystem.mapper.BorrowRuleMapper;
import com.example.booksystem.service.BorrowRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 借阅规则服务实现类
 */
@Service
public class BorrowRuleServiceImpl extends ServiceImpl<BorrowRuleMapper, BorrowRule> implements BorrowRuleService {

    @Override
    public BorrowRule getCurrentRule() {
        LambdaQueryWrapper<BorrowRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BorrowRule::getStatus, 1); // 状态为启用
        queryWrapper.orderByDesc(BorrowRule::getCreateTime);
        List<BorrowRule> rules = list(queryWrapper);

        if (rules.isEmpty()) {
            throw new BusinessException("当前没有生效的借阅规则");
        }

        return rules.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addRule(BorrowRule borrowRule) {
        // 设置初始值
        borrowRule.setStatus(0); // 默认禁用
        borrowRule.setCreateTime(new Date());
        borrowRule.setUpdateTime(new Date());

        return save(borrowRule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRule(BorrowRule borrowRule) {
        // 校验规则是否存在
        BorrowRule existRule = getById(borrowRule.getId());
        if (existRule == null) {
            throw new BusinessException("借阅规则不存在");
        }

        borrowRule.setUpdateTime(new Date());

        return updateById(borrowRule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableRule(Long id) {
        // 校验规则是否存在
        BorrowRule rule = getById(id);
        if (rule == null) {
            throw new BusinessException("借阅规则不存在");
        }

        // 先禁用所有规则
        LambdaQueryWrapper<BorrowRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BorrowRule::getStatus, 1);
        List<BorrowRule> enabledRules = list(queryWrapper);

        for (BorrowRule enabledRule : enabledRules) {
            enabledRule.setStatus(0);
            enabledRule.setUpdateTime(new Date());
            updateById(enabledRule);
        }

        // 启用当前规则
        rule.setStatus(1);
        rule.setUpdateTime(new Date());

        return updateById(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableRule(Long id) {
        // 校验规则是否存在
        BorrowRule rule = getById(id);
        if (rule == null) {
            throw new BusinessException("借阅规则不存在");
        }

        // 校验是否有其他启用的规则
        LambdaQueryWrapper<BorrowRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BorrowRule::getStatus, 1);
        queryWrapper.ne(BorrowRule::getId, id);
        long count = count(queryWrapper);

        if (count == 0 && rule.getStatus() == 1) {
            throw new BusinessException("至少需要一个启用的借阅规则");
        }

        rule.setStatus(0);
        rule.setUpdateTime(new Date());

        return updateById(rule);
    }
}