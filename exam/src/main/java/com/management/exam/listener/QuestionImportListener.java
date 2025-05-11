package com.management.exam.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.fastjson2.JSON;
import com.management.exam.entity.Question;
import com.management.exam.enums.QuestionType;
import com.management.exam.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class QuestionImportListener<T> implements ReadListener<T> {
    private static final int BATCH_COUNT = 100;
    protected final List<Question> cachedQuestions = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    protected final QuestionService questionService;
    protected final Integer paperId;
    protected final QuestionType type;

    @Override
    public void invoke(T data, AnalysisContext context) {
        Question question = convertToQuestion(data);
        question.setPaperId(paperId);
        question.setType(String.valueOf(type));
        cachedQuestions.add(question);
        
        if (cachedQuestions.size() >= BATCH_COUNT) {
            saveData();
            cachedQuestions.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info("所有数据解析完成！");
    }

    protected void saveData() {
        log.info("{}条数据，开始存储数据库！", cachedQuestions.size());
        questionService.batchCreate(cachedQuestions);
        log.info("存储数据库成功！");
    }

    protected abstract Question convertToQuestion(T data);
} 