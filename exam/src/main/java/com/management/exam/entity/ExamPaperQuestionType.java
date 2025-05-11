package com.management.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamPaperQuestionType {
    private Integer id;
    private Integer paperId; // 试卷ID
    private Integer questionTypeId; // 题型ID
    private LocalDateTime createdAt;
}

