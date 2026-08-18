package com.denizer.taskmanagement.dto;

import com.denizer.taskmanagement.entity.TaskPriority;
import com.denizer.taskmanagement.entity.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequestDto {

    @NotBlank(message = "Title cannot be blank.")
    @Size(max = 100, message = "Title cannot exceed 100 characters.")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters.")
    private String description;

    @NotNull(message = "Status cannot be null.")
    private TaskStatus status;

    @NotNull(message = "Priority cannot be null.")
    private TaskPriority priority;

    @NotNull(message = "Due date cannot be null.")
    @FutureOrPresent(message = "Due date cannot be in the past.")
    private LocalDate dueDate;

}