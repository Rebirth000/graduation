package com.management.exam.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamUpdateVO {
    private String examName;
    private String courseName;
    private String paperTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
} 