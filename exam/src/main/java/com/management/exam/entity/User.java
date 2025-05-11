package com.management.exam.entity;

import com.management.exam.enums.Role;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {

    private Integer id;
    private String username;
    private String password;
    private Role role; // 使用枚举类型
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

} 