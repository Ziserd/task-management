package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.TaskRequestDto;
import com.denizer.taskmanagement.dto.TaskResponseDto;
import com.denizer.taskmanagement.entity.Task;
import com.denizer.taskmanagement.entity.TaskPriority;
import com.denizer.taskmanagement.entity.TaskStatus;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.exception.ForbiddenException;
import com.denizer.taskmanagement.exception.ResourceNotFoundException;
import com.denizer.taskmanagement.repository.TaskRepository;
import com.denizer.taskmanagement.repository.UserRepository;
import com.denizer.taskmanagement.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

        return convertToResponseDto(savedTask);
    }

    @Override
    public Page<TaskResponseDto> getTasks(Pageable pageable) {

        User authenticatedUser = getAuthenticatedUser();

        Page<Task> tasks;

        if (authenticatedUser.getRole().name().equals("ADMIN")) {
            tasks = taskRepository.findAll(pageable);
        } else {
            tasks = taskRepository.findByUserId(
                    authenticatedUser.getId(),
                    pageable
            );
        }

        return tasks.map(this::convertToResponseDto);
    }

    @Override
    public TaskResponseDto getTaskById(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found."));

        User user = getAuthenticatedUser();

        if (!user.getRole().name().equals("ADMIN")
                && !task.getUser().getId().equals(user.getId())) {

            throw new ForbiddenException(
                    "You are not allowed to access this task."
            );
        }

        return convertToResponseDto(task);
    }

    @Override
    public List<TaskResponseDto> getAllTasks() {

        User authenticatedUser = getAuthenticatedUser();

        if (authenticatedUser.getRole().name().equals("ADMIN")) {

            return taskRepository.findAll()
                    .stream()
                    .map(this::convertToResponseDto)
                    .toList();
        }

        return taskRepository.findByUserId(authenticatedUser.getId())
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Override
    public Page<TaskResponseDto> getTasksByUserId(
            Long userId,
            Pageable pageable) {

        User authenticatedUser = getAuthenticatedUser();

        if (!authenticatedUser.getId().equals(userId)) {
            throw new ForbiddenException(
                    "You are not allowed to access this user's tasks."
            );
        }

        return taskRepository.findByUserId(userId, pageable)
                .map(this::convertToResponseDto);
    }

    @Override
    public Page<TaskResponseDto> getTasksByStatus(
            TaskStatus status,
            Pageable pageable) {

        User authenticatedUser = getAuthenticatedUser();

        Page<Task> tasks;

        if (authenticatedUser.getRole().name().equals("ADMIN")) {

            tasks = taskRepository.findByStatus(
                    status,
                    pageable
            );

        } else {

            tasks = taskRepository.findByUserIdAndStatus(
                    authenticatedUser.getId(),
                    status,
                    pageable
            );
        }

        return tasks.map(this::convertToResponseDto);
    }

    @Override
    public Page<TaskResponseDto> getTasksByPriority(
            TaskPriority priority,
            Pageable pageable) {

        User authenticatedUser = getAuthenticatedUser();

        Page<Task> tasks;

        if (authenticatedUser.getRole().name().equals("ADMIN")) {

            tasks = taskRepository.findByPriority(
                    priority,
                    pageable
            );

        } else {

            tasks = taskRepository.findByUserIdAndPriority(
                    authenticatedUser.getId(),
                    priority,
                    pageable
            );
        }

        return tasks.map(this::convertToResponseDto);
    }

    @Override
    public Page<TaskResponseDto> getTasksByStatusAndPriority(
            TaskStatus status,
            TaskPriority priority,
            Pageable pageable) {

        User authenticatedUser = getAuthenticatedUser();

        Page<Task> tasks;

        if (authenticatedUser.getRole().name().equals("ADMIN")) {

            tasks = taskRepository.findByStatusAndPriority(
                    status,
                    priority,
                    pageable
            );

        } else {

            tasks = taskRepository.findByUserIdAndStatusAndPriority(
                    authenticatedUser.getId(),
                    status,
                    priority,
                    pageable
            );
        }

        return tasks.map(this::convertToResponseDto);
    }

    @Override
    public TaskResponseDto updateTask(
            Long id,
            TaskRequestDto request) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found."));

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

        return convertToResponseDto(updatedTask);
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

    private TaskResponseDto convertToResponseDto(Task task) {

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

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));
    }
}