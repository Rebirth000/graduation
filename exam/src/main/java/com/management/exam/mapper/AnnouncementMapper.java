package com.management.exam.mapper;

import com.management.exam.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface AnnouncementMapper {
    List<Announcement> selectList(Map<String, Object> params);
    
    int selectCount(Map<String, Object> params);
    
    int insert(Announcement announcement);
    
    int update(Announcement announcement);
    
    int deleteById(@Param("id") Integer id);
    
    Announcement selectById(@Param("id") Integer id);
} 