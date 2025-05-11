package com.management.exam.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class BaseQuestionTemplate {
    @ExcelProperty("题目内容")
    private String title;

    @ExcelProperty("分值")
    private Integer score;
} 