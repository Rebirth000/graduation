package com.management.exam.service;

import com.management.exam.entity.Discussion;
import com.management.exam.entity.DiscussionReply;
import java.util.Map;
import java.util.List;

public interface DiscussionService {
    Map<String, Object> getDiscussionList(int page, int size, String title);
    
    void addDiscussion(Discussion discussion);
    
    void updateDiscussion(Discussion discussion);
    
    void deleteDiscussion(Integer id);
    
    Discussion getDiscussionById(Integer id);
    
    List<DiscussionReply> getDiscussionReplies(Integer discussionId);
    
    void addReply(DiscussionReply reply);
    
    void updateReply(DiscussionReply reply);
    
    void deleteReply(Integer id);
} 