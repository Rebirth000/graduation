package com.management.exam.service.impl;

import com.management.exam.entity.Student;
import com.management.exam.mapper.StudentMapper;
import com.management.exam.service.StudentService;
import com.management.exam.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.DigestUtils;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    
    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private static final String DEFAULT_PASSWORD = "123456";
    
    private String getCurrentStudentId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUsername(token);
    }
    
    @Override
    public Map<String, Object> getStudentList(int page, int size, String name, String studentId, String className, String phone, String email) {
        int offset = (page - 1) * size;
        Map<String, Object> params = new HashMap<>();
        params.put("offset", offset);
        params.put("size", size);
        params.put("name", name);
        params.put("studentId", studentId);
        params.put("className", className);
        params.put("phone", phone);
        params.put("email", email);
        
        List<Student> records = studentMapper.selectList(params);
        int total = studentMapper.selectCount(params);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        return result;
    }
    
    @Override
    @Transactional
    public void addStudent(Student student) {
        // 检查学号是否已存在
        if (studentMapper.findByStudentId(student.getStudentId()).isPresent()) {
            throw new RuntimeException("学号已存在");
        }
        
        // 设置默认密码并加密
        student.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        studentMapper.insert(student);
    }
    
    @Override
    @Transactional
    public void updateStudent(Student student) {
        studentMapper.update(student);
    }
    
    @Override
    @Transactional
    public void deleteStudent(Integer id) {
        studentMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void resetPassword(Integer id) {
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);
        if (studentMapper.resetPassword(id, encodedPassword) != 1) {
            throw new RuntimeException("重置密码失败");
        }
    }

    @Override
    public Student getProfile() {
        String studentId = getCurrentStudentId();
        return studentMapper.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("学生信息不存在"));
    }

    @Override
    @Transactional
    public void updateProfile(Student student) {
        String studentId = getCurrentStudentId();
        Student currentStudent = studentMapper.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("学生信息不存在"));
        
        // 只允许修改部分信息
        currentStudent.setName(student.getName());
        currentStudent.setGender(student.getGender());
        currentStudent.setClassName(student.getClassName());
        currentStudent.setPhone(student.getPhone());
        currentStudent.setEmail(student.getEmail());
        
        studentMapper.update(currentStudent);
    }

    @Override
    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        String studentId = getCurrentStudentId();
        Student student = studentMapper.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("学生信息不存在"));
        
        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, student.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 检查新密码是否与原密码相同
        if (passwordEncoder.matches(newPassword, student.getPassword())) {
            throw new RuntimeException("新密码不能与原密码相同");
        }
        
        // 更新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        if (studentMapper.resetPassword(student.getId(), encodedPassword) != 1) {
            throw new RuntimeException("修改密码失败");
        }
    }

    @Override
    public void batchImport(List<Student> students) {
        if (students == null || students.isEmpty()) {
            throw new RuntimeException("导入的学生数据不能为空");
        }
        
        for (Student student : students) {
            // 设置默认密码为123456
            student.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
            studentMapper.insert(student);
        }
    }
} 