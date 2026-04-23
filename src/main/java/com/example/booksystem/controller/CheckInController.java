package com.example.booksystem.controller;

import com.example.booksystem.common.Result;
import com.example.booksystem.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/check-in")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @PostMapping("/do")
    public Result doCheckIn() {
        checkInService.checkIn();
        return Result.success("打卡成功");
    }

    @GetMapping("/today")
    public Result isCheckedInToday() {
        Map<String, Object> data = new HashMap<>();
        data.put("checkedIn", checkInService.isCheckedInToday());
        data.put("count", checkInService.getCheckInCount());
        return Result.success(data);
    }

    @GetMapping("/count")
    public Result getCheckInCount() {
        Map<String, Object> data = new HashMap<>();
        data.put("count", checkInService.getCheckInCount());
        return Result.success(data);
    }
}
