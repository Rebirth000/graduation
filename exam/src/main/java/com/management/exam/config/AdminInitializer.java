package com.management.exam.config;

import com.management.exam.entity.User;
import com.management.exam.enums.Role;
import com.management.exam.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 检查超级管理员是否已存在
        if (userMapper.findByUsername("admin").isEmpty()) {
            // 创建超级管理员账号
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(Role.SUPER_ADMIN);
            
            userMapper.insert(admin);
            System.out.println("超级管理员账号已创建");
        }
    }
} 