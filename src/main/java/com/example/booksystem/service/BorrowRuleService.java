package com.example.booksystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.booksystem.entity.BorrowRule;

/**
 * 借阅规则服务接口
 */
public interface BorrowRuleService extends IService<BorrowRule> {

    /**
     * 获取当前生效的借阅规则
     *
     * @return 借阅规则
     */
    BorrowRule getCurrentRule();

    /**
     * 添加借阅规则
     *
     * @param borrowRule 借阅规则
     * @return 添加结果
     */
    boolean addRule(BorrowRule borrowRule);

    /**
     * 更新借阅规则
     *
     * @param borrowRule 借阅规则
     * @return 更新结果
     */
    boolean updateRule(BorrowRule borrowRule);

    /**
     * 启用借阅规则
     *
     * @param id 规则ID
     * @return 启用结果
     */
    boolean enableRule(Long id);

    /**
     * 禁用借阅规则
     *
     * @param id 规则ID
     * @return 禁用结果
     */
    boolean disableRule(Long id);
}