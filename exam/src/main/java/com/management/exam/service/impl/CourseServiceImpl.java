package com.management.exam.service.impl;

import com.management.exam.entity.Course;
import com.management.exam.entity.ExamPaper;
import com.management.exam.mapper.CourseMapper;
import com.management.exam.mapper.ExamPaperMapper;
import com.management.exam.service.CourseService;
import com.management.exam.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final ExamPaperMapper examPaperMapper;

    @Override
    public PageResponse<Course> getList(Integer page, Integer size) {
        int offset = (page - 1) * size;
        long total = courseMapper.count();
        List<Course> records = courseMapper.findByPage(offset, size);
        return new PageResponse<>(records, total, page, size);
    }

    @Override
    public List<Course> findAll() {
        return courseMapper.findAll();
    }

    @Override
    public Course findById(Integer id) {
        return courseMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
    }

    @Override
    @Transactional
    public Course create(Course course) {
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());
        courseMapper.insert(course);
        return course;
    }

    @Override
    @Transactional
    public void update(Course course) {
        findById(course.getId()); // 检查是否存在
        course.setUpdateTime(LocalDateTime.now());
        courseMapper.update(course);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        findById(id); // 检查是否存在
        courseMapper.deleteById(id);
    }

    @Override
    public List<ExamPaper> getAssociatedPapers(Integer courseId) {
        findById(courseId); // 检查课程是否存在
        return examPaperMapper.findByCourseId(courseId);
    }

    @Override
    @Transactional
    public void associatePaper(Integer courseId, Integer paperId) {
        findById(courseId); // 检查课程是否存在
        ExamPaper paper = examPaperMapper.findById(paperId)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));
        
        // 检查试卷是否已关联到其他课程
        if (paper.getCourseId() != null) {
            throw new RuntimeException("该试卷已关联到其他课程");
        }
        
        // 更新试卷的课程ID
        paper.setCourseId(courseId);
        examPaperMapper.update(paper);
    }

    @Override
    @Transactional
    public void dissociatePaper(Integer courseId, Integer paperId) {
        findById(courseId); // 检查课程是否存在
        ExamPaper paper = examPaperMapper.findById(paperId)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));
        
        // 检查试卷是否属于该课程
        if (!courseId.equals(paper.getCourseId())) {
            throw new RuntimeException("该试卷不属于此课程");
        }
        
        // 解除试卷与课程的关联
        paper.setCourseId(null);
        examPaperMapper.update(paper);
    }
} 