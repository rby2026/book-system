package com.example.booksystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.booksystem.entity.Announcement;

/**
 * 公告服务接口
 */
public interface AnnouncementService extends IService<Announcement> {

    /**
     * 分页查询公告
     *
     * @param page     分页参数
     * @param title    标题
     * @param status   状态
     * @return 分页结果
     */
    IPage<Announcement> pageAnnouncements(Page<Announcement> page, String title, Integer status);

    /**
     * 添加公告
     *
     * @param announcement 公告信息
     * @return 添加结果
     */
    boolean addAnnouncement(Announcement announcement);

    /**
     * 更新公告
     *
     * @param announcement 公告信息
     * @return 更新结果
     */
    boolean updateAnnouncement(Announcement announcement);

    /**
     * 删除公告
     *
     * @param id 公告ID
     * @return 删除结果
     */
    boolean deleteAnnouncement(Long id);

    /**
     * 获取公告详情
     *
     * @param id 公告ID
     * @return 公告详情
     */
    Announcement getAnnouncementDetail(Long id);

    /**
     * 获取最新公告
     *
     * @param limit 数量限制
     * @return 最新公告列表
     */
    java.util.List<Announcement> getLatestAnnouncements(int limit);
}