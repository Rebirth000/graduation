package com.management.exam.mapper;

import com.management.exam.entity.ExamRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ExamRoomMapper {
    List<ExamRoom> findAll();
    
    Optional<ExamRoom> findById(@Param("id") Integer id);
    
    int insert(ExamRoom examRoom);
    
    int update(ExamRoom examRoom);
    
    int deleteById(@Param("id") Integer id);

    long count();
    
    List<ExamRoom> findByPage(@Param("offset") Integer offset, @Param("size") Integer size);
} 