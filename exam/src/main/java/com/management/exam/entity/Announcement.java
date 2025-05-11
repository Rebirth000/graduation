package com.management.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Announcement {
    private Integer id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 