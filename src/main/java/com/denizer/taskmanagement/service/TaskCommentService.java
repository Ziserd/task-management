package com.denizer.taskmanagement.service;

import com.denizer.taskmanagement.dto.TaskCommentRequestDto;
import com.denizer.taskmanagement.dto.TaskCommentResponseDto;

import java.util.List;

public interface TaskCommentService {

    TaskCommentResponseDto createComment(
            Long taskId,
            TaskCommentRequestDto request
    );

    List<TaskCommentResponseDto> getTaskComments(Long taskId);

    TaskCommentResponseDto updateComment(
            Long commentId,
            TaskCommentRequestDto request
    );

    void deleteComment(Long commentId);
}