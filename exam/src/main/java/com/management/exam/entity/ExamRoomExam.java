package com.management.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamRoomExam {
    private Integer id;
    private Integer examId;
    private Integer examRoomId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

} 