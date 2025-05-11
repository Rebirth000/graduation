package com.management.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamRoom {
    private Integer id;
    private String name;
    private String location;
    private Integer capacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 