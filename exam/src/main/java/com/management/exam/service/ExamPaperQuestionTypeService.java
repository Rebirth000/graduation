package com.management.exam.service;

import com.management.exam.entity.ExamPaperQuestionType;
import java.util.List;

public interface ExamPaperQuestionTypeService {
    
    List<ExamPaperQuestionType> findByExamPaperId(Integer examPaperId);
    
    ExamPaperQuestionType findById(Integer id);
    
    ExamPaperQuestionType create(ExamPaperQuestionType examPaperQuestionType);
    
    void batchCreate(List<ExamPaperQuestionType> list);
    
    void update(ExamPaperQuestionType examPaperQuestionType);
    
    void deleteById(Integer id);
    
    void deleteByExamPaperId(Integer examPaperId);
} 