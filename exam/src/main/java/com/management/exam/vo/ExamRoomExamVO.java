package com.management.exam.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamRoomExamVO {
    private Integer id;
    private Integer examId;
    private Integer examRoomId;
    private String examName;
    private LocalDateTime examStartTime;
    private LocalDateTime examEndTime;
    private String roomName;
    private String roomLocation;
    private Integer roomCapacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 