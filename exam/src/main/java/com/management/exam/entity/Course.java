package com.management.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Course {
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 