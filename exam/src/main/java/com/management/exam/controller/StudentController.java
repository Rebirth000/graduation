package com.management.exam.controller;

import com.management.exam.entity.Student;
import com.management.exam.service.StudentService;
import com.management.exam.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/list")
    public Result<Map<String, Object>> getStudentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email) {
        return Result.success(studentService.getStudentList(page, size, name, studentId, className, phone, email));
    }

    @PostMapping
    public Result<String> addStudent(@RequestBody Student student) {
        try {
            studentService.addStudent(student);
            return Result.success("添加成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<String> updateStudent(@PathVariable Integer id, @RequestBody Student student) {
        student.setId(id);
        studentService.updateStudent(student);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteStudent(@PathVariable Integer id) {
        studentService.deleteStudent(id);
        return Result.success("删除成功");
    }

    @PostMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Integer id) {
        studentService.resetPassword(id);
        return Result.success("密码重置成功");
    }

    @GetMapping("/profile")
    public Result<Student> getProfile() {
        return Result.success(studentService.getProfile());
    }

    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody Student student) {
        studentService.updateProfile(student);
        return Result.success("更新成功");
    }

    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        try {
            studentService.changePassword(oldPassword, newPassword);
            return Result.success("密码更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/batch-import")
    public Result<String> batchImport(@RequestBody List<Student> students) {
        try {
            studentService.batchImport(students);
            return Result.success("批量导入成功");
        }  catch (Exception e) {
            return Result.error("批量导入失败，请检查数据");
        }
    }
} 