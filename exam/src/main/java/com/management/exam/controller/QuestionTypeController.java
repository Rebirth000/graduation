package com.management.exam.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson2.JSON;
import com.management.exam.entity.QuestionType;
import com.management.exam.model.excel.BaseQuestionTemplate;
import com.management.exam.model.excel.ChoiceQuestionTemplate;
import com.management.exam.model.excel.FillBlankQuestionTemplate;
import com.management.exam.model.excel.MultiBlankQuestionTemplate;
import com.management.exam.service.QuestionTypeService;
import com.management.exam.vo.PageResponse;
import com.management.exam.vo.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/question-type")
@RequiredArgsConstructor
public class QuestionTypeController {

    private final QuestionTypeService questionTypeService;

    @GetMapping("/list")
    public Result<PageResponse<QuestionType>> getList(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(questionTypeService.getList(name, page, size));
    }

    @GetMapping("/{id}")
    public Result<QuestionType> findById(@PathVariable Integer id) {
        return Result.success(questionTypeService.findById(id));
    }

    @PostMapping
    public Result<QuestionType> create(@RequestBody QuestionType questionType) {
        return Result.success(questionTypeService.create(questionType));
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody QuestionType questionType) {
        questionType.setId(id);
        questionTypeService.update(questionType);
        return Result.success("");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteById(@PathVariable Integer id) {
        questionTypeService.deleteById(id);
        return Result.success("");
    }

    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> statusMap) {
        Integer status = statusMap.get("status");
        questionTypeService.updateStatus(id, status);
        return Result.success("");
    }

    @PostMapping("/template")
    public void downloadExamPaperTemplateFromNewPage(
            @RequestBody List<Integer> typeIds,
            HttpServletResponse response) throws IOException {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("试卷题目导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build()) {
            int sheetNo = 0;

            // 遍历题型ID列表
            for (Integer typeId : typeIds) {
                QuestionType questionType = questionTypeService.findById(typeId);
                if (questionType == null) continue;

                // 根据题型代码选择对应的模板类
                Class<?> templateClass;
                switch (questionType.getCode()) {
                    case "SINGLE_CHOICE":
                    case "MULTIPLE_CHOICE":
                        templateClass = ChoiceQuestionTemplate.class;
                        break;
                    case "FILL_BLANK":
                        templateClass = FillBlankQuestionTemplate.class;
                        break;
                    case "MULTI_BLANK":
                        templateClass = MultiBlankQuestionTemplate.class;
                        break;
                    default:
                        // 其他题型（包括判断题、简答题、分析题、编程题等）使用基础模板
                        templateClass = BaseQuestionTemplate.class;
                }

                // 创建sheet
                WriteSheet sheet = EasyExcel.writerSheet(sheetNo++, questionType.getName())
                        .head(templateClass)
                        .build();
                excelWriter.write(new ArrayList<>(), sheet);
            }
        } catch (Exception e) {
            // 发生异常时返回JSON错误信息
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, String> map = new HashMap<>(2);
            map.put("status", "failure");
            map.put("message", "下载文件失败: " + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }
} 