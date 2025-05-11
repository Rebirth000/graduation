package com.management.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamAnswer {
    private Long id;
    private Integer examId;
    private Integer studentId;
    private Long questionId;
    private String answer;
    private String answerImages;  // JSON格式存储图片URL列表
    private Integer score;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 