package com.denizer.taskmanagement.dto;

import com.denizer.taskmanagement.entity.TaskActivityAction;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentActivityDto {

    private Long id;

    private Long taskId;

    private String taskTitle;

    private Long userId;

    private String userEmail;

    private TaskActivityAction action;

    private String oldValue;

    private String newValue;

    private LocalDateTime createdAt;
}