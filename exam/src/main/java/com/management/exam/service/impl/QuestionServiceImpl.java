package com.management.exam.service.impl;

import com.alibaba.excel.EasyExcel;
import com.management.exam.entity.Question;
import com.management.exam.enums.QuestionType;
import com.management.exam.listener.ChoiceQuestionImportListener;
import com.management.exam.listener.FillBlankQuestionImportListener;
import com.management.exam.listener.TextQuestionImportListener;
import com.management.exam.mapper.QuestionMapper;
import com.management.exam.model.excel.BaseQuestionTemplate;
import com.management.exam.model.excel.ChoiceQuestionTemplate;
import com.management.exam.model.excel.FillBlankQuestionTemplate;
import com.management.exam.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;

    @Override
    public List<Question> findByPaperId(Integer paperId) {
        return questionMapper.findByPaperId(paperId);
    }

    @Override
    public Question findById(Integer id) {
        return questionMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("题目不存在"));
    }

    @Override
    @Transactional
    public Question create(Question question) {
        questionMapper.insert(question);
        return question;
    }

    @Override
    @Transactional
    public void update(Question question) {
        findById(question.getId()); // 检查是否存在
        questionMapper.update(question);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        findById(id); // 检查是否存在
        questionMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void importQuestions(Integer paperId, QuestionType type, MultipartFile file) {
        try {
            switch (type) {
                case SINGLE_CHOICE, MULTIPLE_CHOICE ->
                    EasyExcel.read(file.getInputStream(), ChoiceQuestionTemplate.class,
                            new ChoiceQuestionImportListener(this, paperId, type))
                            .sheet()
                            .doRead();
                case FILL_BLANK ->
                    EasyExcel.read(file.getInputStream(), FillBlankQuestionTemplate.class,
                            new FillBlankQuestionImportListener(this, paperId))
                            .sheet()
                            .doRead();
                case TRUE_FALSE, SHORT_ANSWER, ANALYSIS ->
                    EasyExcel.read(file.getInputStream(), BaseQuestionTemplate.class,
                            new TextQuestionImportListener(this, paperId, type))
                            .sheet()
                            .doRead();
                default -> throw new IllegalArgumentException("不支持的题目类型：" + type);
            }
        } catch (IOException e) {
            throw new RuntimeException("导入题目失败", e);
        }
    }

    @Override
    @Transactional
    public void batchCreate(List<Question> questions) {
        for (Question question : questions) {
            questionMapper.insert(question);
        }
    }
} 