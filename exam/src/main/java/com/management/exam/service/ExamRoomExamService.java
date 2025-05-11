package com.management.exam.service;

import com.management.exam.entity.ExamRoomExam;
import com.management.exam.vo.ExamRoomExamVO;
import java.util.List;

public interface ExamRoomExamService {
    /**
     * 根据考场ID查询关联的考试信息
     */
    List<ExamRoomExam> getByExamRoomId(Integer examRoomId);

    /**
     * 根据考场ID查询关联的考试详细信息（包含考场和考试信息）
     */
    List<ExamRoomExamVO> getDetailsByExamRoomId(Integer examRoomId);

    /**
     * 根据考试ID查询关联的考场信息
     */
    List<ExamRoomExam> getByExamId(Integer examId);

    /**
     * 根据ID查询考场考试关联信息
     */
    ExamRoomExam getById(Integer id);

    /**
     * 添加考场考试关联
     */
    void add(ExamRoomExam examRoomExam);

    /**
     * 更新考场考试关联
     */
    void update(ExamRoomExam examRoomExam);

    /**
     * 删除考场考试关联
     */
    void deleteById(Integer id);

    /**
     * 删除考场的所有关联考试
     */
    void deleteByExamRoomId(Integer examRoomId);

    /**
     * 删除考试的所有关联考场
     */
    void deleteByExamId(Integer examId);
} 