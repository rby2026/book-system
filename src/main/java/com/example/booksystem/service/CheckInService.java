package com.example.booksystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.booksystem.entity.CheckIn;

import java.util.Map;

public interface CheckInService extends IService<CheckIn> {

    boolean checkIn();

    boolean isCheckedInToday();

    long getCheckInCount();
}
