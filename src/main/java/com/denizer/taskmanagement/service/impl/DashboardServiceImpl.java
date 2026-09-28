package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.DashboardResponseDto;
import com.denizer.taskmanagement.entity.TaskPriority;
import com.denizer.taskmanagement.entity.TaskStatus;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.repository.NotificationRepository;
import com.denizer.taskmanagement.repository.TaskFavoriteRepository;
import com.denizer.taskmanagement.repository.TaskRepository;
import com.denizer.taskmanagement.repository.UserRepository;
import com.denizer.taskmanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final TaskFavoriteRepository taskFavoriteRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardResponseDto getDashboard() {

        User user = getAuthenticatedUser();
        Long userId = user.getId();

        LocalDate today = LocalDate.now();

        long totalTasks = taskRepository.countByUserId(userId);

        long todoTasks =
                taskRepository.countByUserIdAndStatus(
                        userId,
                        TaskStatus.TODO
                );

        long inProgressTasks =
                taskRepository.countByUserIdAndStatus(
                        userId,
                        TaskStatus.IN_PROGRESS
                );

        long completedTasks =
                taskRepository.countByUserIdAndStatus(
                        userId,
                        TaskStatus.COMPLETED
                );

        long pendingTasks =
                taskRepository.countByUserIdAndStatus(
                        userId,
                        TaskStatus.PENDING
                );

        long overdueTasks =
                taskRepository.countByUserIdAndDueDateBeforeAndStatusNot(
                        userId,
                        today,
                        TaskStatus.COMPLETED
                );

        long highPriorityTasks =
                taskRepository.countByUserIdAndPriority(
                        userId,
                        TaskPriority.HIGH
                );

        long mediumPriorityTasks =
                taskRepository.countByUserIdAndPriority(
                        userId,
                        TaskPriority.MEDIUM
                );

        long lowPriorityTasks =
                taskRepository.countByUserIdAndPriority(
                        userId,
                        TaskPriority.LOW
                );

        long unreadNotifications =
                notificationRepository.countByUserIdAndReadFalse(userId);

        long favoriteTasks =
                taskFavoriteRepository.countByUserId(userId);

        return DashboardResponseDto.builder()
                .totalTasks(totalTasks)
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .overdueTasks(overdueTasks)
                .highPriorityTasks(highPriorityTasks)
                .mediumPriorityTasks(mediumPriorityTasks)
                .lowPriorityTasks(lowPriorityTasks)
                .unreadNotifications(unreadNotifications)
                .favoriteTasks(favoriteTasks)
                .build();
    }

    private User getAuthenticatedUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}