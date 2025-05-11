package com.management.exam.service;

import com.management.exam.entity.Question;
import com.management.exam.dto.QuestionImportDTO;
import com.management.exam.enums.QuestionType;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface QuestionService {
    List<Question> findByPaperId(Integer paperId);
    
    Question findById(Integer id);
    
    Question create(Question question);
    
    void update(Question question);
    
    void deleteById(Integer id);
    
    void importQuestions(Integer paperId, QuestionType type, MultipartFile file);

    void batchCreate(List<Question> questions);
} 