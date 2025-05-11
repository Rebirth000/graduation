package com.management.exam.mapper;

import com.management.exam.entity.DiscussionReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DiscussionReplyMapper {
    List<DiscussionReply> selectByDiscussionId(@Param("discussionId") Integer discussionId);
    
    int insert(DiscussionReply reply);
    
    int update(DiscussionReply reply);
    
    int deleteById(@Param("id") Integer id);
    
    DiscussionReply selectById(@Param("id") Integer id);
} 