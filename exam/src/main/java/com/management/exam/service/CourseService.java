package com.management.exam.service;

import com.management.exam.entity.Course;
import com.management.exam.entity.ExamPaper;
import com.management.exam.vo.PageResponse;
import java.util.List;

public interface CourseService {
    /**
     * 获取课程列表（分页）
     */
    PageResponse<Course> getList(Integer page, Integer size);

    /**
     * 获取所有课程
     */
    List<Course> findAll();
    
    /**
     * 根据ID获取课程
     */
    Course findById(Integer id);
    
    /**
     * 创建课程
     */
    Course create(Course course);
    
    /**
     * 更新课程
     */
    void update(Course course);
    
    /**
     * 删除课程
     */
    void deleteById(Integer id);

    /**
     * 获取课程关联的试卷
     */
    List<ExamPaper> getAssociatedPapers(Integer courseId);

    /**
     * 添加试卷到课程
     */
    void associatePaper(Integer courseId, Integer paperId);

    /**
     * 从课程移除试卷
     */
    void dissociatePaper(Integer courseId, Integer paperId);
} 