package com.denizer.taskmanagement.dto;

import com.denizer.taskmanagement.entity.TaskActivityAction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TaskActivityResponseDto {

    private Long id;
    private Long taskId;
    private Long userId;
    private String userEmail;
    private TaskActivityAction action;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;
}