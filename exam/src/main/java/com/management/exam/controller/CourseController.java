package com.management.exam.controller;

import com.management.exam.entity.Course;
import com.management.exam.entity.ExamPaper;
import com.management.exam.service.CourseService;
import com.management.exam.service.ExamPaperService;
import com.management.exam.vo.PageResponse;
import com.management.exam.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final ExamPaperService examPaperService;

    @GetMapping("/list")
    public Result<PageResponse<Course>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(courseService.getList(page, size));
    }

    @GetMapping
    public Result<List<Course>> findAll() {
        return Result.success(courseService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Course> findById(@PathVariable Integer id) {
        return Result.success(courseService.findById(id));
    }

    @PostMapping
    public Result<Course> create(@RequestBody Course course) {
        return Result.success(courseService.create(course));
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody Course course) {
        course.setId(id);
        courseService.update(course);
        return Result.success("");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteById(@PathVariable Integer id) {
        courseService.deleteById(id);
        return Result.success("");
    }

    @GetMapping("/{id}/exam-papers")
    public Result<List<ExamPaper>> getAssociatedPapers(@PathVariable Integer id) {
        return Result.success(courseService.getAssociatedPapers(id));
    }

    @PostMapping("/{courseId}/exam-paper/{paperId}")
    public Result<String> associatePaper(
            @PathVariable Integer courseId,
            @PathVariable Integer paperId) {
        courseService.associatePaper(courseId, paperId);
        return Result.success("");
    }

    @DeleteMapping("/{courseId}/exam-paper/{paperId}")
    public Result<String> dissociatePaper(
            @PathVariable Integer courseId,
            @PathVariable Integer paperId) {
        courseService.dissociatePaper(courseId, paperId);
        return Result.success("");
    }
} 