package com.example.booksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.booksystem.entity.Violation;
import com.example.booksystem.mapper.ViolationMapper;
import com.example.booksystem.service.ViolationService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 违章信息服务实现
 */
@Service
public class ViolationServiceImpl extends ServiceImpl<ViolationMapper, Violation> implements ViolationService {

    @Override
    public IPage<Violation> pageViolations(Page<Violation> page, Long userId, Integer violationType, Integer processStatus) {
        LambdaQueryWrapper<Violation> queryWrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            queryWrapper.eq(Violation::getUserId, userId);
        }
        
        if (violationType != null) {
            queryWrapper.eq(Violation::getViolationType, violationType);
        }
        
        if (processStatus != null) {
            queryWrapper.eq(Violation::getProcessStatus, processStatus);
        }
        
        queryWrapper.orderByDesc(Violation::getViolationTime);
        
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public boolean addViolation(Violation violation) {
        violation.setCreateTime(new Date());
        violation.setUpdateTime(new Date());
        violation.setViolationTime(new Date());
        violation.setProcessStatus(0);
        return save(violation);
    }

    @Override
    public boolean updateViolation(Violation violation) {
        violation.setUpdateTime(new Date());
        if (violation.getProcessStatus() == 1 && violation.getProcessTime() == null) {
            violation.setProcessTime(new Date());
        }
        return updateById(violation);
    }

    @Override
    public boolean deleteViolation(Long id) {
        return removeById(id);
    }

    @Override
    public Violation getViolationDetail(Long id) {
        return getById(id);
    }

    @Override
    public List<Violation> getUserViolations(Long userId) {
        LambdaQueryWrapper<Violation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Violation::getUserId, userId)
                .orderByDesc(Violation::getViolationTime);
        
        return list(queryWrapper);
    }

    @Override
    public List<Violation> getUnprocessedViolations() {
        LambdaQueryWrapper<Violation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Violation::getProcessStatus, 0)
                .orderByDesc(Violation::getViolationTime);
        
        return list(queryWrapper);
    }
}