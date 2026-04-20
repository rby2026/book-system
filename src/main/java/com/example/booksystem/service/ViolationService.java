package com.example.booksystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.booksystem.entity.Violation;

/**
 * 违章信息服务接口
 */
public interface ViolationService extends IService<Violation> {

    /**
     * 分页查询违章信息
     *
     * @param page         分页参数
     * @param userId       用户ID
     * @param violationType 违章类型
     * @param processStatus 处理状态
     * @return 分页结果
     */
    IPage<Violation> pageViolations(Page<Violation> page, Long userId, Integer violationType, Integer processStatus);

    /**
     * 添加违章信息
     *
     * @param violation 违章信息
     * @return 添加结果
     */
    boolean addViolation(Violation violation);

    /**
     * 更新违章信息
     *
     * @param violation 违章信息
     * @return 更新结果
     */
    boolean updateViolation(Violation violation);

    /**
     * 删除违章信息
     *
     * @param id 违章ID
     * @return 删除结果
     */
    boolean deleteViolation(Long id);

    /**
     * 获取违章信息详情
     *
     * @param id 违章ID
     * @return 违章信息详情
     */
    Violation getViolationDetail(Long id);

    /**
     * 获取用户的违章信息
     *
     * @param userId 用户ID
     * @return 违章信息列表
     */
    java.util.List<Violation> getUserViolations(Long userId);

    /**
     * 获取未处理的违章信息
     *
     * @return 未处理违章信息列表
     */
    java.util.List<Violation> getUnprocessedViolations();
}