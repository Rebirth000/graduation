package com.management.exam.service;

import com.management.exam.entity.Student;
import java.util.Map;
import java.util.List;

public interface StudentService {
    Map<String, Object> getStudentList(int page, int size, String name, String studentId, String className, String phone, String email);
    
    void addStudent(Student student);
    
    void updateStudent(Student student);
    
    void deleteStudent(Integer id);

    void resetPassword(Integer id);

    Student getProfile();

    void updateProfile(Student student);

    void changePassword(String oldPassword, String newPassword);

    void batchImport(List<Student> students);
} 