package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.TaskCommentRequestDto;
import com.denizer.taskmanagement.dto.TaskCommentResponseDto;
import com.denizer.taskmanagement.entity.Task;
import com.denizer.taskmanagement.entity.TaskComment;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.exception.ForbiddenException;
import com.denizer.taskmanagement.exception.ResourceNotFoundException;
import com.denizer.taskmanagement.repository.TaskCommentRepository;
import com.denizer.taskmanagement.repository.TaskRepository;
import com.denizer.taskmanagement.repository.UserRepository;
import com.denizer.taskmanagement.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    public TaskCommentResponseDto createComment(
            Long taskId,
            TaskCommentRequestDto request) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found."));

        User user = getAuthenticatedUser();

        TaskComment comment = new TaskComment();

        comment.setTask(task);
        comment.setUser(user);
        comment.setContent(request.getContent());

        TaskComment savedComment =
                taskCommentRepository.save(comment);

        return convertToResponseDto(savedComment);
    }

    @Override
    public List<TaskCommentResponseDto> getTaskComments(Long taskId) {

        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found.");
        }

        return taskCommentRepository
                .findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Override
    public TaskCommentResponseDto updateComment(
            Long commentId,
            TaskCommentRequestDto request) {

        TaskComment comment =
                taskCommentRepository.findById(commentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Comment not found."
                                ));

        User user = getAuthenticatedUser();

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(
                    "You are not allowed to update this comment."
            );
        }

        comment.setContent(request.getContent());

        TaskComment updatedComment =
                taskCommentRepository.save(comment);

        return convertToResponseDto(updatedComment);
    }

    @Override
    public void deleteComment(Long commentId) {

        TaskComment comment =
                taskCommentRepository.findById(commentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Comment not found."
                                ));

        User user = getAuthenticatedUser();

        if (!comment.getUser().getId().equals(user.getId())
                && !user.getRole().name().equals("ADMIN")) {

            throw new ForbiddenException(
                    "You are not allowed to delete this comment."
            );
        }

        taskCommentRepository.delete(comment);
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found."
                        ));
    }

    private TaskCommentResponseDto convertToResponseDto(
            TaskComment comment) {

        return new TaskCommentResponseDto(
                comment.getId(),
                comment.getTask().getId(),
                comment.getUser().getId(),
                comment.getUser().getEmail(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}