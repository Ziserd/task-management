package com.denizer.taskmanagement.service;

import com.denizer.taskmanagement.dto.TaskRequestDto;
import com.denizer.taskmanagement.dto.TaskResponseDto;

import java.util.List;

public interface TaskService {

    TaskResponseDto createTask(TaskRequestDto request);

    TaskResponseDto getTaskById(Long id);

    List<TaskResponseDto> getAllTasks();

    TaskResponseDto updateTask(Long id, TaskRequestDto request);

    void deleteTask(Long id);

    List<TaskResponseDto> getTasksByUserId(Long userId);
}