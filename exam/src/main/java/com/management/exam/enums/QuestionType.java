package com.management.exam.enums;

public enum QuestionType {
    SINGLE_CHOICE("单项选择题"),
    MULTIPLE_CHOICE("多项选择题"),
    FILL_BLANK("填空题"),
    TRUE_FALSE("判断题"),
    SHORT_ANSWER("简答题"),
    ANALYSIS("分析题");

    private final String description;

    QuestionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }


} 