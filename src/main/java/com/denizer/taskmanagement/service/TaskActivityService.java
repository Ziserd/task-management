package com.denizer.taskmanagement.service;

import com.denizer.taskmanagement.dto.TaskActivityResponseDto;

import java.util.List;

public interface TaskActivityService {

    void logActivity(
            Long taskId,
            Long userId,
            String action,
            String oldValue,
            String newValue
    );

    List<TaskActivityResponseDto> getTaskActivities(Long taskId);
}