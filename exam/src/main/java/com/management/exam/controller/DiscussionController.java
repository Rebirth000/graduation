package com.management.exam.controller;

import com.management.exam.entity.Discussion;
import com.management.exam.entity.DiscussionReply;
import com.management.exam.service.DiscussionService;
import com.management.exam.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;

    @GetMapping
    public Result<Map<String, Object>> getDiscussionList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title) {
        return Result.success(discussionService.getDiscussionList(page, size, title));
    }

    @PostMapping
    public Result<String> addDiscussion(@RequestBody Discussion discussion) {
        discussionService.addDiscussion(discussion);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<String> updateDiscussion(
            @PathVariable Integer id,
            @RequestBody Discussion discussion) {
        discussion.setId(id);
        discussionService.updateDiscussion(discussion);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteDiscussion(@PathVariable Integer id) {
        discussionService.deleteDiscussion(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Discussion> getDiscussionById(@PathVariable Integer id) {
        return Result.success(discussionService.getDiscussionById(id));
    }

    @GetMapping("/{discussionId}/replies")
    public Result<List<DiscussionReply>> getDiscussionReplies(
            @PathVariable Integer discussionId) {
        return Result.success(discussionService.getDiscussionReplies(discussionId));
    }

    @PostMapping("/{discussionId}/replies")
    public Result<String> addReply(
            @PathVariable Integer discussionId,
            @RequestBody DiscussionReply reply) {
        reply.setDiscussionId(discussionId);
        discussionService.addReply(reply);
        return Result.success();
    }

    @PutMapping("/replies/{id}")
    public Result<String> updateReply(
            @PathVariable Integer id,
            @RequestBody DiscussionReply reply) {
        reply.setId(id);
        discussionService.updateReply(reply);
        return Result.success();
    }

    @DeleteMapping("/replies/{id}")
    public Result<String> deleteReply(@PathVariable Integer id) {
        discussionService.deleteReply(id);
        return Result.success();
    }
} 