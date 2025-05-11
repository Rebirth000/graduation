package com.management.exam.mapper;

import com.management.exam.entity.ExamPaperQuestionType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ExamPaperQuestionTypeMapper {
    List<ExamPaperQuestionType> findByExamPaperId(@Param("paperId") Integer paperId);
    
    Optional<ExamPaperQuestionType> findById(@Param("id") Integer id);
    
    int insert(ExamPaperQuestionType examPaperQuestionType);
    
    int batchInsert(@Param("list") List<ExamPaperQuestionType> list);
    
    int update(ExamPaperQuestionType examPaperQuestionType);
    
    int deleteById(@Param("id") Integer id);
    
    int deleteByExamPaperId(@Param("paperId") Integer examPaperId);
} 