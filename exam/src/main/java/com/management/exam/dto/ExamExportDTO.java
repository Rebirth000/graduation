package com.management.exam.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExamExportDTO {
    // 导出的考试记录列表
    private List<ExamRecordDTO> examRecords;
    
    // 要导出的题型
    private List<String> questionTypes;
    
    // 是否包含图片
    private Boolean includeImages;
    
    @Data
    public static class ExamRecordDTO {
        private Integer examId;
        private Integer studentId;
    }
} 