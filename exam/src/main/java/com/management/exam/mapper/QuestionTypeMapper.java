package com.management.exam.mapper;

import com.management.exam.entity.QuestionType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface QuestionTypeMapper {
    List<QuestionType> findAll();
    
    List<QuestionType> findByNameWithPage(@Param("name") String name, @Param("offset") int offset, @Param("size") int size);
    
    long countByName(@Param("name") String name);
    
    Optional<QuestionType> findById(@Param("id") Integer id);
    
    int insert(QuestionType questionType);
    
    int update(QuestionType questionType);
    
    int deleteById(@Param("id") Integer id);
    
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    
    int updateDefault(@Param("id") Integer id, @Param("isDefault") Integer isDefault);
    
    int resetAllDefault();
} 