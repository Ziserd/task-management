package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.DashboardResponseDto;
import com.denizer.taskmanagement.entity.TaskPriority;
import com.denizer.taskmanagement.entity.TaskStatus;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.repository.*;
import com.denizer.taskmanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.denizer.taskmanagement.dto.UpcomingTaskDto;
import com.denizer.taskmanagement.entity.Task;
import java.util.List;

import com.denizer.taskmanagement.dto.RecentActivityDto;
import com.denizer.taskmanagement.entity.TaskActivity;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final TaskFavoriteRepository taskFavoriteRepository;
    private final UserRepository userRepository;
    private final TaskActivityRepository taskActivityRepository;

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

        List<UpcomingTaskDto> upcomingTasks = taskRepository
                .findByUserIdAndDueDateGreaterThanEqualAndStatusNotOrderByDueDateAsc(
                        userId,
                        today,
                        TaskStatus.COMPLETED
                )
                .stream()
                .map(this::mapToUpcomingTaskDto)
                .toList();

        long unreadNotifications =
                notificationRepository.countByUserIdAndReadFalse(userId);

        long favoriteTasks =
                taskFavoriteRepository.countByUserId(userId);

        List<RecentActivityDto> recentActivities = taskActivityRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToRecentActivityDto)
                .toList();

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
                .upcomingTasks(upcomingTasks)
                .recentActivities(recentActivities)
                .unreadNotifications(unreadNotifications)
                .favoriteTasks(favoriteTasks)
                .build();
    }

    private UpcomingTaskDto mapToUpcomingTaskDto(Task task) {
        return UpcomingTaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .build();
    }

    private RecentActivityDto mapToRecentActivityDto(TaskActivity activity) {

        Task task = taskRepository.findById(activity.getTaskId())
                .orElse(null);

        return RecentActivityDto.builder()
                .id(activity.getId())
                .taskId(activity.getTaskId())
                .taskTitle(task != null ? task.getTitle() : null)
                .userId(activity.getUser().getId())
                .userEmail(activity.getUser().getEmail())
                .action(activity.getAction())
                .oldValue(activity.getOldValue())
                .newValue(activity.getNewValue())
                .createdAt(activity.getCreatedAt())
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