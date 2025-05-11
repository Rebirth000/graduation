package com.management.exam.mapper;

import com.management.exam.entity.Discussion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface DiscussionMapper {
    List<Discussion> selectList(Map<String, Object> params);
    
    int selectCount(Map<String, Object> params);
    
    int insert(Discussion discussion);
    
    int update(Discussion discussion);
    
    int deleteById(@Param("id") Integer id);
    
    Discussion selectById(@Param("id") Integer id);
} 