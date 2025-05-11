package com.management.exam.mapper;

import com.management.exam.entity.ExamPaper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ExamPaperMapper {
    /**
     * 分页查询试卷列表
     */
    List<ExamPaper> findByPage(@Param("offset") int offset, @Param("size") int size);

    /**
     * 统计试卷总数
     */
    long count();

    /**
     * 查询所有试卷
     */
    List<ExamPaper> findAll();
    
    /**
     * 根据ID查询试卷
     */
    Optional<ExamPaper> findById(@Param("id") Integer id);
    
    /**
     * 插入试卷
     */
    int insert(ExamPaper examPaper);
    
    /**
     * 更新试卷
     */
    int update(ExamPaper examPaper);
    
    /**
     * 删除试卷
     */
    int deleteById(@Param("id") Integer id);

    List<ExamPaper> findByCourseId(Integer courseId);
} 