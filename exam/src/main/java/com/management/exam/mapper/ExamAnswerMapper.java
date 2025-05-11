package com.management.exam.mapper;

import com.management.exam.entity.ExamAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamAnswerMapper {
    /**
     * 批量插入答案
     */
    int batchInsert(@Param("answers") List<ExamAnswer> answers);

    /**
     * 根据考试ID和学生ID查询答案
     */
    List<ExamAnswer> findByExamAndStudent(@Param("examId") Integer examId, @Param("studentId") Integer studentId);
} 