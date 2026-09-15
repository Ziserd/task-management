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
import com.denizer.taskmanagement.dto.TaskStatisticsResponseDto;
import com.denizer.taskmanagement.service.TaskActivityService;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskActivityService taskActivityService;

    public TaskServiceImpl(TaskRepository taskRepository,
                           UserRepository userRepository,
                           TaskActivityService taskActivityService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskActivityService = taskActivityService;
    }

    @Override
    public TaskStatisticsResponseDto getTaskStatistics() {

        User authenticatedUser = getAuthenticatedUser();

        long totalTasks;
        long todoTasks;
        long pendingTasks;
        long inProgressTasks;
        long completedTasks;
        long overdueTasks;

        if (authenticatedUser.getRole().name().equals("ADMIN")) {

            totalTasks = taskRepository.count();

            todoTasks = taskRepository.countByStatus(
                    TaskStatus.TODO
            );

            pendingTasks = taskRepository.countByStatus(
                    TaskStatus.PENDING
            );

            inProgressTasks = taskRepository.countByStatus(
                    TaskStatus.IN_PROGRESS
            );

            completedTasks = taskRepository.countByStatus(
                    TaskStatus.COMPLETED
            );

            overdueTasks = taskRepository.countByDueDateBeforeAndStatusNot(
                    LocalDate.now(),
                    TaskStatus.COMPLETED
            );

        } else {

            Long userId = authenticatedUser.getId();

            totalTasks = taskRepository.countByUserId(userId);

            todoTasks = taskRepository.countByUserIdAndStatus(
                    userId,
                    TaskStatus.TODO
            );

            pendingTasks = taskRepository.countByUserIdAndStatus(
                    userId,
                    TaskStatus.PENDING
            );

            inProgressTasks = taskRepository.countByUserIdAndStatus(
                    userId,
                    TaskStatus.IN_PROGRESS
            );

            completedTasks = taskRepository.countByUserIdAndStatus(
                    userId,
                    TaskStatus.COMPLETED
            );

            overdueTasks = taskRepository
                    .countByUserIdAndDueDateBeforeAndStatusNot(
                            userId,
                            LocalDate.now(),
                            TaskStatus.COMPLETED
                    );
        }

        return new TaskStatisticsResponseDto(
                totalTasks,
                todoTasks,
                pendingTasks,
                inProgressTasks,
                completedTasks,
                overdueTasks
        );
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

        taskActivityService.logActivity(
                savedTask.getId(),
                user.getId(),
                "TASK_CREATED",
                null,
                null
        );

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
    public Page<TaskResponseDto> getOverdueTasks(Pageable pageable) {

        User authenticatedUser = getAuthenticatedUser();

        LocalDate today = LocalDate.now();

        Page<Task> tasks;

        if (authenticatedUser.getRole().name().equals("ADMIN")) {

            tasks = taskRepository.findByDueDateBeforeAndStatusNot(
                    today,
                    TaskStatus.COMPLETED,
                    pageable
            );

        } else {

            tasks = taskRepository.findByUserIdAndDueDateBeforeAndStatusNot(
                    authenticatedUser.getId(),
                    today,
                    TaskStatus.COMPLETED,
                    pageable
            );
        }

        return tasks.map(this::convertToResponseDto);
    }

    @Override
    public Page<TaskResponseDto> searchTasks(
            String search,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueBefore,
            LocalDate dueAfter,
            Pageable pageable) {

        User authenticatedUser = getAuthenticatedUser();

        Page<Task> tasks;

        if (search == null) {
            search = "";
        }

        if (dueBefore == null) { dueBefore = LocalDate.of(9999, 12, 31); } if (dueAfter == null) { dueAfter = LocalDate.of(1, 1, 1); }

        if (authenticatedUser.getRole().name().equals("ADMIN")) {

            tasks = taskRepository.searchTasks(
                    search,
                    status,
                    priority,
                    dueBefore,
                    dueAfter,
                    pageable
            );

        } else {

            tasks = taskRepository.searchTasksByUserId(
                    authenticatedUser.getId(),
                    search,
                    status,
                    priority,
                    dueBefore,
                    dueAfter,
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

        TaskStatus oldStatus = task.getStatus();
        TaskPriority oldPriority = task.getPriority();
        String oldTitle = task.getTitle();
        String oldDescription = task.getDescription();
        LocalDate oldDueDate = task.getDueDate();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        Task updatedTask = taskRepository.save(task);

        if (oldPriority != request.getPriority()) {
            taskActivityService.logActivity(
                    updatedTask.getId(),
                    user.getId(),
                    "PRIORITY_CHANGED",
                    oldPriority.name(),
                    request.getPriority().name()
            );
        }

        if (oldStatus != request.getStatus()) {
            taskActivityService.logActivity(
                    updatedTask.getId(),
                    user.getId(),
                    "STATUS_CHANGED",
                    oldStatus.name(),
                    request.getStatus().name()
            );
        }

        if (!oldTitle.equals(request.getTitle())) {
            taskActivityService.logActivity(
                    updatedTask.getId(),
                    user.getId(),
                    "TASK_UPDATED",
                    oldTitle,
                    request.getTitle()
            );
        }

        if (!oldDescription.equals(request.getDescription())) {
            taskActivityService.logActivity(
                    updatedTask.getId(),
                    user.getId(),
                    "TASK_UPDATED",
                    oldDescription,
                    request.getDescription()
            );
        }

        if (!oldDueDate.equals(request.getDueDate())) {
            taskActivityService.logActivity(
                    updatedTask.getId(),
                    user.getId(),
                    "TASK_UPDATED",
                    oldDueDate.toString(),
                    request.getDueDate().toString()
            );
        }

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

        taskActivityService.logActivity(
                task.getId(),
                user.getId(),
                "TASK_DELETED",
                task.getTitle(),
                null
        );

        taskRepository.delete(task);
    }

    @Override
    public TaskResponseDto assignTask(Long taskId, Long userId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User authenticatedUser = getAuthenticatedUser();

        Long oldAssignedUserId = task.getAssignedUser() != null
                ? task.getAssignedUser().getId()
                : null;

        task.setAssignedUser(user);

        Task savedTask = taskRepository.save(task);

        taskActivityService.logActivity(
                savedTask.getId(),
                authenticatedUser.getId(),
                "TASK_ASSIGNED",
                oldAssignedUserId != null ? oldAssignedUserId.toString() : null,
                user.getId().toString()
        );

        return convertToResponseDto(savedTask);
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
                .assignedUserId(
                    task.getAssignedUser() != null
                        ? task.getAssignedUser().getId()
                        : null
                 )
                .assignedUserEmail(
                        task.getAssignedUser() != null
                                ? task.getAssignedUser().getEmail()
                                : null
                )
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

    @Override
    public Page<TaskResponseDto> getAssignedTasks(Pageable pageable) {

        User user = getAuthenticatedUser();

        Page<Task> tasks =
                taskRepository.findByAssignedUserId(user.getId(), pageable);

        return tasks.map(this::convertToResponseDto);
    }
}