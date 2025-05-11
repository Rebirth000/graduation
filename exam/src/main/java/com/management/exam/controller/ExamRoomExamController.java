package com.management.exam.controller;

import com.management.exam.entity.ExamRoomExam;
import com.management.exam.service.ExamRoomExamService;
import com.management.exam.vo.ExamRoomExamVO;
import com.management.exam.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-room-exam")
public class ExamRoomExamController {

    @Autowired
    private ExamRoomExamService examRoomExamService;

    /**
     * 根据考场ID获取关联的考试详细信息
     */
    @GetMapping("/room/{examRoomId}")
    public Result<List<ExamRoomExamVO>> getByExamRoomId(@PathVariable Integer examRoomId) {
        return Result.success(examRoomExamService.getDetailsByExamRoomId(examRoomId));
    }

    /**
     * 根据考试ID获取关联的考场信息
     */
    @GetMapping("/exam/{examId}")
    public Result<List<ExamRoomExam>> getByExamId(@PathVariable Integer examId) {
        return Result.success(examRoomExamService.getByExamId(examId));
    }

    /**
     * 根据ID获取考场考试关联信息
     */
    @GetMapping("/{id}")
    public Result<ExamRoomExam> getById(@PathVariable Integer id) {
        return Result.success(examRoomExamService.getById(id));
    }

    /**
     * 添加考场考试关联
     */
    @PostMapping
    public Result<String> add(@RequestBody ExamRoomExam examRoomExam) {
        examRoomExamService.add(examRoomExam);
        return Result.success("");
    }

    /**
     * 更新考场考试关联
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody ExamRoomExam examRoomExam) {
        examRoomExam.setId(id);
        examRoomExamService.update(examRoomExam);
        return Result.success("");
    }

    /**
     * 删除考场考试关联
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteById(@PathVariable Integer id) {
        examRoomExamService.deleteById(id);
        return Result.success("");
    }

    /**
     * 删除考场的所有关联考试
     */
    @DeleteMapping("/room/{examRoomId}")
    public Result<String> deleteByExamRoomId(@PathVariable Integer examRoomId) {
        examRoomExamService.deleteByExamRoomId(examRoomId);
        return Result.success("");
    }

    /**
     * 删除考试的所有关联考场
     */
    @DeleteMapping("/exam/{examId}")
    public Result<String> deleteByExamId(@PathVariable Integer examId) {
        examRoomExamService.deleteByExamId(examId);
        return Result.success("");
    }
} 