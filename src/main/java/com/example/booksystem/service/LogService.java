package com.example.booksystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.booksystem.entity.Log;

import java.util.Date;

/**
 * 系统日志服务接口
 */
public interface LogService extends IService<Log> {

    /**
     * 保存日志
     *
     * @param operation 操作类型
     * @param method    请求方法
     * @param params    请求参数
     * @param ip        IP地址
     * @param status    操作状态
     * @param errorMsg  错误信息
     * @param time      执行时长
     */
    void saveLog(String operation, String method, String params, String ip, Integer status, String errorMsg, Long time);

    /**
     * 分页查询日志
     *
     * @param page      分页参数
     * @param username  用户名
     * @param operation 操作类型
     * @param status    操作状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页结果
     */
    IPage<Log> pageLogs(Page<Log> page, String username, String operation, Integer status, Date startTime,
            Date endTime);
}