package com.management.exam.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FillBlankQuestionTemplate extends BaseQuestionTemplate {
    @ExcelProperty("填空前文本")
    private String prefix;

    @ExcelProperty("填空后文本")
    private String suffix;
} 