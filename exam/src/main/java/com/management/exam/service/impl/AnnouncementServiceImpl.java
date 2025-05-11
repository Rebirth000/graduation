package com.management.exam.service.impl;

import com.management.exam.entity.Announcement;
import com.management.exam.mapper.AnnouncementMapper;
import com.management.exam.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {
    
    private final AnnouncementMapper announcementMapper;
    
    @Override
    public Map<String, Object> getAnnouncementList(int page, int size, String title) {
        int offset = (page - 1) * size;
        Map<String, Object> params = new HashMap<>();
        params.put("offset", offset);
        params.put("size", size);
        params.put("title", title);
        
        List<Announcement> records = announcementMapper.selectList(params);
        int total = announcementMapper.selectCount(params);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        return result;
    }
    
    @Override
    @Transactional
    public void addAnnouncement(Announcement announcement) {
        announcementMapper.insert(announcement);
    }
    
    @Override
    @Transactional
    public void updateAnnouncement(Announcement announcement) {
        announcementMapper.update(announcement);
    }
    
    @Override
    @Transactional
    public void deleteAnnouncement(Integer id) {
        announcementMapper.deleteById(id);
    }
    
    @Override
    public Announcement getAnnouncementById(Integer id) {
        return announcementMapper.selectById(id);
    }
} 