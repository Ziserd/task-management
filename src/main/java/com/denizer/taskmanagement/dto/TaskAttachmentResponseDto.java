package com.denizer.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TaskAttachmentResponseDto {

    private Long id;
    private Long taskId;
    private Long userId;
    private String userEmail;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private LocalDateTime createdAt;
}