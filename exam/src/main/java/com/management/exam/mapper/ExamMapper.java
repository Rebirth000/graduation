package com.management.exam.mapper;

import com.management.exam.entity.Exam;
import com.management.exam.dto.ExamHistoryQueryDTO;
import com.management.exam.vo.ExamAnswerDetailVO;
import com.management.exam.vo.ExamHistoryVO;
import com.management.exam.vo.ExamUpdateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ExamMapper {
    /**
     * 分页查询考试列表
     */
    List<Exam> findByPage(@Param("offset") int offset, @Param("size") int size);

    /**
     * 统计考试总数
     */
    long count();

    /**
     * 根据ID查询考试
     */
    Optional<Exam> findById(@Param("id") Integer id);
    
    /**
     * 根据试卷ID查询考试
     */
    List<Exam> findByPaperId(@Param("paperId") Integer paperId);
    
    /**
     * 插入考试
     */
    int insert(Exam exam);

    /**
     * 删除考试
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 查询可报名的考试列表
     */
    List<Exam> selectAvailableExams(@Param("offset") int offset, 
                                  @Param("size") int size, 
                                  @Param("now") LocalDateTime now,
                                  @Param("keyword") String keyword,
                                  @Param("studentId") Integer studentId);

    /**
     * 统计可报名的考试总数
     */ 
    int countAvailableExams(@Param("now") LocalDateTime now,
                           @Param("keyword") String keyword);

    long countStudentExams(@Param("studentId") Integer studentId, @Param("query") ExamHistoryQueryDTO query);
    
    List<ExamHistoryVO> findStudentExams(@Param("studentId") Integer studentId, 
                                        @Param("query") ExamHistoryQueryDTO query,
                                        @Param("offset") Integer offset, 
                                        @Param("pageSize") Integer pageSize);
    
    List<ExamAnswerDetailVO> findExamAnswerDetail(@Param("studentId") Integer studentId, 
                                                 @Param("examId") Integer examId);

    /**
     * 更新考试信息
     */
    int update(Exam exam);

    /**
     * 获取考试详细信息
     */
    ExamUpdateVO getExamDetailById(Integer id);
} 