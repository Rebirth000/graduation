package com.management.exam.service;

import com.management.exam.dto.ExamExportDTO;
import com.management.exam.dto.ExamHistoryQueryDTO;
import com.management.exam.vo.ExamAnswerDetailVO;
import com.management.exam.vo.ExamHistoryVO;
import com.management.exam.vo.PageResponse;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

public interface ExamHistoryService {
    PageResponse<ExamHistoryVO> getStudentExamHistory(Integer studentId, ExamHistoryQueryDTO query);
    
    List<ExamAnswerDetailVO> getStudentExamDetail(Integer studentId, Integer examId);
    
    /**
     * 导出答卷为Excel
     * @param exportDTO 导出参数
     * @param response HTTP响应对象
     */
    void exportExamAnswersExcel(ExamExportDTO exportDTO, HttpServletResponse response);
} 