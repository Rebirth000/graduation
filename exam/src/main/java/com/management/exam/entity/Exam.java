package com.management.exam.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Exam {
    private Integer id;
    private String name;
    private Integer courseId;
    private String courseName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer paperId;
    private String paperName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String registrationStatus;
    private Integer registrationId;
} 