package com.management.exam.vo;

import com.management.exam.entity.QuestionOption;
import lombok.Data;
import java.util.List;

@Data
public class ExamAnswerDetailVO {
    private Integer questionId;
    private String type;  // 题目类型
    private String title;  // 题目内容
    private String options;  // 选择题选项（JSON字符串）
    private Integer questionScore;  // 题目分值
    private String answer;  // 学生答案
    private Integer studentScore;  // 得分
    private List<String> answerImages;  // 学生答案图片列表
    private Integer orderNum;  // 题目顺序
    
    // 用于前端显示的选项列表
    private transient List<QuestionOption> optionList;
}