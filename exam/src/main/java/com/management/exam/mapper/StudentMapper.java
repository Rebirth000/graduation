package com.management.exam.mapper;

import com.management.exam.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface StudentMapper {
    List<Student> selectList(Map<String, Object> params);
    
    int selectCount(Map<String, Object> params);
    
    int insert(Student student);
    
    int update(Student student);
    
    int deleteById(@Param("id") Integer id);
    
    Student selectById(@Param("id") Integer id);

    int resetPassword(@Param("id") Integer id, @Param("password") String password);

    Optional<Student> findByStudentId(@Param("studentId") String studentId);
} 