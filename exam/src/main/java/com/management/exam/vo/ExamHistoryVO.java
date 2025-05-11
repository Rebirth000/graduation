package com.management.exam.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamHistoryVO {
    private Integer examId;
    private String examName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String paperTitle;
    private String status;  // 未开始、进行中、已完成
    private Integer totalScore;  // 总分
    private Integer studentScore;  // 学生得分
    private String examRoom;  // 考场信息
    private String examRoomName;
    private String examRoomLocation;
    private Integer seatNumber;  // 座位号
    private Integer studentId;  // 学生ID
    private String studentName;  // 学生姓名
}