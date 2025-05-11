package com.management.exam.service;

import com.management.exam.entity.User;
import com.management.exam.vo.PageResponse;

public interface AdminService {
    PageResponse<User> getAdminList(Integer page, Integer size);
    
    void addAdmin(String username, String password);
    
    void deleteAdmin(Integer id);
    
    void resetPassword(Integer id);
} 