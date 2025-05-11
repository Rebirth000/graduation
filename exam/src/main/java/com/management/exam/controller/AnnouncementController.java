package com.management.exam.controller;

import com.management.exam.entity.Announcement;
import com.management.exam.service.AnnouncementService;
import com.management.exam.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/list")
    public Result<Map<String, Object>> getAnnouncementList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title) {
        return Result.success(announcementService.getAnnouncementList(page, size, title));
    }

    @GetMapping("/all")
    public Result<Map<String, Object>> getAllAnnouncements() {
        return Result.success(announcementService.getAnnouncementList(1, 10000, null));
    }

    @PostMapping
    public Result<String> addAnnouncement(@RequestBody Announcement announcement) {
        announcementService.addAnnouncement(announcement);
        return Result.success("添加成功");
    }

    @PutMapping("/{id}")
    public Result<String> updateAnnouncement(@PathVariable Integer id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcementService.updateAnnouncement(announcement);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteAnnouncement(@PathVariable Integer id) {
        announcementService.deleteAnnouncement(id);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Integer id) {
        return Result.success(announcementService.getAnnouncementById(id));
    }
} 