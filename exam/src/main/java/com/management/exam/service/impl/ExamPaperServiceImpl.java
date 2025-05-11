package com.management.exam.service.impl;

import com.alibaba.excel.EasyExcel;
import com.management.exam.entity.ExamPaper;
import com.management.exam.entity.ExamPaperQuestionType;
import com.management.exam.entity.Question;
import com.management.exam.entity.QuestionType;
import com.management.exam.mapper.ExamPaperMapper;
import com.management.exam.mapper.ExamPaperQuestionTypeMapper;
import com.management.exam.mapper.QuestionMapper;
import com.management.exam.mapper.QuestionTypeMapper;
import com.management.exam.model.excel.BaseQuestionTemplate;
import com.management.exam.model.excel.ChoiceQuestionTemplate;
import com.management.exam.model.excel.FillBlankQuestionTemplate;
import com.management.exam.model.excel.MultiBlankQuestionTemplate;
import com.management.exam.service.ExamPaperService;
import com.management.exam.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamPaperServiceImpl implements ExamPaperService {

    private final ExamPaperMapper examPaperMapper;
    private final QuestionMapper questionMapper;
    private final QuestionTypeMapper questionTypeMapper;
    private final ExamPaperQuestionTypeMapper examPaperQuestionTypeMapper;

    @Override
    public PageResponse<ExamPaper> getList(Integer page, Integer size) {
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 获取总记录数
        long total = examPaperMapper.count();
        
        // 获取当前页数据
        List<ExamPaper> records = examPaperMapper.findByPage(offset, size);
        
        // 返回分页结果
        return new PageResponse<>(records, total, page, size);
    }

    @Override
    public List<ExamPaper> findAll() {
        return examPaperMapper.findAll();
    }

    @Override
    public ExamPaper findById(Integer id) {
        return examPaperMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));
    }

    @Override
    @Transactional
    public ExamPaper create(ExamPaper examPaper) {
        examPaper.setCreatedAt(LocalDateTime.now());
        examPaper.setUpdatedAt(LocalDateTime.now());
        examPaperMapper.insert(examPaper);
        return examPaper;
    }

    @Override
    @Transactional
    public void update(ExamPaper examPaper) {
        findById(examPaper.getId()); // 检查是否存在
        examPaperMapper.update(examPaper);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        findById(id); // 检查是否存在
        questionMapper.deleteByPaperId(id); // 删除相关题目
        examPaperMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void importExamPaper(Integer id, MultipartFile file) {
        try {
            // 确保试卷存在
            ExamPaper examPaper = findById(id);
            
            // 获取试卷配置的题型
            List<ExamPaperQuestionType> configuredTypes = examPaperQuestionTypeMapper.findByExamPaperId(id);
            if (configuredTypes == null || configuredTypes.isEmpty()) {
                throw new RuntimeException("请先配置试卷题型后再导入题目");
            }
            
            // 创建题型映射
            List<QuestionTypeMapping> typeMapping = new ArrayList<>();
            for (ExamPaperQuestionType type : configuredTypes) {
                QuestionType questionType = questionTypeMapper.findById(type.getQuestionTypeId()).orElse(null);
                if (questionType != null) {
                    Class<?> templateClass = getTemplateClass(questionType.getCode());
                    typeMapping.add(new QuestionTypeMapping(
                        questionType.getName(),
                        questionType.getCode(),
                        templateClass
                    ));
                }
            }
            
            // 删除原有题目
            questionMapper.deleteByPaperId(id);
            
            // 创建临时文件
            java.io.File tempFile = java.io.File.createTempFile("exam_import_", ".xlsx");
            file.transferTo(tempFile);
            
            // 动态导入每种题型
            for (QuestionTypeMapping mapping : typeMapping) {
                importQuestions(id, tempFile, mapping);
            }
            
            // 删除临时文件
            tempFile.delete();
            
        } catch (IOException e) {
            log.error("导入试卷失败", e);
            throw new RuntimeException("导入试卷失败: " + e.getMessage());
        }
    }
    
    // 题型映射类
    private static class QuestionTypeMapping {
        final String name;
        final String code;
        final Class<?> templateClass;
        
        QuestionTypeMapping(String name, String code, Class<?> templateClass) {
            this.name = name;
            this.code = code;
            this.templateClass = templateClass;
        }
    }
    
    // 根据题型代码获取对应的模板类
    private Class<?> getTemplateClass(String typeCode) {
        switch (typeCode) {
            case "SINGLE_CHOICE":
            case "MULTIPLE_CHOICE":
                return ChoiceQuestionTemplate.class;
            case "FILL_BLANK":
                return FillBlankQuestionTemplate.class;
            case "MULTI_BLANK":
                return MultiBlankQuestionTemplate.class;
            default:
                // 其他题型（包括判断题、简答题、分析题、编程题等）使用基础模板
                return BaseQuestionTemplate.class;
        }
    }
    
    // 统一的题目导入方法
    private void importQuestions(Integer paperId, java.io.File file, QuestionTypeMapping mapping) {
        log.info("开始导入{}", mapping.name);
        List<?> questionList = EasyExcel.read(file)
                .head(mapping.templateClass)
                .sheet(mapping.name)
                .doReadSync();
        
        if (questionList != null && !questionList.isEmpty()) {
            log.info("找到 {} 道{}", questionList.size(), mapping.name);
            
            for (Object template : questionList) {
                Question question = new Question();
                question.setPaperId(paperId);
                question.setType(mapping.code);
                
                if (template instanceof ChoiceQuestionTemplate) {
                    processChoiceQuestion(question, (ChoiceQuestionTemplate) template);
                } else if (template instanceof FillBlankQuestionTemplate) {
                    processFillBlankQuestion(question, (FillBlankQuestionTemplate) template);
                } else if (template instanceof MultiBlankQuestionTemplate) {
                    processMultiBlankQuestion(question, (MultiBlankQuestionTemplate) template);
                } else if (template instanceof BaseQuestionTemplate) {
                    processBaseQuestion(question, (BaseQuestionTemplate) template);
                }
                
                if (question.getTitle() != null && question.getScore() != null) {
                    questionMapper.insert(question);
                } else {
                    log.warn("{}数据不完整，跳过此题", mapping.name);
                }
            }
        } else {
            log.info("没有找到{}", mapping.name);
        }
    }
    
    private void processChoiceQuestion(Question question, ChoiceQuestionTemplate template) {
        if (template.getTitle() == null || template.getScore() == null) return;
        
        question.setTitle(template.getTitle());
        question.setScore(template.getScore());
        
        List<String> options = new ArrayList<>();
        if (template.getOptionA() != null) options.add(template.getOptionA());
        if (template.getOptionB() != null) options.add(template.getOptionB());
        if (template.getOptionC() != null) options.add(template.getOptionC());
        if (template.getOptionD() != null) options.add(template.getOptionD());
        
        question.setOptions(options.isEmpty() ? "[]" : "[\"" + String.join("\",\"", options) + "\"]");
    }
    
    private void processFillBlankQuestion(Question question, FillBlankQuestionTemplate template) {
        if (template.getScore() == null) return;
        
        String prefix = template.getPrefix() != null ? template.getPrefix() : "";
        String suffix = template.getSuffix() != null ? template.getSuffix() : "";
        question.setTitle("{\"prefix\":\"" + prefix + "\",\"suffix\":\"" + suffix + "\"}");
        question.setScore(template.getScore());
    }
    
    private void processMultiBlankQuestion(Question question, MultiBlankQuestionTemplate template) {
        if (template.getContent() == null || template.getScore() == null) return;
        
        question.setTitle(template.getContent());
        question.setScore(template.getScore());
    }
    
    private void processBaseQuestion(Question question, BaseQuestionTemplate template) {
        if (template.getTitle() == null || template.getScore() == null) return;
        
        question.setTitle(template.getTitle());
        question.setScore(template.getScore());
    }
    
    @Override
    public ExamPaperQuestionType validateQuestionType(Integer examPaperId, String code) {
        ExamPaper examPaper = findById(examPaperId);
        if (examPaper == null) {
            return null;
        }
        
        // 获取试卷的题型配置
        List<ExamPaperQuestionType> questionTypes = examPaperQuestionTypeMapper.findByExamPaperId(examPaperId);
        if (questionTypes == null || questionTypes.isEmpty()) {
            return null;
        }
        
        // 查找匹配的题型
        for (ExamPaperQuestionType type : questionTypes) {
            QuestionType questionType = questionTypeMapper.findById(type.getQuestionTypeId()).orElse(null);
            if (questionType != null && questionType.getCode().equals(code)) {
                return type;
            }
        }
        
        return null;
    }
} 