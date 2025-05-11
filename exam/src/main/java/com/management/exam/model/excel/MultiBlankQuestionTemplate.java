package com.management.exam.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class MultiBlankQuestionTemplate {
    @ExcelProperty("填空题内容([blank]标记填空位置)")
    private String content;
    
    @ExcelProperty("分值")
    private Integer score;
} 