package com.management.exam.controller;

import com.management.exam.entity.ExamRegistration;
import com.management.exam.service.ExamRegistrationService;
import com.management.exam.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam/registration")
@RequiredArgsConstructor
public class ExamRegistrationController {

    @Autowired
    private ExamRegistrationService examRegistrationService;

    @GetMapping("/available")
    public Result<Map<String, Object>> getAvailableExams(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(examRegistrationService.getAvailableExams(page, size, keyword));
    }

    @PostMapping("/{examId}")
    public Result<String> register(@PathVariable Integer examId) {
        try {
            examRegistrationService.register(examId);
            return Result.success("");
        } catch (RuntimeException e){
            return Result.error(e.getMessage());
        }

    }

    @GetMapping("/my")
    public Result<Map<String, Object>> getMyRegistrations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(examRegistrationService.getMyRegistrations(page, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<ExamRegistration> getRegistrationDetail(@PathVariable Integer id) {
        return Result.success(examRegistrationService.getRegistrationDetail(id));
    }

    @DeleteMapping("/{id}")
    public Result<String> cancelRegistration(@PathVariable Integer id) {
        try {
            examRegistrationService.cancelRegistration(id);
            return Result.success("");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/enter")
    public Result<String> enterExam(@PathVariable Integer id) {
        examRegistrationService.enterExam(id);
        return Result.success("");
    }

    @GetMapping("/{id}/detail")
    public Result<Map<String, Object>> getExamDetail(@PathVariable Integer id) {
        try {
            Map<String, Object> detail = examRegistrationService.getExamDetail(id);
            return Result.success(detail);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/submit")
    public Result<String> submitExam(@PathVariable Integer id, @RequestBody Map<String, Object> answers) {
        examRegistrationService.submitExam(id, answers);
        return Result.success("");
    }
} 