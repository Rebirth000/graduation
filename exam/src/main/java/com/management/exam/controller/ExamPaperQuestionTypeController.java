package com.management.exam.controller;

import com.management.exam.entity.ExamPaperQuestionType;
import com.management.exam.service.ExamPaperQuestionTypeService;
import com.management.exam.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-paper-question-type")
@RequiredArgsConstructor
public class ExamPaperQuestionTypeController {

    private final ExamPaperQuestionTypeService examPaperQuestionTypeService;

    @GetMapping("/exam-paper/{examPaperId}")
    public Result<List<ExamPaperQuestionType>> findByExamPaperId(@PathVariable Integer examPaperId) {
        return Result.success(examPaperQuestionTypeService.findByExamPaperId(examPaperId));
    }

    @GetMapping("/{id}")
    public Result<ExamPaperQuestionType> findById(@PathVariable Integer id) {
        return Result.success(examPaperQuestionTypeService.findById(id));
    }

    @PostMapping
    public Result<ExamPaperQuestionType> create(@RequestBody ExamPaperQuestionType examPaperQuestionType) {
        return Result.success(examPaperQuestionTypeService.create(examPaperQuestionType));
    }

    @PostMapping("/batch")
    public Result<String> batchCreate(@RequestBody List<ExamPaperQuestionType> list) {
        examPaperQuestionTypeService.batchCreate(list);
        return Result.success("");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody ExamPaperQuestionType examPaperQuestionType) {
        examPaperQuestionType.setId(id);
        examPaperQuestionTypeService.update(examPaperQuestionType);
        return Result.success("");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteById(@PathVariable Integer id) {
        examPaperQuestionTypeService.deleteById(id);
        return Result.success("");
    }

    @DeleteMapping("/exam-paper/{examPaperId}")
    public Result<String> deleteByExamPaperId(@PathVariable Integer examPaperId) {
        examPaperQuestionTypeService.deleteByExamPaperId(examPaperId);
        return Result.success("");
    }
} 