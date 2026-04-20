package com.example.booksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.booksystem.entity.Log;
import com.example.booksystem.mapper.LogMapper;
import com.example.booksystem.service.LogService;
import com.example.booksystem.service.UserService;
import com.example.booksystem.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * 系统日志服务实现类
 */
@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements LogService {

    @Autowired
    private UserService userService;

    @Override
    public void saveLog(String operation, String method, String params, String ip, Integer status, String errorMsg,
            Long time) {
        // 获取当前用户
        UserVO currentUser = userService.getCurrentUser();

        // 创建日志对象
        Log log = new Log();
        log.setUserId(currentUser != null ? currentUser.getId() : null);
        log.setUsername(currentUser != null ? currentUser.getUsername() : "未登录用户");
        log.setOperation(operation);
        log.setMethod(method);
        log.setParams(params);
        log.setIp(ip);
        log.setStatus(status);
        log.setErrorMsg(errorMsg);
        log.setOperationTime(new Date());
        log.setExecutionTime(time);

        // 保存日志
        save(log);
    }

    @Override
    public IPage<Log> pageLogs(Page<Log> page, String username, String operation, Integer status, Date startTime,
            Date endTime) {
        LambdaQueryWrapper<Log> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (StringUtils.hasText(username)) {
            queryWrapper.like(Log::getUsername, username);
        }
        if (StringUtils.hasText(operation)) {
            queryWrapper.eq(Log::getOperation, operation);
        }
        if (status != null) {
            queryWrapper.eq(Log::getStatus, status);
        }
        if (startTime != null) {
            queryWrapper.ge(Log::getOperationTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(Log::getOperationTime, endTime);
        }

        // 按操作时间降序排序
        queryWrapper.orderByDesc(Log::getOperationTime);

        return page(page, queryWrapper);
    }
}