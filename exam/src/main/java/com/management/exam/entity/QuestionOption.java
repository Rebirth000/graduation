package com.management.exam.entity;

import lombok.Data;

@Data
public class QuestionOption {
    private String code;  // 选项代号(A,B,C,D)
    private String content;  // 选项内容
}
