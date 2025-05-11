package com.management.exam.service;

import com.management.exam.entity.ExamRoom;
import com.management.exam.vo.PageResponse;

import java.util.List;

public interface ExamRoomService {
    PageResponse<ExamRoom> getList(Integer page, Integer size);
    
    List<ExamRoom> getExamRoomList();
    
    ExamRoom getExamRoomById(Integer id);
    
    void addExamRoom(ExamRoom examRoom);
    
    void updateExamRoom(Integer id, ExamRoom examRoom);
    
    void deleteExamRoom(Integer id);
} 