package com.management.exam.service;

import com.management.exam.entity.Announcement;
import java.util.Map;

public interface AnnouncementService {
    Map<String, Object> getAnnouncementList(int page, int size, String title);
    
    void addAnnouncement(Announcement announcement);
    
    void updateAnnouncement(Announcement announcement);
    
    void deleteAnnouncement(Integer id);
    
    Announcement getAnnouncementById(Integer id);
} 