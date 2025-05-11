package com.management.exam.listener;

import com.alibaba.fastjson2.JSON;
import com.management.exam.entity.Question;
import com.management.exam.enums.QuestionType;
import com.management.exam.model.excel.FillBlankQuestionTemplate;
import com.management.exam.service.QuestionService;

import java.util.HashMap;
import java.util.Map;

public class FillBlankQuestionImportListener extends QuestionImportListener<FillBlankQuestionTemplate> {

    public FillBlankQuestionImportListener(QuestionService questionService, Integer paperId) {
        super(questionService, paperId, QuestionType.FILL_BLANK);
    }

    @Override
    protected Question convertToQuestion(FillBlankQuestionTemplate data) {
        Question question = new Question();
        question.setScore(data.getScore());

        Map<String, String> titleMap = new HashMap<>();
        titleMap.put("prefix", data.getPrefix());
        titleMap.put("suffix", data.getSuffix());
        question.setTitle(JSON.toJSONString(titleMap));

        return question;
    }
} 