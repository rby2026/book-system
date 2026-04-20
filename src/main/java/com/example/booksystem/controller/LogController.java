package com.example.booksystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.booksystem.common.Result;
import com.example.booksystem.entity.Log;
import com.example.booksystem.service.LogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 系统日志控制器
 */
@Api(tags = "系统日志管理")
@RestController
@RequestMapping("/log")
@PreAuthorize("hasRole('ADMIN')")
public class LogController {

    @Autowired
    private LogService logService;

    @ApiOperation("分页查询日志")
    @GetMapping("/page")
    public Result<IPage<Log>> pageLogs(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("用户名") @RequestParam(required = false) String username,
            @ApiParam("操作类型") @RequestParam(required = false) String operation,
            @ApiParam("操作状态") @RequestParam(required = false) Integer status,
            @ApiParam("开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @ApiParam("结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        Page<Log> page = new Page<>(current, size);
        IPage<Log> logPage = logService.pageLogs(page, username, operation, status, startTime, endTime);
        return Result.success(logPage);
    }

    @ApiOperation("删除日志")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteLog(@PathVariable Long id) {
        boolean result = logService.removeById(id);
        return Result.success(result, "删除成功");
    }

    @ApiOperation("批量删除日志")
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteLogs(@RequestBody Long[] ids) {
        boolean result = logService.removeByIds(java.util.Arrays.asList(ids));
        return Result.success(result, "删除成功");
    }
}