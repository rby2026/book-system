package com.example.booksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.booksystem.entity.Announcement;
import com.example.booksystem.mapper.AnnouncementMapper;
import com.example.booksystem.service.AnnouncementService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 公告服务实现
 */
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Override
    public IPage<Announcement> pageAnnouncements(Page<Announcement> page, String title, Integer status) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        
        if (title != null && !title.isEmpty()) {
            queryWrapper.like(Announcement::getTitle, title);
        }
        
        if (status != null) {
            queryWrapper.eq(Announcement::getStatus, status);
        }
        
        queryWrapper.orderByDesc(Announcement::getPublishTime);
        
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public boolean addAnnouncement(Announcement announcement) {
        announcement.setCreateTime(new Date());
        announcement.setUpdateTime(new Date());
        if (announcement.getStatus() == 1) {
            announcement.setPublishTime(new Date());
        }
        return save(announcement);
    }

    @Override
    public boolean updateAnnouncement(Announcement announcement) {
        announcement.setUpdateTime(new Date());
        if (announcement.getStatus() == 1 && announcement.getPublishTime() == null) {
            announcement.setPublishTime(new Date());
        }
        return updateById(announcement);
    }

    @Override
    public boolean deleteAnnouncement(Long id) {
        return removeById(id);
    }

    @Override
    public Announcement getAnnouncementDetail(Long id) {
        return getById(id);
    }

    @Override
    public List<Announcement> getLatestAnnouncements(int limit) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getPublishTime)
                .last("LIMIT " + limit);
        
        return list(queryWrapper);
    }
}