package com.management.exam.service;

import com.management.exam.dto.LoginRequest;
import com.management.exam.dto.LoginResponse;

import java.util.Optional;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

} 