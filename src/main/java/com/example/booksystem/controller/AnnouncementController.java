package com.example.booksystem.controller;

import com.example.booksystem.common.Result;
import com.example.booksystem.entity.Announcement;
import com.example.booksystem.service.AnnouncementService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 分页查询公告
     */
    @GetMapping("/page")
    public Result pageAnnouncements(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {

        Page<Announcement> pageParam = new Page<>(page, size);
        return Result.success(announcementService.pageAnnouncements(pageParam, title, status));
    }

    /**
     * 添加公告
     */
    @PostMapping("/add")
    public Result addAnnouncement(@RequestBody Announcement announcement) {
        return Result.success(announcementService.addAnnouncement(announcement));
    }

    /**
     * 更新公告
     */
    @PutMapping("/update")
    public Result updateAnnouncement(@RequestBody Announcement announcement) {
        return Result.success(announcementService.updateAnnouncement(announcement));
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteAnnouncement(@PathVariable Long id) {
        return Result.success(announcementService.deleteAnnouncement(id));
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/detail/{id}")
    public Result getAnnouncementDetail(@PathVariable Long id) {
        return Result.success(announcementService.getAnnouncementDetail(id));
    }

    /**
     * 获取最新公告
     */
    @GetMapping("/latest")
    public Result getLatestAnnouncements(
            @RequestParam(defaultValue = "5") Integer limit) {
        return Result.success(announcementService.getLatestAnnouncements(limit));
    }
}