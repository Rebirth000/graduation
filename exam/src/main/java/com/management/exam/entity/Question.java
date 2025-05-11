package com.management.exam.entity;

import com.management.exam.enums.QuestionType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Question {
    private Integer id;
    private Integer paperId;
    /**
     * @see QuestionType
     */
    private String type;
    private String title;
    private String options;  // JSON格式存储选项
    private String imageUrl; // 题目图片URL
    private Integer score;
    private Integer orderNum; // 题目顺序
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 