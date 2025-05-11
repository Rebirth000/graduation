package com.management.exam.controller;

import com.management.exam.entity.User;
import com.management.exam.service.AdminService;
import com.management.exam.vo.PageResponse;
import com.management.exam.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/list")
    public Result<PageResponse<User>> getAdminList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminService.getAdminList(page, size));
    }

    @PostMapping
    public Result<String> addAdmin(@RequestBody Map<String, String> request) {
        try {
            adminService.addAdmin(request.get("username"), request.get("password"));
            return Result.success("");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteAdmin(@PathVariable Integer id) {
        adminService.deleteAdmin(id);
        return Result.success("");
    }

    @PostMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Integer id) {
        adminService.resetPassword(id);
        return Result.success("");
    }
} 