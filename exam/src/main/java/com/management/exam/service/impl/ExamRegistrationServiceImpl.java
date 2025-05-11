package com.management.exam.service.impl;

import com.alibaba.fastjson2.JSON;
import com.management.exam.entity.*;
import com.management.exam.enums.RegistrationStatus;
import com.management.exam.mapper.*;
import com.management.exam.service.ExamRegistrationService;
import com.management.exam.service.QuestionService;
import com.management.exam.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamRegistrationServiceImpl implements ExamRegistrationService {

    private final ExamMapper examMapper;
    private final ExamRoomExamMapper examRoomExamMapper;
    private final ExamRegistrationMapper examRegistrationMapper;
    private final ExamAnswerMapper examAnswerMapper;
    private final QuestionService questionService;
    private final StudentMapper studentMapper;
    private final JwtUtil jwtUtil;

    private Integer getCurrentStudentId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization").substring(7);
        String studentId = jwtUtil.extractUsername(token);
        Student student = studentMapper.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        return student.getId();
    }

    @Override
    public Map<String, Object> getAvailableExams(int page, int size, String keyword) {
        int offset = (page - 1) * size;
        Integer studentId = getCurrentStudentId();
        // 获取未开始的考试列表
        List<Exam> exams = examMapper.selectAvailableExams(offset, size, LocalDateTime.now(), keyword, studentId);
        int total = examMapper.countAvailableExams(LocalDateTime.now(), keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("records", exams);
        result.put("total", total);
        return result;
    }

    // 新增方法：检查考试时间冲突
    private void checkExamTimeConflict(Integer studentId, Exam newExam) {
        // 获取学生已报名的所有未取消的考试
        List<ExamRegistration> registrations = examRegistrationMapper.findByStudentId(studentId);

        for (ExamRegistration registration : registrations) {
            // 跳过已取消的报名
            if (RegistrationStatus.CANCELLED.name().equals(registration.getStatus())) {
                continue;
            }

            // 获取已报名考试的详细信息
            Exam existingExam = examMapper.findById(registration.getExamId())
                    .orElseThrow(() -> new RuntimeException("已报名考试不存在"));

            // 检查时间冲突
            if (isTimeConflict(newExam, existingExam)) {
                throw new RuntimeException("考试时间冲突: 与考试[" + existingExam.getName() + "]时间重叠");
            }
        }
    }

    // 新增方法：判断两个考试时间是否冲突
    private boolean isTimeConflict(Exam exam1, Exam exam2) {
        LocalDateTime start1 = exam1.getStartTime();
        LocalDateTime end1 = exam1.getEndTime();
        LocalDateTime start2 = exam2.getStartTime();
        LocalDateTime end2 = exam2.getEndTime();

        // 检查时间重叠的逻辑
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    @Override
    @Transactional
    public void register(Integer examId) {
        // 获取当前学生ID
        Integer studentId = getCurrentStudentId();
        
        // 检查考试是否存在且未开始
        Exam exam = examMapper.findById(examId)
                .orElseThrow(() -> new RuntimeException("考试不存在"));
                
        if (exam.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("考试已开始，无法报名");
        }

        // 检查是否已报名
        ExamRegistration existingRegistration = examRegistrationMapper.findByStudentAndExam(studentId, examId);
        if (existingRegistration != null && !RegistrationStatus.CANCELLED.name().equals(existingRegistration.getStatus())) {
            throw new RuntimeException("您已报名此考试");
        }

        // 检查考试时间冲突
        checkExamTimeConflict(studentId, exam);

        // 获取考试关联的所有考场
        List<ExamRoom> examRooms = examRoomExamMapper.findExamRoomsByExamId(examId);
        if (examRooms.isEmpty()) {
            throw new RuntimeException("该考试未分配考场");
        }

        // 创建或更新报名记录
        ExamRegistration registration;
        if (existingRegistration != null) {
            // 如果是已取消的报名，重用该记录
            registration = existingRegistration;
            registration.setStatus(RegistrationStatus.PENDING.name());
            registration.setRegisterTime(LocalDateTime.now());
        } else {
            // 创建新的报名记录
            registration = new ExamRegistration();
        registration.setStudentId(studentId);
        registration.setExamId(examId);
        registration.setRegisterTime(LocalDateTime.now());
            registration.setStatus(RegistrationStatus.PENDING.name());
        examRegistrationMapper.insert(registration);
        }

        // 分配考场和座位
        boolean assigned = false;
        for (ExamRoom examRoom : examRooms) {
            // 获取该考场的所有报名记录（包括已分配和已取消的）
            List<ExamRegistration> roomRegistrations = examRegistrationMapper.findByExamAndRoom(
                examId, 
                examRoom.getId(),
                null  // 不限制状态，获取所有记录
            );

            // 获取已分配的座位号
            Set<Integer> assignedSeats = roomRegistrations.stream()
                .filter(r -> RegistrationStatus.ASSIGNED.name().equals(r.getStatus()))
                .map(ExamRegistration::getSeatNumber)
                .collect(Collectors.toSet());

            // 找出最小可用座位号
            int seatNumber = 1;
            while (seatNumber <= examRoom.getCapacity()) {
                if (!assignedSeats.contains(seatNumber)) {
                    // 找到可用座位
                    registration.setExamRoomId(examRoom.getId());
                    registration.setSeatNumber(seatNumber);
                    registration.setStatus(RegistrationStatus.ASSIGNED.name());
                    examRegistrationMapper.update(registration);
                    assigned = true;
                    break;
                }
                seatNumber++;
            }

            if (assigned) {
                break;
            }
        }

        if (!assigned) {
            throw new RuntimeException("所有考场都已满");
        }
    }

    @Override
    public Map<String, Object> getMyRegistrations(int page, int size, String keyword) {
        Integer studentId = getCurrentStudentId();
        int offset = (page - 1) * size;
        
        // 获取分页数据
        List<ExamRegistration> registrations = examRegistrationMapper.findByStudentIdWithPaging(studentId, offset, size, keyword);
        int total = examRegistrationMapper.countByStudentId(studentId, keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("records", registrations);
        result.put("total", total);
        return result;
    }

    @Override
    @Transactional
    public void cancelRegistration(Integer registrationId) {
        Integer studentId = getCurrentStudentId();
        
        // 获取报名记录
        ExamRegistration registration = examRegistrationMapper.findById(registrationId);
        if (registration == null || !registration.getStudentId().equals(studentId)) {
            throw new RuntimeException("报名记录不存在");
        }

        // 检查考试是否已开始
        Exam exam = examMapper.findById(registration.getExamId())
                .orElseThrow(() -> new RuntimeException("考试不存在"));
                
        if (exam.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("考试已开始，无法取消报名");
        }

        // 更新状态为已取消
        examRegistrationMapper.updateStatus(registrationId, RegistrationStatus.CANCELLED.name());
    }

    @Override
    public ExamRegistration getRegistrationDetail(Integer id) {
        Integer studentId = getCurrentStudentId();
        
        // 获取报名记录
        ExamRegistration registration = examRegistrationMapper.findById(id);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }
        
        // 验证是否是当前学生的报名记录
        if (!registration.getStudentId().equals(studentId)) {
            throw new RuntimeException("无权查看此报名记录");
        }
        
        return registration;
    }

    @Override
    @Transactional
    public void enterExam(Integer registrationId) {
        Integer studentId = getCurrentStudentId();
        
        // 获取报名记录
        ExamRegistration registration = examRegistrationMapper.findById(registrationId);
        if (registration == null || !registration.getStudentId().equals(studentId)) {
            throw new RuntimeException("报名记录不存在");
        }

        // 检查考试状态
        if (!registration.getStatus().equals(RegistrationStatus.ASSIGNED.name())) {
            throw new RuntimeException("考试状态不正确");
        }

        // 检查考试时间
        Exam exam = examMapper.findById(registration.getExamId())
                .orElseThrow(() -> new RuntimeException("考试不存在"));
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartTime())) {
            throw new RuntimeException("考试还未开始");
        }
        
        if (now.isAfter(exam.getEndTime())) {
            throw new RuntimeException("考试已结束");
        }
    }

    @Override
    public Map<String, Object> getExamDetail(Integer registrationId) {
        Integer studentId = getCurrentStudentId();
        
        // 获取报名记录
        ExamRegistration registration = examRegistrationMapper.findById(registrationId);
        if (registration == null || !registration.getStudentId().equals(studentId)) {
            throw new RuntimeException("报名记录不存在");
        }

        // 获取考试信息
        Exam exam = examMapper.findById(registration.getExamId())
                .orElseThrow(() -> new RuntimeException("考试不存在"));
        
        // 检查考试时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartTime())) {
            throw new RuntimeException("考试还未开始");
        }
        
        if (now.isAfter(exam.getEndTime())) {
            throw new RuntimeException("考试已结束");
        }

        // 检查是否已提交答案
        List<ExamAnswer> existingAnswers = examAnswerMapper.findByExamAndStudent(registration.getExamId(), studentId);
        if (!existingAnswers.isEmpty()) {
            throw new RuntimeException("您已提交过答案，不能重复答题");
        }

        // 获取试题列表
        List<Question> questions = questionService.findByPaperId(exam.getPaperId());

        Map<String, Object> result = new HashMap<>();
        result.put("examInfo", exam);
        result.put("questions", questions);
        return result;
    }

    @Override
    @Transactional
    public void submitExam(Integer registrationId, Map<String, Object> data) {
        Integer studentId = getCurrentStudentId();
        
        // 获取报名记录
        ExamRegistration registration = examRegistrationMapper.findById(registrationId);
        if (registration == null || !registration.getStudentId().equals(studentId)) {
            throw new RuntimeException("报名记录不存在");
        }

        // 检查考试时间
        Exam exam = examMapper.findById(registration.getExamId())
                .orElseThrow(() -> new RuntimeException("考试不存在"));
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(exam.getEndTime())) {
            throw new RuntimeException("考试已结束，无法提交答案");
        }

        // 检查是否已提交答案
        List<ExamAnswer> existingAnswers = examAnswerMapper.findByExamAndStudent(registration.getExamId(), studentId);
        if (!existingAnswers.isEmpty()) {
            throw new RuntimeException("您已提交过答案，不能重复提交");
        }

        // 获取答案数据
        @SuppressWarnings("unchecked")
        Map<String, Object> answers = (Map<String, Object>) data.get("answers");
        if (answers == null || answers.isEmpty()) {
            throw new RuntimeException("答案不能为空");
        }

        // 获取图片数据
        @SuppressWarnings("unchecked")
        Map<String, List<String>> answerImages = (Map<String, List<String>>) data.get("answerImages");

        // 保存答案
        List<ExamAnswer> examAnswers = new ArrayList<>();
        for (Map.Entry<String, Object> entry : answers.entrySet()) {
            String questionId = entry.getKey();
            ExamAnswer answer = new ExamAnswer();
            answer.setExamId(registration.getExamId());
            answer.setStudentId(studentId);
            answer.setQuestionId(Long.parseLong(questionId));
            answer.setAnswer(JSON.toJSONString(entry.getValue()));
            
            // 设置答案图片
            if (answerImages != null && answerImages.containsKey(questionId)) {
                answer.setAnswerImages(JSON.toJSONString(answerImages.get(questionId)));
            } else {
                answer.setAnswerImages("[]");
            }
            
            answer.setCreateTime(now);
            answer.setUpdateTime(now);
            examAnswers.add(answer);
        }

        examAnswerMapper.batchInsert(examAnswers);
    }
} 