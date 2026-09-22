package com.denizer.taskmanagement.controller;

import com.denizer.taskmanagement.dto.TaskAttachmentResponseDto;
import com.denizer.taskmanagement.service.TaskAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskAttachmentController {

    private final TaskAttachmentService taskAttachmentService;

    @PostMapping("/{taskId}/attachments")
    public ResponseEntity<TaskAttachmentResponseDto> uploadAttachment(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskAttachmentService.uploadAttachment(taskId, file));
    }

    @GetMapping("/{taskId}/attachments")
    public ResponseEntity<List<TaskAttachmentResponseDto>> getTaskAttachments(
            @PathVariable Long taskId) {

        return ResponseEntity.ok(
                taskAttachmentService.getTaskAttachments(taskId)
        );
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long attachmentId) {

        taskAttachmentService.deleteAttachment(attachmentId);

        return ResponseEntity.noContent().build();
    }
}