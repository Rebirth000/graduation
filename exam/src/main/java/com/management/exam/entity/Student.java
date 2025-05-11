package com.management.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Student {
    private Integer id;
    private String name;
    private String studentId;
    private String password;
    private String gender;
    private String className;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 