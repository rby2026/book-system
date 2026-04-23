package com.example.booksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.booksystem.entity.CheckIn;
import com.example.booksystem.exception.BusinessException;
import com.example.booksystem.mapper.CheckInMapper;
import com.example.booksystem.service.CheckInService;
import com.example.booksystem.service.UserService;
import com.example.booksystem.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements CheckInService {

    @Autowired
    private UserService userService;

    @Override
    public boolean checkIn() {
        UserVO currentUser = userService.getCurrentUser();

        if (isCheckedInToday()) {
            throw new BusinessException("今天已打卡");
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(currentUser.getId());
        checkIn.setCheckInDate(new Date());
        checkIn.setCreateTime(new Date());

        return save(checkIn);
    }

    @Override
    public boolean isCheckedInToday() {
        UserVO currentUser = userService.getCurrentUser();

        LocalDate today = LocalDate.now();
        Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<CheckIn> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CheckIn::getUserId, currentUser.getId());
        queryWrapper.ge(CheckIn::getCheckInDate, startOfDay);
        queryWrapper.lt(CheckIn::getCheckInDate, endOfDay);

        return count(queryWrapper) > 0;
    }

    @Override
    public long getCheckInCount() {
        UserVO currentUser = userService.getCurrentUser();

        LambdaQueryWrapper<CheckIn> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CheckIn::getUserId, currentUser.getId());

        return count(queryWrapper);
    }
}
