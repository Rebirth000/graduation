package com.management.exam.service;

import com.management.exam.entity.ExamRegistration;
import java.util.List;
import java.util.Map;

public interface ExamRegistrationService {
    
    // 获取可报名的考试列表
    Map<String, Object> getAvailableExams(int page, int size, String keyword);
    
    // 报名考试
    void register(Integer examId);
    
    // 获取我的报名记录
    Map<String, Object> getMyRegistrations(int page, int size, String keyword);
    
    // 取消报名
    void cancelRegistration(Integer registrationId);

    ExamRegistration getRegistrationDetail(Integer id);

    // 进入考试
    void enterExam(Integer registrationId);

    // 获取考试详情（包含试题）
    Map<String, Object> getExamDetail(Integer registrationId);

    // 提交考试答案
    void submitExam(Integer registrationId, Map<String, Object> answers);
} 