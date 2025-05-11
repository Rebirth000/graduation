package com.management.exam.controller;

import com.management.exam.entity.ExamRoom;
import com.management.exam.service.ExamRoomService;
import com.management.exam.vo.PageResponse;
import com.management.exam.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-room")
public class ExamRoomController {

    @Autowired
    private ExamRoomService examRoomService;

    @GetMapping("/list")
    public Result<PageResponse<ExamRoom>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(examRoomService.getList(page, size));
    }

    @GetMapping
    public Result<List<ExamRoom>> getExamRoomList() {
        return Result.success(examRoomService.getExamRoomList());
    }

    @GetMapping("/{id}")
    public Result<ExamRoom> getExamRoomById(@PathVariable Integer id) {
        return Result.success(examRoomService.getExamRoomById(id));
    }

    @PostMapping
    public Result<String> addExamRoom(@RequestBody ExamRoom examRoom) {
        examRoomService.addExamRoom(examRoom);
        return Result.success("");
    }

    @PutMapping("/{id}")
    public Result<String> updateExamRoom(@PathVariable Integer id, @RequestBody ExamRoom examRoom) {
        examRoomService.updateExamRoom(id, examRoom);
        return Result.success("");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteExamRoom(@PathVariable Integer id) {
        examRoomService.deleteExamRoom(id);
        return Result.success("");
    }
} 