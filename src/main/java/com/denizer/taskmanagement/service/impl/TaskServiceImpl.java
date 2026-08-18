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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.denizer.taskmanagement.exception.ForbiddenException;

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

        User user = getAuthenticatedUser();

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

        User authenticatedUser = getAuthenticatedUser();

        if (authenticatedUser.getRole().name().equals("ADMIN")) {
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

        return taskRepository.findByUserId(authenticatedUser.getId())
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

        User authenticatedUser = getAuthenticatedUser();

        if (!authenticatedUser.getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to access this user's tasks.");
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

        User authenticatedUser = getAuthenticatedUser();

        List<Task> tasks;

        if (authenticatedUser.getRole().name().equals("ADMIN")) {
            tasks = taskRepository.findByStatus(status);
        } else {
            tasks = taskRepository.findByUserIdAndStatus(
                    authenticatedUser.getId(),
                    status
            );
        }

        return tasks.stream()
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

        User authenticatedUser = getAuthenticatedUser();

        List<Task> tasks;

        if (authenticatedUser.getRole().name().equals("ADMIN")) {
            tasks = taskRepository.findByPriority(priority);
        } else {
            tasks = taskRepository.findByUserIdAndPriority(
                    authenticatedUser.getId(),
                    priority
            );
        }

        return tasks.stream()
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

        User authenticatedUser = getAuthenticatedUser();

        List<Task> tasks;

        if (authenticatedUser.getRole().name().equals("ADMIN")) {
            tasks = taskRepository.findByStatusAndPriority(
                    status,
                    priority
            );
        } else {
            tasks = taskRepository.findByUserIdAndStatusAndPriority(
                    authenticatedUser.getId(),
                    status,
                    priority
            );
        }

        return tasks.stream()
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

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));
    }

    @Override
    public TaskResponseDto updateTask(Long id, TaskRequestDto request) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));
        User user = getAuthenticatedUser();

        if (!user.getRole().name().equals("ADMIN")
                && !task.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(
                    "You are not allowed to update this task."
            );
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

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

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found."));

        User user = getAuthenticatedUser();

        if (!user.getRole().name().equals("ADMIN")
                && !task.getUser().getId().equals(user.getId())) {

            throw new ForbiddenException(
                    "You are not allowed to delete this task."
            );
        }

        taskRepository.delete(task);
    }
}