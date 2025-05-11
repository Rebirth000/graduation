package com.management.exam.service.impl;

import com.management.exam.entity.QuestionType;
import com.management.exam.exception.ResourceNotFoundException;
import com.management.exam.mapper.QuestionTypeMapper;
import com.management.exam.service.QuestionTypeService;
import com.management.exam.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionTypeServiceImpl implements QuestionTypeService {

    private final QuestionTypeMapper questionTypeMapper;

    @Override
    public PageResponse<QuestionType> getList(String name, Integer page, Integer size) {
        long total = questionTypeMapper.countByName(name);
        List<QuestionType> list = questionTypeMapper.findByNameWithPage(name, (page - 1) * size, size);
        return new PageResponse<>(list, total, page, size);
    }

    @Override
    public List<QuestionType> findAll() {
        return questionTypeMapper.findAll();
    }

    @Override
    public QuestionType findById(Integer id) {
        return questionTypeMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("题型不存在"));
    }

    @Override
    public QuestionType create(QuestionType questionType) {
        questionTypeMapper.insert(questionType);
        return questionType;
    }

    @Override
    public void update(QuestionType questionType) {
        findById(questionType.getId());
        questionTypeMapper.update(questionType);
    }

    @Override
    public void deleteById(Integer id) {
        findById(id);
        questionTypeMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        findById(id);
        questionTypeMapper.updateStatus(id, status);
    }

    @Override
    public void updateDefault(Integer id, Integer isDefault) {
        findById(id);
        questionTypeMapper.updateDefault(id, isDefault);
    }

    @Override
    public void resetAllDefault() {
        questionTypeMapper.resetAllDefault();
    }
}


/**
 ### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'status' in 'field list'
 ### The error may exist in file [C:\Users\33912\Desktop\paper\exam\target\classes\mapper\QuestionTypeMapper.xml]
 ### The error may involve defaultParameterMap
 ### The error occurred while setting parameters
 ### SQL: SELECT              id,             name,             description,             status,             is_default,             created_at,             updated_at         FROM question_type                    ORDER BY id ASC         LIMIT ?, ?
 ### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'status' in 'field list'
 ; bad SQL grammar []
 */