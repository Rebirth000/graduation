package com.management.exam.mapper;

import com.management.exam.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface QuestionMapper {
    List<Question> findByPaperId(@Param("paperId") Integer paperId);
    
    Optional<Question> findById(@Param("id") Integer id);
    
    int insert(Question question);
    
    int batchInsert(@Param("questions") List<Question> questions);
    
    int update(Question question);
    
    int deleteById(@Param("id") Integer id);
    
    int deleteByPaperId(@Param("paperId") Integer paperId);
} 