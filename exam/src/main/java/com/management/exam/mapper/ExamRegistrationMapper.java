package com.management.exam.mapper;

import com.management.exam.entity.ExamRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ExamRegistrationMapper {
    
    // 插入报名记录
    void insert(ExamRegistration registration);
    
    // 更新报名记录
    void update(ExamRegistration registration);
    
    // 获取学生的所有报名记录
    List<ExamRegistration> findByStudentId(Integer studentId);
    
    // 获取考试的所有报名记录
    List<ExamRegistration> findByExamId(Integer examId);
    
    // 获取考场的所有报名记录（用于座位分配）
    List<ExamRegistration> findByExamAndRoom(@Param("examId") Integer examId, 
                                           @Param("examRoomId") Integer examRoomId,
                                           @Param("status") String status);
    
    // 检查学生是否已报名某考试
    ExamRegistration findByStudentAndExam(@Param("studentId") Integer studentId, 
                                        @Param("examId") Integer examId);
    
    // 取消报名
    void updateStatus(@Param("id") Integer id, @Param("status") String status);

    // 根据ID查询报名记录
    ExamRegistration findById(@Param("id") Integer id);

    List<ExamRegistration> findByStudentIdWithPaging(@Param("studentId") Integer studentId,
                                                    @Param("offset") Integer offset,
                                                    @Param("size") Integer size,
                                                    @Param("keyword") String keyword);
    
    int countByStudentId(@Param("studentId") Integer studentId,
                        @Param("keyword") String keyword);

    int deleteByExamId(@Param("examId") Integer examId);
} 