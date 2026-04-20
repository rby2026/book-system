package com.example.booksystem.controller;

import com.example.booksystem.common.Result;
import com.example.booksystem.entity.Violation;
import com.example.booksystem.service.ViolationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 违章信息控制器
 */
@RestController
@RequestMapping("/violation")
public class ViolationController {

    @Autowired
    private ViolationService violationService;

    /**
     * 分页查询违章信息
     */
    @GetMapping("/page")
    public Result pageViolations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer violationType,
            @RequestParam(required = false) Integer processStatus) {

        Page<Violation> pageParam = new Page<>(page, size);
        return Result.success(violationService.pageViolations(pageParam, userId, violationType, processStatus));
    }

    /**
     * 添加违章信息
     */
    @PostMapping("/add")
    public Result addViolation(@RequestBody Violation violation) {
        return Result.success(violationService.addViolation(violation));
    }

    /**
     * 更新违章信息
     */
    @PutMapping("/update")
    public Result updateViolation(@RequestBody Violation violation) {
        return Result.success(violationService.updateViolation(violation));
    }

    /**
     * 删除违章信息
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteViolation(@PathVariable Long id) {
        return Result.success(violationService.deleteViolation(id));
    }

    /**
     * 获取违章信息详情
     */
    @GetMapping("/detail/{id}")
    public Result getViolationDetail(@PathVariable Long id) {
        return Result.success(violationService.getViolationDetail(id));
    }

    /**
     * 获取用户的违章信息
     */
    @GetMapping("/user/{userId}")
    public Result getUserViolations(@PathVariable Long userId) {
        return Result.success(violationService.getUserViolations(userId));
    }

    /**
     * 获取未处理的违章信息
     */
    @GetMapping("/unprocessed")
    public Result getUnprocessedViolations() {
        return Result.success(violationService.getUnprocessedViolations());
    }
}