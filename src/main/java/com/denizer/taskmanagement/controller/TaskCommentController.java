package com.denizer.taskmanagement.controller;

import com.denizer.taskmanagement.dto.TaskCommentRequestDto;
import com.denizer.taskmanagement.dto.TaskCommentResponseDto;
import com.denizer.taskmanagement.service.TaskCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<TaskCommentResponseDto> createComment(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskCommentRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskCommentService.createComment(taskId, request));
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<TaskCommentResponseDto>> getTaskComments(
            @PathVariable Long taskId) {

        return ResponseEntity.ok(
                taskCommentService.getTaskComments(taskId)
        );
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<TaskCommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody TaskCommentRequestDto request) {

        return ResponseEntity.ok(
                taskCommentService.updateComment(commentId, request)
        );
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId) {

        taskCommentService.deleteComment(commentId);

        return ResponseEntity.noContent().build();
    }
}