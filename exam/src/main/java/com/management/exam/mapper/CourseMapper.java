package com.management.exam.mapper;

import com.management.exam.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface CourseMapper {
    /**
     * 查询所有课程
     */
    List<Course> findAll();
    
    /**
     * 分页查询课程列表
     */
    List<Course> findByPage(@Param("offset") int offset, @Param("size") int size);
    
    /**
     * 统计课程总数
     */
    long count();
    
    /**
     * 根据ID查询课程
     */
    Optional<Course> findById(@Param("id") Integer id);
    
    /**
     * 插入课程
     */
    int insert(Course course);
    
    /**
     * 更新课程
     */
    int update(Course course);
    
    /**
     * 删除课程
     */
    int deleteById(@Param("id") Integer id);
} 