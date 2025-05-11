package com.management.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamPaper {
    private Integer id;
    private String title;
    private String description;
    private Integer courseId;  // 关联的课程ID
    private String courseName; // 课程名称（冗余字段，便于显示）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 