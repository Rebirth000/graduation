package com.management.exam.listener;

import com.alibaba.fastjson2.JSON;
import com.management.exam.entity.Question;
import com.management.exam.enums.QuestionType;
import com.management.exam.model.excel.ChoiceQuestionTemplate;
import com.management.exam.service.QuestionService;

import java.util.ArrayList;
import java.util.List;

public class ChoiceQuestionImportListener extends QuestionImportListener<ChoiceQuestionTemplate> {

    public ChoiceQuestionImportListener(QuestionService questionService, Integer paperId, QuestionType type) {
        super(questionService, paperId, type);
    }

    @Override
    protected Question convertToQuestion(ChoiceQuestionTemplate data) {
        Question question = new Question();
        question.setTitle(data.getTitle());
        question.setScore(data.getScore());

        List<String> options = new ArrayList<>();
        options.add(data.getOptionA());
        options.add(data.getOptionB());
        options.add(data.getOptionC());
        options.add(data.getOptionD());
        question.setOptions(JSON.toJSONString(options));

        return question;
    }
} 