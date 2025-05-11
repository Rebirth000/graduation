package com.management.exam.service;

import com.management.exam.entity.ExamPaper;
import com.management.exam.entity.ExamPaperQuestionType;
import com.management.exam.vo.PageResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ExamPaperService {
    PageResponse<ExamPaper> getList(Integer page, Integer size);

    List<ExamPaper> findAll();
    
    ExamPaper findById(Integer id);
    
    ExamPaper create(ExamPaper examPaper);
    
    void update(ExamPaper examPaper);
    
    void deleteById(Integer id);
    
    void importExamPaper(Integer id, MultipartFile file);
    
    /**
     * 验证指定的题型在试卷中是否可用
     * @param examPaperId 试卷ID
     * @param questionTypeName 题型名称
     * @return 题型配置信息，如果题型不可用则返回null
     */
    ExamPaperQuestionType validateQuestionType(Integer examPaperId, String questionTypeName);
} 