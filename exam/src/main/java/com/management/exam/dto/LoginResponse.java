package com.management.exam.dto;

import com.management.exam.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String username;
    private Role role;
    private String token;
    private Integer userId;
} 