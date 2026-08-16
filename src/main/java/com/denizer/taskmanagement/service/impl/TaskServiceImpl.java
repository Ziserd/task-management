package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.TaskRequestDto;
import com.denizer.taskmanagement.dto.TaskResponseDto;
import com.denizer.taskmanagement.entity.TaskPriority;
import com.denizer.taskmanagement.entity.TaskStatus;
import com.denizer.taskmanagement.repository.TaskRepository;
import com.denizer.taskmanagement.service.TaskService;
import org.springframework.stereotype.Service;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.repository.UserRepository;
import com.denizer.taskmanagement.entity.Task;
import com.denizer.taskmanagement.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskServiceImpl(TaskRepository taskRepository,
                           UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TaskResponseDto createTask(TaskRequestDto request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .user(user)
                .build();

        Task savedTask = taskRepository.save(task);

        return TaskResponseDto.builder()
                .id(savedTask.getId())
                .title(savedTask.getTitle())
                .description(savedTask.getDescription())
                .status(savedTask.getStatus())
                .priority(savedTask.getPriority())
                .dueDate(savedTask.getDueDate())
                .userId(savedTask.getUser().getId())
                .createdAt(savedTask.getCreatedAt())
                .updatedAt(savedTask.getUpdatedAt())
                .build();
    }

    @Override
    public TaskResponseDto getTaskById(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        return TaskResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .userId(task.getUser().getId())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    @Override
    public List<TaskResponseDto> getAllTasks() {

        return taskRepository.findAll()
                .stream()
                .map(task -> TaskResponseDto.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .status(task.getStatus())
                        .priority(task.getPriority())
                        .dueDate(task.getDueDate())
                        .userId(task.getUser().getId())
                        .createdAt(task.getCreatedAt())
                        .updatedAt(task.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    public List<TaskResponseDto> getTasksByUserId(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found.");
        }

        return taskRepository.findByUserId(userId)
                .stream()
                .map(task -> TaskResponseDto.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .status(task.getStatus())
                        .priority(task.getPriority())
                        .dueDate(task.getDueDate())
                        .userId(task.getUser().getId())
                        .createdAt(task.getCreatedAt())
                        .updatedAt(task.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    public List<TaskResponseDto> getTasksByStatus(TaskStatus status) {

        return taskRepository.findByStatus(status)
                .stream()
                .map(task -> TaskResponseDto.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .status(task.getStatus())
                        .priority(task.getPriority())
                        .dueDate(task.getDueDate())
                        .userId(task.getUser().getId())
                        .createdAt(task.getCreatedAt())
                        .updatedAt(task.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    public List<TaskResponseDto> getTasksByPriority(TaskPriority priority) {

        return taskRepository.findByPriority(priority)
                .stream()
                .map(task -> TaskResponseDto.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .status(task.getStatus())
                        .priority(task.getPriority())
                        .dueDate(task.getDueDate())
                        .userId(task.getUser().getId())
                        .createdAt(task.getCreatedAt())
                        .updatedAt(task.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    public List<TaskResponseDto> getTasksByStatusAndPriority(
            TaskStatus status,
            TaskPriority priority) {

        return taskRepository.findByStatusAndPriority(status, priority)
                .stream()
                .map(task -> TaskResponseDto.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .status(task.getStatus())
                        .priority(task.getPriority())
                        .dueDate(task.getDueDate())
                        .userId(task.getUser().getId())
                        .createdAt(task.getCreatedAt())
                        .updatedAt(task.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    public TaskResponseDto updateTask(Long id, TaskRequestDto request) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setUser(user);

        Task updatedTask = taskRepository.save(task);

        return TaskResponseDto.builder()
                .id(updatedTask.getId())
                .title(updatedTask.getTitle())
                .description(updatedTask.getDescription())
                .status(updatedTask.getStatus())
                .priority(updatedTask.getPriority())
                .dueDate(updatedTask.getDueDate())
                .userId(updatedTask.getUser().getId())
                .createdAt(updatedTask.getCreatedAt())
                .updatedAt(updatedTask.getUpdatedAt())
                .build();
    }

    @Override
    public void deleteTask(Long id) {

        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found.");
        }

        taskRepository.deleteById(id);
    }
}