package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.TaskResponseDto;
import com.denizer.taskmanagement.entity.Task;
import com.denizer.taskmanagement.entity.TaskFavorite;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.repository.TaskFavoriteRepository;
import com.denizer.taskmanagement.repository.TaskRepository;
import com.denizer.taskmanagement.repository.UserRepository;
import com.denizer.taskmanagement.service.TaskFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.denizer.taskmanagement.exception.ConflictException;

@Service
@RequiredArgsConstructor
public class TaskFavoriteServiceImpl implements TaskFavoriteService {

    private final TaskFavoriteRepository taskFavoriteRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    public void addFavorite(Long taskId) {

        User user = getAuthenticatedUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (taskFavoriteRepository.existsByUserIdAndTaskId(
                user.getId(),
                taskId)) {
            throw new ConflictException("Task is already in favorites");
        }

        TaskFavorite favorite = new TaskFavorite();
        favorite.setUser(user);
        favorite.setTask(task);

        taskFavoriteRepository.save(favorite);
    }

    @Override
    public void removeFavorite(Long taskId) {

        User user = getAuthenticatedUser();

        TaskFavorite favorite = taskFavoriteRepository
                .findByUserIdAndTaskId(user.getId(), taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task is not in favorites"));

        taskFavoriteRepository.delete(favorite);
    }

    @Override
    public Page<TaskResponseDto> getMyFavorites(Pageable pageable) {

        User user = getAuthenticatedUser();

        return taskFavoriteRepository
                .findByUserId(user.getId(), pageable)
                .map(favorite -> mapToDto(favorite.getTask()));
    }

    private User getAuthenticatedUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private TaskResponseDto mapToDto(Task task) {
        return new TaskResponseDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getUser().getId(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getAssignedUser() != null
                        ? task.getAssignedUser().getId()
                        : null,
                task.getAssignedUser() != null
                        ? task.getAssignedUser().getEmail()
                        : null
        );
    }
}