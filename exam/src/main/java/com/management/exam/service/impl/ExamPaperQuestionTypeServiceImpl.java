package com.management.exam.service.impl;

import com.management.exam.entity.ExamPaperQuestionType;
import com.management.exam.exception.ResourceNotFoundException;
import com.management.exam.mapper.ExamPaperQuestionTypeMapper;
import com.management.exam.service.ExamPaperQuestionTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamPaperQuestionTypeServiceImpl implements ExamPaperQuestionTypeService {

    private final ExamPaperQuestionTypeMapper examPaperQuestionTypeMapper;

    @Override
    public List<ExamPaperQuestionType> findByExamPaperId(Integer examPaperId) {
        return examPaperQuestionTypeMapper.findByExamPaperId(examPaperId);
    }

    @Override
    public ExamPaperQuestionType findById(Integer id) {
        return examPaperQuestionTypeMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("试卷题型不存在"));
    }

    @Override
    public ExamPaperQuestionType create(ExamPaperQuestionType examPaperQuestionType) {
        examPaperQuestionTypeMapper.insert(examPaperQuestionType);
        return examPaperQuestionType;
    }

    @Override
    public void batchCreate(List<ExamPaperQuestionType> list) {
        if (list != null && !list.isEmpty()) {
            examPaperQuestionTypeMapper.batchInsert(list);
        }
    }

    @Override
    public void update(ExamPaperQuestionType examPaperQuestionType) {
        findById(examPaperQuestionType.getId());
        examPaperQuestionTypeMapper.update(examPaperQuestionType);
    }

    @Override
    public void deleteById(Integer id) {
        findById(id);
        examPaperQuestionTypeMapper.deleteById(id);
    }

    @Override
    public void deleteByExamPaperId(Integer paperId) {
        examPaperQuestionTypeMapper.deleteByExamPaperId(paperId);
    }
}


/**
 *
 ### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'exam_paper_id' in 'field list'
 ### The error may exist in file [C:\Users\33912\Desktop\paper\exam\target\classes\mapper\ExamPaperQuestionTypeMapper.xml]
 ### The error may involve defaultParameterMap
 ### The error occurred while setting parameters
 ### SQL: SELECT              id,             exam_paper_id,             question_type_id,             count,             score,             total_score,             created_at,             updated_at         FROM exam_paper_question_type         WHERE exam_paper_id = ?
 ### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'exam_paper_id' in 'field list'
 ; bad SQL grammar []
 */