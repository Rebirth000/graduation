package com.management.exam.service.impl;

import com.management.exam.entity.User;
import com.management.exam.enums.Role;
import com.management.exam.mapper.UserMapper;
import com.management.exam.service.AdminService;
import com.management.exam.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    public PageResponse<User> getAdminList(Integer page, Integer size) {
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 获取总记录数
        long total = userMapper.countAdmins();
        
        // 获取当前页数据
        List<User> records = userMapper.findAdminsByPage(offset, size);
        
        // 返回分页结果
        return new PageResponse<>(records, total, page, size);
    }

    @Override
    @Transactional
    public void addAdmin(String username, String password) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新管理员
        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);

        if (userMapper.insert(admin) != 1) {
            throw new RuntimeException("添加管理员失败");
        }
    }

    @Override
    @Transactional
    public void deleteAdmin(Integer id) {
        // 检查是否为超级管理员
        User admin = userMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
                
        if (admin.getRole() == Role.SUPER_ADMIN) {
            throw new RuntimeException("不能删除超级管理员");
        }

        if (userMapper.deleteById(id) != 1) {
            throw new RuntimeException("删除管理员失败");
        }
    }

    @Override
    @Transactional
    public void resetPassword(Integer id) {
        // 检查是否为超级管理员
        User admin = userMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
                
        if (admin.getRole() == Role.SUPER_ADMIN) {
            throw new RuntimeException("不能重置超级管理员密码");
        }

        // 重置密码
        admin.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        
        if (userMapper.update(admin) != 1) {
            throw new RuntimeException("重置密码失败");
        }
    }
} 