package com.management.exam.service.impl;

import com.management.exam.dto.LoginRequest;
import com.management.exam.dto.LoginResponse;
import com.management.exam.entity.Student;
import com.management.exam.entity.User;
import com.management.exam.enums.Role;
import com.management.exam.mapper.StudentMapper;
import com.management.exam.mapper.UserMapper;
import com.management.exam.service.AuthService;
import com.management.exam.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Role role = Role.valueOf(loginRequest.getRole());
        
        // 学生登录逻辑
        if (role == Role.STUDENT) {
            Optional<Student> studentOptional = studentMapper.findByStudentId(loginRequest.getUsername());
            
            // 检查学生是否存在
            if (studentOptional.isEmpty()) {
                throw new RuntimeException("学号不存在");
            }

            Student student = studentOptional.get();
            
            // 检查密码是否正确
            if (!passwordEncoder.matches(loginRequest.getPassword(), student.getPassword())) {
                throw new RuntimeException("密码错误");
            }

            // 生成token并返回
            String token = jwtUtil.generateStudentToken(student);
            return new LoginResponse(student.getStudentId(), Role.STUDENT, token, student.getId());
        }
        
        // 管理员登录逻辑
        Optional<User> userOptional = userMapper.findByUsername(loginRequest.getUsername());

        // 检查用户是否存在
        if (userOptional.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOptional.get();
        
        // 检查密码是否正确
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 检查角色是否匹配
        if (user.getRole() != role) {
            throw new RuntimeException("用户角色不匹配");
        }

        // 生成token并返回
        String token = jwtUtil.generateToken(user);
        return new LoginResponse(user.getUsername(), user.getRole(), token, user.getId());
    }
} 