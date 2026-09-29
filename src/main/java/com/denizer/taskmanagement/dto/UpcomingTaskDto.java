package com.denizer.taskmanagement.dto;

import com.denizer.taskmanagement.entity.TaskPriority;
import com.denizer.taskmanagement.entity.TaskStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingTaskDto {

    private Long id;

    private String title;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate dueDate;
}