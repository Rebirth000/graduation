package com.management.exam.service.impl;

import com.management.exam.entity.Exam;
import com.management.exam.entity.ExamRegistration;
import com.management.exam.mapper.ExamMapper;
import com.management.exam.mapper.ExamRegistrationMapper;
import com.management.exam.service.ExamService;
import com.management.exam.vo.PageResponse;
import com.management.exam.vo.ExamUpdateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamRegistrationMapper examRegistrationMapper;

    @Override
    public PageResponse<Exam> getExamList(Integer page, Integer size) {
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 获取总记录数
        long total = examMapper.count();
        
        // 获取当前页数据
        List<Exam> records = examMapper.findByPage(offset, size);
        
        // 返回分页结果
        return new PageResponse<>(records, total, page, size);
    }

    @Override
    public Exam getExamById(Integer id) {
        return examMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("考试不存在"));
    }

    @Override
    public List<Exam> getExamsByPaperId(Integer paperId) {
        return examMapper.findByPaperId(paperId);
    }

    @Override
    @Transactional
    public void addExam(Exam exam) {
        // Validate course exists
        if (exam.getCourseId() == null) {
            throw new RuntimeException("课程ID不能为空");
        }
        
        int rows = examMapper.insert(exam);
        if (rows != 1) {
            throw new RuntimeException("添加考试失败");
        }
    }

    @Override
    @Transactional
    public ExamUpdateVO updateExam(Integer id, Exam exam) {
        // Validate course exists
        if (exam.getCourseId() == null) {
            throw new RuntimeException("课程ID不能为空");
        }
        
        exam.setId(id);
        // 先执行更新操作
        int rows = examMapper.update(exam);
        if (rows != 1) {
            throw new RuntimeException("更新考试失败");
        }
        // 再获取更新后的详细信息
        ExamUpdateVO result = examMapper.getExamDetailById(id);
        if (result == null) {
            throw new RuntimeException("获取更新后的考试信息失败");
        }
        return result;
    }

    @Override
    @Transactional
    public void deleteExam(Integer id) {
        int rows = examMapper.deleteById(id);
        if (rows != 1) {
            throw new RuntimeException("删除考试失败");
        }
        examRegistrationMapper.deleteByExamId(id);
    }
} 