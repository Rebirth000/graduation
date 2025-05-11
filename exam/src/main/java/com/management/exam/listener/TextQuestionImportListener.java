package com.management.exam.listener;

import com.management.exam.entity.Question;
import com.management.exam.enums.QuestionType;
import com.management.exam.model.excel.BaseQuestionTemplate;
import com.management.exam.service.QuestionService;

public class TextQuestionImportListener extends QuestionImportListener<BaseQuestionTemplate> {

    public TextQuestionImportListener(QuestionService questionService, Integer paperId, QuestionType type) {
        super(questionService, paperId, type);
    }

    @Override
    protected Question convertToQuestion(BaseQuestionTemplate data) {
        Question question = new Question();
        question.setTitle(data.getTitle());
        question.setScore(data.getScore());
        return question;
    }
} 