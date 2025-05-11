package com.management.exam.controller;

import com.management.exam.entity.Exam;
import com.management.exam.service.ExamService;
import com.management.exam.vo.PageResponse;
import com.management.exam.vo.Result;
import com.management.exam.vo.ExamUpdateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @GetMapping
    public Result<PageResponse<Exam>> getExamList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(examService.getExamList(page, size));
    }

    @GetMapping("/{id}")
    public Result<Exam> getExamById(@PathVariable Integer id) {
        return Result.success(examService.getExamById(id));
    }

    @GetMapping("/paper/{paperId}")
    public Result<List<Exam>> getExamsByPaperId(@PathVariable Integer paperId) {
        return Result.success(examService.getExamsByPaperId(paperId));
    }

    @PostMapping
    public Result<String> addExam(@RequestBody Exam exam) {
        examService.addExam(exam);
        return Result.success("");
    }

    @PutMapping("/{id}")
    public Result<ExamUpdateVO> updateExam(@PathVariable Integer id, @RequestBody Exam exam) {
        return Result.success(examService.updateExam(id, exam));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteExam(@PathVariable Integer id) {
        examService.deleteExam(id);
        return Result.success("");
    }
} 