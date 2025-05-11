package com.management.exam.service.impl;

import com.management.exam.entity.ExamRoomExam;
import com.management.exam.mapper.ExamRoomExamMapper;
import com.management.exam.service.ExamRoomExamService;
import com.management.exam.vo.ExamRoomExamVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExamRoomExamServiceImpl implements ExamRoomExamService {

    @Autowired
    private ExamRoomExamMapper examRoomExamMapper;

    @Override
    public List<ExamRoomExam> getByExamRoomId(Integer examRoomId) {
        return examRoomExamMapper.findByExamRoomId(examRoomId);
    }

    @Override
    public List<ExamRoomExamVO> getDetailsByExamRoomId(Integer examRoomId) {
        return examRoomExamMapper.findDetailsByExamRoomId(examRoomId);
    }

    @Override
    public List<ExamRoomExam> getByExamId(Integer examId) {
        return examRoomExamMapper.findByExamId(examId);
    }

    @Override
    public ExamRoomExam getById(Integer id) {
        return examRoomExamMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("考场考试关联记录不存在"));
    }

    @Override
    @Transactional
    public void add(ExamRoomExam examRoomExam) {
        // 这里可以添加业务验证逻辑，比如检查考场和考试是否存在，考场容量是否足够等
        int rows = examRoomExamMapper.insert(examRoomExam);
        if (rows != 1) {
            throw new RuntimeException("添加考场考试关联失败");
        }
    }

    @Override
    @Transactional
    public void update(ExamRoomExam examRoomExam) {
        // 这里可以添加业务验证逻辑
        int rows = examRoomExamMapper.update(examRoomExam);
        if (rows != 1) {
            throw new RuntimeException("更新考场考试关联失败");
        }
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        int rows = examRoomExamMapper.deleteById(id);
        if (rows != 1) {
            throw new RuntimeException("删除考场考试关联失败");
        }
    }

    @Override
    @Transactional
    public void deleteByExamRoomId(Integer examRoomId) {
        examRoomExamMapper.deleteByExamRoomId(examRoomId);
    }

    @Override
    @Transactional
    public void deleteByExamId(Integer examId) {
        examRoomExamMapper.deleteByExamId(examId);
    }
} 