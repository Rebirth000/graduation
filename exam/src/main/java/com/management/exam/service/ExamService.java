package com.management.exam.service;

import com.management.exam.entity.Exam;
import com.management.exam.vo.ExamUpdateVO;
import com.management.exam.vo.PageResponse;
import java.util.List;

public interface ExamService {
    PageResponse<Exam> getExamList(Integer page, Integer size);
    
    Exam getExamById(Integer id);
    
    List<Exam> getExamsByPaperId(Integer paperId);
    
    void addExam(Exam exam);
    
    ExamUpdateVO updateExam(Integer id, Exam exam);
    
    void deleteExam(Integer id);
} 