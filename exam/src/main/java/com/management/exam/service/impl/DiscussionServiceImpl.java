package com.management.exam.service.impl;

import com.management.exam.entity.Discussion;
import com.management.exam.entity.DiscussionReply;
import com.management.exam.mapper.DiscussionMapper;
import com.management.exam.mapper.DiscussionReplyMapper;
import com.management.exam.service.DiscussionService;
import com.management.exam.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiscussionServiceImpl implements DiscussionService {
    
    private final DiscussionMapper discussionMapper;
    private final DiscussionReplyMapper discussionReplyMapper;
    private final JwtUtil jwtUtil;

    private Integer getCurrentUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }
    
    @Override
    public Map<String, Object> getDiscussionList(int page, int size, String title) {
        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        params.put("offset", (page - 1) * size);
        params.put("size", size);
        
        List<Discussion> discussions = discussionMapper.selectList(params);
        int total = discussionMapper.selectCount(params);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", discussions);
        result.put("total", total);
        return result;
    }
    
    @Override
    @Transactional
    public void addDiscussion(Discussion discussion) {
        discussion.setCreatedBy(getCurrentUserId());
        discussionMapper.insert(discussion);
    }
    
    @Override
    @Transactional
    public void updateDiscussion(Discussion discussion) {
        Discussion existing = discussionMapper.selectById(discussion.getId());
        if (existing == null) {
            throw new RuntimeException("讨论不存在");
        }
        discussionMapper.update(discussion);
    }
    
    @Override
    @Transactional
    public void deleteDiscussion(Integer id) {
        Discussion existing = discussionMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("讨论不存在");
        }
        discussionMapper.deleteById(id);
    }
    
    @Override
    public Discussion getDiscussionById(Integer id) {
        return discussionMapper.selectById(id);
    }
    
    @Override
    public List<DiscussionReply> getDiscussionReplies(Integer discussionId) {
        return discussionReplyMapper.selectByDiscussionId(discussionId);
    }
    
    @Override
    @Transactional
    public void addReply(DiscussionReply reply) {
        Discussion discussion = discussionMapper.selectById(reply.getDiscussionId());
        if (discussion == null) {
            throw new RuntimeException("讨论不存在");
        }
        
        if (discussion.getDeadline() != null && LocalDateTime.now().isAfter(discussion.getDeadline())) {
            throw new RuntimeException("讨论已截止，无法回复");
        }

        reply.setCreatedBy(getCurrentUserId());
        
        discussionReplyMapper.insert(reply);
    }
    
    @Override
    @Transactional
    public void updateReply(DiscussionReply reply) {
        DiscussionReply existing = discussionReplyMapper.selectById(reply.getId());
        if (existing == null) {
            throw new RuntimeException("回复不存在");
        }
        
        Discussion discussion = discussionMapper.selectById(existing.getDiscussionId());
        if (discussion.getDeadline() != null && LocalDateTime.now().isAfter(discussion.getDeadline())) {
            throw new RuntimeException("讨论已截止，无法修改回复");
        }
        
        discussionReplyMapper.update(reply);
    }
    
    @Override
    @Transactional
    public void deleteReply(Integer id) {
        DiscussionReply existing = discussionReplyMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("回复不存在");
        }
        
        Discussion discussion = discussionMapper.selectById(existing.getDiscussionId());
        if (discussion.getDeadline() != null && LocalDateTime.now().isAfter(discussion.getDeadline())) {
            throw new RuntimeException("讨论已截止，无法删除回复");
        }
        
        discussionReplyMapper.deleteById(id);
    }
} 