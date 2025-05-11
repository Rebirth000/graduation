package com.management.exam.service.impl;

import com.management.exam.entity.ExamRoom;
import com.management.exam.mapper.ExamRoomMapper;
import com.management.exam.service.ExamRoomService;
import com.management.exam.vo.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExamRoomServiceImpl implements ExamRoomService {
    
    @Autowired
    private ExamRoomMapper examRoomMapper;
    
    @Override
    public PageResponse<ExamRoom> getList(Integer page, Integer size) {
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 获取总记录数
        long total = examRoomMapper.count();
        
        // 获取当前页数据
        List<ExamRoom> records = examRoomMapper.findByPage(offset, size);
        
        // 返回分页结果
        return new PageResponse<>(records, total, page, size);
    }
    
    @Override
    public List<ExamRoom> getExamRoomList() {
        return examRoomMapper.findAll();
    }
    
    @Override
    public ExamRoom getExamRoomById(Integer id) {
        return examRoomMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("考场不存在"));
    }
    
    @Override
    @Transactional
    public void addExamRoom(ExamRoom examRoom) {
        int rows = examRoomMapper.insert(examRoom);
        if (rows != 1) {
            throw new RuntimeException("添加考场失败");
        }
    }
    
    @Override
    @Transactional
    public void updateExamRoom(Integer id, ExamRoom examRoom) {
        examRoom.setId(id);
        int rows = examRoomMapper.update(examRoom);
        if (rows != 1) {
            throw new RuntimeException("更新考场失败");
        }
    }
    
    @Override
    @Transactional
    public void deleteExamRoom(Integer id) {
        int rows = examRoomMapper.deleteById(id);
        if (rows != 1) {
            throw new RuntimeException("删除考场失败");
        }
    }
} 