package com.example.booksystem.controller;

import com.example.booksystem.common.Result;
import com.example.booksystem.entity.BorrowRule;
import com.example.booksystem.service.BorrowRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 借阅规则控制器
 */
@Api(tags = "借阅规则管理")
@RestController
@RequestMapping("/rule")
@PreAuthorize("hasRole('ADMIN')")
public class BorrowRuleController {

    @Autowired
    private BorrowRuleService borrowRuleService;

    @ApiOperation("获取当前生效的借阅规则")
    @GetMapping("/current")
    @PreAuthorize("permitAll()")
    public Result<BorrowRule> getCurrentRule() {
        BorrowRule rule = borrowRuleService.getCurrentRule();
        return Result.success(rule);
    }

    @ApiOperation("获取所有借阅规则")
    @GetMapping("/list")
    public Result<List<BorrowRule>> listRules() {
        List<BorrowRule> rules = borrowRuleService.list();
        return Result.success(rules);
    }

    @ApiOperation("添加借阅规则")
    @PostMapping
    public Result<Boolean> addRule(@RequestBody BorrowRule borrowRule) {
        boolean result = borrowRuleService.addRule(borrowRule);
        return Result.success(result, "添加成功");
    }

    @ApiOperation("更新借阅规则")
    @PutMapping
    public Result<Boolean> updateRule(@RequestBody BorrowRule borrowRule) {
        boolean result = borrowRuleService.updateRule(borrowRule);
        return Result.success(result, "更新成功");
    }

    @ApiOperation("启用借阅规则")
    @PutMapping("/enable/{id}")
    public Result<Boolean> enableRule(@PathVariable Long id) {
        boolean result = borrowRuleService.enableRule(id);
        return Result.success(result, "启用成功");
    }

    @ApiOperation("禁用借阅规则")
    @PutMapping("/disable/{id}")
    public Result<Boolean> disableRule(@PathVariable Long id) {
        boolean result = borrowRuleService.disableRule(id);
        return Result.success(result, "禁用成功");
    }
}