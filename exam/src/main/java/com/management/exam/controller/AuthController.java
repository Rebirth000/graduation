package com.management.exam.controller;

import com.management.exam.dto.LoginRequest;
import com.management.exam.dto.LoginResponse;
import com.management.exam.service.AuthService;
import com.management.exam.vo.Result;
import com.management.exam.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/check-token")
    public Result<String> checkToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return Result.error(401, "Token已过期或无效");
            }
            return Result.success("");
        }
        return Result.error(401, "未提供Token");
    }
} 