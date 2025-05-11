package com.management.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamRegistration {
    private Integer id;
    private Integer studentId;
    private Integer examId;
    private Integer examRoomId;
    private Integer seatNumber;
    private String status;
    private LocalDateTime registerTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 非数据库字段，用于展示
    private String examName;
    private String examRoomName;
    private LocalDateTime examStartTime;
    private LocalDateTime examEndTime;
    private String examRoomLocation;
}