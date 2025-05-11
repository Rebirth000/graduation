package com.management.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuestionType {
    private Integer id;
    private String name;
    private String description;
    private  String code;
    private Boolean enabled; // 状态: 1-启用, 0-禁用
    private Boolean isSystemDefault; // 是否为系统默认题型: 1-是, 0-否
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
