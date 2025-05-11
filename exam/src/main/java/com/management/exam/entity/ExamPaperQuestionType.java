package com.management.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamPaperQuestionType {
    private Integer id;
    private Integer paperId; // 试卷ID
    private Integer questionTypeId; // 题型ID
    private LocalDateTime createdAt;
}

/**
 *
 ### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'exam_paper_id' in 'where clause'
 ### The error may exist in file [C:\Users\33912\Desktop\paper\exam\target\classes\mapper\ExamPaperQuestionTypeMapper.xml]
 ### The error may involve defaultParameterMap
 ### The error occurred while setting parameters
 ### SQL: SELECT              id,             paper_id,             type_id,             created_at         FROM exam_paper_question_type         WHERE exam_paper_id = ?
 ### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'exam_paper_id' in 'where clause'
 ; bad SQL grammar []
 */