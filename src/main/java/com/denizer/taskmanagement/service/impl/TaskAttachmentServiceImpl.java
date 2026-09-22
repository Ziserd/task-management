package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.TaskAttachmentResponseDto;
import com.denizer.taskmanagement.entity.Task;
import com.denizer.taskmanagement.entity.TaskAttachment;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.exception.ForbiddenException;
import com.denizer.taskmanagement.exception.ResourceNotFoundException;
import com.denizer.taskmanagement.repository.TaskAttachmentRepository;
import com.denizer.taskmanagement.repository.TaskRepository;
import com.denizer.taskmanagement.repository.UserRepository;
import com.denizer.taskmanagement.service.TaskAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskAttachmentServiceImpl implements TaskAttachmentService {

    private final TaskAttachmentRepository taskAttachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private final Path uploadDirectory =
            Paths.get("uploads/tasks");

    @Override
    public TaskAttachmentResponseDto uploadAttachment(
            Long taskId,
            MultipartFile file) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found."));

        User user = getAuthenticatedUser();

        if (!user.getRole().name().equals("ADMIN")
                && !task.getUser().getId().equals(user.getId())) {

            throw new ForbiddenException(
                    "You are not allowed to add an attachment to this task."
            );
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File cannot be empty."
            );
        }

        try {
            Path taskDirectory =
                    uploadDirectory.resolve(String.valueOf(taskId));

            Files.createDirectories(taskDirectory);

            String originalFileName = file.getOriginalFilename();

            if (originalFileName == null || originalFileName.isBlank()) {
                throw new IllegalArgumentException(
                        "File name cannot be empty."
                );
            }

            String storedFileName =
                    UUID.randomUUID() + "_" + originalFileName;

            Path filePath =
                    taskDirectory.resolve(storedFileName);

            Files.copy(file.getInputStream(), filePath);

            TaskAttachment attachment = new TaskAttachment();

            attachment.setTask(task);
            attachment.setUser(user);
            attachment.setFileName(originalFileName);
            attachment.setFileType(
                    file.getContentType() != null
                            ? file.getContentType()
                            : "application/octet-stream"
            );
            attachment.setFileSize(file.getSize());
            attachment.setFilePath(filePath.toString());

            TaskAttachment savedAttachment =
                    taskAttachmentRepository.save(attachment);

            return convertToResponseDto(savedAttachment);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to store file.",
                    e
            );
        }
    }

    @Override
    public List<TaskAttachmentResponseDto> getTaskAttachments(
            Long taskId) {

        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException(
                    "Task not found."
            );
        }

        return taskAttachmentRepository
                .findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Override
    public void deleteAttachment(Long attachmentId) {

        TaskAttachment attachment =
                taskAttachmentRepository.findById(attachmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Attachment not found."
                                ));

        User user = getAuthenticatedUser();

        if (!user.getRole().name().equals("ADMIN")
                && !attachment.getUser().getId().equals(user.getId())) {

            throw new ForbiddenException(
                    "You are not allowed to delete this attachment."
            );
        }

        try {
            Path filePath =
                    Paths.get(attachment.getFilePath());

            Files.deleteIfExists(filePath);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to delete file.",
                    e
            );
        }

        taskAttachmentRepository.delete(attachment);
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

    private TaskAttachmentResponseDto convertToResponseDto(
            TaskAttachment attachment) {

        return new TaskAttachmentResponseDto(
                attachment.getId(),
                attachment.getTask().getId(),
                attachment.getUser().getId(),
                attachment.getUser().getEmail(),
                attachment.getFileName(),
                attachment.getFileType(),
                attachment.getFileSize(),
                attachment.getFilePath(),
                attachment.getCreatedAt()
        );
    }
}