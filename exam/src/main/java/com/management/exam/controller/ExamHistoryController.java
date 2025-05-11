package com.management.exam.controller;

import com.management.exam.dto.ExamExportDTO;
import com.management.exam.dto.ExamHistoryQueryDTO;
import com.management.exam.service.ExamHistoryService;
import com.management.exam.utils.JwtUtil;
import com.management.exam.vo.ExamAnswerDetailVO;
import com.management.exam.vo.ExamHistoryVO;
import com.management.exam.vo.PageResponse;
import com.management.exam.vo.Result;
import com.management.exam.mapper.StudentMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@RestController
@RequestMapping("/api/student/exam-history")
@RequiredArgsConstructor
public class ExamHistoryController {
    
    private final ExamHistoryService examHistoryService;
    private final JwtUtil jwtUtil;
    private final StudentMapper studentMapper;

    private Integer getCurrentStudentId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization").substring(7);
        String studentNumber = jwtUtil.extractUsername(token);
        return studentMapper.findByStudentId(studentNumber)
            .orElseThrow(() -> new RuntimeException("学生不存在"))
            .getId();
    }

    @GetMapping
    public Result<PageResponse<ExamHistoryVO>> getExamHistory(ExamHistoryQueryDTO query) {
        Integer studentId = getCurrentStudentId();
        return Result.success(examHistoryService.getStudentExamHistory(studentId, query));
    }

    @GetMapping("/admin")
    public Result<PageResponse<ExamHistoryVO>> getExamHistory2(ExamHistoryQueryDTO query) {
        return Result.success(examHistoryService.getStudentExamHistory(null, query));
    }

    @GetMapping("/{examId}")
    public Result<List<ExamAnswerDetailVO>> getExamDetail(@PathVariable Integer examId) {
        Integer studentId = getCurrentStudentId();
        return Result.success(examHistoryService.getStudentExamDetail(studentId, examId));
    }

    @GetMapping("/detail/admin/{examId}/{studentId}")
    public Result<List<ExamAnswerDetailVO>> getExamDetail2(@PathVariable Integer examId, @PathVariable Integer studentId) {
        return Result.success(examHistoryService.getStudentExamDetail(studentId, examId));
    }
    
    /**
     * 导出答卷为Excel
     */
    @PostMapping("/export/excel")
    public void exportExamAnswersExcel(@RequestBody ExamExportDTO exportDTO, HttpServletResponse response) {
        examHistoryService.exportExamAnswersExcel(exportDTO, response);
    }
} 