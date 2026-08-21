package com.denizer.taskmanagement.service;

import com.denizer.taskmanagement.dto.TaskRequestDto;
import com.denizer.taskmanagement.dto.TaskResponseDto;
import com.denizer.taskmanagement.entity.TaskPriority;
import com.denizer.taskmanagement.entity.TaskStatus;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TaskService {

    TaskResponseDto createTask(TaskRequestDto request);

    TaskResponseDto getTaskById(Long id);

    List<TaskResponseDto> getAllTasks();

    TaskResponseDto updateTask(Long id, TaskRequestDto request);

    void deleteTask(Long id);

    Page<TaskResponseDto> getTasksByUserId(Long userId, Pageable pageable);

    Page<TaskResponseDto> getTasks(Pageable pageable);

    Page<TaskResponseDto> getTasksByStatus(TaskStatus status, Pageable pageable);

    Page<TaskResponseDto> getTasksByPriority(TaskPriority priority, Pageable pageable);

    Page<TaskResponseDto> getTasksByStatusAndPriority(
            TaskStatus status,
            TaskPriority priority,
            Pageable pageable
    );
}