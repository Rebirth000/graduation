package com.management.exam.mapper;

import com.management.exam.entity.ExamRoom;
import com.management.exam.entity.ExamRoomExam;
import com.management.exam.vo.ExamRoomExamVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ExamRoomExamMapper {
    List<ExamRoomExam> findByExamRoomId(@Param("examRoomId") Integer examRoomId);
    
    List<ExamRoomExamVO> findDetailsByExamRoomId(@Param("examRoomId") Integer examRoomId);
    
    List<ExamRoomExam> findByExamId(@Param("examId") Integer examId);
    
    Optional<ExamRoomExam> findById(@Param("id") Integer id);
    
    int insert(ExamRoomExam examRoomExam);
    
    int update(ExamRoomExam examRoomExam);
    
    int deleteById(@Param("id") Integer id);
    
    int deleteByExamRoomId(@Param("examRoomId") Integer examRoomId);
    
    int deleteByExamId(@Param("examId") Integer examId);

    /**
     * 获取考试关联的所有考场
     */
    List<ExamRoom> findExamRoomsByExamId(@Param("examId") Integer examId);
} 