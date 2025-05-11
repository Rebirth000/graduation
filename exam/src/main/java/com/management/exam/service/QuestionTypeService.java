package com.management.exam.service;

import com.management.exam.entity.QuestionType;
import com.management.exam.vo.PageResponse;
import java.util.List;

public interface QuestionTypeService {
    
    PageResponse<QuestionType> getList(String name, Integer page, Integer size);
    
    List<QuestionType> findAll();
    
    QuestionType findById(Integer id);
    
    QuestionType create(QuestionType questionType);
    
    void update(QuestionType questionType);
    
    void deleteById(Integer id);
    
    void updateStatus(Integer id, Integer status);
    
    void updateDefault(Integer id, Integer isDefault);
    
    void resetAllDefault();
} 