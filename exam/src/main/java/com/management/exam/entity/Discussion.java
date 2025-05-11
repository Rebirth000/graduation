package com.management.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Discussion {
    private Integer id;
    private String title;
    private String content;
    private LocalDateTime deadline;
    private Integer createdBy;
    private String creatorRole;
    private String creatorName;  // 创建人姓名，非数据库字段
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 