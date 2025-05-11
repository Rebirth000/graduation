package com.management.exam.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class QuestionImportDTO {
    @ExcelProperty("题目")
    private String title;

    @ExcelProperty("选项A")
    private String optionA;

    @ExcelProperty("选项B")
    private String optionB;

    @ExcelProperty("选项C")
    private String optionC;

    @ExcelProperty("选项D")
    private String optionD;

    @ExcelProperty("填空前文本")
    private String prefix;

    @ExcelProperty("填空项")
    private String blank;

    @ExcelProperty("填空后文本")
    private String suffix;

    @ExcelProperty("分值")
    private Integer score;
} 