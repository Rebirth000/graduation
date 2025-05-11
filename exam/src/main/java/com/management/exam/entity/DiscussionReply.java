package com.management.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DiscussionReply {
    private Integer id;
    private Integer discussionId;
    private String content;
    private Integer createdBy;
    private String creatorRole;
    private String creatorName;  // 回复人姓名，非数据库字段
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 