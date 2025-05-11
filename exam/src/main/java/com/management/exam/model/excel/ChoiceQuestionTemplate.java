package com.management.exam.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChoiceQuestionTemplate extends BaseQuestionTemplate {
    @ExcelProperty("选项A")
    private String optionA;

    @ExcelProperty("选项B")
    private String optionB;

    @ExcelProperty("选项C")
    private String optionC;

    @ExcelProperty("选项D")
    private String optionD;
} 