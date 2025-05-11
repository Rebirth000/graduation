package com.management.exam.vo;

import java.time.LocalDateTime;

public class ExamPaperQuestionTypeVO {
    private Integer id;
    private Integer paperId;
    private Integer questionTypeId;
    private String code;
    private String name;
    private LocalDateTime createdAt;

    // getter/setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPaperId() { return paperId; }
    public void setPaperId(Integer paperId) { this.paperId = paperId; }
    public Integer getQuestionTypeId() { return questionTypeId; }
    public void setQuestionTypeId(Integer questionTypeId) { this.questionTypeId = questionTypeId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}