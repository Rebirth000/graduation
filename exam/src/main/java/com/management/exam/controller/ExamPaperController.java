package com.management.exam.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson2.JSON;
import com.management.exam.entity.ExamPaper;
import com.management.exam.entity.ExamPaperQuestionType;
import com.management.exam.entity.Question;
import com.management.exam.entity.QuestionType;
import com.management.exam.model.excel.BaseQuestionTemplate;
import com.management.exam.model.excel.ChoiceQuestionTemplate;
import com.management.exam.model.excel.FillBlankQuestionTemplate;
import com.management.exam.model.excel.MultiBlankQuestionTemplate;
import com.management.exam.service.ExamPaperQuestionTypeService;
import com.management.exam.service.ExamPaperService;
import com.management.exam.service.QuestionService;
import com.management.exam.service.QuestionTypeService;
import com.management.exam.vo.ExamPaperQuestionTypeVO;
import com.management.exam.vo.PageResponse;
import com.management.exam.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam-paper")
@RequiredArgsConstructor
public class ExamPaperController {

    private final ExamPaperService examPaperService;
    private final QuestionService questionService;
    private final QuestionTypeService questionTypeService;
    private final ExamPaperQuestionTypeService examPaperQuestionTypeService;

    @GetMapping("/list")
    public Result<PageResponse<ExamPaper>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(examPaperService.getList(page, size));
    }

    @GetMapping
    public Result<List<ExamPaper>> findAll() {
        return Result.success(examPaperService.findAll());
    }

    @GetMapping("/{id}")
    public Result<ExamPaper> findById(@PathVariable Integer id) {
        return Result.success(examPaperService.findById(id));
    }

    @GetMapping("/{id}/questions")
    public Result<List<Question>> findQuestions(@PathVariable Integer id) {
        return Result.success(questionService.findByPaperId(id));
    }

    @GetMapping("/{id}/question-types")
    public Result<List<ExamPaperQuestionTypeVO>> findQuestionTypes(@PathVariable Integer id) {
        List<ExamPaperQuestionType> list = examPaperQuestionTypeService.findByExamPaperId(id);
        List<ExamPaperQuestionTypeVO> voList = new ArrayList<>();
        for (ExamPaperQuestionType epqt : list) {
            ExamPaperQuestionTypeVO vo = new ExamPaperQuestionTypeVO();
            vo.setId(epqt.getId());
            vo.setPaperId(epqt.getPaperId());
            vo.setQuestionTypeId(epqt.getQuestionTypeId());
            vo.setCreatedAt(epqt.getCreatedAt());
            // 查询题型详情
            if (epqt.getQuestionTypeId() != null) {
                QuestionType qt = questionTypeService.findById(epqt.getQuestionTypeId());
                if (qt != null) {
                    vo.setCode(qt.getCode());
                    vo.setName(qt.getName());
                }
            }
            voList.add(vo);
        }
        return Result.success(voList);
    }
    @GetMapping("/available-question-types")
    public Result<List<QuestionType>> findAvailableQuestionTypes() {
        // 获取状态为启用的题型
        List<QuestionType> allTypes = questionTypeService.findAll();
        List<QuestionType> availableTypes = new ArrayList<>();
        for (QuestionType type : allTypes) {
            if (type.getEnabled() != null && type.getEnabled()) {
                availableTypes.add(type);
            }
        }
        return Result.success(availableTypes);
    }

    @PostMapping
    public Result<ExamPaper> create(@RequestBody ExamPaper examPaper) {
        return Result.success(examPaperService.create(examPaper));
    }
    
    @PostMapping("/{id}/question-types")
    public Result<String> saveQuestionTypes(@PathVariable Integer id, @RequestBody List<ExamPaperQuestionType> types) {
        // 先删除该试卷下所有的题型配置
        examPaperQuestionTypeService.deleteByExamPaperId(id);
        
        // 设置试卷ID并保存
        for (ExamPaperQuestionType type : types) {
            type.setPaperId(id);
        }
        examPaperQuestionTypeService.batchCreate(types);
        return Result.success("");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody ExamPaper examPaper) {
        examPaper.setId(id);
        examPaperService.update(examPaper);
        return Result.success("");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteById(@PathVariable Integer id) {
        examPaperService.deleteById(id);
        return Result.success("");
    }

    @PostMapping("/{id}/questions")
    public Result<Question> createQuestion(@PathVariable Integer id, @RequestBody Question question) {
        // 验证题型是否在试卷配置中
        ExamPaperQuestionType questionTypeConfig = examPaperService.validateQuestionType(id, question.getType());
        if (questionTypeConfig == null) {
            return Result.error("该题型在当前试卷中不允许使用，请先在试卷中配置该题型。");
        }

        question.setPaperId(id);
        return Result.success(questionService.create(question));
    }

    @PutMapping("/questions/{id}")
    public Result<String> updateQuestion(@PathVariable Integer id, @RequestBody Question question) {
        question.setId(id);
        questionService.update(question);
        return Result.success("");
    }

    @DeleteMapping("/questions/{id}")
    public Result<String> deleteQuestion(@PathVariable Integer id) {
        questionService.deleteById(id);
        return Result.success("");
    }

    @PostMapping("/{id}/import")
    public Result<String> importExamPaper(@PathVariable Integer id, @RequestParam MultipartFile file) {
        examPaperService.importExamPaper(id, file);
        return Result.success("");
    }

    @GetMapping("/template")
    public void downloadExamPaperTemplate(HttpServletResponse response) throws IOException {
        // 设置正确的内容类型
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 设置文件名
        String fileName = URLEncoder.encode("试卷题目导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build()) {
            // 单选题sheet
            WriteSheet singleChoiceSheet = EasyExcel.writerSheet(0, "单选题")
                    .head(ChoiceQuestionTemplate.class).build()
                    ;
            excelWriter.write(new ArrayList<>(), singleChoiceSheet);

            // 多选题sheet
            WriteSheet multipleChoiceSheet = EasyExcel.writerSheet(1, "多选题")
                    .head(ChoiceQuestionTemplate.class).build()
                    ;
            excelWriter.write(new ArrayList<>(), multipleChoiceSheet);

            // 填空题sheet
            WriteSheet fillBlankSheet = EasyExcel.writerSheet(2, "填空题")
                    .head(FillBlankQuestionTemplate.class).build()
                    ;
            excelWriter.write(new ArrayList<>(), fillBlankSheet);
            
            // 多空填空题sheet
            WriteSheet multiBlankSheet = EasyExcel.writerSheet(3, "多空填空题")
                    .head(MultiBlankQuestionTemplate.class).build()
                    ;
            excelWriter.write(new ArrayList<>(), multiBlankSheet);

            // 判断题sheet
            WriteSheet trueFalseSheet = EasyExcel.writerSheet(4, "判断题")
                    .head(BaseQuestionTemplate.class).build()
                    ;
            excelWriter.write(new ArrayList<>(), trueFalseSheet);

            // 简答题sheet
            WriteSheet shortAnswerSheet = EasyExcel.writerSheet(5, "简答题")
                    .head(BaseQuestionTemplate.class).build()
                    ;
            excelWriter.write(new ArrayList<>(), shortAnswerSheet);

            // 分析题sheet
            WriteSheet analysisSheet = EasyExcel.writerSheet(6, "分析题")
                    .head(BaseQuestionTemplate.class).build()
                    ;
            excelWriter.write(new ArrayList<>(), analysisSheet);
        } catch (Exception e) {
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, String> map = new HashMap<>(2);
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }


} 