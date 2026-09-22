package com.denizer.taskmanagement.service;

import com.denizer.taskmanagement.dto.TaskAttachmentResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaskAttachmentService {

    TaskAttachmentResponseDto uploadAttachment(
            Long taskId,
            MultipartFile file
    );

    List<TaskAttachmentResponseDto> getTaskAttachments(
            Long taskId
    );

    void deleteAttachment(Long attachmentId);
}